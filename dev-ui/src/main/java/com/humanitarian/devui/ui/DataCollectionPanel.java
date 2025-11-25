package com.humanitarian.devui.ui;

import com.humanitarian.devui.model.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Panel for collecting new post data directly from the UI.
 * Allows users to manually input posts and comments without requiring web scraping.
 * Data is saved to SessionDataBuffer for review before committing to database.
 */
public class DataCollectionPanel extends JPanel {
    private Model model;
    private SessionDataBuffer buffer;
    private JTextArea contentArea;
    private JTextField authorField;
    private JTextField keywordField;
    private JComboBox<ReliefItem.Category> categoryCombo;
    private JComboBox<Sentiment.SentimentType> sentimentCombo;
    private JSpinner confidenceSpinner;
    private JCheckBox isCommentCheckBox;
    private JTextField parentPostIdField;
    private JLabel statusLabel;
    private JLabel bufferStatusLabel;

    public DataCollectionPanel(Model model, SessionDataBuffer buffer) {
        this.model = model;
        this.buffer = buffer;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("✏️ Data Entry - Manual Post/Comment Creation"));

        // Left panel - Input fields
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.WEST);

        // Right panel - Preview and action buttons
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.CENTER);

        // Bottom panel - Status
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        statusLabel = new JLabel("Ready to add new data");
        bufferStatusLabel = new JLabel("Buffer: 0 items");
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(bufferStatusLabel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
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

        // Keyword/Source field
        panel.add(new JLabel("Disaster Keyword (e.g., #yagi, #bualoi):"));
        keywordField = new JTextField(20);
        panel.add(keywordField);
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
        JLabel previewLabel = new JLabel("Buffer Status & Preview:");
        panel.add(previewLabel);

        JTextArea previewArea = new JTextArea(15, 40);
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JScrollPane scrollPane = new JScrollPane(previewArea);
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));

        // Add button
        JButton addButton = new JButton("Add to Buffer");
        addButton.setFont(new Font("Arial", Font.BOLD, 12));
        addButton.setMaximumSize(new Dimension(200, 40));
        addButton.setBackground(new Color(66, 133, 244));
        addButton.setForeground(Color.WHITE);
        addButton.setOpaque(true);
        addButton.setBorderPainted(false);
        addButton.addActionListener(e -> addPostOrComment(previewArea));
        panel.add(addButton);
        panel.add(Box.createVerticalStrut(5));

        // View buffer button
        JButton viewBufferButton = new JButton("View Buffer");
        viewBufferButton.setMaximumSize(new Dimension(200, 40));
        viewBufferButton.addActionListener(e -> showBufferStatus(previewArea));
        panel.add(viewBufferButton);


        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void addPostOrComment(JTextArea previewArea) {
        String content = contentArea.getText().trim();
        String author = authorField.getText().trim();
        String keyword = keywordField.getText().trim();
        ReliefItem.Category category = (ReliefItem.Category) categoryCombo.getSelectedItem();
        Sentiment.SentimentType sentimentType = (Sentiment.SentimentType) sentimentCombo.getSelectedItem();
        double confidence = ((Number) confidenceSpinner.getValue()).doubleValue();

        // Validation
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter post content", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (author.isEmpty()) {
            author = "User " + System.currentTimeMillis() % 1000;
        }

        if (keyword.isEmpty()) {
            keyword = "general";
        }

        try {
            if (isCommentCheckBox.isSelected()) {
                // Add as comment to buffer
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
                    LocalDateTime.now(),
                    author
                );
                comment.setSentiment(sentiment);
                comment.setReliefItem(reliefItem);

                buffer.addComment(comment);
                statusLabel.setText("✓ Comment added to buffer (not yet saved to DB)");
            } else {
                // Add as post to buffer
                Sentiment sentiment = new Sentiment(sentimentType, confidence, content);
                ReliefItem reliefItem = new ReliefItem(category, "Relief item", 3);

                FacebookPost post = new FacebookPost(
                    "POST_" + System.currentTimeMillis(),
                    content,
                    LocalDateTime.now(),
                    author,
                    "PAGE_" + keyword
                );
                post.setSentiment(sentiment);
                post.setReliefItem(reliefItem);

                buffer.addPost(post);
                statusLabel.setText("✓ Post added to buffer (not yet saved to DB)");
            }

            clearForm();
            bufferStatusLabel.setText("Buffer: " + (buffer.getTotalPosts() + buffer.getTotalComments()) + " items");
            previewArea.append("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] Data added to buffer\n");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("✗ Error: " + e.getMessage());
        }
    }

    private void clearForm() {
        contentArea.setText("");
        authorField.setText("");
        keywordField.setText("");
        categoryCombo.setSelectedIndex(0);
        sentimentCombo.setSelectedItem(Sentiment.SentimentType.NEUTRAL);
        confidenceSpinner.setValue(0.8);
        isCommentCheckBox.setSelected(false);
        parentPostIdField.setText("");
        parentPostIdField.setEnabled(false);
    }

    private void showBufferStatus(JTextArea previewArea) {
        previewArea.setText(buffer.generateSummary());
        bufferStatusLabel.setText("Buffer: " + (buffer.getTotalPosts() + buffer.getTotalComments()) + " items");
    }
}
