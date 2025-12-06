package com.humanitarian.logistics.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class Post implements Serializable, Comparable<Post> {
    private static final long serialVersionUID = 1L;

    private final String postId;
    private final String content;
    private final LocalDateTime createdAt;
    private final String author;
    private final String source;

    private Sentiment sentiment;
    private ReliefItem reliefItem;
    private String disasterKeyword;
    private final List<Comment> comments;

    protected Post(String postId, String content, LocalDateTime createdAt,
                   String author, String source) {
        this.postId = Objects.requireNonNull(postId, "Post ID cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
        this.author = Objects.requireNonNull(author, "Author cannot be null");
        this.source = Objects.requireNonNull(source, "Source cannot be null");
        this.comments = new ArrayList<>();
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

    public String getSource() {
        return source;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public ReliefItem getReliefItem() {
        return reliefItem;
    }

    public String getDisasterKeyword() {
        return disasterKeyword;
    }

    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }

    public void setReliefItem(ReliefItem reliefItem) {
        this.reliefItem = reliefItem;
    }

    public void setDisasterKeyword(String disasterKeyword) {
        this.disasterKeyword = disasterKeyword;
    }

    public void addComment(Comment comment) {
        if (comment != null) {
            this.comments.add(comment);
        }
    }

    public void addComments(List<Comment> newComments) {
        if (newComments != null) {
            this.comments.addAll(newComments);
        }
    }

    public void removeComment(String commentId) {
        this.comments.removeIf(c -> c.getCommentId().equals(commentId));
    }

    public void updateComment(Comment updatedComment) {
        for (int i = 0; i < this.comments.size(); i++) {
            if (this.comments.get(i).getCommentId().equals(updatedComment.getCommentId())) {
                this.comments.set(i, updatedComment);
                break;
            }
        }
    }

    @Override
    public int compareTo(Post other) {
        return this.createdAt.compareTo(other.createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(postId, post.postId) &&
               Objects.equals(source, post.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, source);
    }

    @Override
    public String toString() {
        return "Post{" +
                "postId='" + postId + '\'' +
                ", author='" + author + '\'' +
                ", source='" + source + '\'' +
                ", createdAt=" + createdAt +
                ", sentiment=" + sentiment +
                ", reliefItem=" + reliefItem +
                '}';
    }
}
