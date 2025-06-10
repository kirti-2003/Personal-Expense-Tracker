package com.expensetracker.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Enhanced Transaction model with more fields and better validation
 */
public class Transaction {
    private String id;
    private String type; // "income" or "expense"
    private String category;
    private double amount;
    private String description;
    private LocalDate date;
    private String notes;
    private String paymentMethod;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Constructors
    public Transaction() {}
    
    public Transaction(String type, String category, double amount, String description, 
                      LocalDate date, String notes, String paymentMethod) {
        this.type = type.toLowerCase();
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.notes = notes;
        this.paymentMethod = paymentMethod;
    }
    
    public Transaction(String id, String type, String category, double amount, 
                      String description, LocalDate date, String notes, String paymentMethod) {
        this(type, category, amount, description, date, notes, paymentMethod);
        this.id = id;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type.toLowerCase(); }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getFormattedDate() {
        return date != null ? date.format(DATE_FORMAT) : "";
    }
    
    @Override
    public String toString() {
        return String.format("Transaction{id='%s', type='%s', category='%s', amount=%.2f, description='%s', date=%s}",
                id, type, category, amount, description, getFormattedDate());
    }
}