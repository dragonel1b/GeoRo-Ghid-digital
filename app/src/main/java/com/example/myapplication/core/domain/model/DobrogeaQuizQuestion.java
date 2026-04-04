package com.example.myapplication.core.domain.model;

public class DobrogeaQuizQuestion {
    private String question;
    private String[] options;
    private int correctAnswerIndex;
    private int pointsReward;

    public DobrogeaQuizQuestion(String question, String[] options, int correctIndex, int points) {
        this.question = question;
        this.options = options;
        this.correctAnswerIndex = correctIndex;
        this.pointsReward = points;
    }

    // Getters
    public String getQuestion() { return question; }
    public String[] getOptions() { return options; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public int getPointsReward() { return pointsReward; }
}
