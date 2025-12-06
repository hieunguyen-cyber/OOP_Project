package com.humanitarian.logistics.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Comment implements Serializable, Comparable<Comment> {
    private static final long serialVersionUID = 1L;

    private final String commentId;
    private final String postId;
    private final String content;
    private final LocalDateTime createdAt;
    private final String author;

    private Sentiment sentiment;
    private ReliefItem reliefItem;

    public Comment(String commentId, String postId, String content,
                   LocalDateTime createdAt, String author) {
        this.commentId = Objects.requireNonNull(commentId, "Comment ID cannot be null");
        this.postId = Objects.requireNonNull(postId, "Post ID cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
        this.author = Objects.requireNonNull(author, "Author cannot be null");
    }

    public String getCommentId() {
        return commentId;
    }

    public String getPostId() {
        return postId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getAuthor() {
        return author;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }

    public ReliefItem getReliefItem() {
        return reliefItem;
    }

    public void setReliefItem(ReliefItem reliefItem) {
        this.reliefItem = reliefItem;
    }

    @Override
    public int compareTo(Comment other) {
        return this.createdAt.compareTo(other.createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(commentId, comment.commentId) &&
               Objects.equals(postId, comment.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId, postId);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId='" + commentId + '\'' +
                ", author='" + author + '\'' +
                ", createdAt=" + createdAt +
                ", sentiment=" + sentiment +
                ", reliefItem=" + reliefItem +
                '}';
    }
}
