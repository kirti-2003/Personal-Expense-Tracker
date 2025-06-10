package com.expensetracker.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Singleton class for MongoDB connection management
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    
    // Replace with your MongoDB Atlas connection string
    private static final String CONNECTION_STRING = "mongodb+srv://kirti:Pass@expensetrackerenhanced.6bu6ons.mongodb.net/?retryWrites=true&w=majority&appName=expenseTrackerEnhanced";
    private static final String DATABASE_NAME = "expenseTrackerEnhanced";
    
    private DatabaseConnection() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to MongoDB: " + e.getMessage());
        }
    }
    
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public MongoDatabase getDatabase() {
        return database;
    }
    
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}