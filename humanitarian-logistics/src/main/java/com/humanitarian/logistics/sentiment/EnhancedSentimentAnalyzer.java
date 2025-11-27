package com.humanitarian.logistics.sentiment;

import com.humanitarian.logistics.model.Sentiment;

/**
 * Enhanced Sentiment Analyzer with Vietnamese language support.
 * Uses multi-dimensional sentiment scoring with domain-specific keywords.
 */
public class EnhancedSentimentAnalyzer implements SentimentAnalyzer {
    
    // English keywords
    private static final String[] POSITIVE_WORDS_EN = {
            "good", "great", "excellent", "happy", "love", "thank", "thanks",
            "appreciate", "support", "help", "aid", "relief", "better", "improved",
            "success", "wonderful", "fantastic", "amazing", "grateful", "appreciate",
            "effective", "working", "progress", "hope", "recover", "safe", "stable"
    };

    private static final String[] NEGATIVE_WORDS_EN = {
            "bad", "poor", "terrible", "sad", "hate", "angry", "upset", "frustrated",
            "struggle", "difficult", "problem", "issue", "lack", "missing", "needed",
            "fail", "failure", "disaster", "crisis", "emergency", "suffering", "pain",
            "loss", "damage", "fear", "worried", "concern", "risk", "danger", "critical"
    };

    // Vietnamese keywords (humanitarian domain)
    private static final String[] POSITIVE_WORDS_VI = {
            "tốt", "tuyệt vời", "xuất sắc", "tuyệt", "yêu", "cảm ơn", "cám ơn",
            "biết ơn", "hỗ trợ", "giúp đỡ", "trợ giúp", "cứu", "cứu trợ", "hỗ trợ",
            "cải thiện", "tốt hơn", "thành công", "hỏi", "phục hồi", "ổn định",
            "an toàn", "yên tâm", "tích cực", "hiệu quả", "hoạt động", "tiến bộ",
            "hy vọng", "thoát khỏi", "vượt qua", "sống sót", "bình phục", "khỏe"
    };

    private static final String[] NEGATIVE_WORDS_VI = {
            "xấu", "tệ", "kinh khủng", "buồn", "ghét", "tức giận", "bực", "thất vọng",
            "đấu tranh", "khó khăn", "vấn đề", "lo lắng", "thiếu", "cần", "cần thiết",
            "thất bại", "tai nạn", "thảm họa", "khủng hoảng", "tình trạng khẩn cấp",
            "đau khổ", "chết", "mất", "hư hại", "sợ", "lo sợ", "quan tâm", "rủi ro",
            "nguy hiểm", "nguy kịch", "bệnh", "ốm", "bị thương", "tổn thương"
    };

    public EnhancedSentimentAnalyzer() {
    }

    @Override
    public Sentiment analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new Sentiment(Sentiment.SentimentType.NEUTRAL, 0.0, "");
        }

        String lowerText = text.toLowerCase();
        
        // Count keyword occurrences
        int positiveCount = countMatches(lowerText, POSITIVE_WORDS_EN) + 
                           countMatches(lowerText, POSITIVE_WORDS_VI);
        int negativeCount = countMatches(lowerText, NEGATIVE_WORDS_EN) + 
                           countMatches(lowerText, NEGATIVE_WORDS_VI);

        // Determine sentiment and confidence
        Sentiment.SentimentType type;
        double confidence;

        if (positiveCount > negativeCount) {
            type = Sentiment.SentimentType.POSITIVE;
            // Confidence increases with more positive indicators
            confidence = Math.min(0.99, 0.6 + (positiveCount * 0.15));
        } else if (negativeCount > positiveCount) {
            type = Sentiment.SentimentType.NEGATIVE;
            // Confidence increases with more negative indicators
            confidence = Math.min(0.99, 0.6 + (negativeCount * 0.15));
        } else {
            type = Sentiment.SentimentType.NEUTRAL;
            // Lower confidence if no clear sentiment indicators
            confidence = positiveCount == 0 && negativeCount == 0 ? 0.4 : 0.5;
        }

        return new Sentiment(type, confidence, text);
    }

    @Override
    public Sentiment[] analyzeSentimentBatch(String[] texts) {
        Sentiment[] results = new Sentiment[texts.length];
        for (int i = 0; i < texts.length; i++) {
            results[i] = analyzeSentiment(texts[i]);
        }
        return results;
    }

    @Override
    public String getModelName() {
        return "EnhancedSentimentAnalyzer-v2.0 (Vietnamese + English)";
    }

    @Override
    public void initialize() {
        System.out.println("✓ EnhancedSentimentAnalyzer initialized with bilingual support");
        System.out.println("  - English keywords: " + POSITIVE_WORDS_EN.length + " positive, " + 
                          NEGATIVE_WORDS_EN.length + " negative");
        System.out.println("  - Vietnamese keywords: " + POSITIVE_WORDS_VI.length + " positive, " + 
                          NEGATIVE_WORDS_VI.length + " negative");
    }

    @Override
    public void shutdown() {
        System.out.println("EnhancedSentimentAnalyzer shutdown");
    }

    /**
     * Count how many keywords match in the text
     */
    private int countMatches(String text, String[] keywords) {
        int count = 0;
        for (String keyword : keywords) {
            // Check for word boundaries to avoid substring matches
            if (text.contains(" " + keyword + " ") || text.contains(" " + keyword) || 
                text.endsWith(keyword) || text.startsWith(keyword)) {
                count++;
            }
        }
        return count;
    }
}
