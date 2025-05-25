package com.example.myapplication.Joc1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class RomAttractionAdapter extends RecyclerView.Adapter<RomAttractionAdapter.AttractionViewHolder> {
    private List<String> attractions;

    public RomAttractionAdapter(List<String> attractions) {
        this.attractions = attractions;
    }

    @NonNull
    @Override
    public AttractionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rom_attraction, parent, false);
        return new AttractionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttractionViewHolder holder, int position) {
        String attraction = attractions.get(position);
        holder.attractionText.setText(attraction);

        // Apply animation
        holder.cardView.setAlpha(0f);
        holder.cardView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(position * 100L)
                .start();
    }

    @Override
    public int getItemCount() {
        return attractions.size();
    }

    static class AttractionViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView attractionText;

        AttractionViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.attractionCard);
            attractionText = itemView.findViewById(R.id.attractionText);
        }
    }

    public void updateAttractions(List<String> newAttractions) {
        this.attractions = newAttractions;
        notifyDataSetChanged();
    }
}
