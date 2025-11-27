package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.model.*;
import com.humanitarian.logistics.database.DatabaseLoader;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Panel for collecting new post data directly from the UI.
 * Allows users to manually input posts and comments without requiring web scraping.
 * Also supports loading data from the dev-ui curated database.
 */
public class DataCollectionPanel extends JPanel {
    private final Model model;
    private JTextArea postContentArea;
    private JTextArea commentsArea;
    private JTextField authorField;
    private JComboBox<String> disasterTypeCombo;
    private JComboBox<ReliefItem.Category> categoryCombo;
    private JComboBox<Sentiment.SentimentType> sentimentCombo;
    private JSpinner confidenceSpinner;
    private JLabel statusLabel;

    public DataCollectionPanel(Model model) {
        this.model = model;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("📝 Data Collection - Add Posts & Comments"));

        // Main content area - two panels side by side
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left side - Post input
        mainPanel.add(createPostInputPanel());

        // Right side - Comments input
        mainPanel.add(createCommentsInputPanel());

        add(mainPanel, BorderLayout.CENTER);

        // Bottom panel - Control buttons and status
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createPostInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Post Information"));

        // Input fields panel
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Author
        fieldsPanel.add(new JLabel("Author:"));
        authorField = new JTextField("Anonymous");
        authorField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        fieldsPanel.add(authorField);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Disaster Type
        fieldsPanel.add(new JLabel("Disaster Type:"));
        disasterTypeCombo = new JComboBox<>();
        updateDisasterTypeCombo();
        disasterTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        fieldsPanel.add(disasterTypeCombo);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Relief Category
        fieldsPanel.add(new JLabel("Relief Category:"));
        categoryCombo = new JComboBox<>(ReliefItem.Category.values());
        categoryCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        fieldsPanel.add(categoryCombo);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Sentiment
        fieldsPanel.add(new JLabel("Sentiment:"));
        sentimentCombo = new JComboBox<>(Sentiment.SentimentType.values());
        sentimentCombo.setSelectedItem(Sentiment.SentimentType.NEUTRAL);
        sentimentCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        fieldsPanel.add(sentimentCombo);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // Confidence
        fieldsPanel.add(new JLabel("Confidence (0.0 - 1.0):"));
        confidenceSpinner = new JSpinner(new SpinnerNumberModel(0.8, 0.0, 1.0, 0.1));
        fieldsPanel.add(confidenceSpinner);
        fieldsPanel.add(Box.createVerticalStrut(10));

        // Post content
        fieldsPanel.add(new JLabel("Post Content:"));
        postContentArea = new JTextArea(8, 35);
        postContentArea.setLineWrap(true);
        postContentArea.setWrapStyleWord(true);
        JScrollPane postScroll = new JScrollPane(postContentArea);
        fieldsPanel.add(postScroll);
        fieldsPanel.add(Box.createVerticalStrut(5));

        // Info label
        JLabel infoLabel = new JLabel("Post ID will be auto-generated");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        infoLabel.setForeground(new Color(100, 100, 100));
        fieldsPanel.add(infoLabel);

        panel.add(fieldsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCommentsInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Comments (one per line)"));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(5, 5));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPanel.add(new JLabel("Enter each comment on a new line:"), BorderLayout.NORTH);

        commentsArea = new JTextArea(16, 35);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsArea.setFont(new Font("Arial", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(commentsArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Example label
        JLabel exampleLabel = new JLabel("Example: Line 1 = Comment 1, Line 2 = Comment 2, etc.");
        exampleLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        exampleLabel.setForeground(new Color(100, 100, 100));
        contentPanel.add(exampleLabel, BorderLayout.SOUTH);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Status label
        statusLabel = new JLabel("Ready to add new post with comments");
        panel.add(statusLabel, BorderLayout.WEST);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        // Clear button
        JButton clearButton = new JButton("Clear");
        clearButton.setPreferredSize(new Dimension(100, 35));
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);

        // Save button
        JButton saveButton = new JButton("💾 Save Post & Comments");
        saveButton.setPreferredSize(new Dimension(200, 35));
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));
        saveButton.setBackground(new Color(46, 204, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.setOpaque(true);
        saveButton.addActionListener(e -> savePostWithComments());
        buttonPanel.add(saveButton);

        // Use Our Database button
        JButton useOurDatabaseButton = new JButton("📚 Use Our Database");
        useOurDatabaseButton.setPreferredSize(new Dimension(180, 35));
        useOurDatabaseButton.setFont(new Font("Arial", Font.BOLD, 12));
        useOurDatabaseButton.setBackground(new Color(34, 139, 34)); // Forest Green
        useOurDatabaseButton.setForeground(Color.WHITE);
        useOurDatabaseButton.setOpaque(true);
        useOurDatabaseButton.setBorderPainted(false);
        useOurDatabaseButton.addActionListener(e -> loadOurDatabase());
        buttonPanel.add(useOurDatabaseButton);

        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }

    private void savePostWithComments() {
        String postContent = postContentArea.getText().trim();
        String commentsText = commentsArea.getText().trim();
        String author = authorField.getText().trim();
        String disasterType = (String) disasterTypeCombo.getSelectedItem();
        ReliefItem.Category category = (ReliefItem.Category) categoryCombo.getSelectedItem();
        Sentiment.SentimentType sentiment = (Sentiment.SentimentType) sentimentCombo.getSelectedItem();
        double confidence = ((Number) confidenceSpinner.getValue()).doubleValue();

        // Validation
        if (postContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter post content", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (disasterType == null || disasterType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a Disaster Type", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Generate unique post ID
            String postId = UUID.randomUUID().toString().substring(0, 13);
            LocalDateTime now = LocalDateTime.now();

            // Create and save post
            FacebookPost post = new FacebookPost(
                postId,
                postContent,
                now,
                author.isEmpty() ? "Anonymous" : author,
                "manual_entry"
            );

            // Set post metadata
            post.setDisasterKeyword(disasterType);
            post.setSentiment(new Sentiment(sentiment, confidence, postContent));
            if (category != null) {
                post.setReliefItem(new ReliefItem(category, category.name(), 1));
            }

            // Set disaster type
            DisasterType disaster = DisasterManager.getInstance().findDisasterType(disasterType);
            if (disaster != null) {
                post.setDisasterType(disaster);
            }

            // Add post to model (auto-saves to database)
            model.addPost(post);

            int commentCount = 0;

            // Parse and save comments
            if (!commentsText.isEmpty()) {
                String[] commentLines = commentsText.split("\n");
                for (String line : commentLines) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        commentCount++;
                        
                        // Generate comment ID
                        String commentId = "COMMENT_" + System.currentTimeMillis() + "_" + commentCount;

                        // Create comment
                        Comment comment = new Comment(
                            commentId,
                            postId,
                            line,
                            now,
                            author.isEmpty() ? "Anonymous" : author
                        );

                        // Set comment metadata
                        comment.setSentiment(new Sentiment(sentiment, confidence, line));
                        if (category != null) {
                            comment.setReliefItem(new ReliefItem(category, category.name(), 1));
                        }

                        // Add comment to post
                        post.addComment(comment);
                    }
                }
            }

            // Show success message
            String message = String.format(
                "✓ Post saved successfully!\n\n" +
                "Post ID: %s\n" +
                "Comments: %d\n" +
                "Author: %s\n" +
                "Disaster: %s",
                postId, commentCount, author.isEmpty() ? "Anonymous" : author, disasterType
            );

            statusLabel.setText("✓ Post saved with " + commentCount + " comments");
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear form
            clearForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving post: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("✗ Error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        postContentArea.setText("");
        commentsArea.setText("");
        authorField.setText("Anonymous");
        disasterTypeCombo.setSelectedIndex(0);
        categoryCombo.setSelectedIndex(0);
        sentimentCombo.setSelectedItem(Sentiment.SentimentType.NEUTRAL);
        confidenceSpinner.setValue(0.8);
        statusLabel.setText("Form cleared");
    }

    private void loadOurDatabase() {
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
            List<Post> posts = model.getPosts();
            int totalPosts = posts.size();
            int totalComments = posts.stream().mapToInt(p -> p.getComments().size()).sum();
            
            String loadMsg = "✓ Our database loaded successfully\n" +
                            "Posts imported: " + totalPosts + "\n" +
                            "Comments: " + totalComments + "\n" +
                            "New disaster types added: " + addedCount;
            
            statusLabel.setText(loadMsg);
            
            // Save all data to database to persist it
            try {
                com.humanitarian.logistics.database.DatabaseManager dbMgr = new com.humanitarian.logistics.database.DatabaseManager();
                for (Post post : model.getPosts()) {
                    dbMgr.savePost(post);
                    for (Comment comment : post.getComments()) {
                        dbMgr.saveComment(comment);
                    }
                }
                loadMsg += "\n✓ Data saved to database";
            } catch (Exception dbEx) {
                System.err.println("Warning: Data not saved to database: " + dbEx.getMessage());
            }
            
            JOptionPane.showMessageDialog(this, loadMsg, "Database Loaded", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("✗ Error: " + e.getMessage());
        }
    }

    private void updateDisasterTypeCombo() {
        disasterTypeCombo.removeAllItems();
        List<String> disasterNames = DisasterManager.getInstance().getAllDisasterNames();
        for (String name : disasterNames) {
            disasterTypeCombo.addItem(name);
        }
        if (disasterTypeCombo.getItemCount() == 0) {
            disasterTypeCombo.addItem("yagi");
            disasterTypeCombo.addItem("flood");
            disasterTypeCombo.addItem("matmo");
        }
    }
}
