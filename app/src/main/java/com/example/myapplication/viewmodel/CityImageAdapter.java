package com.example.myapplication.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import java.util.ArrayList;
import java.util.List;

public class CityImageAdapter extends RecyclerView.Adapter<CityImageAdapter.ViewHolder> {
    private Context context;
    private List<CityImage> images;

    public CityImageAdapter(Context context, List<CityImage> images) {
        this.context = context;
        this.images = images;
    }

    public CityImageAdapter(Context context, ArrayList<String> imageUris) {
        this.context = context;
        this.images = new ArrayList<>();
        for (String uriString : imageUris) {
            images.add(new CityImage(uriString));
        }
    }
    
    public CityImageAdapter(Context context, ArrayList<String> imageUris, boolean isUserManaged) {
        this.context = context;
        this.images = new ArrayList<>();
        for (String uriString : imageUris) {
            images.add(new CityImage(uriString));
        }
        // The isUserManaged parameter is now ignored, but we keep the constructor for compatibility
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_city_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CityImage image = images.get(position);
        try {
            holder.imageView.setImageURI(Uri.parse(image.getImageUri()));
        } catch (Exception e) {
            // If image loading fails, show a placeholder
            holder.imageView.setImageResource(R.drawable.ic_add_photo);
        }

        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(image.getImageUri()), "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void addImage(String imageUri) {
        images.add(new CityImage(imageUri));
        notifyItemInserted(images.size() - 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.city_image);
        }
    }
}
