package com.humanitarian.devui.model;

import java.time.LocalDateTime;

public class YouTubePost extends Post {
    private static final long serialVersionUID = 1L;

    private String channelId;
    private int likes;
    private int views;
    private DisasterType disasterType;

    public YouTubePost(String postId, String content, LocalDateTime createdAt,
                       String author, String channelId) {
        super(postId, content, createdAt, author, "YOUTUBE");
        this.channelId = channelId;
        this.likes = 0;
        this.views = 0;
        this.disasterType = null;

        this.setReliefItem(new ReliefItem(ReliefItem.Category.FOOD, "General Relief", 3));
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public DisasterType getDisasterType() {
        return disasterType;
    }

    public void setDisasterType(DisasterType disasterType) {
        this.disasterType = disasterType;
    }

    @Override
    public String toString() {
        return "YouTubePost{" +
                "postId='" + getPostId() + '\'' +
                ", content='" + getContent() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", channelId='" + channelId + '\'' +
                ", likes=" + likes +
                ", views=" + views +
                ", comments=" + getComments().size() +
                '}';
    }
}
