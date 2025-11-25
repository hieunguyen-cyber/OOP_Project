package com.humanitarian.logistics.model;

import java.time.LocalDateTime;

/**
 * Concrete implementation of Post for Facebook.
 * Demonstrates inheritance and polymorphism.
 */
public class FacebookPost extends Post {
    private static final long serialVersionUID = 1L;

    private String pageId;
    private int likes;
    private int shares;
    private DisasterType disasterType;

    public FacebookPost(String postId, String content, LocalDateTime createdAt,
                        String author, String pageId) {
        super(postId, content, createdAt, author, "FACEBOOK");
        this.pageId = pageId;
        this.likes = 0;
        this.shares = 0;
        this.disasterType = null;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        if (likes < 0) {
            throw new IllegalArgumentException("Likes cannot be negative");
        }
        this.likes = likes;
    }

    public int getShares() {
        return shares;
    }

    public void setShares(int shares) {
        if (shares < 0) {
            throw new IllegalArgumentException("Shares cannot be negative");
        }
        this.shares = shares;
    }

    public DisasterType getDisasterType() {
        return disasterType;
    }

    public void setDisasterType(DisasterType disasterType) {
        this.disasterType = disasterType;
    }

    @Override
    public String toString() {
        return "FacebookPost{" +
                "postId='" + getPostId() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", likes=" + likes +
                ", shares=" + shares +
                ", disasterType=" + (disasterType != null ? disasterType.getName() : "N/A") +
                ", sentiment=" + getSentiment() +
                '}';
    }
}
