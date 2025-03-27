package com.example.statementanalyzer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.statementanalyzer.R;
import com.example.statementanalyzer.model.OnboardingItem;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<OnboardingItem> onboardingItems;

    public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OnboardingViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_onboarding,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.bind(onboardingItems.get(position));
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    class OnboardingViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView titleTextView;
        private TextView descriptionTextView;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageOnboarding);
            titleTextView = itemView.findViewById(R.id.textTitle);
            descriptionTextView = itemView.findViewById(R.id.textDescription);
        }

        void bind(OnboardingItem onboardingItem) {
            imageView.setImageResource(onboardingItem.getImageResId());
            titleTextView.setText(onboardingItem.getTitle());
            descriptionTextView.setText(onboardingItem.getDescription());

            // Apply animations
            imageView.setAlpha(0f);
            titleTextView.setAlpha(0f);
            descriptionTextView.setAlpha(0f);

            imageView.setTranslationY(50f);
            titleTextView.setTranslationY(50f);
            descriptionTextView.setTranslationY(50f);

            imageView.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(100).start();
            titleTextView.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(300).start();
            descriptionTextView.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(500).start();
        }
    }
}