package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Helper class for connecting ViewPager2 with TabLayout as carousel indicator
 */
public class CarouselIndicatorHelper {

    /**
     * Setup custom tab indicators for image carousel
     *
     * @param viewPager2 The ViewPager2 with images
     * @param tabLayout The TabLayout to use as indicator
     * @param context The context
     * @param imageCount Number of images in the carousel
     */
    public static void setupCarouselIndicator(
            @NonNull ViewPager2 viewPager2,
            @NonNull TabLayout tabLayout,
            @NonNull Context context,
            int imageCount) {

        // Clear existing tabs
        tabLayout.removeAllTabs();

        // Add tabs for each image
        for (int i = 0; i < imageCount; i++) {
            tabLayout.addTab(tabLayout.newTab());
        }

        // Active and inactive colors
        int activeColor = ContextCompat.getColor(context, R.color.white);
        int inactiveColor = ContextCompat.getColor(context, R.color.white);
        float inactiveAlpha = 0.4f;

        // Create custom tab views
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                tabLayout, viewPager2, (tab, position) -> {
            // Create a circular indicator
            View view = LayoutInflater.from(context).inflate(R.layout.item_carousel_indicator, null);
            ImageView indicator = view.findViewById(R.id.carousel_indicator);
            
            // Create dot shape
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setSize(24, 24);
            
            // Apply color based on selected state
            dot.setColor(position == viewPager2.getCurrentItem() ? activeColor : inactiveColor);
            indicator.setAlpha(position == viewPager2.getCurrentItem() ? 1.0f : inactiveAlpha);
            indicator.setImageDrawable(dot);
            
            tab.setCustomView(view);
        });
        tabLayoutMediator.attach();

        // Update indicator when page changes
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update all tabs
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null && tab.getCustomView() != null) {
                        ImageView indicator = tab.getCustomView().findViewById(R.id.carousel_indicator);
                        indicator.setAlpha(i == position ? 1.0f : inactiveAlpha);
                    }
                }
            }
        });
    }
} 