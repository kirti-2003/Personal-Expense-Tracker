package com.expensetracker.models;

import java.time.LocalDate;

/**
 * Financial Goal model
 */
public class Goal {
    private String id;
    private String name;
    private String description;
    private double targetAmount;
    private double currentAmount;
    private LocalDate targetDate;
    private LocalDate createdDate;
    private String status; // "active", "completed", "paused"
    
    public Goal() {}
    
    public Goal(String name, String description, double targetAmount, LocalDate targetDate) {
        this.name = name;
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = 0.0;
        this.targetDate = targetDate;
        this.createdDate = LocalDate.now();
        this.status = "active";
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
    
    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
    
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public double getProgress() {
        return targetAmount > 0 ? (currentAmount / targetAmount) * 100 : 0;
    }
    
    public double getRemainingAmount() {
        return targetAmount - currentAmount;
    }
    
    public boolean isCompleted() {
        return currentAmount >= targetAmount;
    }
}