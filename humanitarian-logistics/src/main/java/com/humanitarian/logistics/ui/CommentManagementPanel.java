package com.humanitarian.logistics.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.humanitarian.logistics.database.DatabaseLoader;
import com.humanitarian.logistics.database.DatabaseManager;
import com.humanitarian.logistics.model.Comment;
import com.humanitarian.logistics.model.Post;
import com.humanitarian.logistics.model.ReliefItem;
import com.humanitarian.logistics.model.Sentiment;

public class CommentManagementPanel extends JPanel implements ModelListener {
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
        }
        initializeUI();
        refreshTable();
        
        model.addModelListener(this);
    }
    
    @Override
    public void modelChanged() {
        refreshTable();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("💬 Comment Management"));

        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.NORTH);

        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

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

        String[] columns = {"Comment ID", "Author", "Posted At", "Sentiment", "Category", "Disaster Type", "Content Preview"};
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

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton useOurDatabaseButton = new JButton("📚 Use Our Database");
        useOurDatabaseButton.setFont(new Font("Arial", Font.BOLD, 11));
        useOurDatabaseButton.setBackground(new Color(34, 139, 34));
        useOurDatabaseButton.setForeground(Color.WHITE);
        useOurDatabaseButton.setOpaque(true);
        useOurDatabaseButton.setBorderPainted(false);
        useOurDatabaseButton.setFocusPainted(false);
        useOurDatabaseButton.setContentAreaFilled(true);
        useOurDatabaseButton.setPreferredSize(new Dimension(150, 35));
        useOurDatabaseButton.addActionListener(e -> loadOurDatabase());
        buttonPanel.add(useOurDatabaseButton);

        JButton resetDatabaseButton = new JButton("🔴 Reset Database");
        resetDatabaseButton.setFont(new Font("Arial", Font.BOLD, 11));
        resetDatabaseButton.setBackground(new Color(231, 76, 60));
        resetDatabaseButton.setForeground(Color.WHITE);
        resetDatabaseButton.setOpaque(true);
        resetDatabaseButton.setBorderPainted(false);
        resetDatabaseButton.setFocusPainted(false);
        resetDatabaseButton.setContentAreaFilled(true);
        resetDatabaseButton.setPreferredSize(new Dimension(150, 35));
        resetDatabaseButton.addActionListener(e -> resetDatabase());
        buttonPanel.add(resetDatabaseButton);

        buttonPanel.add(Box.createHorizontalStrut(20));

        JButton deleteButton = new JButton("🗑️ Delete");
        deleteButton.setFont(new Font("Arial", Font.BOLD, 11));
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setOpaque(true);
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setContentAreaFilled(true);
        deleteButton.setPreferredSize(new Dimension(100, 35));
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
        editButton.setBorderPainted(false);
        editButton.setFocusPainted(false);
        editButton.setContentAreaFilled(true);
        editButton.setPreferredSize(new Dimension(100, 35));
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
        
        String category = comment.getReliefItem() != null && comment.getReliefItem().getCategory() != null ?
            comment.getReliefItem().getCategory().getDisplayName() : "N/A";
        
        String disasterType = comment.getDisasterType() != null ? 
            comment.getDisasterType() : "N/A";
        
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
            category,
            disasterType,
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
            details.append("Disaster Type: ").append(comment.getDisasterType() != null ? comment.getDisasterType() : "N/A").append("\n");
            
            if (comment.getSentiment() != null) {
                details.append("Sentiment: ").append(comment.getSentiment().getType()).append("\n");
                details.append("Confidence: ").append(String.format("%.2f", comment.getSentiment().getConfidence())).append("\n");
            } else {
                details.append("Sentiment: [Not analyzed yet]\n");
                details.append("Confidence: N/A\n");
            }
            
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

        if (commentToDelete != null && parentPost != null) {
            String preview = commentToDelete.getContent();
            if (preview.length() > 80) {
                preview = preview.substring(0, 77) + "...";
            }
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete comment from " + commentToDelete.getAuthor() + "?\n\n" + preview,
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {

                    parentPost.removeComment(commentToDelete.getCommentId());
                    
                    if (dbManager != null) {
                        dbManager.deleteComment(commentToDelete.getCommentId());
                    }
                    refreshTable();
                    detailsArea.setText("");
                    JOptionPane.showMessageDialog(this, "✓ Comment deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    statusLabel.setText("✓ Comment deleted and saved");
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

        JLabel contentLabel = new JLabel("Comment Content:");
        contentLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(contentLabel);
        
        JTextArea contentArea = new JTextArea(5, 50);
        contentArea.setText(comment.getContent());
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(new Font("Arial", Font.PLAIN, 10));
        panel.add(new JScrollPane(contentArea));
        panel.add(Box.createVerticalStrut(10));

        JLabel sentimentLabel = new JLabel("Sentiment Type:");
        sentimentLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(sentimentLabel);
        
        JComboBox<Sentiment.SentimentType> sentimentCombo = new JComboBox<>(Sentiment.SentimentType.values());
        if (comment.getSentiment() != null) {
            sentimentCombo.setSelectedItem(comment.getSentiment().getType());
        } else {
            sentimentCombo.setSelectedIndex(0);
        }
        panel.add(sentimentCombo);
        panel.add(Box.createVerticalStrut(10));

        JLabel confidenceLabel = new JLabel("Confidence (0.0 - 1.0):");
        confidenceLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(confidenceLabel);
        
        double confidence = comment.getSentiment() != null ? comment.getSentiment().getConfidence() : 0.0;
        JSpinner confidenceSpinner = new JSpinner(new SpinnerNumberModel(confidence, 0.0, 1.0, 0.1));
        panel.add(confidenceSpinner);
        panel.add(Box.createVerticalStrut(10));

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(categoryLabel);
        
        JComboBox<ReliefItem.Category> categoryCombo = new JComboBox<>(ReliefItem.Category.values());
        ReliefItem.Category currentCategory = comment.getReliefItem() != null ? comment.getReliefItem().getCategory() : ReliefItem.Category.FOOD;
        categoryCombo.setSelectedItem(currentCategory);
        panel.add(categoryCombo);
        panel.add(Box.createVerticalStrut(10));

        JLabel disasterTypeLabel = new JLabel("Disaster Type:");
        disasterTypeLabel.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(disasterTypeLabel);
        
        JComboBox<String> disasterTypeCombo = new JComboBox<>();
        disasterTypeCombo.addItem("All Disasters");
        for (String name : com.humanitarian.logistics.model.DisasterManager.getInstance().getAllDisasterNames()) {
            disasterTypeCombo.addItem(name);
        }
        String currentDisasterType = comment.getDisasterType() != null ? comment.getDisasterType() : "All Disasters";
        disasterTypeCombo.setSelectedItem(currentDisasterType);
        panel.add(disasterTypeCombo);
        panel.add(Box.createVerticalStrut(10));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        JButton saveBtn = new JButton("💾 Save");
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
                ReliefItem.Category newCategory = (ReliefItem.Category) categoryCombo.getSelectedItem();
                String newDisasterType = (String) disasterTypeCombo.getSelectedItem();
                
                Sentiment newSentiment = new Sentiment(newType, newConfidence, newContent);
                Comment updatedComment = new Comment(
                    comment.getCommentId(),
                    comment.getPostId(),
                    newContent,
                    comment.getCreatedAt(),
                    comment.getAuthor()
                );
                updatedComment.setSentiment(newSentiment);
                updatedComment.setDisasterType(newDisasterType);
                if (comment.getReliefItem() != null) {
                    ReliefItem updatedReliefItem = new ReliefItem(
                        newCategory,
                        comment.getReliefItem().getDescription(),
                        comment.getReliefItem().getPriority()
                    );
                    updatedComment.setReliefItem(updatedReliefItem);
                } else {
                    ReliefItem newReliefItem = new ReliefItem(newCategory, "", 3);
                    updatedComment.setReliefItem(newReliefItem);
                }
                
                parentPost.updateComment(updatedComment);
                
                if (dbManager != null) {
                    dbManager.updateComment(updatedComment);
                }
                
                refreshTable();
                dialog.dispose();
                JOptionPane.showMessageDialog(CommentManagementPanel.this, "✓ Comment updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating comment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveBtn);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelBtn);

        panel.add(buttonPanel);

        dialog.add(panel);
        dialog.setVisible(true);
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
                com.humanitarian.logistics.model.DisasterManager.getInstance().getAllDisasterNames()
            );
            
            DatabaseLoader.loadOurDatabase(model);
            
            java.util.Set<String> missingDisasters = new java.util.HashSet<>();
            
            for (Post post : model.getPosts()) {
                String disasterKeyword = post.getDisasterKeyword();
                if (disasterKeyword != null && !disasterKeyword.isEmpty()) {
                    String normalizedDisaster = com.humanitarian.logistics.model.DisasterType.normalize(disasterKeyword);
                    
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
                com.humanitarian.logistics.model.DisasterManager.getInstance().getOrCreateDisasterType(disaster);
                addedCount++;
            }
            
            java.util.List<Post> posts = model.getPosts();
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
                    String disasterType = post.getDisasterKeyword();
                    if (disasterType == null || disasterType.isEmpty()) {
                        disasterType = "N/A";
                    }
                    for (Comment comment : post.getComments()) {
                        comment.setDisasterType(disasterType);
                        dbMgr.saveComment(comment);
                    }
                }
                loadMsg += "\n✓ Data saved to database";
            } catch (Exception dbEx) {
                System.err.println("Warning: Data not saved to database: " + dbEx.getMessage());
            }
            
            JOptionPane.showMessageDialog(this, loadMsg, "Database Loaded", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("✗ Error: " + e.getMessage());
        }
    }

    private void resetDatabase() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "This will delete the entire database file and create a new empty one.\nAll data will be lost. Continue?",
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
                File projectRoot = new File(currentDir);
                while (projectRoot != null && !projectRoot.getName().equals("OOP_Project")) {
                    projectRoot = projectRoot.getParentFile();
                }
                
                String basePath;
                if (projectRoot != null) {
                    basePath = projectRoot.getAbsolutePath() + "/humanitarian-logistics/data";
                } else {
                    if (currentDir.endsWith("humanitarian-logistics")) {
                        basePath = currentDir + "/data";
                    } else {
                        basePath = currentDir + "/humanitarian-logistics/data";
                    }
                }
                
                File dataDir = new File(basePath);
                if (!dataDir.exists()) {
                    dataDir.mkdirs();
                }
                
                String dbPath = basePath + "/humanitarian_logistics_user.db";
                File dbFile = new File(dbPath);
                
                if (dbFile.exists()) {
                    if (!dbFile.delete()) {
                        throw new Exception("Failed to delete old database file");
                    }
                    System.out.println("Deleted main DB file: " + dbPath);
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
                
                Thread.sleep(200);

                try {
                    Class.forName("org.sqlite.JDBC");
                    java.sql.Connection conn = java.sql.DriverManager.getConnection(
                        "jdbc:sqlite:" + dbPath
                    );
                    conn.setAutoCommit(false);
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        
                        stmt.execute("PRAGMA foreign_keys = ON");
                        
                        stmt.execute("CREATE TABLE IF NOT EXISTS posts (" +
                            "post_id INTEGER PRIMARY KEY, " +
                            "title TEXT NOT NULL, " +
                            "content TEXT NOT NULL, " +
                            "author TEXT, " +
                            "posted_at TEXT, " +
                            "source TEXT" +
                            ")");
                        
                        stmt.execute("CREATE TABLE IF NOT EXISTS comments (" +
                            "comment_id INTEGER PRIMARY KEY, " +
                            "post_id INTEGER NOT NULL, " +
                            "content TEXT NOT NULL, " +
                            "author TEXT, " +
                            "created_at TEXT, " +
                            "sentiment_type TEXT, " +
                            "sentiment_confidence REAL, " +
                            "relief_category TEXT, " +
                            "FOREIGN KEY(post_id) REFERENCES posts(post_id) ON DELETE CASCADE" +
                            ")");
                        
                        conn.commit();
                        statusLabel.setText("✓ Database reset successfully");
                        detailsArea.setText("Database has been reset.\n" +
                            "✓ Old file deleted: " + dbPath + "\n" +
                            "✓ WAL/SHM/Journal files cleaned\n" +
                            "✓ New empty database created with fresh schema\n");
                        
                        model.clearPosts();
                        
                        model.resetDatabaseConnection();
                        
                        JOptionPane.showMessageDialog(
                            this,
                            "✓ Database reset successfully!\n\nOld file deleted and new empty database created.",
                            "Reset Complete",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        refreshTable();
                    }
                    conn.close();
                } catch (Exception dbEx) {
                    throw new Exception("Failed to create new database: " + dbEx.getMessage());
                }
            } catch (Exception ex) {
                statusLabel.setText("❌ Error during reset: " + ex.getMessage());
                JOptionPane.showMessageDialog(
                    this,
                    "Error during database reset: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void checkAndCollectMissingDisasters(String content, 
                                                   java.util.Set<String> userDisasters, 
                                                   java.util.Set<String> missingDisasters) {
        com.humanitarian.logistics.model.DisasterManager disasterManager = com.humanitarian.logistics.model.DisasterManager.getInstance();
        
        for (com.humanitarian.logistics.model.DisasterType disaster : disasterManager.getAllDisasterTypes()) {
            String disasterName = disaster.getName();
            
            if (disaster.getAliases().stream().anyMatch(content::contains)) {
                if (!userDisasters.contains(disasterName)) {
                    missingDisasters.add(disasterName);
                }
            }
        }
    }

}
