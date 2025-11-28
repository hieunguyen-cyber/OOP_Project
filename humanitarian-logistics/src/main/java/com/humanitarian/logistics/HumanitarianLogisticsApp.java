package com.humanitarian.logistics;

import com.humanitarian.logistics.ui.Model;
import com.humanitarian.logistics.ui.View;
import com.humanitarian.logistics.model.*;
import com.humanitarian.logistics.sentiment.PythonSentimentAnalyzer;
import com.humanitarian.logistics.database.DataPersistenceManager;

/**
 * Main application entry point.
 * Demonstrates proper application initialization and MVC pattern usage.
 */
public class HumanitarianLogisticsApp {
    public static void main(String[] args) {
        try {
            // Initialize DisasterManager with disaster types
            DisasterManager disasterManager = DisasterManager.getInstance();
            
            // Load persisted custom disaster types
            DataPersistenceManager persistenceManager = new DataPersistenceManager();
            persistenceManager.loadDisasters(disasterManager);
            
            // Initialize MVC components
            Model model = new Model();

            // Initialize with Python sentiment analyzer (Vietnamese + English support)
            PythonSentimentAnalyzer analyzer = new PythonSentimentAnalyzer(
                "http://localhost:5001",
                "xlm-roberta-large-xnli (Vietnamese + English)"
            );
            model.setSentimentAnalyzer(analyzer);

            // Note: Sample data is NOT loaded automatically
            // User can load data using:
            // 1. "Use Sample Data" button - loads sample posts
            // 2. "Use Our Database" button - loads curated database

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
