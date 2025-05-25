package com.example.myapplication.model;

/**
 * Represents a node in the interactive story for Transilvania.
 * Each node contains the story text, available choices, and next node IDs.
 */
public class TransilvaniaStoryNode {
    private int id;
    private String content;
    private String title;
    private String context;
    private String[] choices;
    private int[] nextNodes;
    private int pointsReward;
    private String correctAnswer;
    private String feedback;

    private TransilvaniaStoryNode(int id, String content, String title, String context, String[] choices, int[] nextNodes, 
                                int pointsReward, String correctAnswer, String feedback) {
        this.id = id;
        this.content = content;
        this.title = title;
        this.context = context;
        this.choices = choices;
        this.nextNodes = nextNodes;
        this.pointsReward = pointsReward;
        this.correctAnswer = correctAnswer;
        this.feedback = feedback;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getContext() {
        return context;
    }

    public String[] getChoices() {
        return choices;
    }

    public int[] getNextNodes() {
        return nextNodes;
    }

    public int getPointsReward() {
        return pointsReward;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getFeedback() {
        return feedback;
    }

    /**
     * Builder class for creating StoryNode instances
     */
    public static class Builder {
        private int id;
        private String content;
        private String title;
        private String context;
        private String[] choices;
        private int[] nextNodes;
        private int pointsReward;
        private String correctAnswer;
        private String feedback;

        public Builder(int id, String content) {
            this.id = id;
            this.content = content;
        }
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder context(String context) {
            this.context = context;
            return this;
        }

        public Builder choices(String[] choices) {
            this.choices = choices;
            return this;
        }

        public Builder nextNodes(int[] nextNodes) {
            this.nextNodes = nextNodes;
            return this;
        }

        public Builder pointsReward(int pointsReward) {
            this.pointsReward = pointsReward;
            return this;
        }

        public Builder correctAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
            return this;
        }

        public Builder feedback(String feedback) {
            this.feedback = feedback;
            return this;
        }

        public TransilvaniaStoryNode build() {
            return new TransilvaniaStoryNode(id, content, title, context, choices, nextNodes, pointsReward, correctAnswer, feedback);
        }
    }
} 