package com.humanitarian.logistics.crawler;

import java.util.*;
import java.util.logging.Logger;

public class CrawlerRegistry {
    private static final Logger LOGGER = Logger.getLogger(CrawlerRegistry.class.getName());
    private static final CrawlerRegistry INSTANCE = new CrawlerRegistry();
    
    private final Map<String, CrawlerFactory> crawlers = new LinkedHashMap<>();
    
    @FunctionalInterface
    public interface CrawlerFactory {
        DataCrawler create();
    }
    
    public static class CrawlerConfig {
        public final String name;
        public final String displayName;
        public final String description;
        public final CrawlerFactory factory;
        public final boolean requiresInitialization;
        public final boolean supportsKeywordSearch;
        public final boolean supportsUrlCrawl;
        
        public CrawlerConfig(String name, String displayName, String description, 
                            CrawlerFactory factory, boolean requiresInit, 
                            boolean supportsKeywords, boolean supportsUrl) {
            this.name = name;
            this.displayName = displayName;
            this.description = description;
            this.factory = factory;
            this.requiresInitialization = requiresInit;
            this.supportsKeywordSearch = supportsKeywords;
            this.supportsUrlCrawl = supportsUrl;
        }
    }
    
    private final Map<String, CrawlerConfig> crawlerConfigs = new LinkedHashMap<>();
    
    private CrawlerRegistry() {}
    
    public static CrawlerRegistry getInstance() {
        return INSTANCE;
    }
    
    public void registerCrawler(CrawlerConfig config) {
        crawlers.put(config.name, config.factory);
        crawlerConfigs.put(config.name, config);
        LOGGER.info("✓ Registered crawler: " + config.displayName);
    }
    
    public void registerCrawler(String name, String displayName, String description, 
                                CrawlerFactory factory) {
        registerCrawler(new CrawlerConfig(name, displayName, description, factory, false, true, true));
    }
    
    public List<String> getCrawlerNames() {
        return new ArrayList<>(crawlers.keySet());
    }
    
    public List<String> getCrawlerDisplayNames() {
        return crawlerConfigs.values().stream()
            .map(c -> c.displayName)
            .toList();
    }
    
    public CrawlerConfig getConfig(String crawlerName) {
        return crawlerConfigs.get(crawlerName);
    }
    
    public DataCrawler createCrawler(String crawlerName) {
        CrawlerFactory factory = crawlers.get(crawlerName);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown crawler: " + crawlerName);
        }
        return factory.create();
    }
    
    public boolean supportsKeywordSearch(String crawlerName) {
        CrawlerConfig config = crawlerConfigs.get(crawlerName);
        return config != null && config.supportsKeywordSearch;
    }
    
    public boolean supportsUrlCrawl(String crawlerName) {
        CrawlerConfig config = crawlerConfigs.get(crawlerName);
        return config != null && config.supportsUrlCrawl;
    }
    
    public boolean requiresInitialization(String crawlerName) {
        CrawlerConfig config = crawlerConfigs.get(crawlerName);
        return config != null && config.requiresInitialization;
    }
    
    public String getDescription(String crawlerName) {
        CrawlerConfig config = crawlerConfigs.get(crawlerName);
        return config != null ? config.description : "No description available";
    }
}
