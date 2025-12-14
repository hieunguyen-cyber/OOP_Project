package com.humanitarian.logistics.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.humanitarian.logistics.model.Comment;
import com.humanitarian.logistics.model.Post;
import com.humanitarian.logistics.model.ReliefItem;
import com.humanitarian.logistics.model.Sentiment;
import com.humanitarian.logistics.model.YouTubePost;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static final Object lock = new Object();
    
    private String dbUrl;
    private String dbFilePath;
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

    private String getDbFilePath() {
        String currentDir = System.getProperty("user.dir");
        String basePath;
        
        if (currentDir.endsWith("/humanitarian-logistics") || currentDir.endsWith("\\humanitarian-logistics")) {
            basePath = currentDir + "/data";
        } else if (currentDir.endsWith("/OOP_Project") || currentDir.endsWith("\\OOP_Project")) {
            basePath = currentDir + "/humanitarian-logistics/data";
        } else {
            File currentFile = new File(currentDir);
            File projectRoot = currentFile;
            while (projectRoot != null && !projectRoot.getName().equals("humanitarian-logistics")) {
                projectRoot = projectRoot.getParentFile();
            }
            
            if (projectRoot != null) {
                basePath = projectRoot.getAbsolutePath() + "/data";
            } else {
                basePath = currentDir + "/humanitarian-logistics/data";
            }
        }
        
        java.io.File dir = new java.io.File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        return basePath + "/humanitarian_logistics_user.db";
    }

    private void ensureConnection() throws ClassNotFoundException, SQLException {
        synchronized (lock) {
            if (!initialized) {
                Class.forName("org.sqlite.JDBC");
                dbFilePath = getDbFilePath();
                dbUrl = "jdbc:sqlite:" + dbFilePath;
                
                if (connection != null && !connection.isClosed()) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                    }
                }
                
                connection = DriverManager.getConnection(dbUrl);
                
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA busy_timeout = 30000");
                    stmt.execute("PRAGMA foreign_keys = ON");
                    stmt.execute("PRAGMA journal_mode = DELETE");
                    stmt.execute("PRAGMA synchronous = NORMAL");
                    stmt.execute("PRAGMA cache_size = -64000");
                    stmt.execute("PRAGMA temp_store = MEMORY");
                }
                
                // Attempt database recovery if corrupted
                try {
                    checkDatabaseIntegrity();
                } catch (SQLException e) {
                    System.err.println("⚠ Database integrity check failed, attempting recovery...");
                    recoverDatabase();
                }
                
                createTables();
                initialized = true;
            }
        }
    }
    
    private void checkDatabaseIntegrity() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeQuery("PRAGMA integrity_check");
        }
    }
    
    private void recoverDatabase() throws SQLException {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
        }
        
        // Remove corrupted database files
        File mainDb = new File(dbFilePath);
        File walFile = new File(dbFilePath + "-wal");
        File shmFile = new File(dbFilePath + "-shm");
        File journalFile = new File(dbFilePath + "-journal");
        
        if (mainDb.exists()) mainDb.delete();
        if (walFile.exists()) walFile.delete();
        if (shmFile.exists()) shmFile.delete();
        if (journalFile.exists()) journalFile.delete();
        
        // Reconnect with clean database
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        connection = DriverManager.getConnection(dbUrl);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA busy_timeout = 30000");
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute("PRAGMA journal_mode = DELETE");
            stmt.execute("PRAGMA synchronous = NORMAL");
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
                "disaster_keyword TEXT" + 
                ")";

        String commentsTable = "CREATE TABLE IF NOT EXISTS comments (" +
                "comment_id TEXT PRIMARY KEY," +
                "post_id TEXT," +
                "content TEXT," +
                "author TEXT," +
                "created_at TEXT," +
                "sentiment TEXT," +
                "confidence REAL," +
                "relief_category TEXT," +
                "disaster_type TEXT," +
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
            
            try {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                if (e.getMessage().contains("SQLITE_IOERR")) {
                    System.err.println("⚠ Database I/O error detected, attempting recovery...");
                    recoverDatabase();
                    // Retry the insert after recovery
                    try (PreparedStatement retryPstmt = connection.prepareStatement(sql)) {
                        retryPstmt.setString(1, post.getPostId());
                        retryPstmt.setString(2, post.getContent());
                        retryPstmt.setString(3, post.getAuthor());
                        retryPstmt.setString(4, post.getSource());
                        retryPstmt.setString(5, post.getCreatedAt().toString());
                        retryPstmt.setString(6, post.getSentiment() != null ? post.getSentiment().getType().toString() : null);
                        retryPstmt.setDouble(7, post.getSentiment() != null ? post.getSentiment().getConfidence() : 0);
                        retryPstmt.setString(8, reliefCategory);
                        retryPstmt.setString(9, post.getDisasterKeyword());
                        retryPstmt.executeUpdate();
                    }
                } else {
                    throw e;
                }
            }
        }

        for (Comment comment : post.getComments()) {
            saveComment(comment);
        }
    }

    public void saveComment(Comment comment) throws SQLException, ClassNotFoundException {
        ensureConnection();
        String sql = "INSERT OR REPLACE INTO comments VALUES(?,?,?,?,?,?,?,?,?)";
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
            pstmt.setString(9, comment.getDisasterType());
            
            try {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                if (e.getMessage().contains("SQLITE_IOERR")) {
                    System.err.println("⚠ Database I/O error detected, attempting recovery...");
                    recoverDatabase();
                    // Retry the insert after recovery
                    try (PreparedStatement retryPstmt = connection.prepareStatement(sql)) {
                        retryPstmt.setString(1, comment.getCommentId());
                        retryPstmt.setString(2, comment.getPostId());
                        retryPstmt.setString(3, comment.getContent());
                        retryPstmt.setString(4, comment.getAuthor());
                        retryPstmt.setString(5, comment.getCreatedAt().toString());
                        retryPstmt.setString(6, comment.getSentiment() != null ? comment.getSentiment().getType().toString() : null);
                        retryPstmt.setDouble(7, comment.getSentiment() != null ? comment.getSentiment().getConfidence() : 0);
                        retryPstmt.setString(8, commentReliefCategory);
                        retryPstmt.setString(9, comment.getDisasterType());
                        retryPstmt.executeUpdate();
                    }
                } else {
                    throw e;
                }
            }
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
                posts.add(post);
            }
        }
        return posts;
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
        String deleteSql = "DELETE FROM comments WHERE comment_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
            pstmt.setString(1, comment.getCommentId());
            pstmt.executeUpdate();
        }
        
        saveComment(comment);
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

    public void clearAllComments() throws SQLException, ClassNotFoundException {
        ensureConnection();
        String sql = "DELETE FROM comments";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            commit();
        }
    }

    /**
     * Load all comments directly from database with sentiment and category data
     * This bypasses the model and reads directly from DB
     */
    public java.util.List<Comment> getAllCommentsFromDatabase() throws SQLException, ClassNotFoundException {
        ensureConnection();
        java.util.List<Comment> comments = new java.util.ArrayList<>();
        String sql = "SELECT comment_id, post_id, content, author, created_at, sentiment, confidence, relief_category, disaster_type FROM comments";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String commentId = rs.getString("comment_id");
                String postId = rs.getString("post_id");
                String content = rs.getString("content");
                String author = rs.getString("author");
                java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(rs.getString("created_at"));
                
                Comment comment = new Comment(commentId, postId, content, createdAt, author);
                
                String sentimentStr = rs.getString("sentiment");
                if (sentimentStr != null && !sentimentStr.isEmpty()) {
                    try {
                        Sentiment.SentimentType type = Sentiment.SentimentType.valueOf(sentimentStr);
                        double confidence = rs.getDouble("confidence");
                        comment.setSentiment(new Sentiment(type, confidence, content));
                    } catch (IllegalArgumentException | SQLException e) {
                    }
                }
                
                String categoryStr = rs.getString("relief_category");
                if (categoryStr != null && !categoryStr.isEmpty()) {
                    try {
                        ReliefItem.Category category = ReliefItem.Category.valueOf(categoryStr);
                        comment.setReliefItem(new ReliefItem(category, "Database loaded", 3));
                    } catch (IllegalArgumentException e) {
                    }
                }
                
                String disasterType = rs.getString("disaster_type");
                if (disasterType != null && !disasterType.isEmpty()) {
                    comment.setDisasterType(disasterType);
                }
                
                comments.add(comment);
            }
        }
        
        return comments;
    }

    public void close() throws SQLException {
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
