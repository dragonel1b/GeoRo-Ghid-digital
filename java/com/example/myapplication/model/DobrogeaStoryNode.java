package com.example.myapplication.model;

public class DobrogeaStoryNode {
    private int nodeId;
    private String storyText;
    private String[] choices;
    private int[] nextNodes;
    private int pointsReward;

    public DobrogeaStoryNode(int id, String text, String[] choices, int[] nextNodes, int points) {
        this.nodeId = id;
        this.storyText = text;
        this.choices = choices;
        this.nextNodes = nextNodes;
        this.pointsReward = points;
    }

    // Getters
    public int getNodeId() { return nodeId; }
    public String getStoryText() { return storyText; }
    public String[] getChoices() { return choices; }
    public int[] getNextNodes() { return nextNodes; }
    public int getPointsReward() { return pointsReward; }
}
