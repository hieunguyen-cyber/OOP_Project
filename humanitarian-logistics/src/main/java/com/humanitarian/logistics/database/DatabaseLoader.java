package com.humanitarian.logistics.database;

import com.humanitarian.logistics.model.*;
import com.humanitarian.logistics.ui.Model;
import java.sql.*;
import java.time.LocalDateTime;
import java.io.File;

/**
 * Utility class to load data from dev-ui's curated database.
 * Reads posts and comments from humanitarian_logistics_curated.db
 */
public class DatabaseLoader {
    
    // Method to get dev-ui database path - computed every time to pick up new files
    private static String getDevUIDbPath() {
        String currentDir = System.getProperty("user.dir");
        String dbPath;
        
        // Always resolve to absolute path from OOP_Project root
        if (currentDir.endsWith("humanitarian-logistics")) {
            // If running from humanitarian-logistics dir, go up to root then to dev-ui
            File rootDir = new File(currentDir).getParentFile();
            dbPath = rootDir.getAbsolutePath() + "/dev-ui/data/humanitarian_logistics_curated.db";
        } else {
            // If running from OOP_Project root
            dbPath = currentDir + "/dev-ui/data/humanitarian_logistics_curated.db";
        }
        
        return dbPath;
    }
    
    public static void loadOurDatabase(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        model.getPosts().clear();
        loadFromDevUIDatabase(model);
        
        // Save loaded data to user database to ensure persistence
        saveLoadedDataToUserDatabase(model);
    }
    
    private static void saveLoadedDataToUserDatabase(Model model) {
        DatabaseManager dbManager = null;
        try {
            // Always ensure data directory exists and use it
            String currentDir = System.getProperty("user.dir");
            String basePath;
            
            // Resolve correct base path - works whether running from root or from subdir
            File currentFile = new File(currentDir);
            File projectRoot = currentFile;
            
            // Navigate up to find 'humanitarian-logistics' folder
            while (projectRoot != null && !projectRoot.getName().equals("OOP_Project")) {
                projectRoot = projectRoot.getParentFile();
            }
            
            if (projectRoot != null) {
                // Found OOP_Project root
                basePath = projectRoot.getAbsolutePath() + "/humanitarian-logistics/data";
            } else {
                // Fallback: assume standard structure
                if (currentDir.endsWith("humanitarian-logistics")) {
                    basePath = currentDir + "/data";
                } else {
                    basePath = currentDir + "/humanitarian-logistics/data";
                }
            }
            
            // Ensure data directory exists
            File dataDir = new File(basePath);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                System.out.println("DEBUG: Created data directory: " + basePath);
            }
            
            // Delete old user database file and connections to reset it
            String dbFilePath = basePath + "/humanitarian_logistics_user.db";
            java.io.File userDbFile = new java.io.File(dbFilePath);
            if (userDbFile.exists()) {
                userDbFile.delete();
                // Also delete journal file if exists
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
                Thread.sleep(200); // Wait for file system to fully release
            }
            
            System.out.println("DEBUG: Database will be saved to: " + dbFilePath);
            System.out.println("DEBUG: Creating fresh DatabaseManager for user database...");
            // Create new DatabaseManager (will create fresh database)
            dbManager = new DatabaseManager();
            System.out.println("DEBUG: DatabaseManager created, now saving " + model.getPosts().size() + " posts");
            
            // Save all loaded posts and their comments
            for (Post post : model.getPosts()) {
                System.out.println("DEBUG: Saving post " + post.getPostId() + " with " + post.getComments().size() + " comments");
                dbManager.savePost(post);
            }
            dbManager.commit();
            System.out.println("✓ Data saved to user database (humanitarian_logistics_user.db)");
        } catch (Exception e) {
            System.err.println("Error saving to user database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (dbManager != null) {
                dbManager.close();
            }
        }
    }
    
    private static void loadFromDevUIDatabase(Model model) {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = getDevUIDbPath();
            String dbUrl = "jdbc:sqlite:" + dbPath;
            System.out.println("DEBUG: Connecting to: " + dbUrl);
            try (Connection connection = DriverManager.getConnection(dbUrl)) {
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
                YouTubePost post = new YouTubePost(
                    rs.getString("post_id"),
                    rs.getString("content"),
                    LocalDateTime.parse(rs.getString("created_at")),
                    rs.getString("author"),
                    rs.getString("source")
                );
                
                // Don't load sentiment from DB - force re-analysis with Python API
                // String sentimentStr = rs.getString("sentiment");
                // if (sentimentStr != null && !sentimentStr.isEmpty()) {
                //     ...
                // }
                
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
                    
                    // Don't load sentiment from DB - force re-analysis with Python API
                    // String sentimentStr = rs.getString("sentiment");
                    // if (sentimentStr != null && !sentimentStr.isEmpty()) {
                    //     ...
                    // }
                    
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
