package com.example.myapplication.RomApp;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class CityImageAdapter extends RecyclerView.Adapter<CityImageAdapter.ImageViewHolder> {
    private Context context;
    private List<String> images;
    private OnImageClickListener onImageClickListener;
    private OnImageLongClickListener onImageLongClickListener;

    public interface OnImageClickListener {
        void onImageClick(int position, ImageView imageView);
    }

    public interface OnImageLongClickListener {
        boolean onImageLongClick(int position, ImageView imageView);
    }

    public CityImageAdapter(List<String> images) {
        this.images = new ArrayList<>(images);
    }

    public CityImageAdapter(Context context, List<String> images) {
        this.context = context;
        this.images = new ArrayList<>(images);
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.onImageClickListener = listener;
    }

    public void setOnImageLongClickListener(OnImageLongClickListener listener) {
        this.onImageLongClickListener = listener;
    }

    public void removeImage(int position) {
        if (position >= 0 && position < images.size()) {
            images.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateImage(int position, String newImageName) {
        if (position >= 0 && position < images.size()) {
            images.set(position, newImageName);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_city_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageName = images.get(position);
        int resourceId = context.getResources().getIdentifier(
                imageName, "drawable", context.getPackageName());
        holder.imageView.setImageResource(resourceId);

        // Set up click listeners
        if (onImageClickListener != null) {
            holder.imageView.setOnClickListener(v ->
                    onImageClickListener.onImageClick(position, holder.imageView));
        }

        if (onImageLongClickListener != null) {
            holder.imageView.setOnLongClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return onImageLongClickListener.onImageLongClick(position, holder.imageView);
            });
        }

        // Set up delete button
        holder.deleteButton.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Confirmare ștergere")
                    .setMessage("Sigur doriți să ștergeți această imagine?")
                    .setPositiveButton("Șterge", (dialog, which) -> {
                        removeImage(position);
                        Snackbar.make(v, "Imagine ștearsă", Snackbar.LENGTH_LONG)
                                .setAction("Anulează", v2 -> {
                                    images.add(position, imageName);
                                    notifyItemInserted(position);
                                })
                                .show();
                    })
                    .setNegativeButton("Anulează", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cityImage);
            deleteButton = itemView.findViewById(R.id.btnDeleteImage);
        }
    }
}
