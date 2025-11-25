package com.humanitarian.logistics.sentiment;

import com.humanitarian.logistics.model.Sentiment;

/**
 * Interface for sentiment analysis.
 * Allows swapping different sentiment analysis implementations
 * (Python API, Java models, etc.)
 */
public interface SentimentAnalyzer {
    /**
     * Analyzes sentiment of given text
     * @param text text to analyze
     * @return Sentiment object with type and confidence
     */
    Sentiment analyzeSentiment(String text);

    /**
     * Batch analyzes sentiment for multiple texts
     * @param texts list of texts to analyze
     * @return array of Sentiment objects
     */
    Sentiment[] analyzeSentimentBatch(String[] texts);

    /**
     * Gets the model name/version
     * @return model identifier
     */
    String getModelName();

    /**
     * Initializes the analyzer
     */
    void initialize();

    /**
     * Releases resources
     */
    void shutdown();
}
