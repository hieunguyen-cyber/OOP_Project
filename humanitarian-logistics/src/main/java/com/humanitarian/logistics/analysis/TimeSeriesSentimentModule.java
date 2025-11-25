package com.humanitarian.logistics.analysis;

import com.humanitarian.logistics.model.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Analysis Module 2: Tracks sentiment per relief item over time.
 * Shows how public sentiment changes for each relief category during recovery period.
 * 
 * Problem 2 - Detailed Analysis:
 * - Analyze sentiment trends for each relief category (cash, medical, shelter, food, transportation)
 * - Track sentiment changes across time buckets (6-hour intervals)
 * - Identify patterns: improving, deteriorating, or stable
 * - Measure effectiveness by comparing positive vs negative sentiment
 * - Highlight areas of success and gaps needing attention
 */
public class TimeSeriesSentimentModule implements AnalysisModule {
    private static final int TIME_BUCKET_HOURS = 6; // Group data into 6-hour buckets

    @Override
    public Map<String, Object> analyze(List<Post> posts) {
        Map<String, Object> results = new LinkedHashMap<>();

        // Group sentiments by relief category and time bucket
        Map<ReliefItem.Category, Map<LocalDateTime, List<Sentiment>>> timeSeries = new HashMap<>();
        
        // Collect all sentiments for each category
        for (Post post : posts) {
            if (post.getReliefItem() != null && post.getSentiment() != null) {
                ReliefItem.Category category = post.getReliefItem().getCategory();
                LocalDateTime bucket = getTimeBucket(post.getCreatedAt());

                timeSeries.computeIfAbsent(category, k -> new TreeMap<>())
                        .computeIfAbsent(bucket, k -> new ArrayList<>())
                        .add(post.getSentiment());
            }

            // Also process comments
            for (Comment comment : post.getComments()) {
                if (comment.getReliefItem() != null && comment.getSentiment() != null) {
                    ReliefItem.Category category = comment.getReliefItem().getCategory();
                    LocalDateTime bucket = getTimeBucket(comment.getCreatedAt());

                    timeSeries.computeIfAbsent(category, k -> new TreeMap<>())
                            .computeIfAbsent(bucket, k -> new ArrayList<>())
                            .add(comment.getSentiment());
                }
            }
        }

        // Convert to detailed analysis format
        Map<String, Object> timeSeriesAnalysis = new LinkedHashMap<>();
        Map<String, Object> sectorEffectiveness = new LinkedHashMap<>();
        Map<String, Object> detailedInsights = new LinkedHashMap<>();
        
        for (ReliefItem.Category category : ReliefItem.Category.values()) {
            Map<LocalDateTime, List<Sentiment>> categoryTimeSeries = timeSeries.get(category);
            if (categoryTimeSeries != null && !categoryTimeSeries.isEmpty()) {
                String categoryName = category.getDisplayName();
                
                // Analyze time series
                Map<String, Object> categoryAnalysis = analyzeTimeSeries(categoryTimeSeries);
                timeSeriesAnalysis.put(categoryName, categoryAnalysis);
                
                // Determine effectiveness
                sectorEffectiveness.put(categoryName, determineEffectiveness(categoryAnalysis));
                
                // Generate detailed insights
                detailedInsights.put(categoryName, generateDetailedInsights(categoryName, categoryAnalysis));
            }
        }

        results.put("time_bucket_hours", TIME_BUCKET_HOURS);
        results.put("problem_2_time_series_sentiment", timeSeriesAnalysis);
        results.put("sector_effectiveness", sectorEffectiveness);
        results.put("detailed_insights", detailedInsights);
        results.put("summary", generateSummary(sectorEffectiveness));

        return results;
    }

    private LocalDateTime getTimeBucket(LocalDateTime dateTime) {
        long hours = dateTime.getHour() / TIME_BUCKET_HOURS;
        return dateTime.withHour((int) (hours * TIME_BUCKET_HOURS))
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    /**
     * Detailed analysis of sentiment time series for a relief category
     */
    private Map<String, Object> analyzeTimeSeries(Map<LocalDateTime, List<Sentiment>> timeSeries) {
        Map<String, Object> analysis = new LinkedHashMap<>();
        List<Map<String, Object>> timePoints = new ArrayList<>();
        
        double positiveSum = 0;
        double negativeSum = 0;
        double totalCount = 0;

        for (Map.Entry<LocalDateTime, List<Sentiment>> entry : timeSeries.entrySet()) {
            List<Sentiment> sentiments = entry.getValue();
            Map<String, Object> timePoint = new LinkedHashMap<>();

            long positive = sentiments.stream().filter(Sentiment::isPositive).count();
            long negative = sentiments.stream().filter(Sentiment::isNegative).count();
            long neutral = sentiments.stream().filter(Sentiment::isNeutral).count();
            
            double positiveRatio = (double) positive / sentiments.size();
            double negativeRatio = (double) negative / sentiments.size();
            double neutralRatio = (double) neutral / sentiments.size();

            timePoint.put("timestamp", entry.getKey().toString());
            timePoint.put("positive_count", positive);
            timePoint.put("negative_count", negative);
            timePoint.put("neutral_count", neutral);
            timePoint.put("total_count", sentiments.size());
            
            timePoint.put("positive_ratio", String.format("%.2f%%", positiveRatio * 100));
            timePoint.put("negative_ratio", String.format("%.2f%%", negativeRatio * 100));
            timePoint.put("neutral_ratio", String.format("%.2f%%", neutralRatio * 100));
            
            // Calculate sentiment score (-1 to 1)
            double sentimentScore = (positive - negative) / (double) sentiments.size();
            timePoint.put("sentiment_score", String.format("%.2f", sentimentScore));

            timePoints.add(timePoint);
            
            // Accumulate for overall stats
            positiveSum += positive;
            negativeSum += negative;
            totalCount += sentiments.size();
        }

        analysis.put("time_points", timePoints);
        analysis.put("trend", calculateTrend(timePoints));
        analysis.put("overall_positive_ratio", String.format("%.2f%%", (positiveSum / totalCount) * 100));
        analysis.put("overall_negative_ratio", String.format("%.2f%%", (negativeSum / totalCount) * 100));
        analysis.put("total_records", (int) totalCount);
        
        // Calculate volatility (how much sentiment changes over time)
        double volatility = calculateVolatility(timePoints);
        analysis.put("sentiment_volatility", String.format("%.2f", volatility));

        return analysis;
    }

    /**
     * Calculate trend based on first and last sentiment ratio
     */
    private String calculateTrend(List<Map<String, Object>> timePoints) {
        if (timePoints.size() < 2) {
            return "INSUFFICIENT_DATA";
        }

        // Extract positive ratio from first and last time point
        String firstRatioStr = (String) timePoints.get(0).get("positive_ratio");
        String lastRatioStr = (String) timePoints.get(timePoints.size() - 1).get("positive_ratio");
        
        double firstRatio = Double.parseDouble(firstRatioStr.replace("%", "")) / 100;
        double lastRatio = Double.parseDouble(lastRatioStr.replace("%", "")) / 100;

        double change = lastRatio - firstRatio;
        
        if (change > 0.15) {
            return "STRONGLY_IMPROVING";
        } else if (change > 0.05) {
            return "IMPROVING";
        } else if (change < -0.15) {
            return "STRONGLY_DETERIORATING";
        } else if (change < -0.05) {
            return "DETERIORATING";
        } else {
            return "STABLE";
        }
    }

    /**
     * Calculate volatility - how much sentiment fluctuates
     */
    private double calculateVolatility(List<Map<String, Object>> timePoints) {
        if (timePoints.size() < 2) return 0;
        
        double sumSquaredDifferences = 0;
        double prevScore = 0;
        
        for (Map<String, Object> point : timePoints) {
            String scoreStr = (String) point.get("sentiment_score");
            double score = Double.parseDouble(scoreStr);
            
            if (prevScore != 0) {
                sumSquaredDifferences += Math.pow(score - prevScore, 2);
            }
            prevScore = score;
        }
        
        return Math.sqrt(sumSquaredDifferences / (timePoints.size() - 1));
    }

    /**
     * Determine effectiveness based on sentiment analysis
     */
    private Map<String, Object> determineEffectiveness(Map<String, Object> categoryAnalysis) {
        Map<String, Object> effectiveness = new LinkedHashMap<>();
        
        String trend = (String) categoryAnalysis.get("trend");
        String positiveRatioStr = (String) categoryAnalysis.get("overall_positive_ratio");
        double positiveRatio = Double.parseDouble(positiveRatioStr.replace("%", "")) / 100;
        
        effectiveness.put("trend", trend);
        effectiveness.put("positive_sentiment_percentage", positiveRatioStr);
        
        // Effectiveness determination
        String status;
        String recommendation;
        
        if ("STRONGLY_IMPROVING".equals(trend)) {
            status = "HIGHLY EFFECTIVE";
            recommendation = "Continue current approach - strong positive momentum";
        } else if ("IMPROVING".equals(trend)) {
            status = "EFFECTIVE";
            recommendation = "Current efforts are working - maintain and optimize";
        } else if ("STRONGLY_DETERIORATING".equals(trend)) {
            status = "CRITICAL - NEEDS URGENT ATTENTION";
            recommendation = "Immediate intervention required - sentiment declining rapidly";
        } else if ("DETERIORATING".equals(trend)) {
            status = "NEEDS ATTENTION";
            recommendation = "Monitor closely and adjust strategy";
        } else {
            status = "STABLE";
            recommendation = "Maintain current operations while seeking improvements";
        }
        
        // Also consider positive ratio threshold
        if (positiveRatio > 0.7) {
            status = "HIGHLY EFFECTIVE";
        } else if (positiveRatio < 0.3) {
            status = "CRITICAL - NEEDS URGENT ATTENTION";
        }
        
        effectiveness.put("status", status);
        effectiveness.put("recommendation", recommendation);
        
        return effectiveness;
    }

    /**
     * Generate detailed insights for a relief category
     */
    private Map<String, Object> generateDetailedInsights(String categoryName, Map<String, Object> analysis) {
        Map<String, Object> insights = new LinkedHashMap<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> timePoints = (List<Map<String, Object>>) analysis.get("time_points");
        
        // Find peak and lowest sentiment times
        Map<String, Object> peakTime = timePoints.stream()
                .max(Comparator.comparingDouble(t -> {
                    String scoreStr = (String) t.get("sentiment_score");
                    return Double.parseDouble(scoreStr);
                }))
                .orElse(null);
        
        Map<String, Object> lowestTime = timePoints.stream()
                .min(Comparator.comparingDouble(t -> {
                    String scoreStr = (String) t.get("sentiment_score");
                    return Double.parseDouble(scoreStr);
                }))
                .orElse(null);
        
        insights.put("peak_sentiment_time", peakTime != null ? peakTime.get("timestamp") : "N/A");
        insights.put("peak_sentiment_score", peakTime != null ? peakTime.get("sentiment_score") : "N/A");
        insights.put("lowest_sentiment_time", lowestTime != null ? lowestTime.get("timestamp") : "N/A");
        insights.put("lowest_sentiment_score", lowestTime != null ? lowestTime.get("sentiment_score") : "N/A");
        
        // Generate narrative
        String narrative = generateNarrative(categoryName, analysis);
        insights.put("narrative", narrative);
        
        return insights;
    }

    /**
     * Generate narrative analysis for sector
     */
    private String generateNarrative(String categoryName, Map<String, Object> analysis) {
        String trend = (String) analysis.get("trend");
        String positiveStr = (String) analysis.get("overall_positive_ratio");
        double positive = Double.parseDouble(positiveStr.replace("%", "")) / 100;
        
        StringBuilder narrative = new StringBuilder();
        narrative.append(categoryName).append(": ");
        
        if ("STRONGLY_IMPROVING".equals(trend) && positive > 0.7) {
            narrative.append("Strong increase in positive sentiment. ")
                    .append(categoryName)
                    .append(" aid distribution activities are well regarded and demonstrating high effectiveness. ")
                    .append("Public confidence in this relief sector is growing significantly.");
        } else if ("IMPROVING".equals(trend) && positive > 0.6) {
            narrative.append("Positive sentiment is increasing over time. ")
                    .append(categoryName)
                    .append(" relief efforts are being received favorably, indicating reasonable effectiveness.");
        } else if ("STABLE".equals(trend) && positive > 0.6) {
            narrative.append("Consistent positive sentiment maintained. ")
                    .append(categoryName)
                    .append(" services are stable and meeting expectations.");
        } else if ("DETERIORATING".equals(trend) || "STRONGLY_DETERIORATING".equals(trend)) {
            narrative.append("Declining sentiment detected. ")
                    .append(categoryName)
                    .append(" relief efforts are facing challenges or public dissatisfaction. ")
                    .append("This sector requires strategic review and potential intervention.");
        } else if (positive < 0.4) {
            narrative.append("Significant negative sentiment. ")
                    .append(categoryName)
                    .append(" shows infrastructure damage, service gaps, or ongoing unmet needs. ")
                    .append("Urgent attention and resource allocation recommended.");
        }
        
        return narrative.toString();
    }

    /**
     * Generate overall summary of all sectors
     */
    private Map<String, Object> generateSummary(Map<String, Object> sectorEffectiveness) {
        Map<String, Object> summary = new LinkedHashMap<>();
        
        int effectiveCount = 0;
        int needsAttentionCount = 0;
        int criticalCount = 0;
        
        for (Object value : sectorEffectiveness.values()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sector = (Map<String, Object>) value;
            String status = (String) sector.get("status");
            
            if (status.contains("HIGHLY EFFECTIVE")) {
                effectiveCount++;
            } else if (status.contains("NEEDS ATTENTION")) {
                needsAttentionCount++;
            } else if (status.contains("CRITICAL")) {
                criticalCount++;
            }
        }
        
        summary.put("highly_effective_sectors", effectiveCount);
        summary.put("sectors_needing_attention", needsAttentionCount);
        summary.put("critical_sectors", criticalCount);
        summary.put("total_sectors_analyzed", sectorEffectiveness.size());
        
        return summary;
    }

    @Override
    public String getModuleName() {
        return "Time Series Sentiment Module (Problem 2)";
    }

    @Override
    public String getDescription() {
        return "Tracks sentiment changes over time for each relief item category to determine sector effectiveness. " +
               "Analyzes trends, volatility, and generates recommendations for each relief sector.";
    }
}
