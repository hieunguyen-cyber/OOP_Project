package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.model.*;
import com.humanitarian.logistics.database.DatabaseManager;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel-style table panel for managing comments in user app.
 * Allows users to view, edit, and manage individual comments from humanitarian_logistics_user.db
 */
public class CommentManagementPanel extends JPanel {
    private final Model model;
    private DatabaseManager dbManager;
    private JTable commentTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsArea;
    private JLabel statusLabel;
    private JLabel totalLabel;

    public CommentManagementPanel(Model model) {
        this.model = model;
        try {
            this.dbManager = new DatabaseManager();
        } catch (Exception e) {
            System.err.println("Error initializing DatabaseManager: " + e.getMessage());
        }
        initializeUI();
        refreshTable();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("💬 Comment Management"));

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
                return false;
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
        panel.setBorder(BorderFactory.createTitledBorder("Comment Details"));
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
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        JButton deleteButton = new JButton("🗑️ Delete");
        deleteButton.addActionListener(e -> deleteSelectedComment());
        buttonPanel.add(deleteButton);

        JButton editButton = new JButton("✏️ Edit");
        editButton.addActionListener(e -> editSelectedComment());
        buttonPanel.add(editButton);

        panel.add(buttonPanel, BorderLayout.EAST);

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

        Comment commentToDelete = null;
        int count = 0;
        for (Post post : model.getPosts()) {
            for (Comment c : post.getComments()) {
                if (count == selectedRow) {
                    commentToDelete = c;
                    break;
                }
                count++;
            }
            if (commentToDelete != null) break;
        }

        if (commentToDelete != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete comment from " + commentToDelete.getAuthor() + "?\nThis action will be saved to database.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (dbManager != null) {
                        dbManager.deleteComment(commentToDelete.getCommentId());
                        refreshTable();
                        detailsArea.setText("");
                        statusLabel.setText("✓ Comment deleted and saved to database");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error deleting comment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void editSelectedComment() {
        int selectedRow = commentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a comment to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Comment commentToEdit = null;
        int count = 0;
        for (Post post : model.getPosts()) {
            for (Comment c : post.getComments()) {
                if (count == selectedRow) {
                    commentToEdit = c;
                    break;
                }
                count++;
            }
            if (commentToEdit != null) break;
        }

        if (commentToEdit != null) {
            showEditDialog(commentToEdit);
        }
    }

    private void showEditDialog(Comment comment) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Edit Comment", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Content
        panel.add(new JLabel("Content:"));
        JTextArea contentArea = new JTextArea(5, 50);
        contentArea.setText(comment.getContent());
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(contentArea));
        panel.add(Box.createVerticalStrut(10));

        // Sentiment
        panel.add(new JLabel("Sentiment:"));
        JComboBox<Sentiment.SentimentType> sentimentCombo = new JComboBox<>(Sentiment.SentimentType.values());
        sentimentCombo.setSelectedItem(comment.getSentiment().getType());
        panel.add(sentimentCombo);
        panel.add(Box.createVerticalStrut(10));

        // Confidence
        panel.add(new JLabel("Confidence:"));
        JSpinner confidenceSpinner = new JSpinner(new SpinnerNumberModel(comment.getSentiment().getConfidence(), 0.0, 1.0, 0.1));
        panel.add(confidenceSpinner);
        panel.add(Box.createVerticalGlue());

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> {
            try {
                String newContent = contentArea.getText();
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
                model.updateComment(updatedComment);
                
                // Update in database
                if (dbManager != null) {
                    dbManager.updateComment(updatedComment);
                }
                
                refreshTable();
                dialog.dispose();
                statusLabel.setText("✓ Comment updated and saved to database");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating comment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveBtn);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelBtn);

        panel.add(buttonPanel);

        dialog.add(panel);
        dialog.setVisible(true);
    }

}
