package com.humanitarian.devui.ui;

import com.humanitarian.devui.model.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Redesigned Data Collection Panel for dev-ui.
 * Simple, intuitive interface for adding posts with comments.
 * Features:
 * - Post Content: User input (ID auto-generated)
 * - Comments: Each line = one comment (parsed and saved separately)
 * - One-click save: Saves post + all comments to database
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

    public DataCollectionPanel(Model model, SessionDataBuffer buffer) {
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
            YouTubePost post = new YouTubePost(
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

            int commentCount = 0;

            // Parse and add comments BEFORE adding post to model
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

            // NOW add post to model with all comments already attached
            model.addPost(post);

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
