package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.LeaderboardEntry;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adaptor pentru afișarea intrărilor din clasament (leaderboard) în RecyclerView
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
    
    private final List<LeaderboardEntry> entries;
    private final SimpleDateFormat dateFormat;
    private final String currentUserId;
    
    /**
     * Constructor pentru LeaderboardAdapter
     * @param entries Lista de intrări din clasament
     */
    public LeaderboardAdapter(List<LeaderboardEntry> entries) {
        this.entries = entries;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        
        // Obținem ID-ul utilizatorului curent (dacă este autentificat)
        String userId = null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        this.currentUserId = userId;
    }
    
    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_entry, parent, false);
        return new LeaderboardViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardEntry entry = entries.get(position);
        holder.bind(entry);
    }
    
    @Override
    public int getItemCount() {
        return entries.size();
    }
    
    /**
     * ViewHolder pentru intrările din clasament
     */
    class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        private final TextView rankTextView;
        private final TextView usernameTextView;
        private final TextView scoreTextView;
        private final TextView dateTextView;
        private final ImageView profileImageView;
        private final View itemBackground;
        
        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            scoreTextView = itemView.findViewById(R.id.scoreTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            itemBackground = itemView.findViewById(R.id.itemBackground);
        }
        
        /**
         * Leagă datele intrării de ViewHolder
         * @param entry Intrarea din clasament
         */
        public void bind(LeaderboardEntry entry) {
            // Setăm rangul
            rankTextView.setText(String.valueOf(entry.getRank()));
            
            // Setăm numele utilizatorului
            usernameTextView.setText(entry.getDisplayNameOrUsername());
            
            // Setăm scorul
            scoreTextView.setText(String.valueOf(entry.getScore()));
            
            // Setăm data (dacă este disponibilă)
            if (entry.getAchievedAt() != null) {
                dateTextView.setText(dateFormat.format(entry.getAchievedAt()));
            } else {
                dateTextView.setText("");
            }
            
            // Încărcăm imaginea de profil (dacă este disponibilă)
            if (entry.getProfileImageUrl() != null && !entry.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(entry.getProfileImageUrl())
                        .placeholder(R.drawable.default_profile_image)
                        .error(R.drawable.default_profile_image)
                        .circleCrop()
                        .into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.default_profile_image);
            }
            
            // Evidențiem intrarea utilizatorului curent
            if (currentUserId != null && currentUserId.equals(entry.getUserId())) {
                itemBackground.setBackgroundResource(R.drawable.bg_leaderboard_current_user);
            } else {
                // Aplicăm un fundal diferit pentru primele 3 poziții
                switch (entry.getRank()) {
                    case 1:
                        itemBackground.setBackgroundResource(R.drawable.bg_leaderboard_gold);
                        break;
                    case 2:
                        itemBackground.setBackgroundResource(R.drawable.bg_leaderboard_silver);
                        break;
                    case 3:
                        itemBackground.setBackgroundResource(R.drawable.bg_leaderboard_bronze);
                        break;
                    default:
                        itemBackground.setBackgroundResource(R.drawable.bg_leaderboard_normal);
                        break;
                }
            }
        }
    }
} 