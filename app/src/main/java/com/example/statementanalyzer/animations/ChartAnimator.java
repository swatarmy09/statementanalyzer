package com.example.statementanalyzer.animations;

import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class ChartAnimator {

    // Animate pie chart with sequential segment appearance
    public static void animatePieChartSequential(PieChart pieChart, PieData finalData, int duration) {
        PieDataSet dataSet = (PieDataSet) finalData.getDataSet();
        List<PieEntry> entries = dataSet.getValues();

        // Start with empty values
        List<PieEntry> animatedEntries = new ArrayList<>();
        for (PieEntry entry : entries) {
            animatedEntries.add(new PieEntry(0f, entry.getLabel()));
        }

        PieDataSet animatedDataSet = new PieDataSet(animatedEntries, dataSet.getLabel());
        animatedDataSet.setColors(dataSet.getColors());
        animatedDataSet.setValueTextSize(dataSet.getValueTextSize());
        animatedDataSet.setValueTextColor(dataSet.getValueTextColor());

        PieData animatedData = new PieData(animatedDataSet);
        pieChart.setData(animatedData);
        pieChart.invalidate();

        // Animate each segment sequentially
        for (int i = 0; i < entries.size(); i++) {
            final int index = i;
            final float targetValue = entries.get(i).getValue();

            ValueAnimator animator = ValueAnimator.ofFloat(0f, targetValue);
            animator.setDuration(duration);
            animator.setStartDelay(i * (duration / 3));
            animator.setInterpolator(new DecelerateInterpolator());

            animator.addUpdateListener(animation -> {
                float animatedValue = (float) animation.getAnimatedValue();
                animatedEntries.set(index, new PieEntry(animatedValue, entries.get(index).getLabel())); // Fixed setValue issue
                animatedDataSet.setValues(animatedEntries);  // Update dataset
                pieChart.getData().notifyDataChanged();
                pieChart.notifyDataSetChanged();
                pieChart.invalidate();
            });

            animator.start();
        }
    }

    // Animate bar chart with growing bars
    public static void animateBarChartGrowing(BarChart barChart, BarData finalData, int duration) {
        BarDataSet dataSet = (BarDataSet) finalData.getDataSetByIndex(0);
        List<BarEntry> entries = dataSet.getValues();

        // Start with zero height bars
        List<BarEntry> animatedEntries = new ArrayList<>();
        for (BarEntry entry : entries) {
            animatedEntries.add(new BarEntry(entry.getX(), 0f));
        }

        BarDataSet animatedDataSet = new BarDataSet(animatedEntries, dataSet.getLabel());
        animatedDataSet.setColors(dataSet.getColors());
        animatedDataSet.setValueTextSize(dataSet.getValueTextSize());

        BarData animatedData = new BarData(animatedDataSet);
        animatedData.setBarWidth(finalData.getBarWidth());

        barChart.setData(animatedData);
        barChart.invalidate();

        // Animate all bars simultaneously
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float animatedFraction = animation.getAnimatedFraction();

            for (int i = 0; i < entries.size(); i++) {
                float targetValue = entries.get(i).getY();
                animatedEntries.set(i, new BarEntry(entries.get(i).getX(), targetValue * animatedFraction));
            }

            animatedDataSet.setValues(animatedEntries);
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
            barChart.invalidate();
        });

        animator.start();
    }

    // Animate line chart with drawing effect
    public static void animateLineChartDrawing(LineChart lineChart, LineData finalData, int duration) {
        LineDataSet dataSet = (LineDataSet) finalData.getDataSetByIndex(0);
        List<Entry> entries = dataSet.getValues();

        // Set data and enable animation
        lineChart.setData(finalData);
        lineChart.animateX(duration);
    }
}
