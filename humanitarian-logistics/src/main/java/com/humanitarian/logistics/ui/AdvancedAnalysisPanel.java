package com.humanitarian.logistics.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.humanitarian.logistics.model.Comment;
import com.humanitarian.logistics.model.DisasterManager;
import com.humanitarian.logistics.model.DisasterType;
import com.humanitarian.logistics.model.Post;
import com.humanitarian.logistics.model.ReliefItem;
import com.humanitarian.logistics.model.YouTubePost;

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

        JPanel individualCategoryPanel = new JPanel(new BorderLayout());
        
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.setBorder(BorderFactory.createTitledBorder("Select Disaster & Relief Category"));
        
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
        
        JComboBox<String> categorySelector = new JComboBox<>();
        categorySelector.addItem("ALL CATEGORIES");
        for (ReliefItem.Category cat : ReliefItem.Category.values()) {
            categorySelector.addItem(cat.getDisplayName());
        }
        selectorPanel.add(categorySelector);
        
        selectorPanel.add(new JLabel("  |  Chart Type: "));
        JComboBox<String> chartTypeSelector = new JComboBox<>(new String[]{"Bar Chart", "Pie Chart"});
        chartTypeSelector.setPreferredSize(new Dimension(120, 25));
        selectorPanel.add(chartTypeSelector);
        
        ChartPanel chartPanel0 = new ChartPanel(null);
        chartPanel0.setPreferredSize(new Dimension(800, 350));
        InteractiveChartUtility.makeChartInteractive(chartPanel0);
        
        JTextArea textArea0 = new JTextArea(8, 50);
        textArea0.setEditable(false);
        textArea0.setFont(new Font("Monospaced", Font.PLAIN, 9));
        
        JButton btnAnalyzeCategory = new JButton("Visualize");
        btnAnalyzeCategory.addActionListener(e -> {
            try {
                String selectedCategory = (String) categorySelector.getSelectedItem();
                String selectedDisaster = (String) disasterSelector.getSelectedItem();
                
                StringBuilder sb = new StringBuilder();
                sb.append("=== SATISFACTION ANALYSIS (Comments): ").append(selectedCategory);
                if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                    sb.append(" | Disaster: ").append(selectedDisaster);
                }
                sb.append(" ===\n\n");
                
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                List<Comment> allComments = getAllCommentsFromDatabase();
                
                if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                    final String disasterFilter = selectedDisaster;
                    allComments = allComments.stream()
                        .filter(c -> disasterFilter.equals(c.getDisasterType()))
                        .collect(Collectors.toList());
                }
                
                if (allComments.isEmpty()) {
                    sb.append("❌ No comments found for selected filters!\n");
                    sb.append("Try selecting different disaster type or category.\n");
                    textArea0.setText(sb.toString());
                    chartPanel0.setChart(null);  // Clear chart
                    return;
                }
                
                if ("ALL CATEGORIES".equals(selectedCategory)) {

                    Map<ReliefItem.Category, List<Comment>> byCategory = allComments.stream()
                        .filter(c -> c.getReliefItem() != null)
                        .collect(Collectors.groupingBy(c -> c.getReliefItem().getCategory()));
                    
                    System.out.println("DEBUG: Categories found: " + byCategory.size());
                    byCategory.forEach((category, categoryComments) -> {
                        System.out.println("  - " + category.getDisplayName() + ": " + categoryComments.size() + " comments");
                    });
                    
                    byCategory.forEach((category, categoryComments) -> {
                        int total = categoryComments.size();
                        if (total == 0) return;
                        
                        long positive = categoryComments.stream()
                            .filter(c -> c.getSentiment() != null && c.getSentiment().isPositive())
                            .count();
                        long negative = categoryComments.stream()
                            .filter(c -> c.getSentiment() != null && c.getSentiment().isNegative())
                            .count();
                        long neutral = categoryComments.stream()
                            .filter(c -> c.getSentiment() != null && c.getSentiment().isNeutral())
                            .count();
                        
                        double posPct = (double) positive / total * 100;
                        double negPct = (double) negative / total * 100;
                        double neuPct = (double) neutral / total * 100;
                        double satisfactionScore = (positive - negative) / (double) total;
                        
                        dataset.addValue(posPct, "Positive", category.getDisplayName());
                        dataset.addValue(negPct, "Negative", category.getDisplayName());
                        dataset.addValue(neuPct, "Neutral", category.getDisplayName());
                        
                        sb.append(String.format("📦 %s (Total: %d comments)\n", category.getDisplayName(), total));
                        sb.append(String.format("   Positive: %d (%.1f%%)\n", positive, posPct));
                        sb.append(String.format("   Negative: %d (%.1f%%)\n", negative, negPct));
                        sb.append(String.format("   Neutral:  %d (%.1f%%)\n", neutral, neuPct));
                        sb.append(String.format("   Satisfaction Score: %.2f\n", satisfactionScore));
                        
                        if (satisfactionScore > 0.6) sb.append("   ✅ HIGHLY EFFECTIVE\n");
                        else if (satisfactionScore > 0.2) sb.append("   ⚠️ NEEDS IMPROVEMENT\n");
                        else sb.append("   🚨 CRITICAL - URGENT ATTENTION\n");
                        sb.append("\n");
                    });
                    
                    String chartType = (String) chartTypeSelector.getSelectedItem();
                    JFreeChart chart;
                    
                    if ("Pie Chart".equals(chartType)) {
                        DefaultPieDataset<String> pieDataset = new DefaultPieDataset<>();
                        
                        double posSum = 0, negSum = 0, neuSum = 0;
                        for (Object colObj : dataset.getColumnKeys()) {
                            Comparable<?> col = (Comparable<?>) colObj;
                            Number pos = dataset.getValue("Positive", col);
                            Number neg = dataset.getValue("Negative", col);
                            Number neu = dataset.getValue("Neutral", col);
                            if (pos != null) posSum += pos.doubleValue();
                            if (neg != null) negSum += neg.doubleValue();
                            if (neu != null) neuSum += neu.doubleValue();
                        }
                        
                        System.out.println("DEBUG: Pie - posSum=" + posSum + ", negSum=" + negSum + ", neuSum=" + neuSum);
                        
                        pieDataset.setValue("Positive", posSum);
                        pieDataset.setValue("Negative", negSum);
                        pieDataset.setValue("Neutral", neuSum);
                        
                        chart = ChartFactory.createPieChart(
                            "Satisfaction Analysis - All Categories (Pie View - Comments)",
                            pieDataset
                        );
                    } else {

                        chart = ChartFactory.createStackedBarChart(
                            "Satisfaction Analysis - All Categories (Comments)",
                            "Relief Category", "Percentage (%)", dataset
                        );
                    }
                    chartPanel0.setChart(chart);
                } else {

                    ReliefItem.Category targetCategory = null;
                    for (ReliefItem.Category cat : ReliefItem.Category.values()) {
                        if (cat.getDisplayName().equals(selectedCategory)) {
                            targetCategory = cat;
                            break;
                        }
                    }
                    
                    final ReliefItem.Category finalCategory = targetCategory;
                    if (finalCategory != null) {
                        List<Comment> categoryComments = allComments.stream()
                            .filter(c -> c.getReliefItem() != null && c.getReliefItem().getCategory() == finalCategory)
                            .collect(Collectors.toList());
                        
                        System.out.println("DEBUG: Comments for category " + finalCategory.getDisplayName() + ": " + categoryComments.size());
                        
                        int total = categoryComments.size();
                        long positive = categoryComments.stream()
                            .filter(c -> c.getSentiment() != null && c.getSentiment().isPositive())
                            .count();
                        long negative = categoryComments.stream()
                            .filter(c -> c.getSentiment() != null && c.getSentiment().isNegative())
                            .count();
                        long neutral = total - positive - negative;
                        
                        double posPct = total > 0 ? (double) positive / total * 100 : 0;
                        double negPct = total > 0 ? (double) negative / total * 100 : 0;
                        double neuPct = total > 0 ? (double) neutral / total * 100 : 0;
                        double satisfactionScore = total > 0 ? (positive - negative) / (double) total : 0;
                        
                        String chartType = (String) chartTypeSelector.getSelectedItem();
                        JFreeChart chart;
                        
                        if ("Pie Chart".equals(chartType)) {
                            DefaultPieDataset<String> pieDataset = new DefaultPieDataset<>();
                            pieDataset.setValue("Positive (" + positive + ")", posPct);
                            pieDataset.setValue("Negative (" + negative + ")", negPct);
                            pieDataset.setValue("Neutral (" + neutral + ")", neuPct);
                            
                            chart = ChartFactory.createPieChart(
                                "Sentiment Distribution: " + selectedCategory + " (Comments)", pieDataset
                            );
                        } else {
                            DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
                            barDataset.addValue(posPct, "Positive", selectedCategory);
                            barDataset.addValue(negPct, "Negative", selectedCategory);
                            barDataset.addValue(neuPct, "Neutral", selectedCategory);
                            
                            chart = ChartFactory.createStackedBarChart(
                                "Sentiment Distribution: " + selectedCategory + " (Comments)",
                                "Category", "Percentage (%)", barDataset
                            );
                        }
                        chartPanel0.setChart(chart);
                        
                        sb.append(String.format("📊 Detailed Analysis for: %s\n\n", selectedCategory));
                        sb.append(String.format("Total Comments: %d\n", total));
                        sb.append(String.format("Positive: %d (%.1f%%)\n", positive, posPct));
                        sb.append(String.format("Negative: %d (%.1f%%)\n", negative, negPct));
                        sb.append(String.format("Neutral:  %d (%.1f%%)\n", neutral, neuPct));
                        sb.append(String.format("\nSatisfaction Score: %.2f\n\n", satisfactionScore));
                        
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
                        
                        sb.append("\n\n📝 Sample Comments for this category:\n");
                        categoryComments.stream().limit(10).forEach(comment -> {
                            String sentiment = comment.getSentiment() != null ? comment.getSentiment().getType().toString() : "N/A";
                            sb.append(String.format("  - %s (%s): %s\n",
                                comment.getAuthor(),
                                sentiment,
                                comment.getContent().substring(0, Math.min(50, comment.getContent().length())) + "..."
                            ));
                        });
                    } else {
                        sb.append("❌ Category not found: ").append(selectedCategory).append("\n");
                    }
                }
                
                InteractiveChartUtility.enableChartInteractivity(chartPanel0);
                textArea0.setText(sb.toString());
                textArea0.setCaretPosition(0);
                System.out.println("DEBUG: Analysis complete!");
            } catch (Exception ex) {
                System.err.println("ERROR: " + ex.getMessage());
                ex.printStackTrace();
                textArea0.setText("Error: " + ex.getMessage() + "\n\nStacktrace:\n" + getStackTrace(ex));
            }
        });
        
        JPanel buttonPanel0 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel0.add(btnAnalyzeCategory);
        
        JButton analyzeAllButton = new JButton("🔍 Analyze All Posts with Python API");
        analyzeAllButton.setFont(new Font("Arial", Font.BOLD, 12));
        analyzeAllButton.setBackground(new Color(3, 155, 229));
        analyzeAllButton.setForeground(Color.WHITE);
        analyzeAllButton.setOpaque(true);
        analyzeAllButton.setBorderPainted(false);
        analyzeAllButton.addActionListener(e -> analyzeAllPostsAction());
        buttonPanel0.add(analyzeAllButton);
        
        disasterSelector.addActionListener(e -> btnAnalyzeCategory.doClick());
        categorySelector.addActionListener(e -> btnAnalyzeCategory.doClick());
        chartTypeSelector.addActionListener(e -> btnAnalyzeCategory.doClick());
        
        individualCategoryPanel.add(selectorPanel, BorderLayout.NORTH);
        individualCategoryPanel.add(chartPanel0, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel0, BorderLayout.NORTH);
        southPanel.add(new JScrollPane(textArea0), BorderLayout.CENTER);
        individualCategoryPanel.add(southPanel, BorderLayout.SOUTH);
        
        tabs.addTab("By Category (Selector)", individualCategoryPanel);

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
                List<Comment> allComments = getAllCommentsFromDatabase();
                
                DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

                long pos = allComments.stream().filter(c -> c.getSentiment() != null && c.getSentiment().isPositive()).count();
                long neg = allComments.stream().filter(c -> c.getSentiment() != null && c.getSentiment().isNegative()).count();
                long neu = allComments.size() - pos - neg;

                dataset.setValue("Positive (" + pos + ")", pos);
                dataset.setValue("Negative (" + neg + ")", neg);
                dataset.setValue("Neutral (" + neu + ")", neu);

                JFreeChart chart = ChartFactory.createPieChart("Sentiment Distribution (Comments)", dataset);
                pieChartPanel.setChart(chart);
                InteractiveChartUtility.enableChartInteractivity(pieChartPanel);

                StringBuilder sb = new StringBuilder("=== DETAILED PROBLEM 1 ANALYSIS (Comments) ===\n\n");
                Map<ReliefItem.Category, List<Comment>> byCategory = allComments.stream()
                    .filter(c -> c.getReliefItem() != null)
                    .collect(Collectors.groupingBy(c -> c.getReliefItem().getCategory()));

                byCategory.forEach((category, categoryComments) -> {
                    sb.append(String.format("📦 %s (%d comments)\n", category.getDisplayName(), categoryComments.size()));
                    categoryComments.forEach(comment -> {
                        String sentiment = comment.getSentiment() != null ? comment.getSentiment().getType().toString() : "N/A";
                        double confidence = comment.getSentiment() != null ? comment.getSentiment().getConfidence() : 0.0;
                        sb.append(String.format("   - %s: %s (%.2f)\n",
                            comment.getAuthor(),
                            sentiment,
                            confidence));
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

        JPanel categoryTemporalPanel = new JPanel(new BorderLayout());
        
        JPanel selectorPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel2.setBorder(BorderFactory.createTitledBorder("Select Disaster & Relief Category for Temporal Analysis"));
        
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
        
        JComboBox<String> categorySelector2 = new JComboBox<>();
        categorySelector2.addItem("ALL CATEGORIES");
        for (ReliefItem.Category cat : ReliefItem.Category.values()) {
            categorySelector2.addItem(cat.getDisplayName());
        }
        
        selectorPanel2.add(categorySelector2);
        
        ChartPanel chartPanel2 = new ChartPanel(null);
        chartPanel2.setPreferredSize(new Dimension(800, 350));
        InteractiveChartUtility.makeChartInteractive(chartPanel2);
        
        JTextArea textArea2 = new JTextArea(8, 50);
        textArea2.setEditable(false);
        textArea2.setFont(new Font("Monospaced", Font.PLAIN, 9));
        
        JButton btnAnalyzeCategoryTemporal = new JButton("Visualize");
        btnAnalyzeCategoryTemporal.addActionListener(e -> {
            try {
                String selectedDisaster = (String) disasterSelector2.getSelectedItem();
                String selectedCategory = (String) categorySelector2.getSelectedItem();
                
                List<Comment> allComments = getAllCommentsFromDatabase();
                
                if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                    final String disasterFilter = selectedDisaster;
                    allComments = allComments.stream()
                        .filter(c -> disasterFilter.equals(c.getDisasterType()))
                        .collect(Collectors.toList());
                }
                
                if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                    final String disasterName = selectedDisaster;
                    allComments = allComments.stream()
                        .filter(c -> {
                            for (Post post : model.getPosts()) {
                                if (post.getComments().contains(c)) {
                                    if (post instanceof YouTubePost) {
                                        YouTubePost ytPost = (YouTubePost) post;
                                        DisasterType type = ytPost.getDisasterType();
                                        return type != null && type.getName().equals(disasterName);
                                    }
                                }
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
                
                List<Comment> filteredComments = allComments.stream()
                    .filter(c -> finalCategory == null || (c.getReliefItem() != null && c.getReliefItem().getCategory() == finalCategory))
                    .collect(Collectors.toList());
                
                if (filteredComments.isEmpty()) {
                    sb.append("No data available for selected category");
                    textArea2.setText(sb.toString());
                    return;
                }
                
                Map<String, List<Comment>> byDate = filteredComments.stream()
                    .collect(Collectors.groupingBy(c -> c.getCreatedAt().toLocalDate().toString()));
                
                byDate.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    String date = entry.getKey();
                    List<Comment> dateComments = entry.getValue();
                    
                    long pos = dateComments.stream()
                        .filter(c -> c.getSentiment() != null && c.getSentiment().isPositive())
                        .count();
                    long neg = dateComments.stream()
                        .filter(c -> c.getSentiment() != null && c.getSentiment().isNegative())
                        .count();
                    long neu = dateComments.size() - pos - neg;
                    
                    double posPct = dateComments.isEmpty() ? 0 : (double) pos / dateComments.size() * 100;
                    double negPct = dateComments.isEmpty() ? 0 : (double) neg / dateComments.size() * 100;
                    double neuPct = dateComments.isEmpty() ? 0 : (double) neu / dateComments.size() * 100;
                    
                    dataset.addValue(posPct, "Positive", date);
                    dataset.addValue(negPct, "Negative", date);
                    dataset.addValue(neuPct, "Neutral", date);
                    
                    String trend = pos > neg ? "📈 IMPROVING" : (neg > pos ? "📉 DETERIORATING" : "→ STABLE");
                    sb.append(String.format("%s: %s\n", date, trend));
                    sb.append(String.format("   Total: %d | Positive: %d (%.1f%%) | Negative: %d (%.1f%%) | Neutral: %d (%.1f%%)\n", 
                        dateComments.size(), pos, posPct, neg, negPct, neu, neuPct));
                });
                
                JFreeChart chart = ChartFactory.createStackedBarChart(
                    "Temporal Sentiment: " + selectedCategory,
                    "Date", "Percentage (%)", dataset
                );
                chartPanel2.setChart(chart);
                
                sb.append("\n=== TREND ANALYSIS ===\n");
                List<Map.Entry<String, List<Comment>>> sortedEntries = new ArrayList<>(byDate.entrySet());
                sortedEntries.sort(Map.Entry.comparingByKey());
                
                if (sortedEntries.size() >= 2) {
                    List<Comment> firstDateComments = sortedEntries.get(0).getValue();
                    List<Comment> lastDateComments = sortedEntries.get(sortedEntries.size() - 1).getValue();
                    
                    double firstPosPct = firstDateComments.isEmpty() ? 0 : (double) firstDateComments.stream()
                        .filter(c -> c.getSentiment() != null && c.getSentiment().isPositive())
                        .count() / firstDateComments.size() * 100;
                    
                    double lastPosPct = lastDateComments.isEmpty() ? 0 : (double) lastDateComments.stream()
                        .filter(c -> c.getSentiment() != null && c.getSentiment().isPositive())
                        .count() / lastDateComments.size() * 100;
                    
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
        
        JPanel buttonPanel2a = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel2a.add(btnAnalyzeCategoryTemporal);
        
        JButton analyzeAllButton2 = new JButton("🔍 Analyze All Posts with Python API");
        analyzeAllButton2.setFont(new Font("Arial", Font.BOLD, 12));
        analyzeAllButton2.setBackground(new Color(3, 155, 229));
        analyzeAllButton2.setForeground(Color.WHITE);
        analyzeAllButton2.setOpaque(true);
        analyzeAllButton2.setBorderPainted(false);
        analyzeAllButton2.addActionListener(e -> analyzeAllPostsAction());
        buttonPanel2a.add(analyzeAllButton2);
        
        categoryTemporalPanel.add(selectorPanel2, BorderLayout.NORTH);
        categoryTemporalPanel.add(chartPanel2, BorderLayout.CENTER);
        JPanel southPanel2 = new JPanel(new BorderLayout());
        southPanel2.add(buttonPanel2a, BorderLayout.NORTH);
        southPanel2.add(new JScrollPane(textArea2), BorderLayout.CENTER);
        categoryTemporalPanel.add(southPanel2, BorderLayout.SOUTH);
        
        tabs.addTab("By Category (Temporal)", categoryTemporalPanel);

        JPanel temporalPanel = new JPanel(new BorderLayout());
        
        JPanel selectorPanel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel3.setBorder(BorderFactory.createTitledBorder("Select Disaster Type for Temporal Analysis"));
        
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
        
        ChartPanel chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(800, 350));
        InteractiveChartUtility.makeChartInteractive(chartPanel);
        JTextArea textArea = new JTextArea(8, 50);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 9));

        temporalPanel.add(selectorPanel3, BorderLayout.NORTH);
        temporalPanel.add(chartPanel, BorderLayout.CENTER);
        temporalPanel.add(new JScrollPane(textArea), BorderLayout.SOUTH);

        JButton btnTemporal = new JButton("Refresh");
        btnTemporal.addActionListener(e -> {
            try {
                List<Comment> allComments = getAllCommentsFromDatabase();
                
                String selectedDisaster = (String) disasterSelector3.getSelectedItem();
                if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                    final String disasterFilter = selectedDisaster;
                    allComments = allComments.stream()
                        .filter(c -> disasterFilter.equals(c.getDisasterType()))
                        .collect(Collectors.toList());
                }
                
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                StringBuilder sb = new StringBuilder("=== TEMPORAL SENTIMENT ANALYSIS (Problem 2 - Comments) ===\n\n");

                Map<String, List<Comment>> byDate = allComments.stream()
                    .collect(Collectors.groupingBy(c -> c.getCreatedAt().toLocalDate().toString()));

                byDate.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    String date = entry.getKey();
                    List<Comment> dateComments = entry.getValue();

                    long pos = dateComments.stream().filter(c -> c.getSentiment() != null && c.getSentiment().isPositive()).count();
                    long neg = dateComments.stream().filter(c -> c.getSentiment() != null && c.getSentiment().isNegative()).count();
                    long neu = dateComments.size() - pos - neg;

                    dataset.addValue(pos, "Positive", date);
                    dataset.addValue(neg, "Negative", date);
                    dataset.addValue(neu, "Neutral", date);

                    String trend = pos > neg ? "📈 IMPROVING" : (neg > pos ? "📉 DETERIORATING" : "→ STABLE");
                    sb.append(String.format("%s: %s | Comments:%d | Pos:%d Neg:%d Neu:%d\n", date, trend, dateComments.size(), pos, neg, neu));
                });

                JFreeChart chart = ChartFactory.createStackedBarChart(
                    "Sentiment Over Time (Problem 2 - Comments)",
                    "Date", "Comments", dataset
                );
                chartPanel.setChart(chart);
                InteractiveChartUtility.enableChartInteractivity(chartPanel);
                textArea.setText(sb.toString());
            } catch (Exception ex) {
                textArea.setText("Error: " + ex.getMessage());
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(btnTemporal);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        temporalPanel.add(southPanel, BorderLayout.SOUTH);
        
        tabs.addTab("Over Time", temporalPanel);

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
                            String sentiment = c.getSentiment() != null ? c.getSentiment().getType().toString() : "N/A";
                            double confidence = c.getSentiment() != null ? c.getSentiment().getConfidence() : 0.0;
                            sb.append(String.format("     [%d] %s @ %s: %s (%.2f) - \"%s\"\n",
                                i + 1, c.getAuthor(),
                                c.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")),
                                sentiment,
                                confidence,
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

                String selectedDisaster = (String) disasterSelector3.getSelectedItem();
                
                List<Post> posts = model.getPosts();
                if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                    posts = posts.stream()
                        .filter(p -> {
                            if (p instanceof YouTubePost) {
                                YouTubePost ytPost = (YouTubePost) p;
                                DisasterType type = ytPost.getDisasterType();
                                return type != null && type.getName().equals(selectedDisaster);
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
                }
                
                List<Comment> allComments = getAllCommentsFromDatabase();
                if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                    final String disasterFilter = selectedDisaster;
                    allComments = allComments.stream()
                        .filter(c -> disasterFilter.equals(c.getDisasterType()))
                        .collect(Collectors.toList());
                }
                
                StringBuilder sb = new StringBuilder();

                sb.append("═".repeat(70)).append("\n");
                sb.append("PROBLEM 1 & 2 COMBINED ANALYSIS REPORT\n");
                if (selectedDisaster != null && !selectedDisaster.equals("All Disasters")) {
                    sb.append("(Disaster: ").append(selectedDisaster).append(")\n");
                }
                sb.append("═".repeat(70)).append("\n\n");

                sb.append("📊 PROBLEM 1: PUBLIC SATISFACTION ANALYSIS (Comments)\n");
                sb.append("─".repeat(70)).append("\n");

                Map<ReliefItem.Category, List<Comment>> byCategory = allComments.stream()
                    .filter(c -> c.getReliefItem() != null)
                    .collect(Collectors.groupingBy(c -> c.getReliefItem().getCategory()));

                byCategory.forEach((cat, catComments) -> {
                    long pos = catComments.stream().filter(c -> c.getSentiment() != null && c.getSentiment().isPositive()).count();
                    double posPct = catComments.size() > 0 ? (double) pos / catComments.size() * 100 : 0;
                    String status = posPct > 70 ? "✅ EXCELLENT" : (posPct > 50 ? "⚠️ MODERATE" : "❌ CRITICAL");
                    sb.append(String.format("%-20s: %.1f%% satisfaction %s (%d comments)\n", cat.getDisplayName(), posPct, status, catComments.size()));
                });

                sb.append("\n📈 PROBLEM 2: TEMPORAL SENTIMENT TRACKING (Comments)\n");
                sb.append("─".repeat(70)).append("\n");

                Map<String, List<Comment>> byDate = allComments.stream()
                    .collect(Collectors.groupingBy(c -> c.getCreatedAt().toLocalDate().toString()));

                byDate.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    List<Comment> dateComments = entry.getValue();
                    long pos = dateComments.stream().filter(c -> c.getSentiment() != null && c.getSentiment().isPositive()).count();
                    long neg = dateComments.stream().filter(c -> c.getSentiment() != null && c.getSentiment().isNegative()).count();
                    String trend = pos > neg ? "↗ IMPROVING" : (neg > pos ? "↘ DETERIORATING" : "→ STABLE");
                    sb.append(String.format("%s: %s (P:%d N:%d)\n", entry.getKey(), trend, pos, neg));
                });

                sb.append("\n📋 SUMMARY\n");
                sb.append("─".repeat(70)).append("\n");
                sb.append(String.format("Total Posts: %d\n", posts.size()));
                sb.append(String.format("Total Comments: %d\n", allComments.size()));

                long totalPos = allComments.stream().filter(c -> c.getSentiment() != null && c.getSentiment().isPositive()).count();
                sb.append(String.format("Overall Satisfaction: %.1f%%\n", allComments.size() > 0 ? (double) totalPos / allComments.size() * 100 : 0));

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

    private String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    private List<Comment> getAllCommentsFromDatabase() {
        List<Comment> allComments = new ArrayList<>();
        try {
            com.humanitarian.logistics.database.DatabaseManager dbManager = com.humanitarian.logistics.database.DatabaseManager.getInstance();
            // Force reload from disk to ensure we get latest data
            dbManager.reloadFromDisk();
            allComments = dbManager.getAllCommentsFromDatabase();
        } catch (Exception e) {
            System.err.println("Could not load from database, using model fallback");
            for (Post post : model.getPosts()) {
                allComments.addAll(post.getComments());
            }
        }
        return allComments;
    }

    private void analyzeAllPostsAction() {
        try {
            int analyzedCount = model.analyzeAllPosts();
            
            // Reload database connection to ensure latest data is available
            getAllCommentsFromDatabase();
            
            JOptionPane.showMessageDialog(
                this,
                "✓ Sentiment analysis complete!\n\n" +
                "Analyzed: " + analyzedCount + " posts via Python API\n\n" +
                "Sentiments updated in memory and saved to database.\n" +
                "Click 'Problem 1' or 'Problem 2' to view analysis.",
                "Analysis Complete",
                JOptionPane.INFORMATION_MESSAGE
            );

            mainTabs.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error during analysis:\n" + e.getMessage() + "\n\n" +
                "Make sure Python API is running: python sentiment_api.py",
                "Analysis Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
