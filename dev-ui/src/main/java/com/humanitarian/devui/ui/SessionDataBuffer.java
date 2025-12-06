package com.humanitarian.devui.ui;

import com.humanitarian.devui.model.*;
import java.util.*;
import java.time.LocalDateTime;

public class SessionDataBuffer {
    private List<Post> pendingPosts = new ArrayList<>();
    private List<Comment> pendingComments = new ArrayList<>();
    private Map<String, Post> postMap = new HashMap<>();
    private boolean isDirty = false;

    public void addPost(Post post) {
        pendingPosts.add(post);
        postMap.put(post.getPostId(), post);

        for (Comment comment : post.getComments()) {
            pendingComments.add(comment);
        }
        isDirty = true;
    }

    public void addComment(Comment comment) {
        pendingComments.add(comment);
        isDirty = true;
    }

    public void removePost(String postId) {
        pendingPosts.removeIf(p -> p.getPostId().equals(postId));
        postMap.remove(postId);
        isDirty = true;
    }

    public void removeComment(String commentId) {
        pendingComments.removeIf(c -> c.getCommentId().equals(commentId));
        isDirty = true;
    }

    public void updatePost(Post post) {
        for (int i = 0; i < pendingPosts.size(); i++) {
            if (pendingPosts.get(i).getPostId().equals(post.getPostId())) {
                pendingPosts.set(i, post);
                postMap.put(post.getPostId(), post);
                isDirty = true;
                break;
            }
        }
    }

    public void updateComment(Comment comment) {
        for (int i = 0; i < pendingComments.size(); i++) {
            if (pendingComments.get(i).getCommentId().equals(comment.getCommentId())) {
                pendingComments.set(i, comment);
                isDirty = true;
                break;
            }
        }
    }

    public List<Post> getPendingPosts() {
        return new ArrayList<>(pendingPosts);
    }

    public List<Comment> getPendingComments() {
        return new ArrayList<>(pendingComments);
    }

    public int getTotalPosts() {
        return pendingPosts.size();
    }

    public int getTotalComments() {
        return pendingComments.size();
    }

    public void clear() {
        pendingPosts.clear();
        pendingComments.clear();
        postMap.clear();
        isDirty = false;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void resetDirtyFlag() {
        isDirty = false;
    }

    public Post getPost(String postId) {
        return postMap.get(postId);
    }

    public String generateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SESSION DATA BUFFER ===\n\n");
        sb.append("Posts Pending: ").append(pendingPosts.size()).append("\n");
        sb.append("Comments Pending: ").append(pendingComments.size()).append("\n");
        sb.append("Total Items: ").append(pendingPosts.size() + pendingComments.size()).append("\n\n");
        
        if (!pendingPosts.isEmpty()) {
            sb.append("--- Posts ---\n");
            for (Post post : pendingPosts) {
                sb.append("• ").append(post.getPostId()).append(" | ");
                sb.append(post.getAuthor()).append(" | ");
                sb.append(post.getContent().substring(0, Math.min(30, post.getContent().length())));
                if (post.getContent().length() > 30) sb.append("...");
                sb.append("\n");
            }
        }

        if (!pendingComments.isEmpty()) {
            sb.append("\n--- Comments ---\n");
            for (Comment comment : pendingComments) {
                sb.append("• ").append(comment.getCommentId()).append(" | ");
                sb.append(comment.getAuthor()).append(" | ");
                sb.append(comment.getContent().substring(0, Math.min(30, comment.getContent().length())));
                if (comment.getContent().length() > 30) sb.append("...");
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
