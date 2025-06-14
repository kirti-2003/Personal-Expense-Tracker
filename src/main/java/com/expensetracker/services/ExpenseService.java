package com.expensetracker.services;

import com.expensetracker.dao.TransactionDAO;
import com.expensetracker.dao.BudgetDAO;
import com.expensetracker.dao.GoalDAO;
import com.expensetracker.models.Transaction;
import com.expensetracker.models.Budget;
import com.expensetracker.models.Goal;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced service layer for business logic
 */
public class ExpenseService {
    private TransactionDAO transactionDAO;
    private BudgetDAO budgetDAO;
    private GoalDAO goalDAO;
    
    public ExpenseService() {
        this.transactionDAO = new TransactionDAO();
        this.budgetDAO = new BudgetDAO();
        this.goalDAO = new GoalDAO();
    }
     public static class BudgetCheckResult {
        public final boolean limitExceeded;
        public final Budget budget; // The relevant budget, if any
        public final double newPotentialSpentAmount;
        public final boolean budgetFound;

        public BudgetCheckResult(boolean limitExceeded, Budget budget, double newPotentialSpentAmount, boolean budgetFound) {
            this.limitExceeded = limitExceeded;
            this.budget = budget;
            this.newPotentialSpentAmount = newPotentialSpentAmount;
            this.budgetFound = budgetFound;
        }
    }
    
    // Transaction Services
    public void addTransaction(Transaction transaction) throws Exception {
        transactionDAO.addTransaction(transaction);
        updateBudgetSpending(transaction);
    }
    
    public List<Transaction> getAllTransactions() throws Exception {
        return transactionDAO.getAllTransactions();
    }
    
    public List<Transaction> getTransactionsForCurrentMonth() throws Exception {
        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();
        return transactionDAO.getTransactionsByDateRange(start, end);
    }
    
    public void updateTransaction(Transaction transaction) throws Exception {
        transactionDAO.updateTransaction(transaction);
    }
    
    public void deleteTransaction(String id) throws Exception {
        transactionDAO.deleteTransaction(id);
    }
    
    // Financial Calculations
    public double getTotalIncome(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> "income".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    
    public double getTotalExpenses(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> "expense".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    
    public double getBalance(List<Transaction> transactions) {
        return getTotalIncome(transactions) - getTotalExpenses(transactions);
    }
    
    public Map<String, Double> getCategoryTotals(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }
    
    public Map<LocalDate, Double> getDailyTotals(List<Transaction> transactions, String type) {
        return transactions.stream()
                .filter(t -> type.equals(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getDate,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }
    
    // Budget Services
    public void addBudget(Budget budget) throws Exception {
        budgetDAO.addBudget(budget);
    }
    
    public List<Budget> getAllBudgets() throws Exception {
        return budgetDAO.getAllBudgets();
    }
    
    public void updateBudget(Budget budget) throws Exception {
        budgetDAO.updateBudget(budget);
    }
    
    public void deleteBudget(String id) throws Exception {
        budgetDAO.deleteBudget(id);
    }
     public BudgetCheckResult checkBudgetBeforeTransaction(String category, double transactionAmount, LocalDate transactionDate) throws Exception {
        List<Budget> budgets = getAllBudgets();
        for (Budget budget : budgets) {
            if (budget.getCategory().equalsIgnoreCase(category)) {
                // Check if the transaction date falls within the budget period
                if (!transactionDate.isBefore(budget.getStartDate()) && !transactionDate.isAfter(budget.getEndDate())) {
                    double newPotentialSpent = budget.getSpentAmount() + transactionAmount;
                    boolean exceeded = newPotentialSpent > budget.getLimitAmount();
                    return new BudgetCheckResult(exceeded, budget, newPotentialSpent, true);
                }
            }
        }
        return new BudgetCheckResult(false, null, 0, false); // No active budget found for this category and date
    }
    
    private void updateBudgetSpending(Transaction transaction) throws Exception {
        if ("expense".equals(transaction.getType())) {
            List<Budget> budgets = getAllBudgets();
            for (Budget budget : budgets) {
                if (budget.getCategory().equals(transaction.getCategory())) {
                    budget.setSpentAmount(budget.getSpentAmount() + transaction.getAmount());
                    updateBudget(budget);
                    break;
                }
            }
        }
    }
    
    // Goal Services
    public void addGoal(Goal goal) throws Exception {
        goalDAO.addGoal(goal);
    }
    
    public List<Goal> getAllGoals() throws Exception {
        return goalDAO.getAllGoals();
    }
    
    public void updateGoal(Goal goal) throws Exception {
        goalDAO.updateGoal(goal);
    }
    
    public void deleteGoal(String id) throws Exception {
        goalDAO.deleteGoal(id);
    }
    
    public void updateGoalProgress(String goalId, double amount) throws Exception {
        List<Goal> goals = getAllGoals();
        for (Goal goal : goals) {
            if (goal.getId().equals(goalId)) {
                goal.setCurrentAmount(goal.getCurrentAmount() + amount);
                if (goal.isCompleted()) {
                    goal.setStatus("completed");
                }
                updateGoal(goal);
                break;
            }
        }
    }
    
}
