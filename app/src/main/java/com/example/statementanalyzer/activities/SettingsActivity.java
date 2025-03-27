package com.example.statementanalyzer.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.statementanalyzer.R;
import com.example.statementanalyzer.data.ExportManager;
import com.example.statementanalyzer.data.FirebaseManager;
import com.example.statementanalyzer.data.PreferenceManager;
import com.example.statementanalyzer.utils.ThemeUtils;

public class SettingsActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private FirebaseManager firebaseManager;
    private ExportManager exportManager;

    private Switch darkModeSwitch;
    private Switch notificationsSwitch;
    private Button exportCsvButton;
    private Button exportJsonButton;
    private Button clearDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize managers
        preferenceManager = new PreferenceManager(this);
        firebaseManager = new FirebaseManager();
        exportManager = new ExportManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        // Initialize UI components
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        exportCsvButton = findViewById(R.id.exportCsvButton);
        exportJsonButton = findViewById(R.id.exportJsonButton);
        clearDataButton = findViewById(R.id.clearDataButton);

        // Set initial switch states
        darkModeSwitch.setChecked(preferenceManager.isDarkModeEnabled());
        notificationsSwitch.setChecked(preferenceManager.isNotificationEnabled());

        // Setup click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Dark mode switch
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setDarkModeEnabled(isChecked);
            ThemeUtils.applyTheme(this);
            recreate();
        });

        // Notifications switch
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setNotificationEnabled(isChecked);
            Toast.makeText(
                    SettingsActivity.this,
                    isChecked ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Export to CSV button
        exportCsvButton.setOnClickListener(v -> {
            Toast.makeText(this, "Exporting data to CSV...", Toast.LENGTH_SHORT).show();

            firebaseManager.fetchFinancialData(financialDataList -> {
                if (financialDataList.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(
                                SettingsActivity.this,
                                "No data to export",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
                    return;
                }

                exportManager.exportToCSV(financialDataList, new ExportManager.ExportCallback() {
                    @Override
                    public void onSuccess(android.net.Uri fileUri) {
                        runOnUiThread(() -> {
                            Toast.makeText(
                                    SettingsActivity.this,
                                    "Export successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            exportManager.shareFile(fileUri, "Share CSV File");
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            Toast.makeText(
                                    SettingsActivity.this,
                                    "Export failed: " + errorMessage,
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                });
            });
        });

        // Export to JSON button
        exportJsonButton.setOnClickListener(v -> {
            Toast.makeText(this, "Exporting data to JSON...", Toast.LENGTH_SHORT).show();

            firebaseManager.fetchFinancialData(financialDataList -> {
                if (financialDataList.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(
                                SettingsActivity.this,
                                "No data to export",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
                    return;
                }

                exportManager.exportToJSON(financialDataList, new ExportManager.ExportCallback() {
                    @Override
                    public void onSuccess(android.net.Uri fileUri) {
                        runOnUiThread(() -> {
                            Toast.makeText(
                                    SettingsActivity.this,
                                    "Export successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            exportManager.shareFile(fileUri, "Share JSON File");
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            Toast.makeText(
                                    SettingsActivity.this,
                                    "Export failed: " + errorMessage,
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                });
            });
        });

        // Clear data button
        clearDataButton.setOnClickListener(v -> {
            // Show confirmation dialog
            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Clear Data");
            builder.setMessage("Are you sure you want to clear all your financial data? This action cannot be undone.");
            builder.setPositiveButton("Clear", (dialog, which) -> {
                // Clear data from Firebase
                firebaseManager.clearAllData(success -> {
                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(
                                    SettingsActivity.this,
                                    "All data cleared successfully",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            Toast.makeText(
                                    SettingsActivity.this,
                                    "Failed to clear data",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                });
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}