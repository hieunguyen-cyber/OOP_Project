package com.humanitarian.devui.database;

import com.humanitarian.devui.model.*;
import java.sql.*;
import java.util.*;
import java.io.File;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static final Object lock = new Object();
    
    private String dbUrl;
    private Connection connection;
    private boolean initialized = false;

    public DatabaseManager() {

    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    private String getDbUrl() {
        String currentDir = System.getProperty("user.dir");
        String basePath;
        
        File currentFile = new File(currentDir);
        File projectRoot = currentFile;
        
        while (projectRoot != null && !projectRoot.getName().equals("OOP_Project")) {
            projectRoot = projectRoot.getParentFile();
        }
        
        if (projectRoot != null) {

            basePath = projectRoot.getAbsolutePath() + "/dev-ui/data";
        } else {

            if (currentDir.endsWith("dev-ui")) {
                basePath = currentDir + "/data";
            } else {
                basePath = currentDir + "/dev-ui/data";
            }
        }
        
        java.io.File dir = new java.io.File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String dbPath = basePath + "/humanitarian_logistics_curated.db";
        return "jdbc:sqlite:" + dbPath;
    }

    private void ensureConnection() throws ClassNotFoundException, SQLException {
        synchronized (lock) {
            if (!initialized) {
                Class.forName("org.sqlite.JDBC");
                dbUrl = getDbUrl();
                
                if (connection != null && !connection.isClosed()) {
                    try {
                        connection.close();
                    } catch (SQLException e) {

                    }
                }
                
                String urlWithTimeout = dbUrl + "?timeout=30000&journal_mode=WAL";
                connection = DriverManager.getConnection(urlWithTimeout);
                
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                    stmt.execute("PRAGMA journal_mode = WAL");
                    stmt.execute("PRAGMA busy_timeout = 30000");
                }
                createTables();
                System.out.println("Database initialized: " + dbUrl);
                initialized = true;
            }
        }
    }

    private void createTables() throws SQLException {

        try (Statement stmt = connection.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS posts_old");
            stmt.execute("DROP TABLE IF EXISTS comments_old");
        }
        
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

    public void savePost(Post post) throws SQLException, ClassNotFoundException {
        ensureConnection();
        String sql = "INSERT OR REPLACE INTO posts VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, post.getPostId());
            pstmt.setString(2, post.getContent());
            pstmt.setString(3, post.getAuthor());
            pstmt.setString(4, post.getSource());
            pstmt.setString(5, post.getCreatedAt().toString());
            pstmt.setString(6, post.getSentiment() != null ? post.getSentiment().getType().toString() : null);
            pstmt.setDouble(7, post.getSentiment() != null ? post.getSentiment().getConfidence() : 0);
            String reliefCategory = null;
            if (post.getReliefItem() != null && post.getReliefItem().getCategory() != null) {
                reliefCategory = post.getReliefItem().getCategory().name();
            }
            pstmt.setString(8, reliefCategory);
            pstmt.setString(9, post.getDisasterKeyword());
            pstmt.executeUpdate();
        }
        
        for (Comment comment : post.getComments()) {
            saveComment(comment);
        }
    }

    public boolean isDuplicateLink(String postId) throws SQLException, ClassNotFoundException {
        ensureConnection();
        String sql = "SELECT COUNT(*) FROM posts WHERE post_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void deleteComment(String commentId) throws SQLException, ClassNotFoundException {
        ensureConnection();
        String sql = "DELETE FROM comments WHERE comment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, commentId);
            pstmt.executeUpdate();
            commit();
        }
    }

    public void updateComment(Comment comment) throws SQLException, ClassNotFoundException {
        ensureConnection();
        String sql = "UPDATE comments SET content = ?, sentiment = ?, confidence = ?, relief_category = ? WHERE comment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, comment.getContent());
            pstmt.setString(2, comment.getSentiment() != null ? comment.getSentiment().getType().toString() : null);
            pstmt.setDouble(3, comment.getSentiment() != null ? comment.getSentiment().getConfidence() : 0);
            String updateReliefCategory = null;
            if (comment.getReliefItem() != null && comment.getReliefItem().getCategory() != null) {
                updateReliefCategory = comment.getReliefItem().getCategory().name();
            }
            pstmt.setString(4, updateReliefCategory);
            pstmt.setString(5, comment.getCommentId());
            pstmt.executeUpdate();
            commit();
        }
    }

    public void commit() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            try {
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            } catch (SQLException e) {

                if (!e.getMessage().contains("auto-commit")) {
                    throw e;
                }
            }
        }
    }

    public void saveComment(Comment comment) throws SQLException, ClassNotFoundException {
        ensureConnection();
        String sql = "INSERT OR REPLACE INTO comments VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, comment.getCommentId());
            pstmt.setString(2, comment.getPostId());
            pstmt.setString(3, comment.getContent());
            pstmt.setString(4, comment.getAuthor());
            pstmt.setString(5, comment.getCreatedAt().toString());
            pstmt.setString(6, comment.getSentiment() != null ? comment.getSentiment().getType().toString() : null);
            pstmt.setDouble(7, comment.getSentiment() != null ? comment.getSentiment().getConfidence() : 0);
            String commentReliefCategory = null;
            if (comment.getReliefItem() != null && comment.getReliefItem().getCategory() != null) {
                commentReliefCategory = comment.getReliefItem().getCategory().name();
            }
            pstmt.setString(8, commentReliefCategory);
            pstmt.executeUpdate();
        }
    }

    public List<Post> getAllPosts() throws SQLException, ClassNotFoundException {
        ensureConnection();
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM posts";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Post post = reconstructPost(rs);
                
                String postId = post.getPostId();
                List<Comment> comments = getCommentsForPost(postId);
                for (Comment comment : comments) {
                    post.addComment(comment);
                }
                
                posts.add(post);
            }
        }
        return posts;
    }

    private List<Comment> getCommentsForPost(String postId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comments WHERE post_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String commentId = rs.getString("comment_id");
                    String content = rs.getString("content");
                    String author = rs.getString("author");
                    java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(rs.getString("created_at"));
                    
                    Comment comment = new Comment(commentId, postId, content, createdAt, author);
                    
                    String sentimentStr = rs.getString("sentiment");
                    if (sentimentStr != null) {
                        Sentiment.SentimentType type = Sentiment.SentimentType.valueOf(sentimentStr);
                        double confidence = rs.getDouble("confidence");
                        comment.setSentiment(new Sentiment(type, confidence, content));
                    }
                    
                    String reliefCategory = rs.getString("relief_category");
                    if (reliefCategory != null) {
                        ReliefItem.Category category = ReliefItem.Category.valueOf(reliefCategory);
                        comment.setReliefItem(new ReliefItem(category, "Database loaded", 3));
                    }
                    
                    comments.add(comment);
                }
            }
        }
        return comments;
    }

    private Post reconstructPost(ResultSet rs) throws SQLException {
        String postId = rs.getString("post_id");
        String content = rs.getString("content");
        String author = rs.getString("author");

        java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(rs.getString("created_at"));

        YouTubePost post = new YouTubePost(postId, content, createdAt, author, "");

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

    public void reset() {
        try {

            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException e) {

                }
                connection = null;
            }
        } catch (Exception e) {
            System.err.println("Error closing connection during reset: " + e.getMessage());
        }
        
        initialized = false;
        dbUrl = null;
    }
}
