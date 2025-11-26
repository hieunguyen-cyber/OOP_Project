package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.model.*;
import com.humanitarian.logistics.database.DatabaseLoader;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for collecting new post data directly from the UI.
 * Allows users to manually input posts and comments without requiring web scraping.
 */
public class DataCollectionPanel extends JPanel {
    private Model model;
    private JTextArea contentArea;
    private JTextField authorField;
    private JComboBox<String> disasterTypeCombo;
    private JComboBox<ReliefItem.Category> categoryCombo;
    private JComboBox<Sentiment.SentimentType> sentimentCombo;
    private JSpinner confidenceSpinner;
    private JCheckBox isCommentCheckBox;
    private JTextField parentPostIdField;
    private JSpinner dateSpinner;
    private JLabel statusLabel;

    public DataCollectionPanel(Model model) {
        this.model = model;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Data Collection - Manual Entry"));

        // Left panel - Input fields
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.WEST);

        // Right panel - Preview and action buttons
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.CENTER);

        // Bottom panel - Status
        statusLabel = new JLabel("Ready to add new data");
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(350, 0));

        // Content area
        JLabel contentLabel = new JLabel("Post Content:");
        panel.add(contentLabel);
        contentArea = new JTextArea(5, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));

        // Author field
        panel.add(new JLabel("Author:"));
        authorField = new JTextField(20);
        panel.add(authorField);
        panel.add(Box.createVerticalStrut(8));

        // Date field (yyyy-MM-dd format)
        panel.add(new JLabel("Date (yyyy-MM-dd): *"));
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new java.util.Date());
        panel.add(dateSpinner);
        panel.add(Box.createVerticalStrut(8));

        // Disaster Type Dropdown
        panel.add(new JLabel("Disaster Type: *"));
        disasterTypeCombo = new JComboBox<>();
        updateDisasterTypeCombo();
        disasterTypeCombo.setMaximumSize(new Dimension(300, 25));
        panel.add(disasterTypeCombo);
        panel.add(Box.createVerticalStrut(8));

        // Relief Category
        panel.add(new JLabel("Relief Category:"));
        categoryCombo = new JComboBox<>(ReliefItem.Category.values());
        panel.add(categoryCombo);
        panel.add(Box.createVerticalStrut(8));

        // Sentiment
        panel.add(new JLabel("Sentiment:"));
        sentimentCombo = new JComboBox<>(Sentiment.SentimentType.values());
        sentimentCombo.setSelectedItem(Sentiment.SentimentType.NEUTRAL);
        panel.add(sentimentCombo);
        panel.add(Box.createVerticalStrut(8));

        // Confidence score
        panel.add(new JLabel("Confidence (0.0 - 1.0):"));
        confidenceSpinner = new JSpinner(new SpinnerNumberModel(0.8, 0.0, 1.0, 0.1));
        panel.add(confidenceSpinner);
        panel.add(Box.createVerticalStrut(8));

        // Is comment checkbox
        isCommentCheckBox = new JCheckBox("This is a comment on a post");
        isCommentCheckBox.addActionListener(e -> {
            parentPostIdField.setEnabled(isCommentCheckBox.isSelected());
        });
        panel.add(isCommentCheckBox);
        panel.add(Box.createVerticalStrut(8));

        // Parent post ID field
        panel.add(new JLabel("Parent Post ID (if comment):"));
        parentPostIdField = new JTextField(20);
        parentPostIdField.setEnabled(false);
        panel.add(parentPostIdField);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Preview text area
        JLabel previewLabel = new JLabel("Preview & Statistics:");
        panel.add(previewLabel);

        JTextArea previewArea = new JTextArea(15, 40);
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JScrollPane scrollPane = new JScrollPane(previewArea);
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));

        // Add button
        JButton addButton = new JButton("Add Post/Comment");
        addButton.setFont(new Font("Arial", Font.BOLD, 12));
        addButton.setMaximumSize(new Dimension(200, 40));
        addButton.addActionListener(e -> addPostOrComment(previewArea));
        panel.add(addButton);
        panel.add(Box.createVerticalStrut(5));

        // Batch import (future feature)
        JButton statsButton = new JButton("Show Data Statistics");
        statsButton.setMaximumSize(new Dimension(200, 40));
        statsButton.addActionListener(e -> showDataStatistics(previewArea));
        panel.add(statsButton);
        panel.add(Box.createVerticalStrut(5));

        // Use Our Database button
        JButton useOurDatabaseButton = new JButton("Use Our Database");
        useOurDatabaseButton.setFont(new Font("Arial", Font.BOLD, 12));
        useOurDatabaseButton.setMaximumSize(new Dimension(200, 40));
        useOurDatabaseButton.setBackground(new Color(34, 139, 34)); // Forest Green
        useOurDatabaseButton.setForeground(Color.WHITE);
        useOurDatabaseButton.setOpaque(true);
        useOurDatabaseButton.setBorderPainted(false);
        useOurDatabaseButton.setFocusPainted(false);
        useOurDatabaseButton.addActionListener(e -> loadOurDatabase(previewArea));
        panel.add(useOurDatabaseButton);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void addPostOrComment(JTextArea previewArea) {
        String content = contentArea.getText().trim();
        String author = authorField.getText().trim();
        String selectedDisaster = (String) disasterTypeCombo.getSelectedItem();
        ReliefItem.Category category = (ReliefItem.Category) categoryCombo.getSelectedItem();
        Sentiment.SentimentType sentimentType = (Sentiment.SentimentType) sentimentCombo.getSelectedItem();
        double confidence = ((Number) confidenceSpinner.getValue()).doubleValue();
        java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();

        // Validation
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter post content", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (author.isEmpty()) {
            author = "User " + System.currentTimeMillis() % 1000;
        }

        if (selectedDisaster == null || selectedDisaster.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a Disaster Type", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert java.util.Date to LocalDateTime
        LocalDateTime postDateTime = selectedDate.toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDateTime();

        try {
            if (isCommentCheckBox.isSelected()) {
                // Add as comment
                String parentPostId = parentPostIdField.getText().trim();
                if (parentPostId.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter parent post ID for comment", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Sentiment sentiment = new Sentiment(sentimentType, confidence, content);
                ReliefItem reliefItem = new ReliefItem(category, "Relief item", 3);

                Comment comment = new Comment(
                    "COMMENT_" + System.currentTimeMillis(),
                    parentPostId,
                    content,
                    postDateTime,
                    author
                );
                comment.setSentiment(sentiment);
                comment.setReliefItem(reliefItem);

                FacebookPost post = new FacebookPost(
                    parentPostId,
                    content,
                    postDateTime,
                    author,
                    "PAGE_" + selectedDisaster
                );
                post.setSentiment(sentiment);
                post.setReliefItem(reliefItem);
                post.addComment(comment);

                model.addPost(post);
                statusLabel.setText("✓ Comment added successfully");
            } else {
                // Add as post
                Sentiment sentiment = new Sentiment(sentimentType, confidence, content);
                ReliefItem reliefItem = new ReliefItem(category, "Relief item", 3);

                FacebookPost post = new FacebookPost(
                    "POST_" + System.currentTimeMillis(),
                    content,
                    postDateTime,
                    author,
                    "PAGE_" + selectedDisaster
                );
                post.setSentiment(sentiment);
                post.setReliefItem(reliefItem);

                model.addPost(post);
                statusLabel.setText("✓ Post added: " + post.getPostId());
            }

            clearForm();
            previewArea.append("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] Data added successfully\n");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("✗ Error: " + e.getMessage());
        }
    }

    private void clearForm() {
        contentArea.setText("");
        authorField.setText("");
        dateSpinner.setValue(new java.util.Date());
        categoryCombo.setSelectedIndex(0);
        sentimentCombo.setSelectedItem(Sentiment.SentimentType.NEUTRAL);
        confidenceSpinner.setValue(0.8);
        isCommentCheckBox.setSelected(false);
        parentPostIdField.setText("");
        parentPostIdField.setEnabled(false);
    }

    private void showDataStatistics(JTextArea previewArea) {
        java.util.List<Post> posts = model.getPosts();
        int totalPosts = posts.size();
        int totalComments = posts.stream().mapToInt(p -> p.getComments().size()).sum();

        int positive = (int) posts.stream().filter(p -> p.getSentiment().getType() == Sentiment.SentimentType.POSITIVE).count();
        int negative = (int) posts.stream().filter(p -> p.getSentiment().getType() == Sentiment.SentimentType.NEGATIVE).count();
        int neutral = (int) posts.stream().filter(p -> p.getSentiment().getType() == Sentiment.SentimentType.NEUTRAL).count();

        previewArea.setText("=== DATA STATISTICS ===\n");
        previewArea.append("Total Posts: " + totalPosts + "\n");
        previewArea.append("Total Comments: " + totalComments + "\n");
        previewArea.append("Positive: " + positive + "\n");
        previewArea.append("Negative: " + negative + "\n");
        previewArea.append("Neutral: " + neutral + "\n");
    }

    private void loadOurDatabase(JTextArea previewArea) {
        int confirmResult = JOptionPane.showConfirmDialog(this, 
            "This will replace all your current data with our curated database.\n" +
            "Your current data will be lost. Continue?",
            "Load Our Database",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmResult != JOptionPane.YES_OPTION) {
            statusLabel.setText("⊘ Database load cancelled");
            return;
        }
        
        try {
            // Get disaster types before loading new data
            java.util.Set<String> userDisasters = new java.util.HashSet<>(
                DisasterManager.getInstance().getAllDisasterNames()
            );
            
            // Load the fixed database
            DatabaseLoader.loadOurDatabase(model);
            
            // Auto-detect and add missing disaster types from loaded data
            java.util.Set<String> missingDisasters = new java.util.HashSet<>();
            for (Post post : model.getPosts()) {
                if (post instanceof FacebookPost) {
                    FacebookPost fbPost = (FacebookPost) post;
                    String pageId = fbPost.getPageId();
                    
                    // Extract disaster name from PAGE_xxx format
                    if (pageId.startsWith("PAGE_")) {
                        String disasterName = pageId.substring(5);
                        if (!userDisasters.contains(disasterName)) {
                            missingDisasters.add(disasterName);
                        }
                    }
                }
            }
            
            // Add missing disasters automatically
            int addedCount = 0;
            for (String disaster : missingDisasters) {
                DisasterManager.getInstance().getOrCreateDisasterType(disaster);
                addedCount++;
            }
            
            // Show statistics
            showDataStatistics(previewArea);
            
            String loadMsg = "✓ Our database loaded successfully\n" +
                            "Posts imported: " + model.getPosts().size() + "\n" +
                            "New disaster types added: " + addedCount;
            
            statusLabel.setText(loadMsg);
            previewArea.insert("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + 
                "] " + loadMsg + "\n", 0);
            
            JOptionPane.showMessageDialog(this, loadMsg, "Database Loaded", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("✗ Error: " + e.getMessage());
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
}
