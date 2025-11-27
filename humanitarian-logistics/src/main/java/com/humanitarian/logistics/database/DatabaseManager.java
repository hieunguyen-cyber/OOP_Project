package com.humanitarian.logistics.database;

import com.humanitarian.logistics.model.*;
import java.sql.*;
import java.util.*;

/**
 * Database manager for storing and retrieving posts and comments.
 * Demonstrates abstraction and encapsulation of database operations.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:humanitarian_logistics_user.db";
    private Connection connection;

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            System.out.println("Database initialized");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String postsTable = "CREATE TABLE IF NOT EXISTS posts (" +
                "post_id TEXT PRIMARY KEY," +
                "content TEXT," +
                "author TEXT," +
                "source TEXT," +
                "created_at TEXT," +
                "sentiment TEXT," +
                "confidence REAL," +
                "relief_category TEXT," +
                "disaster_keyword TEXT)";

        String commentsTable = "CREATE TABLE IF NOT EXISTS comments (" +
                "comment_id TEXT PRIMARY KEY," +
                "post_id TEXT," +
                "content TEXT," +
                "author TEXT," +
                "created_at TEXT," +
                "sentiment TEXT," +
                "confidence REAL," +
                "relief_category TEXT," +
                "FOREIGN KEY(post_id) REFERENCES posts(post_id))";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(postsTable);
            stmt.execute(commentsTable);
        }
    }

    public void savePost(Post post) throws SQLException {
        String sql = "INSERT OR REPLACE INTO posts VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, post.getPostId());
            pstmt.setString(2, post.getContent());
            pstmt.setString(3, post.getAuthor());
            pstmt.setString(4, post.getSource());
            pstmt.setString(5, post.getCreatedAt().toString());
            pstmt.setString(6, post.getSentiment() != null ? post.getSentiment().getType().toString() : null);
            pstmt.setDouble(7, post.getSentiment() != null ? post.getSentiment().getConfidence() : 0);
            pstmt.setString(8, post.getReliefItem() != null ? post.getReliefItem().getCategory().name() : null);
            pstmt.setString(9, post.getDisasterKeyword());
            pstmt.executeUpdate();
        }

        // Save all comments associated with this post
        for (Comment comment : post.getComments()) {
            saveComment(comment);
        }
    }

    public void saveComment(Comment comment) throws SQLException {
        String sql = "INSERT OR REPLACE INTO comments VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, comment.getCommentId());
            pstmt.setString(2, comment.getPostId());
            pstmt.setString(3, comment.getContent());
            pstmt.setString(4, comment.getAuthor());
            pstmt.setString(5, comment.getCreatedAt().toString());
            pstmt.setString(6, comment.getSentiment() != null ? comment.getSentiment().getType().toString() : null);
            pstmt.setDouble(7, comment.getSentiment() != null ? comment.getSentiment().getConfidence() : 0);
            pstmt.setString(8, comment.getReliefItem() != null ? comment.getReliefItem().getCategory().name() : null);
            pstmt.executeUpdate();
        }
    }

    public List<Post> getAllPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM posts";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Post post = reconstructPost(rs);
                posts.add(post);
            }
        }
        return posts;
    }

    public void deleteComment(String commentId) throws SQLException {
        String sql = "DELETE FROM comments WHERE comment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, commentId);
            pstmt.executeUpdate();
            commit();
        }
    }

    public void updateComment(Comment comment) throws SQLException {
        String sql = "UPDATE comments SET content = ?, sentiment = ?, confidence = ?, relief_category = ? WHERE comment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, comment.getContent());
            pstmt.setString(2, comment.getSentiment() != null ? comment.getSentiment().getType().toString() : null);
            pstmt.setDouble(3, comment.getSentiment() != null ? comment.getSentiment().getConfidence() : 0);
            pstmt.setString(4, comment.getReliefItem() != null ? comment.getReliefItem().getCategory().name() : null);
            pstmt.setString(5, comment.getCommentId());
            pstmt.executeUpdate();
            commit();
        }
    }

    private Post reconstructPost(ResultSet rs) throws SQLException {
        String postId = rs.getString("post_id");
        String content = rs.getString("content");
        String author = rs.getString("author");

        java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(rs.getString("created_at"));

        FacebookPost post = new FacebookPost(postId, content, createdAt, author, "");

        String sentimentStr = rs.getString("sentiment");
        if (sentimentStr != null) {
            Sentiment.SentimentType type = Sentiment.SentimentType.valueOf(sentimentStr);
            double confidence = rs.getDouble("confidence");
            post.setSentiment(new Sentiment(type, confidence, content));
        }

        String reliefCategory = rs.getString("relief_category");
        if (reliefCategory != null) {
            ReliefItem.Category category = ReliefItem.Category.valueOf(reliefCategory);
            post.setReliefItem(new ReliefItem(category, "Database loaded", 3));
        }

        post.setDisasterKeyword(rs.getString("disaster_keyword"));

        return post;
    }

    public void clearAllComments() throws SQLException {
        String sql = "DELETE FROM comments";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            commit();
        }
    }

    public void commit() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.commit();
        }
    }

    public void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed");
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error during database cleanup: " + e.getMessage());
            }
        }
    }
}
