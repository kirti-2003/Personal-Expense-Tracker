// === START OF FILE: src/com/expensetracker/ExpenseService.java ===
package expensetracker;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ExpenseService handles business logic operations like calculating totals and grouping.
 */
public class ExpenseService {
    private TransactionDAO transactionDAO;

    public ExpenseService(TransactionDAO dao) {
        this.transactionDAO = dao;
    }

    /**
     * Retrieves all transactions for a given year and month.
     */
    public List<Transaction> getTransactionsForMonth(int year, int month) throws Exception {
        return transactionDAO.getTransactionsByMonth(year, month);
    }

    /**
     * Calculates total income from the given transactions.
     */
    public double calculateTotalIncome(List<Transaction> transactions) {
        return transactions.stream()
                .filter(tx -> "income".equalsIgnoreCase(tx.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Calculates total expenses.
     */
    public double calculateTotalExpense(List<Transaction> transactions) {
        return transactions.stream()
                .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Calculates overall balance = income - expense.
     */
    public double calculateBalance(List<Transaction> transactions) {
        return calculateTotalIncome(transactions) - calculateTotalExpense(transactions);
    }

    /**
     * Returns a map of day of month to total income amount for that day.
     */
    public Map<Integer, Double> getIncomeByDay(List<Transaction> transactions) {
        return transactions.stream()
                .filter(tx -> "income".equalsIgnoreCase(tx.getType()))
                .collect(Collectors.groupingBy(
                        tx -> getDayFromDate(tx.getDate()),
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    /**
     * Returns a map of day of month to total expense amount for that day.
     */
    public Map<Integer, Double> getExpenseByDay(List<Transaction> transactions) {
        return transactions.stream()
                .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
                .collect(Collectors.groupingBy(
                        tx -> getDayFromDate(tx.getDate()),
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    /**
     * Returns aggregated amounts by category.
     */
    public Map<String, Double> getCategoryTotals(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    private int getDayFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }
}
