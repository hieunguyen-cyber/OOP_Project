package com.humanitarian.devui.ui;

import com.humanitarian.devui.model.*;
import com.humanitarian.devui.crawler.*;
import com.humanitarian.devui.database.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CrawlControlPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(CrawlControlPanel.class.getName());
    private final Model model;
    private final SessionDataBuffer buffer;
    private JSpinner postLimitSpinner;
    private JSpinner commentLimitSpinner;
    private JTextArea keywordArea;
    private JTextArea crawlResultsArea;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JButton crawlButton;
    private JTextArea postUrlField;
    private JButton crawlUrlButton;
    private JComboBox<String> disasterTypeCombo;
    private JButton addNewDisasterButton;
    private JComboBox<String> platformSelector;
    private String selectedCrawlerName = "YOUTUBE";
    private CrawlerRegistry crawlerRegistry = CrawlerRegistry.getInstance();

    public CrawlControlPanel(Model model, SessionDataBuffer buffer) {
        this.model = model;
        this.buffer = buffer;

        if (crawlerRegistry.getCrawlerNames().isEmpty()) {
            CrawlerManager.initializeCrawlers();
        }
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel topPanel = createPlatformSelectorPanel();
        add(topPanel, BorderLayout.NORTH);
        
        setBorder(BorderFactory.createTitledBorder("Web Crawler Control"));

        JPanel configPanel = createConfigPanel();
        add(configPanel, BorderLayout.WEST);

        JPanel resultsPanel = createResultsPanel();
        add(resultsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createPlatformSelectorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.setBackground(new Color(240, 240, 240));
        
        JLabel platformLabel = new JLabel("Data Source: ");
        platformLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(platformLabel);
        
        String[] crawlerNames = crawlerRegistry.getCrawlerDisplayNames().toArray(new String[0]);
        platformSelector = new JComboBox<>(crawlerNames);
        platformSelector.setFont(new Font("Arial", Font.PLAIN, 12));
        platformSelector.addActionListener(e -> updateUIForCrawler());
        platformSelector.setPreferredSize(new Dimension(200, 30));
        panel.add(platformSelector);
        
        JLabel descriptionLabel = new JLabel();
        descriptionLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        descriptionLabel.setForeground(Color.GRAY);
        updateCrawlerDescription(descriptionLabel);
        platformSelector.addActionListener(e -> updateCrawlerDescription(descriptionLabel));
        panel.add(descriptionLabel);
        
        return panel;
    }

    private void updateCrawlerDescription(JLabel descriptionLabel) {
        String displayName = (String) platformSelector.getSelectedItem();
        String crawlerName = getCrawlerNameByDisplay(displayName);
        String description = crawlerRegistry.getDescription(crawlerName);
        descriptionLabel.setText("  (" + description + ")");
    }

    private void updateUIForCrawler() {
        String displayName = (String) platformSelector.getSelectedItem();
        selectedCrawlerName = getCrawlerNameByDisplay(displayName);
        
        CrawlerRegistry.CrawlerConfig config = crawlerRegistry.getConfig(selectedCrawlerName);
        String crawlerName = config != null ? config.displayName : selectedCrawlerName;
        
        setBorder(BorderFactory.createTitledBorder("Web Crawler Control - " + crawlerName));
        
        if (crawlButton != null) {
            crawlButton.setText("Crawl Data from " + crawlerName);
            crawlButton.setVisible(config != null && config.supportsKeywordSearch);
        }
        if (crawlUrlButton != null) {
            crawlUrlButton.setText("Crawl from URLs (" + crawlerName + ")");
            crawlUrlButton.setVisible(config != null && config.supportsUrlCrawl);
        }
        
        crawlResultsArea.setText("✓ Ready to crawl with " + crawlerName + "\n");
        statusLabel.setText("Selected: " + crawlerName);
    }
    
    private String getCrawlerNameByDisplay(String displayName) {
        for (String crawlerName : crawlerRegistry.getCrawlerNames()) {
            CrawlerRegistry.CrawlerConfig config = crawlerRegistry.getConfig(crawlerName);
            if (config != null && config.displayName.equals(displayName)) {
                return crawlerName;
            }
        }
        return "YOUTUBE";
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(300, 0));

        JLabel section1Label = new JLabel("SECTION 1: Search by Hashtag/Keyword");
        section1Label.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(section1Label);
        panel.add(Box.createVerticalStrut(8));

        panel.add(new JLabel("Max Posts to Crawl:"));
        postLimitSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        panel.add(postLimitSpinner);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Max Comments per Post:"));
        commentLimitSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 100, 5));
        panel.add(commentLimitSpinner);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Keywords/Hashtags (1 per line):"));
        keywordArea = new JTextArea(5, 25);
        keywordArea.setLineWrap(true);
        keywordArea.setWrapStyleWord(true);
        keywordArea.setText("#yagi\n#bualoi\n#matmo\ndisaster\naid");
        JScrollPane keywordScroll = new JScrollPane(keywordArea);
        panel.add(keywordScroll);
        panel.add(Box.createVerticalStrut(10));

        crawlButton = new JButton("Crawl Data");
        crawlButton.setFont(new Font("Arial", Font.BOLD, 12));
        crawlButton.setMaximumSize(new Dimension(250, 40));
        crawlButton.addActionListener(e -> startCrawling());
        panel.add(crawlButton);
        panel.add(Box.createVerticalStrut(15));

        JLabel section2Label = new JLabel("SECTION 2: Crawl Posts by URLs");
        section2Label.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(section2Label);
        panel.add(Box.createVerticalStrut(8));

        panel.add(new JLabel("Post URLs (1 per line):"));
        postUrlField = new JTextArea(6, 25);
        postUrlField.setLineWrap(true);
        postUrlField.setWrapStyleWord(true);
        postUrlField.setText("https://www.youtube.com/watch?v=");
        JScrollPane urlScroll = new JScrollPane(postUrlField);
        panel.add(urlScroll);
        panel.add(Box.createVerticalStrut(8));

        panel.add(new JLabel("Disaster Type:"));
        disasterTypeCombo = new JComboBox<>();
        updateDisasterTypeCombo();
        disasterTypeCombo.setMaximumSize(new Dimension(250, 25));
        panel.add(disasterTypeCombo);
        panel.add(Box.createVerticalStrut(5));

        addNewDisasterButton = new JButton("+ Add New Disaster Type");
        addNewDisasterButton.setFont(new Font("Arial", Font.PLAIN, 10));
        addNewDisasterButton.setMaximumSize(new Dimension(250, 25));
        addNewDisasterButton.addActionListener(e -> showAddDisasterDialog());
        panel.add(addNewDisasterButton);
        panel.add(Box.createVerticalStrut(8));

        crawlUrlButton = new JButton("Crawl Posts from URLs");
        crawlUrlButton.setFont(new Font("Arial", Font.BOLD, 12));
        crawlUrlButton.setMaximumSize(new Dimension(250, 40));
        crawlUrlButton.addActionListener(e -> startCrawlingByUrl());
        panel.add(crawlUrlButton);
        panel.add(Box.createVerticalStrut(15));

        JLabel section3Label = new JLabel("UTILITIES");
        section3Label.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(section3Label);
        panel.add(Box.createVerticalStrut(8));

        JButton sampleDataButton = new JButton("Use Sample Data");
        sampleDataButton.setMaximumSize(new Dimension(250, 40));
        sampleDataButton.addActionListener(e -> loadSampleData());
        panel.add(sampleDataButton);

        panel.add(Box.createVerticalStrut(8));

        JButton resetDbButton = new JButton("Reset Database");
        resetDbButton.setMaximumSize(new Dimension(250, 40));
        resetDbButton.setBackground(new Color(231, 76, 60));
        resetDbButton.setForeground(Color.WHITE);
        resetDbButton.setOpaque(true);
        resetDbButton.addActionListener(e -> resetDatabase());
        panel.add(resetDbButton);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel resultsLabel = new JLabel("Crawl Results & Statistics:");
        panel.add(resultsLabel, BorderLayout.NORTH);

        crawlResultsArea = new JTextArea(20, 50);
        crawlResultsArea.setEditable(false);
        crawlResultsArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JScrollPane scrollPane = new JScrollPane(crawlResultsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        panel.add(progressBar);

        statusLabel = new JLabel("Ready");
        panel.add(statusLabel);

        return panel;
    }

    private void startCrawling() {
        new Thread(() -> {
            DataCrawler crawler = null;
            try {
                crawlButton.setEnabled(false);
                progressBar.setValue(0);

                int postLimit = (Integer) postLimitSpinner.getValue();
                int commentLimit = (Integer) commentLimitSpinner.getValue();
                String[] keywords = keywordArea.getText().split("\n");
                
                List<String> hashtags = new ArrayList<>();
                for (String keyword : keywords) {
                    String cleaned = keyword.trim();
                    if (!cleaned.isEmpty()) {
                        hashtags.add(cleaned);
                    }
                }

                statusLabel.setText("⏳ Crawling in progress...");
                progressBar.setIndeterminate(true);

                CrawlerRegistry.CrawlerConfig config = crawlerRegistry.getConfig(selectedCrawlerName);
                String crawlerDisplayName = config != null ? config.displayName : selectedCrawlerName;
                
                crawlResultsArea.setText("Starting " + crawlerDisplayName + " crawl...\n");
                crawlResultsArea.append("Post Limit: " + postLimit + "\n");
                crawlResultsArea.append("Comment Limit per Post: " + commentLimit + "\n");
                crawlResultsArea.append("Keywords: " + String.join(", ", hashtags) + "\n");
                crawlResultsArea.append("-".repeat(60) + "\n\n");

                List<Post> posts = new ArrayList<>();
                boolean usedRealCrawler = false;
                
                try {
                    crawlResultsArea.append("Initializing " + crawlerDisplayName + " crawler...\n");
                    crawler = crawlerRegistry.createCrawler(selectedCrawlerName);
                    
                    if (config != null && config.requiresInitialization && crawler instanceof YouTubeCrawler) {
                        ((YouTubeCrawler) crawler).initialize();
                    }
                    
                    if (crawler.isInitialized() || !config.requiresInitialization) {
                        crawlResultsArea.append("✓ Crawler initialized\n");
                        crawlResultsArea.append("Crawling...\n\n");
                        posts = crawler.crawlPosts(hashtags, new ArrayList<>(), postLimit);
                        usedRealCrawler = true;
                        crawlResultsArea.append("✓ Successfully crawled " + posts.size() + " items from " + crawlerDisplayName + "\n\n");
                    }
                    
                    if (posts.isEmpty() && usedRealCrawler) {
                        throw new Exception("Crawler returned no results");
                    }
                } catch (Exception e) {
                    crawlResultsArea.append("⚠️ " + crawlerDisplayName + " crawler unavailable: " + e.getMessage() + "\n\n");
                    crawlResultsArea.append("Falling back to Mock Data generator...\n\n");
                    
                    crawler = crawlerRegistry.createCrawler("MOCK");
                    posts = crawler.crawlPosts(new ArrayList<>(List.of(keywords)), hashtags, postLimit);
                    usedRealCrawler = false;
                }

                if (!usedRealCrawler) {
                    for (Post post : posts) {
                        CrawlingUtility.addCommentsToPost(post, commentLimit);
                    }
                }

                List<String> keywordList = new ArrayList<>(hashtags);
                int addedCount = CrawlingUtility.processAndAddPosts(posts, buffer, keywordList, commentLimit, !usedRealCrawler);

                updateCrawlResults(posts);
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                statusLabel.setText("✓ Crawl completed: " + addedCount + " posts added to buffer");

            } catch (Exception e) {
                crawlResultsArea.append("\n✗ Error during crawling: " + e.getMessage());
                statusLabel.setText("✗ Error: " + e.getMessage());
                progressBar.setIndeterminate(false);
                LOGGER.severe("Crawling error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                crawlButton.setEnabled(true);

                if (crawler != null) {
                    try {
                        crawler.shutdown();
                    } catch (Exception e) {
                        LOGGER.warning("Error shutting down crawler: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    private void startCrawlingByUrl() {
        new Thread(() -> {
            try {
                crawlUrlButton.setEnabled(false);
                progressBar.setValue(0);

                String selectedDisasterName = (String) disasterTypeCombo.getSelectedItem();
                if (selectedDisasterName == null || selectedDisasterName.isEmpty()) {
                    statusLabel.setText("✗ Please select a disaster type");
                    crawlResultsArea.setText("Error: No disaster type selected\n");
                    return;
                }

                DisasterType selectedDisaster = DisasterManager.getInstance().getDisasterType(selectedDisasterName);
                
                CrawlerRegistry.CrawlerConfig config = crawlerRegistry.getConfig(selectedCrawlerName);
                if (config != null && !config.supportsUrlCrawl) {
                    statusLabel.setText("✗ " + config.displayName + " does not support URL-based crawling");
                    crawlResultsArea.setText("This crawler only supports keyword-based search.\n");
                    crawlUrlButton.setEnabled(true);
                    return;
                }

                String urlText = postUrlField.getText().trim();
                List<String> validUrls = CrawlingUtility.validateAndCleanUrls(urlText, selectedCrawlerName);
                
                if (validUrls.isEmpty()) {
                    statusLabel.setText("✗ Please enter valid URL(s)");
                    crawlResultsArea.setText("Error: No valid URLs found\n");
                    crawlUrlButton.setEnabled(true);
                    return;
                }

                statusLabel.setText("⏳ Crawling " + validUrls.size() + " URL(s)...");
                progressBar.setIndeterminate(true);

                CrawlerRegistry.CrawlerConfig crawlerConfig = crawlerRegistry.getConfig(selectedCrawlerName);
                String crawlerName = crawlerConfig != null ? crawlerConfig.displayName : selectedCrawlerName;
                
                crawlResultsArea.setText("Starting " + crawlerName + " URL crawl...\n");
                crawlResultsArea.append("Disaster Type: " + selectedDisasterName + "\n");
                crawlResultsArea.append("Total URLs: " + validUrls.size() + "\n");
                crawlResultsArea.append("-".repeat(60) + "\n\n");

                List<Post> allPosts = new ArrayList<>();
                int successCount = 0;
                int failCount = 0;

                for (int i = 0; i < validUrls.size(); i++) {
                    String postUrl = validUrls.get(i);
                    crawlResultsArea.append("\n[" + (i + 1) + "/" + validUrls.size() + "] Processing URL:\n");
                    crawlResultsArea.append("  " + postUrl.substring(0, Math.min(70, postUrl.length())) + "\n");
                    
                    try {
                        DataCrawler crawler = crawlerRegistry.createCrawler(selectedCrawlerName);
                        
                        if (config != null && config.requiresInitialization && crawler instanceof YouTubeCrawler) {
                            ((YouTubeCrawler) crawler).initialize();
                        }
                        
                        Post post = null;
                        if (crawler instanceof YouTubeCrawler) {
                            post = ((YouTubeCrawler) crawler).crawlVideoByUrl(postUrl);
                        }
                        
                        if (crawler instanceof YouTubeCrawler) {
                            crawler.shutdown();
                        }
                        
                        if (post != null) {

                            if (post instanceof YouTubePost) {
                                ((YouTubePost) post).setDisasterType(selectedDisaster);
                            }
                            
                            if (!CrawlingUtility.isDuplicatePost(post.getPostId())) {
                                crawlResultsArea.append("  ✓ Success: " + post.getComments().size() + " comments\n");
                                allPosts.add(post);
                                buffer.addPost(post);
                                successCount++;
                            } else {
                                crawlResultsArea.append("  ⚠️ Duplicate (skipped)\n");
                                failCount++;
                            }
                            
                            int progress = (int) ((i + 1.0) / validUrls.size() * 100);
                            progressBar.setIndeterminate(false);
                            progressBar.setValue(progress);
                        } else {
                            crawlResultsArea.append("  ✗ Failed: Could not extract post\n");
                            failCount++;
                        }
                    } catch (Exception e) {
                        crawlResultsArea.append("  ✗ Error: " + e.getMessage() + "\n");
                        failCount++;
                    }
                }

                crawlResultsArea.append("\n" + "=".repeat(60) + "\n");
                crawlResultsArea.append("📊 CRAWL SUMMARY\n");
                crawlResultsArea.append("Crawler: " + crawlerName + "\n");
                crawlResultsArea.append("Disaster Type: " + selectedDisasterName + "\n");
                crawlResultsArea.append("Total URLs: " + validUrls.size() + "\n");
                crawlResultsArea.append("✓ Successful: " + successCount + "\n");
                crawlResultsArea.append("✗ Failed: " + failCount + "\n");
                crawlResultsArea.append("=".repeat(60) + "\n");

                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                statusLabel.setText("✓ Crawl completed: " + successCount + " posts, " + failCount + " failed");

                if (!allPosts.isEmpty()) {
                    updateCrawlResults(allPosts);
                }

            } catch (Exception e) {
                crawlResultsArea.append("\n✗ Error during crawling: " + e.getMessage());
                statusLabel.setText("✗ Error: " + e.getMessage());
                progressBar.setIndeterminate(false);
                LOGGER.severe("Crawling error: " + e.getMessage());
            } finally {
                crawlUrlButton.setEnabled(true);
            }
        }).start();
    }

    private void updateCrawlResults(List<Post> posts) {
        StringBuilder results = new StringBuilder();

        int totalComments = posts.stream().mapToInt(p -> p.getComments().size()).sum();
        long positiveComments = posts.stream()
            .flatMap(p -> p.getComments().stream())
            .filter(c -> c.getSentiment() != null && c.getSentiment().isPositive())
            .count();
        long negativeComments = posts.stream()
            .flatMap(p -> p.getComments().stream())
            .filter(c -> c.getSentiment() != null && c.getSentiment().isNegative())
            .count();

        results.append("\n=== CRAWL SUMMARY ===\n");
        results.append(String.format("Total Posts Crawled: %d\n", posts.size()));
        results.append(String.format("Total Comments: %d\n", totalComments));
        results.append(String.format("Avg Comments per Post: %.1f\n", totalComments / (double) Math.max(1, posts.size())));
        results.append("\n=== SENTIMENT DISTRIBUTION (Comments) ===\n");
        results.append(String.format("Positive Comments: %d (%.1f%%)\n", positiveComments, 
            (double) positiveComments / Math.max(1, totalComments) * 100));
        results.append(String.format("Negative Comments: %d (%.1f%%)\n", negativeComments,
            (double) negativeComments / Math.max(1, totalComments) * 100));

        results.append("\n=== RELIEF CATEGORIES ===\n");
        var categoryStats = posts.stream()
            .filter(p -> p.getReliefItem() != null && p.getReliefItem().getCategory() != null)
            .collect(java.util.stream.Collectors.groupingBy(
                p -> p.getReliefItem().getCategory(),
                java.util.stream.Collectors.counting()
            ));

        categoryStats.forEach((cat, count) -> {
            results.append(String.format("%s: %d posts\n", cat.getDisplayName(), count));
        });

        results.append("\n=== DETAILED POST INFORMATION ===\n");
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            results.append(String.format("\nPost %d: %s\n", i + 1, post.getPostId()));
            results.append(String.format("  Author: %s\n", post.getAuthor()));
            String category = "N/A";
            if (post.getReliefItem() != null && post.getReliefItem().getCategory() != null) {
                category = post.getReliefItem().getCategory().getDisplayName();
            }
            results.append(String.format("  Category: %s\n", category));
            results.append(String.format("  Comments: %d\n", post.getComments().size()));
            String sentiment = "N/A";
            double confidence = 0;
            if (post.getSentiment() != null) {
                sentiment = post.getSentiment().getType().toString();
                confidence = post.getSentiment().getConfidence();
            }
            results.append(String.format("  Sentiment: %s (%.2f)\n", sentiment, confidence));
        }

        crawlResultsArea.setText(results.toString());
    }

    private void updateDisasterTypeCombo() {
        disasterTypeCombo.removeAllItems();
        
        List<String> disasterNames = DisasterManager.getInstance().getAllDisasterNames();
        for (String name : disasterNames) {
            disasterTypeCombo.addItem(name);
        }
        
        if (disasterNames.contains("yagi")) {
            disasterTypeCombo.setSelectedItem("yagi");
        }
    }

    private void showAddDisasterDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Disaster Type");
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(new JLabel("Disaster Type Name:"));
        JTextField nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(350, 30));
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Keywords/Aliases (comma-separated):"));
        JTextArea aliasesArea = new JTextArea(3, 40);
        aliasesArea.setLineWrap(true);
        aliasesArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(aliasesArea);
        scrollPane.setMaximumSize(new Dimension(350, 80));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        JButton addButton = new JButton("Add to Database");
        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String aliases = aliasesArea.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a disaster type name", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            DisasterType newDisaster = DisasterManager.getInstance().getOrCreateDisasterType(name);
            
            if (!aliases.isEmpty()) {
                String[] aliasArray = aliases.split(",");
                for (String alias : aliasArray) {
                    newDisaster.addAlias(alias.trim());
                }
            }

            updateDisasterTypeCombo();
            disasterTypeCombo.setSelectedItem(name);

            JOptionPane.showMessageDialog(dialog, "✓ Disaster type '" + name + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void loadSampleData() {
        try {
            statusLabel.setText("Loading sample data...");
            
            List<Post> samplePosts = new ArrayList<>();
            String[] sampleTopics = {"#yagi", "#bualoi", "#matmo"};
            ReliefItem.Category[] categories = ReliefItem.Category.values();

            for (int p = 0; p < 3; p++) {
                String topic = sampleTopics[p % sampleTopics.length];
                ReliefItem.Category category = categories[p % categories.length];
                ReliefItem reliefItem = new ReliefItem(category, "Relief for " + category.getDisplayName(), 3);

                YouTubePost post = new YouTubePost(
                    "POST_SAMPLE_" + p,
                    "Post about " + topic + " - " + category.getDisplayName() + " assistance needed",
                    java.time.LocalDateTime.now().minusHours(p),
                    "Author_" + p,
                    "PAGE_" + topic
                );

                Sentiment.SentimentType sentiment = Sentiment.SentimentType.values()[p % 3];
                post.setSentiment(new Sentiment(sentiment, 0.8 + Math.random() * 0.2, post.getContent()));
                post.setReliefItem(reliefItem);

                samplePosts.add(post);
            }

            for (Post post : samplePosts) {
                buffer.addPost(post);
            }
            
            statusLabel.setText("✓ Sample data loaded: " + samplePosts.size() + " posts");
            crawlResultsArea.setText("Sample data loaded successfully!\n");
            crawlResultsArea.append("Posts: " + samplePosts.size() + "\n");
            
            JOptionPane.showMessageDialog(
                this,
                "Sample data loaded successfully!\nPosts: " + samplePosts.size(),
                "Sample Data Loaded",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            statusLabel.setText("❌ Error loading sample data: " + ex.getMessage());
            JOptionPane.showMessageDialog(
                this,
                "Error loading sample data: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void resetDatabase() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "This will delete the entire database file and create a new empty one.\nAll posts and comments will be lost. Continue?",
            "Reset Database",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {

                try {

                    DatabaseManager tempManager = new DatabaseManager();
                    tempManager.close();
                } catch (Exception e) {
                }
                
                Thread.sleep(300);
                
                String currentDir = System.getProperty("user.dir");
                String basePath;
                
                if (currentDir.endsWith("dev-ui")) {
                    basePath = currentDir + "/data";
                } else {
                    basePath = currentDir + "/dev-ui/data";
                }
                
                File dir = new File(basePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                String dbPath = basePath + "/humanitarian_logistics_curated.db";
                File dbFile = new File(dbPath);
                
                System.out.println("Resetting database at: " + dbPath);
                
                if (dbFile.exists()) {
                    boolean deleted = dbFile.delete();
                    System.out.println("Delete main DB file: " + deleted);
                    if (!deleted) {
                        throw new Exception("Failed to delete old database file at " + dbPath);
                    }
                }
                
                File walFile = new File(dbPath + "-wal");
                if (walFile.exists()) {
                    walFile.delete();
                    System.out.println("Deleted WAL file: " + dbPath + "-wal");
                }
                
                File shmFile = new File(dbPath + "-shm");
                if (shmFile.exists()) {
                    shmFile.delete();
                    System.out.println("Deleted SHM file: " + dbPath + "-shm");
                }
                
                File journalFile = new File(dbPath + "-journal");
                if (journalFile.exists()) {
                    journalFile.delete();
                    System.out.println("Deleted journal file: " + dbPath + "-journal");
                }
                
                statusLabel.setText("✓ Old database files deleted");
                
                Thread.sleep(200);
                
                try {
                    Class.forName("org.sqlite.JDBC");
                    java.sql.Connection conn = java.sql.DriverManager.getConnection(
                        "jdbc:sqlite:" + dbPath
                    );
                    conn.setAutoCommit(false);
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        
                        System.out.println("Creating new database schema...");
                        
                        stmt.execute("PRAGMA foreign_keys = ON");
                        
                        stmt.execute("CREATE TABLE IF NOT EXISTS posts (" +
                            "post_id TEXT PRIMARY KEY, " +
                            "content TEXT, " +
                            "author TEXT, " +
                            "source TEXT, " +
                            "created_at TEXT, " +
                            "sentiment TEXT, " +
                            "confidence REAL, " +
                            "relief_category TEXT, " +
                            "disaster_keyword TEXT" +
                            ")");
                        
                        stmt.execute("CREATE TABLE IF NOT EXISTS comments (" +
                            "comment_id TEXT PRIMARY KEY, " +
                            "post_id TEXT NOT NULL, " +
                            "content TEXT, " +
                            "author TEXT, " +
                            "created_at TEXT, " +
                            "sentiment TEXT, " +
                            "confidence REAL, " +
                            "relief_category TEXT, " +
                            "FOREIGN KEY(post_id) REFERENCES posts(post_id) ON DELETE CASCADE" +
                            ")");
                        
                        conn.commit();
                        System.out.println("Database schema committed successfully");
                    }
                    conn.close();
                    System.out.println("Database connection closed");
                } catch (Exception dbEx) {
                    System.err.println("Database creation error: " + dbEx.getMessage());
                    dbEx.printStackTrace();
                    throw new Exception("Failed to create new database: " + dbEx.getMessage());
                }
                
                model.clearPosts();
                buffer.clear();
                
                model.resetDatabaseConnection();
                
                statusLabel.setText("✓ Database reset successfully");
                crawlResultsArea.setText("Database has been reset.\n");
                crawlResultsArea.append("✓ Old database file deleted: " + dbPath + "\n");
                crawlResultsArea.append("✓ WAL/SHM/Journal files cleaned: " + dbPath + "-wal, -shm, -journal\n");
                crawlResultsArea.append("✓ New empty database created with fresh schema\n");
                crawlResultsArea.append("Ready for new data crawl\n");
                
                JOptionPane.showMessageDialog(
                    this,
                    "✓ Database reset successfully!\n\n" +
                    "Old file: " + dbPath + "\n" +
                    "Cleaned: -wal, -shm, -journal files\n" +
                    "Status: Deleted and replaced with new empty database",
                    "Reset Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                System.err.println("Reset database error: " + ex.getMessage());
                ex.printStackTrace();
                statusLabel.setText("❌ Error during reset: " + ex.getMessage());
                JOptionPane.showMessageDialog(
                    this,
                    "Error during database reset:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
