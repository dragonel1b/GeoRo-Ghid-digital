package com.example.myapplication.RomApp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.CityPreviewActivity;
import java.util.List;
import java.util.ArrayList;

public class DobrogeaCityAdapter extends RecyclerView.Adapter<DobrogeaCityAdapter.CityViewHolder> {

    private final List<City> cities;
    private final Dobrogea activity;

    public DobrogeaCityAdapter(Dobrogea activity, List<City> cities) {
        this.activity = activity;
        this.cities = cities;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city_dobrogea, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        City city = cities.get(position);
        holder.bind(city);
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    class CityViewHolder extends RecyclerView.ViewHolder {
        private final TextView cityName;
        private final TextView pointsBadge;
        private final SwitchMaterial citySwitch;
        private final MaterialButton infoButton;
        private final ViewPager2 imageCarousel;
        private final View cardView;

        CityViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cityCard);
            cityName = itemView.findViewById(R.id.cityName);
            pointsBadge = itemView.findViewById(R.id.pointsBadge);
            citySwitch = itemView.findViewById(R.id.citySwitch);
            infoButton = itemView.findViewById(R.id.infoButton);
            imageCarousel = itemView.findViewById(R.id.imageCarousel);

            setupListeners();
        }

        private void setupListeners() {
            // City selection switch
            citySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    City city = cities.get(position);
                    city.setSelected(isChecked);

                    // Animate points badge
                    if (isChecked) {
                        showPointsBadgeWithAnimation();
                    } else {
                        hidePointsBadge();
                    }

                    // Update progress in activity
                    activity.onCitySelected(isChecked);
                }
            });

            // Info button
            infoButton.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.icon_hover_scale));
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    City city = cities.get(position);
                    launchCityPreview(v.getContext(), city);
                }
            });

            // Image carousel click
            imageCarousel.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.icon_hover_scale));
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    City city = cities.get(position);
                    launchCityPreview(v.getContext(), city);
                }
            });
        }

        private void launchCityPreview(Context context, City city) {
            Intent intent = new Intent(context, CityPreviewActivity.class);
            intent.putExtra("cityName", city.getName());
            intent.putExtra("cityDescription", city.getDescription());
            intent.putStringArrayListExtra("cityImages", new ArrayList<>(city.getImageUrls()));

            // Start activity with animation
            context.startActivity(intent);
            if (context instanceof Activity) {
                ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
            }
        }

        void bind(City city) {
            cityName.setText(city.getName());
            citySwitch.setChecked(city.isSelected());

            // Set up image carousel
            CityImageAdapter imageAdapter = new CityImageAdapter(activity, new ArrayList<>(city.getImageUrls()));
            imageCarousel.setAdapter(imageAdapter);
            imageCarousel.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

            // Add page transformer for smooth transitions
            imageCarousel.setPageTransformer((page, position) -> {
                float absPosition = Math.abs(position);
                page.setAlpha(1 - (0.5f * absPosition));
                page.setScaleX(0.95f + (0.05f * (1 - absPosition)));
                page.setScaleY(0.95f + (0.05f * (1 - absPosition)));
            });

            // Show/hide points badge based on selection
            pointsBadge.setVisibility(city.isSelected() ? View.VISIBLE : View.INVISIBLE);

            // Apply card animation
            cardView.startAnimation(AnimationUtils.loadAnimation(itemView.getContext(), R.anim.fade_slide_in));
        }

        private void showPointsBadgeWithAnimation() {
            pointsBadge.setVisibility(View.VISIBLE);
            Animation anim = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.points_badge_appear);
            pointsBadge.startAnimation(anim);

            // Play confetti animation on the card
            cardView.post(() -> {
                Animation confettiAnim = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.confetti_success);
                cardView.startAnimation(confettiAnim);

                // Add haptic feedback
                cardView.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            });
        }

        private void hidePointsBadge() {
            Animation anim = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.fade_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    pointsBadge.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            pointsBadge.startAnimation(anim);
        }
    }

    public static class City {
        private final String name;
        private final List<String> imageUrls;
        private final String description;
        private boolean selected;

        public City(String name, List<String> imageUrls, String description) {
            this.name = name;
            this.imageUrls = imageUrls;
            this.description = description;
            this.selected = false;
        }

        public String getName() { return name; }
        public List<String> getImageUrls() { return imageUrls; }
        public String getDescription() { return description; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }
}
