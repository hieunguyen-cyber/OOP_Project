package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.model.*;
import com.humanitarian.logistics.sentiment.SentimentAnalyzer;
import com.humanitarian.logistics.preprocessor.ReliefItemClassifier;
import com.humanitarian.logistics.database.DatabaseManager;
import com.humanitarian.logistics.database.DataPersistenceManager;
import com.humanitarian.logistics.analysis.*;

import java.util.*;

/**
 * Model component of MVC pattern.
 * Manages application state, data, and business logic.
 */
public class Model {
    private List<Post> posts;
    private SentimentAnalyzer sentimentAnalyzer;
    private ReliefItemClassifier reliefClassifier;
    private DatabaseManager dbManager;
    private DataPersistenceManager persistenceManager;
    private Map<String, AnalysisModule> analysisModules;
    private List<ModelListener> listeners;

    public Model() {
        this.posts = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.analysisModules = new LinkedHashMap<>();
        this.reliefClassifier = new ReliefItemClassifier();
        this.dbManager = new DatabaseManager();
        this.persistenceManager = new DataPersistenceManager();

        registerAnalysisModules();
        
        // Load persisted data
        loadPersistedData();
    }

    private void registerAnalysisModules() {
        analysisModules.put("satisfaction", new SatisfactionAnalysisModule());
        analysisModules.put("time_series", new TimeSeriesSentimentModule());
    }

    public void setSentimentAnalyzer(SentimentAnalyzer analyzer) {
        if (this.sentimentAnalyzer != null) {
            this.sentimentAnalyzer.shutdown();
        }
        this.sentimentAnalyzer = analyzer;
        this.sentimentAnalyzer.initialize();
        notifyListeners();
    }

    public List<Post> getPosts() {
        return new ArrayList<>(posts);
    }

    public void clearPosts() {
        posts.clear();
        notifyListeners();
    }

    public void addPost(Post post) {
        // Classify relief item if not already done
        if (post.getReliefItem() == null) {
            reliefClassifier.classifyPost(post);
        }

        // Analyze sentiment if not already done
        if (post.getSentiment() == null && sentimentAnalyzer != null) {
            Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(post.getContent());
            post.setSentiment(sentiment);
        }

        // Classify comments
        for (Comment comment : post.getComments()) {
            if (comment.getReliefItem() == null) {
                reliefClassifier.classifyPost(new PostAdapter(comment));
            }
            if (comment.getSentiment() == null && sentimentAnalyzer != null) {
                Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(comment.getContent());
                comment.setSentiment(sentiment);
            }
        }

        this.posts.add(post);
        try {
            dbManager.savePost(post);
        } catch (Exception e) {
            System.err.println("Error saving post: " + e.getMessage());
        }
        notifyListeners();
    }

    public void addPosts(List<Post> newPosts) {
        for (Post post : newPosts) {
            addPost(post);
        }
    }

    public void updateComment(Comment updatedComment) {
        for (Post post : posts) {
            for (Comment comment : post.getComments()) {
                if (comment.getCommentId().equals(updatedComment.getCommentId())) {
                    post.updateComment(updatedComment);
                    notifyListeners();
                    return;
                }
            }
        }
    }

    public void removeComment(String commentId) {
        for (Post post : posts) {
            for (Comment comment : post.getComments()) {
                if (comment.getCommentId().equals(commentId)) {
                    post.removeComment(commentId);
                    notifyListeners();
                    return;
                }
            }
        }
    }

    public Map<String, Object> performAnalysis(String moduleName) {
        AnalysisModule module = analysisModules.get(moduleName);
        if (module == null) {
            return Collections.emptyMap();
        }
        return module.analyze(posts);
    }

    public Map<String, AnalysisModule> getAnalysisModules() {
        return new LinkedHashMap<>(analysisModules);
    }

    public void addModelListener(ModelListener listener) {
        listeners.add(listener);
    }

    public void removeModelListener(ModelListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (ModelListener listener : listeners) {
            listener.modelChanged();
        }
    }

    // Adapter to treat Comment as Post for classification
    private static class PostAdapter extends Post {
        PostAdapter(Comment comment) {
            super(comment.getCommentId(), comment.getContent(), comment.getCreatedAt(),
                    comment.getAuthor(), "ADAPTER");
        }
    }

    /**
     * Load persisted data from local cache
     */
    private void loadPersistedData() {
        List<Post> loadedPosts = persistenceManager.loadPosts();
        if (!loadedPosts.isEmpty()) {
            posts.addAll(loadedPosts);
            notifyListeners();
            System.out.println("✓ Persisted data loaded: " + loadedPosts.size() + " posts");
        }
    }

    /**
     * Save all posts to persistent storage
     */
    public void savePersistedData() {
        persistenceManager.savePosts(posts);
    }

    /**
     * Clear all persisted data
     */
    public void clearPersistedData() {
        persistenceManager.clearAllData();
        posts.clear();
        notifyListeners();
    }

    /**
     * Get persistence manager
     */
    public DataPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    /**
     * Batch analyze all posts through both classification models.
     * Runs all posts through ReliefItemClassifier and SentimentAnalyzer,
     * then updates database with new classifications.
     * 
     * @return number of posts analyzed
     */
    public int analyzeAllPosts() {
        System.out.println("Starting batch analysis of " + posts.size() + " posts...");
        int analyzed = 0;

        for (Post post : posts) {
            try {
                // 1. Classify relief category
                reliefClassifier.classifyPost(post);

                // 2. Analyze sentiment
                if (sentimentAnalyzer != null) {
                    Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(post.getContent());
                    post.setSentiment(sentiment);
                }

                // 3. Analyze comments in the post
                for (Comment comment : post.getComments()) {
                    reliefClassifier.classifyPost(new PostAdapter(comment));
                    if (sentimentAnalyzer != null) {
                        Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(comment.getContent());
                        comment.setSentiment(sentiment);
                    }
                }

                // 4. Update database with new classifications
                dbManager.savePost(post);
                analyzed++;

                System.out.println("✓ Analyzed post " + analyzed + "/" + posts.size() + 
                    " (ID: " + post.getPostId() + ")");
            } catch (Exception e) {
                System.err.println("✗ Error analyzing post " + post.getPostId() + ": " + e.getMessage());
            }
        }

        notifyListeners();
        System.out.println("✓ Batch analysis complete! Analyzed " + analyzed + "/" + posts.size() + " posts");
        return analyzed;
    }

    /**
     * Reset database connection after manual database reset.
     * CRITICAL: Call this after database files are deleted to force reconnection.
     */
    public void resetDatabaseConnection() {
        System.out.println("DEBUG: Model.resetDatabaseConnection() called");
        if (dbManager != null) {
            try {
                // Call reset() on the existing instance to close connection and reset flags
                dbManager.reset();
                System.out.println("DEBUG: Called reset() on existing dbManager");
            } catch (Exception e) {
                System.err.println("Error resetting dbManager: " + e.getMessage());
            }
        }
    }
}
