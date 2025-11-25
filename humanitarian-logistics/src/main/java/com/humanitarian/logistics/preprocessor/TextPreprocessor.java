package com.humanitarian.logistics.preprocessor;

/**
 * Interface for text preprocessing operations.
 * Supports pluggable preprocessing strategies.
 */
public interface TextPreprocessor {
    /**
     * Preprocesses text (cleaning, normalization, etc.)
     * @param text raw text to preprocess
     * @return preprocessed text
     */
    String preprocess(String text);

    /**
     * Gets the name of this preprocessor
     * @return preprocessor name
     */
    String getName();
}
