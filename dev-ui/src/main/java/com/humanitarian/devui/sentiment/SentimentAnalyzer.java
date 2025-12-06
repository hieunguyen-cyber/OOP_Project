package com.humanitarian.devui.sentiment;

import com.humanitarian.devui.model.Sentiment;

public interface SentimentAnalyzer {
    
    Sentiment analyzeSentiment(String text);

    Sentiment[] analyzeSentimentBatch(String[] texts);

    String getModelName();

    void initialize();

    void shutdown();
}
