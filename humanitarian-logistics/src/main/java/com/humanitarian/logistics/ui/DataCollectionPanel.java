package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.model.*;
import com.humanitarian.logistics.database.DatabaseLoader;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createPostInputPanel());

        mainPanel.add(createCommentsInputPanel());

        add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createPostInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Post Information"));

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        fieldsPanel.add(new JLabel("Author:"));
        authorField = new JTextField("Anonymous");
        authorField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        fieldsPanel.add(authorField);
        fieldsPanel.add(Box.createVerticalStrut(8));

        fieldsPanel.add(new JLabel("Disaster Type:"));
        disasterTypeCombo = new JComboBox<>();
        updateDisasterTypeCombo();
        disasterTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        fieldsPanel.add(disasterTypeCombo);
        fieldsPanel.add(Box.createVerticalStrut(8));

        fieldsPanel.add(new JLabel("Relief Category:"));
        categoryCombo = new JComboBox<>(ReliefItem.Category.values());
        categoryCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        fieldsPanel.add(categoryCombo);
        fieldsPanel.add(Box.createVerticalStrut(8));

        fieldsPanel.add(new JLabel("Sentiment:"));
        sentimentCombo = new JComboBox<>(Sentiment.SentimentType.values());
        sentimentCombo.setSelectedItem(Sentiment.SentimentType.NEUTRAL);
        sentimentCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        fieldsPanel.add(sentimentCombo);
        fieldsPanel.add(Box.createVerticalStrut(8));

        fieldsPanel.add(new JLabel("Confidence (0.0 - 1.0):"));
        confidenceSpinner = new JSpinner(new SpinnerNumberModel(0.8, 0.0, 1.0, 0.1));
        fieldsPanel.add(confidenceSpinner);
        fieldsPanel.add(Box.createVerticalStrut(10));

        fieldsPanel.add(new JLabel("Post Content:"));
        postContentArea = new JTextArea(8, 35);
        postContentArea.setLineWrap(true);
        postContentArea.setWrapStyleWord(true);
        JScrollPane postScroll = new JScrollPane(postContentArea);
        fieldsPanel.add(postScroll);
        fieldsPanel.add(Box.createVerticalStrut(5));

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

        statusLabel = new JLabel("Ready to add new post with comments");
        panel.add(statusLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        JButton clearButton = new JButton("Clear");
        clearButton.setPreferredSize(new Dimension(100, 35));
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);

        JButton saveButton = new JButton("💾 Save Post & Comments");
        saveButton.setPreferredSize(new Dimension(200, 35));
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));
        saveButton.setBackground(new Color(46, 204, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.setOpaque(true);
        saveButton.addActionListener(e -> savePostWithComments());
        buttonPanel.add(saveButton);

        JButton useOurDatabaseButton = new JButton("📚 Use Our Database");
        useOurDatabaseButton.setPreferredSize(new Dimension(180, 35));
        useOurDatabaseButton.setFont(new Font("Arial", Font.BOLD, 12));
        useOurDatabaseButton.setBackground(new Color(34, 139, 34));
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

        if (postContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter post content", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (disasterType == null || disasterType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a Disaster Type", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {

            String postId = UUID.randomUUID().toString().substring(0, 13);
            LocalDateTime now = LocalDateTime.now();

            YouTubePost post = new YouTubePost(
                postId,
                postContent,
                now,
                author.isEmpty() ? "Anonymous" : author,
                "manual_entry"
            );

            post.setDisasterKeyword(disasterType);
            post.setSentiment(new Sentiment(sentiment, confidence, postContent));
            if (category != null) {
                post.setReliefItem(new ReliefItem(category, category.name(), 1));
            }

            DisasterType disaster = DisasterManager.getInstance().findDisasterType(disasterType);
            if (disaster != null) {
                post.setDisasterType(disaster);
            }

            model.addPost(post);

            int commentCount = 0;

            if (!commentsText.isEmpty()) {
                String[] commentLines = commentsText.split("\n");
                for (String line : commentLines) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        commentCount++;
                        
                        String commentId = "COMMENT_" + System.currentTimeMillis() + "_" + commentCount;

                        Comment comment = new Comment(
                            commentId,
                            postId,
                            line,
                            now,
                            author.isEmpty() ? "Anonymous" : author
                        );

                        comment.setSentiment(new Sentiment(sentiment, confidence, line));
                        if (category != null) {
                            comment.setReliefItem(new ReliefItem(category, category.name(), 1));
                        }

                        post.addComment(comment);
                    }
                }
            }

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

            java.util.Set<String> userDisasters = new java.util.HashSet<>(
                DisasterManager.getInstance().getAllDisasterNames()
            );
            
            DatabaseLoader.loadOurDatabase(model);
            
            java.util.Set<String> missingDisasters = new java.util.HashSet<>();
            
            for (Post post : model.getPosts()) {

                String disasterKeyword = post.getDisasterKeyword();
                if (disasterKeyword != null && !disasterKeyword.isEmpty()) {
                    String normalizedDisaster = DisasterType.normalize(disasterKeyword);
                    
                    if (!userDisasters.contains(normalizedDisaster) && 
                        !missingDisasters.contains(normalizedDisaster)) {
                        missingDisasters.add(normalizedDisaster);
                    }
                }
                
                for (Comment comment : post.getComments()) {
                    checkAndCollectMissingDisasters(comment.getContent().toLowerCase(), 
                        userDisasters, missingDisasters);
                }
            }
            
            int addedCount = 0;
            for (String disaster : missingDisasters) {
                DisasterManager.getInstance().getOrCreateDisasterType(disaster);
                addedCount++;
            }
            
            List<Post> posts = model.getPosts();
            int totalPosts = posts.size();
            int totalComments = posts.stream().mapToInt(p -> p.getComments().size()).sum();
            
            String loadMsg = "✓ Our database loaded successfully\n" +
                            "Posts imported: " + totalPosts + "\n" +
                            "Comments: " + totalComments + "\n" +
                            "New disaster types added: " + addedCount;
            
            statusLabel.setText(loadMsg);
            
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

    private void checkAndCollectMissingDisasters(String content, 
                                                   java.util.Set<String> userDisasters, 
                                                   java.util.Set<String> missingDisasters) {
        DisasterManager disasterManager = DisasterManager.getInstance();
        
        for (DisasterType disaster : disasterManager.getAllDisasterTypes()) {
            String disasterName = disaster.getName();
            
            if (disaster.getAliases().stream().anyMatch(content::contains)) {

                if (!userDisasters.contains(disasterName)) {
                    missingDisasters.add(disasterName);
                }
            }
        }
    }
}
