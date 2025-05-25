package com.example.myapplication.viewmodel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import java.util.ArrayList;

public class CityImageAdapter extends RecyclerView.Adapter<CityImageAdapter.ImageViewHolder> {
    private final Context context;
    private final ArrayList<String> images;
    private final boolean isUserManaged;

    public CityImageAdapter(Context context, ArrayList<String> images, boolean isUserManaged) {
        this.context = context;
        this.images = images;
        this.isUserManaged = isUserManaged;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_city_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageName = images.get(position);

        // Get drawable resource ID from the image name
        int resourceId = context.getResources().getIdentifier(
                imageName,
                "drawable",
                context.getPackageName()
        );

        if (resourceId != 0) {
            holder.imageView.setImageResource(resourceId);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cityImage);
        }
    }
}
