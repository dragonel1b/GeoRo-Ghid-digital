package com.example.myapplication.RomApp;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.RomApp.RegionBaseActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.CityImageAdapter;
import com.example.myapplication.RomApp.DobrogeaCityAdapter;
import com.example.myapplication.RomApp.DobrogeaCityAdapter.City;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dobrogea extends RegionBaseActivity {
    private RecyclerView cityRecyclerView;
    private CircularProgressIndicator progressIndicator;
    private TextView regionTitle;
    private DobrogeaCityAdapter cityAdapter;
    private List<City> cities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRegionName("Dobrogea");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dobrogea);

        initializeCities();
        initializeViews();
        setupRecyclerView();
        setupAnimations();
    }

    private void initializeCities() {
        cities = Arrays.asList(
                new City("Constanța", Arrays.asList("url1", "url2"), "Constanța este un oraș-port la Marea Neagră și cel mai mare port maritim al României. Este un important centru cultural, istoric și economic."),
                new City("Tulcea", Arrays.asList("url1", "url2"), "Tulcea este poarta de intrare în Delta Dunării și un important centru turistic și industrial. Orașul are o istorie bogată și o cultură diversă."),
                new City("Mangalia", Arrays.asList("url1", "url2"), "Mangalia este cel mai vechi oraș din România, cu o istorie de peste 2500 de ani. Este o stațiune balneară importantă și un port maritim."),
                new City("Medgidia", Arrays.asList("url1", "url2"), "Medgidia este un important nod feroviar și rutier în Dobrogea. Orașul are o istorie bogată și este cunoscut pentru industria sa."),
                new City("Cernavodă", Arrays.asList("url1", "url2"), "Cernavodă este cunoscută pentru centrala nucleară și podul peste Dunăre. Este un important centru energetic al României.")
        );
    }

    private void initializeViews() {
        cityRecyclerView = findViewById(R.id.cityRecyclerView);
        progressIndicator = findViewById(R.id.progressIndicator);
        regionTitle = findViewById(R.id.textViewRegionTitle);
        regionTitle.setText(getRegionName());

        // Set up back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            backButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            finish();
        });
    }

    private void setupRecyclerView() {
        cityAdapter = new DobrogeaCityAdapter(this, cities);
        cityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cityRecyclerView.setAdapter(cityAdapter);
        cityRecyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(
                this, R.anim.layout_animation_fall_down));
    }

    private void setupAnimations() {
        // Animate title and progress
        regionTitle.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));
        progressIndicator.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));
    }

    public void showCityDetails(City city) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.RomanianBottomSheetDialog);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_city_details, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Initialize views
        TextView cityName = bottomSheetView.findViewById(R.id.cityName);
        TextView cityDescription = bottomSheetView.findViewById(R.id.cityDescription);
        ViewPager2 imageCarousel = bottomSheetView.findViewById(R.id.imageCarousel);
        TabLayout imageIndicator = bottomSheetView.findViewById(R.id.imageIndicator);
        ImageButton closeButton = bottomSheetView.findViewById(R.id.closeButton);

        // Set data
        cityName.setText(city.getName());
        cityDescription.setText(city.getDescription());

        // Set up image carousel in bottom sheet
        CityImageAdapter detailImageAdapter = new CityImageAdapter(this, new ArrayList<>(city.getImageUrls()));
        imageCarousel.setAdapter(detailImageAdapter);
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

        // Set up image click listener for full-screen view
        detailImageAdapter.setOnImageClickListener((position, imageView) -> {
            imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.icon_hover_scale));
            // TODO: Implement full-screen image view
        });

        // Close button
        closeButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.icon_hover_scale));
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    public void onCitySelected(boolean isChecked) {
        updatePoints(isChecked ? POINTS_PER_CITY : -POINTS_PER_CITY);
    }

    @Override
    protected void updatePoints(int points) {
        super.updatePoints(points);
        progressIndicator.setProgress((int) ((totalPoints / (float) (cities.size() * POINTS_PER_CITY)) * 100));

        if (totalPoints == cities.size() * POINTS_PER_CITY) {
            onRegionComplete();
        }
    }

    @Override
    protected void onRegionComplete() {
        // TODO: Show completion animation and unlock next region
    }
}
