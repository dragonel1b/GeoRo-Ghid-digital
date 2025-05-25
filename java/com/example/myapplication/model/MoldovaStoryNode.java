package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the Moldova story, containing all information needed to display a scene
 * and navigate to subsequent nodes.
 */
public class MoldovaStoryNode {
    private final int id;
    private final String storyText;
    private final String title;
    private final String context;
    private final List<Choice> choices;
    private final boolean isInteractive;
    private final String interactionType;
    private final String sceneType;

    private MoldovaStoryNode(Builder builder) {
        this.id = builder.id;
        this.storyText = builder.storyText;
        this.title = builder.title;
        this.context = builder.context;
        this.choices = builder.choices;
        this.isInteractive = builder.isInteractive;
        this.interactionType = builder.interactionType;
        this.sceneType = builder.sceneType;
    }

    public int getId() {
        return id;
    }

    public String getStoryText() {
        return storyText;
    }

    public String getTitle() {
        return title;
    }

    public String getContext() {
        return context;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public boolean isInteractive() {
        return isInteractive;
    }

    public String getInteractionType() {
        return interactionType;
    }
    
    public String getSceneType() {
        return sceneType;
    }
    
    /**
     * Represents a single choice option in a story node
     */
    public static class Choice {
        private final String text;
        private final int nextSceneIndex;
        
        public Choice(String text, int nextSceneIndex) {
            this.text = text;
            this.nextSceneIndex = nextSceneIndex;
        }
        
        public String getText() {
            return text;
        }
        
        public int getNextSceneIndex() {
            return nextSceneIndex;
        }
    }

    /**
     * Builder class for creating MoldovaStoryNode instances.
     */
    public static class Builder {
        private final int id;
        private final String storyText;
        private String title = "";
        private String context = "";
        private List<Choice> choices = new ArrayList<>();
        private boolean isInteractive = false;
        private String interactionType = "";
        private String sceneType = "landscape";

        public Builder(int id, String storyText) {
            this.id = id;
            this.storyText = storyText;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder context(String context) {
            this.context = context;
            return this;
        }

        public Builder choices(String[] choiceTexts, int[] nextNodes) {
            if (choiceTexts != null && nextNodes != null) {
                this.choices = new ArrayList<>();
                int minLength = Math.min(choiceTexts.length, nextNodes.length);
                for (int i = 0; i < minLength; i++) {
                    this.choices.add(new Choice(choiceTexts[i], nextNodes[i]));
                }
            }
            return this;
        }

        public Builder addChoice(String text, int nextNode) {
            if (this.choices == null) {
                this.choices = new ArrayList<>();
            }
            this.choices.add(new Choice(text, nextNode));
            return this;
        }

        public Builder interactive(boolean isInteractive) {
            this.isInteractive = isInteractive;
            return this;
        }

        public Builder interactionType(String interactionType) {
            this.interactionType = interactionType;
            return this;
        }
        
        public Builder sceneType(String sceneType) {
            this.sceneType = sceneType;
            return this;
        }

        public MoldovaStoryNode build() {
            return new MoldovaStoryNode(this);
        }
    }
} 