package com.example.myapplication.core.domain.model;

import java.util.function.Function;

/**
 * Represents a node in the interactive story for Banat.
 * Each node contains story title, text, context, available choices, and logic for determining the next node.
 */
public class BanatStoryNode {
    private String title;
    private String text;
    private String context;
    private String imageResourceName;
    private String choiceText1;
    private String choiceText2;
    private Function<Integer, Integer> nextSceneFunction; // Takes choice index (0 or 1) and returns next scene index

    /**
     * Creates a story node with two choices
     */
    public BanatStoryNode(String title, String text, String context, String imageResourceName, 
                         String choiceText1, String choiceText2, Function<Integer, Integer> nextSceneFunction) {
        this.title = title;
        this.text = text;
        this.context = context;
        this.imageResourceName = imageResourceName;
        this.choiceText1 = choiceText1;
        this.choiceText2 = choiceText2;
        this.nextSceneFunction = nextSceneFunction;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getContext() {
        return context;
    }

    public String getImageResourceName() {
        return imageResourceName;
    }

    public String getChoiceText1() {
        return choiceText1;
    }

    public String getChoiceText2() {
        return choiceText2;
    }

    public Function<Integer, Integer> getNextSceneFunction() {
        return nextSceneFunction;
    }
    
    /**
     * Determines if this node is an interactive scene that requires special handling
     */
    public boolean isInteractiveScene() {
        // A node is interactive if it has no choices but requires user interaction
        return choiceText1 == null && choiceText2 == null && imageResourceName != null && 
               (imageResourceName.contains("interactive") || imageResourceName.contains("quiz"));
    }
    
    /**
     * Gets the next scene index for choice 1, or -1 if not specified
     */
    public int getChoiceNextScene1() {
        if (nextSceneFunction != null) {
            return nextSceneFunction.apply(0);
        }
        return -1;
    }
    
    /**
     * Gets the next scene index for choice 2, or -1 if not specified
     */
    public int getChoiceNextScene2() {
        if (nextSceneFunction != null) {
            return nextSceneFunction.apply(1);
        }
        return -1;
    }
} 