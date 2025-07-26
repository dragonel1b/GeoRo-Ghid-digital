package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.munteniausage.GameModeManager;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

/**
 * Adapter pentru afișarea modurilor de joc în RecyclerView
 */
public class GameModeAdapter extends RecyclerView.Adapter<GameModeAdapter.ViewHolder> {
    
    private List<GameModeManager.GameMode> gameModes;
    private OnGameModeClickListener listener;
    private GameModeManager.GameMode selectedGameMode;
    
    public interface OnGameModeClickListener {
        void onGameModeClick(GameModeManager.GameMode gameMode);
    }
    
    public GameModeAdapter(List<GameModeManager.GameMode> gameModes, OnGameModeClickListener listener) {
        this.gameModes = gameModes;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_mode, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameModeManager.GameMode gameMode = gameModes.get(position);
        holder.bind(gameMode);
    }
    
    @Override
    public int getItemCount() {
        return gameModes.size();
    }
    
    public void setSelectedGameMode(GameModeManager.GameMode gameMode) {
        this.selectedGameMode = gameMode;
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView emojiText;
        private TextView titleText;
        private TextView descriptionText;
        private TextView statsText;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.gameModeCard);
            emojiText = itemView.findViewById(R.id.gameModeEmoji);
            titleText = itemView.findViewById(R.id.gameModeTitle);
            descriptionText = itemView.findViewById(R.id.gameModeDescription);
            statsText = itemView.findViewById(R.id.gameModeStats);
            
            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onGameModeClick(gameModes.get(position));
                }
            });
        }
        
        public void bind(GameModeManager.GameMode gameMode) {
            emojiText.setText(gameMode.emoji);
            titleText.setText(gameMode.displayName);
            descriptionText.setText(gameMode.description);
            
            // Build stats text
            StringBuilder stats = new StringBuilder();
            stats.append("📊 Întrebări: ").append(gameMode.questionCount == -1 ? "Unlimited" : gameMode.questionCount).append("\n");
            stats.append("⏱️ Timp/întrebare: ").append(gameMode.timePerQuestion).append("s\n");
            stats.append("🎯 Lifeline-uri: ").append(gameMode.isEliminationMode ? "Nu" : "Da");
            
            statsText.setText(stats.toString());
            
            // Highlight selected game mode
            boolean isSelected = selectedGameMode == gameMode;
            cardView.setStrokeWidth(isSelected ? 4 : 2);
            cardView.setStrokeColor(itemView.getContext().getColor(
                isSelected ? R.color.muntenia_primary : R.color.muntenia_secondary));
        }
    }
} 