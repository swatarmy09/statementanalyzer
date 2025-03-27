package com.example.statementanalyzer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.statementanalyzer.data.PreferenceManager;

public class ThemeUtils {

    // Apply theme based on preferences
    public static void applyTheme(Context context) {
        PreferenceManager preferenceManager = new PreferenceManager(context);

        if (preferenceManager.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // Toggle dark mode
    public static void toggleDarkMode(Activity activity) {
        PreferenceManager preferenceManager = new PreferenceManager(activity);
        boolean isDarkMode = preferenceManager.isDarkModeEnabled();

        // Toggle preference
        preferenceManager.setDarkModeEnabled(!isDarkMode);

        // Apply new theme
        if (!isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Recreate activity to apply theme changes
        activity.recreate();
    }

    // Check if device is in dark mode
    public static boolean isInDarkMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }
}