package com.example.statementanalyzer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.statementanalyzer.R;
import com.example.statementanalyzer.adapters.OnboardingAdapter;
import com.example.statementanalyzer.data.PreferenceManager;
import com.example.statementanalyzer.model.OnboardingItem;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private OnboardingAdapter onboardingAdapter;
    private LinearLayout indicatorsContainer;
    private Button nextButton;
    private Button skipButton;
    private ViewPager2 onboardingViewPager;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize views
        indicatorsContainer = findViewById(R.id.indicatorsContainer);
        nextButton = findViewById(R.id.nextButton);
        skipButton = findViewById(R.id.skipButton);
        onboardingViewPager = findViewById(R.id.onboardingViewPager);

        // Initialize preference manager
        preferenceManager = new PreferenceManager(this);

        // Setup onboarding items
        setupOnboardingItems();

        // Setup onboarding adapter
        onboardingAdapter = new OnboardingAdapter(getOnboardingItems());
        onboardingViewPager.setAdapter(onboardingAdapter);

        // Setup indicators
        setupIndicators();
        setCurrentIndicator(0);

        // Setup ViewPager callback
        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);

                // Update button text for last page
                if (position == onboardingAdapter.getItemCount() - 1) {
                    nextButton.setText("Get Started");
                } else {
                    nextButton.setText("Next");
                }

                // Animate button
                nextButton.setAlpha(0f);
                nextButton.animate().alpha(1f).setDuration(300).start();
            }
        });

        // Setup button click listeners
        nextButton.setOnClickListener(v -> {
            if (onboardingViewPager.getCurrentItem() < onboardingAdapter.getItemCount() - 1) {
                onboardingViewPager.setCurrentItem(onboardingViewPager.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        skipButton.setOnClickListener(v -> finishOnboarding());
    }

    private void setupOnboardingItems() {
        // Apply enter transition animation
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private List<OnboardingItem> getOnboardingItems() {
        List<OnboardingItem> onboardingItems = new ArrayList<>();

        OnboardingItem item1 = new OnboardingItem();
        item1.setTitle("Upload Statements");
        item1.setDescription("Upload your financial statements in PDF or CSV format to extract and analyze your financial data.");
        item1.setImageResId(R.drawable.onboarding_upload);

        OnboardingItem item2 = new OnboardingItem();
        item2.setTitle("Visualize Your Finances");
        item2.setDescription("See your spending patterns and financial trends through interactive charts and graphs.");
        item2.setImageResId(R.drawable.onboarding_charts);

        OnboardingItem item3 = new OnboardingItem();
        item3.setTitle("AI-Powered Insights");
        item3.setDescription("Get personalized financial insights and recommendations powered by artificial intelligence.");
        item3.setImageResId(R.drawable.onboarding_ai);

        OnboardingItem item4 = new OnboardingItem();
        item4.setTitle("Ask Questions");
        item4.setDescription("Chat with our AI assistant to get answers about your finances and spending habits.");
        item4.setImageResId(R.drawable.onboarding_chat);

        onboardingItems.add(item1);
        onboardingItems.add(item2);
        onboardingItems.add(item3);
        onboardingItems.add(item4);

        return onboardingItems;
    }

    private void setupIndicators() {
        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            indicatorsContainer.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int index) {
        int childCount = indicatorsContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) indicatorsContainer.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.indicator_active
                ));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.indicator_inactive
                ));
            }
        }
    }

    private void finishOnboarding() {
        preferenceManager.setFirstLaunchCompleted();
        startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}