package com.example.myapplication.viewmodel;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.myapplication.R;
import com.example.myapplication.viewmodel.CityImageAdapter;
import java.util.ArrayList;

public abstract class BaseCityActivity extends AppCompatActivity {
    protected ViewPager2 imageCarousel;
    protected CityImageAdapter imageAdapter;
    protected TabLayout imageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_detail);

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getCityName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up image carousel
        imageCarousel = findViewById(R.id.imageCarousel);
        imageIndicator = findViewById(R.id.imageIndicator);

        // Initialize adapter with isUserManaged=false by default
        // Child classes can override this by creating their own adapter
        imageAdapter = new CityImageAdapter(this, getCityImages(), false);
        imageCarousel.setAdapter(imageAdapter);

        // Set up tab indicator with accessibility descriptions
        new TabLayoutMediator(imageIndicator, imageCarousel,
                (tab, position) -> {
                    tab.setText("");
                    tab.setContentDescription("Image " + (position + 1) + " of " + getCityImages().size());
                }
        ).attach();

        // Add page transformer for smooth transitions
        imageCarousel.setPageTransformer((page, position) -> {
            float absPosition = Math.abs(position);
            // Fade effect
            page.setAlpha(1 - (0.5f * absPosition));
            // Scale effect
            float scale = 0.85f + (0.15f * (1 - absPosition));
            page.setScaleX(scale);
            page.setScaleY(scale);
        });

        // Initialize content
        initializeSpecificContent();
    }

    protected void addSection(LinearLayout container, String title, String content) {
        // Forward to the new method with default highlighting (false)
        addSection(container, title, content, false);
    }

    protected void addSection(LinearLayout container, String title, String content, boolean isHighlighted) {
        if (container == null) {
            return;
        }

        View sectionView = getLayoutInflater().inflate(R.layout.section_layout, container, false);

        TextView titleView = sectionView.findViewById(R.id.sectionTitle);
        TextView contentView = sectionView.findViewById(R.id.sectionContent);

        titleView.setText(title);
        contentView.setText(content);

        if (isHighlighted) {
            // Apply enhanced styling for highlighted sections
            sectionView.setBackgroundResource(R.drawable.enhanced_card_background);
            titleView.setTextAppearance(android.R.style.TextAppearance_Large);
            titleView.setTextColor(getResources().getColor(R.color.colorPrimary));
            contentView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        // Apply fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_slide_in);
        fadeIn.setDuration(500);
        sectionView.startAnimation(fadeIn);

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

    protected abstract String getCityName();
    protected abstract ArrayList<String> getCityImages();
    protected abstract void initializeSpecificContent();
}
