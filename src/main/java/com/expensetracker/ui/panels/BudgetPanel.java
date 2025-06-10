package com.expensetracker.ui.panels;

import com.expensetracker.models.Budget;
import com.expensetracker.services.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel for managing budgets
 */
public class BudgetPanel extends JPanel {
    private ExpenseService expenseService;
    private Runnable refreshCallback;
    
    private JPanel budgetListPanel;
    private JTextField categoryField;
    private JTextField limitField;
    private JComboBox<String> periodCombo;
    
    public BudgetPanel(ExpenseService expenseService, Runnable refreshCallback) {
        this.expenseService = expenseService;
        this.refreshCallback = refreshCallback;
        initializeUI();
        loadBudgets();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Budget Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // Main content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createBudgetList());
        splitPane.setRightComponent(createBudgetForm());
        splitPane.setDividerLocation(600);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createBudgetList() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel listTitle = new JLabel("Current Budgets");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        listTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(listTitle, BorderLayout.NORTH);
        
        budgetListPanel = new JPanel();
        budgetListPanel.setLayout(new BoxLayout(budgetListPanel, BoxLayout.Y_AXIS));
        budgetListPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(budgetListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBudgetForm() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        JLabel formTitle = new JLabel("Add New Budget");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.add(formTitle, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Category
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        categoryField = new JTextField(15);
        formPanel.add(categoryField, gbc);
        
        // Limit Amount
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Limit Amount:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        limitField = new JTextField(15);
        formPanel.add(limitField, gbc);
        
        // Period
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Period:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        periodCombo = new JComboBox<>(new String[]{"monthly", "weekly", "yearly"});
        formPanel.add(periodCombo, gbc);
        
        // Button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 10, 10, 10);
        
        JButton addButton = new JButton("Add Budget");
        addButton.setBackground(new Color(34, 197, 94));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.addActionListener(e -> addBudget());
        formPanel.add(addButton, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void addBudget() {
        try {
            String category = categoryField.getText().trim();
            String limitText = limitField.getText().trim();
            
            if (category.isEmpty() || limitText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double limit = Double.parseDouble(limitText);
            if (limit <= 0) {
                JOptionPane.showMessageDialog(this, "Limit must be greater than 0", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String period = (String) periodCombo.getSelectedItem();
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusMonths(1); // Default to 1 month
            
            Budget budget = new Budget(category, limit, startDate, endDate, period);
            expenseService.addBudget(budget);
            
            loadBudgets();
            clearForm();
            refreshCallback.run();
            
            JOptionPane.showMessageDialog(this, "Budget added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding budget: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        categoryField.setText("");
        limitField.setText("");
        periodCombo.setSelectedIndex(0);
    }
    
    private void loadBudgets() {
        try {
            budgetListPanel.removeAll();
            
            List<Budget> budgets = expenseService.getAllBudgets();
            
            if (budgets.isEmpty()) {
                JLabel emptyLabel = new JLabel("No budgets created yet");
                emptyLabel.setForeground(new Color(107, 114, 128));
                emptyLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                budgetListPanel.add(emptyLabel);
            } else {
                for (Budget budget : budgets) {
                    budgetListPanel.add(createBudgetItem(budget));
                }
            }
            
            budgetListPanel.revalidate();
            budgetListPanel.repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading budgets: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createBudgetItem(Budget budget) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        
        JLabel categoryLabel = new JLabel(budget.getCategory());
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel amountLabel = new JLabel(String.format("₹%.0f / ₹%.0f", budget.getSpentAmount(), budget.getLimitAmount()));
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        amountLabel.setForeground(new Color(107, 114, 128));
        
        infoPanel.add(categoryLabel, BorderLayout.NORTH);
        infoPanel.add(amountLabel, BorderLayout.SOUTH);
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) budget.getPercentageUsed());
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%.1f%%", budget.getPercentageUsed()));
        
        Color progressColor = budget.isOverBudget() ? new Color(239, 68, 68) : new Color(34, 197, 94);
        progressBar.setForeground(progressColor);
        
        item.add(infoPanel, BorderLayout.WEST);
        item.add(progressBar, BorderLayout.CENTER);
        
        return item;
    }
}