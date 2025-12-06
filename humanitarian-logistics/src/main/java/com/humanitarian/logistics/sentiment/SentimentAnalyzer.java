package com.humanitarian.logistics.sentiment;

import com.humanitarian.logistics.model.Sentiment;

public interface SentimentAnalyzer {
    
    Sentiment analyzeSentiment(String text);

    Sentiment[] analyzeSentimentBatch(String[] texts);

    String getModelName();

    void initialize();

    void shutdown();
}
