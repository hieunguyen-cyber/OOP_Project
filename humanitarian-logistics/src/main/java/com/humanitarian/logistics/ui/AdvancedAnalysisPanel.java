package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.model.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Advanced Analysis Panel with detailed visualizations for Problem 1 & 2.
 */
public class AdvancedAnalysisPanel extends JPanel {
    private Model model;
    private JTabbedPane mainTabs;

    public AdvancedAnalysisPanel(Model model) {
        this.model = model;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Advanced Analysis System"));

        mainTabs = new JTabbedPane();
        mainTabs.addTab("📊 Problem 1: Satisfaction", createProblem1Tab());
        mainTabs.addTab("📈 Problem 2: Temporal", createProblem2Tab());
        mainTabs.addTab("🔄 Combined Report", createCombinedTab());

        add(mainTabs, BorderLayout.CENTER);
    }

    private JPanel createProblem1Tab() {
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();

        // === NEW: By Individual Category with Selector ===
        JPanel individualCategoryPanel = new JPanel(new BorderLayout());
        
        // Create top panel with disaster AND category selectors
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.setBorder(BorderFactory.createTitledBorder("Select Disaster & Relief Category"));
        
        // Disaster selector
        JLabel disasterLabel = new JLabel("Disaster Type: ");
        JComboBox<String> disasterSelector = new JComboBox<>();
        disasterSelector.addItem("All Disasters");
        List<String> disasterNames = DisasterManager.getInstance().getAllDisasterNames();
        for (String name : disasterNames) {
            disasterSelector.addItem(name);
        }
        disasterSelector.setPreferredSize(new Dimension(120, 25));
        
        selectorPanel.add(disasterLabel);
        selectorPanel.add(disasterSelector);
        selectorPanel.add(new JLabel("  |  Category: "));
        
        // Category selector
        JComboBox<String> categorySelector = new JComboBox<>();
        categorySelector.addItem("ALL CATEGORIES");
        for (ReliefItem.Category cat : ReliefItem.Category.values()) {
            categorySelector.addItem(cat.getDisplayName());
        }
        selectorPanel.add(categorySelector);
        
        // Chart and text area
        ChartPanel chartPanel0 = new ChartPanel(null);
        chartPanel0.setPreferredSize(new Dimension(800, 350));
        InteractiveChartUtility.makeChartInteractive(chartPanel0);
        
        JTextArea textArea0 = new JTextArea(8, 50);
        textArea0.setEditable(false);
        textArea0.setFont(new Font("Monospaced", Font.PLAIN, 9));
        
        JButton btnAnalyzeCategory = new JButton("Analyze");
        btnAnalyzeCategory.addActionListener(e -> {
            try {
                // Get selected disaster and category
                String selectedDisaster = (String) disasterSelector.getSelectedItem();
                String selectedCategory = (String) categorySelector.getSelectedItem();
                
                // Filter posts by disaster type first
                List<Post> posts = model.getPosts();
                if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                    posts = posts.stream()
                        .filter(p -> {
                            if (p instanceof FacebookPost) {
                                FacebookPost fbPost = (FacebookPost) p;
                                DisasterType type = fbPost.getDisasterType();
                                return type != null && type.getName().equals(selectedDisaster);
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
                }
                
                StringBuilder sb = new StringBuilder();
                sb.append("=== SATISFACTION ANALYSIS: ").append(selectedCategory).append(" ===\n\n");
                
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                
                if ("ALL CATEGORIES".equals(selectedCategory)) {
                    // Show all categories comparison
                    Map<ReliefItem.Category, List<Post>> byCategory = posts.stream()
                        .filter(p -> p.getReliefItem() != null)
                        .collect(Collectors.groupingBy(p -> p.getReliefItem().getCategory()));
                    
                    byCategory.forEach((category, categoryPosts) -> {
                        int total = categoryPosts.size();
                        if (total == 0) return;
                        
                        long positive = categoryPosts.stream()
                            .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
                            .count();
                        long negative = categoryPosts.stream()
                            .filter(p -> p.getSentiment() != null && p.getSentiment().isNegative())
                            .count();
                        long neutral = categoryPosts.stream()
                            .filter(p -> p.getSentiment() != null && p.getSentiment().isNeutral())
                            .count();
                        
                        double posPct = (double) positive / total * 100;
                        double negPct = (double) negative / total * 100;
                        double neuPct = (double) neutral / total * 100;
                        double satisfactionScore = (positive - negative) / (double) total;
                        
                        dataset.addValue(posPct, "Positive", category.getDisplayName());
                        dataset.addValue(negPct, "Negative", category.getDisplayName());
                        dataset.addValue(neuPct, "Neutral", category.getDisplayName());
                        
                        sb.append(String.format("📦 %s (Total: %d)\n", category.getDisplayName(), total));
                        sb.append(String.format("   Positive: %d (%.1f%%)\n", positive, posPct));
                        sb.append(String.format("   Negative: %d (%.1f%%)\n", negative, negPct));
                        sb.append(String.format("   Neutral:  %d (%.1f%%)\n", neutral, neuPct));
                        sb.append(String.format("   Satisfaction Score: %.2f\n", satisfactionScore));
                        
                        if (satisfactionScore > 0.6) sb.append("   ✅ HIGHLY EFFECTIVE\n");
                        else if (satisfactionScore > 0.2) sb.append("   ⚠️ NEEDS IMPROVEMENT\n");
                        else sb.append("   🚨 CRITICAL - URGENT ATTENTION\n");
                        sb.append("\n");
                    });
                    
                    JFreeChart chart = ChartFactory.createStackedBarChart(
                        "Satisfaction Analysis - All Categories",
                        "Relief Category", "Percentage (%)", dataset
                    );
                    chartPanel0.setChart(chart);
                } else {
                    // Show specific category details
                    ReliefItem.Category targetCategory = null;
                    for (ReliefItem.Category cat : ReliefItem.Category.values()) {
                        if (cat.getDisplayName().equals(selectedCategory)) {
                            targetCategory = cat;
                            break;
                        }
                    }
                    
                    final ReliefItem.Category finalCategory = targetCategory;
                    if (finalCategory != null) {
                        List<Post> categoryPosts = posts.stream()
                            .filter(p -> p.getReliefItem() != null && p.getReliefItem().getCategory() == finalCategory)
                            .collect(Collectors.toList());
                        
                        int total = categoryPosts.size();
                        long positive = categoryPosts.stream()
                            .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
                            .count();
                        long negative = categoryPosts.stream()
                            .filter(p -> p.getSentiment() != null && p.getSentiment().isNegative())
                            .count();
                        long neutral = total - positive - negative;
                        
                        double posPct = total > 0 ? (double) positive / total * 100 : 0;
                        double negPct = total > 0 ? (double) negative / total * 100 : 0;
                        double neuPct = total > 0 ? (double) neutral / total * 100 : 0;
                        double satisfactionScore = total > 0 ? (positive - negative) / (double) total : 0;
                        
                        // Create pie chart for selected category
                        DefaultPieDataset<String> pieDataset = new DefaultPieDataset<>();
                        pieDataset.setValue("Positive (" + positive + ")", posPct);
                        pieDataset.setValue("Negative (" + negative + ")", negPct);
                        pieDataset.setValue("Neutral (" + neutral + ")", neuPct);
                        
                        JFreeChart chart = ChartFactory.createPieChart(
                            "Sentiment Distribution: " + selectedCategory, pieDataset
                        );
                        chartPanel0.setChart(chart);
                        
                        sb.append(String.format("📊 Detailed Analysis for: %s\n\n", selectedCategory));
                        sb.append(String.format("Total Records: %d\n", total));
                        sb.append(String.format("Positive: %d (%.1f%%)\n", positive, posPct));
                        sb.append(String.format("Negative: %d (%.1f%%)\n", negative, negPct));
                        sb.append(String.format("Neutral:  %d (%.1f%%)\n", neutral, neuPct));
                        sb.append(String.format("\nSatisfaction Score: %.2f\n\n", satisfactionScore));
                        
                        // Effectiveness assessment
                        if (satisfactionScore > 0.6) {
                            sb.append("✅ STATUS: HIGHLY EFFECTIVE\n");
                            sb.append("Assessment: This relief category is well-received\n");
                            sb.append("Recommendation: Maintain and scale current operations\n");
                        } else if (satisfactionScore > 0.2) {
                            sb.append("⚠️ STATUS: SATISFACTORY\n");
                            sb.append("Assessment: Relief efforts are working but need optimization\n");
                            sb.append("Recommendation: Monitor closely and optimize delivery\n");
                        } else if (satisfactionScore > -0.2) {
                            sb.append("🟡 STATUS: NEUTRAL\n");
                            sb.append("Assessment: Mixed sentiment, unclear effectiveness\n");
                            sb.append("Recommendation: Gather more data and review strategy\n");
                        } else if (satisfactionScore > -0.6) {
                            sb.append("🔴 STATUS: NEEDS URGENT ATTENTION\n");
                            sb.append("Assessment: More negative than positive sentiment\n");
                            sb.append("Recommendation: Investigate issues and adjust strategy\n");
                        } else {
                            sb.append("🚨 STATUS: CRITICAL\n");
                            sb.append("Assessment: High dissatisfaction detected\n");
                            sb.append("Recommendation: Urgent intervention required\n");
                        }
                        
                        // List individual posts
                        sb.append("\n\n📝 Recent Posts/Comments for this category:\n");
                        categoryPosts.stream().limit(10).forEach(post -> {
                            sb.append(String.format("  - %s (%s): %s\n",
                                post.getAuthor(),
                                post.getSentiment().getType(),
                                post.getContent().substring(0, Math.min(50, post.getContent().length())) + "..."
                            ));
                        });
                    }
                }
                
                InteractiveChartUtility.enableChartInteractivity(chartPanel0);
                textArea0.setText(sb.toString());
                textArea0.setCaretPosition(0);
            } catch (Exception ex) {
                textArea0.setText("Error: " + ex.getMessage());
                // Log error
            }
        });
        
        JPanel buttonPanel0 = new JPanel();
        buttonPanel0.add(btnAnalyzeCategory);
        
        individualCategoryPanel.add(selectorPanel, BorderLayout.NORTH);
        individualCategoryPanel.add(chartPanel0, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel0, BorderLayout.NORTH);
        southPanel.add(new JScrollPane(textArea0), BorderLayout.CENTER);
        individualCategoryPanel.add(southPanel, BorderLayout.SOUTH);
        
        tabs.addTab("By Category (Selector)", individualCategoryPanel);

        // By Category
        JPanel categoryPanel = new JPanel(new BorderLayout());
        ChartPanel chartPanel1 = new ChartPanel(null);
        chartPanel1.setPreferredSize(new Dimension(800, 350));
        InteractiveChartUtility.makeChartInteractive(chartPanel1);
        JTextArea textArea1 = new JTextArea(6, 50);
        textArea1.setEditable(false);
        textArea1.setFont(new Font("Monospaced", Font.PLAIN, 9));

        categoryPanel.add(chartPanel1, BorderLayout.CENTER);
        categoryPanel.add(new JScrollPane(textArea1), BorderLayout.SOUTH);

        JButton btn1 = new JButton("Refresh");
        btn1.addActionListener(e -> {
            try {
                List<Post> posts = model.getPosts();
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                StringBuilder sb = new StringBuilder("=== SATISFACTION BY CATEGORY ===\n\n");

                Map<ReliefItem.Category, List<Post>> byCategory = posts.stream()
                    .filter(p -> p.getReliefItem() != null)
                    .collect(Collectors.groupingBy(p -> p.getReliefItem().getCategory()));

                byCategory.forEach((category, categoryPosts) -> {
                    int total = categoryPosts.size();
                    long positive = categoryPosts.stream()
                        .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
                        .count();
                    long negative = categoryPosts.stream()
                        .filter(p -> p.getSentiment() != null && p.getSentiment().isNegative())
                        .count();

                    double posPct = (double) positive / total * 100;
                    double negPct = (double) negative / total * 100;

                    dataset.addValue(posPct, "Positive", category.getDisplayName());
                    dataset.addValue(negPct, "Negative", category.getDisplayName());

                    sb.append(String.format("%s: Pos %.0f%% | Neg %.0f%% (%d posts)\n",
                        category.getDisplayName(), posPct, negPct, total));
                    if (negPct > 50) sb.append("  ⚠️ CRITICAL\n");
                    else if (posPct > 60) sb.append("  ✅ SATISFIED\n");
                });

                JFreeChart chart = ChartFactory.createStackedBarChart(
                    "Satisfaction by Category (Problem 1)",
                    "Category", "%", dataset
                );
                chartPanel1.setChart(chart);
                InteractiveChartUtility.enableChartInteractivity(chartPanel1);
                textArea1.setText(sb.toString());
            } catch (Exception ex) {
                textArea1.setText("Error: " + ex.getMessage());
            }
        });

        JPanel buttonPanel1 = new JPanel();
        buttonPanel1.add(btn1);
        categoryPanel.add(buttonPanel1, BorderLayout.SOUTH);
        tabs.addTab("By Category", categoryPanel);

        // Sentiment Distribution
        JPanel sentimentPanel = new JPanel(new BorderLayout());
        ChartPanel pieChartPanel = new ChartPanel(null);
        pieChartPanel.setPreferredSize(new Dimension(400, 350));
        InteractiveChartUtility.makeChartInteractive(pieChartPanel);
        JTextArea textArea2 = new JTextArea(10, 50);
        textArea2.setEditable(false);
        textArea2.setFont(new Font("Monospaced", Font.PLAIN, 9));

        sentimentPanel.add(pieChartPanel, BorderLayout.WEST);
        sentimentPanel.add(new JScrollPane(textArea2), BorderLayout.CENTER);

        JButton btn2 = new JButton("Refresh");
        btn2.addActionListener(e -> {
            try {
                List<Post> posts = model.getPosts();
                DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

                long pos = posts.stream().filter(p -> p.getSentiment() != null && p.getSentiment().isPositive()).count();
                long neg = posts.stream().filter(p -> p.getSentiment() != null && p.getSentiment().isNegative()).count();
                long neu = posts.size() - pos - neg;

                dataset.setValue("Positive (" + pos + ")", pos);
                dataset.setValue("Negative (" + neg + ")", neg);
                dataset.setValue("Neutral (" + neu + ")", neu);

                JFreeChart chart = ChartFactory.createPieChart("Sentiment Distribution", dataset);
                pieChartPanel.setChart(chart);
                InteractiveChartUtility.enableChartInteractivity(pieChartPanel);

                StringBuilder sb = new StringBuilder("=== DETAILED PROBLEM 1 ANALYSIS ===\n\n");
                Map<ReliefItem.Category, List<Post>> byCategory = posts.stream()
                    .filter(p -> p.getReliefItem() != null)
                    .collect(Collectors.groupingBy(p -> p.getReliefItem().getCategory()));

                byCategory.forEach((category, categoryPosts) -> {
                    sb.append(String.format("📦 %s (%d posts)\n", category.getDisplayName(), categoryPosts.size()));
                    categoryPosts.forEach(post -> {
                        sb.append(String.format("   - %s: %s (%.2f)\n",
                            post.getAuthor(),
                            post.getSentiment().getType(),
                            post.getSentiment().getConfidence()));
                    });
                    sb.append("\n");
                });

                textArea2.setText(sb.toString());
            } catch (Exception ex) {
                textArea2.setText("Error: " + ex.getMessage());
            }
        });

        JPanel buttonPanel2 = new JPanel();
        buttonPanel2.add(btn2);
        sentimentPanel.add(buttonPanel2, BorderLayout.SOUTH);
        tabs.addTab("Sentiment", sentimentPanel);

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProblem2Tab() {
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();

        // === NEW: By Category Temporal with Selector ===
        JPanel categoryTemporalPanel = new JPanel(new BorderLayout());
        
        // Create top panel with disaster AND category selectors
        JPanel selectorPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel2.setBorder(BorderFactory.createTitledBorder("Select Disaster & Relief Category for Temporal Analysis"));
        
        // Disaster selector
        JLabel disasterLabel2 = new JLabel("Disaster Type: ");
        JComboBox<String> disasterSelector2 = new JComboBox<>();
        disasterSelector2.addItem("All Disasters");
        List<String> disasterNames2 = DisasterManager.getInstance().getAllDisasterNames();
        for (String name : disasterNames2) {
            disasterSelector2.addItem(name);
        }
        disasterSelector2.setPreferredSize(new Dimension(120, 25));
        
        selectorPanel2.add(disasterLabel2);
        selectorPanel2.add(disasterSelector2);
        selectorPanel2.add(new JLabel("  |  Category: "));
        
        // Category selector
        JComboBox<String> categorySelector2 = new JComboBox<>();
        categorySelector2.addItem("ALL CATEGORIES");
        for (ReliefItem.Category cat : ReliefItem.Category.values()) {
            categorySelector2.addItem(cat.getDisplayName());
        }
        
        selectorPanel2.add(categorySelector2);
        
        // Chart and text area
        ChartPanel chartPanel2 = new ChartPanel(null);
        chartPanel2.setPreferredSize(new Dimension(800, 350));
        InteractiveChartUtility.makeChartInteractive(chartPanel2);
        
        JTextArea textArea2 = new JTextArea(8, 50);
        textArea2.setEditable(false);
        textArea2.setFont(new Font("Monospaced", Font.PLAIN, 9));
        
        JButton btnAnalyzeCategoryTemporal = new JButton("Analyze");
        btnAnalyzeCategoryTemporal.addActionListener(e -> {
            try {
                // Get selected disaster and category
                String selectedDisaster = (String) disasterSelector2.getSelectedItem();
                String selectedCategory = (String) categorySelector2.getSelectedItem();
                
                // Filter posts by disaster type first
                List<Post> posts = model.getPosts();
                if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                    posts = posts.stream()
                        .filter(p -> {
                            if (p instanceof FacebookPost) {
                                FacebookPost fbPost = (FacebookPost) p;
                                DisasterType type = fbPost.getDisasterType();
                                return type != null && type.getName().equals(selectedDisaster);
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
                }
                
                StringBuilder sb = new StringBuilder();
                sb.append("=== TEMPORAL SENTIMENT ANALYSIS: ").append(selectedCategory).append(" ===\n\n");
                
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                
                ReliefItem.Category targetCategory = null;
                if (!"ALL CATEGORIES".equals(selectedCategory)) {
                    for (ReliefItem.Category cat : ReliefItem.Category.values()) {
                        if (cat.getDisplayName().equals(selectedCategory)) {
                            targetCategory = cat;
                            break;
                        }
                    }
                }
                
                final ReliefItem.Category finalCategory = targetCategory;
                
                // Filter posts by category if selected
                List<Post> filteredPosts = posts.stream()
                    .filter(p -> finalCategory == null || (p.getReliefItem() != null && p.getReliefItem().getCategory() == finalCategory))
                    .collect(Collectors.toList());
                
                if (filteredPosts.isEmpty()) {
                    sb.append("No data available for selected category");
                    textArea2.setText(sb.toString());
                    return;
                }
                
                // Group by date
                Map<String, List<Post>> byDate = filteredPosts.stream()
                    .collect(Collectors.groupingBy(p -> p.getCreatedAt().toLocalDate().toString()));
                
                byDate.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    String date = entry.getKey();
                    List<Post> datePosts = entry.getValue();
                    
                    long pos = datePosts.stream()
                        .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
                        .count();
                    long neg = datePosts.stream()
                        .filter(p -> p.getSentiment() != null && p.getSentiment().isNegative())
                        .count();
                    long neu = datePosts.size() - pos - neg;
                    
                    double posPct = (double) pos / datePosts.size() * 100;
                    double negPct = (double) neg / datePosts.size() * 100;
                    double neuPct = (double) neu / datePosts.size() * 100;
                    
                    dataset.addValue(posPct, "Positive", date);
                    dataset.addValue(negPct, "Negative", date);
                    dataset.addValue(neuPct, "Neutral", date);
                    
                    String trend = pos > neg ? "📈 IMPROVING" : (neg > pos ? "📉 DETERIORATING" : "→ STABLE");
                    sb.append(String.format("%s: %s\n", date, trend));
                    sb.append(String.format("   Total: %d | Positive: %d (%.1f%%) | Negative: %d (%.1f%%)\n", 
                        datePosts.size(), pos, posPct, neg, negPct));
                });
                
                JFreeChart chart = ChartFactory.createStackedBarChart(
                    "Temporal Sentiment: " + selectedCategory,
                    "Date", "Percentage (%)", dataset
                );
                chartPanel2.setChart(chart);
                
                // Overall trend analysis
                sb.append("\n=== TREND ANALYSIS ===\n");
                List<Map.Entry<String, List<Post>>> sortedEntries = new ArrayList<>(byDate.entrySet());
                sortedEntries.sort(Map.Entry.comparingByKey());
                
                if (sortedEntries.size() >= 2) {
                    double firstPosPct = (double) sortedEntries.get(0).getValue().stream()
                        .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
                        .count() / sortedEntries.get(0).getValue().size() * 100;
                    
                    double lastPosPct = (double) sortedEntries.get(sortedEntries.size() - 1).getValue().stream()
                        .filter(p -> p.getSentiment() != null && p.getSentiment().isPositive())
                        .count() / sortedEntries.get(sortedEntries.size() - 1).getValue().size() * 100;
                    
                    double change = lastPosPct - firstPosPct;
                    
                    sb.append(String.format("Initial: %.1f%% positive\n", firstPosPct));
                    sb.append(String.format("Latest:  %.1f%% positive\n", lastPosPct));
                    sb.append(String.format("Change:  %+.1f%%\n\n", change));
                    
                    if (change > 15) {
                        sb.append("✅ STRONGLY IMPROVING - Relief efforts are becoming more effective");
                    } else if (change > 5) {
                        sb.append("📈 IMPROVING - Positive sentiment trend detected");
                    } else if (change > -5) {
                        sb.append("→ STABLE - Sentiment levels maintained");
                    } else if (change > -15) {
                        sb.append("📉 DETERIORATING - Negative sentiment trend detected");
                    } else {
                        sb.append("🚨 STRONGLY DETERIORATING - Urgent intervention needed");
                    }
                }
                
                InteractiveChartUtility.enableChartInteractivity(chartPanel2);
                textArea2.setText(sb.toString());
                textArea2.setCaretPosition(0);
            } catch (Exception ex) {
                textArea2.setText("Error: " + ex.getMessage());
            }
        });
        
        JPanel buttonPanel2a = new JPanel();
        buttonPanel2a.add(btnAnalyzeCategoryTemporal);
        
        categoryTemporalPanel.add(selectorPanel2, BorderLayout.NORTH);
        categoryTemporalPanel.add(chartPanel2, BorderLayout.CENTER);
        JPanel southPanel2 = new JPanel(new BorderLayout());
        southPanel2.add(buttonPanel2a, BorderLayout.NORTH);
        southPanel2.add(new JScrollPane(textArea2), BorderLayout.CENTER);
        categoryTemporalPanel.add(southPanel2, BorderLayout.SOUTH);
        
        tabs.addTab("By Category (Temporal)", categoryTemporalPanel);

        // Temporal Distribution
        JPanel temporalPanel = new JPanel(new BorderLayout());
        ChartPanel chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(800, 350));
        InteractiveChartUtility.makeChartInteractive(chartPanel);
        JTextArea textArea = new JTextArea(8, 50);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 9));

        temporalPanel.add(chartPanel, BorderLayout.CENTER);
        temporalPanel.add(new JScrollPane(textArea), BorderLayout.SOUTH);

        JButton btnTemporal = new JButton("Refresh");
        btnTemporal.addActionListener(e -> {
            try {
                List<Post> posts = model.getPosts();
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                StringBuilder sb = new StringBuilder("=== TEMPORAL SENTIMENT ANALYSIS (Problem 2) ===\n\n");

                Map<String, List<Post>> byDate = posts.stream()
                    .collect(Collectors.groupingBy(p -> p.getCreatedAt().toLocalDate().toString()));

                byDate.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    String date = entry.getKey();
                    List<Post> datePosts = entry.getValue();

                    long pos = datePosts.stream().filter(p -> p.getSentiment() != null && p.getSentiment().isPositive()).count();
                    long neg = datePosts.stream().filter(p -> p.getSentiment() != null && p.getSentiment().isNegative()).count();
                    long neu = datePosts.size() - pos - neg;

                    dataset.addValue(pos, "Positive", date);
                    dataset.addValue(neg, "Negative", date);
                    dataset.addValue(neu, "Neutral", date);

                    String trend = pos > neg ? "📈 IMPROVING" : (neg > pos ? "📉 DETERIORATING" : "→ STABLE");
                    sb.append(String.format("%s: %s | Posts:%d | Pos:%d Neg:%d\n", date, trend, datePosts.size(), pos, neg));
                });

                JFreeChart chart = ChartFactory.createStackedBarChart(
                    "Sentiment Over Time (Problem 2)",
                    "Date", "Posts", dataset
                );
                chartPanel.setChart(chart);
                InteractiveChartUtility.enableChartInteractivity(chartPanel);
                textArea.setText(sb.toString());
            } catch (Exception ex) {
                textArea.setText("Error: " + ex.getMessage());
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnTemporal);
        temporalPanel.add(buttonPanel, BorderLayout.SOUTH);
        tabs.addTab("Over Time", temporalPanel);

        // Comment Analysis
        JPanel commentPanel = new JPanel(new BorderLayout());
        JTextArea commentArea = new JTextArea();
        commentArea.setEditable(false);
        commentArea.setFont(new Font("Monospaced", Font.PLAIN, 9));

        commentPanel.add(new JScrollPane(commentArea), BorderLayout.CENTER);

        JButton btnComment = new JButton("Refresh Comment Analysis");
        btnComment.addActionListener(e -> {
            try {
                List<Post> posts = model.getPosts();
                StringBuilder sb = new StringBuilder("=== COMMENT SENTIMENT OVER TIME ===\n\n");

                posts.forEach(post -> {
                    if (!post.getComments().isEmpty()) {
                        sb.append(String.format("📌 Post %s (%s)\n", post.getPostId(), post.getAuthor()));
                        sb.append(String.format("   Posted: %s\n", post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

                        List<Comment> sorted = post.getComments().stream()
                            .sorted(Comparator.comparing(Comment::getCreatedAt))
                            .collect(Collectors.toList());

                        for (int i = 0; i < sorted.size(); i++) {
                            Comment c = sorted.get(i);
                            sb.append(String.format("     [%d] %s @ %s: %s (%.2f) - \"%s\"\n",
                                i + 1, c.getAuthor(),
                                c.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")),
                                c.getSentiment().getType(),
                                c.getSentiment().getConfidence(),
                                truncate(c.getContent(), 50)));
                        }
                        sb.append("\n");
                    }
                });

                commentArea.setText(sb.toString());
            } catch (Exception ex) {
                commentArea.setText("Error: " + ex.getMessage());
            }
        });

        JPanel buttonPanel2 = new JPanel();
        buttonPanel2.add(btnComment);
        commentPanel.add(buttonPanel2, BorderLayout.SOUTH);
        tabs.addTab("Comments", commentPanel);

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCombinedTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Add disaster selector panel
        JPanel selectorPanel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel3.setBorder(BorderFactory.createTitledBorder("Select Disaster Type"));
        
        JLabel disasterLabel3 = new JLabel("Disaster Type: ");
        JComboBox<String> disasterSelector3 = new JComboBox<>();
        disasterSelector3.addItem("All Disasters");
        List<String> disasterNames3 = DisasterManager.getInstance().getAllDisasterNames();
        for (String name : disasterNames3) {
            disasterSelector3.addItem(name);
        }
        disasterSelector3.setPreferredSize(new Dimension(120, 25));
        
        selectorPanel3.add(disasterLabel3);
        selectorPanel3.add(disasterSelector3);
        panel.add(selectorPanel3, BorderLayout.NORTH);
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 9));

        JButton btn = new JButton("Generate Report");
        btn.addActionListener(e -> {
            try {
                // Get selected disaster
                String selectedDisaster = (String) disasterSelector3.getSelectedItem();
                
                // Filter posts by disaster type
                List<Post> posts = model.getPosts();
                if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                    posts = posts.stream()
                        .filter(p -> {
                            if (p instanceof FacebookPost) {
                                FacebookPost fbPost = (FacebookPost) p;
                                DisasterType type = fbPost.getDisasterType();
                                return type != null && type.getName().equals(selectedDisaster);
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
                }
                
                StringBuilder sb = new StringBuilder();

                sb.append("═".repeat(70)).append("\n");
                sb.append("PROBLEM 1 & 2 COMBINED ANALYSIS REPORT\n");
                if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                    sb.append("(Disaster: ").append(selectedDisaster).append(")\n");
                }
                sb.append("═".repeat(70)).append("\n\n");

                // Problem 1
                sb.append("📊 PROBLEM 1: PUBLIC SATISFACTION ANALYSIS\n");
                sb.append("─".repeat(70)).append("\n");

                Map<ReliefItem.Category, List<Post>> byCategory = posts.stream()
                    .filter(p -> p.getReliefItem() != null)
                    .collect(Collectors.groupingBy(p -> p.getReliefItem().getCategory()));

                byCategory.forEach((cat, catPosts) -> {
                    long pos = catPosts.stream().filter(p -> p.getSentiment() != null && p.getSentiment().isPositive()).count();
                    double posPct = (double) pos / catPosts.size() * 100;
                    String status = posPct > 70 ? "✅ EXCELLENT" : (posPct > 50 ? "⚠️ MODERATE" : "❌ CRITICAL");
                    sb.append(String.format("%-20s: %.1f%% satisfaction %s\n", cat.getDisplayName(), posPct, status));
                });

                // Problem 2
                sb.append("\n📈 PROBLEM 2: TEMPORAL SENTIMENT TRACKING\n");
                sb.append("─".repeat(70)).append("\n");

                Map<String, List<Post>> byDate = posts.stream()
                    .collect(Collectors.groupingBy(p -> p.getCreatedAt().toLocalDate().toString()));

                byDate.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    List<Post> datePosts = entry.getValue();
                    long pos = datePosts.stream().filter(p -> p.getSentiment() != null && p.getSentiment().isPositive()).count();
                    long neg = datePosts.stream().filter(p -> p.getSentiment() != null && p.getSentiment().isNegative()).count();
                    String trend = pos > neg ? "↗ IMPROVING" : (neg > pos ? "↘ DETERIORATING" : "→ STABLE");
                    sb.append(String.format("%s: %s (P:%d N:%d)\n", entry.getKey(), trend, pos, neg));
                });

                // Summary
                sb.append("\n📋 SUMMARY\n");
                sb.append("─".repeat(70)).append("\n");
                sb.append(String.format("Total Posts: %d\n", posts.size()));
                sb.append(String.format("Total Comments: %d\n", posts.stream().mapToInt(p -> p.getComments().size()).sum()));

                long totalPos = posts.stream().filter(p -> p.getSentiment() != null && p.getSentiment().isPositive()).count();
                sb.append(String.format("Overall Satisfaction: %.1f%%\n", (double) totalPos / posts.size() * 100));

                textArea.setText(sb.toString());
            } catch (Exception ex) {
                textArea.setText("Error: " + ex.getMessage());
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btn);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private String truncate(String s, int len) {
        return s.length() <= len ? s : s.substring(0, len) + "...";
    }
}
