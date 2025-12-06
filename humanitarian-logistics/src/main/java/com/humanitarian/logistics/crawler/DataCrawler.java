package com.humanitarian.logistics.crawler;

import com.humanitarian.logistics.model.Post;
import java.util.List;

public interface DataCrawler {
    
    List<Post> crawlPosts(List<String> keywords, List<String> hashtags, int limit);

    String getCrawlerName();

    boolean isInitialized();

    void shutdown();
}
