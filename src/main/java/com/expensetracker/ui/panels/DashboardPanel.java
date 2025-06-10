package com.expensetracker.ui.panels;

import com.expensetracker.models.Transaction;
import com.expensetracker.models.Budget;
import com.expensetracker.models.Goal;
import com.expensetracker.services.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

/**
 * Dashboard panel showing financial overview
 */
public class DashboardPanel extends JPanel {
    private ExpenseService expenseService;
    
    // UI Components
    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JLabel balanceLabel;
    private JPanel summaryCardsPanel;
    private JPanel budgetOverviewPanel;
    private JPanel goalOverviewPanel;
    private JPanel recentTransactionsPanel;
    
    public DashboardPanel(ExpenseService expenseService) {
        this.expenseService = expenseService;
        initializeUI();
        refreshData();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create main scroll pane
        JScrollPane scrollPane = new JScrollPane(createMainContent());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        
        // Summary cards
        mainPanel.add(createSummarySection());
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Budget overview
        mainPanel.add(createBudgetSection());
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Goals overview
        mainPanel.add(createGoalsSection());
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Recent transactions
        mainPanel.add(createRecentTransactionsSection());
        
        return mainPanel;
    }
    
    private JPanel createSummarySection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Financial Overview");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        section.add(titleLabel, BorderLayout.NORTH);
        
        summaryCardsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryCardsPanel.setBackground(Color.WHITE);
        
        // Create summary cards
        totalIncomeLabel = new JLabel("₹0.00");
        totalExpenseLabel = new JLabel("₹0.00");
        balanceLabel = new JLabel("₹0.00");
        
        summaryCardsPanel.add(createSummaryCard("Total Income", totalIncomeLabel, new Color(34, 197, 94)));
        summaryCardsPanel.add(createSummaryCard("Total Expenses", totalExpenseLabel, new Color(239, 68, 68)));
        summaryCardsPanel.add(createSummaryCard("Balance", balanceLabel, new Color(37, 99, 235)));
        
        section.add(summaryCardsPanel, BorderLayout.CENTER);
        return section;
    }
    
    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(107, 114, 128));
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createBudgetSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Budget Overview");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        section.add(titleLabel, BorderLayout.NORTH);
        
        budgetOverviewPanel = new JPanel();
        budgetOverviewPanel.setLayout(new BoxLayout(budgetOverviewPanel, BoxLayout.Y_AXIS));
        budgetOverviewPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(budgetOverviewPanel);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        
        section.add(scrollPane, BorderLayout.CENTER);
        return section;
    }
    
    private JPanel createGoalsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Goals Progress");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        section.add(titleLabel, BorderLayout.NORTH);
        
        goalOverviewPanel = new JPanel();
        goalOverviewPanel.setLayout(new BoxLayout(goalOverviewPanel, BoxLayout.Y_AXIS));
        goalOverviewPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(goalOverviewPanel);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        
        section.add(scrollPane, BorderLayout.CENTER);
        return section;
    }
    
    private JPanel createRecentTransactionsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Recent Transactions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        section.add(titleLabel, BorderLayout.NORTH);
        
        recentTransactionsPanel = new JPanel();
        recentTransactionsPanel.setLayout(new BoxLayout(recentTransactionsPanel, BoxLayout.Y_AXIS));
        recentTransactionsPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(recentTransactionsPanel);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        
        section.add(scrollPane, BorderLayout.CENTER);
        return section;
    }
    
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Refresh financial summary
                List<Transaction> currentMonthTransactions = expenseService.getTransactionsForCurrentMonth();
                
                double totalIncome = expenseService.getTotalIncome(currentMonthTransactions);
                double totalExpenses = expenseService.getTotalExpenses(currentMonthTransactions);
                double balance = expenseService.getBalance(currentMonthTransactions);
                
                totalIncomeLabel.setText(String.format("₹%.2f", totalIncome));
                totalExpenseLabel.setText(String.format("₹%.2f", totalExpenses));
                balanceLabel.setText(String.format("₹%.2f", balance));
                
                // Refresh budget overview
                refreshBudgetOverview();
                
                // Refresh goals overview
                refreshGoalsOverview();
                
                // Refresh recent transactions
                refreshRecentTransactions(currentMonthTransactions);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error refreshing dashboard: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void refreshBudgetOverview() throws Exception {
        budgetOverviewPanel.removeAll();
        
        List<Budget> budgets = expenseService.getAllBudgets();
        if (budgets.isEmpty()) {
            JLabel noBudgetsLabel = new JLabel("No budgets set up yet");
            noBudgetsLabel.setForeground(new Color(107, 114, 128));
            noBudgetsLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            budgetOverviewPanel.add(noBudgetsLabel);
        } else {
            for (Budget budget : budgets) {
                budgetOverviewPanel.add(createBudgetItem(budget));
            }
        }
        
        budgetOverviewPanel.revalidate();
        budgetOverviewPanel.repaint();
    }
    
    private void refreshGoalsOverview() throws Exception {
        goalOverviewPanel.removeAll();
        
        List<Goal> goals = expenseService.getAllGoals();
        if (goals.isEmpty()) {
            JLabel noGoalsLabel = new JLabel("No goals set up yet");
            noGoalsLabel.setForeground(new Color(107, 114, 128));
            noGoalsLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            goalOverviewPanel.add(noGoalsLabel);
        } else {
            for (Goal goal : goals) {
                goalOverviewPanel.add(createGoalItem(goal));
            }
        }
        
        goalOverviewPanel.revalidate();
        goalOverviewPanel.repaint();
    }
    
    private void refreshRecentTransactions(List<Transaction> transactions) {
        recentTransactionsPanel.removeAll();
        
        if (transactions.isEmpty()) {
            JLabel noTransactionsLabel = new JLabel("No transactions yet");
            noTransactionsLabel.setForeground(new Color(107, 114, 128));
            noTransactionsLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            recentTransactionsPanel.add(noTransactionsLabel);
        } else {
            // Show only last 5 transactions
            int count = Math.min(5, transactions.size());
            for (int i = 0; i < count; i++) {
                recentTransactionsPanel.add(createTransactionItem(transactions.get(i)));
            }
        }
        
        recentTransactionsPanel.revalidate();
        recentTransactionsPanel.repaint();
    }
    
    private JPanel createBudgetItem(Budget budget) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel categoryLabel = new JLabel(budget.getCategory());
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) budget.getPercentageUsed());
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("₹%.0f / ₹%.0f", budget.getSpentAmount(), budget.getLimitAmount()));
        
        Color progressColor = budget.isOverBudget() ? new Color(239, 68, 68) : new Color(34, 197, 94);
        progressBar.setForeground(progressColor);
        
        item.add(categoryLabel, BorderLayout.NORTH);
        item.add(progressBar, BorderLayout.CENTER);
        
        return item;
    }
    
    private JPanel createGoalItem(Goal goal) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel nameLabel = new JLabel(goal.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) goal.getProgress());
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("₹%.0f / ₹%.0f", goal.getCurrentAmount(), goal.getTargetAmount()));
        progressBar.setForeground(new Color(37, 99, 235));
        
        item.add(nameLabel, BorderLayout.NORTH);
        item.add(progressBar, BorderLayout.CENTER);
        
        return item;
    }
    
    private JPanel createTransactionItem(Transaction transaction) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        
        JLabel descriptionLabel = new JLabel(transaction.getDescription());
        descriptionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JLabel categoryLabel = new JLabel(transaction.getCategory() + " • " + transaction.getFormattedDate());
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        categoryLabel.setForeground(new Color(107, 114, 128));
        
        leftPanel.add(descriptionLabel, BorderLayout.NORTH);
        leftPanel.add(categoryLabel, BorderLayout.SOUTH);
        
        JLabel amountLabel = new JLabel(String.format("₹%.2f", transaction.getAmount()));
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        amountLabel.setForeground("income".equals(transaction.getType()) ? 
            new Color(34, 197, 94) : new Color(239, 68, 68));
        
        item.add(leftPanel, BorderLayout.CENTER);
        item.add(amountLabel, BorderLayout.EAST);
        
        return item;
    }
}

