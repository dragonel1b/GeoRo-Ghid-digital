package com.example.myapplication.model;

import java.util.List;

/**
 * Clasa model pentru întrebări în jocurile de quiz
 */
public class Question {
    private String question;
    private List<String> options;
    private int correctAnswerIndex;
    private String fact;
    private int imageResourceId;

    /**
     * Constructor pentru crearea unei întrebări
     *
     * @param question Textul întrebării
     * @param options Lista de opțiuni de răspuns
     * @param correctAnswerIndex Indexul răspunsului corect (0-based)
     * @param fact Informația suplimentară despre răspuns
     * @param imageResourceId ID-ul resursei pentru imagine
     */
    public Question(String question, List<String> options, int correctAnswerIndex, String fact, int imageResourceId) {
        this.question = question;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.fact = fact;
        this.imageResourceId = imageResourceId;
    }

    /**
     * @return Textul întrebării
     */
    public String getQuestion() {
        return question;
    }

    /**
     * @param question Textul întrebării
     */
    public void setQuestion(String question) {
        this.question = question;
    }

    /**
     * @return Lista de opțiuni de răspuns
     */
    public List<String> getOptions() {
        return options;
    }

    /**
     * @param options Lista de opțiuni de răspuns
     */
    public void setOptions(List<String> options) {
        this.options = options;
    }

    /**
     * @return Indexul răspunsului corect
     */
    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    /**
     * @param correctAnswerIndex Indexul răspunsului corect
     */
    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    /**
     * @return Informația suplimentară despre răspuns
     */
    public String getFact() {
        return fact;
    }

    /**
     * @param fact Informația suplimentară despre răspuns
     */
    public void setFact(String fact) {
        this.fact = fact;
    }

    /**
     * @return ID-ul resursei pentru imagine
     */
    public int getImageResourceId() {
        return imageResourceId;
    }

    /**
     * @param imageResourceId ID-ul resursei pentru imagine
     */
    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }
} 