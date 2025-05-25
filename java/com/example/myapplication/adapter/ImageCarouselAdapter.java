package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;

public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<String> images;

    public ImageCarouselAdapter(Context context, ArrayList<String> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_loading, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the image resource name
        String imageName = images.get(position);
        int resId = context.getResources().getIdentifier(
            imageName, 
            "drawable", 
            context.getPackageName()
        );
        
        if (resId != 0) {
            // Load the actual drawable
            holder.imageView.setImageResource(resId);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            // Fallback to placeholder if image not found
            holder.imageView.setImageResource(R.drawable.ic_region);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            // Create a new ImageView since it's not in the layout
            imageView = new ImageView(itemView.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
            ((FrameLayout) itemView).addView(imageView);
        }
    }
}
