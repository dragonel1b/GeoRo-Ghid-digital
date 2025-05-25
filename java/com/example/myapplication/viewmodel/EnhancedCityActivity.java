package com.example.myapplication.viewmodel;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.myapplication.R;
import com.example.myapplication.viewmodel.CityImageAdapter;
import com.example.myapplication.viewmodel.CityViewModel;
import com.example.myapplication.viewmodel.CityInfoBottomSheet;
import com.example.myapplication.viewmodel.SectionPreviewActivity;
import java.util.ArrayList;

public class EnhancedCityActivity extends BaseCityActivity {
    private ArrayList<String> cityImages;
    private CityImageAdapter imageAdapter;
    private CityViewModel viewModel;
    private TextView pointsTextView;
    private MediaPlayer soundEffect;
    private View confettiView;
    private ViewPager2 imageCarousel;
    private TabLayout imageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CityViewModel.class);
        setupViews();
        setupEnhancedImageCarousel();
        initializeSpecificContent();
        setupSoundEffects();
        observeViewModel();
    }

    private void setupViews() {
        pointsTextView = findViewById(R.id.pointsText);
        confettiView = findViewById(R.id.confetti_view);

        // Setup View Cities button
        findViewById(R.id.viewCitiesButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, CityListActivity.class);
            intent.putExtra(CityListActivity.EXTRA_REGION_NAME, getRegionName());
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // Apply elastic scroll
        LinearLayout mainContainer = findViewById(R.id.cityContentContainer);
        mainContainer.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
    }

    private void setupEnhancedImageCarousel() {
        imageCarousel = findViewById(R.id.imageCarousel);
        imageIndicator = findViewById(R.id.imageIndicator);

        imageAdapter = new CityImageAdapter(this, getCityImages(), true);
        imageCarousel.setAdapter(imageAdapter);

        new TabLayoutMediator(imageIndicator, imageCarousel,
                (tab, position) -> {
                    tab.setText("");
                    tab.setContentDescription("Image " + (position + 1) + " of " + getCityImages().size());
                }
        ).attach();

        // Enhanced page transformer
        imageCarousel.setPageTransformer((page, position) -> {
            float absPosition = Math.abs(position);
            page.setAlpha(1.0f - (0.7f * absPosition));
            float scale = 0.85f + (0.15f * (1 - absPosition));
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setBackgroundResource(R.drawable.blurred_image_background);
        });

        // Add touch feedback
        imageCarousel.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100);
                    break;
            }
            return false;
        });
    }

    private void setupSoundEffects() {
        soundEffect = MediaPlayer.create(this, R.raw.click_sound);
    }

    private void observeViewModel() {
        viewModel.getTotalPoints().observe(this, points -> {
            pointsTextView.setText("💰 " + points);
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.points_update);
            pointsTextView.startAnimation(anim);
        });

        viewModel.getShowConfetti().observe(this, show -> {
            if (show) {
                confettiView.setVisibility(View.VISIBLE);
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.confetti_success);
                confettiView.startAnimation(anim);
            } else {
                confettiView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected String getCityName() {
        return "Constanța"; // or any relevant city name
    }

    protected String getRegionName() {
        // Get region name from the class name of the actual activity
        String className = getClass().getSimpleName();
        if (className.contains("Constanta")) {
            return "Dobrogea";
        } else if (className.contains("Moldova")) {
            return "Moldova";
        } else if (className.contains("Transilvania")) {
            return "Transilvania";
        }
        return "Unknown Region";
    }

    @Override
    protected void initializeSpecificContent() {
        LinearLayout container = findViewById(R.id.cityContentContainer);
        addSection(container, "Istorie", "Constanța, cunoscută în antichitate sub numele de Tomis, este cel mai vechi oraș atestat de pe teritoriul României.", true);
        addSection(container, "Atracții Turistice", "Cazinoul, Moscheea Carol I, Portul Tomis și plajele moderne sunt doar câteva dintre atracțiile care fac din Constanța o destinație turistică de top.", false);
        addSection(container, "Cultură", "Un oraș multicultural unde se împletesc influențele române, grecești, turcești și tătare, creând un mozaic cultural unic.", false);
    }

    protected void addSection(LinearLayout container, String title, String content, boolean isHighlighted) {
        if (container == null) {
            return;
        }

        View sectionView = getLayoutInflater().inflate(R.layout.section_layout, container, false);

        TextView titleView = sectionView.findViewById(R.id.sectionTitle);
        TextView contentView = sectionView.findViewById(R.id.sectionContent);
        CheckBox checkBox = sectionView.findViewById(R.id.visitedCheckbox);
        View importantBadge = sectionView.findViewById(R.id.importantBadge);

        titleView.setText(title);
        contentView.setText(content);

        if (isHighlighted) {
            // Apply enhanced styling for highlighted sections
            sectionView.setBackgroundResource(R.drawable.enhanced_card_background);
            titleView.setTextAppearance(android.R.style.TextAppearance_Large);
            titleView.setTextColor(getResources().getColor(R.color.colorPrimary));
            contentView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            importantBadge.setVisibility(View.VISIBLE);
        }

        // Handle checkbox changes
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Play sound effect
            if (soundEffect != null) {
                soundEffect.start();
            }

            // Perform haptic feedback
            buttonView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            // Update points
            if (isChecked) {
                viewModel.addPoints(10); // Add 10 points when checked
                viewModel.setShowConfetti(true); // Show confetti animation
            } else {
                viewModel.subtractPoints(10); // Subtract 10 points when unchecked
            }
        });

        // Make the entire section clickable to show preview
        sectionView.setOnClickListener(v -> {
            Intent intent = new Intent(this, SectionPreviewActivity.class);
            intent.putExtra(SectionPreviewActivity.EXTRA_TITLE, title);
            intent.putExtra(SectionPreviewActivity.EXTRA_CONTENT, content);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // Apply fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_slide_in);
        fadeIn.setDuration(500);
        sectionView.startAnimation(fadeIn);

        container.addView(sectionView);
    }

    @Override
    protected ArrayList<String> getCityImages() {
        if (cityImages == null) {
            cityImages = new ArrayList<>();
            // Add your image resources here
            cityImages.add("dobrogea_constanta_1");
            cityImages.add("dobrogea_constanta_casino");
        }
        return cityImages;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundEffect != null) {
            soundEffect.release();
            soundEffect = null;
        }
    }
}
