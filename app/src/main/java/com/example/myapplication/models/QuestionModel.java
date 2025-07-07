package com.example.myapplication.models;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Model class for quiz questions used in the game activities
 */
public class QuestionModel {
    private String question;
    private String correctAnswer;
    private List<String> incorrectAnswers;
    private int imageResourceId;
    private String fact;

    /**
     * Constructor gol necesar pentru Firestore
     */
    public QuestionModel() {
        this.incorrectAnswers = new ArrayList<>();
    }

    /**
     * Constructor for a question with image and fact
     */
    public QuestionModel(String question, String correctAnswer, List<String> incorrectAnswers, 
                          int imageResourceId, String fact) {
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers != null ? incorrectAnswers : new ArrayList<>();
        this.imageResourceId = imageResourceId;
        this.fact = fact;
    }

    /**
     * Constructor for a question with only image
     */
    public QuestionModel(String question, String correctAnswer, int imageResourceId) {
        this(question, correctAnswer, null, imageResourceId, "");
    }

    /**
     * Constructor for a question without image or fact
     */
    public QuestionModel(String question, String correctAnswer) {
        this(question, correctAnswer, null, 0, "");
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(List<String> incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers != null ? incorrectAnswers : new ArrayList<>();
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public String getFact() {
        return fact;
    }

    public void setFact(String fact) {
        this.fact = fact;
    }
    
    /**
     * Gets all answers (correct + incorrect) as a single List
     */
    public List<String> getAnswers() {
        List<String> allAnswers = new ArrayList<>();
        allAnswers.add(correctAnswer);
        allAnswers.addAll(incorrectAnswers);
        return allAnswers;
    }
    
    /**
     * Returns the index of the correct answer (always 0 as per current implementation)
     */
    public int getCorrectAnswerIndex() {
        return 0; // Correct answer is always at index 0 in the getAnswers() list
    }
} 