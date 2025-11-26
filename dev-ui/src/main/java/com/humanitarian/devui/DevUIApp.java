package com.humanitarian.devui;

import com.humanitarian.devui.ui.Model;
import com.humanitarian.devui.ui.View;
import com.humanitarian.devui.model.*;
import com.humanitarian.devui.database.DatabaseManager;
import com.humanitarian.devui.database.DataPersistenceManager;
import com.humanitarian.devui.sentiment.SimpleSentimentAnalyzer;

/**
 * Main application entry point.
 * Demonstrates proper application initialization and MVC pattern usage.
 */
public class DevUIApp {
    public static void main(String[] args) {
        try {
            // Initialize DisasterManager with disaster types
            DisasterManager disasterManager = DisasterManager.getInstance();
            
            // Load persisted custom disasters before creating Model
            DataPersistenceManager persistenceManager = new DataPersistenceManager();
            persistenceManager.loadDisasters(disasterManager);
            
            // Initialize MVC components
            Model model = new Model();

            // Initialize with default sentiment analyzer
            SimpleSentimentAnalyzer analyzer = new SimpleSentimentAnalyzer();
            model.setSentimentAnalyzer(analyzer);

            // Load saved data from database on startup
            try {
                DatabaseManager dbManager = new DatabaseManager();
                java.util.List<Post> savedPosts = dbManager.getAllPosts();
                if (!savedPosts.isEmpty()) {
                    System.out.println("Loading " + savedPosts.size() + " saved posts from database...");
                    for (Post post : savedPosts) {
                        model.addPost(post);
                    }
                    System.out.println("✓ Loaded " + savedPosts.size() + " posts from database");
                }
                dbManager.close();
            } catch (Exception e) {
                System.err.println("Note: Could not load from database: " + e.getMessage());
            }

            // Start the UI
            javax.swing.SwingUtilities.invokeLater(() -> {
                new View(model);
            });

            System.out.println("Application started successfully");
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
