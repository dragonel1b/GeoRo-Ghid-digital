package com.example.myapplication.RomApp;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.myapplication.R;
import java.util.ArrayList;

public abstract class BaseCityActivity extends AppCompatActivity {
    protected ViewPager2 imageCarousel;
    protected CityImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_detail);

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getCityName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get images before setting up the carousel
        ArrayList<String> images = getCityImages();
        if (images != null) {
            // Set up image carousel only if we have images
            imageCarousel = findViewById(R.id.imageCarousel);
            imageAdapter = new CityImageAdapter(this, images);
            imageCarousel.setAdapter(imageAdapter);
        }

        // Initialize content
        initializeSpecificContent();
    }

    protected void addSection(LinearLayout container, String title, String content) {
        View sectionView = getLayoutInflater().inflate(R.layout.section_layout, container, false);

        TextView titleView = sectionView.findViewById(R.id.sectionTitle);
        TextView contentView = sectionView.findViewById(R.id.sectionContent);

        titleView.setText(title);
        contentView.setText(content);

        sectionView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));
        container.addView(sectionView);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // Abstract methods that must be implemented by child classes
    protected abstract String getCityName();
    protected abstract ArrayList<String> getCityImages();
    protected abstract void initializeSpecificContent();
}
