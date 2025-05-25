package com.example.myapplication.models;

/**
 * Model class for quiz questions used in the game activities
 */
public class QuestionModel {
    private String question;
    private String correctAnswer;
    private String[] incorrectAnswers;
    private int imageResourceId;
    private String fact;

    /**
     * Constructor for a question with image and fact
     */
    public QuestionModel(String question, String correctAnswer, String[] incorrectAnswers, 
                          int imageResourceId, String fact) {
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers;
        this.imageResourceId = imageResourceId;
        this.fact = fact;
    }

    /**
     * Constructor for a question with only image
     */
    public QuestionModel(String question, String correctAnswer, String[] incorrectAnswers, 
                          int imageResourceId) {
        this(question, correctAnswer, incorrectAnswers, imageResourceId, "");
    }

    /**
     * Constructor for a question without image or fact
     */
    public QuestionModel(String question, String correctAnswer, String[] incorrectAnswers) {
        this(question, correctAnswer, incorrectAnswers, 0, "");
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String[] getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String getFact() {
        return fact;
    }
} 