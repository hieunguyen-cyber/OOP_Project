package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.model.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Advanced Analysis Panel with Problem 1 and Problem 2 visualizations.
 * Problem 1: Satisfaction analysis per relief category
 * Problem 2: Temporal sentiment tracking and trend analysis
 */
public class AnalysisPanel extends JPanel {
    private final Model model;
    private JTabbedPane tabbedPane;
    private JTextArea problem1ResultsArea;
    private JTextArea problem2ResultsArea;
    private JTextArea comparisonResultsArea;
    private ChartPanel problem1ChartPanel;
    private ChartPanel problem2ChartPanel;
    private ChartPanel trendChartPanel;
    private JComboBox<String> problem1DisasterCombo;
    private JComboBox<String> problem2DisasterCombo;
    private JComboBox<String> comparisonDisasterCombo;

    public AnalysisPanel(Model model) {
        this.model = model;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Advanced Analysis - Problem 1 & 2"));

        // Create tabbed pane with per-tab disaster selectors
        tabbedPane = new JTabbedPane();

        // Problem 1 Tab
        JPanel problem1Panel = createProblem1Panel();
        tabbedPane.addTab("Problem 1: Satisfaction Analysis", problem1Panel);

        // Problem 2 Tab
        JPanel problem2Panel = createProblem2Panel();
        tabbedPane.addTab("Problem 2: Temporal Sentiment Tracking", problem2Panel);

        // Comparison Tab
        JPanel comparisonPanel = createComparisonPanel();
        tabbedPane.addTab("Overall Comparison", comparisonPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createProblem1Panel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: Disaster selector for this tab
        JPanel selectorPanel = new JPanel();
        selectorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.setBorder(BorderFactory.createTitledBorder("Disaster Type"));
        
        problem1DisasterCombo = new JComboBox<>();
        problem1DisasterCombo.addItem("All Disasters");
        List<String> disasterNames = DisasterManager.getInstance().getAllDisasterNames();
        System.out.println("DEBUG: Problem1 loaded " + disasterNames.size() + " disaster names: " + disasterNames);
        for (String name : disasterNames) {
            problem1DisasterCombo.addItem(name);
        }
        problem1DisasterCombo.setPreferredSize(new Dimension(150, 25));
        problem1DisasterCombo.addActionListener(e -> updateProblem1Analysis());
        selectorPanel.add(problem1DisasterCombo);
        
        JButton analyzeBtn = new JButton("Analyze");
        analyzeBtn.addActionListener(e -> updateProblem1Analysis());
        selectorPanel.add(analyzeBtn);
        panel.add(selectorPanel, BorderLayout.NORTH);

        // Center: Chart and Results
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(10, 10));
        
        // Chart
        problem1ChartPanel = new ChartPanel(null);
        problem1ChartPanel.setPreferredSize(new Dimension(600, 300));
        contentPanel.add(problem1ChartPanel, BorderLayout.NORTH);
        
        // Results
        problem1ResultsArea = new JTextArea(10, 50);
        problem1ResultsArea.setEditable(false);
        problem1ResultsArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JScrollPane scrollPane = new JScrollPane(problem1ResultsArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProblem2Panel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: Disaster selector for this tab
        JPanel selectorPanel = new JPanel();
        selectorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.setBorder(BorderFactory.createTitledBorder("Disaster Type"));
        
        problem2DisasterCombo = new JComboBox<>();
        problem2DisasterCombo.addItem("All Disasters");
        List<String> disasterNames = DisasterManager.getInstance().getAllDisasterNames();
        for (String name : disasterNames) {
            problem2DisasterCombo.addItem(name);
        }
        problem2DisasterCombo.setPreferredSize(new Dimension(150, 25));
        problem2DisasterCombo.addActionListener(e -> updateProblem2Analysis());
        selectorPanel.add(problem2DisasterCombo);
        
        JButton analyzeBtn = new JButton("Analyze");
        analyzeBtn.addActionListener(e -> updateProblem2Analysis());
        selectorPanel.add(analyzeBtn);
        panel.add(selectorPanel, BorderLayout.NORTH);

        // Center: Charts and Results
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        // Trend Chart
        trendChartPanel = new ChartPanel(null);
        trendChartPanel.setPreferredSize(new Dimension(600, 250));
        contentPanel.add(trendChartPanel);

        // Time Series Chart
        problem2ChartPanel = new ChartPanel(null);
        problem2ChartPanel.setPreferredSize(new Dimension(600, 250));
        contentPanel.add(problem2ChartPanel);
        
        // Results
        problem2ResultsArea = new JTextArea(8, 50);
        problem2ResultsArea.setEditable(false);
        problem2ResultsArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JScrollPane scrollPane = new JScrollPane(problem2ResultsArea);
        contentPanel.add(scrollPane);
        
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createComparisonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: Disaster selector for this tab
        JPanel selectorPanel = new JPanel();
        selectorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.setBorder(BorderFactory.createTitledBorder("Disaster Type"));
        
        comparisonDisasterCombo = new JComboBox<>();
        comparisonDisasterCombo.addItem("All Disasters");
        List<String> disasterNames = DisasterManager.getInstance().getAllDisasterNames();
        for (String name : disasterNames) {
            comparisonDisasterCombo.addItem(name);
        }
        comparisonDisasterCombo.setPreferredSize(new Dimension(150, 25));
        comparisonDisasterCombo.addActionListener(e -> updateComparisonAnalysis(comparisonResultsArea));
        selectorPanel.add(comparisonDisasterCombo);
        
        JButton analyzeBtn = new JButton("Analyze");
        analyzeBtn.addActionListener(e -> updateComparisonAnalysis(comparisonResultsArea));
        selectorPanel.add(analyzeBtn);
        panel.add(selectorPanel, BorderLayout.NORTH);

        // Center: Results area
        comparisonResultsArea = new JTextArea(30, 60);
        comparisonResultsArea.setEditable(false);
        comparisonResultsArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JScrollPane scrollPane = new JScrollPane(comparisonResultsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void updateProblem1Analysis() {
        try {
            List<Post> posts = getFilteredPosts((String) problem1DisasterCombo.getSelectedItem());
            if (posts.isEmpty()) {
                problem1ResultsArea.setText("No data available for selected disaster type. Please add posts first.");
                return;
            }

            // Problem 1: Satisfaction Analysis per Relief Category
            StringBuilder results = new StringBuilder();
            results.append("=== PROBLEM 1: PUBLIC SATISFACTION ANALYSIS ===\n");
            
            String selectedDisaster = (String) problem1DisasterCombo.getSelectedItem();
            if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                results.append("Disaster Type: ").append(selectedDisaster).append("\n");
            }
            
            results.append("Determining public satisfaction/dissatisfaction per relief item\n\n");

            // Group by relief category
            Map<ReliefItem.Category, List<Post>> byCategory = posts.stream()
                .filter(p -> p.getReliefItem() != null)
                .collect(Collectors.groupingBy(p -> p.getReliefItem().getCategory()));

            // Create dataset for chart
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            byCategory.forEach((category, categoryPosts) -> {
                int total = categoryPosts.size();
                int positive = (int) categoryPosts.stream()
                    .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
                    .count();
                int negative = (int) categoryPosts.stream()
                    .filter(p -> p.getSentiment() != null && p.getSentiment().isNegative())
                    .count();
                int neutral = total - positive - negative;

                double positivePercent = (double) positive / total * 100;
                double negativePercent = (double) negative / total * 100;
                double neutralPercent = (double) neutral / total * 100;

                results.append(String.format("Category: %s\n", category.getDisplayName()));
                results.append(String.format("  Total Posts: %d\n", total));
                results.append(String.format("  Positive: %d (%.1f%%)\n", positive, positivePercent));
                results.append(String.format("  Negative: %d (%.1f%%)\n", negative, negativePercent));
                results.append(String.format("  Neutral: %d (%.1f%%)\n", neutral, neutralPercent));

                if (negativePercent > 50) {
                    results.append("  ⚠ STATUS: URGENT - More than 50% negative sentiment\n");
                } else if (positivePercent > 60) {
                    results.append("  ✓ STATUS: SATISFIED - More than 60% positive sentiment\n");
                } else {
                    results.append("  ◆ STATUS: NEUTRAL - Balanced sentiment\n");
                }
                results.append("\n");

                // Add to dataset
                dataset.addValue(positivePercent, "Positive", category.getDisplayName());
                dataset.addValue(negativePercent, "Negative", category.getDisplayName());
                dataset.addValue(neutralPercent, "Neutral", category.getDisplayName());
            });

            problem1ResultsArea.setText(results.toString());

            // Create and display chart
            JFreeChart chart = ChartFactory.createStackedBarChart(
                "Public Satisfaction by Relief Category (Problem 1)",
                "Relief Category",
                "Percentage (%)",
                dataset
            );

            problem1ChartPanel.setChart(chart);
        } catch (Exception e) {
            problem1ResultsArea.setText("Error: " + e.getMessage());
        }
    }

    private void updateProblem2Analysis() {
        try {
            List<Post> posts = getFilteredPosts((String) problem2DisasterCombo.getSelectedItem());
            if (posts.isEmpty()) {
                problem2ResultsArea.setText("No data available for selected disaster type. Please add posts first.");
                return;
            }

            StringBuilder results = new StringBuilder();
            results.append("=== PROBLEM 2: TEMPORAL SENTIMENT TRACKING ===\n");
            
            String selectedDisaster = (String) problem2DisasterCombo.getSelectedItem();
            if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                results.append("Disaster Type: ").append(selectedDisaster).append("\n");
            }
            
            results.append("Analyzing sentiment evolution over time per relief item\n\n");

            // Group by time buckets (6-hour intervals)
            Map<String, List<Post>> byTimeBucket = posts.stream()
                .collect(Collectors.groupingBy(p -> {
                    LocalDateTime dt = p.getCreatedAt();
                    int hour = dt.getHour();
                    int bucket = hour / 6;
                    return String.format("%02d:00-%02d:59", bucket * 6, (bucket + 1) * 6 - 1);
                }));

            // Create dataset for time series chart
            DefaultCategoryDataset timeDataset = new DefaultCategoryDataset();
            DefaultCategoryDataset trendDataset = new DefaultCategoryDataset();

            byTimeBucket.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String timeBucket = entry.getKey();
                    List<Post> bucketPosts = entry.getValue();

                    int positive = (int) bucketPosts.stream()
                        .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
                        .count();
                    int negative = (int) bucketPosts.stream()
                        .filter(p -> p.getSentiment() != null && p.getSentiment().isNegative())
                        .count();
                    int neutral = bucketPosts.size() - positive - negative;

                    timeDataset.addValue(positive, "Positive", timeBucket);
                    timeDataset.addValue(negative, "Negative", timeBucket);
                    timeDataset.addValue(neutral, "Neutral", timeBucket);

                    trendDataset.addValue(positive - negative, "Sentiment Score", timeBucket);

                    results.append(String.format("Time Period: %s\n", timeBucket));
                    results.append(String.format("  Posts: %d | Pos: %d | Neg: %d | Neutral: %d\n", 
                        bucketPosts.size(), positive, negative, neutral));
                });

            // Detect trends per category
            results.append("\n--- TREND ANALYSIS BY CATEGORY ---\n");
            Map<ReliefItem.Category, List<Post>> byCategory = posts.stream()
                .filter(p -> p.getReliefItem() != null)
                .collect(Collectors.groupingBy(p -> p.getReliefItem().getCategory()));

            byCategory.forEach((category, categoryPosts) -> {
                int positive = (int) categoryPosts.stream()
                    .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
                    .count();
                int negative = (int) categoryPosts.stream()
                    .filter(p -> p.getSentiment() != null && p.getSentiment().isNegative())
                    .count();

                String trend = positive > negative ? "IMPROVING ↗" : 
                              negative > positive ? "DETERIORATING ↘" : "STABLE →";
                results.append(String.format("%s: %s\n", category.getDisplayName(), trend));
            });

            problem2ResultsArea.setText(results.toString());

            // Create charts
            JFreeChart timeChart = ChartFactory.createStackedBarChart(
                "Sentiment Distribution Over Time (Problem 2)",
                "Time Period",
                "Number of Posts",
                timeDataset
            );

            problem2ChartPanel.setChart(timeChart);

            JFreeChart trendChart = ChartFactory.createLineChart(
                "Sentiment Trend Score (Positive - Negative)",
                "Time Period",
                "Score",
                trendDataset
            );

            trendChartPanel.setChart(trendChart);
        } catch (Exception e) {
            problem2ResultsArea.setText("Error: " + e.getMessage());
        }
    }

    private void updateComparisonAnalysis(JTextArea comparisonArea) {
        try {
            List<Post> posts = getFilteredPosts((String) comparisonDisasterCombo.getSelectedItem());
            if (posts.isEmpty()) {
                comparisonArea.setText("No data available for selected disaster type.");
                return;
            }

            StringBuilder comparison = new StringBuilder();
            comparison.append("=== COMPREHENSIVE ANALYSIS COMPARISON ===\n");
            
            String selectedDisaster = (String) comparisonDisasterCombo.getSelectedItem();
            if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                comparison.append("Disaster Type: ").append(selectedDisaster).append("\n");
            }
            
            comparison.append("\n");
            comparison.append("PROBLEM 1: SATISFACTION ANALYSIS (Public Satisfaction per Relief Item)\n");
            comparison.append("-".repeat(70)).append("\n");

            Map<ReliefItem.Category, List<Post>> byCategory = posts.stream()
                .filter(p -> p.getReliefItem() != null)
                .collect(Collectors.groupingBy(p -> p.getReliefItem().getCategory()));

            byCategory.forEach((category, categoryPosts) -> {
                int total = categoryPosts.size();
                int positive = (int) categoryPosts.stream()
                    .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
                    .count();
                double positivePercent = (double) positive / total * 100;

                comparison.append(String.format("%-20s: %.1f%% satisfied (%d/%d posts)\n", 
                    category.getDisplayName(), positivePercent, positive, total));
            });

            comparison.append("\n");
            comparison.append("PROBLEM 2: TEMPORAL SENTIMENT TRACKING (Sentiment Trends Over Time)\n");
            comparison.append("-".repeat(70)).append("\n");

            Map<String, Integer> sentimentTrend = new LinkedHashMap<>();
            posts.forEach(post -> {
                LocalDateTime dt = post.getCreatedAt();
                int hour = dt.getHour();
                int bucket = hour / 6;
                String timeBucket = String.format("%02d:00-%02d:59", bucket * 6, (bucket + 1) * 6 - 1);

                int score = 0;
                if (post.getSentiment() != null) {
                    if (post.getSentiment().isPositive()) score = 1;
                    else if (post.getSentiment().isNegative()) score = -1;
                }

                sentimentTrend.put(timeBucket, sentimentTrend.getOrDefault(timeBucket, 0) + score);
            });

            sentimentTrend.forEach((timeBucket, score) -> {
                String trend = score > 0 ? "IMPROVING ↗" : score < 0 ? "DETERIORATING ↘" : "STABLE →";
                comparison.append(String.format("%-20s: Score %3d - %s\n", timeBucket, score, trend));
            });

            comparison.append("\n");
            comparison.append("OVERALL STATISTICS\n");
            comparison.append("-".repeat(70)).append("\n");

            int totalPosts = posts.size();
            int totalComments = posts.stream().mapToInt(p -> p.getComments().size()).sum();
            int positive = (int) posts.stream()
                .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
                .count();
            int negative = (int) posts.stream()
                .filter(p -> p.getSentiment() != null && p.getSentiment().isNegative())
                .count();

            comparison.append(String.format("Total Posts: %d\n", totalPosts));
            comparison.append(String.format("Total Comments: %d\n", totalComments));
            comparison.append(String.format("Overall Positive: %d (%.1f%%)\n", positive, (double)positive/totalPosts*100));
            comparison.append(String.format("Overall Negative: %d (%.1f%%)\n", negative, (double)negative/totalPosts*100));
            comparison.append(String.format("Satisfaction Rate: %.1f%%\n", (double)positive/totalPosts*100));

            comparisonArea.setText(comparison.toString());
        } catch (Exception e) {
            comparisonArea.setText("Error: " + e.getMessage());
        }
    }

    public void refresh() {
        updateProblem1Analysis();
        updateProblem2Analysis();
        if (comparisonResultsArea != null) {
            updateComparisonAnalysis(comparisonResultsArea);
        }
    }

    /**
     * Get posts filtered by the selected disaster type
     */
    private List<Post> getFilteredPosts(String disasterName) {
        List<Post> allPosts = model.getPosts();
        
        // If "All Disasters" is selected, return all posts
        if (disasterName == null || disasterName.equals("All Disasters")) {
            return allPosts;
        }
        
        // Filter posts by disaster type
        return allPosts.stream()
            .filter(p -> {
                if (p instanceof FacebookPost) {
                    FacebookPost fbPost = (FacebookPost) p;
                    DisasterType type = fbPost.getDisasterType();
                    return type != null && type.getName().equals(disasterName);
                }
                return false;
            })
            .collect(Collectors.toList());
    }
}
