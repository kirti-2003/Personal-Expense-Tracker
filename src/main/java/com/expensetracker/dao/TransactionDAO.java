package com.expensetracker.dao;

import com.expensetracker.models.Transaction;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced DAO for Transaction operations
 */
public class TransactionDAO {
    private MongoCollection<Document> collection;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public TransactionDAO() {
        collection = DatabaseConnection.getInstance().getDatabase().getCollection("transactions");
    }
    
    public void addTransaction(Transaction transaction) throws Exception {
        try {
            Document doc = new Document()
                    .append("type", transaction.getType())
                    .append("category", transaction.getCategory())
                    .append("amount", transaction.getAmount())
                    .append("description", transaction.getDescription())
                    .append("date", transaction.getFormattedDate())
                    .append("notes", transaction.getNotes())
                    .append("paymentMethod", transaction.getPaymentMethod());
            
            collection.insertOne(doc);
            transaction.setId(doc.getObjectId("_id").toHexString());
        } catch (Exception e) {
            throw new Exception("Failed to add transaction: " + e.getMessage());
        }
    }
    
    public List<Transaction> getAllTransactions() throws Exception {
        try {
            List<Document> docs = collection.find()
                    .sort(Sorts.descending("date"))
                    .into(new ArrayList<>());
            
            return documentsToTransactions(docs);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve transactions: " + e.getMessage());
        }
    }
    
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) throws Exception {
        try {
            String startDateStr = startDate.format(DATE_FORMAT);
            String endDateStr = endDate.format(DATE_FORMAT);
            
            List<Document> docs = collection.find(
                    Filters.and(
                            Filters.gte("date", startDateStr),
                            Filters.lte("date", endDateStr)
                    ))
                    .sort(Sorts.descending("date"))
                    .into(new ArrayList<>());
            
            return documentsToTransactions(docs);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve transactions: " + e.getMessage());
        }
    }
    
    public List<Transaction> getTransactionsByCategory(String category) throws Exception {
        try {
            List<Document> docs = collection.find(Filters.eq("category", category))
                    .sort(Sorts.descending("date"))
                    .into(new ArrayList<>());
            
            return documentsToTransactions(docs);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve transactions: " + e.getMessage());
        }
    }
    
    public void updateTransaction(Transaction transaction) throws Exception {
        try {
            Document doc = new Document()
                    .append("type", transaction.getType())
                    .append("category", transaction.getCategory())
                    .append("amount", transaction.getAmount())
                    .append("description", transaction.getDescription())
                    .append("date", transaction.getFormattedDate())
                    .append("notes", transaction.getNotes())
                    .append("paymentMethod", transaction.getPaymentMethod());
            
            collection.replaceOne(
                    Filters.eq("_id", new ObjectId(transaction.getId())),
                    doc
            );
        } catch (Exception e) {
            throw new Exception("Failed to update transaction: " + e.getMessage());
        }
    }
    
    public void deleteTransaction(String id) throws Exception {
        try {
            collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
        } catch (Exception e) {
            throw new Exception("Failed to delete transaction: " + e.getMessage());
        }
    }
    
    private List<Transaction> documentsToTransactions(List<Document> docs) {
        List<Transaction> transactions = new ArrayList<>();
        for (Document doc : docs) {
            Transaction transaction = new Transaction();
            transaction.setId(doc.getObjectId("_id").toHexString());
            transaction.setType(doc.getString("type"));
            transaction.setCategory(doc.getString("category"));
            transaction.setAmount(doc.getDouble("amount"));
            transaction.setDescription(doc.getString("description"));
            transaction.setDate(LocalDate.parse(doc.getString("date"), DATE_FORMAT));
            transaction.setNotes(doc.getString("notes"));
            transaction.setPaymentMethod(doc.getString("paymentMethod"));
            transactions.add(transaction);
        }
        return transactions;
    }
}