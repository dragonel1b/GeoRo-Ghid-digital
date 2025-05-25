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
    
    /**
     * Gets all answers (correct + incorrect) as a single array
     */
    public String[] getAnswers() {
        String[] allAnswers = new String[incorrectAnswers.length + 1];
        allAnswers[0] = correctAnswer;
        System.arraycopy(incorrectAnswers, 0, allAnswers, 1, incorrectAnswers.length);
        return allAnswers;
    }
    
    /**
     * Returns the index of the correct answer (always 0 as per current implementation)
     */
    public int getCorrectAnswerIndex() {
        return 0; // Correct answer is always at index 0 in the getAnswers() array
    }
} 