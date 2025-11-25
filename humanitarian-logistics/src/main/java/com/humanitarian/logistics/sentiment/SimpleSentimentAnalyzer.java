package com.humanitarian.logistics.sentiment;

import com.humanitarian.logistics.model.Sentiment;

/**
 * Mock/Fallback sentiment analyzer for when Python API is unavailable.
 * Demonstrates the Strategy pattern - can be swapped for PythonSentimentAnalyzer.
 */
public class SimpleSentimentAnalyzer implements SentimentAnalyzer {
    private static final String[] POSITIVE_WORDS = {
            "good", "great", "excellent", "happy", "love", "thank", "thanks",
            "appreciate", "support", "help", "aid", "relief", "better", "improved",
            "success", "wonderful", "fantastic", "amazing"
    };

    private static final String[] NEGATIVE_WORDS = {
            "bad", "poor", "terrible", "sad", "hate", "angry", "upset", "frustrated",
            "struggle", "difficult", "problem", "issue", "lack", "missing", "needed",
            "fail", "failure", "disaster", "crisis", "emergency"
    };

    @Override
    public Sentiment analyzeSentiment(String text) {
        if (text == null || text.isEmpty()) {
            return new Sentiment(Sentiment.SentimentType.NEUTRAL, 0.0, "");
        }

        String lowerText = text.toLowerCase();
        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : POSITIVE_WORDS) {
            if (lowerText.contains(word)) {
                positiveCount++;
            }
        }

        for (String word : NEGATIVE_WORDS) {
            if (lowerText.contains(word)) {
                negativeCount++;
            }
        }

        Sentiment.SentimentType type;
        double confidence;

        if (positiveCount > negativeCount) {
            type = Sentiment.SentimentType.POSITIVE;
            confidence = Math.min(0.99, 0.5 + (positiveCount * 0.1));
        } else if (negativeCount > positiveCount) {
            type = Sentiment.SentimentType.NEGATIVE;
            confidence = Math.min(0.99, 0.5 + (negativeCount * 0.1));
        } else {
            type = Sentiment.SentimentType.NEUTRAL;
            confidence = 0.5;
        }

        return new Sentiment(type, confidence, text);
    }

    @Override
    public Sentiment[] analyzeSentimentBatch(String[] texts) {
        Sentiment[] results = new Sentiment[texts.length];
        for (int i = 0; i < texts.length; i++) {
            results[i] = analyzeSentiment(texts[i]);
        }
        return results;
    }

    @Override
    public String getModelName() {
        return "SimpleSentimentAnalyzer-v1.0";
    }

    @Override
    public void initialize() {
        System.out.println("SimpleSentimentAnalyzer initialized");
    }

    @Override
    public void shutdown() {
        System.out.println("SimpleSentimentAnalyzer shutdown");
    }
}
