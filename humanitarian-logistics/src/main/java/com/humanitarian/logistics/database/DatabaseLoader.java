package com.humanitarian.logistics.database;

import com.humanitarian.logistics.model.*;
import com.humanitarian.logistics.ui.Model;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * Utility class to load data from dev-ui's curated database.
 * Reads posts and comments from humanitarian_logistics_curated.db
 */
public class DatabaseLoader {
    
    // Dev-UI's curated database file - use relative path from project root
    // The database is located at: ../dev-ui/humanitarian_logistics_curated.db
    private static final String DEV_UI_DB_URL = "jdbc:sqlite:../dev-ui/humanitarian_logistics_curated.db";
    
    public static void loadOurDatabase(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        model.getPosts().clear();
        loadFromDevUIDatabase(model);
    }
    
    private static void loadFromDevUIDatabase(Model model) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection connection = DriverManager.getConnection(DEV_UI_DB_URL)) {
                loadPostsFromDevUI(connection, model);
                loadCommentsFromDevUI(connection, model);
                int postCount = model.getPosts().size();
                System.out.println("\n✓ LOADED FROM DEV-UI DATABASE");
                System.out.println("  Total posts: " + postCount);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error loading from dev-ui database: " + e.getMessage());
        }
    }
    
    private static void loadPostsFromDevUI(Connection connection, Model model) throws SQLException {
        String sql = "SELECT * FROM posts";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                FacebookPost post = new FacebookPost(
                    rs.getString("post_id"),
                    rs.getString("content"),
                    LocalDateTime.parse(rs.getString("created_at")),
                    rs.getString("author"),
                    rs.getString("source")
                );
                
                // Set sentiment if available
                String sentimentStr = rs.getString("sentiment");
                if (sentimentStr != null && !sentimentStr.isEmpty()) {
                    try {
                        Sentiment.SentimentType sentimentType = Sentiment.SentimentType.valueOf(sentimentStr);
                        double confidence = rs.getDouble("confidence");
                        post.setSentiment(new Sentiment(sentimentType, confidence, post.getContent()));
                    } catch (IllegalArgumentException e) {
                        // Invalid sentiment type, skip
                    }
                }
                
                // Set relief category if available
                String categoryStr = rs.getString("relief_category");
                if (categoryStr != null && !categoryStr.isEmpty()) {
                    try {
                        ReliefItem.Category category = ReliefItem.Category.valueOf(categoryStr);
                        post.setReliefItem(new ReliefItem(category, categoryStr, 1));
                    } catch (IllegalArgumentException e) {
                        // Invalid category, skip
                    }
                }
                
                // Set disaster keyword
                post.setDisasterKeyword(rs.getString("disaster_keyword"));
                
                // Try to set disaster type
                String keyword = rs.getString("disaster_keyword");
                if (keyword != null && !keyword.isEmpty()) {
                    DisasterType disaster = DisasterManager.getInstance().findDisasterType(keyword);
                    if (disaster != null) {
                        post.setDisasterType(disaster);
                    }
                }
                
                model.addPost(post);
            }
        }
    }
    
    private static void loadCommentsFromDevUI(Connection connection, Model model) throws SQLException {
        String sql = "SELECT * FROM comments";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String commentId = rs.getString("comment_id");
                String postId = rs.getString("post_id");
                String content = rs.getString("content");
                String author = rs.getString("author");
                LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
                
                // Find the post this comment belongs to
                Post targetPost = null;
                for (Post post : model.getPosts()) {
                    if (post.getPostId().equals(postId)) {
                        targetPost = post;
                        break;
                    }
                }
                
                if (targetPost != null) {
                    // Create comment
                    Comment comment = new Comment(commentId, postId, content, createdAt, author);
                    
                    // Set sentiment if available
                    String sentimentStr = rs.getString("sentiment");
                    if (sentimentStr != null && !sentimentStr.isEmpty()) {
                        try {
                            Sentiment.SentimentType sentimentType = Sentiment.SentimentType.valueOf(sentimentStr);
                            double confidence = rs.getDouble("confidence");
                            comment.setSentiment(new Sentiment(sentimentType, confidence, content));
                        } catch (IllegalArgumentException e) {
                            // Invalid sentiment type, skip
                        }
                    }
                    
                    // Set relief category if available
                    String categoryStr = rs.getString("relief_category");
                    if (categoryStr != null && !categoryStr.isEmpty()) {
                        try {
                            ReliefItem.Category category = ReliefItem.Category.valueOf(categoryStr);
                            comment.setReliefItem(new ReliefItem(category, categoryStr, 1));
                        } catch (IllegalArgumentException e) {
                            // Invalid category, skip
                        }
                    }
                    
                    // Add comment to post
                    targetPost.addComment(comment);
                }
            }
        }
    }
}
