package com.example.statementanalyzer.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.statementanalyzer.R;
import com.example.statementanalyzer.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatMessages;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        holder.senderTextView.setText(message.getSender());
        holder.messageTextView.setText(message.getMessage());

        // Align messages based on sender
        if (message.isUser()) {
            // User message - right aligned
            holder.messageCardView.setCardBackgroundColor(Color.parseColor("#A64D79"));
            holder.messageTextView.setTextColor(Color.WHITE);

            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) holder.messageCardView.getLayoutParams();
            layoutParams.setMarginStart(80);
            layoutParams.setMarginEnd(8);
            holder.messageCardView.setLayoutParams(layoutParams);

            holder.senderTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

            ViewGroup.MarginLayoutParams senderLayoutParams =
                    (ViewGroup.MarginLayoutParams) holder.senderTextView.getLayoutParams();
            senderLayoutParams.setMarginStart(0);
            senderLayoutParams.setMarginEnd(12);
            holder.senderTextView.setLayoutParams(senderLayoutParams);
        } else {
            // AI message - left aligned
            holder.messageCardView.setCardBackgroundColor(Color.parseColor("#E1E1E1"));
            holder.messageTextView.setTextColor(Color.BLACK);

            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) holder.messageCardView.getLayoutParams();
            layoutParams.setMarginStart(8);
            layoutParams.setMarginEnd(80);
            holder.messageCardView.setLayoutParams(layoutParams);

            holder.senderTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

            ViewGroup.MarginLayoutParams senderLayoutParams =
                    (ViewGroup.MarginLayoutParams) holder.senderTextView.getLayoutParams();
            senderLayoutParams.setMarginStart(12);
            senderLayoutParams.setMarginEnd(0);
            holder.senderTextView.setLayoutParams(senderLayoutParams);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView;
        TextView messageTextView;
        CardView messageCardView;


        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}