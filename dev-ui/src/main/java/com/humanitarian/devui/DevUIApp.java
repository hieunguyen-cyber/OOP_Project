package com.humanitarian.devui;

import com.humanitarian.devui.ui.Model;
import com.humanitarian.devui.ui.View;
import com.humanitarian.devui.model.*;
import com.humanitarian.devui.database.DatabaseManager;
import com.humanitarian.devui.database.DataPersistenceManager;
import com.humanitarian.devui.sentiment.PythonSentimentAnalyzer;

public class DevUIApp {
    public static void main(String[] args) {
        try {

            DisasterManager disasterManager = DisasterManager.getInstance();
            
            DataPersistenceManager persistenceManager = new DataPersistenceManager();
            persistenceManager.loadDisasters(disasterManager);
            
            Model model = new Model();

            PythonSentimentAnalyzer analyzer = new PythonSentimentAnalyzer(
                "http://localhost:5001", 
                "xlm-roberta-large-xnli (Vietnamese + English)"
            );
            model.setSentimentAnalyzer(analyzer);

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
