package com.humanitarian.devui.model;

import java.io.Serializable;
import java.util.Objects;

public class Sentiment implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum SentimentType {
        POSITIVE, NEGATIVE, NEUTRAL
    }

    private final SentimentType type;
    private final double confidence;
    private final String rawText;

    public Sentiment(SentimentType type, double confidence, String rawText) {
        if (confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }
        this.type = Objects.requireNonNull(type, "Sentiment type cannot be null");
        this.confidence = confidence;
        this.rawText = Objects.requireNonNull(rawText, "Raw text cannot be null");
    }

    public SentimentType getType() {
        return type;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getRawText() {
        return rawText;
    }

    public boolean isPositive() {
        return type == SentimentType.POSITIVE;
    }

    public boolean isNegative() {
        return type == SentimentType.NEGATIVE;
    }

    public boolean isNeutral() {
        return type == SentimentType.NEUTRAL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sentiment sentiment = (Sentiment) o;
        return Double.compare(sentiment.confidence, confidence) == 0 &&
               type == sentiment.type &&
               Objects.equals(rawText, sentiment.rawText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, confidence, rawText);
    }

    @Override
    public String toString() {
        return "Sentiment{" +
                "type=" + type +
                ", confidence=" + confidence +
                '}';
    }
}
