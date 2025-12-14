package com.humanitarian.logistics.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import com.humanitarian.logistics.model.Comment;
import com.humanitarian.logistics.model.DisasterManager;
import com.humanitarian.logistics.model.DisasterType;
import com.humanitarian.logistics.model.Post;
import com.humanitarian.logistics.model.ReliefItem;
import com.humanitarian.logistics.model.Sentiment;
import com.humanitarian.logistics.model.YouTubePost;
import com.humanitarian.logistics.ui.Model;

public class DatabaseLoader {
    
    private static String getDevUIDbPath() {
        String currentDir = System.getProperty("user.dir");
        String dbPath;
        
        if (currentDir.endsWith("humanitarian-logistics")) {
            dbPath = currentDir + "/data/humanitarian_logistics_curated.db";
        } else {
            dbPath = currentDir + "/humanitarian-logistics/data/humanitarian_logistics_curated.db";
        }
        
        return dbPath;
    }
    
    public static void loadOurDatabase(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        model.getPosts().clear();
        loadFromDevUIDatabase(model);
        
        saveLoadedDataToUserDatabase(model);
    }
    
    private static void saveLoadedDataToUserDatabase(Model model) {
        DatabaseManager dbManager = null;
        try {
            String currentDir = System.getProperty("user.dir");
            String basePath;
            
            File currentFile = new File(currentDir);
            File projectRoot = currentFile;
            
            while (projectRoot != null && !projectRoot.getName().equals("OOP_Project")) {
                projectRoot = projectRoot.getParentFile();
            }
            
            if (projectRoot != null) {
                basePath = projectRoot.getAbsolutePath() + "/humanitarian-logistics/data";
            } else {
                if (currentDir.endsWith("humanitarian-logistics")) {
                    basePath = currentDir + "/data";
                } else {
                    basePath = currentDir + "/humanitarian-logistics/data";
                }
            }
            
            File dataDir = new File(basePath);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            String dbFilePath = basePath + "/humanitarian_logistics_user.db";
            java.io.File userDbFile = new java.io.File(dbFilePath);
            if (userDbFile.exists()) {
                userDbFile.delete();
                java.io.File journalFile = new java.io.File(dbFilePath + "-journal");
                if (journalFile.exists()) {
                    journalFile.delete();
                }
                java.io.File shmFile = new java.io.File(dbFilePath + "-shm");
                if (shmFile.exists()) {
                    shmFile.delete();
                }
                java.io.File walFile = new java.io.File(dbFilePath + "-wal");
                if (walFile.exists()) {
                    walFile.delete();
                }
                Thread.sleep(200);
            }

            dbManager = new DatabaseManager();
            
            for (Post post : model.getPosts()) {
                dbManager.savePost(post);
            }
            try {
                dbManager.commit();
            } catch (SQLException e) {
            }
        } catch (Exception e) {
            System.err.println("Error saving to user database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (dbManager != null) {
                try {
                    dbManager.close();
                } catch (SQLException e) {
                }
            }
        }
    }
    
    private static void loadFromDevUIDatabase(Model model) {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = getDevUIDbPath();
            
            File dbFile = new File(dbPath);
            if (!dbFile.exists()) {
                System.err.println("Curated database not found at: " + dbPath);
                return;
            }
            
            String dbUrl = "jdbc:sqlite:" + dbPath;
            try (Connection connection = DriverManager.getConnection(dbUrl)) {
                loadPostsFromDevUI(connection, model);
                loadCommentsFromDevUI(connection, model);
                int postCount = model.getPosts().size();
                System.out.println("Loaded from dev-ui database: " + postCount + " posts");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error loading from curated database: " + e.getMessage());
        }
    }
    
    private static void loadPostsFromDevUI(Connection connection, Model model) throws SQLException {
        String sql = "SELECT * FROM posts";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                YouTubePost post = new YouTubePost(
                    rs.getString("post_id"),
                    rs.getString("content"),
                    LocalDateTime.parse(rs.getString("created_at")),
                    rs.getString("author"),
                    rs.getString("source")
                );
                
                String categoryStr = rs.getString("relief_category");
                if (categoryStr != null && !categoryStr.isEmpty()) {
                    try {
                        ReliefItem.Category category = ReliefItem.Category.valueOf(categoryStr);
                        post.setReliefItem(new ReliefItem(category, categoryStr, 1));
                    } catch (IllegalArgumentException e) {
                    }
                }
                
                post.setDisasterKeyword(rs.getString("disaster_keyword"));
                
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
                
                Post targetPost = null;
                for (Post post : model.getPosts()) {
                    if (post.getPostId().equals(postId)) {
                        targetPost = post;
                        break;
                    }
                }
                
                if (targetPost != null) {
                    Comment comment = new Comment(commentId, postId, content, createdAt, author);
                    
                    String sentimentStr = rs.getString("sentiment");
                    if (sentimentStr != null && !sentimentStr.isEmpty()) {
                        try {
                            Sentiment.SentimentType sentimentType = Sentiment.SentimentType.valueOf(sentimentStr);
                            double confidence = rs.getDouble("confidence");
                            comment.setSentiment(new Sentiment(sentimentType, confidence, content));
                        } catch (IllegalArgumentException | SQLException e) {
                        }
                    }
                    
                    String categoryStr = rs.getString("relief_category");
                    if (categoryStr != null && !categoryStr.isEmpty() && !categoryStr.equals("N/A")) {
                        try {
                            ReliefItem.Category category = ReliefItem.Category.valueOf(categoryStr);
                            comment.setReliefItem(new ReliefItem(category, categoryStr, 1));
                        } catch (IllegalArgumentException e) {
                        }
                    }
                    
                    String disasterType = rs.getString("disaster_type");
                    if (disasterType != null && !disasterType.isEmpty()) {
                        comment.setDisasterType(disasterType);
                    }
                    
                    targetPost.addComment(comment);
                }
            }
        }
    }
}
