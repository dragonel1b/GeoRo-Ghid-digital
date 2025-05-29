package com.example.myapplication.bucovinausage;

/**
 * Represents a story node in the Bucovina story experience.
 * Each node contains text content and an optional image resource ID.
 */
public class BucovinaStoryNode {
    
    private final String text;
    private final int imageResourceId;
    
    /**
     * Creates a story node with text and an image.
     *
     * @param text The text content of the story node
     * @param imageResourceId The resource ID of the image for this node
     */
    public BucovinaStoryNode(String text, int imageResourceId) {
        this.text = text;
        this.imageResourceId = imageResourceId;
    }
    
    /**
     * Creates a story node with text only (no image).
     *
     * @param text The text content of the story node
     */
    public BucovinaStoryNode(String text) {
        this.text = text;
        this.imageResourceId = -1; // No image
    }
    
    /**
     * Gets the text content of this story node.
     *
     * @return The text content
     */
    public String getText() {
        return text;
    }
    
    /**
     * Gets the image resource ID for this story node.
     *
     * @return The image resource ID, or -1 if no image is available
     */
    public int getImageResourceId() {
        return imageResourceId;
    }
    
    /**
     * Checks if this story node has an image.
     *
     * @return true if an image is available, false otherwise
     */
    public boolean hasImage() {
        return imageResourceId != -1;
    }
} 