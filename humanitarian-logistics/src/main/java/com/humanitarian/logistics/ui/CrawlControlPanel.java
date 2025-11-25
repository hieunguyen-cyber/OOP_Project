package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.model.*;
import com.humanitarian.logistics.crawler.FacebookCrawler;
import com.humanitarian.logistics.crawler.MockDataCrawler;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for controlling data crawling from Facebook.
 * Allows users to configure crawler settings and fetch data.
 */
public class CrawlControlPanel extends JPanel {
    private final Model model;
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

    public CrawlControlPanel(Model model) {
        this.model = model;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Web Crawler Control - Crawl Facebook Posts"));

        // Left: Configuration panel
        JPanel configPanel = createConfigPanel();
        add(configPanel, BorderLayout.WEST);

        // Right: Results panel
        JPanel resultsPanel = createResultsPanel();
        add(resultsPanel, BorderLayout.CENTER);

        // Bottom: Status and progress
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(300, 0));

        // ===== SECTION 1: Hashtag/Keyword Search =====
        JLabel section1Label = new JLabel("SECTION 1: Search by Hashtag/Keyword");
        section1Label.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(section1Label);
        panel.add(Box.createVerticalStrut(8));

        // Post limit
        panel.add(new JLabel("Max Posts to Crawl:"));
        postLimitSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        panel.add(postLimitSpinner);
        panel.add(Box.createVerticalStrut(10));

        // Comment limit per post
        panel.add(new JLabel("Max Comments per Post:"));
        commentLimitSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 100, 5));
        panel.add(commentLimitSpinner);
        panel.add(Box.createVerticalStrut(10));

        // Keywords
        panel.add(new JLabel("Keywords/Hashtags (1 per line):"));
        keywordArea = new JTextArea(5, 25);
        keywordArea.setLineWrap(true);
        keywordArea.setWrapStyleWord(true);
        keywordArea.setText("#yagi\n#bualoi\n#matmo\ndisaster\naid");
        JScrollPane keywordScroll = new JScrollPane(keywordArea);
        panel.add(keywordScroll);
        panel.add(Box.createVerticalStrut(10));

        // Crawl button
        crawlButton = new JButton("Crawl Facebook Data");
        crawlButton.setFont(new Font("Arial", Font.BOLD, 12));
        crawlButton.setMaximumSize(new Dimension(250, 40));
        crawlButton.addActionListener(e -> startCrawling());
        panel.add(crawlButton);
        panel.add(Box.createVerticalStrut(15));

        // ===== SECTION 2: Multiple Post URLs =====
        JLabel section2Label = new JLabel("SECTION 2: Crawl Posts by URLs");
        section2Label.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(section2Label);
        panel.add(Box.createVerticalStrut(8));

        panel.add(new JLabel("Post URLs (1 per line):"));
        postUrlField = new JTextArea(6, 25);
        postUrlField.setLineWrap(true);
        postUrlField.setWrapStyleWord(true);
        postUrlField.setText("https://www.facebook.com/");
        JScrollPane urlScroll = new JScrollPane(postUrlField);
        panel.add(urlScroll);
        panel.add(Box.createVerticalStrut(8));

        // Disaster Type Selector
        panel.add(new JLabel("Disaster Type:"));
        disasterTypeCombo = new JComboBox<>();
        updateDisasterTypeCombo();
        disasterTypeCombo.setMaximumSize(new Dimension(250, 25));
        panel.add(disasterTypeCombo);
        panel.add(Box.createVerticalStrut(5));

        // Add new disaster type button
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

        // ===== SECTION 3: Utilities =====
        JLabel section3Label = new JLabel("UTILITIES");
        section3Label.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(section3Label);
        panel.add(Box.createVerticalStrut(8));

        // Use Mock Data button
        JButton mockButton = new JButton("Use Sample Data");
        mockButton.setMaximumSize(new Dimension(250, 40));
        mockButton.addActionListener(e -> loadSampleData());
        panel.add(mockButton);

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

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        panel.add(progressBar);

        // Status label
        statusLabel = new JLabel("Ready");
        panel.add(statusLabel);

        return panel;
    }

    private void startCrawling() {
        new Thread(() -> {
            FacebookCrawler facebookCrawler = null;
            try {
                crawlButton.setEnabled(false);
                progressBar.setValue(0);

                int postLimit = (Integer) postLimitSpinner.getValue();
                int commentLimit = (Integer) commentLimitSpinner.getValue();
                String[] keywords = keywordArea.getText().split("\n");
                
                // Filter and clean hashtags
                List<String> hashtags = new ArrayList<>();
                for (String keyword : keywords) {
                    String cleaned = keyword.trim();
                    if (!cleaned.isEmpty()) {
                        hashtags.add(cleaned);
                    }
                }

                statusLabel.setText("⏳ Crawling in progress... (This may take a while)");
                progressBar.setIndeterminate(true);

                crawlResultsArea.setText("Starting Facebook crawl...\n");
                crawlResultsArea.append("Post Limit: " + postLimit + "\n");
                crawlResultsArea.append("Comment Limit per Post: " + commentLimit + "\n");
                crawlResultsArea.append("Hashtags: " + String.join(", ", hashtags) + "\n");
                crawlResultsArea.append("-".repeat(60) + "\n\n");

                List<Post> posts = new ArrayList<>();
                boolean usedRealCrawler = false;
                
                // Try real Facebook crawler first
                try {
                    crawlResultsArea.append("Initializing Facebook Selenium crawler...\n");
                    crawlResultsArea.append("Checking for ChromeDriver and Chrome browser...\n\n");
                    
                    facebookCrawler = new FacebookCrawler();
                    // Direct login - no need for cookie file
                    // facebookCrawler.loadCookieFromFile("cookie.txt");
                    facebookCrawler.initialize();
                    
                    if (facebookCrawler.isInitialized()) {
                        crawlResultsArea.append("✓ Facebook crawler initialized\n");
                        crawlResultsArea.append("Crawling from Facebook hashtags...\n\n");
                        posts = facebookCrawler.crawlPosts(new ArrayList<>(List.of(keywords)), hashtags, postLimit);
                        usedRealCrawler = true;
                        crawlResultsArea.append("✓ Successfully crawled " + posts.size() + " posts from Facebook\n\n");
                    } else {
                        throw new Exception("Failed to initialize ChromeDriver - check console for details");
                    }
                } catch (IllegalStateException ise) {
                    crawlResultsArea.append("❌ ChromeDriver Not Found\n");
                    crawlResultsArea.append("Error: " + ise.getMessage() + "\n\n");
                    crawlResultsArea.append("SOLUTIONS:\n");
                    crawlResultsArea.append("1. Install Chrome: https://www.google.com/chrome/\n");
                    crawlResultsArea.append("2. Download ChromeDriver: https://chromedriver.chromium.org/\n");
                    crawlResultsArea.append("3. Move to /usr/local/bin/ or add to PATH\n\n");
                    crawlResultsArea.append("For now, using sample data...\n\n");
                    
                    // Fallback to mock data
                    MockDataCrawler mockCrawler = new MockDataCrawler();
                    posts = mockCrawler.crawlPosts(
                        new ArrayList<>(List.of(keywords)),
                        hashtags,
                        postLimit
                    );
                    usedRealCrawler = false;
                } catch (Exception fbError) {
                    crawlResultsArea.append("⚠ Facebook crawler unavailable\n");
                    crawlResultsArea.append("Reason: " + fbError.getClass().getSimpleName() + "\n");
                    crawlResultsArea.append("Details: " + fbError.getMessage() + "\n\n");
                    crawlResultsArea.append("Falling back to sample data generator...\n\n");
                    
                    // Fallback to mock data
                    MockDataCrawler mockCrawler = new MockDataCrawler();
                    posts = mockCrawler.crawlPosts(
                        new ArrayList<>(List.of(keywords)),
                        hashtags,
                        postLimit
                    );
                    usedRealCrawler = false;
                }

                // Only add mock comments if using mock data (real crawler already has comments)
                if (!usedRealCrawler) {
                    for (Post post : posts) {
                        addCommentsToPost(post, commentLimit);
                    }
                }

                // Add posts to model
                for (Post post : posts) {
                    // Automatically assign disaster type based on keywords
                    if (post instanceof FacebookPost) {
                        FacebookPost fbPost = (FacebookPost) post;
                        DisasterType disasterType = findDisasterTypeForPost(fbPost, hashtags);
                        fbPost.setDisasterType(disasterType);
                    }
                    model.addPost(post);
                }

                // Update results
                updateCrawlResults(posts);
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                statusLabel.setText("✓ Crawl completed successfully (" + posts.size() + " posts)");

            } catch (Exception e) {
                crawlResultsArea.append("\n✗ Error during crawling: " + e.getMessage());
                statusLabel.setText("✗ Error: " + e.getMessage());
                progressBar.setIndeterminate(false);
                System.err.println("Crawling error: " + e.getMessage());
            } finally {
                crawlButton.setEnabled(true);
                // Cleanup crawler resources
                if (facebookCrawler != null) {
                    try {
                        facebookCrawler.shutdown();
                    } catch (Exception e) {
                        System.err.println("Error shutting down crawler: " + e.getMessage());
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

                // Get selected disaster type
                String selectedDisasterName = (String) disasterTypeCombo.getSelectedItem();
                if (selectedDisasterName == null || selectedDisasterName.isEmpty()) {
                    statusLabel.setText("✗ Please select a disaster type");
                    crawlResultsArea.setText("Error: No disaster type selected\n");
                    return;
                }

                DisasterType selectedDisaster = DisasterManager.getInstance().getDisasterType(selectedDisasterName);

                // Parse multiple URLs from textarea
                String urlText = postUrlField.getText().trim();
                
                if (urlText.isEmpty() || urlText.equals("https://www.facebook.com/")) {
                    statusLabel.setText("✗ Please enter valid post URL(s)");
                    crawlResultsArea.setText("Error: URL(s) are empty or invalid\n");
                    return;
                }

                // Split by newlines and filter empty lines
                String[] urls = urlText.split("\n");
                List<String> validUrls = new ArrayList<>();
                for (String url : urls) {
                    String cleanUrl = url.trim();
                    if (!cleanUrl.isEmpty() && cleanUrl.contains("facebook.com")) {
                        validUrls.add(cleanUrl);
                    }
                }

                if (validUrls.isEmpty()) {
                    statusLabel.setText("✗ No valid Facebook URLs found");
                    crawlResultsArea.setText("Error: Please enter valid facebook.com URLs\n");
                    return;
                }

                statusLabel.setText("⏳ Crawling " + validUrls.size() + " post(s) for disaster: " + selectedDisasterName);
                progressBar.setIndeterminate(true);

                crawlResultsArea.setText("Starting crawl from user-provided URLs...\n");
                crawlResultsArea.append("Disaster Type: " + selectedDisasterName + "\n");
                crawlResultsArea.append("Total URLs: " + validUrls.size() + "\n");
                crawlResultsArea.append("-".repeat(60) + "\n\n");

                List<Post> allPosts = new ArrayList<>();
                int successCount = 0;
                int failCount = 0;

                // Crawl each URL
                for (int i = 0; i < validUrls.size(); i++) {
                    String postUrl = validUrls.get(i);
                    crawlResultsArea.append("\n[" + (i + 1) + "/" + validUrls.size() + "] Processing URL:\n");
                    crawlResultsArea.append("  " + postUrl.substring(0, Math.min(70, postUrl.length())) + "\n");
                    
                    try {
                        // Use FacebookCrawler to crawl this URL directly (no login)
                        FacebookCrawler urlCrawler = new FacebookCrawler();
                        FacebookPost post = urlCrawler.crawlPostByUrl(postUrl);
                        
                        if (post != null) {
                            // Set the disaster type
                            post.setDisasterType(selectedDisaster);
                            
                            crawlResultsArea.append("  ✓ Success: " + post.getComments().size() + " comments extracted\n");
                            allPosts.add(post);
                            model.addPost(post);
                            successCount++;
                            
                            // Update progress
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

                // Display summary
                crawlResultsArea.append("\n" + "=".repeat(60) + "\n");
                crawlResultsArea.append("📊 CRAWL SUMMARY\n");
                crawlResultsArea.append("Disaster Type: " + selectedDisasterName + "\n");
                crawlResultsArea.append("Total URLs: " + validUrls.size() + "\n");
                crawlResultsArea.append("✓ Successful: " + successCount + "\n");
                crawlResultsArea.append("✗ Failed: " + failCount + "\n");
                crawlResultsArea.append("Total Comments: " + 
                    allPosts.stream().mapToInt(p -> p.getComments().size()).sum() + "\n");
                crawlResultsArea.append("=".repeat(60) + "\n");

                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                statusLabel.setText("✓ Crawl completed: " + successCount + " posts, " + failCount + " failed");

                // Display detailed results
                if (!allPosts.isEmpty()) {
                    updateCrawlResults(allPosts);
                }

            } catch (Exception e) {
                crawlResultsArea.append("\n✗ Error during crawling: " + e.getMessage());
                statusLabel.setText("✗ Error: " + e.getMessage());
                progressBar.setIndeterminate(false);
                System.err.println("Crawling error: " + e.getMessage());
            } finally {
                crawlUrlButton.setEnabled(true);
            }
        }).start();
    }

    private void addCommentsToPost(Post post, int commentLimit) {
        String[] commentTemplates = {
            "The relief distribution was well organized",
            "Not enough resources were provided to affected areas",
            "Great effort from the humanitarian team",
            "Need more medical support in the affected region",
            "Food aid arrived on time",
            "Disappointed with the response time",
            "Excellent coordination with local authorities",
            "More shelter needed for displaced families",
            "Transportation assistance was very helpful",
            "Cash assistance made a big difference"
        };

        int commentCount = Math.min(commentLimit, commentTemplates.length);
        for (int i = 0; i < commentCount; i++) {
            String content = commentTemplates[i];
            Comment comment = new Comment(
                "COMMENT_" + post.getPostId() + "_" + i,
                post.getPostId(),
                content,
                post.getCreatedAt().plusHours(i + 1),
                "User_" + (i + 1)
            );

            // Random sentiment
            Sentiment.SentimentType type = Math.random() > 0.5 ?
                (Math.random() > 0.5 ? Sentiment.SentimentType.POSITIVE : Sentiment.SentimentType.NEGATIVE)
                : Sentiment.SentimentType.NEUTRAL;
            double confidence = 0.7 + Math.random() * 0.3;

            comment.setSentiment(new Sentiment(type, confidence, content));
            comment.setReliefItem(post.getReliefItem());
            post.addComment(comment);
        }
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
            .filter(p -> p.getReliefItem() != null)
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
            results.append(String.format("  Category: %s\n", post.getReliefItem().getCategory().getDisplayName()));
            results.append(String.format("  Comments: %d\n", post.getComments().size()));
            results.append(String.format("  Sentiment: %s (%.2f)\n", 
                post.getSentiment().getType(),
                post.getSentiment().getConfidence()));
        }

        crawlResultsArea.setText(results.toString());
    }

    private void loadSampleData() {
        try {
            statusLabel.setText("Loading sample data...");
            
            // Create sample posts
            List<Post> samplePosts = new ArrayList<>();
            String[] sampleTopics = {"#yagi", "#bualoi", "#matmo"};
            ReliefItem.Category[] categories = ReliefItem.Category.values();

            for (int p = 0; p < 5; p++) {
                String topic = sampleTopics[p % sampleTopics.length];
                ReliefItem.Category category = categories[p % categories.length];
                ReliefItem reliefItem = new ReliefItem(category, "Relief for " + category.getDisplayName(), 3);

                FacebookPost post = new FacebookPost(
                    "POST_SAMPLE_" + p,
                    "Post about " + topic + " - " + category.getDisplayName() + " assistance needed",
                    LocalDateTime.now().minusHours(p),
                    "Author_" + p,
                    "PAGE_" + topic
                );

                Sentiment.SentimentType sentiment = Sentiment.SentimentType.values()[p % 3];
                post.setSentiment(new Sentiment(sentiment, 0.8 + Math.random() * 0.2, post.getContent()));
                post.setReliefItem(reliefItem);

                // Add comments
                for (int c = 0; c < 8; c++) {
                    Comment comment = new Comment(
                        "COMMENT_" + p + "_" + c,
                        post.getPostId(),
                        "Comment " + c + " on post " + p,
                        post.getCreatedAt().plusMinutes(c * 30),
                        "Commenter_" + c
                    );

                    Sentiment.SentimentType commentSentiment = Sentiment.SentimentType.values()[(p + c) % 3];
                    comment.setSentiment(new Sentiment(commentSentiment, 0.75 + Math.random() * 0.25, comment.getContent()));
                    comment.setReliefItem(reliefItem);
                    post.addComment(comment);
                }

                samplePosts.add(post);
            }

            // Add to model
            for (Post post : samplePosts) {
                model.addPost(post);
            }

            // Update results
            updateCrawlResults(samplePosts);
            statusLabel.setText("✓ Sample data loaded successfully!");
            crawlResultsArea.insert("✓ Loaded 5 sample posts with 8 comments each\n", 0);

        } catch (Exception e) {
            statusLabel.setText("✗ Error loading sample data: " + e.getMessage());
        }
    }

    /**
     * Update the disaster type combo box with available disaster types
     */
    private void updateDisasterTypeCombo() {
        disasterTypeCombo.removeAllItems();
        
        List<String> disasterNames = DisasterManager.getInstance().getAllDisasterNames();
        for (String name : disasterNames) {
            disasterTypeCombo.addItem(name);
        }
        
        // Set default to "yagi"
        if (disasterNames.contains("yagi")) {
            disasterTypeCombo.setSelectedItem("yagi");
        }
    }

    /**
     * Show dialog to add a new disaster type
     */
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

        // Disaster type name input
        panel.add(new JLabel("Disaster Type Name:"));
        JTextField nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(350, 30));
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(10));

        // Description of keywords/aliases
        panel.add(new JLabel("Keywords/Aliases (comma-separated):"));
        JTextArea aliasesArea = new JTextArea(3, 40);
        aliasesArea.setLineWrap(true);
        aliasesArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(aliasesArea);
        scrollPane.setMaximumSize(new Dimension(350, 80));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));

        // Buttons
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

            // Create and add new disaster type
            DisasterType newDisaster = DisasterManager.getInstance().getOrCreateDisasterType(name);
            
            // Add aliases
            if (!aliases.isEmpty()) {
                String[] aliasArray = aliases.split(",");
                for (String alias : aliasArray) {
                    newDisaster.addAlias(alias.trim());
                }
            }

            // Update combo box and select the new disaster
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

    /**
     * Find the appropriate disaster type for a post based on keywords used
     */
    @SuppressWarnings("unused")
    private DisasterType findDisasterTypeForPost(FacebookPost ignored, List<String> keywords) {
        DisasterManager manager = DisasterManager.getInstance();
        
        // Try to find a matching disaster type from the keywords
        for (String keyword : keywords) {
            DisasterType disaster = manager.findDisasterType(keyword);
            if (disaster != null) {
                return disaster;
            }
        }
        
        // Default to "yagi" if no match found
        return manager.getDisasterType("yagi");
    }
}
