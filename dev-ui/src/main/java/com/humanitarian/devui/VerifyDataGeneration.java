package com.humanitarian.devui;

import com.humanitarian.devui.model.*;
import com.humanitarian.devui.database.DataPersistenceManager;
import java.util.*;

public class VerifyDataGeneration {
    public static void main(String[] args) {
        System.out.println("=== Verifying Sample Data Generation ===\n");
        
        DataPersistenceManager persistence = new DataPersistenceManager();
        List<Post> posts = persistence.loadPosts();
        
        if (posts.isEmpty()) {
            System.out.println("❌ No posts found!");
            System.out.println("Please generate sample data first by clicking 'Generate Mock Data' in the application.");
            return;
        }
        
        System.out.println("✓ Loaded " + posts.size() + " posts\n");
        
        int postsWithSentiment = 0;
        int postsWithCategory = 0;
        int totalComments = 0;
        int commentsWithSentiment = 0;
        int commentsWithCategory = 0;
        
        for (int i = 0; i < Math.min(3, posts.size()); i++) {
            Post post = posts.get(i);
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("Post #" + (i + 1));
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("ID: " + post.getPostId());
            System.out.println("Content: " + (post.getContent().length() > 70 
                ? post.getContent().substring(0, 70) + "..." 
                : post.getContent()));
            
            if (post.getSentiment() != null) {
                System.out.println("✓ Sentiment: " + post.getSentiment().getType() + 
                    " (confidence: " + String.format("%.2f", post.getSentiment().getConfidence()) + ")");
                postsWithSentiment++;
            } else {
                System.out.println("❌ Sentiment: NULL");
            }
            
            if (post.getReliefItem() != null) {
                System.out.println("✓ Category: " + post.getReliefItem().getCategory());
                postsWithCategory++;
            } else {
                System.out.println("❌ Category: NULL");
            }
            
            if (post.getComments() != null && !post.getComments().isEmpty()) {
                System.out.println("\nComments (" + post.getComments().size() + " total):");
                
                for (int j = 0; j < Math.min(2, post.getComments().size()); j++) {
                    Comment comment = post.getComments().get(j);
                    totalComments++;
                    
                    System.out.println("\n  Comment #" + (j + 1) + ":");
                    System.out.println("  Content: " + (comment.getContent().length() > 60
                        ? comment.getContent().substring(0, 60) + "..."
                        : comment.getContent()));
                    
                    if (comment.getSentiment() != null) {
                        System.out.println("  ✓ Sentiment: " + comment.getSentiment().getType() + 
                            " (confidence: " + String.format("%.2f", comment.getSentiment().getConfidence()) + ")");
                        commentsWithSentiment++;
                    } else {
                        System.out.println("  ❌ Sentiment: NULL");
                    }
                    
                    if (comment.getReliefItem() != null) {
                        System.out.println("  ✓ Category: " + comment.getReliefItem().getCategory());
                        commentsWithCategory++;
                    } else {
                        System.out.println("  ❌ Category: NULL");
                    }
                }
            } else {
                System.out.println("\nComments: None");
            }
            
        }
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📊 OVERALL STATISTICS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Posts with sentiment: " + postsWithSentiment + "/" + posts.size() + 
            " (" + String.format("%.1f", 100.0 * postsWithSentiment / posts.size()) + "%)");
        System.out.println("Posts with category: " + postsWithCategory + "/" + posts.size() + 
            " (" + String.format("%.1f", 100.0 * postsWithCategory / posts.size()) + "%)");
        
        if (totalComments > 0) {
            System.out.println("Comments with sentiment: " + commentsWithSentiment + "/" + totalComments + 
                " (" + String.format("%.1f", 100.0 * commentsWithSentiment / totalComments) + "%)");
            System.out.println("Comments with category: " + commentsWithCategory + "/" + totalComments + 
                " (" + String.format("%.1f", 100.0 * commentsWithCategory / totalComments) + "%)");
        }
        
        if (postsWithSentiment == posts.size() && postsWithCategory == posts.size() &&
            totalComments == commentsWithSentiment && totalComments == commentsWithCategory) {
            System.out.println("✅ Sample data generation is WORKING CORRECTLY!");
            System.out.println("All posts and comments have sentiment and category values.");
        } else {
            System.out.println("⚠️  Some data is missing sentiment or category values:");
            if (postsWithSentiment < posts.size()) {
                System.out.println("   - " + (posts.size() - postsWithSentiment) + " posts missing sentiment");
            }
            if (postsWithCategory < posts.size()) {
                System.out.println("   - " + (posts.size() - postsWithCategory) + " posts missing category");
            }
            if (totalComments > 0) {
                if (commentsWithSentiment < totalComments) {
                    System.out.println("   - " + (totalComments - commentsWithSentiment) + " comments missing sentiment");
                }
                if (commentsWithCategory < totalComments) {
                    System.out.println("   - " + (totalComments - commentsWithCategory) + " comments missing category");
                }
            }
        }
    }
}
