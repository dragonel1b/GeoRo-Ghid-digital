package com.example.myapplication.RomApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.example.myapplication.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;

public class CityPreviewActivity extends AppCompatActivity {
    private ViewPager2 imageCarousel;
    private TextView cityDescription;
    private MaterialToolbar toolbar;
    private TabLayout imageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_preview);

        // Initialize views
        imageCarousel = findViewById(R.id.imageCarousel);
        cityDescription = findViewById(R.id.cityDescription);
        toolbar = findViewById(R.id.toolbar);
        imageIndicator = findViewById(R.id.imageIndicator);

        // Get data from intent
        String cityName = getIntent().getStringExtra("cityName");
        String description = getIntent().getStringExtra("cityDescription");
        ArrayList<String> images = getIntent().getStringArrayListExtra("cityImages");

        if (cityName == null || description == null || images == null) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Error loading city details", Snackbar.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up toolbar
        toolbar.setTitle(cityName);
        toolbar.setNavigationOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // Set up image carousel
        CityImageAdapter imageAdapter = new CityImageAdapter(this, images);
        imageCarousel.setAdapter(imageAdapter);
        imageCarousel.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(imageIndicator, imageCarousel,
                (tab, position) -> tab.setText("")
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

        // Set city description
        cityDescription.setText(description);
        cityDescription.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));

        // Set up Learn More button
        MaterialButton learnMoreButton = findViewById(R.id.learnMoreButton);
        learnMoreButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));

            // Launch the appropriate city activity based on the city name
            Intent intent = null;
            switch (cityName) {
                case "Constanța":
                    intent = new Intent(this, ConstantaActivity.class);
                    break;
                // Add other cities here as they are implemented
                default:
                    Snackbar.make(v, "Coming soon: " + cityName + " detailed view",
                            Snackbar.LENGTH_SHORT).show();
                    return;
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
            }
        });

        // Animate the entire layout
        findViewById(R.id.rootLayout).startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.fade_in));
    }
}
