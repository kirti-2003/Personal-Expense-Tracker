package com.expensetracker.ui;


import com.expensetracker.services.ExpenseService;
import com.expensetracker.ui.panels.*;

import javax.swing.*;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.*;


/**
 * Main application frame with tabbed interface
 */
public class MainFrame extends JFrame {
    private ExpenseService expenseService;
    private JTabbedPane tabbedPane;
    
    // Panel references
    private DashboardPanel dashboardPanel;
    private TransactionPanel transactionPanel;
    private BudgetPanel budgetPanel;
    private GoalPanel goalPanel;
    private ReportPanel reportPanel;
    
    public MainFrame() {
        this.expenseService = new ExpenseService();
        initializeUI();
        setupEventListeners();
    }
    
    private void initializeUI() {
        setTitle("Personal Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Set modern look and feel
        try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
    e.printStackTrace();
}

        
        // Create main container
        setLayout(new BorderLayout());
        
        // Create header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Initialize panels
        dashboardPanel = new DashboardPanel(expenseService);
        transactionPanel = new TransactionPanel(expenseService, this::refreshDashboard);
        budgetPanel = new BudgetPanel(expenseService, this::refreshDashboard);
        goalPanel = new GoalPanel(expenseService, this::refreshDashboard);
        reportPanel = new ReportPanel(expenseService);
        
        // Add panels to tabs
        tabbedPane.addTab("Dashboard", new ImageIcon(), dashboardPanel, "Overview of your finances");
        tabbedPane.addTab("Transactions", new ImageIcon(), transactionPanel, "Manage income and expenses");
        tabbedPane.addTab("Budgets", new ImageIcon(), budgetPanel, "Set and track budgets");
        tabbedPane.addTab("Goals", new ImageIcon(), goalPanel, "Financial goals and targets");
        tabbedPane.addTab("Reports", new ImageIcon(), reportPanel, "Detailed financial reports");
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(37, 99, 235));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Personal Expense Tracker");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Manage your finances with ease");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(219, 234, 254));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        header.add(titlePanel, BorderLayout.WEST);
        
        return header;
    }
    
    private JPanel createStatusPanel() {
        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        status.setBackground(new Color(248, 250, 252));
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(107, 114, 128));
        
        status.add(statusLabel);
        return status;
    }
    
    private void setupEventListeners() {
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            switch (selectedIndex) {
                case 0: // Dashboard
                    dashboardPanel.refreshData();
                    break;
                case 4: // Reports
                    reportPanel.refreshData();
                    break;
            }
        });
    }
    
    public void refreshDashboard() {
        dashboardPanel.refreshData();
    }
    
    public void switchToTab(int tabIndex) {
        tabbedPane.setSelectedIndex(tabIndex);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Error starting application: " + e.getMessage(),
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}