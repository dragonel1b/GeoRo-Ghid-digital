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
        // For now just set a placeholder color and text
        int[] colors = {Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.MAGENTA};
        holder.imageView.setBackgroundColor(colors[position % colors.length]);
        
        // Get city name from filename
        String filename = images.get(position);
        String cityName = filename;
        if (filename.contains(".")) {
            cityName = filename.substring(0, filename.lastIndexOf('.'));
        }
        if (!cityName.isEmpty()) {
            cityName = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
        }
        
        // Create a bitmap with the city name
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(colors[position % colors.length]);
        
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(36);
        paint.setTextAlign(Paint.Align.CENTER);
        
        canvas.drawText(cityName, 100, 100, paint);
        holder.imageView.setImageBitmap(bitmap);
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
