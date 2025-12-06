package com.humanitarian.devui.crawler;

import java.util.logging.Logger;

public class CrawlerManager {
    private static final Logger LOGGER = Logger.getLogger(CrawlerManager.class.getName());
    
    public static void initializeCrawlers() {
        CrawlerRegistry registry = CrawlerRegistry.getInstance();
        
        registry.registerCrawler(
            new CrawlerRegistry.CrawlerConfig(
                "YOUTUBE",
                "YouTube",
                "Crawl videos and comments from YouTube using Selenium",
                YouTubeCrawler::new,
                true,
                true,
                true
            )
        );
        
        registry.registerCrawler(
            new CrawlerRegistry.CrawlerConfig(
                "MOCK",
                "Sample/Mock Data",
                "Generate sample data for testing (no real crawling)",
                MockDataCrawler::new,
                false,
                true,
                false
            )
        );
        
        LOGGER.info("✓ All crawlers initialized: " + registry.getCrawlerDisplayNames());
    }
}
