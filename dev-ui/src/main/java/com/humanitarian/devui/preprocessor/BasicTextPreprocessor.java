package com.humanitarian.devui.preprocessor;

import java.util.regex.Pattern;

public class BasicTextPreprocessor implements TextPreprocessor {
    private static final Pattern URL_PATTERN = Pattern.compile("http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+");
    private static final Pattern EXTRA_WHITESPACE_PATTERN = Pattern.compile("\\s+");

    @Override
    public String preprocess(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        text = text.toLowerCase();

        text = URL_PATTERN.matcher(text).replaceAll("");

        text = text.replaceAll("&quot;", "\"")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&nbsp;", " ");

        text = text.replaceAll("[^a-z0-9\\s#@.,!?'\"-]", "");

        text = EXTRA_WHITESPACE_PATTERN.matcher(text).replaceAll(" ");

        return text.trim();
    }

    @Override
    public String getName() {
        return "BasicTextPreprocessor";
    }
}
