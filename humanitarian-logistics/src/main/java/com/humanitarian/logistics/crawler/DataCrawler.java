package com.humanitarian.logistics.crawler;

import com.humanitarian.logistics.model.Post;
import java.util.List;

/**
 * Interface for data crawlers from different social media sources.
 * Demonstrates interface-based design and polymorphism.
 * Easy to add new crawler implementations without modifying existing code.
 */
public interface DataCrawler {
    /**
     * Crawls posts based on keywords and hashtags
     * @param keywords list of keywords to search for
     * @param hashtags list of hashtags to search for
     * @param limit maximum number of posts to retrieve
     * @return list of collected posts
     */
    List<Post> crawlPosts(List<String> keywords, List<String> hashtags, int limit);

    /**
     * Gets the name/identifier of the crawler
     * @return crawler name
     */
    String getCrawlerName();

    /**
     * Validates if crawler is properly initialized
     * @return true if ready to crawl, false otherwise
     */
    boolean isInitialized();

    /**
     * Performs cleanup operations
     */
    void shutdown();
}
