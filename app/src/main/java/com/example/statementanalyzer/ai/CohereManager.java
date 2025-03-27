package com.example.statementanalyzer.ai;

import android.util.Log;

import com.example.statementanalyzer.model.FinancialData;
import com.example.statementanalyzer.model.Transaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CohereManager {

    private static final String TAG = "CohereManager";
    private static final String COHERE_API_URL = "https://api.cohere.ai/v1/generate";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String apiKey;
    private final OkHttpClient client;

    public interface CohereCallback {
        void onResponse(String response);
        void onError(String errorMessage);
    }

    public CohereManager(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void generateFinancialStory(List<FinancialData> financialDataList, CohereCallback callback) {
        try {
            // Create a summary of the financial data
            StringBuilder prompt = new StringBuilder();
            prompt.append("Generate a comprehensive financial analysis based on the following data:\n\n");

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

            for (FinancialData data : financialDataList) {
                prompt.append("Statement Period: ")
                        .append(dateFormat.format(data.getStartDate()))
                        .append(" to ")
                        .append(dateFormat.format(data.getEndDate()))
                        .append("\n");

                prompt.append("Total Income: $").append(String.format("%.2f", data.getTotalIncome())).append("\n");
                prompt.append("Total Expenses: $").append(String.format("%.2f", Math.abs(data.getTotalExpenses()))).append("\n");
                prompt.append("Net Change: $").append(String.format("%.2f", data.getTotalIncome() + data.getTotalExpenses())).append("\n\n");

                prompt.append("Expense Categories:\n");
                for (Map.Entry<String, Double> entry : data.getCategoryTotals().entrySet()) {
                    prompt.append("- ").append(entry.getKey()).append(": $")
                            .append(String.format("%.2f", Math.abs(entry.getValue())))
                            .append("\n");
                }

                prompt.append("\nTop 5 Transactions:\n");
                List<Transaction> transactions = data.getTransactions();
                transactions.sort((t1, t2) -> Double.compare(Math.abs(t2.getAmount()), Math.abs(t1.getAmount())));

                int count = Math.min(5, transactions.size());
                for (int i = 0; i < count; i++) {
                    Transaction transaction = transactions.get(i);
                    prompt.append("- ")
                            .append(dateFormat.format(transaction.getDate()))
                            .append(": ")
                            .append(transaction.getDescription())
                            .append(" - $")
                            .append(String.format("%.2f", Math.abs(transaction.getAmount())))
                            .append(" (")
                            .append(transaction.getCategory())
                            .append(")\n");
                }

                prompt.append("\n");
            }

            prompt.append("Based on this data, provide a detailed financial analysis including:\n");
            prompt.append("1. Overall financial health assessment\n");
            prompt.append("2. Spending patterns and trends\n");
            prompt.append("3. Areas where the user could save money\n");
            prompt.append("4. Recommendations for budget improvements\n");
            prompt.append("5. Any unusual transactions or patterns to be aware of\n\n");
            prompt.append("Format the analysis in a clear, concise manner with sections and bullet points where appropriate.");

            // Create JSON request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "command");
            requestBody.put("prompt", prompt.toString());
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);

            // Create request
            Request request = new Request.Builder()
                    .url(COHERE_API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toString(), JSON))
                    .build();

            // Execute request
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Cohere API request failed", e);
                    callback.onError("Failed to connect to AI service: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            callback.onError("API error: " + response.code() + " " + response.message());
                            return;
                        }

                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String generatedText = jsonResponse.getJSONArray("generations")
                                .getJSONObject(0)
                                .getString("text");

                        callback.onResponse(generatedText.trim());
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing Cohere API response", e);
                        callback.onError("Error processing AI response: " + e.getMessage());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating Cohere API request", e);
            callback.onError("Error creating AI request: " + e.getMessage());
        }
    }

    public void getChatResponse(String userMessage, List<FinancialData> financialDataList, CohereCallback callback) {
        try {
            // Create a context with financial data summary
            StringBuilder prompt = new StringBuilder();
            prompt.append("You are a financial assistant helping a user understand their financial data. ");
            prompt.append("Here is a summary of their financial data:\n\n");

            // Add financial data summary
            double totalIncome = 0;
            double totalExpenses = 0;

            for (FinancialData data : financialDataList) {
                totalIncome += data.getTotalIncome();
                totalExpenses += data.getTotalExpenses();
            }

            prompt.append("Total Income: $").append(String.format("%.2f", totalIncome)).append("\n");
            prompt.append("Total Expenses: $").append(String.format("%.2f", Math.abs(totalExpenses))).append("\n");
            prompt.append("Net Change: $").append(String.format("%.2f", totalIncome + totalExpenses)).append("\n\n");

            // Add top expense categories
            prompt.append("Top Expense Categories:\n");
            for (FinancialData data : financialDataList) {
                for (Map.Entry<String, Double> entry : data.getCategoryTotals().entrySet()) {
                    prompt.append("- ").append(entry.getKey()).append(": $")
                            .append(String.format("%.2f", Math.abs(entry.getValue())))
                            .append("\n");
                }
            }

            prompt.append("\nThe user asks: ").append(userMessage).append("\n\n");
            prompt.append("Provide a helpful, concise response addressing their question based on the financial data provided. ");
            prompt.append("If you can't answer based on the data, suggest what additional information might be needed.");

            // Create JSON request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "command");
            requestBody.put("prompt", prompt.toString());
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);

            // Create request
            Request request = new Request.Builder()
                    .url(COHERE_API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toString(), JSON))
                    .build();

            // Execute request
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Cohere API request failed", e);
                    callback.onError("Failed to connect to AI service: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            callback.onError("API error: " + response.code() + " " + response.message());
                            return;
                        }

                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String generatedText = jsonResponse.getJSONArray("generations")
                                .getJSONObject(0)
                                .getString("text");

                        callback.onResponse(generatedText.trim());
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing Cohere API response", e);
                        callback.onError("Error processing AI response: " + e.getMessage());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating Cohere API request", e);
            callback.onError("Error creating AI request: " + e.getMessage());
        }
    }
}
