package com.expensetracker.ui.panels;

import com.expensetracker.models.Transaction;
import com.expensetracker.services.ExpenseService;
import com.expensetracker.models.Budget;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Panel for managing transactions
 */
public class TransactionPanel extends JPanel {
    private ExpenseService expenseService;
    private Runnable refreshCallback;
    
    // UI Components
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    
    // Form components
    private JComboBox<String> typeCombo;
    private JComboBox<String> categoryCombo;
    private JTextField amountField;
    private JTextField descriptionField;
    private JTextField notesField;
    private JComboBox<String> paymentMethodCombo;
    private JSpinner dateSpinner;
    
    private final String[] INCOME_CATEGORIES = {"Salary", "Freelance", "Investment", "Gift", "Other"};
    private final String[] EXPENSE_CATEGORIES = {"Food", "Transport", "Entertainment", "Shopping", 
                                                 "Bills", "Healthcare", "Education", "Travel", "Groceries", "Utilities", "Other"};
    private final String[] PAYMENT_METHODS = {"Cash", "Credit Card", "Debit Card", "Bank Transfer", 
                                              "Digital Wallet", "Other"};
    
    public TransactionPanel(ExpenseService expenseService, Runnable refreshCallback) {
        this.expenseService = expenseService;
        this.refreshCallback = refreshCallback;
        initializeUI();
        loadTransactions();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create main content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createTransactionList());
        splitPane.setRightComponent(createTransactionForm());
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.6);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createTransactionList() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("Transactions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Search field
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        
        headerPanel.add(searchPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Transaction table
        String[] columnNames = {"Date", "Type", "Category", "Description", "Amount", "Payment Method"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        transactionTable = new JTable(tableModel);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transactionTable.setRowHeight(25);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        sorter = new TableRowSorter<>(tableModel);
        transactionTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton editButton = createButton("Edit", new Color(37, 99, 235));
        editButton.addActionListener(e -> editSelectedTransaction());
        
        JButton deleteButton = createButton("Delete", new Color(239, 68, 68));
        deleteButton.addActionListener(e -> deleteSelectedTransaction());
        
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTransactionForm() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        JLabel titleLabel = new JLabel("Add Transaction");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Type
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        typeCombo = new JComboBox<>(new String[]{"expense", "income"}); // expense first
        typeCombo.addActionListener(e -> updateCategoryOptions());
        formPanel.add(typeCombo, gbc);
        
        // Category
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        categoryCombo = new JComboBox<>();
        formPanel.add(categoryCombo, gbc);
        
        // Amount
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        amountField = new JTextField();
        formPanel.add(amountField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        descriptionField = new JTextField();
        formPanel.add(descriptionField, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        notesField = new JTextField();
        formPanel.add(notesField, gbc);
        
        // Payment Method
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Payment Method:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        paymentMethodCombo = new JComboBox<>(PAYMENT_METHODS);
        formPanel.add(paymentMethodCombo, gbc);
        
        // Date
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        
        java.util.Date today = new java.util.Date();
        // Prevent selecting dates before today. End date is null (no limit).
        SpinnerDateModel dateModel = new SpinnerDateModel(today, today, null, java.util.Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        formPanel.add(dateSpinner, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 5, 5, 5);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = createButton("Add Transaction", new Color(34, 197, 94));
        addButton.addActionListener(this::addTransaction);
        
        JButton clearButton = createButton("Clear", new Color(107, 114, 128));
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        
        formPanel.add(buttonPanel, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Initialize category options
        updateCategoryOptions(); // Call after typeCombo is set, default to "expense" categories
        
        return panel;
    }
    
    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private void updateCategoryOptions() {
        categoryCombo.removeAllItems();
        String selectedType = (String) typeCombo.getSelectedItem();
        
        String[] categories = "income".equalsIgnoreCase(selectedType) ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
        for (String category : categories) {
            categoryCombo.addItem(category);
        }
    }
    
    private void addTransaction(ActionEvent e) {
        try {
            // Validate input
            if (amountField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (descriptionField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a description.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (categoryCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select a category.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than 0.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String type = (String) typeCombo.getSelectedItem();
            String category = (String) categoryCombo.getSelectedItem();
            java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
            LocalDate transactionDate = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // Check budget limit if it's an expense
            if ("expense".equalsIgnoreCase(type)) {
                ExpenseService.BudgetCheckResult budgetCheck = expenseService.checkBudgetBeforeTransaction(category, amount, transactionDate);
                
                if (budgetCheck.budgetFound && budgetCheck.limitExceeded) {
                    Budget budget = budgetCheck.budget;
                    String message = String.format(
                        "Warning: Budget limit for '%s' will be exceeded!\n" +
                        "Current Spending: ₹%.2f of ₹%.2f limit.\n" +
                        "This transaction of ₹%.2f would bring total spending to ₹%.2f.\n\n" +
                        "Do you want to proceed and add this transaction anyway?",
                        budget.getCategory(),
                        budget.getSpentAmount(),
                        budget.getLimitAmount(),
                        amount,
                        budgetCheck.newPotentialSpentAmount
                    );
                    
                    int choice = JOptionPane.showConfirmDialog(this, message, "Budget Limit Exceeded", 
                                                               JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice == JOptionPane.NO_OPTION) {
                        JOptionPane.showMessageDialog(this, "Transaction cancelled. You can edit the budget limit in the Budget Management section or reduce transaction amount.", "Transaction Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        return; // Stop processing
                    }
                }
            }
            
            // Create transaction
            Transaction transaction = new Transaction();
            transaction.setType(type);
            transaction.setCategory(category);
            transaction.setAmount(amount);
            transaction.setDescription(descriptionField.getText().trim());
            transaction.setNotes(notesField.getText().trim());
            transaction.setPaymentMethod((String) paymentMethodCombo.getSelectedItem());
            transaction.setDate(transactionDate);
            
            // Add to database
            expenseService.addTransaction(transaction);
            
            // Refresh UI
            loadTransactions(); // Reloads transactions in this panel
            clearForm();
            if (refreshCallback != null) {
                refreshCallback.run(); // Triggers refresh in MainFrame for other panels like BudgetPanel
            }
            
            JOptionPane.showMessageDialog(this, "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding transaction: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // For debugging
        }
    }
    
    private void editSelectedTransaction() {
        int selectedRowView = transactionTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // TODO: Implement edit functionality. This would involve:
        // 1. Getting the actual transaction ID (if not directly in table model, may need a hidden column or map).
        // 2. Fetching the transaction.
        // 3. Populating a form/dialog with its details.
        // 4. Saving changes via expenseService.updateTransaction().
        // 5. Handling budget adjustments if amount/category/date changes.
        JOptionPane.showMessageDialog(this, "Edit functionality is not fully implemented yet.", "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deleteSelectedTransaction() {
        int selectedRowView = transactionTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        // Assuming the table model might not directly store transaction IDs.
        // For robust deletion, you'd need to map the view row to a model index and then to a transaction ID.
        // This example will be simplified. A real app would need a more reliable way to get the ID.
        // String transactionId = ... get ID from selected row ...
    
        int choice = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this selected transaction?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                // This is a placeholder. You need a way to get the actual transaction ID.
                // For now, this delete will not work correctly without an ID.
                // Let's assume you have a way to get the ID.
                // For example, if the description is unique for testing:
                // String description = (String) tableModel.getValueAt(transactionTable.convertRowIndexToModel(selectedRowView), 3);
                // List<Transaction> transactions = expenseService.getAllTransactions();
                // String idToDelete = null;
                // for(Transaction t : transactions) {
                //    if(t.getDescription().equals(description)) { // This is not robust!
                //        idToDelete = t.getId();
                //        break;
                //    }
                // }
                // if(idToDelete != null) {
                //    expenseService.deleteTransaction(idToDelete);
                //    loadTransactions();
                //    if (refreshCallback != null) refreshCallback.run();
                //    JOptionPane.showMessageDialog(this, "Transaction deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // } else {
                //    JOptionPane.showMessageDialog(this, "Could not find transaction to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                // }
                 JOptionPane.showMessageDialog(this, "Delete by row selection needs more robust ID mapping. Placeholder for now.", "Info", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting transaction: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        typeCombo.setSelectedIndex(0); // Default to expense
        updateCategoryOptions();
        amountField.setText("");
        descriptionField.setText("");
        notesField.setText("");
        paymentMethodCombo.setSelectedIndex(0);
        dateSpinner.setValue(new java.util.Date()); // Reset to current date
    }
    
    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            // Case-insensitive filter across all columns that are strings
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }
    }
    
    private void loadTransactions() {
        try {
            List<Transaction> transactions = expenseService.getAllTransactions();
            
            tableModel.setRowCount(0); // Clear existing rows
            
            for (Transaction transaction : transactions) {
                Object[] row = {
                    transaction.getFormattedDate(), // Assuming getFormattedDate returns yyyy-MM-dd
                    transaction.getType().toUpperCase(),
                    transaction.getCategory(),
                    transaction.getDescription(),
                    String.format("₹%.2f", transaction.getAmount()), // Ensure currency symbol consistency
                    transaction.getPaymentMethod()
                };
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
