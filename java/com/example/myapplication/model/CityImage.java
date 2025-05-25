package com.example.myapplication.model;

import android.net.Uri;

public class CityImage {
    private final Uri imageUri;
    private final boolean isUserManaged;

    public CityImage(Uri imageUri, boolean isUserManaged) {
        this.imageUri = imageUri;
        this.isUserManaged = isUserManaged;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public boolean isUserManaged() {
        return isUserManaged;
    }
} 