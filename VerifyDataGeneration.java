import java.io.*;
import java.util.*;

/**
 * Verification script to inspect generated sample data
 * Shows sentiment and category values for posts and comments
 */
public class VerifyDataGeneration {
    
    static class Comment implements Serializable {
        public String id;
        public String postId;
        public String content;
        public String sentiment;  // sentiment type
        public double confidence;
        public String reliefItem;
        public String category;
    }
    
    static class Post implements Serializable {
        public String id;
        public String content;
        public String sentiment;
        public double confidence;
        public String reliefItem;
        public List<Comment> comments = new ArrayList<>();
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Verifying Generated Sample Data ===\n");
        
        verifyProject("dev-ui", System.getProperty("user.home") + "/.humanitarian_devui/posts.dat");
        System.out.println("\n" + "=".repeat(50) + "\n");
        verifyProject("humanitarian-logistics", System.getProperty("user.home") + "/.humanitarian_logistics/posts.dat");
    }
    
    static void verifyProject(String projectName, String filePath) throws Exception {
        System.out.println("Project: " + projectName);
        System.out.println("File: " + filePath);
        
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("❌ No data file found!");
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            List<Object> posts = (List<Object>) ois.readObject();
            
            System.out.println("✓ Loaded " + posts.size() + " posts\n");
            
            int totalComments = 0;
            int commentsWithSentiment = 0;
            int commentsWithCategory = 0;
            int postsWithSentiment = 0;
            int postsWithCategory = 0;
            
            // Sample first 3 posts
            for (int i = 0; i < Math.min(3, posts.size()); i++) {
                Object postObj = posts.get(i);
                
                // Use reflection to access fields
                Class<?> postClass = postObj.getClass();
                
                try {
                    String postId = getFieldValue(postObj, "id", "");
                    String content = getFieldValue(postObj, "content", "");
                    String sentiment = getFieldValue(postObj, "sentiment", null);
                    double confidence = getFieldAsDouble(postObj, "confidence");
                    Object reliefItemObj = getField(postObj, "reliefItem");
                    
                    String category = "NONE";
                    if (reliefItemObj != null) {
                        category = getFieldValue(reliefItemObj, "category", "NONE");
                    }
                    
                    System.out.println("Post #" + (i+1) + " (ID: " + postId + ")");
                    System.out.println("  Content: " + (content.length() > 60 ? content.substring(0, 60) + "..." : content));
                    System.out.println("  Sentiment: " + (sentiment != null ? sentiment : "❌ NULL"));
                    System.out.println("  Confidence: " + (sentiment != null ? String.format("%.2f", confidence) : "❌ N/A"));
                    System.out.println("  Category: " + (category.equals("NONE") ? "❌ NULL" : "✓ " + category));
                    
                    // Check comments
                    List<?> comments = (List<?>) getField(postObj, "comments");
                    if (comments != null && !comments.isEmpty()) {
                        System.out.println("  Comments: " + comments.size());
                        
                        for (int j = 0; j < Math.min(2, comments.size()); j++) {
                            Object commentObj = comments.get(j);
                            String commentContent = getFieldValue(commentObj, "content", "");
                            String commentSentiment = getFieldValue(commentObj, "sentiment", null);
                            double commentConfidence = getFieldAsDouble(commentObj, "confidence");
                            
                            Object commentReliefObj = getField(commentObj, "reliefItem");
                            String commentCategory = "NONE";
                            if (commentReliefObj != null) {
                                commentCategory = getFieldValue(commentReliefObj, "category", "NONE");
                            }
                            
                            totalComments++;
                            if (commentSentiment != null) commentsWithSentiment++;
                            if (!commentCategory.equals("NONE")) commentsWithCategory++;
                            
                            System.out.println("    Comment #" + (j+1) + ": " + 
                                (commentContent.length() > 45 ? commentContent.substring(0, 45) + "..." : commentContent));
                            System.out.println("      Sentiment: " + (commentSentiment != null ? "✓ " + commentSentiment : "❌ NULL") + 
                                " (" + String.format("%.2f", commentConfidence) + ")");
                            System.out.println("      Category: " + (commentCategory.equals("NONE") ? "❌ NULL" : "✓ " + commentCategory));
                        }
                    }
                    
                    if (sentiment != null) postsWithSentiment++;
                    if (!category.equals("NONE")) postsWithCategory++;
                    
                } catch (Exception e) {
                    System.out.println("Error reading post: " + e.getMessage());
                }
                
                System.out.println();
            }
            
            // Statistics
            System.out.println("\n📊 STATISTICS");
            System.out.println("Posts with sentiment: " + postsWithSentiment + "/" + posts.size());
            System.out.println("Posts with category: " + postsWithCategory + "/" + posts.size());
            if (totalComments > 0) {
                System.out.println("Comments with sentiment: " + commentsWithSentiment + "/" + totalComments + 
                    " (" + String.format("%.1f", 100.0 * commentsWithSentiment / totalComments) + "%)");
                System.out.println("Comments with category: " + commentsWithCategory + "/" + totalComments + 
                    " (" + String.format("%.1f", 100.0 * commentsWithCategory / totalComments) + "%)");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static String getFieldValue(Object obj, String fieldName, String defaultValue) {
        try {
            Object value = getField(obj, fieldName);
            return value != null ? value.toString() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    static double getFieldAsDouble(Object obj, String fieldName) {
        try {
            Object value = getField(obj, fieldName);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    static Object getField(Object obj, String fieldName) throws Exception {
        Class<?> clazz = obj.getClass();
        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}
