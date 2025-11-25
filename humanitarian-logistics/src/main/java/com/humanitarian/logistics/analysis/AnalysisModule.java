package com.humanitarian.logistics.analysis;

import com.humanitarian.logistics.model.Post;
import java.util.List;
import java.util.Map;

/**
 * Interface for analysis modules addressing specific problems.
 * Allows implementation of different analysis strategies.
 */
public interface AnalysisModule {
    /**
     * Performs analysis on posts and comments
     * @param posts list of posts to analyze
     * @return analysis results as a map
     */
    Map<String, Object> analyze(List<Post> posts);

    /**
     * Gets the name of this analysis module
     * @return module name
     */
    String getModuleName();

    /**
     * Gets description of what this module does
     * @return module description
     */
    String getDescription();
}
