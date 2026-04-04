package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.core.domain.model.RegionProgress;
import java.util.List;
import java.util.Locale;

/**
 * Adapter pentru afișarea progresului utilizatorului în fiecare regiune
 */
public class RegionProgressAdapter extends RecyclerView.Adapter<RegionProgressAdapter.RegionProgressViewHolder> {
    
    private final List<RegionProgress> regionProgressList;
    private OnRegionClickListener onRegionClickListener;

    public interface OnRegionClickListener {
        void onRegionClick(RegionProgress regionProgress);
    }

    public RegionProgressAdapter(List<RegionProgress> regionProgressList) {
        this.regionProgressList = regionProgressList;
    }

    public void setOnRegionClickListener(OnRegionClickListener listener) {
        this.onRegionClickListener = listener;
    }

    @NonNull
    @Override
    public RegionProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_region_progress, parent, false);
        return new RegionProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegionProgressViewHolder holder, int position) {
        RegionProgress regionProgress = regionProgressList.get(position);
        holder.bind(regionProgress);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onRegionClickListener != null) {
                onRegionClickListener.onRegionClick(regionProgress);
            }
        });
    }

    @Override
    public int getItemCount() {
        return regionProgressList.size();
    }

    /**
     * Actualizează lista de regiuni
     */
    public void updateRegions(List<RegionProgress> newRegions) {
        regionProgressList.clear();
        regionProgressList.addAll(newRegions);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder pentru progresul regiunii
     */
    static class RegionProgressViewHolder extends RecyclerView.ViewHolder {
        private final ImageView regionIcon;
        private final TextView regionNameText;
        private final TextView quizzesCompletedText;
        private final TextView accuracyText;
        private final TextView bestScoreText;

        public RegionProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            regionIcon = itemView.findViewById(R.id.regionIcon);
            regionNameText = itemView.findViewById(R.id.regionNameText);
            quizzesCompletedText = itemView.findViewById(R.id.quizzesCompletedText);
            accuracyText = itemView.findViewById(R.id.accuracyText);
            bestScoreText = itemView.findViewById(R.id.bestScoreText);
        }

        public void bind(RegionProgress regionProgress) {
            // Setează numele regiunii
            regionNameText.setText(regionProgress.getRegionName());
            
            // Setează iconita regiunii
            regionIcon.setImageResource(regionProgress.getIconResource());
            
            // Setează numărul de quiz-uri completate
            int quizzesCount = regionProgress.getQuizzesCompleted();
            if (quizzesCount == 0) {
                quizzesCompletedText.setText("Niciun quiz");
            } else if (quizzesCount == 1) {
                quizzesCompletedText.setText("1 quiz");
            } else {
                quizzesCompletedText.setText(String.format(Locale.getDefault(), "%d quiz-uri", quizzesCount));
            }
            
            // Setează acuratețea
            if (regionProgress.hasCompletedQuizzes()) {
                accuracyText.setText(String.format(Locale.getDefault(), "%.0f%% acuratețe", 
                    regionProgress.getAverageAccuracy()));
                accuracyText.setVisibility(View.VISIBLE);
            } else {
                accuracyText.setText("Începe explorarea!");
                accuracyText.setVisibility(View.VISIBLE);
            }
            
            // Setează cel mai bun scor
            if (regionProgress.hasCompletedQuizzes()) {
                bestScoreText.setText(String.valueOf(regionProgress.getBestScore()));
            } else {
                bestScoreText.setText("--");
            }
        }
    }
} 