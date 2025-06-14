package com.expensetracker.ui.panels;

import com.expensetracker.models.Budget;
import com.expensetracker.services.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;
import javax.swing.Timer; // Import Timer for auto-refresh
import javax.swing.SwingUtilities; // To ensure UI updates are on the correct thread

/**
 * Enhanced panel for managing budgets with progress tracking
 */
public class BudgetPanel extends JPanel {
    private ExpenseService expenseService;
    private Runnable refreshCallback;
    
    private JPanel budgetListPanel;
    private JTextField categoryField;
    private JTextField limitField;
    private JComboBox<String> periodCombo;
    private Timer refreshTimer; // Timer for auto-refreshing budget data
    
    // Predefined categories that match transaction categories
    private final String[] EXPENSE_CATEGORIES = {
        "Food", "Transport", "Entertainment", "Shopping", 
        "Bills", "Healthcare", "Education", "Groceries", "Utilities"
    };
    
    public BudgetPanel(ExpenseService expenseService, Runnable refreshCallback) {
        this.expenseService = expenseService;
        this.refreshCallback = refreshCallback;
        initializeUI();
        loadBudgets();
        startAutoRefresh(); // Start the auto-refresh mechanism
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
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(Color.WHITE);
        JLabel infoLabel = new JLabel("<html><b>Note:</b> Budget amounts increase when you add transactions with matching categories</html>");
        infoLabel.setForeground(new Color(59, 130, 246));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoPanel.add(infoLabel);
        
        // Main content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createBudgetList());
        splitPane.setRightComponent(createBudgetForm());
        splitPane.setDividerLocation(650);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
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
        scrollPane.setPreferredSize(new Dimension(600, 400));
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
        
        // Category dropdown instead of text field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox<String> categoryCombo = new JComboBox<>(EXPENSE_CATEGORIES);
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(categoryCombo, gbc);
        
        // Limit Amount
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Limit Amount:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        limitField = new JTextField(15);
        limitField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(limitField, gbc);
        
        // Period
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Period:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        periodCombo = new JComboBox<>(new String[]{"monthly", "weekly", "yearly"});
        periodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(periodCombo, gbc);
        
        // Button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 10, 10, 10);
        
        JButton addButton = new JButton("Add Budget");
        addButton.setBackground(new Color(34, 197, 94));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.addActionListener(e -> addBudget((String) categoryCombo.getSelectedItem()));
        formPanel.add(addButton, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void addBudget(String category) {
        try {
            String limitText = limitField.getText().trim();
            
            if (limitText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a limit amount", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double limit = Double.parseDouble(limitText);
            if (limit <= 0) {
                JOptionPane.showMessageDialog(this, "Limit must be greater than 0", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String period = (String) periodCombo.getSelectedItem();
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = calculateEndDate(startDate, period);
            
            Budget budget = new Budget(category, limit, startDate, endDate, period);
            expenseService.addBudget(budget);
            
            loadBudgets(); // Refresh this panel's list
            clearForm();
            refreshCallback.run(); // Notify MainFrame or other panels
            
            JOptionPane.showMessageDialog(this, "Budget added successfully!\n\nNote: Spending will increase when you add expense transactions with '" + category + "' category.", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding budget: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private LocalDate calculateEndDate(LocalDate startDate, String period) {
        switch (period) {
            case "weekly":
                return startDate.plusWeeks(1);
            case "monthly":
                return startDate.plusMonths(1);
            case "yearly":
                return startDate.plusYears(1);
            default:
                return startDate.plusMonths(1);
        }
    }
    
    private void clearForm() {
        limitField.setText("");
        periodCombo.setSelectedIndex(0);
    }
    
    /**
     * Loads/reloads budgets from the service and updates the UI.
     * Made public to allow external refresh calls and is now used by an internal timer.
     */
    public void loadBudgets() {
        try {
            budgetListPanel.removeAll();
            
            List<Budget> budgets = expenseService.getAllBudgets();
            
            if (budgets.isEmpty()) {
                JLabel emptyLabel = new JLabel("<html><div style='text-align: center;'>" +
                        "No budgets created yet<br>" +
                        "<small>Create budgets to track your spending limits</small></div></html>");
                emptyLabel.setForeground(new Color(107, 114, 128));
                emptyLabel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
                emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
                budgetListPanel.add(emptyLabel);
            } else {
                for (Budget budget : budgets) {
                    budgetListPanel.add(createBudgetItem(budget));
                    budgetListPanel.add(Box.createVerticalStrut(10)); // Add spacing
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
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // Left panel - Budget info
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Category name
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel categoryLabel = new JLabel(budget.getCategory());
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        infoPanel.add(categoryLabel, gbc);
        
        // Period info
        gbc.gridy = 1;
        JLabel periodLabel = new JLabel(budget.getPeriod().toUpperCase() + " BUDGET");
        periodLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        periodLabel.setForeground(new Color(107, 114, 128));
        infoPanel.add(periodLabel, gbc);
        
        // Amount info
        gbc.gridy = 2;
        double remaining = budget.getRemainingAmount();
        String amountText = String.format("₹%.0f spent of ₹%.0f", budget.getSpentAmount(), budget.getLimitAmount());
        if (remaining < 0) {
            amountText += String.format(" (₹%.0f over budget)", Math.abs(remaining));
        } else {
            amountText += String.format(" (₹%.0f remaining)", remaining);
        }
        
        JLabel amountLabel = new JLabel(amountText);
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        amountLabel.setForeground(budget.isOverBudget() ? new Color(239, 68, 68) : new Color(107, 114, 128));
        infoPanel.add(amountLabel, gbc);
        
        // Center panel - Progress bar
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBackground(Color.WHITE);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) Math.min(budget.getPercentageUsed(), 100));
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%.1f%%", budget.getPercentageUsed()));
        progressBar.setPreferredSize(new Dimension(200, 25));
        
        Color progressColor = budget.isOverBudget() ? new Color(239, 68, 68) : 
                             budget.getPercentageUsed() > 80 ? new Color(245, 158, 11) : 
                             new Color(34, 197, 94);
        progressBar.setForeground(progressColor);
        
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        // Right panel - Action buttons with better visibility
        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setBackground(Color.WHITE);
        GridBagConstraints actionGbc = new GridBagConstraints();
        actionGbc.insets = new Insets(3, 3, 3, 3);
        actionGbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Edit button with improved styling
        actionGbc.gridx = 0; actionGbc.gridy = 0;
        JButton editButton = createStyledButton("Edit Limit", new Color(59, 130, 246));
        editButton.addActionListener(e -> editBudgetLimit(budget));
        actionPanel.add(editButton, actionGbc);
        
        // Delete button with improved styling
        actionGbc.gridy = 1;
        JButton deleteButton = createStyledButton("Delete", new Color(239, 68, 68));
        deleteButton.addActionListener(e -> deleteBudget(budget));
        actionPanel.add(deleteButton, actionGbc);
        
        // Reset button with improved styling
        actionGbc.gridy = 2;
        JButton resetButton = createStyledButton("Reset", new Color(107, 114, 128));
        resetButton.addActionListener(e -> resetBudgetSpending(budget));
        actionPanel.add(resetButton, actionGbc);
        
        item.add(infoPanel, BorderLayout.WEST);
        item.add(progressPanel, BorderLayout.CENTER);
        item.add(actionPanel, BorderLayout.EAST);
        
        return item;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(85, 30));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void editBudgetLimit(Budget budget) {
        String newLimitText = JOptionPane.showInputDialog(
                this,
                "Enter new budget limit for " + budget.getCategory() + ":",
                "Edit Budget Limit",
                JOptionPane.QUESTION_MESSAGE
        );
        
        if (newLimitText != null && !newLimitText.trim().isEmpty()) {
            try {
                double newLimit = Double.parseDouble(newLimitText.trim());
                if (newLimit <= 0) {
                    JOptionPane.showMessageDialog(this, "Limit must be greater than 0", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                budget.setLimitAmount(newLimit);
                expenseService.updateBudget(budget);
                loadBudgets(); // Refresh this panel's list
                refreshCallback.run(); // Notify MainFrame or other panels
                
                JOptionPane.showMessageDialog(this, "Budget limit updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error updating budget: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteBudget(Budget budget) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the budget for " + budget.getCategory() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                expenseService.deleteBudget(budget.getId());
                loadBudgets(); // Refresh this panel's list
                refreshCallback.run(); // Notify MainFrame or other panels
                
                JOptionPane.showMessageDialog(this, "Budget deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting budget: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void resetBudgetSpending(Budget budget) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Reset spending amount for " + budget.getCategory() + " to ₹0?",
                "Reset Budget Spending",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                budget.setSpentAmount(0.0);
                expenseService.updateBudget(budget);
                loadBudgets(); // Refresh this panel's list
                refreshCallback.run(); // Notify MainFrame or other panels
                
                JOptionPane.showMessageDialog(this, "Budget spending reset successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error resetting budget: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void startAutoRefresh() {
        // Refresh every 2 seconds (2000 milliseconds) to get the latest budget spending.
        // This ensures that when a transaction is added elsewhere, this panel reflects the change.
        refreshTimer = new Timer(2000, e -> SwingUtilities.invokeLater(this::loadBudgets));
        refreshTimer.setRepeats(true);
        refreshTimer.start();
    }
    
    /**
     * Stops the auto-refresh timer. It's good practice to call this when the component is removed.
     */
    public void stopAutoRefresh() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
    }
}
