package com.example.myapplication.core.domain.model;

/**
 * Class to represent nodes in the Bucovina interactive story.
 * Each node contains story text, choices, and metadata.
 */
public class BucovinaStoryNode {
    private int id;
    private String storyText;
    private String title;
    private String context;
    private String[] choices;
    private int[] nextNodes;
    private int imageResource;
    private int soundResource;
    private String fact;
    private boolean isEndNode;
    private int rewardPoints;

    private BucovinaStoryNode(Builder builder) {
        this.id = builder.id;
        this.storyText = builder.storyText;
        this.title = builder.title;
        this.context = builder.context;
        this.choices = builder.choices;
        this.nextNodes = builder.nextNodes;
        this.imageResource = builder.imageResource;
        this.soundResource = builder.soundResource;
        this.fact = builder.fact;
        this.isEndNode = builder.isEndNode;
        this.rewardPoints = builder.rewardPoints;
    }

    public int getId() {
        return id;
    }

    public String getStoryText() {
        return storyText;
    }
    
    public String getText() {
        return storyText;
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

    public int getImageResource() {
        return imageResource;
    }

    public int getSoundResource() {
        return soundResource;
    }

    public String getFact() {
        return fact;
    }
    
    public boolean isEndNode() {
        return isEndNode;
    }
    
    public int getRewardPoints() {
        return rewardPoints;
    }

    public boolean hasChoices() {
        return choices != null && choices.length > 0;
    }

    public boolean hasFact() {
        return fact != null && !fact.isEmpty();
    }

    public static class Builder {
        private final int id;
        private final String storyText;
        private String title = "";
        private String context = "";
        private String[] choices = new String[0];
        private int[] nextNodes = new int[0];
        private int imageResource = 0;
        private int soundResource = 0;
        private String fact = "";
        private boolean isEndNode = false;
        private int rewardPoints = 0;

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

        public Builder choices(String[] choices) {
            this.choices = choices;
            return this;
        }

        public Builder nextNodes(int[] nextNodes) {
            this.nextNodes = nextNodes;
            return this;
        }

        public Builder imageResource(int imageResource) {
            this.imageResource = imageResource;
            return this;
        }

        public Builder soundResource(int soundResource) {
            this.soundResource = soundResource;
            return this;
        }

        public Builder fact(String fact) {
            this.fact = fact;
            return this;
        }
        
        public Builder isEndNode(boolean isEndNode) {
            this.isEndNode = isEndNode;
            return this;
        }
        
        public Builder rewardPoints(int rewardPoints) {
            this.rewardPoints = rewardPoints;
            return this;
        }

        public BucovinaStoryNode build() {
            return new BucovinaStoryNode(this);
        }
    }
} 