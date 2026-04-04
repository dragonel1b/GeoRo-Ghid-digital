package com.example.myapplication.core.domain.model;

import java.util.List;

/**
 * Model pentru întrebările de quiz
 */
public class QuestionModel {
    private String question;
    private List<String> options;
    private int correctOptionIndex;
    private String explanation;
    private int imageResourceId;
    private String region;

    public QuestionModel(String question, List<String> options, int correctOptionIndex, String explanation, int imageResourceId, String region) {
        this.question = question;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = explanation;
        this.imageResourceId = imageResourceId;
        this.region = region;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public void setCorrectOptionIndex(int correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getFact() {
        return explanation;
    }
    
    public int getCorrectAnswerIndex() {
        return correctOptionIndex;
    }
    
    public String getCorrectAnswer() {
        if (options != null && correctOptionIndex >= 0 && correctOptionIndex < options.size()) {
            return options.get(correctOptionIndex);
        }
        return "";
    }
    
    public String[] getAnswers() {
        if (options == null) {
            return new String[0];
        }
        return options.toArray(new String[0]);
    }
} 