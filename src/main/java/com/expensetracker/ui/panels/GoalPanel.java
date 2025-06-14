package com.expensetracker.ui.panels;

import com.expensetracker.models.Goal;
import com.expensetracker.services.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing financial goals
 */
public class GoalPanel extends JPanel {
    private ExpenseService expenseService;
    private Runnable refreshCallback;
    
    private JPanel goalListPanel;
    private JTextField nameField;
    private JTextField descriptionField;
    private JTextField targetAmountField;
    private JSpinner targetDateSpinner;
    
    public GoalPanel(ExpenseService expenseService, Runnable refreshCallback) {
        this.expenseService = expenseService;
        this.refreshCallback = refreshCallback;
        initializeUI();
        loadGoals();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Financial Goals");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // Main content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createGoalList());
        splitPane.setRightComponent(createGoalForm());
        splitPane.setDividerLocation(600);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createGoalList() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel listTitle = new JLabel("Your Goals");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        listTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(listTitle, BorderLayout.NORTH);
        
        goalListPanel = new JPanel();
        goalListPanel.setLayout(new BoxLayout(goalListPanel, BoxLayout.Y_AXIS));
        goalListPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(goalListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createGoalForm() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        JLabel formTitle = new JLabel("Add New Goal");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.add(formTitle, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Goal Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField = new JTextField(15);
        formPanel.add(nameField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        descriptionField = new JTextField(15);
        formPanel.add(descriptionField, gbc);
        
        // Target Amount
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Target Amount:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        targetAmountField = new JTextField(15);
        formPanel.add(targetAmountField, gbc);
        
        // Target Date
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Target Date:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        
        SpinnerDateModel dateModel = new SpinnerDateModel();
        targetDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(targetDateSpinner, "yyyy-MM-dd");
        targetDateSpinner.setEditor(dateEditor);
        formPanel.add(targetDateSpinner, gbc);
        
        // Button
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 10, 10, 10);
        
        JButton addButton = createStyledButton("Add Goal", new Color(37, 99, 235), Color.WHITE, new Font("Segoe UI", Font.BOLD, 14));
        addButton.addActionListener(e -> addGoal());
        formPanel.add(addButton, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void addGoal() {
        try {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            String targetAmountText = targetAmountField.getText().trim();
            
            if (name.isEmpty() || targetAmountText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill required fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double targetAmount = Double.parseDouble(targetAmountText);
            if (targetAmount <= 0) {
                JOptionPane.showMessageDialog(this, "Target amount must be greater than 0", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Convert date
            java.util.Date utilDate = (java.util.Date) targetDateSpinner.getValue();
            LocalDate targetDate = utilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            
            Goal goal = new Goal(name, description, targetAmount, targetDate);
            expenseService.addGoal(goal);
            
            loadGoals();
            clearForm();
            refreshCallback.run();
            
            JOptionPane.showMessageDialog(this, "Goal added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding goal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        nameField.setText("");
        descriptionField.setText("");
        targetAmountField.setText("");
        targetDateSpinner.setValue(new java.util.Date());
    }
    
    private void loadGoals() {
        try {
            goalListPanel.removeAll();
            
            List<Goal> goals = expenseService.getAllGoals();
            
            if (goals.isEmpty()) {
                JLabel emptyLabel = new JLabel("No goals created yet");
                emptyLabel.setForeground(new Color(107, 114, 128));
                emptyLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                goalListPanel.add(emptyLabel);
            } else {
                for (Goal goal : goals) {
                    goalListPanel.add(createGoalItem(goal));
                    goalListPanel.add(Box.createVerticalStrut(10));
                }
            }
            
            goalListPanel.revalidate();
            goalListPanel.repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading goals: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createGoalItem(Goal goal) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // Left panel - Goal info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(goal.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel descLabel = new JLabel(goal.getDescription());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(107, 114, 128));
        
        JLabel amountLabel = new JLabel(String.format("₹%.0f / ₹%.0f (%.1f%%)", 
                goal.getCurrentAmount(), goal.getTargetAmount(), goal.getProgress()));
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        amountLabel.setForeground(new Color(107, 114, 128));
        
        JLabel dateLabel = new JLabel("Target: " + goal.getTargetDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateLabel.setForeground(new Color(107, 114, 128));
        
        infoPanel.add(nameLabel);
        infoPanel.add(descLabel);
        infoPanel.add(amountLabel);
        infoPanel.add(dateLabel);
        
        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) goal.getProgress());
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%.1f%% Complete", goal.getProgress()));
        
        Color progressColor = goal.isCompleted() ? new Color(34, 197, 94) : new Color(37, 99, 235);
        progressBar.setForeground(progressColor);
        progressBar.setPreferredSize(new Dimension(200, 20));
        
        // Right panel - Actions
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBackground(Color.WHITE);
        
        if (!goal.isCompleted()) {
            JButton addMoneyButton = createStyledButton("Add Money", new Color(34, 197, 94), Color.WHITE, new Font("Segoe UI", Font.BOLD, 12));
            addMoneyButton.addActionListener(e -> showAddMoneyDialog(goal));
            actionPanel.add(addMoneyButton);
            actionPanel.add(Box.createVerticalStrut(5));
        } else {
            JLabel completedLabel = new JLabel("✓ COMPLETED");
            completedLabel.setForeground(new Color(34, 197, 94));
            completedLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            actionPanel.add(completedLabel);
            actionPanel.add(Box.createVerticalStrut(5));
        }
        
        JButton deleteButton = createStyledButton("Delete", new Color(239, 68, 68), Color.WHITE, new Font("Segoe UI", Font.BOLD, 12));
        deleteButton.addActionListener(e -> deleteGoal(goal));
        actionPanel.add(deleteButton);
        
        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(progressBar, BorderLayout.SOUTH);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        item.add(infoPanel, BorderLayout.WEST);
        item.add(centerPanel, BorderLayout.CENTER);
        item.add(actionPanel, BorderLayout.EAST);
        
        return item;
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color fgColor, Font font) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(font);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

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
    
    private void showAddMoneyDialog(Goal goal) {
        String input = JOptionPane.showInputDialog(
            this,
            "How much money would you like to add to \"" + goal.getName() + "\"?",
            "Add Money to Goal",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(input.trim());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be greater than 0", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Update goal
                goal.setCurrentAmount(goal.getCurrentAmount() + amount);
                if (goal.isCompleted()) {
                    goal.setStatus("completed");
                }
                
                expenseService.updateGoal(goal);
                loadGoals();
                refreshCallback.run();
                
                String message = String.format("₹%.2f added to %s!\nCurrent: ₹%.2f / ₹%.2f", 
                        amount, goal.getName(), goal.getCurrentAmount(), goal.getTargetAmount());
                
                if (goal.isCompleted()) {
                    message += "\n\n🎉 Congratulations! Goal completed!";
                }
                
                JOptionPane.showMessageDialog(this, message, "Money Added", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error updating goal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteGoal(Goal goal) {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the goal \"" + goal.getName() + "\"?",
            "Delete Goal",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                expenseService.deleteGoal(goal.getId());
                loadGoals();
                refreshCallback.run();
                JOptionPane.showMessageDialog(this, "Goal deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting goal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
