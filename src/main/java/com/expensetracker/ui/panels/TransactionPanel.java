package com.expensetracker.ui.panels;

import com.expensetracker.models.Transaction;
import com.expensetracker.services.ExpenseService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;

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
                                                 "Bills", "Healthcare", "Education", "Travel", "Other"};
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
        typeCombo = new JComboBox<>(new String[]{"income", "expense"});
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
        
        SpinnerDateModel dateModel = new SpinnerDateModel();
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
        updateCategoryOptions();
        
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
        
        String[] categories = "income".equals(selectedType) ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
        for (String category : categories) {
            categoryCombo.addItem(category);
        }
    }
    
    private void addTransaction(ActionEvent e) {
        try {
            // Validate input
            if (amountField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an amount", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (descriptionField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a description", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than 0", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create transaction
            Transaction transaction = new Transaction();
            transaction.setType((String) typeCombo.getSelectedItem());
            transaction.setCategory((String) categoryCombo.getSelectedItem());
            transaction.setAmount(amount);
            transaction.setDescription(descriptionField.getText().trim());
            transaction.setNotes(notesField.getText().trim());
            transaction.setPaymentMethod((String) paymentMethodCombo.getSelectedItem());
            
            // Convert date
            java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
            transaction.setDate(utilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            
            // Add to database
            expenseService.addTransaction(transaction);
            
            // Refresh UI
            loadTransactions();
            clearForm();
            refreshCallback.run();
            
            JOptionPane.showMessageDialog(this, "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding transaction: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // TODO: Implement edit functionality
        JOptionPane.showMessageDialog(this, "Edit functionality will be implemented in next version", "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deleteSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this transaction?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Get transaction ID from the first column (hidden)
                // TODO: Implement delete functionality
                JOptionPane.showMessageDialog(this, "Delete functionality will be implemented in next version", "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        typeCombo.setSelectedIndex(0);
        updateCategoryOptions();
        amountField.setText("");
        descriptionField.setText("");
        notesField.setText("");
        paymentMethodCombo.setSelectedIndex(0);
        dateSpinner.setValue(new java.util.Date());
    }
    
    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }
    
    private void loadTransactions() {
        try {
            List<Transaction> transactions = expenseService.getAllTransactions();
            
            tableModel.setRowCount(0);
            
            for (Transaction transaction : transactions) {
                Object[] row = {
                    transaction.getFormattedDate(),
                    transaction.getType().toUpperCase(),
                    transaction.getCategory(),
                    transaction.getDescription(),
                    String.format("₹%.2f", transaction.getAmount()),
                    transaction.getPaymentMethod()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
