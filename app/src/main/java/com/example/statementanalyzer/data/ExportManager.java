package com.example.statementanalyzer.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.example.statementanalyzer.model.FinancialData;
import com.example.statementanalyzer.model.Transaction;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExportManager {

    private static final String TAG = "ExportManager";
    private final Context context;

    public interface ExportCallback {
        void onSuccess(Uri fileUri);
        void onError(String errorMessage);
    }

    public ExportManager(Context context) {
        this.context = context;
    }

    // Export financial data to CSV
    public void exportToCSV(List<FinancialData> financialDataList, ExportCallback callback) {
        new Thread(() -> {
            try {
                // Create file
                File exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                File file = new File(exportDir, "financial_data_" + timestamp + ".csv");

                // Write CSV data
                FileWriter fileWriter = new FileWriter(file);
                CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT
                        .withHeader("Date", "Description", "Amount", "Category"));

                for (FinancialData data : financialDataList) {
                    for (Transaction transaction : data.getTransactions()) {
                        String dateStr = new SimpleDateFormat("MM/dd/yyyy", Locale.US)
                                .format(transaction.getDate());

                        csvPrinter.printRecord(
                                dateStr,
                                transaction.getDescription(),
                                transaction.getAmount(),
                                transaction.getCategory()
                        );
                    }
                }

                csvPrinter.flush();
                csvPrinter.close();

                // Get content URI via FileProvider
                Uri contentUri = FileProvider.getUriForFile(
                        context,
                        "com.example.statementanalyzer.fileprovider",
                        file
                );

                // Return success
                callback.onSuccess(contentUri);

            } catch (IOException e) {
                Log.e(TAG, "Error exporting to CSV", e);
                callback.onError("Error exporting to CSV: " + e.getMessage());
            }
        }).start();
    }

    // Export financial data to JSON
    public void exportToJSON(List<FinancialData> financialDataList, ExportCallback callback) {
        new Thread(() -> {
            try {
                // Create file
                File exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                File file = new File(exportDir, "financial_data_" + timestamp + ".json");

                // Create JSON data
                JSONArray jsonArray = new JSONArray();

                for (FinancialData data : financialDataList) {
                    JSONObject dataObject = new JSONObject();

                    // Add basic info
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    dataObject.put("startDate", dateFormat.format(data.getStartDate()));
                    dataObject.put("endDate", dateFormat.format(data.getEndDate()));
                    dataObject.put("totalIncome", data.getTotalIncome());
                    dataObject.put("totalExpenses", data.getTotalExpenses());

                    // Add category totals
                    JSONObject categoryTotals = new JSONObject();
                    for (Map.Entry<String, Double> entry : data.getCategoryTotals().entrySet()) {
                        categoryTotals.put(entry.getKey(), entry.getValue());
                    }
                    dataObject.put("categoryTotals", categoryTotals);

                    // Add transactions
                    JSONArray transactionsArray = new JSONArray();
                    for (Transaction transaction : data.getTransactions()) {
                        JSONObject transactionObject = new JSONObject();
                        transactionObject.put("date", dateFormat.format(transaction.getDate()));
                        transactionObject.put("description", transaction.getDescription());
                        transactionObject.put("amount", transaction.getAmount());
                        transactionObject.put("category", transaction.getCategory());

                        transactionsArray.put(transactionObject);
                    }
                    dataObject.put("transactions", transactionsArray);

                    jsonArray.put(dataObject);
                }

                // Write to file
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(jsonArray.toString(4)); // Pretty print with 4-space indentation
                fileWriter.flush();
                fileWriter.close();

                // Get content URI via FileProvider
                Uri contentUri = FileProvider.getUriForFile(
                        context,
                        "com.example.statementanalyzer.fileprovider",
                        file
                );

                // Return success
                callback.onSuccess(contentUri);

            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error exporting to JSON", e);
                callback.onError("Error exporting to JSON: " + e.getMessage());
            }
        }).start();
    }

    // Share exported file
    public void shareFile(Uri fileUri, String title) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setType("*/*");

        // Grant temporary read permission
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Start the share activity
        context.startActivity(Intent.createChooser(shareIntent, title));
    }
}