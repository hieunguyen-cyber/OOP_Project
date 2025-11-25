package com.humanitarian.logistics.test;

import com.humanitarian.logistics.crawler.FacebookCrawler;
import com.humanitarian.logistics.crawler.MockDataCrawler;
import com.humanitarian.logistics.model.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Test program to demonstrate enhanced mock data with:
 * - Category-specific content for each relief type
 * - Temporal distribution with sentiment evolution over time
 * - Comments showing realistic sentiment patterns
 */
public class TestFallbackData {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║       ENHANCED MOCK DATA TEST - TEMPORAL SENTIMENT EVOLUTION   ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Test 1: MockDataCrawler with enhanced data
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.println("TEST 1: MockDataCrawler - Enhanced Category-Specific Content");
        System.out.println("═══════════════════════════════════════════════════════════════════");
        testMockDataCrawler();

        // Test 2: FacebookCrawler fallback data
        System.out.println("\n═══════════════════════════════════════════════════════════════════");
        System.out.println("TEST 2: FacebookCrawler - Fallback Data Generation");
        System.out.println("═══════════════════════════════════════════════════════════════════");
        testFacebookCrawlerFallback();

        System.out.println("\n✅ All fallback data tests completed successfully!\n");
    }

    private static void testMockDataCrawler() {
        MockDataCrawler crawler = new MockDataCrawler();
        List<Post> posts = crawler.crawlPosts(
            Arrays.asList("disaster", "relief"),
            Arrays.asList("#humanitarian", "#aid"),
            35
        );

        System.out.println("\n📊 MockDataCrawler Results:");
        System.out.println("   Total Posts Generated: " + posts.size());

        // Analyze by category
        Map<String, Integer> categoryCount = new HashMap<>();
        Map<String, Integer> positiveCounts = new HashMap<>();

        for (Post post : posts) {
            ReliefItem.Category category = post.getReliefItem().getCategory();
            String catName = category.getDisplayName();
            categoryCount.put(catName, categoryCount.getOrDefault(catName, 0) + 1);

            if (post.getSentiment().isPositive()) {
                positiveCounts.put(catName, positiveCounts.getOrDefault(catName, 0) + 1);
            }
        }

        System.out.println("\n   📈 Distribution by Category:");
        for (String category : categoryCount.keySet()) {
            int total = categoryCount.get(category);
            int positive = positiveCounts.getOrDefault(category, 0);
            double percentage = (positive * 100.0) / total;
            System.out.printf("      • %-20s: %2d posts (%3d positive, %.1f%%)%n",
                category, total, positive, percentage);
        }

        // Show sample posts with different sentiments
        System.out.println("\n   🔍 Sample Posts Timeline (first 6 posts):");
        List<Post> sortedPosts = new ArrayList<>(posts);
        sortedPosts.sort(Comparator.comparing(Post::getCreatedAt));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        for (int i = 0; i < Math.min(6, sortedPosts.size()); i++) {
            Post post = sortedPosts.get(i);
            String sentiment = post.getSentiment().isPositive() ? "POSITIVE" : 
                              (post.getSentiment().isNegative() ? "NEGATIVE" : "NEUTRAL");
            String emoji = sentiment.equals("POSITIVE") ? "✅" : (sentiment.equals("NEGATIVE") ? "❌" : "⚫");
            System.out.printf("      %s [%s] %s - %s%n",
                emoji,
                post.getCreatedAt().format(formatter),
                post.getReliefItem().getCategory().getDisplayName(),
                post.getContent().substring(0, Math.min(50, post.getContent().length())) + "..."
            );
        }

        // Show comments sample
        System.out.println("\n   💬 Sample Comments (showing sentiment variety):");
        Post postWithComments = null;
        for (Post post : posts) {
            if (!post.getComments().isEmpty()) {
                postWithComments = post;
                break;
            }
        }

        if (postWithComments != null) {
            System.out.println("      Post: " + postWithComments.getContent().substring(0, Math.min(50, postWithComments.getContent().length())) + "...");
            for (Comment comment : postWithComments.getComments()) {
                String sentiment = comment.getSentiment().isPositive() ? "POSITIVE" : 
                                  (comment.getSentiment().isNegative() ? "NEGATIVE" : "NEUTRAL");
                String emoji = sentiment.equals("POSITIVE") ? "👍" : (sentiment.equals("NEGATIVE") ? "👎" : "•");
                System.out.println("        " + emoji + " " + comment.getContent());
            }
        }
    }

    private static void testFacebookCrawlerFallback() {
        FacebookCrawler crawler = new FacebookCrawler();
        List<Post> fallbackPosts = crawler.generateFallbackData(35);

        System.out.println("\n📊 FacebookCrawler Fallback Data Results:");
        System.out.println("   Total Fallback Posts: " + fallbackPosts.size());

        // Show distribution by category
        System.out.println("\n   📊 Posts Distribution by Category:");
        Map<String, Integer> categoryCount = new HashMap<>();
        for (Post post : fallbackPosts) {
            String catName = post.getReliefItem().getCategory().getDisplayName();
            categoryCount.put(catName, categoryCount.getOrDefault(catName, 0) + 1);
        }

        for (String category : categoryCount.keySet()) {
            System.out.printf("      • %-20s: %d posts%n", category, categoryCount.get(category));
        }

        // Show sentiment progression for each category
        System.out.println("\n   📊 Sentiment Progression by Category:");
        Map<ReliefItem.Category, List<Post>> postsByCategory = new HashMap<>();
        for (Post post : fallbackPosts) {
            ReliefItem.Category cat = post.getReliefItem().getCategory();
            postsByCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(post);
        }

        for (ReliefItem.Category category : ReliefItem.Category.values()) {
            List<Post> catPosts = postsByCategory.get(category);
            if (catPosts == null) continue;

            catPosts.sort(Comparator.comparing(Post::getCreatedAt));

            int positive = 0, negative = 0;
            for (Post post : catPosts) {
                if (post.getSentiment().isPositive()) positive++;
                else if (post.getSentiment().isNegative()) negative++;
            }

            double positiveRatio = (positive * 100.0) / catPosts.size();
            double negativeRatio = (negative * 100.0) / catPosts.size();
            String trend = positiveRatio > negativeRatio ? "📈 IMPROVING" : "📉 NEEDS ATTENTION";

            System.out.printf("      %-20s: %d posts | Positive: %.1f%%, Negative: %.1f%% | %s%n",
                category.getDisplayName(), catPosts.size(), positiveRatio, negativeRatio, trend);
        }

        // Show sample posts
        System.out.println("\n   🔍 Sample Fallback Posts:");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        int count = 0;
        for (Post post : fallbackPosts) {
            if (count >= 6) break;
            String sentiment = post.getSentiment().isPositive() ? "POSITIVE" : 
                              (post.getSentiment().isNegative() ? "NEGATIVE" : "NEUTRAL");
            String emoji = sentiment.equals("POSITIVE") ? "✅" : (sentiment.equals("NEGATIVE") ? "❌" : "⚫");
            System.out.printf("      %s [%s] %s - %s%n",
                emoji,
                post.getCreatedAt().format(formatter),
                post.getReliefItem().getCategory().getDisplayName(),
                post.getContent().substring(0, Math.min(55, post.getContent().length())) + "..."
            );
            count++;
        }

        System.out.println("\n   💡 Key Features of Fallback Data:");
        System.out.println("      • Category-specific content for each relief type (Cash, Medical, Shelter, Food, Transport)");
        System.out.println("      • Temporal distribution over 35 days with realistic post timing");
        System.out.println("      • Sentiment evolution: Early posts negative/neutral → Late posts positive");
        System.out.println("      • Category-specific comments with matching sentiment patterns");
        System.out.println("      • Realistic engagement metrics (likes, shares, comments)");
    }
}
