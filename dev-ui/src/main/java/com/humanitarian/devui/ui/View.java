package com.humanitarian.devui.ui;

import com.humanitarian.devui.model.*;
import com.humanitarian.devui.database.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * View component of MVC pattern - Main GUI with comprehensive tabbed interface.
 * Dev mode: Data Entry, Comment Management with Save/Cancel system.
 */
public class View extends JFrame implements ModelListener {
    private Model model;
    private SessionDataBuffer dataBuffer;
    private JTabbedPane mainTabbedPane;
    private DataCollectionPanel dataCollectionPanel;
    private CommentManagementPanel commentPanel;
    private CrawlControlPanel crawlPanel;
    private JLabel statusLabel;
    private JButton saveButton;
    private JButton cancelButton;

    public View(Model model) {
        this.model = model;
        this.dataBuffer = new SessionDataBuffer();
        model.addModelListener(this);
        initializeUI();
    }

    private void initializeUI() {
        setTitle("🚀 Dev UI - Curated Data Collection Mode");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 950);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Add proper cleanup on window close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cleanupAndExit();
            }
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Title panel
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Tabbed pane with all major components
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("Arial", Font.BOLD, 12));

        // Tab 0: Web Crawler
        crawlPanel = new CrawlControlPanel(model, dataBuffer);
        mainTabbedPane.addTab("🌐 Crawl Web", crawlPanel);

        // Tab 1: Manual Data Entry
        dataCollectionPanel = new DataCollectionPanel(model, dataBuffer);
        mainTabbedPane.addTab("✏️  Data Entry", dataCollectionPanel);

        // Tab 2: Comment Management (Excel-style)
        commentPanel = new CommentManagementPanel(model, dataBuffer);
        mainTabbedPane.addTab("💬 Comments Manager", commentPanel);

        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);

        // Control panel with Save/Cancel
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.EAST);

        // Status bar
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        this.add(mainPanel);
        this.setVisible(true);
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(244, 67, 54));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("🚀 Dev UI - Curated Data Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Web Crawler (Facebook/YouTube) + Manual data entry + Comment management + Save to humanitarian_logistics_curated.db");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(255, 200, 200));

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.setOpaque(false);
        labelPanel.add(titleLabel);
        labelPanel.add(subtitleLabel);

        panel.add(labelPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Actions"));
        panel.setBackground(new Color(248, 248, 248));
        panel.setPreferredSize(new Dimension(140, 0));

        // Save button
        saveButton = new JButton("💾 SAVE");
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));
        saveButton.setBackground(new Color(34, 139, 34)); // Green
        saveButton.setForeground(Color.WHITE);
        saveButton.setOpaque(true);
        saveButton.setBorderPainted(false);
        saveButton.setMaximumSize(new Dimension(120, 50));
        saveButton.addActionListener(e -> saveToDatabase());
        panel.add(saveButton);
        panel.add(Box.createVerticalStrut(10));

        // Cancel button
        cancelButton = new JButton("❌ CANCEL");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelButton.setBackground(new Color(211, 47, 47)); // Red
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setOpaque(true);
        cancelButton.setBorderPainted(false);
        cancelButton.setMaximumSize(new Dimension(120, 50));
        cancelButton.addActionListener(e -> cancelChanges());
        panel.add(cancelButton);
        panel.add(Box.createVerticalStrut(10));

        // View buffer button
        JButton viewBufferBtn = new JButton("📋 View Buffer");
        viewBufferBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        viewBufferBtn.setMaximumSize(new Dimension(120, 40));
        viewBufferBtn.addActionListener(e -> showBufferInfo());
        panel.add(viewBufferBtn);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("System Status"));
        panel.setBackground(Color.WHITE);

        statusLabel = new JLabel("Ready - Select a tab to start | Buffer: 0 items");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        panel.add(statusLabel, BorderLayout.WEST);

        // Quick info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JLabel postCountLabel = new JLabel("Posts: 0");
        JLabel commentCountLabel = new JLabel("Comments: 0");

        postCountLabel.setFont(new Font("Arial", Font.BOLD, 11));
        commentCountLabel.setFont(new Font("Arial", Font.BOLD, 11));

        infoPanel.add(postCountLabel);
        infoPanel.add(new JSeparator(JSeparator.VERTICAL));
        infoPanel.add(commentCountLabel);

        panel.add(infoPanel, BorderLayout.EAST);

        return panel;
    }

    private void saveToDatabase() {
        int totalItems = dataBuffer.getTotalPosts() + dataBuffer.getTotalComments();
        if (totalItems == 0) {
            JOptionPane.showMessageDialog(this, "Buffer is empty. Nothing to save.", "No Data", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Save " + totalItems + " items to humanitarian_logistics_curated.db?\n" +
            "Posts: " + dataBuffer.getTotalPosts() + " | Comments: " + dataBuffer.getTotalComments(),
            "Confirm Save",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseManager dbManager = new DatabaseManager();
                int duplicates = 0;
                int saved = 0;
                
                // Save posts (check for duplicates)
                for (Post post : dataBuffer.getPendingPosts()) {
                    if (dbManager.isDuplicateLink(post.getPostId())) {
                        duplicates++;
                        statusLabel.setText("⚠️ Found duplicate link, skipping: " + post.getPostId());
                    } else {
                        // Support both YouTubePost and YouTubePost
                        dbManager.savePost(post);
                        saved++;
                    }
                }

                // Save comments from buffer AND from posts
                for (Comment comment : dataBuffer.getPendingComments()) {
                    dbManager.saveComment(comment);
                }
                
                // Also save comments attached to posts
                for (Post post : dataBuffer.getPendingPosts()) {
                    for (Comment comment : post.getComments()) {
                        dbManager.saveComment(comment);
                    }
                }
                
                System.out.println("DEBUG: About to commit...");
                dbManager.commit();
                System.out.println("DEBUG: Commit done");
                dbManager.close();
                System.out.println("DEBUG: Close done");

                dataBuffer.clear();
                
                // Reload data from database to show in UI
                try {
                    DatabaseManager reloadDb = new DatabaseManager();
                    List<Post> savedPosts = reloadDb.getAllPosts();
                    System.out.println("DEBUG: Reloaded " + savedPosts.size() + " posts");
                    for (Post post : savedPosts) {
                        model.addPost(post);
                    }
                    reloadDb.close();
                } catch (Exception e) {
                    System.err.println("Error reloading data: " + e.getMessage());
                }
                
                statusLabel.setText("✓ Data saved successfully | Saved: " + saved + " posts, Duplicates: " + duplicates + " | Buffer: 0 items");
                commentPanel.refreshTable();
                String msg = "✓ " + totalItems + " items processed\n" +
                            "Saved: " + saved + " posts\n" +
                            "Duplicates (skipped): " + duplicates;
                JOptionPane.showMessageDialog(this, msg, "Save Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving to database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("✗ Error saving data: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void cancelChanges() {
        int totalItems = dataBuffer.getTotalPosts() + dataBuffer.getTotalComments();
        if (totalItems == 0) {
            JOptionPane.showMessageDialog(this, "Buffer is already empty.", "No Data", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Discard " + totalItems + " unsaved items in buffer?\n" +
            "This action cannot be undone.",
            "Confirm Cancel",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dataBuffer.clear();
            statusLabel.setText("✓ Buffer cleared | Buffer: 0 items");
            commentPanel.refreshTable();
            JOptionPane.showMessageDialog(this, "✓ Buffer cleared", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showBufferInfo() {
        int total = dataBuffer.getTotalPosts() + dataBuffer.getTotalComments();
        String info = "📊 Session Buffer Status\n\n" +
                      "Posts (pending): " + dataBuffer.getTotalPosts() + "\n" +
                      "Comments (pending): " + dataBuffer.getTotalComments() + "\n" +
                      "Total items: " + total + "\n\n" +
                      "Status: " + (dataBuffer.isDirty() ? "Modified ✏️" : "Saved ✓");
        
        JOptionPane.showMessageDialog(this, info, "Buffer Info", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void modelChanged() {
        SwingUtilities.invokeLater(() -> {
            List<Post> posts = model.getPosts();
            int totalComments = posts.stream().mapToInt(p -> p.getComments().size()).sum();
            statusLabel.setText("✓ Model updated - Posts: " + posts.size() + " | Comments: " + totalComments);
        });
    }

    private void cleanupAndExit() {
        try {
            System.out.println("Cleaning up resources...");
            
            // Save persisted data before exit
            try {
                model.savePersistedData();
                model.getPersistenceManager().saveDisasters(DisasterManager.getInstance());
                System.out.println("✓ Data saved successfully");
            } catch (Exception e) {
                System.err.println("Warning: Could not save data: " + e.getMessage());
            }
            
            // Suppress any cleanup errors to prevent "Errors during cleaning null"
            try {
                if (commentPanel != null) {
                    // CommentPanel cleanup
                }
                if (crawlPanel != null) {
                    // CrawlPanel cleanup
                }
            } catch (Throwable t) {
                // Silently ignore
            }
            
            System.out.println("✓ Cleanup complete. Exiting...");
        } catch (Throwable t) {
            System.err.println("Error during cleanup: " + t.getMessage());
        } finally {
            System.exit(0);
        }
    }
}

