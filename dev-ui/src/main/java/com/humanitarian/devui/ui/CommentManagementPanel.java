package com.humanitarian.devui.ui;

import com.humanitarian.devui.model.*;
import com.humanitarian.devui.database.DatabaseManager;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Excel-style table panel for managing comments.
 * Allows users to view, edit, and manage individual comments.
 */
public class CommentManagementPanel extends JPanel implements ModelListener {
    private final Model model;
    private DatabaseManager dbManager;
    private JTable commentTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsArea;
    private JLabel statusLabel;
    private JLabel totalLabel;

    public CommentManagementPanel(Model model, SessionDataBuffer buffer) {
        this.model = model;
        try {
            this.dbManager = new DatabaseManager();
        } catch (Exception e) {
            System.err.println("Error initializing DatabaseManager: " + e.getMessage());
            this.dbManager = null;
        }
        // Register as model listener to get notified of changes
        model.addModelListener(this);
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("💬 Comment Management - Excel Style"));

        // Top: Statistics + Refresh button
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.NORTH);

        // Middle: Table and Details
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        // Bottom: Status
        statusLabel = new JLabel("Ready - Select a comment to view details");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.setBackground(new Color(240, 240, 240));

        totalLabel = new JLabel("📊 ");
        updateStatsPanel();

        panel.add(totalLabel);
        
        // Refresh button
        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        refreshBtn.addActionListener(e -> {
            refreshTable();
            updateStatsPanel();
            statusLabel.setText("✓ Table refreshed");
        });
        panel.add(Box.createHorizontalStrut(20));
        panel.add(refreshBtn);

        return panel;
    }

    private void updateStatsPanel() {
        int total = model.getPosts().stream().mapToInt(p -> p.getComments().size()).sum();
        totalLabel.setText("Total Comments: " + total);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));

        // Table
        String[] columns = {"Comment ID", "Author", "Posted At", "Sentiment", "Content Preview"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Editing via details panel
            }
        };

        commentTable = new JTable(tableModel);
        commentTable.setRowHeight(25);
        commentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = commentTable.getSelectedRow();
                if (selectedRow >= 0) {
                    showCommentDetails(selectedRow);
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(commentTable);
        tableScroll.setPreferredSize(new Dimension(0, 300));
        panel.add(tableScroll, BorderLayout.CENTER);

        // Details panel
        JPanel detailsPanel = createDetailsPanel();
        panel.add(detailsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Comment Details & Actions"));
        panel.setPreferredSize(new Dimension(0, 200));

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(detailsArea);
        panel.add(scroll, BorderLayout.CENTER);

        // Action buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton deleteButton = new JButton("🗑️ Delete");
        deleteButton.setFont(new Font("Arial", Font.BOLD, 11));
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setOpaque(true);
        deleteButton.addActionListener(e -> {
            try {
                deleteSelectedComment();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Delete Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        buttonPanel.add(deleteButton);

        JButton editButton = new JButton("✏️ Edit");
        editButton.setFont(new Font("Arial", Font.BOLD, 11));
        editButton.setBackground(new Color(0, 123, 255));
        editButton.setForeground(Color.WHITE);
        editButton.setOpaque(true);
        editButton.addActionListener(e -> {
            try {
                editSelectedComment();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Edit Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        buttonPanel.add(editButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);

        // Add comments from all posts
        for (Post post : model.getPosts()) {
            for (Comment comment : post.getComments()) {
                addCommentRow(comment);
            }
        }

        statusLabel.setText("Loaded " + tableModel.getRowCount() + " comments");
    }

    private void addCommentRow(Comment comment) {
        String sentimentType = comment.getSentiment() != null ? 
            comment.getSentiment().getType().toString() : "N/A";
        
        String content = comment.getContent();
        if (content.length() > 50) {
            content = content.substring(0, 47) + "...";
        }

        String dateStr = comment.getCreatedAt() != null ?
            comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) :
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        tableModel.addRow(new Object[]{
            comment.getCommentId(),
            comment.getAuthor(),
            dateStr,
            sentimentType,
            content
        });
    }

    private void showCommentDetails(int row) {
        Comment comment = null;
        int count = 0;
        for (Post post : model.getPosts()) {
            for (Comment c : post.getComments()) {
                if (count == row) {
                    comment = c;
                    break;
                }
                count++;
            }
            if (comment != null) break;
        }

        if (comment != null) {
            StringBuilder details = new StringBuilder();
            details.append("=== COMMENT DETAILS ===\n\n");
            details.append("ID: ").append(comment.getCommentId()).append("\n");
            details.append("Author: ").append(comment.getAuthor()).append("\n");
            details.append("Posted: ").append(comment.getCreatedAt()).append("\n");
            details.append("Sentiment: ").append(comment.getSentiment().getType()).append("\n");
            details.append("Confidence: ").append(String.format("%.2f", comment.getSentiment().getConfidence())).append("\n");
            details.append("\n--- Content ---\n");
            details.append(comment.getContent()).append("\n");
            
            if (comment.getReliefItem() != null) {
                details.append("\n--- Relief Category ---\n");
                details.append(comment.getReliefItem().getCategory()).append("\n");
            }

            detailsArea.setText(details.toString());
            statusLabel.setText("✓ Comment selected: " + comment.getCommentId());
        }
    }

    private void deleteSelectedComment() {
        int selectedRow = commentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a comment to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Find comment and its post
        Comment commentToDelete = null;
        Post parentPost = null;
        int count = 0;
        
        for (Post post : model.getPosts()) {
            for (Comment c : post.getComments()) {
                if (count == selectedRow) {
                    commentToDelete = c;
                    parentPost = post;
                    break;
                }
                count++;
            }
            if (commentToDelete != null) break;
        }

        if (commentToDelete == null) {
            JOptionPane.showMessageDialog(this, "Comment not found", "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("✗ Error: Comment not found");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete comment from " + commentToDelete.getAuthor() + "?\n" +
            "Content: \"" + commentToDelete.getContent().substring(0, Math.min(50, commentToDelete.getContent().length())) + "...\"\n" +
            "This will be saved to database.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Remove from model
                if (parentPost != null) {
                    parentPost.removeComment(commentToDelete.getCommentId());
                }
                
                // Delete from database
                if (dbManager != null) {
                    dbManager.deleteComment(commentToDelete.getCommentId());
                }
                
                // Refresh UI
                refreshTable();
                detailsArea.setText("");
                statusLabel.setText("✓ Comment deleted and saved to database");
                JOptionPane.showMessageDialog(this, "✓ Comment deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting comment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("✗ Error deleting comment: " + e.getMessage());
            }
        }
    }

    private void editSelectedComment() {
        int selectedRow = commentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a comment to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Find comment and its post
        Comment commentToEdit = null;
        Post parentPost = null;
        int count = 0;
        
        for (Post post : model.getPosts()) {
            for (Comment c : post.getComments()) {
                if (count == selectedRow) {
                    commentToEdit = c;
                    parentPost = post;
                    break;
                }
                count++;
            }
            if (commentToEdit != null) break;
        }

        if (commentToEdit == null) {
            JOptionPane.showMessageDialog(this, "Comment not found", "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("✗ Error: Comment not found");
            return;
        }

        if (commentToEdit != null && parentPost != null) {
            showEditDialog(commentToEdit, parentPost);
        }
    }

    private void showEditDialog(Comment comment, Post parentPost) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Edit Comment", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Content
        panel.add(new JLabel("Comment Content:"));
        JTextArea contentArea = new JTextArea(5, 50);
        contentArea.setText(comment.getContent());
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(new Font("Arial", Font.PLAIN, 11));
        panel.add(new JScrollPane(contentArea));
        panel.add(Box.createVerticalStrut(10));

        // Sentiment
        panel.add(new JLabel("Sentiment:"));
        JComboBox<Sentiment.SentimentType> sentimentCombo = new JComboBox<>(Sentiment.SentimentType.values());
        sentimentCombo.setSelectedItem(comment.getSentiment().getType());
        sentimentCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        panel.add(sentimentCombo);
        panel.add(Box.createVerticalStrut(10));

        // Confidence
        panel.add(new JLabel("Confidence (0.0 - 1.0):"));
        JSpinner confidenceSpinner = new JSpinner(new SpinnerNumberModel(
            comment.getSentiment().getConfidence(), 0.0, 1.0, 0.1
        ));
        panel.add(confidenceSpinner);
        panel.add(Box.createVerticalGlue());

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton saveBtn = new JButton("💾 Save Changes");
        saveBtn.setFont(new Font("Arial", Font.BOLD, 11));
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setOpaque(true);
        saveBtn.addActionListener(e -> {
            try {
                String newContent = contentArea.getText().trim();
                if (newContent.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Comment content cannot be empty", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Sentiment.SentimentType newType = (Sentiment.SentimentType) sentimentCombo.getSelectedItem();
                double newConfidence = ((Number) confidenceSpinner.getValue()).doubleValue();
                
                // Create updated comment
                Sentiment newSentiment = new Sentiment(newType, newConfidence, newContent);
                Comment updatedComment = new Comment(
                    comment.getCommentId(),
                    comment.getPostId(),
                    newContent,
                    comment.getCreatedAt(),
                    comment.getAuthor()
                );
                updatedComment.setSentiment(newSentiment);
                if (comment.getReliefItem() != null) {
                    updatedComment.setReliefItem(comment.getReliefItem());
                }
                
                // Update in model
                if (parentPost != null) {
                    parentPost.updateComment(updatedComment);
                }
                
                // Update in database
                if (dbManager != null) {
                    dbManager.updateComment(updatedComment);
                }
                
                // Refresh UI
                refreshTable();
                dialog.dispose();
                statusLabel.setText("✓ Comment updated and saved to database");
                JOptionPane.showMessageDialog(CommentManagementPanel.this, "✓ Comment updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating comment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("✗ Error updating comment: " + ex.getMessage());
            }
        });
        buttonPanel.add(saveBtn);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 11));
        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelBtn);

        panel.add(buttonPanel);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * Clear and refresh the comment table after database reset
     */
    public void clearAndRefresh() {
        tableModel.setRowCount(0);
        detailsArea.setText("");
        statusLabel.setText("Database reset - No comments available");
        totalLabel.setText("Total: 0 comments");
    }

    /**
     * Called when model changes (e.g., after database reset)
     */
    @Override
    public void modelChanged() {
        refreshTable();
    }

}
