package com.example.myapplication.Joc1.Culinary;

import android.view.View;

public class FeatureItem {
    private String title;
    private int iconResId;
    private View.OnClickListener onClickListener;

    public FeatureItem(String title, int iconResId, View.OnClickListener onClickListener) {
        this.title = title;
        this.iconResId = iconResId;
        this.onClickListener = onClickListener;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
