package com.example.demo.sentiment_analysis.comment.repository;

import com.example.demo.sentiment_analysis.ai.enumeration.SentimentType;
import com.example.demo.sentiment_analysis.comment.model.Comment;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepo extends MongoRepository<Comment, ObjectId> {
    void deleteByPostId(ObjectId postsId);
    void deleteByUserId(ObjectId userId);
    List<Comment> findByPostId(ObjectId postId);
    Slice<Comment> findByPostIdIn(List<ObjectId> publicPostIds, Pageable pageable);
    long countByPostId(ObjectId postId);

    // --- Added for Sentiment-Based Comment Recommendation feature ---

    // Used to check the 10-comment threshold for the logged-in user
    long countByUserId(ObjectId userId);

    // Used to get every post the user has commented on, with the sentiment of each of their comments
    List<Comment> findByUserId(ObjectId userId);

    // Used to find OTHER users' opposite-sentiment comments on the same post
    List<Comment> findByPostIdAndSentimentAndUserIdNot(ObjectId postId, SentimentType sentiment, ObjectId userId);
}
