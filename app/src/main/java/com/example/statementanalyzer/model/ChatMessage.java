package com.example.statementanalyzer.model;

public class ChatMessage {
    private String sender;
    private String message;
    private long timestamp;
    private boolean isUser;

    public ChatMessage() {
        // Required empty constructor for Firebase
    }

    public ChatMessage(String sender, String message, long timestamp, boolean isUser) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.isUser = isUser;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }
}