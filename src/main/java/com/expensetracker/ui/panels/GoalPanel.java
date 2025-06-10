package com.expensetracker.ui.panels;

import com.expensetracker.models.Goal;
import com.expensetracker.services.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
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
        
        JLabel listTitle = new JLabel("Current Goals");
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
        
        JButton addButton = new JButton("Add Goal");
        addButton.setBackground(new Color(37, 99, 235));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(goal.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel descLabel = new JLabel(goal.getDescription());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(107, 114, 128));
        
        JLabel amountLabel = new JLabel(String.format("₹%.0f / ₹%.0f", goal.getCurrentAmount(), goal.getTargetAmount()));
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        amountLabel.setForeground(new Color(107, 114, 128));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(nameLabel);
        textPanel.add(descLabel);
        textPanel.add(amountLabel);
        
        infoPanel.add(textPanel, BorderLayout.CENTER);
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) goal.getProgress());
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%.1f%%", goal.getProgress()));
        progressBar.setForeground(new Color(37, 99, 235));
        
        item.add(infoPanel, BorderLayout.WEST);
        item.add(progressBar, BorderLayout.CENTER);
        
        return item;
    }
}