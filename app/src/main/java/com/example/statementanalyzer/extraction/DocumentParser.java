package com.example.statementanalyzer.extraction;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.statementanalyzer.model.FinancialData;
import com.example.statementanalyzer.model.Transaction;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentParser {

    private static final String TAG = "DocumentParser";
    private final Context context;

    // Common date formats in financial statements
    private final SimpleDateFormat[] dateFormats = {
            new SimpleDateFormat("MM/dd/yyyy", Locale.US),
            new SimpleDateFormat("MM-dd-yyyy", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd", Locale.US),
            new SimpleDateFormat("MMM dd, yyyy", Locale.US)
    };

    // Common categories for transactions
    private final Map<String, String> categoryKeywords = new HashMap<>();

    public DocumentParser(Context context) {
        this.context = context;
        initializeCategoryKeywords();
    }

    private void initializeCategoryKeywords() {
        // Shopping
        categoryKeywords.put("amazon", "Shopping");
        categoryKeywords.put("walmart", "Shopping");
        categoryKeywords.put("target", "Shopping");
        categoryKeywords.put("ebay", "Shopping");
        categoryKeywords.put("store", "Shopping");
        categoryKeywords.put("shop", "Shopping");

        // Groceries
        categoryKeywords.put("grocery", "Groceries");
        categoryKeywords.put("supermarket", "Groceries");
        categoryKeywords.put("food", "Groceries");
        categoryKeywords.put("market", "Groceries");

        // Dining
        categoryKeywords.put("restaurant", "Dining");
        categoryKeywords.put("cafe", "Dining");
        categoryKeywords.put("coffee", "Dining");
        categoryKeywords.put("starbucks", "Dining");
        categoryKeywords.put("mcdonald", "Dining");
        categoryKeywords.put("burger", "Dining");
        categoryKeywords.put("pizza", "Dining");

        // Transportation
        categoryKeywords.put("gas", "Transportation");
        categoryKeywords.put("uber", "Transportation");
        categoryKeywords.put("lyft", "Transportation");
        categoryKeywords.put("taxi", "Transportation");
        categoryKeywords.put("transit", "Transportation");
        categoryKeywords.put("parking", "Transportation");
        categoryKeywords.put("auto", "Transportation");

        // Utilities
        categoryKeywords.put("electric", "Utilities");
        categoryKeywords.put("water", "Utilities");
        categoryKeywords.put("gas bill", "Utilities");
        categoryKeywords.put("internet", "Utilities");
        categoryKeywords.put("phone", "Utilities");
        categoryKeywords.put("mobile", "Utilities");
        categoryKeywords.put("utility", "Utilities");

        // Housing
        categoryKeywords.put("rent", "Housing");
        categoryKeywords.put("mortgage", "Housing");
        categoryKeywords.put("apartment", "Housing");
        categoryKeywords.put("home", "Housing");

        // Entertainment
        categoryKeywords.put("movie", "Entertainment");
        categoryKeywords.put("netflix", "Entertainment");
        categoryKeywords.put("spotify", "Entertainment");
        categoryKeywords.put("hulu", "Entertainment");
        categoryKeywords.put("disney", "Entertainment");
        categoryKeywords.put("theater", "Entertainment");
        categoryKeywords.put("game", "Entertainment");

        // Health
        categoryKeywords.put("doctor", "Health");
        categoryKeywords.put("medical", "Health");
        categoryKeywords.put("pharmacy", "Health");
        categoryKeywords.put("hospital", "Health");
        categoryKeywords.put("clinic", "Health");
        categoryKeywords.put("dental", "Health");
        categoryKeywords.put("vision", "Health");

        // Income
        categoryKeywords.put("salary", "Income");
        categoryKeywords.put("deposit", "Income");
        categoryKeywords.put("payroll", "Income");
        categoryKeywords.put("direct deposit", "Income");
        categoryKeywords.put("payment received", "Income");
        categoryKeywords.put("refund", "Income");
    }

    public List<FinancialData> parseDocument(Uri documentUri) throws IOException {
        String mimeType = context.getContentResolver().getType(documentUri);

        if (mimeType != null) {
            if (mimeType.equals("application/pdf")) {
                return parsePdf(documentUri);
            } else if (mimeType.equals("text/csv") || mimeType.equals("application/vnd.ms-excel")) {
                return parseCsv(documentUri);
            }
        }

        // Try to determine type by extension
        String fileName = documentUri.getLastPathSegment();
        if (fileName != null) {
            if (fileName.toLowerCase().endsWith(".pdf")) {
                return parsePdf(documentUri);
            } else if (fileName.toLowerCase().endsWith(".csv")) {
                return parseCsv(documentUri);
            }
        }

        throw new IOException("Unsupported file type");
    }

    private List<FinancialData> parsePdf(Uri pdfUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
        PDDocument document = PDDocument.load(inputStream);

        try {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Extract statement period
            Date startDate = null;
            Date endDate = null;

            Pattern periodPattern = Pattern.compile("(?i)statement period:?\\s*(\\w+\\s*\\d+,?\\s*\\d+)\\s*(?:to|-)\\s*(\\w+\\s*\\d+,?\\s*\\d+)");
            Matcher periodMatcher = periodPattern.matcher(text);

            if (periodMatcher.find()) {
                String startDateStr = periodMatcher.group(1);
                String endDateStr = periodMatcher.group(2);

                startDate = parseDate(startDateStr);
                endDate = parseDate(endDateStr);
            } else {
                // If statement period not found, use current month
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startDate = cal.getTime();

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = cal.getTime();
            }

            // Extract transactions
            List<Transaction> transactions = new ArrayList<>();

            // Pattern for transaction lines (date, description, amount)
            Pattern transactionPattern = Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\s+([\\w\\s&.,'\\-]+)\\s+([\\-+]?\\$?\\d+,?\\d+\\.\\d{2})");
            Matcher transactionMatcher = transactionPattern.matcher(text);

            while (transactionMatcher.find()) {
                String dateStr = transactionMatcher.group(1);
                String description = transactionMatcher.group(2).trim();
                String amountStr = transactionMatcher.group(3).replaceAll("[\\$,]", "");

                Date date = parseDate(dateStr);
                double amount = Double.parseDouble(amountStr);
                String category = categorizeTransaction(description, amount);

                Transaction transaction = new Transaction(date, description, amount, category);
                transactions.add(transaction);
            }

            // Create financial data object
            FinancialData financialData = new FinancialData();
            financialData.setStartDate(startDate);
            financialData.setEndDate(endDate);
            financialData.setTransactions(transactions);

            // Calculate totals
            double totalIncome = 0;
            double totalExpenses = 0;
            Map<String, Double> categoryTotals = new HashMap<>();

            for (Transaction transaction : transactions) {
                double amount = transaction.getAmount();
                String category = transaction.getCategory();

                if (amount > 0) {
                    totalIncome += amount;
                } else {
                    totalExpenses += amount;
                }

                double currentTotal = categoryTotals.getOrDefault(category, 0.0);
                categoryTotals.put(category, currentTotal + amount);
            }

            financialData.setTotalIncome(totalIncome);
            financialData.setTotalExpenses(totalExpenses);
            financialData.setCategoryTotals(categoryTotals);

            List<FinancialData> result = new ArrayList<>();
            result.add(financialData);
            return result;

        } finally {
            document.close();
            inputStream.close();
        }
    }

    private List<FinancialData> parseCsv(Uri csvUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(csvUri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            List<CSVRecord> records = csvParser.getRecords();

            // Determine column indices
            int dateIndex = -1;
            int descriptionIndex = -1;
            int amountIndex = -1;

            for (int i = 0; i < csvParser.getHeaderMap().size(); i++) {
                String header = csvParser.getHeaderNames().get(i).toLowerCase();

                if (header.contains("date")) {
                    dateIndex = i;
                } else if (header.contains("description") || header.contains("merchant") || header.contains("payee")) {
                    descriptionIndex = i;
                } else if (header.contains("amount") || header.contains("transaction")) {
                    amountIndex = i;
                }
            }

            if (dateIndex == -1 || descriptionIndex == -1 || amountIndex == -1) {
                throw new IOException("CSV format not recognized");
            }

            // Extract transactions
            List<Transaction> transactions = new ArrayList<>();
            Date earliestDate = null;
            Date latestDate = null;

            for (CSVRecord record : records) {
                String dateStr = record.get(dateIndex);
                String description = record.get(descriptionIndex);
                String amountStr = record.get(amountIndex).replaceAll("[\\$,]", "");

                Date date = parseDate(dateStr);
                if (date == null) continue;

                double amount = Double.parseDouble(amountStr);
                String category = categorizeTransaction(description, amount);

                Transaction transaction = new Transaction(date, description, amount, category);
                transactions.add(transaction);

                // Track statement period
                if (earliestDate == null || date.before(earliestDate)) {
                    earliestDate = date;
                }
                if (latestDate == null || date.after(latestDate)) {
                    latestDate = date;
                }
            }

            // Create financial data object
            FinancialData financialData = new FinancialData();
            financialData.setStartDate(earliestDate);
            financialData.setEndDate(latestDate);
            financialData.setTransactions(transactions);

            // Calculate totals
            double totalIncome = 0;
            double totalExpenses = 0;
            Map<String, Double> categoryTotals = new HashMap<>();

            for (Transaction transaction : transactions) {
                double amount = transaction.getAmount();
                String category = transaction.getCategory();

                if (amount > 0) {
                    totalIncome += amount;
                } else {
                    totalExpenses += amount;
                }

                double currentTotal = categoryTotals.getOrDefault(category, 0.0);
                categoryTotals.put(category, currentTotal + amount);
            }

            financialData.setTotalIncome(totalIncome);
            financialData.setTotalExpenses(totalExpenses);
            financialData.setCategoryTotals(categoryTotals);

            List<FinancialData> result = new ArrayList<>();
            result.add(financialData);
            return result;

        } finally {
            reader.close();
            inputStream.close();
        }
    }

    private Date parseDate(String dateStr) {
        for (SimpleDateFormat format : dateFormats) {
            try {
                return format.parse(dateStr);
            } catch (ParseException e) {
                // Try next format
            }
        }
        return null;
    }

    private String categorizeTransaction(String description, double amount) {
        // Default categories
        if (amount > 0) {
            return "Income";
        }

        // Check for category keywords
        String lowerDesc = description.toLowerCase();
        for (Map.Entry<String, String> entry : categoryKeywords.entrySet()) {
            if (lowerDesc.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Default category for expenses
        return "Miscellaneous";
    }
}