# Personal Expense Tracker - Java Edition

A comprehensive personal expense tracking application built with Java Swing and MongoDB Atlas.

## Features

- **Dashboard**: Overview of financial status with income, expenses, and balance
- **Transaction Management**: Add, view, edit, and delete income/expense transactions
- **Budget Tracking**: Set budgets for different categories and monitor spending
- **Financial Goals**: Set and track progress towards financial objectives
- **Reports**: Detailed financial reports and category breakdowns
- **Modern UI**: Clean and intuitive Java Swing interface

## Technology Stack

- **Frontend**: Java Swing with modern UI components
- **Backend**: MongoDB Atlas (Cloud Database)
- **Build Tool**: Maven
- **Java Version**: 11+

### Prerequisites

1. **Java 11 or higher** - [Download here](https://adoptopenjdk.net/)
2. **NetBeans IDE** - [Download here](https://netbeans.apache.org/download/)
3. **MongoDB Atlas Account** - [Sign up here](https://www.mongodb.com/cloud/atlas)

### Database Setup

1. Create a MongoDB Atlas cluster
2. Create a database named `expenseTrackerEnhanced`
3. Create the following collections:
   - `transactions`
   - `budgets`
   - `goals`
4. Get your connection string from MongoDB Atlas

### Project Setup

1. **Clone or Download** the project files
2. **Open in NetBeans**:
   - File → Open Project
   - Select the project folder
   - NetBeans will automatically detect it as a Maven project

3. **Update Database Connection**:
   - Open `src/main/java/com/expensetracker/dao/DatabaseConnection.java`
   - Replace the `CONNECTION_STRING` with your MongoDB Atlas connection string:
   ```java
   private static final String CONNECTION_STRING = "your_mongodb_atlas_connection_string_here";
   ```

4. **Build the Project**:
   - Right-click on project → Clean and Build
   - Maven will download all dependencies automatically

5. **Run the Application**:
   - Right-click on `MainFrame.java` → Run File
   - Or run the main method in `MainFrame.java`

## Usage Guide

### 1. Dashboard
- View your financial overview at a glance
- See income, expenses, and balance for the current month
- Monitor budget progress and goal achievements
- Review recent transactions

### 2. Transactions
- Add new income or expense transactions
- Choose from predefined categories or add custom ones
- Filter and search through your transaction history
- Edit or delete existing transactions

### 3. Budgets
- Set monthly, weekly, or yearly budgets for different categories
- Monitor spending against your budget limits
- Get visual feedback when approaching or exceeding limits

### 4. Goals
- Set financial goals with target amounts and dates
- Track progress towards your objectives
- Update goal progress manually or automatically

### 5. Reports
- View detailed financial reports
- Analyze spending patterns by category
- Generate monthly summaries
