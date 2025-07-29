package com.example.myapplication.models;

import com.google.firebase.Timestamp;

/**
 * Model pentru curiozități sau feedback AI generate și salvate din aplicație
 */
public class CuriosityModel {
    private String id;
    private String userQuestion;
    private String aiResponse;
    private String region; // opțional, pentru filtrare
    private String type;   // ex: "curiozitate", "feedback", "istorie", etc.
    private Timestamp timestamp;
    private String source; // "AI" sau altă sursă

    public CuriosityModel() {}

    public CuriosityModel(String userQuestion, String aiResponse, String region, String type, Timestamp timestamp, String source) {
        this.userQuestion = userQuestion;
        this.aiResponse = aiResponse;
        this.region = region;
        this.type = type;
        this.timestamp = timestamp;
        this.source = source;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserQuestion() { return userQuestion; }
    public void setUserQuestion(String userQuestion) { this.userQuestion = userQuestion; }

    public String getAiResponse() { return aiResponse; }
    public void setAiResponse(String aiResponse) { this.aiResponse = aiResponse; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
} 