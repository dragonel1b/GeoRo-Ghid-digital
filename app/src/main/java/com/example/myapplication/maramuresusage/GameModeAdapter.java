package com.example.myapplication.maramuresusage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import java.util.List;

public class GameModeAdapter extends RecyclerView.Adapter<GameModeAdapter.GameModeViewHolder> {
    public interface OnGameModeClickListener {
        void onGameModeClick(com.example.myapplication.maramuresusage.GameModeManager.GameMode gameMode);
    }
    private List<com.example.myapplication.maramuresusage.GameModeManager.GameMode> gameModes;
    private OnGameModeClickListener listener;
    public GameModeAdapter(List<com.example.myapplication.maramuresusage.GameModeManager.GameMode> gameModes, OnGameModeClickListener listener) {
        this.gameModes = gameModes;
        this.listener = listener;
    }
    @NonNull
    @Override
    public GameModeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_mode, parent, false);
        return new GameModeViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull GameModeViewHolder holder, int position) {
        com.example.myapplication.maramuresusage.GameModeManager.GameMode mode = gameModes.get(position);
        holder.textName.setText(mode.emoji + " " + mode.displayName);
        holder.textDescription.setText(mode.description);
        holder.itemView.setOnClickListener(v -> listener.onGameModeClick(mode));
    }
    @Override
    public int getItemCount() {
        return gameModes.size();
    }
    
    public void setSelectedGameMode(com.example.myapplication.maramuresusage.GameModeManager.GameMode gameMode) {
        // This method can be used to highlight the selected game mode
        // For now, we'll just notify the adapter to refresh
        notifyDataSetChanged();
    }
    static class GameModeViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textDescription;
        GameModeViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.gameModeTitle);
            textDescription = itemView.findViewById(R.id.gameModeDescription);
        }
    }
} 