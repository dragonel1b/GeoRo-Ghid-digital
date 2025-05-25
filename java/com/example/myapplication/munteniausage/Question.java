package com.example.myapplication.munteniausage;

import java.util.List;

public class Question {
    private String question;
    private List<String> options;
    private int correctAnswerIndex;
    private String explanation;
    private int imageResourceId;

    public Question(String question, List<String> options, int correctAnswerIndex, String explanation, int imageResourceId) {
        this.question = question;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.explanation = explanation;
        this.imageResourceId = imageResourceId;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
} 