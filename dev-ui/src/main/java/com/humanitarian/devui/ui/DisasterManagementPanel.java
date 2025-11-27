package com.humanitarian.devui.ui;

import com.humanitarian.devui.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Panel for managing disaster types and their associated comments.
 * Allows users to add/remove disaster types and manage related data.
 */
public class DisasterManagementPanel extends JPanel {
    private Model model;
    private JTable disasterTable;
    private DefaultTableModel tableModel;
    private JTextField newDisasterField;
    private JTextArea aliasesArea;
    private JLabel statusLabel;

    public DisasterManagementPanel(Model model) {
        this.model = model;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("⚠️ Disaster Type Management"));

        // Left: Disaster list
        JPanel leftPanel = createDisasterListPanel();
        add(leftPanel, BorderLayout.WEST);

        // Center: Actions and info
        JPanel centerPanel = createActionPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Bottom: Status
        statusLabel = new JLabel("Ready to manage disaster types");
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createDisasterListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(400, 0));

        // Title
        panel.add(new JLabel("Available Disaster Types:"), BorderLayout.NORTH);

        // Table with disaster types
        String[] columnNames = {"Disaster Type", "Comment Count"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only
            }
        };
        disasterTable = new JTable(tableModel);
        disasterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        disasterTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        disasterTable.getColumnModel().getColumn(1).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(disasterTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Delete button
        JButton deleteButton = new JButton("🗑️ Delete Selected Disaster");
        deleteButton.setFont(new Font("Arial", Font.BOLD, 11));
        deleteButton.setBackground(new Color(211, 47, 47));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setOpaque(true);
        deleteButton.setBorderPainted(false);
        deleteButton.setMaximumSize(new Dimension(250, 40));
        deleteButton.addActionListener(e -> deleteSelectedDisaster());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Refresh on init
        refreshDisasterList();

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add new disaster section
        JLabel addLabel = new JLabel("Add New Disaster Type:");
        addLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(addLabel);
        panel.add(Box.createVerticalStrut(8));

        panel.add(new JLabel("Disaster Name:"));
        newDisasterField = new JTextField(25);
        panel.add(newDisasterField);
        panel.add(Box.createVerticalStrut(8));

        panel.add(new JLabel("Aliases/Keywords (comma-separated):"));
        aliasesArea = new JTextArea(4, 25);
        aliasesArea.setLineWrap(true);
        aliasesArea.setWrapStyleWord(true);
        JScrollPane aliasScroll = new JScrollPane(aliasesArea);
        panel.add(aliasScroll);
        panel.add(Box.createVerticalStrut(8));

        JButton addButton = new JButton("➕ Add Disaster Type");
        addButton.setFont(new Font("Arial", Font.BOLD, 11));
        addButton.setBackground(new Color(76, 175, 80));
        addButton.setForeground(Color.WHITE);
        addButton.setOpaque(true);
        addButton.setBorderPainted(false);
        addButton.setMaximumSize(new Dimension(200, 40));
        addButton.addActionListener(e -> addNewDisaster());
        panel.add(addButton);

        panel.add(Box.createVerticalStrut(20));

        // Reset button
        JButton resetButton = new JButton("🔄 Reset to Default Disasters");
        resetButton.setFont(new Font("Arial", Font.BOLD, 11));
        resetButton.setBackground(new Color(255, 152, 0));
        resetButton.setForeground(Color.WHITE);
        resetButton.setOpaque(true);
        resetButton.setBorderPainted(false);
        resetButton.setMaximumSize(new Dimension(200, 40));
        resetButton.addActionListener(e -> resetToDefaultDisasters());
        panel.add(resetButton);

        panel.add(Box.createVerticalStrut(20));

        // Info section
        JLabel infoLabel = new JLabel("ℹ️ Information:");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(infoLabel);
        panel.add(Box.createVerticalStrut(8));

        JTextArea infoArea = new JTextArea(8, 25);
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setText("• Disaster types are used to categorize posts and comments\n" +
                        "• When you delete a disaster type, all associated comments will be deleted\n" +
                        "• Aliases help in auto-detection during web crawling\n" +
                        "• Select a disaster type and click 'Delete' to remove it\n" +
                        "• Click 'Reset' to restore default 5 disaster types\n" +
                        "• Deleted data cannot be recovered");
        infoArea.setFont(new Font("Arial", Font.PLAIN, 10));
        JScrollPane infoScroll = new JScrollPane(infoArea);
        panel.add(infoScroll);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void addNewDisaster() {
        String name = newDisasterField.getText().trim();
        String aliases = aliasesArea.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a disaster type name", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DisasterType newDisaster = DisasterManager.getInstance().getOrCreateDisasterType(name);

            if (!aliases.isEmpty()) {
                String[] aliasArray = aliases.split(",");
                for (String alias : aliasArray) {
                    newDisaster.addAlias(alias.trim());
                }
            }

            statusLabel.setText("✓ Disaster type '" + name + "' added successfully");
            newDisasterField.setText("");
            aliasesArea.setText("");
            refreshDisasterList();

            JOptionPane.showMessageDialog(this, "✓ Disaster type added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding disaster type: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("✗ Error: " + e.getMessage());
        }
    }

    private void deleteSelectedDisaster() {
        int selectedRow = disasterTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a disaster type to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String disasterName = (String) tableModel.getValueAt(selectedRow, 0);
        int commentCount = (Integer) tableModel.getValueAt(selectedRow, 1);

        String message = "Delete disaster type '" + disasterName + "'?\n\n" +
                        "This will delete:\n" +
                        "• The disaster type\n" +
                        "• All " + commentCount + " associated comments\n\n" +
                        "This action cannot be undone!";

        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Delete all comments associated with this disaster
                List<Post> posts = model.getPosts();
                int deletedComments = 0;

                for (Post post : posts) {
                    if (post instanceof FacebookPost) {
                        FacebookPost fbPost = (FacebookPost) post;
                        DisasterType postDisaster = fbPost.getDisasterType();
                        
                        // Check if post is tagged with the disaster being deleted
                        if (postDisaster != null && postDisaster.getName().equalsIgnoreCase(disasterName)) {
                            // Delete all comments from this post
                            java.util.List<Comment> comments = fbPost.getComments();
                            int postCommentCount = comments.size();
                            comments.clear();
                            deletedComments += postCommentCount;
                        }
                    }
                }

                // Remove the disaster type
                DisasterManager.getInstance().removeDisasterType(disasterName);

                statusLabel.setText("✓ Deleted '" + disasterName + "' and " + deletedComments + " associated comments");
                refreshDisasterList();

                JOptionPane.showMessageDialog(this,
                    "✓ Disaster type deleted\n" +
                    "Comments deleted: " + deletedComments,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting disaster type: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("✗ Error: " + e.getMessage());
            }
        }
    }

    private void resetToDefaultDisasters() {
        String message = "Reset all disaster types to default (5 types)?\n\n" +
                        "This will:\n• Keep: yagi, matmo, flood, disaster, aid\n" +
                        "• Delete all custom disaster types and their associated comments\n\n" +
                        "This action cannot be undone.";
        int confirm = JOptionPane.showConfirmDialog(this, message, "Reset Disasters", 
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Set<String> defaultDisasters = new HashSet<>(Arrays.asList(
                    "yagi", "matmo", "flood", "disaster", "aid"));
                List<String> allDisasters = new ArrayList<>(DisasterManager.getInstance().getAllDisasterNames());
                
                int deletedCount = 0;
                int deletedComments = 0;
                
                for (String disasterName : allDisasters) {
                    if (!defaultDisasters.contains(disasterName.toLowerCase())) {
                        // Delete associated comments first
                        List<Post> posts = model.getPosts();
                        for (Post post : posts) {
                            if (post instanceof FacebookPost) {
                                FacebookPost fbPost = (FacebookPost) post;
                                List<Comment> comments = fbPost.getComments();
                                List<Comment> toRemove = new ArrayList<>();
                                
                                for (Comment comment : comments) {
                                    if (fbPost.getPageId().contains(disasterName)) {
                                        toRemove.add(comment);
                                        deletedComments++;
                                    }
                                }
                                
                                for (Comment comment : toRemove) {
                                    comments.remove(comment);
                                }
                            }
                        }
                        
                        // Then remove the disaster
                        DisasterManager.getInstance().removeDisasterType(disasterName);
                        deletedCount++;
                    }
                }
                
                // Save the reset state to persistence (only default disasters will be saved)
                model.getPersistenceManager().saveDisasters(DisasterManager.getInstance());
                
                refreshDisasterList();
                statusLabel.setText("✓ Reset to default disasters. Deleted: " + deletedCount + " types, " + deletedComments + " comments");
                
                JOptionPane.showMessageDialog(this, 
                    "Reset complete!\n\nDefault disasters restored.\nDeleted: " + deletedCount + " custom types\nDeleted comments: " + deletedComments,
                    "Reset Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error resetting disasters: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("✗ Error: " + e.getMessage());
            }
        }
    }

    public void refreshDisasterList() {
        tableModel.setRowCount(0); // Clear existing rows

        List<String> disasterNames = DisasterManager.getInstance().getAllDisasterNames();
        List<Post> posts = model.getPosts();

        for (String name : disasterNames) {
            // Count comments associated with this disaster
            int commentCount = 0;
            for (Post post : posts) {
                if (post instanceof FacebookPost) {
                    FacebookPost fbPost = (FacebookPost) post;
                    if (fbPost.getPageId().contains(name)) {
                        commentCount += fbPost.getComments().size();
                    }
                }
            }

            tableModel.addRow(new Object[]{name, commentCount});
        }
    }
}
