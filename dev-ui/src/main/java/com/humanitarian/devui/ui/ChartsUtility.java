package com.humanitarian.devui.ui;

import com.humanitarian.devui.model.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ChartsUtility {

    public static JFreeChart createSentimentPieChart(List<Post> posts) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        long positive = posts.stream()
            .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
            .count();
        long negative = posts.stream()
            .filter(p -> p.getSentiment() != null && p.getSentiment().isNegative())
            .count();
        long neutral = posts.stream()
            .filter(p -> p.getSentiment() != null && p.getSentiment().isNeutral())
            .count();

        dataset.setValue("Positive (" + positive + ")", positive);
        dataset.setValue("Negative (" + negative + ")", negative);
        dataset.setValue("Neutral (" + neutral + ")", neutral);

        return ChartFactory.createPieChart("Sentiment Distribution", dataset);
    }

    public static JFreeChart createCategoryBarChart(List<Post> posts) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<ReliefItem.Category, Long> categoryCount = posts.stream()
            .filter(p -> p.getReliefItem() != null)
            .collect(Collectors.groupingBy(
                p -> p.getReliefItem().getCategory(),
                Collectors.counting()
            ));

        categoryCount.forEach((category, count) -> {
            dataset.addValue(count, "Posts", category.getDisplayName());
        });

        return ChartFactory.createBarChart(
            "Posts by Relief Category",
            "Relief Category",
            "Number of Posts",
            dataset
        );
    }

    public static JFreeChart createStackedSentimentByCategoryChart(List<Post> posts) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<ReliefItem.Category, List<Post>> byCategory = posts.stream()
            .filter(p -> p.getReliefItem() != null)
            .collect(Collectors.groupingBy(p -> p.getReliefItem().getCategory()));

        byCategory.forEach((category, categoryPosts) -> {
            long positive = categoryPosts.stream()
                .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
                .count();
            long negative = categoryPosts.stream()
                .filter(p -> p.getSentiment() != null && p.getSentiment().isNegative())
                .count();
            long neutral = categoryPosts.size() - positive - negative;

            dataset.addValue(positive, "Positive", category.getDisplayName());
            dataset.addValue(negative, "Negative", category.getDisplayName());
            dataset.addValue(neutral, "Neutral", category.getDisplayName());
        });

        return ChartFactory.createStackedBarChart(
            "Sentiment by Relief Category",
            "Relief Category",
            "Number of Posts",
            dataset
        );
    }

    public static JFreeChart createConfidenceTrendChart(List<Post> posts) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries positiveSeries = new XYSeries("Positive Confidence");
        XYSeries negativeSeries = new XYSeries("Negative Confidence");

        List<Post> sortedPosts = posts.stream()
            .sorted(Comparator.comparing(Post::getCreatedAt))
            .collect(Collectors.toList());

        for (int i = 0; i < sortedPosts.size(); i++) {
            Post post = sortedPosts.get(i);
            if (post.getSentiment() != null) {
                if (post.getSentiment().isPositive()) {
                    positiveSeries.add(i, post.getSentiment().getConfidence());
                } else if (post.getSentiment().isNegative()) {
                    negativeSeries.add(i, post.getSentiment().getConfidence());
                }
            }
        }

        dataset.addSeries(positiveSeries);
        dataset.addSeries(negativeSeries);

        return ChartFactory.createXYLineChart(
            "Sentiment Confidence Trend",
            "Post Index",
            "Confidence Score",
            dataset
        );
    }

    public static JFreeChart createProblem1Chart(Map<ReliefItem.Category, Double> satisfactionMap) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        satisfactionMap.forEach((category, satisfaction) -> {
            dataset.addValue(satisfaction, "Satisfaction %", category.getDisplayName());
        });

        return ChartFactory.createBarChart(
            "Problem 1: Public Satisfaction by Relief Category",
            "Relief Category",
            "Satisfaction Percentage (%)",
            dataset
        );
    }

    public static JFreeChart createTemporalComparisonChart(Map<String, Integer> sentimentScores) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        sentimentScores.forEach((timeBucket, score) -> {
            if (score != null) {
                dataset.addValue(score > 0 ? score : 0, "Positive", timeBucket);
                dataset.addValue(score < 0 ? Math.abs(score) : 0, "Negative", timeBucket);
            }
        });

        return ChartFactory.createStackedBarChart(
            "Problem 2: Sentiment Distribution Over Time",
            "Time Period",
            "Sentiment Count",
            dataset
        );
    }

    public static JFreeChart createAuthorContributionChart(List<Post> posts) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<String, Long> authorCount = posts.stream()
            .collect(Collectors.groupingBy(Post::getAuthor, Collectors.counting()));

        authorCount.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(10)
            .forEach(entry -> {
                dataset.addValue(entry.getValue(), "Posts", entry.getKey());
            });

        return ChartFactory.createBarChart(
            "Top Authors by Contribution",
            "Author",
            "Number of Posts",
            dataset
        );
    }

    public static JFreeChart createCombinedAnalysisChart(List<Post> posts) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int totalPosts = posts.size();
        int totalComments = posts.stream().mapToInt(p -> p.getComments().size()).sum();
        long positive = posts.stream()
            .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
            .count();
        long negative = posts.stream()
            .filter(p -> p.getSentiment() != null && p.getSentiment().isNegative())
            .count();

        dataset.addValue(positive, "Positive", "Sentiment");
        dataset.addValue(negative, "Negative", "Sentiment");
        dataset.addValue(totalPosts, "Posts", "Metrics");
        dataset.addValue(totalComments, "Comments", "Metrics");

        return ChartFactory.createBarChart(
            "Combined Analysis Overview",
            "Category",
            "Count",
            dataset
        );
    }
}
