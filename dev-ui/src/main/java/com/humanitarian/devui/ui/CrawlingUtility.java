package com.humanitarian.devui.ui;

import com.humanitarian.devui.model.*;
import com.humanitarian.devui.crawler.DataCrawler;
import com.humanitarian.devui.database.DatabaseManager;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

public class CrawlingUtility {
    private static final Logger LOGGER = Logger.getLogger(CrawlingUtility.class.getName());
    
    public static void addCommentsToPost(Post post, int commentLimit) {
        String[] commentTemplates = {
            "The relief distribution was well organized",
            "Not enough resources were provided to affected areas",
            "Great effort from the humanitarian team",
            "Need more medical support in the affected region",
            "Food aid arrived on time",
            "Disappointed with the response time",
            "Excellent coordination with local authorities",
            "More shelter needed for displaced families",
            "Transportation assistance was very helpful",
            "Cash assistance made a big difference"
        };

        int commentCount = Math.min(commentLimit, commentTemplates.length);
        for (int i = 0; i < commentCount; i++) {
            String content = commentTemplates[i];
            Comment comment = new Comment(
                "COMMENT_" + post.getPostId() + "_" + i,
                post.getPostId(),
                content,
                post.getCreatedAt().plusHours(i + 1),
                "User_" + (i + 1)
            );

            Sentiment.SentimentType type = Math.random() > 0.5 ?
                (Math.random() > 0.5 ? Sentiment.SentimentType.POSITIVE : Sentiment.SentimentType.NEGATIVE)
                : Sentiment.SentimentType.NEUTRAL;
            double confidence = 0.7 + Math.random() * 0.3;

            comment.setSentiment(new Sentiment(type, confidence, content));
            comment.setReliefItem(post.getReliefItem());
            post.addComment(comment);
        }
    }
    
    public static boolean isDuplicatePost(String postId) {
        DatabaseManager dbChecker = new DatabaseManager();
        try {
            return dbChecker.isDuplicateLink(postId);
        } catch (Exception e) {
            LOGGER.warning("Error checking duplicate: " + e.getMessage());
            return false;
        } finally {
            try {
                dbChecker.close();
            } catch (Exception e) {

            }
        }
    }
    
    public static int processAndAddPosts(List<Post> posts, SessionDataBuffer buffer, 
                                         List<String> keywords, int commentLimit, 
                                         boolean addCommentsToMocks) {
        int addedCount = 0;
        int duplicateCount = 0;
        
        for (Post post : posts) {

            if (isDuplicatePost(post.getPostId())) {
                duplicateCount++;
                LOGGER.fine("Duplicate post skipped: " + post.getPostId());
                continue;
            }
            
            if (addCommentsToMocks) {
                addCommentsToPost(post, commentLimit);
            }
            
            if (keywords != null && !keywords.isEmpty()) {
                DisasterType disasterType = findDisasterTypeForPost(post, keywords);
                if (post instanceof YouTubePost) {
                    ((YouTubePost) post).setDisasterType(disasterType);
                }
            }
            
            buffer.addPost(post);
            addedCount++;
        }
        
        LOGGER.info("Processed posts: " + addedCount + " added, " + duplicateCount + " duplicates skipped");
        return addedCount;
    }
    
    public static DisasterType findDisasterTypeForPost(Post post, List<String> keywords) {
        DisasterManager manager = DisasterManager.getInstance();
        
        for (String keyword : keywords) {
            DisasterType disaster = manager.findDisasterType(keyword);
            if (disaster != null) {
                return disaster;
            }
        }
        
        return manager.getDisasterType("yagi");
    }
    
    public static List<String> validateAndCleanUrls(String urlText, String platformType) {
        List<String> validUrls = new ArrayList<>();
        
        if (urlText == null || urlText.trim().isEmpty()) {
            return validUrls;
        }
        
        String[] urls = urlText.split("\n");
        
        for (String url : urls) {
            String cleanUrl = url.trim();
            if (cleanUrl.isEmpty()) {
                continue;
            }
            
            if ("YOUTUBE".equals(platformType)) {
                if (cleanUrl.contains("youtube.com") || cleanUrl.contains("youtu.be")) {
                    validUrls.add(cleanUrl);
                }
            } else {

                if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
                    validUrls.add(cleanUrl);
                }
            }
        }
        
        return validUrls;
    }
}
