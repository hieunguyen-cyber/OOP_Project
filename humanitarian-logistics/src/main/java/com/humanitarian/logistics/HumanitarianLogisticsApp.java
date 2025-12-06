package com.humanitarian.logistics;

import com.humanitarian.logistics.ui.Model;
import com.humanitarian.logistics.ui.View;
import com.humanitarian.logistics.model.*;
import com.humanitarian.logistics.sentiment.PythonSentimentAnalyzer;
import com.humanitarian.logistics.database.DataPersistenceManager;

public class HumanitarianLogisticsApp {
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
