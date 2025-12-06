package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class View extends JFrame implements ModelListener {
    private Model model;
    private JTabbedPane mainTabbedPane;
    private CrawlControlPanel crawlPanel;
    private DataCollectionPanel dataCollectionPanel;
    private AdvancedAnalysisPanel advancedAnalysisPanel;
    private CommentManagementPanel commentPanel;
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

        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("Arial", Font.BOLD, 12));

        crawlPanel = new CrawlControlPanel(model);
        mainTabbedPane.addTab("🌐 Crawl Web", crawlPanel);

        dataCollectionPanel = new DataCollectionPanel(model);
        mainTabbedPane.addTab("✏️  Data Entry", dataCollectionPanel);

        commentPanel = new CommentManagementPanel(model);
        mainTabbedPane.addTab("💬 Comments Manager", commentPanel);

        advancedAnalysisPanel = new AdvancedAnalysisPanel(model);
        mainTabbedPane.addTab("📊 Analysis", advancedAnalysisPanel);

        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);

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
            
            model.savePersistedData();
            
            com.humanitarian.logistics.database.DataPersistenceManager persistenceManager = 
                model.getPersistenceManager();
            persistenceManager.saveDisasters(com.humanitarian.logistics.model.DisasterManager.getInstance());
            
            try {
                if (commentPanel != null) {

                }
            } catch (Throwable t) {

            }
            
            System.out.println("✓ Cleanup complete. Exiting...");
        } catch (Throwable t) {
            System.err.println("Error during cleanup: " + t.getMessage());
        } finally {
            System.exit(0);
        }
    }
}
