package com.humanitarian.logistics.preprocessor;

import java.util.regex.Pattern;

/**
 * Concrete implementation of text preprocessor.
 * Handles text cleaning, normalization, and HTML entity removal.
 */
public class BasicTextPreprocessor implements TextPreprocessor {
    private static final Pattern URL_PATTERN = Pattern.compile("http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+");
    private static final Pattern EXTRA_WHITESPACE_PATTERN = Pattern.compile("\\s+");

    @Override
    public String preprocess(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Convert to lowercase
        text = text.toLowerCase();

        // Remove URLs
        text = URL_PATTERN.matcher(text).replaceAll("");

        // Remove mentions but keep hashtags
        // text = MENTION_PATTERN.matcher(text).replaceAll("");

        // Remove HTML entities
        text = text.replaceAll("&quot;", "\"")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&nbsp;", " ");

        // Remove special characters but keep spaces and basic punctuation
        text = text.replaceAll("[^a-z0-9\\s#@.,!?'\"-]", "");

        // Normalize whitespace
        text = EXTRA_WHITESPACE_PATTERN.matcher(text).replaceAll(" ");

        return text.trim();
    }

    @Override
    public String getName() {
        return "BasicTextPreprocessor";
    }
}
