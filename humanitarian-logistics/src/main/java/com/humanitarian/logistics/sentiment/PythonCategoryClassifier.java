package com.humanitarian.logistics.sentiment;

import com.humanitarian.logistics.model.ReliefItem;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PythonCategoryClassifier {
    private static final Logger LOGGER = Logger.getLogger(PythonCategoryClassifier.class.getName());
    private static final String API_ENDPOINT = "http://localhost:5001/classify_category";
    private static final String BATCH_ENDPOINT = "http://localhost:5001/classify_batch_category";
    private static final int TIMEOUT = 30000;

    public ReliefItem.Category classifyText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return ReliefItem.Category.FOOD;
        }

        try {

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("text", text);

            HttpURLConnection connection = (HttpURLConnection) new URL(API_ENDPOINT).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.toString().getBytes("utf-8"));
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }

                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                if (jsonResponse.has("category")) {
                    String category = jsonResponse.get("category").getAsString();
                    double confidence = jsonResponse.get("confidence").getAsDouble();
                    
                    try {
                        ReliefItem.Category.valueOf(category);
                    } catch (IllegalArgumentException e) {
                        LOGGER.log(Level.WARNING, "Invalid category '" + category + "' received from API. Using FOOD fallback.");
                        return ReliefItem.Category.FOOD;
                    }
                    
                    LOGGER.info("✓ Category classified via Python API: " + category + 
                               " (confidence: " + String.format("%.2f%%", confidence * 100) + ")");
                    LOGGER.info("  Model: " + jsonResponse.get("model").getAsString());
                    LOGGER.info("  Category name: " + jsonResponse.get("category_name").getAsString());
                    
                    return ReliefItem.Category.valueOf(category);
                }
            } else {
                LOGGER.log(Level.WARNING, "Python API error (code " + responseCode + "). Falling back to FOOD.");
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to connect to Python API: " + e.getMessage() + 
                      ". Make sure to run: python sentiment_api.py");
            LOGGER.log(Level.WARNING, "Falling back to default category: FOOD");
        }

        return ReliefItem.Category.FOOD;
    }

    public void classifyPost(com.humanitarian.logistics.model.Post post) {
        if (post.getReliefItem() == null) {
            ReliefItem.Category category = classifyText(post.getContent());
            if (category != null) {
                post.setReliefItem(new ReliefItem(category, "ML-classified (Keyword-based)", 3));
            }
        }
    }

    public boolean isApiAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:5001/health").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            
            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public String getApiStatus() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:5001/health").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            
            if (connection.getResponseCode() == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                return "Python API Status: " + jsonResponse.get("status").getAsString() + 
                       " | Category Model: " + jsonResponse.get("category_model").getAsString();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
