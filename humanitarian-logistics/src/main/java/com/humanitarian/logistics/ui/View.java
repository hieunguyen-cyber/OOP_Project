package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * View component of MVC pattern - Main GUI with comprehensive tabbed interface.
 * Includes: Crawler Control, Data Collection, Problem 1 & 2 Analysis.
 */
public class View extends JFrame implements ModelListener {
    private Model model;
    private JTabbedPane mainTabbedPane;
    private CrawlControlPanel crawlPanel;
    private DataCollectionPanel dataCollectionPanel;
    private AdvancedAnalysisPanel advancedAnalysisPanel;
    private CommentManagementPanel commentPanel;
    private DisasterManagementPanel disasterPanel;
    private JLabel statusLabel;

    public View(Model model) {
        this.model = model;
        model.addModelListener(this);
        initializeUI();
    }

    private void initializeUI() {
        setTitle("🚨 Humanitarian Logistics Analysis System v2.0");
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

        // Tab 1: Web Crawler Control
        crawlPanel = new CrawlControlPanel(model);
        mainTabbedPane.addTab("🌐 Web Crawler", crawlPanel);

        // Tab 2: Manual Data Entry
        dataCollectionPanel = new DataCollectionPanel(model);
        mainTabbedPane.addTab("✏️  Data Entry", dataCollectionPanel);

        // Tab 3: Comments Management
        commentPanel = new CommentManagementPanel(model);
        mainTabbedPane.addTab("💬 Comments Manager", commentPanel);

        // Tab 4: Disaster Management
        disasterPanel = new DisasterManagementPanel(model);
        mainTabbedPane.addTab("⚠️ Disaster Management", disasterPanel);

        // Tab 5: Advanced Analysis
        advancedAnalysisPanel = new AdvancedAnalysisPanel(model);
        mainTabbedPane.addTab("📊 Analysis", advancedAnalysisPanel);

        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        this.add(mainPanel);
        this.setVisible(true);
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Humanitarian Logistics Analysis System - Problem 1 & 2 Solutions");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Complete web crawler + manual data entry + multi-dimensional analysis");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 200, 200));

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.setOpaque(false);
        labelPanel.add(titleLabel);
        labelPanel.add(subtitleLabel);

        panel.add(labelPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("System Status"));
        panel.setBackground(Color.WHITE);

        statusLabel = new JLabel("Ready - Select a tab to start");
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
            System.out.println("Saving data before exit...");
            
            // Save persisted data
            model.savePersistedData();
            
            // Save custom disaster types
            com.humanitarian.logistics.database.DataPersistenceManager persistenceManager = 
                model.getPersistenceManager();
            persistenceManager.saveDisasters(com.humanitarian.logistics.model.DisasterManager.getInstance());
            
            // Suppress any cleanup errors to prevent "Errors during cleaning null"
            try {
                if (crawlPanel != null) {
                    // Let crawler finish gracefully
                }
            } catch (Throwable t) {
                // Silently ignore
            }
            
            try {
                if (commentPanel != null) {
                    // CommentPanel cleanup
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

