package com.example.myapplication.model;

public class MaramuresStoryNode {
    public enum NodeType {
        STORY,
        QUIZ,
        CHOICE,
        INTERACTIVE
    }

    private final int nodeId;
    private final String title;
    private final String storyText;
    private final String context;
    private final String[] choices;
    private final int[] nextNodes;
    private final int pointsReward;
    private final NodeType nodeType;
    private final String correctAnswer;
    private final String feedback;
    private final int imageResourceId;
    private final int soundResourceId;

    private MaramuresStoryNode(Builder builder) {
        this.nodeId = builder.nodeId;
        this.title = builder.title;
        this.storyText = builder.storyText;
        this.context = builder.context;
        this.choices = builder.choices;
        this.nextNodes = builder.nextNodes;
        this.pointsReward = builder.pointsReward;
        this.nodeType = builder.nodeType;
        this.correctAnswer = builder.correctAnswer;
        this.feedback = builder.feedback;
        this.imageResourceId = builder.imageResourceId;
        this.soundResourceId = builder.soundResourceId;
    }

    public static class Builder {
        private int nodeId;
        private String title = "";
        private String storyText;
        private String context = "";
        private String[] choices = new String[]{"Continuă"};
        private int[] nextNodes = new int[]{1};
        private int pointsReward = 0;
        private NodeType nodeType = NodeType.STORY;
        private String correctAnswer;
        private String feedback;
        private int imageResourceId = 0;
        private int soundResourceId = 0;

        public Builder(int nodeId, String storyText) {
            this.nodeId = nodeId;
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

        public Builder choices(String[] choices) {
            this.choices = choices;
            return this;
        }

        public Builder nextNodes(int[] nextNodes) {
            this.nextNodes = nextNodes;
            return this;
        }

        public Builder pointsReward(int points) {
            this.pointsReward = points;
            return this;
        }

        public Builder nodeType(NodeType type) {
            this.nodeType = type;
            return this;
        }

        public Builder correctAnswer(String answer) {
            this.correctAnswer = answer;
            return this;
        }

        public Builder feedback(String feedback) {
            this.feedback = feedback;
            return this;
        }

        public Builder imageResource(int resourceId) {
            this.imageResourceId = resourceId;
            return this;
        }

        public Builder soundResource(int resourceId) {
            this.soundResourceId = resourceId;
            return this;
        }

        public MaramuresStoryNode build() {
            validateNode();
            return new MaramuresStoryNode(this);
        }

        private void validateNode() {
            if (storyText == null || storyText.isEmpty()) {
                throw new IllegalStateException("Story text cannot be empty");
            }
            if (choices.length != nextNodes.length) {
                throw new IllegalStateException("Number of choices must match number of next nodes");
            }
            if (nodeType == NodeType.QUIZ && correctAnswer == null) {
                throw new IllegalStateException("Quiz nodes must have a correct answer");
            }
        }
    }

    // Getters
    public int getNodeId() { return nodeId; }
    public String getTitle() { return title; }
    public String getStoryText() { return storyText; }
    public String getContext() { return context; }
    public String[] getChoices() { return choices; }
    public int[] getNextNodes() { return nextNodes; }
    public int getPointsReward() { return pointsReward; }
    public NodeType getNodeType() { return nodeType; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getFeedback() { return feedback; }
    public int getImageResourceId() { return imageResourceId; }
    public int getSoundResourceId() { return soundResourceId; }

    public boolean isQuizNode() { return nodeType == NodeType.QUIZ; }
    public boolean isChoiceNode() { return nodeType == NodeType.CHOICE; }
    public boolean isInteractiveNode() { return nodeType == NodeType.INTERACTIVE; }
    public boolean hasMultipleChoices() { return choices.length > 1; }

    public int getNextNodeForChoice(int choiceIndex) {
        if (choiceIndex < 0 || choiceIndex >= nextNodes.length) {
            return nodeId + 1;
        }
        return nextNodes[choiceIndex];
    }

    public boolean isCorrectAnswer(String answer) {
        if (correctAnswer == null) return false;
        return correctAnswer.equals(answer);
    }
} 