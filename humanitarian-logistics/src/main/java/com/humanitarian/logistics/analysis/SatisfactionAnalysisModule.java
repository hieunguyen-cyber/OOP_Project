package com.humanitarian.logistics.analysis;

import com.humanitarian.logistics.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class SatisfactionAnalysisModule implements AnalysisModule {
    @Override
    public Map<String, Object> analyze(List<Post> posts) {
        Map<String, Object> results = new LinkedHashMap<>();

        Map<ReliefItem.Category, List<Sentiment>> sentimentsByCategory = new HashMap<>();

        for (ReliefItem.Category category : ReliefItem.Category.values()) {
            sentimentsByCategory.put(category, new ArrayList<>());
        }

        for (Post post : posts) {
            if (post.getReliefItem() != null && post.getSentiment() != null) {
                sentimentsByCategory.computeIfAbsent(post.getReliefItem().getCategory(), k -> new ArrayList<>())
                        .add(post.getSentiment());
            }

            for (Comment comment : post.getComments()) {
                if (comment.getReliefItem() != null && comment.getSentiment() != null) {
                    sentimentsByCategory.computeIfAbsent(comment.getReliefItem().getCategory(), k -> new ArrayList<>())
                            .add(comment.getSentiment());
                }
            }
        }

        Map<String, Map<String, Object>> categoryStats = new LinkedHashMap<>();
        Map<String, Object> categoryEffectiveness = new LinkedHashMap<>();
        
        for (ReliefItem.Category category : ReliefItem.Category.values()) {
            List<Sentiment> sentiments = sentimentsByCategory.get(category);
            if (!sentiments.isEmpty()) {
                Map<String, Object> stats = calculateSentimentStats(sentiments, category);
                categoryStats.put(category.getDisplayName(), stats);
                categoryEffectiveness.put(category.getDisplayName(), assessCategoryEffectiveness(stats));
            }
        }

        results.put("problem_1_satisfaction_analysis", categoryStats);
        results.put("category_effectiveness", categoryEffectiveness);
        results.put("total_records_analyzed", posts.size());
        results.put("detailed_insights", generateDetailedInsights(categoryStats));
        results.put("resource_allocation_recommendations", generateResourceRecommendations(categoryStats));
        results.put("summary", generateSummary(categoryStats));

        return results;
    }

    private Map<String, Object> calculateSentimentStats(List<Sentiment> sentiments,
                                                        ReliefItem.Category category) {
        Map<String, Object> stats = new LinkedHashMap<>();

        long positiveCount = sentiments.stream().filter(Sentiment::isPositive).count();
        long negativeCount = sentiments.stream().filter(Sentiment::isNegative).count();
        long neutralCount = sentiments.stream().filter(Sentiment::isNeutral).count();

        double totalConfidence = sentiments.stream()
                .mapToDouble(Sentiment::getConfidence).sum();
        double avgConfidence = totalConfidence / sentiments.size();

        double positivePercentage = (double) positiveCount / sentiments.size() * 100;
        double negativePercentage = (double) negativeCount / sentiments.size() * 100;
        double neutralPercentage = (double) neutralCount / sentiments.size() * 100;

        double satisfactionScore = (positiveCount - negativeCount) / (double) sentiments.size();

        stats.put("category", category.getDisplayName());
        stats.put("total_mentions", sentiments.size());
        stats.put("positive_count", positiveCount);
        stats.put("negative_count", negativeCount);
        stats.put("neutral_count", neutralCount);
        stats.put("positive_percentage", String.format("%.2f%%", positivePercentage));
        stats.put("negative_percentage", String.format("%.2f%%", negativePercentage));
        stats.put("neutral_percentage", String.format("%.2f%%", neutralPercentage));
        stats.put("average_confidence", String.format("%.2f", avgConfidence));
        stats.put("satisfaction_score", String.format("%.2f", satisfactionScore));
        stats.put("net_sentiment", positiveCount - negativeCount);

        return stats;
    }

    private Map<String, Object> assessCategoryEffectiveness(Map<String, Object> stats) {
        Map<String, Object> effectiveness = new LinkedHashMap<>();

        String positiveStr = (String) stats.get("positive_percentage");
        String negativeStr = (String) stats.get("negative_percentage");
        String scoreStr = (String) stats.get("satisfaction_score");
        
        double positive = Double.parseDouble(positiveStr.replace("%", ""));
        double negative = Double.parseDouble(negativeStr.replace("%", ""));
        double score = Double.parseDouble(scoreStr);

        String status;
        String assessment;
        String recommendation;

        if (positive > 70) {
            status = "HIGHLY SATISFACTORY";
            assessment = "This relief category is well-received with strong positive sentiment";
            recommendation = "Maintain and scale current operations";
        } else if (positive > 60) {
            status = "SATISFACTORY";
            assessment = "This relief category has positive reception overall";
            recommendation = "Continue current approach while monitoring for improvements";
        } else if (positive > 50 || (positive > negative && score > 0)) {
            status = "NEUTRAL TO POSITIVE";
            assessment = "Mixed reception but slightly positive overall";
            recommendation = "Review implementation and address user concerns";
        } else if (negative > positive && negative < 50) {
            status = "NEEDS ATTENTION";
            assessment = "More negative than positive sentiment detected";
            recommendation = "Investigate issues and adjust delivery strategy";
        } else if (negative > 60) {
            status = "CRITICAL";
            assessment = "High level of dissatisfaction detected";
            recommendation = "Urgent intervention required - review and revise strategy";
        } else {
            status = "INCONCLUSIVE";
            assessment = "Insufficient data for clear determination";
            recommendation = "Collect more data and re-evaluate";
        }

        effectiveness.put("status", status);
        effectiveness.put("assessment", assessment);
        effectiveness.put("recommendation", recommendation);
        effectiveness.put("satisfaction_score", scoreStr);

        return effectiveness;
    }

    private Map<String, Object> generateDetailedInsights(Map<String, Map<String, Object>> categoryStats) {
        Map<String, Object> insights = new LinkedHashMap<>();

        List<Map.Entry<String, Map<String, Object>>> sorted = categoryStats.entrySet().stream()
                .sorted((a, b) -> {
                    String scoreA = (String) a.getValue().get("satisfaction_score");
                    String scoreB = (String) b.getValue().get("satisfaction_score");
                    return Double.compare(Double.parseDouble(scoreB), Double.parseDouble(scoreA));
                })
                .collect(Collectors.toList());

        if (!sorted.isEmpty()) {
            String topCategory = sorted.get(0).getKey();
            Map<String, Object> topStats = sorted.get(0).getValue();
            long positive = ((Number) topStats.get("positive_count")).longValue();
            long total = ((Number) topStats.get("total_mentions")).longValue();
            insights.put("highest_satisfaction_category", topCategory + " (" + positive + "/" + total + " positive)");
        }

        if (!sorted.isEmpty()) {
            String bottomCategory = sorted.get(sorted.size() - 1).getKey();
            Map<String, Object> bottomStats = sorted.get(sorted.size() - 1).getValue();
            long negative = ((Number) bottomStats.get("negative_count")).longValue();
            long total = ((Number) bottomStats.get("total_mentions")).longValue();
            insights.put("lowest_satisfaction_category", bottomCategory + " (" + negative + "/" + total + " negative)");
        }

        List<String> criticalCategories = categoryStats.entrySet().stream()
                .filter(e -> {
                    String negStr = (String) e.getValue().get("negative_percentage");
                    double neg = Double.parseDouble(negStr.replace("%", ""));
                    return neg > 60;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        insights.put("critical_categories", criticalCategories.isEmpty() ? "None" : String.join(", ", criticalCategories));

        return insights;
    }

    private Map<String, Object> generateResourceRecommendations(Map<String, Map<String, Object>> categoryStats) {
        Map<String, Object> recommendations = new LinkedHashMap<>();

        List<Map.Entry<String, Map<String, Object>>> sorted = categoryStats.entrySet().stream()
                .sorted((a, b) -> {
                    String negA = (String) a.getValue().get("negative_percentage");
                    String negB = (String) b.getValue().get("negative_percentage");
                    return Double.compare(
                            Double.parseDouble(negB.replace("%", "")),
                            Double.parseDouble(negA.replace("%", ""))
                    );
                })
                .collect(Collectors.toList());

        List<String> urgent = new ArrayList<>();
        List<String> moderate = new ArrayList<>();
        List<String> lowPriority = new ArrayList<>();

        for (Map.Entry<String, Map<String, Object>> entry : sorted) {
            String category = entry.getKey();
            String negPercentageStr = (String) entry.getValue().get("negative_percentage");
            double negPercentage = Double.parseDouble(negPercentageStr.replace("%", ""));

            if (negPercentage > 60) {
                urgent.add(category + " (" + String.format("%.1f", negPercentage) + "% negative)");
            } else if (negPercentage > 40) {
                moderate.add(category + " (" + String.format("%.1f", negPercentage) + "% negative)");
            } else {
                lowPriority.add(category);
            }
        }

        recommendations.put("urgent_attention_required", urgent.isEmpty() ? "None" : urgent);
        recommendations.put("moderate_priority", moderate.isEmpty() ? "None" : moderate);
        recommendations.put("stable_operations", lowPriority.isEmpty() ? "None" : lowPriority);

        return recommendations;
    }

    private Map<String, Object> generateSummary(Map<String, Map<String, Object>> categoryStats) {
        Map<String, Object> summary = new LinkedHashMap<>();

        long totalPositive = 0;
        long totalNegative = 0;
        long totalNeutral = 0;
        int categoriesWithData = 0;

        for (Map<String, Object> stats : categoryStats.values()) {
            totalPositive += ((Number) stats.get("positive_count")).longValue();
            totalNegative += ((Number) stats.get("negative_count")).longValue();
            totalNeutral += ((Number) stats.get("neutral_count")).longValue();
            categoriesWithData++;
        }

        long total = totalPositive + totalNegative + totalNeutral;

        summary.put("total_records", total);
        summary.put("total_positive", totalPositive + " (" + String.format("%.1f", 100.0 * totalPositive / total) + "%)");
        summary.put("total_negative", totalNegative + " (" + String.format("%.1f", 100.0 * totalNegative / total) + "%)");
        summary.put("total_neutral", totalNeutral + " (" + String.format("%.1f", 100.0 * totalNeutral / total) + "%)");
        summary.put("overall_satisfaction_score", String.format("%.2f", (totalPositive - totalNegative) / (double) total));
        summary.put("categories_analyzed", categoriesWithData);

        return summary;
    }

    @Override
    public String getModuleName() {
        return "Satisfaction Analysis Module (Problem 1)";
    }

    @Override
    public String getDescription() {
        return "Analyzes public satisfaction and dissatisfaction for different relief item categories. " +
               "Provides detailed effectiveness assessment and resource allocation recommendations for each sector.";
    }
}
