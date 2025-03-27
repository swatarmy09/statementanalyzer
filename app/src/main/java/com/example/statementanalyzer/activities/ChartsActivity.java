package com.example.statementanalyzer.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.statementanalyzer.R;
import com.example.statementanalyzer.ai.CohereManager;
import com.example.statementanalyzer.animations.ChartAnimator;
import com.example.statementanalyzer.animations.ViewAnimations;
import com.example.statementanalyzer.data.FirebaseManager;
import com.example.statementanalyzer.model.FinancialData;
import com.example.statementanalyzer.model.Transaction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ChartsActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private CohereManager cohereManager;
    private List<FinancialData> financialDataList;

    private PieChart categoryPieChart;
    private BarChart expensesBarChart;
    private LineChart trendLineChart;
    private TextView analysisTextView;
    private View progressBar;

    // Theme colors
    private final int primaryColor = Color.parseColor("#A64D79");
    private final int secondaryColor = Color.parseColor("#6A1E55");
    private final int tertiaryColor = Color.parseColor("#3B1C32");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize components
        firebaseManager = new FirebaseManager();
        cohereManager = new CohereManager(getString(R.string.cohere_api_key));

        // Initialize UI components
        categoryPieChart = findViewById(R.id.categoryPieChart);
        expensesBarChart = findViewById(R.id.expensesBarChart);
        trendLineChart = findViewById(R.id.trendLineChart);
        analysisTextView = findViewById(R.id.analysisTextView);
        progressBar = findViewById(R.id.progressBar);

        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);

        // Apply enter transition animation
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        // Load data and generate charts
        loadFinancialData();
    }

    private void loadFinancialData() {
        firebaseManager.fetchFinancialData(financialDataList -> {
            this.financialDataList = financialDataList;

            if (financialDataList.isEmpty()) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    analysisTextView.setText("No financial data available. Please upload a statement first.");
                });
                return;
            }

            runOnUiThread(() -> {
                // Generate charts with animations
                generateCategoryPieChart();
                generateExpensesBarChart();
                generateTrendLineChart();

                // Generate AI analysis
                generateAIAnalysis();
            });
        });
    }

    private void generateCategoryPieChart() {
        // Aggregate category data from all financial data
        Map<String, Double> categoryTotals = new HashMap<>();

        for (FinancialData data : financialDataList) {
            for (Map.Entry<String, Double> entry : data.getCategoryTotals().entrySet()) {
                String category = entry.getKey();
                double amount = entry.getValue();

                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            }
        }

        // Create pie chart entries
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
        dataSet.setColors(getCustomColors());
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        // Create pie data
        PieData pieData = new PieData(dataSet);

        // Configure chart
        categoryPieChart.getDescription().setEnabled(false);
        categoryPieChart.setCenterText("Expense Categories");
        categoryPieChart.setCenterTextSize(16f);
        categoryPieChart.setHoleRadius(40f);
        categoryPieChart.setTransparentCircleRadius(45f);
        categoryPieChart.setEntryLabelTextSize(12f);
        categoryPieChart.setEntryLabelColor(Color.WHITE);
        categoryPieChart.getLegend().setEnabled(true);
        categoryPieChart.getLegend().setTextSize(12f);

        // Apply sequential animation
        ChartAnimator.animatePieChartSequential(categoryPieChart, pieData, 1200);
    }

    private void generateExpensesBarChart() {
        // Get top 5 expense categories
        Map<String, Double> categoryTotals = new HashMap<>();

        for (FinancialData data : financialDataList) {
            for (Map.Entry<String, Double> entry : data.getCategoryTotals().entrySet()) {
                String category = entry.getKey();
                double amount = entry.getValue();

                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            }
        }

        // Sort categories by amount
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(categoryTotals.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Take top 5 categories
        int count = Math.min(5, sortedEntries.size());
        List<String> categories = new ArrayList<>();
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Map.Entry<String, Double> entry = sortedEntries.get(i);
            categories.add(entry.getKey());
            entries.add(new BarEntry(i, entry.getValue().floatValue()));
        }

        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Top Expense Categories");
        dataSet.setColors(getCustomColors());
        dataSet.setValueTextSize(12f);

        // Create bar data
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        // Configure chart
        expensesBarChart.getDescription().setEnabled(false);
        expensesBarChart.getLegend().setEnabled(false);

        // Configure X axis
        XAxis xAxis = expensesBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(categories));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);

        // Configure Y axis
        YAxis leftAxis = expensesBarChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextSize(10f);

        expensesBarChart.getAxisRight().setEnabled(false);
        expensesBarChart.setFitBars(true);

        // Apply growing animation
        ChartAnimator.animateBarChartGrowing(expensesBarChart, barData, 1500);
    }

    private void generateTrendLineChart() {
        // Aggregate daily expenses
        Map<Date, Double> dailyExpenses = new TreeMap<>();

        for (FinancialData data : financialDataList) {
            for (Transaction transaction : data.getTransactions()) {
                if (transaction.getAmount() < 0) {  // Only expenses
                    // Normalize date to remove time component
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(transaction.getDate());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    Date normalizedDate = cal.getTime();

                    double amount = Math.abs(transaction.getAmount());
                    dailyExpenses.put(normalizedDate,
                            dailyExpenses.getOrDefault(normalizedDate, 0.0) + amount);
                }
            }
        }

        // Create line chart entries
        List<Entry> entries = new ArrayList<>();
        List<String> dateLabels = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.US);

        int index = 0;
        for (Map.Entry<Date, Double> entry : dailyExpenses.entrySet()) {
            entries.add(new Entry(index, entry.getValue().floatValue()));
            dateLabels.add(dateFormat.format(entry.getKey()));
            index++;
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Daily Expenses");
        dataSet.setColor(primaryColor);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(secondaryColor);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(tertiaryColor);
        dataSet.setFillAlpha(80);

        // Create line data
        LineData lineData = new LineData(dataSet);

        // Configure chart
        trendLineChart.setData(lineData);
        trendLineChart.getDescription().setEnabled(false);

        // Configure X axis
        XAxis xAxis = trendLineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45f);
        xAxis.setTextSize(10f);

        // Configure Y axis
        YAxis leftAxis = trendLineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextSize(10f);

        trendLineChart.getAxisRight().setEnabled(false);
        trendLineChart.getLegend().setEnabled(true);

        // Apply drawing animation
        ChartAnimator.animateLineChartDrawing(trendLineChart, lineData, 2000);
    }

    private void generateAIAnalysis() {
        // Ensure analysisTextView exists
        if (analysisTextView == null) {
            return;
        }

        // Show progress bar while fetching AI-generated analysis
        progressBar.setVisibility(View.VISIBLE);

        // Call AI to generate analysis
        cohereManager.generateFinancialStory(financialDataList, new CohereManager.CohereCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    analysisTextView.setText(response);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    analysisTextView.setText("Error generating analysis: " + errorMessage);
                });
            }
        });
    }


    private int[] getCustomColors() {
        // Create a color array with theme colors and additional colors
        int[] colors = new int[]{
                primaryColor,
                secondaryColor,
                tertiaryColor,
                Color.parseColor("#D295BF"),
                Color.parseColor("#8D5477"),
                Color.rgb(64, 89, 128),
                Color.rgb(149, 165, 124),
                Color.rgb(217, 184, 162),
                Color.rgb(191, 134, 134),
                Color.rgb(179, 48, 80)
        };

        return colors;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}