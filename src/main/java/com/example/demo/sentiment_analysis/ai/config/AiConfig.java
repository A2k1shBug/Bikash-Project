package com.example.demo.sentiment_analysis.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

@Configuration
public class AiConfig {
    // HTTP client used to call the externally deployed sentiment model (Render).
    // Render's free tier can cold-start slowly, so the read timeout is generous.
    @Bean
    public RestClient sentimentRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(90_000);
        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
    @Bean
    public RetryTemplate retryTemplate() {
        return new RetryTemplate();
    }
//        @Bean
//    public ResponseErrorHandler responseErrorHandler() {
//        return new ResponseErrorHandler() {
//            @Override
//            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
//                return response.getStatusCode().isError();
//            }
//
//            @Override
//            public void handleError(org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
//                throw new RestClientResponseException(
//                        "HTTP " + response.getStatusCode(),
//                        response.getStatusCode().value(),
//                        response.getStatusText(),
//                        response.getHeaders(),
//                        null,
//                        null
//                );
//            }
//        };
//    }
}