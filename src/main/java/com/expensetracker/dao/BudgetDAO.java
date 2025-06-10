package com.expensetracker.dao;

import com.expensetracker.models.Budget;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Budget operations
 */
public class BudgetDAO {
    private MongoCollection<Document> collection;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public BudgetDAO() {
        collection = DatabaseConnection.getInstance().getDatabase().getCollection("budgets");
    }
    
    public void addBudget(Budget budget) throws Exception {
        try {
            Document doc = new Document()
                    .append("category", budget.getCategory())
                    .append("limitAmount", budget.getLimitAmount())
                    .append("spentAmount", budget.getSpentAmount())
                    .append("startDate", budget.getStartDate().format(DATE_FORMAT))
                    .append("endDate", budget.getEndDate().format(DATE_FORMAT))
                    .append("period", budget.getPeriod());
            
            collection.insertOne(doc);
            budget.setId(doc.getObjectId("_id").toHexString());
        } catch (Exception e) {
            throw new Exception("Failed to add budget: " + e.getMessage());
        }
    }
    
    public List<Budget> getAllBudgets() throws Exception {
        try {
            List<Document> docs = collection.find().into(new ArrayList<>());
            return documentsToBudgets(docs);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve budgets: " + e.getMessage());
        }
    }
    
    public void updateBudget(Budget budget) throws Exception {
        try {
            Document doc = new Document()
                    .append("category", budget.getCategory())
                    .append("limitAmount", budget.getLimitAmount())
                    .append("spentAmount", budget.getSpentAmount())
                    .append("startDate", budget.getStartDate().format(DATE_FORMAT))
                    .append("endDate", budget.getEndDate().format(DATE_FORMAT))
                    .append("period", budget.getPeriod());
            
            collection.replaceOne(
                    Filters.eq("_id", new ObjectId(budget.getId())),
                    doc
            );
        } catch (Exception e) {
            throw new Exception("Failed to update budget: " + e.getMessage());
        }
    }
    
    public void deleteBudget(String id) throws Exception {
        try {
            collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
        } catch (Exception e) {
            throw new Exception("Failed to delete budget: " + e.getMessage());
        }
    }
    
    private List<Budget> documentsToBudgets(List<Document> docs) {
        List<Budget> budgets = new ArrayList<>();
        for (Document doc : docs) {
            Budget budget = new Budget();
            budget.setId(doc.getObjectId("_id").toHexString());
            budget.setCategory(doc.getString("category"));
            budget.setLimitAmount(doc.getDouble("limitAmount"));
            budget.setSpentAmount(doc.getDouble("spentAmount"));
            budget.setStartDate(LocalDate.parse(doc.getString("startDate"), DATE_FORMAT));
            budget.setEndDate(LocalDate.parse(doc.getString("endDate"), DATE_FORMAT));
            budget.setPeriod(doc.getString("period"));
            budgets.add(budget);
        }
        return budgets;
    }
}