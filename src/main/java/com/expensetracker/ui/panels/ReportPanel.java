package com.expensetracker.ui.panels;

import com.expensetracker.models.Transaction;
import com.expensetracker.services.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Panel for displaying financial reports and analytics
 */
public class ReportPanel extends JPanel {
    private ExpenseService expenseService;
    
    private JPanel summaryPanel;
    private JPanel categoryPanel;
    
    public ReportPanel(ExpenseService expenseService) {
        this.expenseService = expenseService;
        initializeUI();
        refreshData();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Financial Reports");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Summary section
        summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Monthly Summary"));
        
        // Category breakdown
        categoryPanel = new JPanel(); // This panel holds the actual category items
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBackground(Color.WHITE);
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Category Breakdown")); // Border is on the panel that scrolls

        JScrollPane categoryScrollPane = new JScrollPane(categoryPanel);
        categoryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        categoryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // Remove default border of JScrollPane if categoryPanel already has one, or style as needed
        categoryScrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(summaryPanel);
        splitPane.setBottomComponent(categoryScrollPane); // Add the scrollable category panel
        splitPane.setDividerLocation(200); // Adjusted for potentially smaller summary panel height
        splitPane.setResizeWeight(0.3);    // Summary panel gets 30% of resize delta
        splitPane.setOneTouchExpandable(true); // Adds collapse/expand arrows
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Add mainPanel directly, it's no longer wrapped in its own JScrollPane
        add(mainPanel, BorderLayout.CENTER);
    }
    
    public void refreshData() {
        try {
            List<Transaction> transactions = expenseService.getTransactionsForCurrentMonth();
            
            // Update summary
            updateSummary(transactions);
            
            // Update category breakdown
            updateCategoryBreakdown(transactions);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading report data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateSummary(List<Transaction> transactions) {
        summaryPanel.removeAll();
        
        double totalIncome = expenseService.getTotalIncome(transactions);
        double totalExpenses = expenseService.getTotalExpenses(transactions);
        double balance = expenseService.getBalance(transactions);
        
        JPanel summaryCard = new JPanel(new GridLayout(3, 2, 10, 10));
        summaryCard.setBackground(Color.WHITE);
        summaryCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Income
        summaryCard.add(createLabel("Total Income:", Font.BOLD));
        JLabel incomeLabel = createLabel(String.format("₹%.2f", totalIncome), Font.PLAIN);
        incomeLabel.setForeground(new Color(34, 197, 94));
        summaryCard.add(incomeLabel);
        
        // Expenses
        summaryCard.add(createLabel("Total Expenses:", Font.BOLD));
        JLabel expenseLabel = createLabel(String.format("₹%.2f", totalExpenses), Font.PLAIN);
        expenseLabel.setForeground(new Color(239, 68, 68));
        summaryCard.add(expenseLabel);
        
        // Balance
        summaryCard.add(createLabel("Net Balance:", Font.BOLD));
        JLabel balanceLabel = createLabel(String.format("₹%.2f", balance), Font.PLAIN);
        balanceLabel.setForeground(balance >= 0 ? new Color(34, 197, 94) : new Color(239, 68, 68));
        summaryCard.add(balanceLabel);
        
        summaryPanel.add(summaryCard);
        summaryPanel.revalidate();
        summaryPanel.repaint();
    }
    
    private void updateCategoryBreakdown(List<Transaction> transactions) {
        categoryPanel.removeAll();
        
        Map<String, Double> categoryTotals = expenseService.getCategoryTotals(transactions);
        
        if (categoryTotals.isEmpty()) {
            JLabel emptyLabel = new JLabel("No transactions for category breakdown");
            emptyLabel.setForeground(new Color(107, 114, 128));
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            categoryPanel.add(emptyLabel);
        } else {
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                JPanel categoryItem = new JPanel(new BorderLayout());
                categoryItem.setBackground(Color.WHITE);
                categoryItem.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
                
                JLabel categoryLabel = new JLabel(entry.getKey());
                categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                
                JLabel amountLabel = new JLabel(String.format("₹%.2f", entry.getValue()));
                amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                amountLabel.setForeground(new Color(107, 114, 128));
                
                categoryItem.add(categoryLabel, BorderLayout.WEST);
                categoryItem.add(amountLabel, BorderLayout.EAST);
                
                categoryPanel.add(categoryItem);
            }
        }
        
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }
    
    private JLabel createLabel(String text, int style) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, 14));
        return label;
    }
}
