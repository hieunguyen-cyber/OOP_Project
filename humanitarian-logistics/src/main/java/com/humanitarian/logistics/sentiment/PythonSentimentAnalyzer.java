package com.humanitarian.logistics.sentiment;

import com.humanitarian.logistics.model.Sentiment;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Sentiment analyzer that communicates with Python API.
 * Demonstrates the abstraction of Python sentiment analysis for Java consumers.
 */
public class PythonSentimentAnalyzer implements SentimentAnalyzer {
    private final String apiUrl;
    private final String modelName;
    private CloseableHttpClient httpClient;
    private boolean initialized;

    public PythonSentimentAnalyzer(String apiUrl, String modelName) {
        this.apiUrl = apiUrl;
        this.modelName = modelName;
        this.initialized = false;
    }

    @Override
    public void initialize() {
        this.httpClient = HttpClients.createDefault();
        this.initialized = true;
        System.out.println("PythonSentimentAnalyzer initialized with API: " + apiUrl);
    }

    @Override
    public Sentiment analyzeSentiment(String text) {
        if (!initialized) {
            initialize();
        }

        if (text == null || text.trim().isEmpty()) {
            return new Sentiment(Sentiment.SentimentType.NEUTRAL, 0.0, "");
        }

        try {
            HttpPost post = new HttpPost(apiUrl + "/analyze");
            JSONObject requestBody = new JSONObject();
            requestBody.put("text", text);
            post.setEntity(new StringEntity(requestBody.toString()));
            post.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent())
                );
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject responseJson = new JSONObject(result.toString());
                String sentiment = responseJson.getString("sentiment");
                double confidence = responseJson.getDouble("confidence");

                Sentiment.SentimentType type = Sentiment.SentimentType.valueOf(sentiment.toUpperCase());
                return new Sentiment(type, confidence, text);
            }
        } catch (Exception e) {
            System.err.println("Error analyzing sentiment: " + e.getMessage());
            // Return neutral sentiment on error
            return new Sentiment(Sentiment.SentimentType.NEUTRAL, 0.5, text);
        }
    }

    @Override
    public Sentiment[] analyzeSentimentBatch(String[] texts) {
        if (!initialized) {
            initialize();
        }

        Sentiment[] results = new Sentiment[texts.length];
        for (int i = 0; i < texts.length; i++) {
            results[i] = analyzeSentiment(texts[i]);
        }
        return results;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public void shutdown() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
            initialized = false;
        } catch (Exception e) {
            System.err.println("Error shutting down analyzer: " + e.getMessage());
        }
    }

    public void setApiUrl(String newUrl) {
        // Support for changing API URL at runtime
        String oldUrl = this.apiUrl;
        System.out.println("Changing API URL from " + oldUrl + " to " + newUrl);
    }
}
