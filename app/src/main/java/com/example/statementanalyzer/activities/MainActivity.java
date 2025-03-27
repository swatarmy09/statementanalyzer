package com.example.statementanalyzer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.statementanalyzer.R;
import com.example.statementanalyzer.animations.ViewAnimations;
import com.example.statementanalyzer.data.FirebaseManager;
import com.example.statementanalyzer.data.PreferenceManager;
import com.example.statementanalyzer.extraction.DocumentParser;
import com.example.statementanalyzer.fragments.HelpDialogFragment;
import com.example.statementanalyzer.model.FinancialData;
import com.example.statementanalyzer.utils.NotificationUtils;
import com.example.statementanalyzer.utils.ThemeUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private DocumentParser documentParser;
    private ActivityResultLauncher<String[]> filePickerLauncher;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply theme
        preferenceManager = new PreferenceManager(this);
        ThemeUtils.applyTheme(this);

        setContentView(R.layout.activity_main);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create notification channel
        NotificationUtils.createNotificationChannel(this);

        // Initialize components
        firebaseManager = new FirebaseManager();
        documentParser = new DocumentParser(this);

        // Setup UI components
        Button uploadButton = findViewById(R.id.uploadButton);
        Button viewChartsButton = findViewById(R.id.viewChartsButton);
        Button chatbotButton = findViewById(R.id.chatbotButton);
        FloatingActionButton helpFab = findViewById(R.id.helpFab);

        // Setup file picker launcher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::handleSelectedFile
        );

        // Set click listeners
        uploadButton.setOnClickListener(v -> {
            ViewAnimations.pulse(v);
            openFilePicker();
        });

        viewChartsButton.setOnClickListener(v -> {
            ViewAnimations.pulse(v);
            openChartsActivity();
        });

        chatbotButton.setOnClickListener(v -> {
            ViewAnimations.pulse(v);
            openChatbotActivity();
        });

        helpFab.setOnClickListener(v -> {
            ViewAnimations.bounce(v);
            showHelp();
        });

        // Check if buttons should be enabled
        checkDataAvailability();

        // Apply animations to UI elements
        animateUI();
    }

    private void animateUI() {
        // Logo animation
        View logoImageView = findViewById(R.id.logoImageView);
        ViewAnimations.fadeIn(logoImageView, 800, 300);

        // Welcome text animation
        View welcomeTextView = findViewById(R.id.welcomeTextView);
        ViewAnimations.slideInFromBottom(welcomeTextView, 800, 500);

        // Description text animation
        View descriptionTextView = findViewById(R.id.descriptionTextView);
        ViewAnimations.slideInFromBottom(descriptionTextView, 800, 700);

        // Upload card animation
        View uploadCardView = findViewById(R.id.uploadCardView);
        ViewAnimations.slideInFromBottom(uploadCardView, 800, 900);

        // Features card animation
        View featuresCardView = findViewById(R.id.featuresCardView);
        ViewAnimations.slideInFromBottom(featuresCardView, 800, 1100);

        // FAB animation
        View helpFab = findViewById(R.id.helpFab);
        ViewAnimations.fadeIn(helpFab, 800, 1300);
    }

    private void checkDataAvailability() {
        firebaseManager.fetchFinancialData(financialDataList -> {
            boolean hasData = !financialDataList.isEmpty();

            runOnUiThread(() -> {
                findViewById(R.id.viewChartsButton).setEnabled(hasData);
                findViewById(R.id.chatbotButton).setEnabled(hasData);
            });
        });
    }

    private void openFilePicker() {
        filePickerLauncher.launch(new String[]{"application/pdf", "text/csv"});
    }

    private void handleSelectedFile(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        // Process file in background
        new Thread(() -> {
            try {
                List<FinancialData> extractedData = documentParser.parseDocument(uri);

                // Upload to Firebase
                firebaseManager.uploadData(extractedData, success -> {
                    runOnUiThread(() -> {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        if (success) {
                            Toast.makeText(MainActivity.this,
                                    "Data extracted and uploaded successfully",
                                    Toast.LENGTH_SHORT).show();

                            // Enable analysis features
                            findViewById(R.id.viewChartsButton).setEnabled(true);
                            findViewById(R.id.chatbotButton).setEnabled(true);

                            // Show notification
                            NotificationUtils.showAnalysisCompleteNotification(MainActivity.this);

                            // Apply animation to buttons
                            ViewAnimations.bounce(findViewById(R.id.viewChartsButton));
                            ViewAnimations.bounce(findViewById(R.id.chatbotButton));
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Failed to upload data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this,
                            "Error processing file: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void openChartsActivity() {
        Intent intent = new Intent(this, ChartsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openChatbotActivity() {
        Intent intent = new Intent(this, ChatbotActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showHelp() {
        // Show help dialog
        HelpDialogFragment helpDialog = new HelpDialogFragment();
        helpDialog.show(getSupportFragmentManager(), "HelpDialogFragment");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
