package com.example.myapplication.Joc1.Culinary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.os.LocaleListCompat;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.Locale;

/**
 * Welcoming activity for first-time users of the culinary module
 * Implements a modern onboarding flow with ViewPager2
 */
public class CulinaryWelcomeActivity extends AppCompatActivity {

    private ViewPager2 onboardingViewPager;
    private DotsIndicator dotsIndicator;
    private LinearProgressIndicator progressIndicator;
    private MaterialButton skipButton;
    private MaterialButton nextButton;
    
    private FirebaseAnalytics firebaseAnalytics;
    private OnboardingPagerAdapter pagerAdapter;
    
    private static final int TOTAL_PAGES = 3;
    private static final String EVENT_ONBOARDING_STARTED = "onboarding_started";
    private static final String EVENT_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String EVENT_ONBOARDING_SKIPPED = "onboarding_skipped";
    private static final String EVENT_PAGE_VIEWED = "onboarding_page_viewed";
    private static final String EVENT_LANGUAGE_SELECTED = "language_selected";
    private static final String EVENT_NOTIFICATIONS_ENABLED = "notifications_enabled";
    
    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                logAnalyticsEvent(EVENT_NOTIFICATIONS_ENABLED, true);
                Snackbar.make(
                    onboardingViewPager, 
                    "Veți primi notificări cu sfaturi culinare!", 
                    Snackbar.LENGTH_SHORT
                ).show();
            } else {
                logAnalyticsEvent(EVENT_NOTIFICATIONS_ENABLED, false);
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_culinary_welcome);
        
        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        logAnalyticsEvent(EVENT_ONBOARDING_STARTED, null);
        
        // Initialize UI components
        initViews();
        setupViewPager();
        setupListeners();
    }
    
    private void initViews() {
        onboardingViewPager = findViewById(R.id.onboardingViewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        progressIndicator = findViewById(R.id.onboardingProgress);
        skipButton = findViewById(R.id.skipButton);
        nextButton = findViewById(R.id.nextButton);
        
        progressIndicator.setMax(TOTAL_PAGES);
        progressIndicator.setProgress(1);
    }
    
    private void setupViewPager() {
        pagerAdapter = new OnboardingPagerAdapter();
        onboardingViewPager.setAdapter(pagerAdapter);
        onboardingViewPager.setOffscreenPageLimit(1);
        
        // Connect ViewPager2 with DotsIndicator
        dotsIndicator.setViewPager2(onboardingViewPager);
        
        // Page change callback
        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateProgressAndButtons(position);
                logAnalyticsEvent(EVENT_PAGE_VIEWED, position + 1);
            }
        });
    }
    
    private void setupListeners() {
        skipButton.setOnClickListener(v -> {
            logAnalyticsEvent(EVENT_ONBOARDING_SKIPPED, null);
            completeOnboarding();
        });
        
        nextButton.setOnClickListener(v -> {
            int currentPosition = onboardingViewPager.getCurrentItem();
            
            if (currentPosition < TOTAL_PAGES - 1) {
                // Go to next page
                onboardingViewPager.setCurrentItem(currentPosition + 1);
            } else {
                // We're on the last page, complete onboarding
                logAnalyticsEvent(EVENT_ONBOARDING_COMPLETED, null);
                completeOnboarding();
            }
        });
    }
    
    private void updateProgressAndButtons(int position) {
        progressIndicator.setProgress(position + 1);
        
        if (position == TOTAL_PAGES - 1) {
            // Last page
            nextButton.setText("Începeți");
            nextButton.setIcon(null);
            skipButton.setVisibility(View.GONE);
        } else {
            nextButton.setText("Înainte");
            nextButton.setIcon(getDrawable(android.R.drawable.ic_media_next));
            skipButton.setVisibility(View.VISIBLE);
        }
    }
    
    private void completeOnboarding() {
        // Mark onboarding as complete
        CulinaryPreferences.setOnboardingComplete(this, true);
        
        // Check if we should show the profile setup
        if (CulinaryPreferences.hasUserProfile(this)) {
            // User already has a profile, go directly to modern culinary activity
            openModernCulinary();
        } else {
            // Ask user if they want to set up profile first or skip
            showProfileSetupDialog();
        }
    }
    
    private void openTraditionalCulinary() {
        // Navigate to traditional culinary activity
        Intent intent = new Intent(this, RomCulinaryActivity.class);
        startActivity(intent);
        finish();
    }

    private void openModernCulinary() {
        // Navigate to modern culinary activity
        Intent intent = new Intent(this, ModernCulinaryActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void showProfileSetupDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Configurare profil culinar")
               .setMessage("Doriți să configurați profilul culinar acum? Acesta vă va ajuta să primiți recomandări personalizate.")
               .setPositiveButton("Configurați profilul", (dialog, which) -> navigateToProfileSetup())
               .setNegativeButton("Săriți acest pas", (dialog, which) -> openModernCulinary())
               .show();
    }
    
    private void navigateToProfileSetup() {
        // Navigate to profile setup
        Intent intent = new Intent(this, CulinaryProfileSetupActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, 
                Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    
    private void setAppLanguage(String languageCode) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocales);
        
        logAnalyticsEvent(EVENT_LANGUAGE_SELECTED, languageCode);
    }
    
    private void logAnalyticsEvent(String eventName, Object value) {
        try {
            Bundle params = new Bundle();
            if (value != null) {
                if (value instanceof Integer) {
                    params.putInt("value", (Integer) value);
                } else if (value instanceof Boolean) {
                    params.putBoolean("value", (Boolean) value);
                } else if (value instanceof String) {
                    params.putString("value", (String) value);
                }
            }
            firebaseAnalytics.logEvent(eventName, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * ViewPager adapter for onboarding pages
     */
    private class OnboardingPagerAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<OnboardingPageViewHolder> {
        private final int[] layouts = {
            R.layout.layout_onboarding_welcome,
            R.layout.layout_onboarding_features,
            R.layout.layout_onboarding_cta
        };
        
        @NonNull
        @Override
        public OnboardingPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(layouts[viewType], parent, false);
            return new OnboardingPageViewHolder(view, viewType);
        }
        
        @Override
        public void onBindViewHolder(@NonNull OnboardingPageViewHolder holder, int position) {
            holder.bind();
        }
        
        @Override
        public int getItemCount() {
            return layouts.length;
        }
        
        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }
    
    /**
     * ViewHolder for onboarding pages
     */
    private class OnboardingPageViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        private final int pageType;
        
        public OnboardingPageViewHolder(@NonNull View itemView, int pageType) {
            super(itemView);
            this.pageType = pageType;
        }
        
        public void bind() {
            switch (pageType) {
                case 0: // Welcome page
                    setupWelcomePage();
                    break;
                case 1: // Features page
                    // No special setup needed
                    break;
                case 2: // CTA page
                    setupCtaPage();
                    break;
            }
        }
        
        private void setupWelcomePage() {
            Chip romanianChip = itemView.findViewById(R.id.languageRomanianChip);
            Chip englishChip = itemView.findViewById(R.id.languageEnglishChip);
            
            romanianChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    setAppLanguage("ro");
                }
            });
            
            englishChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    setAppLanguage("en");
                }
            });
            
            // Set initial language based on device locale
            Locale locale = getResources().getConfiguration().getLocales().get(0);
            if (locale.getLanguage().equals("en")) {
                englishChip.setChecked(true);
            } else {
                romanianChip.setChecked(true);
            }
        }
        
        private void setupCtaPage() {
            MaterialButton btnStartTraditional = itemView.findViewById(R.id.btn_start_traditional);
            MaterialButton btnStartModern = itemView.findViewById(R.id.btn_start_modern);
            MaterialButton btnSetupProfile = itemView.findViewById(R.id.btn_setup_profile);
            SwitchMaterial notificationsSwitch = itemView.findViewById(R.id.notificationsSwitch);
            
            btnStartTraditional.setOnClickListener(v -> {
                logAnalyticsEvent("traditional_experience_selected", null);
                openTraditionalCulinary();
            });
            
            btnStartModern.setOnClickListener(v -> {
                logAnalyticsEvent("modern_experience_selected", null);
                openModernCulinary();
            });
            
            btnSetupProfile.setOnClickListener(v -> {
                logAnalyticsEvent("profile_setup_selected", null);
                navigateToProfileSetup();
            });
            
            notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    requestNotificationPermission();
                }
            });
        }
    }
    
    /**
     * Helper class for culinary-related preferences
     */
    public static class CulinaryPreferences {
        private static final String PREF_NAME = "culinary_prefs";
        private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";
        private static final String KEY_HAS_PROFILE = "has_profile";
        
        public static void setOnboardingComplete(android.content.Context context, boolean complete) {
            android.content.SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply();
        }
        
        public static boolean isOnboardingComplete(android.content.Context context) {
            android.content.SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false);
        }
        
        public static boolean hasUserProfile(android.content.Context context) {
            android.content.SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            return prefs.getBoolean(KEY_HAS_PROFILE, false);
        }
        
        public static void setHasUserProfile(android.content.Context context, boolean hasProfile) {
            android.content.SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_HAS_PROFILE, hasProfile).apply();
        }
    }
} 