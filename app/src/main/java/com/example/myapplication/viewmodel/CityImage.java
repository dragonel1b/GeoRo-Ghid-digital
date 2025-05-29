package com.example.myapplication.viewmodel;

public class CityImage {
    private String imageUri;
    
    public CityImage(String imageUri) {
        this.imageUri = imageUri;
    }
    
    public String getImageUri() {
        return imageUri;
    }
    
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
} 