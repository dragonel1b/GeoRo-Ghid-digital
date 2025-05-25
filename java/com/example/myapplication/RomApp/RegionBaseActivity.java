package com.example.myapplication.RomApp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public abstract class RegionBaseActivity extends AppCompatActivity {
    protected static final int POINTS_PER_CITY = 5;
    protected int totalPoints = 0;
    protected String regionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_in_right);
    }

    protected void updatePoints(int points) {
        totalPoints += points;
        // Override this method in child classes to update UI
    }

    protected void onRegionComplete() {
        // Override this method in child classes to handle region completion
    }

    protected void setRegionName(String name) {
        this.regionName = name;
    }

    protected String getRegionName() {
        return regionName;
    }
}
