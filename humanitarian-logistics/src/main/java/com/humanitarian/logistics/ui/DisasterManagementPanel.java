package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;

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

        JPanel leftPanel = createDisasterListPanel();
        add(leftPanel, BorderLayout.WEST);

        JPanel centerPanel = createActionPanel();
        add(centerPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("Ready to manage disaster types");
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createDisasterListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(400, 0));

        panel.add(new JLabel("Available Disaster Types:"), BorderLayout.NORTH);

        String[] columnNames = {"Disaster Type", "Comment Count"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        disasterTable = new JTable(tableModel);
        disasterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        disasterTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        disasterTable.getColumnModel().getColumn(1).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(disasterTable);
        panel.add(scrollPane, BorderLayout.CENTER);

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

        refreshDisasterList();

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

                List<Post> posts = model.getPosts();
                int deletedComments = 0;

                for (Post post : posts) {
                    if (post instanceof YouTubePost) {
                        YouTubePost ytPost = (YouTubePost) post;
                        DisasterType postDisaster = ytPost.getDisasterType();
                        
                        if (postDisaster != null && postDisaster.getName().equalsIgnoreCase(disasterName)) {

                            java.util.List<Comment> comments = ytPost.getComments();
                            int postCommentCount = comments.size();
                            comments.clear();
                            deletedComments += postCommentCount;
                        }
                    }
                }

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

    public void refreshDisasterList() {
        tableModel.setRowCount(0);

        List<String> disasterNames = DisasterManager.getInstance().getAllDisasterNames();
        List<Post> posts = model.getPosts();

        for (String name : disasterNames) {

            int commentCount = 0;
            for (Post post : posts) {
                if (post instanceof YouTubePost) {
                    YouTubePost ytPost = (YouTubePost) post;
                    if (ytPost.getChannelId().contains(name)) {
                        commentCount += ytPost.getComments().size();
                    }
                }
            }

            tableModel.addRow(new Object[]{name, commentCount});
        }
    }

    private void resetToDefaultDisasters() {
        String message = "Reset all disaster types to default (5 types)?\n\n" +
                        "This will:\n" +
                        "• Keep: yagi, matmo, flood, disaster, aid\n" +
                        "• Delete: All custom disaster types\n" +
                        "• Delete: All comments associated with custom disasters\n\n" +
                        "This action cannot be undone!";

        int confirm = JOptionPane.showConfirmDialog(this, message, "Reset Disasters", 
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DisasterManager manager = DisasterManager.getInstance();
                Set<String> defaultDisasters = new HashSet<>(Arrays.asList(
                    "yagi", "matmo", "flood", "disaster", "aid"
                ));

                List<String> allDisasters = new ArrayList<>(manager.getAllDisasterNames());
                int removedCount = 0;
                int deletedComments = 0;

                for (String disasterName : allDisasters) {
                    if (!defaultDisasters.contains(disasterName.toLowerCase())) {

                        List<Post> posts = model.getPosts();
                        for (Post post : posts) {
                            if (post instanceof YouTubePost) {
                                YouTubePost ytPost = (YouTubePost) post;
                                java.util.List<Comment> comments = ytPost.getComments();
                                java.util.List<Comment> toRemove = new java.util.ArrayList<>();

                                for (Comment comment : comments) {
                                    if (ytPost.getChannelId().contains(disasterName)) {
                                        toRemove.add(comment);
                                        deletedComments++;
                                    }
                                }

                                for (Comment comment : toRemove) {
                                    comments.remove(comment);
                                }
                            }
                        }

                        manager.removeDisasterType(disasterName);
                        removedCount++;
                    }
                }

                statusLabel.setText("✓ Reset to default disasters | Removed: " + removedCount + 
                                   " custom types, Deleted: " + deletedComments + " comments");
                refreshDisasterList();
                
                model.getPersistenceManager().saveDisasters(DisasterManager.getInstance());

                JOptionPane.showMessageDialog(this,
                    "✓ Reset complete\n" +
                    "Custom disasters removed: " + removedCount + "\n" +
                    "Comments deleted: " + deletedComments,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error resetting disasters: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("✗ Error: " + e.getMessage());
            }
        }
    }
}
