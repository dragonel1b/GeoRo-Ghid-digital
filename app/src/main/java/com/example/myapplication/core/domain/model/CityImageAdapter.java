package com.example.myapplication.core.domain.model;

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
import java.util.List;

public class CityImageAdapter extends RecyclerView.Adapter<CityImageAdapter.ImageViewHolder> {
    private final List<CityImage> images;
    private final Context context;

    public CityImageAdapter(List<CityImage> images, Context context) {
        this.images = images;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        CityImage cityImage = images.get(position);
        Uri imageUri = cityImage.getImageUri();

        try {
            // Încarcă imaginea direct prin ImageView pentru teste
            String uriString = imageUri.toString();
            if (uriString.startsWith("android.resource://")) {
                // Încercăm să obținem resourse ID din URI
                String[] parts = uriString.split("/");
                if (parts.length > 0) {
                    try {
                        int resourceId = Integer.parseInt(parts[parts.length - 1]);
                        // Setăm direct imaginea pentru a testa dacă resursa este validă
                        holder.imageView.setImageResource(resourceId);
                    } catch (NumberFormatException e) {
                        // Folosim Glide ca backup
                        loadWithGlide(holder, imageUri);
                    }
                } else {
                    loadWithGlide(holder, imageUri);
                }
            } else {
                loadWithGlide(holder, imageUri);
            }
        } catch (Exception e) {
            // În caz de orice eroare, încercăm încărcarea cu Glide ca ultimă opțiune
            loadWithGlide(holder, imageUri);
        }

        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        });
    }

    private void loadWithGlide(ImageViewHolder holder, Uri imageUri) {
        Glide.with(context)
                .load(imageUri)
                .placeholder(android.R.drawable.ic_menu_gallery) // placeholder standard Android
                .error(android.R.drawable.ic_menu_report_image) // folosim o imagine de eroare standard Android
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void addImage(Uri imageUri) {
        images.add(new CityImage(imageUri, true));
        notifyItemInserted(images.size() - 1);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.city_image);
        }
    }
} 