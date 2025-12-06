package com.humanitarian.devui.ui;

import com.humanitarian.devui.model.*;
import com.humanitarian.devui.sentiment.SentimentAnalyzer;
import com.humanitarian.devui.sentiment.PythonCategoryClassifier;
import com.humanitarian.devui.database.DatabaseManager;
import com.humanitarian.devui.database.DataPersistenceManager;
import com.humanitarian.devui.analysis.*;

import java.util.*;

public class Model {
    private List<Post> posts;
    private SentimentAnalyzer sentimentAnalyzer;
    private PythonCategoryClassifier categoryClassifier;
    private DatabaseManager dbManager;
    private Map<String, AnalysisModule> analysisModules;
    private List<ModelListener> listeners;
    private DataPersistenceManager persistenceManager;

    public Model() {
        this.posts = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.analysisModules = new LinkedHashMap<>();
        this.categoryClassifier = new PythonCategoryClassifier();
        this.dbManager = new DatabaseManager();
        this.persistenceManager = new DataPersistenceManager();

        registerAnalysisModules();
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

        if (post.getReliefItem() == null) {
            categoryClassifier.classifyPost(post);
        }

        if (post.getSentiment() == null && sentimentAnalyzer != null) {
            Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(post.getContent());
            post.setSentiment(sentiment);
        }

        for (Comment comment : post.getComments()) {
            if (comment.getReliefItem() == null) {
                categoryClassifier.classifyPost(new PostAdapter(comment));
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

    private static class PostAdapter extends Post {
        PostAdapter(Comment comment) {
            super(comment.getCommentId(), comment.getContent(), comment.getCreatedAt(),
                    comment.getAuthor(), "ADAPTER");
        }
    }

    public void loadPersistedData() {
        List<Post> loadedPosts = persistenceManager.loadPosts();
        if (loadedPosts != null && !loadedPosts.isEmpty()) {

            for (Post post : loadedPosts) {
                addPost(post);
            }
            notifyListeners();
        }
    }

    public void savePersistedData() {
        persistenceManager.savePosts(new ArrayList<>(posts));
    }

    public void clearPersistedData() {
        persistenceManager.clearAllData();
        this.posts.clear();
        notifyListeners();
    }

    public DataPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    public int analyzeAllPosts() {
        System.out.println("Starting batch analysis of " + posts.size() + " posts...");
        System.out.println("✓ Category Classification: Keyword-based (Instant Vietnamese)");
        System.out.println("✓ Sentiment Analysis: xlm-roberta-large-xnli (Vietnamese + 100+ languages)");
        int analyzed = 0;

        for (Post post : posts) {
            try {

                categoryClassifier.classifyPost(post);

                if (sentimentAnalyzer != null) {
                    Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(post.getContent());
                    post.setSentiment(sentiment);
                }

                for (Comment comment : post.getComments()) {
                    categoryClassifier.classifyPost(new PostAdapter(comment));
                    if (sentimentAnalyzer != null) {
                        Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(comment.getContent());
                        comment.setSentiment(sentiment);
                    }
                }

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

    public void resetDatabaseConnection() {
        if (dbManager != null) {
            try {

                dbManager.reset();
            } catch (Exception e) {
                System.err.println("Error resetting dbManager: " + e.getMessage());
            }
        }
    }
}
