package com.humanitarian.devui.model;

import java.time.LocalDateTime;
import java.util.*;

public class DataEntry {
    private String id;
    private String facebookLink;
    private String content;
    private String author;
    private String disasterKeyword;
    private String reliefCategory;
    private String sentimentType;
    private double confidence;
    private LocalDateTime createdAt;

    public DataEntry() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getFacebookLink() { return facebookLink; }
    public void setFacebookLink(String facebookLink) { this.facebookLink = facebookLink; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getDisasterKeyword() { return disasterKeyword; }
    public void setDisasterKeyword(String disasterKeyword) { this.disasterKeyword = disasterKeyword; }
    public String getReliefCategory() { return reliefCategory; }
    public void setReliefCategory(String reliefCategory) { this.reliefCategory = reliefCategory; }
    public String getSentimentType() { return sentimentType; }
    public void setSentimentType(String sentimentType) { this.sentimentType = sentimentType; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "DataEntry{" + "id='" + id + '\'' + ", link='" + facebookLink + '\'' + 
               ", keyword='" + disasterKeyword + '\'' + ", category='" + reliefCategory + '\'' + '}';
    }
}
