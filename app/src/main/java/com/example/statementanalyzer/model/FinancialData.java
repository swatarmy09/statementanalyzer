package com.example.statementanalyzer.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinancialData {
    private Date startDate;
    private Date endDate;
    private double totalIncome;
    private double totalExpenses;
    private Map<String, Double> categoryTotals;
    private List<Transaction> transactions;

    public FinancialData() {
        this.categoryTotals = new HashMap<>();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public Map<String, Double> getCategoryTotals() {
        return categoryTotals;
    }

    public void setCategoryTotals(Map<String, Double> categoryTotals) {
        this.categoryTotals = categoryTotals;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);

        // Update totals
        double amount = transaction.getAmount();
        if (amount > 0) {
            totalIncome += amount;
        } else {
            totalExpenses += amount;
        }

        // Update category totals
        String category = transaction.getCategory();
        double currentTotal = categoryTotals.getOrDefault(category, 0.0);
        categoryTotals.put(category, currentTotal + amount);
    }
}