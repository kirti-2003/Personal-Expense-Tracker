package com.expensetracker.dao;

import com.expensetracker.models.Goal;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Goal operations
 */
public class GoalDAO {
    private MongoCollection<Document> collection;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public GoalDAO() {
        collection = DatabaseConnection.getInstance().getDatabase().getCollection("goals");
    }
    
    public void addGoal(Goal goal) throws Exception {
        try {
            Document doc = new Document()
                    .append("name", goal.getName())
                    .append("description", goal.getDescription())
                    .append("targetAmount", goal.getTargetAmount())
                    .append("currentAmount", goal.getCurrentAmount())
                    .append("targetDate", goal.getTargetDate().format(DATE_FORMAT))
                    .append("createdDate", goal.getCreatedDate().format(DATE_FORMAT))
                    .append("status", goal.getStatus());
            
            collection.insertOne(doc);
            goal.setId(doc.getObjectId("_id").toHexString());
        } catch (Exception e) {
            throw new Exception("Failed to add goal: " + e.getMessage());
        }
    }
    
    public List<Goal> getAllGoals() throws Exception {
        try {
            List<Document> docs = collection.find().into(new ArrayList<>());
            return documentsToGoals(docs);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve goals: " + e.getMessage());
        }
    }
    
    public void updateGoal(Goal goal) throws Exception {
        try {
            Document doc = new Document()
                    .append("name", goal.getName())
                    .append("description", goal.getDescription())
                    .append("targetAmount", goal.getTargetAmount())
                    .append("currentAmount", goal.getCurrentAmount())
                    .append("targetDate", goal.getTargetDate().format(DATE_FORMAT))
                    .append("createdDate", goal.getCreatedDate().format(DATE_FORMAT))
                    .append("status", goal.getStatus());
            
            collection.replaceOne(
                    Filters.eq("_id", new ObjectId(goal.getId())),
                    doc
            );
        } catch (Exception e) {
            throw new Exception("Failed to update goal: " + e.getMessage());
        }
    }
    
    public void deleteGoal(String id) throws Exception {
        try {
            collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
        } catch (Exception e) {
            throw new Exception("Failed to delete goal: " + e.getMessage());
        }
    }
    
    private List<Goal> documentsToGoals(List<Document> docs) {
        List<Goal> goals = new ArrayList<>();
        for (Document doc : docs) {
            Goal goal = new Goal();
            goal.setId(doc.getObjectId("_id").toHexString());
            goal.setName(doc.getString("name"));
            goal.setDescription(doc.getString("description"));
            goal.setTargetAmount(doc.getDouble("targetAmount"));
            goal.setCurrentAmount(doc.getDouble("currentAmount"));
            goal.setTargetDate(LocalDate.parse(doc.getString("targetDate"), DATE_FORMAT));
            goal.setCreatedDate(LocalDate.parse(doc.getString("createdDate"), DATE_FORMAT));
            goal.setStatus(doc.getString("status"));
            goals.add(goal);
        }
        return goals;
    }
}