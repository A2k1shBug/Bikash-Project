package com.example.demo.sentiment_analysis.ai.service;

import com.example.demo.sentiment_analysis.ai.SentimentResult;
import com.example.demo.sentiment_analysis.ai.enumeration.SentimentType;
import com.example.demo.sentiment_analysis.exception.SentimentAnalysisException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class SentimentAiService {
    private final RestClient restClient;
    private final String predictUrl;

    public SentimentAiService(
            RestClient restClient,
            @Value("${sentiment.api.url:https://sentiment-analysis-model-br1d.onrender.com/predict}") String predictUrl) {
        this.restClient = restClient;
        this.predictUrl = predictUrl;
    }

    public SentimentResult analyze(String text) {
        try {
            // Validate input
            if (text == null || text.trim().isEmpty()) {
                return new SentimentResult(SentimentType.NEUTRAL, 0.0);
            }

            JsonNode response = restClient.post()
                    .uri(predictUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Map.of("review", text))
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null || response.isNull()) {
                throw new IllegalStateException("Empty response from sentiment API");
            }

            // Only the sentiment and the confidence score are used; any other fields are ignored.
            SentimentType sentiment = parseSentiment(findField(response,
                    "sentiment", "label", "prediction", "predicted_sentiment", "predicted_label"));
            Double confidence = parseConfidence(findField(response,
                    "confidence", "confidence_score", "score", "probability"));

            return new SentimentResult(sentiment, confidence);

        } catch (Exception e) {
            log.error("Sentiment analysis failed for text: {}", text, e);
            throw new SentimentAnalysisException("Sentiment analysis unavailable");
        }
    }

    // Looks for the first matching field at the top level, then inside a nested object
    // (e.g. {"result": {...}} / {"data": {...}} / {"prediction": {...}}).
    private JsonNode findField(JsonNode root, String... names) {
        for (String name : names) {
            JsonNode node = root.get(name);
            if (node != null && !node.isNull() && !node.isContainerNode()) {
                return node;
            }
        }
        for (JsonNode child : root) {
            if (child.isObject()) {
                for (String name : names) {
                    JsonNode node = child.get(name);
                    if (node != null && !node.isNull() && !node.isContainerNode()) {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    private SentimentType parseSentiment(JsonNode node) {
        if (node == null) {
            throw new IllegalStateException("No sentiment field in sentiment API response");
        }
        String value = node.asText().trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "POSITIVE", "POS" -> SentimentType.POSITIVE;
            case "NEGATIVE", "NEG" -> SentimentType.NEGATIVE;
            case "NEUTRAL", "NEU" -> SentimentType.NEUTRAL;
            default -> throw new IllegalStateException("Unexpected sentiment value: " + value);
        };
    }

    // The UI shows Math.round(confidence * 100) + "%", so confidence is kept on a 0.0-1.0 scale.
    // If the model reports a percentage (e.g. 87.5), it is converted.
    private Double parseConfidence(JsonNode node) {
        if (node == null) {
            throw new IllegalStateException("No confidence field in sentiment API response");
        }
        double value = node.isNumber() ? node.asDouble()
                : Double.parseDouble(node.asText().trim().replace("%", ""));
        if (value > 1.0) {
            value = value / 100.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}