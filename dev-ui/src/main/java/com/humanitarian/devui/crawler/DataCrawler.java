package com.humanitarian.devui.crawler;

import com.humanitarian.devui.model.Post;
import java.util.List;

public interface DataCrawler {
    
    List<Post> crawlPosts(List<String> keywords, List<String> hashtags, int limit);

    String getCrawlerName();

    boolean isInitialized();

    void shutdown();
}
