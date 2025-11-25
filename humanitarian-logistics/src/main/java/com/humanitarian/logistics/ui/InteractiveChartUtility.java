package com.humanitarian.logistics.ui;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.entity.ChartEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Utility for adding interactive features to JFreeChart ChartPanels.
 * Provides enhanced tooltips, zoom, and pan capabilities.
 */
public class InteractiveChartUtility {

    /**
     * Add enhanced interactive features to a ChartPanel
     */
    public static void makeChartInteractive(ChartPanel chartPanel) {
        // Only enable features if chart is not null
        if (chartPanel.getChart() != null) {
            chartPanel.setDomainZoomable(true);
            chartPanel.setRangeZoomable(true);
            chartPanel.setMouseWheelEnabled(true);
        }
        
        // Enable better tooltips
        chartPanel.setDisplayToolTips(true);
        
        // Add enhanced mouse listener for tooltips
        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                // On click, show detailed info
                ChartEntity entity = event.getEntity();
                if (entity != null) {
                    System.out.println("Clicked: " + entity.getToolTipText());
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                // Hover feedback is handled by JFreeChart's built-in tooltip
            }
        });
        
        // Set chart panel background and borders for better UI
        chartPanel.setBackground(new Color(245, 245, 245));
        chartPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
    }
    
    /**
     * Enable interactivity on a ChartPanel with an existing chart
     */
    public static void enableChartInteractivity(ChartPanel chartPanel) {
        if (chartPanel != null && chartPanel.getChart() != null) {
            chartPanel.setDomainZoomable(true);
            chartPanel.setRangeZoomable(true);
            chartPanel.setMouseWheelEnabled(true);
        }
    }

    /**
     * Create builder for fluent configuration
     */
    public static ChartPanelBuilder builder(ChartPanel chartPanel) {
        return new ChartPanelBuilder(chartPanel);
    }

    /**
     * Fluent builder for ChartPanel configuration
     */
    public static class ChartPanelBuilder {
        private ChartPanel chartPanel;
        private boolean domainZoom = true;
        private boolean rangeZoom = true;
        private boolean mouseWheel = true;
        private boolean tooltips = true;

        public ChartPanelBuilder(ChartPanel chartPanel) {
            this.chartPanel = chartPanel;
        }

        public ChartPanelBuilder domainZoomable(boolean enabled) {
            this.domainZoom = enabled;
            return this;
        }

        public ChartPanelBuilder rangeZoomable(boolean enabled) {
            this.rangeZoom = enabled;
            return this;
        }

        public ChartPanelBuilder mouseWheelEnabled(boolean enabled) {
            this.mouseWheel = enabled;
            return this;
        }

        public ChartPanelBuilder tooltipsEnabled(boolean enabled) {
            this.tooltips = enabled;
            return this;
        }

        public ChartPanel build() {
            chartPanel.setDomainZoomable(domainZoom);
            chartPanel.setRangeZoomable(rangeZoom);
            chartPanel.setMouseWheelEnabled(mouseWheel);
            chartPanel.setDisplayToolTips(tooltips);
            return chartPanel;
        }
    }

    /**
     * Add right-click context menu support
     */
    public static void addContextMenuSupport(ChartPanel chartPanel) {
        chartPanel.setPopupMenu(null); // Disable default popup to allow custom
        
        chartPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    // Right-click detected
                    JPopupMenu menu = createContextMenu(chartPanel);
                    menu.show(chartPanel, e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {
                chartPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                chartPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private static JPopupMenu createContextMenu(ChartPanel chartPanel) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem zoomIn = new JMenuItem("Zoom In (100%)");
        zoomIn.addActionListener(e -> chartPanel.zoomInBoth(0.5, 0.5));
        
        JMenuItem zoomOut = new JMenuItem("Zoom Out");
        zoomOut.addActionListener(e -> chartPanel.zoomOutBoth(0.5, 0.5));
        
        JMenuItem resetZoom = new JMenuItem("Reset Zoom");
        resetZoom.addActionListener(e -> chartPanel.restoreAutoBounds());
        
        JMenuItem saveChart = new JMenuItem("Save Chart as PNG");
        saveChart.addActionListener(e -> {
            try {
                javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
                if (chooser.showSaveDialog(chartPanel) == javax.swing.JFileChooser.APPROVE_OPTION) {
                    String filename = chooser.getSelectedFile().getPath();
                    if (!filename.toLowerCase().endsWith(".png")) {
                        filename += ".png";
                    }
                    org.jfree.chart.ChartUtils.saveChartAsPNG(
                        new java.io.File(filename),
                        chartPanel.getChart(),
                        chartPanel.getWidth(),
                        chartPanel.getHeight()
                    );
                }
            } catch (Exception ex) {
                System.err.println("Error saving chart: " + ex.getMessage());
            }
        });
        
        menu.add(zoomIn);
        menu.add(zoomOut);
        menu.add(resetZoom);
        menu.addSeparator();
        menu.add(saveChart);
        
        return menu;
    }

    /**
     * Enhanced tooltip rendering with additional information
     */
    public static void enhanceTooltips(ChartPanel chartPanel, java.util.function.Function<ChartEntity, String> tooltipProvider) {
        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                ChartEntity entity = event.getEntity();
                if (entity != null && tooltipProvider != null) {
                    String tooltip = tooltipProvider.apply(entity);
                    if (tooltip != null) {
                        System.out.println("Enhanced Tooltip: " + tooltip);
                    }
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                // Enhanced tooltip on hover
            }
        });
    }
}
