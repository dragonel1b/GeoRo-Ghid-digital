package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for recipe image carousel
 */
public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ImageViewHolder> {

    private final List<String> imageUrls;
    private OnImageClickListener clickListener;
    private OnImageAddedListener imageAddedListener;

    /**
     * Interface for image click events
     */
    public interface OnImageClickListener {
        void onImageClick(int position, String imageUrl);
    }

    /**
     * Interface for image added events
     */
    public interface OnImageAddedListener {
        void onImageAdded(String imageUri);
    }

    public ImageCarouselAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public ImageCarouselAdapter(Activity activity, List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnImageAddedListener(OnImageAddedListener listener) {
        this.imageAddedListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carousel_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        holder.bind(imageUrl, position);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView imageView;
        private String currentImageUrl;
        private int currentPosition;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.carouselImageView);
            itemView.setOnClickListener(this);
        }

        @SuppressLint("StringFormatInvalid")
        void bind(String imageUrl, int position) {
            this.currentImageUrl = imageUrl;
            this.currentPosition = position;
            // Încercăm să obținem resource ID din numele fișierului
            int resourceId = itemView.getContext().getResources().getIdentifier(
                    imageUrl, "drawable", itemView.getContext().getPackageName());

            if (resourceId != 0) {
                // Dacă am găsit resursa, o încărcăm direct
                Glide.with(itemView.getContext())
                        .load(resourceId)
                        .placeholder(R.drawable.image_placeholder_background)
                        .error(R.drawable.image_placeholder_background)
                        .transform(new CenterCrop())
                        .into(imageView);

                // Logăm succesul în consolă pentru debug
                android.util.Log.d("ImageCarousel", "Imagine încărcată cu succes: " + imageUrl + " (ID: " + resourceId + ")");
            } else {
                // Dacă nu am găsit resursa, încercăm să o încărcăm ca URL
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.image_placeholder_background)
                    .error(R.drawable.image_placeholder_background)
                    .transform(new CenterCrop())
                    .into(imageView);

                // Logăm eroarea în consolă pentru debug
                android.util.Log.e("ImageCarousel", "Imagine negăsită! Nu există resursă pentru: " + imageUrl);
            }
            // Set content description
            imageView.setContentDescription(
                    itemView.getContext().getString(R.string.recipe_image_description, position + 1));
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onImageClick(currentPosition, currentImageUrl);
            }
        }
    }
}
