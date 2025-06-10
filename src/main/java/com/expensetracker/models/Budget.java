package com.expensetracker.models;

import java.time.LocalDate;

/**
 * Budget model for tracking spending limits
 */
public class Budget {
    private String id;
    private String category;
    private double limitAmount;
    private double spentAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String period; // "monthly", "weekly", "yearly"
    
    public Budget() {}
    
    public Budget(String category, double limitAmount, LocalDate startDate, LocalDate endDate, String period) {
        this.category = category;
        this.limitAmount = limitAmount;
        this.spentAmount = 0.0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.period = period;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }
    
    public double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    
    public double getRemainingAmount() {
        return limitAmount - spentAmount;
    }
    
    public double getPercentageUsed() {
        return limitAmount > 0 ? (spentAmount / limitAmount) * 100 : 0;
    }
    
    public boolean isOverBudget() {
        return spentAmount > limitAmount;
    }
}