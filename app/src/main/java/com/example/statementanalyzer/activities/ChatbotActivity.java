package com.example.statementanalyzer.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.statementanalyzer.R;
import com.example.statementanalyzer.adapters.ChatAdapter;
import com.example.statementanalyzer.ai.CohereManager;
import com.example.statementanalyzer.animations.ViewAnimations;
import com.example.statementanalyzer.data.FirebaseManager;
import com.example.statementanalyzer.model.ChatMessage;
import com.example.statementanalyzer.model.FinancialData;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private CohereManager cohereManager;
    private List<FinancialData> financialDataList;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize components
        firebaseManager = new FirebaseManager();
        cohereManager = new CohereManager(getString(R.string.cohere_api_key));
        chatMessages = new ArrayList<>();

        // Initialize UI components
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Apply enter transition animation
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        // Load financial data
        loadFinancialData();

        // Setup click listeners
        sendButton.setOnClickListener(v -> {
            ViewAnimations.pulse(v);
            sendMessage();
        });
    }

    private void loadFinancialData() {
        progressBar.setVisibility(View.VISIBLE);

        firebaseManager.fetchFinancialData(financialDataList -> {
            this.financialDataList = financialDataList;

            if (financialDataList.isEmpty()) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(
                            ChatbotActivity.this,
                            "No financial data available. Please upload a statement first.",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                });
                return;
            }

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);

                // Add welcome message
                ChatMessage welcomeMessage = new ChatMessage(
                        "AI Assistant",
                        "Hello! I'm your financial assistant. I can answer questions about your financial data. What would you like to know?",
                        System.currentTimeMillis(),
                        false
                );
                chatMessages.add(welcomeMessage);
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);

                // Animate welcome message
                ViewAnimations.fadeIn(
                        chatRecyclerView.findViewHolderForAdapterPosition(0).itemView,
                        500,
                        0
                );
            });
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        // Add user message
        ChatMessage userMessage = new ChatMessage(
                "You",
                messageText,
                System.currentTimeMillis(),
                true
        );
        chatMessages.add(userMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);

        // Clear input
        messageEditText.setText("");

        // Scroll to bottom
        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);

        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);

        // Get AI response
        cohereManager.getChatResponse(messageText, financialDataList, new CohereManager.CohereCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    // Add AI message
                    ChatMessage aiMessage = new ChatMessage(
                            "AI Assistant",
                            response,
                            System.currentTimeMillis(),
                            false
                    );
                    chatMessages.add(aiMessage);
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);

                    // Scroll to bottom
                    chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);

                    // Animate new message
                    ViewAnimations.fadeIn(
                            chatRecyclerView.findViewHolderForAdapterPosition(chatMessages.size() - 1).itemView,
                            500,
                            0
                    );
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    // Add error message
                    ChatMessage errorMsg = new ChatMessage(
                            "AI Assistant",
                            "Sorry, I encountered an error: " + errorMessage,
                            System.currentTimeMillis(),
                            false
                    );
                    chatMessages.add(errorMsg);
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);

                    // Scroll to bottom
                    chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                });
            }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}