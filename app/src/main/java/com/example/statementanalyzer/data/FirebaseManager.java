package com.example.statementanalyzer.data;

import android.util.Log;

import com.example.statementanalyzer.model.FinancialData;
import com.example.statementanalyzer.model.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {

    private static final String TAG = "FirebaseManager";
    private static final String COLLECTION_FINANCIAL_DATA = "financial_data";
    private static final String SUBCOLLECTION_TRANSACTIONS = "transactions";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface FirebaseCallback {
        void onSuccess(boolean success);
    }

    public interface DataCallback {
        void onDataLoaded(List<FinancialData> financialDataList);
    }

    public FirebaseManager() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();

        // Ensure user is signed in
        if (auth.getCurrentUser() == null) {
            // Sign in anonymously if no user is signed in
            auth.signInAnonymously()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Anonymous sign-in successful");
                        } else {
                            Log.e(TAG, "Anonymous sign-in failed", task.getException());
                        }
                    });
        }
    }

    public void uploadData(List<FinancialData> financialDataList, FirebaseCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onSuccess(false);
            return;
        }

        String userId = currentUser.getUid();

        // Upload each financial data object
        for (FinancialData data : financialDataList) {
            // Create financial data document
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("startDate", data.getStartDate());
            dataMap.put("endDate", data.getEndDate());
            dataMap.put("totalIncome", data.getTotalIncome());
            dataMap.put("totalExpenses", data.getTotalExpenses());
            dataMap.put("categoryTotals", data.getCategoryTotals());
            dataMap.put("timestamp", new Date());

            // Add document to Firestore
            db.collection("users")
                    .document(userId)
                    .collection(COLLECTION_FINANCIAL_DATA)
                    .add(dataMap)
                    .addOnSuccessListener(documentReference -> {
                        // Upload transactions as subcollection
                        uploadTransactions(documentReference, data.getTransactions(), callback);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error uploading financial data", e);
                        callback.onSuccess(false);
                    });
        }
    }

    private void uploadTransactions(DocumentReference docRef, List<Transaction> transactions, FirebaseCallback callback) {
        // Create a batch to upload all transactions at once
        for (Transaction transaction : transactions) {
            Map<String, Object> transactionMap = new HashMap<>();
            transactionMap.put("date", transaction.getDate());
            transactionMap.put("description", transaction.getDescription());
            transactionMap.put("amount", transaction.getAmount());
            transactionMap.put("category", transaction.getCategory());

            docRef.collection(SUBCOLLECTION_TRANSACTIONS)
                    .add(transactionMap)
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error uploading transaction", e);
                        callback.onSuccess(false);
                    });
        }

        callback.onSuccess(true);
    }

    public void fetchFinancialData(DataCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onDataLoaded(new ArrayList<>());
            return;
        }

        String userId = currentUser.getUid();

        db.collection("users")
                .document(userId)
                .collection(COLLECTION_FINANCIAL_DATA)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FinancialData> financialDataList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Parse financial data
                        FinancialData data = new FinancialData();
                        data.setStartDate(document.getDate("startDate"));
                        data.setEndDate(document.getDate("endDate"));
                        data.setTotalIncome(document.getDouble("totalIncome"));
                        data.setTotalExpenses(document.getDouble("totalExpenses"));

                        // Parse category totals
                        Map<String, Double> categoryTotals = new HashMap<>();
                        Map<String, Object> categoryMap = (Map<String, Object>) document.get("categoryTotals");
                        if (categoryMap != null) {
                            for (Map.Entry<String, Object> entry : categoryMap.entrySet()) {
                                categoryTotals.put(entry.getKey(), ((Number) entry.getValue()).doubleValue());
                            }
                        }
                        data.setCategoryTotals(categoryTotals);

                        // Fetch transactions for this financial data
                        fetchTransactions(document.getReference(), data, financialDataList, callback);
                    }

                    // If no financial data found, return empty list
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onDataLoaded(financialDataList);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching financial data", e);
                    callback.onDataLoaded(new ArrayList<>());
                });
    }

    private void fetchTransactions(DocumentReference docRef, FinancialData data,
                                   List<FinancialData> financialDataList, DataCallback callback) {
        docRef.collection(SUBCOLLECTION_TRANSACTIONS)
                .get()
                .addOnSuccessListener(transactionSnapshots -> {
                    List<Transaction> transactions = new ArrayList<>();

                    for (QueryDocumentSnapshot document : transactionSnapshots) {
                        Transaction transaction = new Transaction();
                        transaction.setDate(document.getDate("date"));
                        transaction.setDescription(document.getString("description"));
                        transaction.setAmount(document.getDouble("amount"));
                        transaction.setCategory(document.getString("category"));

                        transactions.add(transaction);
                    }

                    data.setTransactions(transactions);
                    financialDataList.add(data);

                    // Return the updated list
                    callback.onDataLoaded(financialDataList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching transactions", e);
                    // Still add the financial data even without transactions
                    financialDataList.add(data);
                    callback.onDataLoaded(financialDataList);
                });
    }

    public void clearAllData(FirebaseCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onSuccess(false);
            return;
        }

        String userId = currentUser.getUid();

        // Get all financial data documents
        db.collection("users")
                .document(userId)
                .collection(COLLECTION_FINANCIAL_DATA)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onSuccess(true);
                        return;
                    }

                    int totalDocuments = queryDocumentSnapshots.size();
                    final int[] deletedCount = {0};

                    // Delete each document and its subcollections
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // First delete all transactions in subcollection
                        document.getReference().collection(SUBCOLLECTION_TRANSACTIONS)
                                .get()
                                .addOnSuccessListener(transactionSnapshots -> {
                                    for (QueryDocumentSnapshot transactionDoc : transactionSnapshots) {
                                        transactionDoc.getReference().delete();
                                    }

                                    // Then delete the main document
                                    document.getReference().delete()
                                            .addOnSuccessListener(aVoid -> {
                                                deletedCount[0]++;
                                                if (deletedCount[0] >= totalDocuments) {
                                                    callback.onSuccess(true);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error deleting document", e);
                                                callback.onSuccess(false);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error getting transactions for deletion", e);
                                    callback.onSuccess(false);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting documents for deletion", e);
                    callback.onSuccess(false);
                });
    }
}