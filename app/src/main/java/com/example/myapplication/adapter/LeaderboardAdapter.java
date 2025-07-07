package com.example.myapplication.adapter;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.myapplication.R;
import com.example.myapplication.model.LeaderboardEntry;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adaptor pentru afișarea intrărilor din clasament (leaderboard) în RecyclerView
 * Actualizat cu animații și stiluri îmbunătățite
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
    
    private final List<LeaderboardEntry> entries;
    private final SimpleDateFormat dateFormat;
    private final String currentUserId;
    private int lastPosition = -1;
    
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
        
        // Asigurăm că rangurile sunt corect atribuite
        recalculateRanks();
    }
    
    /**
     * Recalculează rangurile pentru toate intrările
     */
    private void recalculateRanks() {
        if (entries == null || entries.isEmpty()) return;
        
        int currentRank = 1;
        int position = 1;
        int previousScore = -1;
        
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry entry = entries.get(i);
            
            // Primul element primește rangul 1
            if (i == 0) {
                entry.setRank(currentRank);
                previousScore = entry.getScore();
            } else {
                // Dacă scorul este diferit de cel precedent, actualizăm rangul
                if (entry.getScore() < previousScore) {
                    currentRank = position;
                    previousScore = entry.getScore();
                }
                entry.setRank(currentRank);
            }
            
            position++;
        }
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
        
        // Aplicăm animație la afișare doar pentru elementele noi
        if (position > lastPosition) {
            animateItem(holder.itemView, position);
            lastPosition = position;
        }
    }
    
    /**
     * Animează intrarea în listă cu efect spectaculos
     */
    private void animateItem(View view, int position) {
        // Stare inițială pentru animații spectaculoase
        view.setTranslationY(150f);
        view.setTranslationX(50f);
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setRotation(-5f);
        
        // Animație de translație Y (de jos în sus)
        ObjectAnimator translationYAnimator = ObjectAnimator.ofFloat(view, "translationY", 150f, 0f);
        translationYAnimator.setDuration(500);
        translationYAnimator.setStartDelay(position * 80); // Întârziere progresivă crescută
        translationYAnimator.setInterpolator(new android.view.animation.OvershootInterpolator(1.2f));
        
        // Animație de translație X (slide din dreapta)
        ObjectAnimator translationXAnimator = ObjectAnimator.ofFloat(view, "translationX", 50f, 0f);
        translationXAnimator.setDuration(400);
        translationXAnimator.setStartDelay(position * 80);
        translationXAnimator.setInterpolator(new DecelerateInterpolator());
        
        // Animație de alpha (fade in)
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        alphaAnimator.setDuration(400);
        alphaAnimator.setStartDelay(position * 80);
        
        // Animație de scale X (zoom in)
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f);
        scaleXAnimator.setDuration(500);
        scaleXAnimator.setStartDelay(position * 80);
        scaleXAnimator.setInterpolator(new android.view.animation.BounceInterpolator());
        
        // Animație de scale Y (zoom in)
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f);
        scaleYAnimator.setDuration(500);
        scaleYAnimator.setStartDelay(position * 80);
        scaleYAnimator.setInterpolator(new android.view.animation.BounceInterpolator());
        
        // Animație de rotație (enderezare)
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", -5f, 0f);
        rotationAnimator.setDuration(600);
        rotationAnimator.setStartDelay(position * 80);
        rotationAnimator.setInterpolator(new android.view.animation.OvershootInterpolator(0.8f));
        
        // Start toate animațiile în paralel
        translationYAnimator.start();
        translationXAnimator.start();
        alphaAnimator.start();
        scaleXAnimator.start();
        scaleYAnimator.start();
        rotationAnimator.start();
        
        // Adăugăm un efect de pulsare după 1 secundă pentru top 3
        if (position < 3) {
            view.postDelayed(() -> {
                ObjectAnimator pulseX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f, 1f);
                ObjectAnimator pulseY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f, 1f);
                pulseX.setDuration(300);
                pulseY.setDuration(300);
                pulseX.start();
                pulseY.start();
            }, 1000 + (position * 100));
        }
    }
    
    @Override
    public int getItemCount() {
        return entries.size();
    }
    
    /**
     * Actualizează lista de intrări și recalculează rangurile
     * @param newEntries Noua listă de intrări
     */
    public void updateEntries(List<LeaderboardEntry> newEntries) {
        entries.clear();
        if (newEntries != null) {
            entries.addAll(newEntries);
            recalculateRanks();
        }
        lastPosition = -1; // Resetăm pentru a reaplica animațiile
        notifyDataSetChanged();
    }
    
    /**
     * Actualizează lista de intrări FĂRĂ a recalcula rangurile
     * Folosit când rangurile sunt deja setate corect (ex: de la poziția 4 în sus)
     * @param newEntries Noua listă de intrări cu ranguri deja corecte
     */
    public void updateEntriesWithoutRecalculation(List<LeaderboardEntry> newEntries) {
        entries.clear();
        if (newEntries != null) {
            entries.addAll(newEntries);
            // NU recalculăm rangurile - le păstrăm ca sunt
        }
        lastPosition = -1; // Resetăm pentru a reaplica animațiile
        notifyDataSetChanged();
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
            
            // Încărcăm imaginea de profil cu tranziții îmbunătățite
            if (entry.getProfileImageUrl() != null && !entry.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(entry.getProfileImageUrl())
                        .placeholder(R.drawable.default_profile_image)
                        .error(R.drawable.default_profile_image)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.default_profile_image);
            }
            
            // Evidențiem intrarea utilizatorului curent
            if (currentUserId != null && currentUserId.equals(entry.getUserId())) {
                itemBackground.setBackgroundResource(R.drawable.bg_leaderboard_current_user);
                // Aplicăm elevație pentru a evidenția și mai mult
                itemBackground.setElevation(10f);
                usernameTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
            } else {
                // Aplicăm un fundal diferit pentru primele 3 poziții
                switch (entry.getRank()) {
                    case 1:
                        itemBackground.setBackgroundResource(R.drawable.bg_leaderboard_gold);
                        itemBackground.setElevation(8f);
                        rankTextView.setTextSize(20);
                        break;
                    case 2:
                        itemBackground.setBackgroundResource(R.drawable.bg_leaderboard_silver);
                        itemBackground.setElevation(6f);
                        rankTextView.setTextSize(18);
                        break;
                    case 3:
                        itemBackground.setBackgroundResource(R.drawable.bg_leaderboard_bronze);
                        itemBackground.setElevation(4f);
                        rankTextView.setTextSize(16);
                        break;
                    default:
                        itemBackground.setBackgroundResource(R.drawable.bg_leaderboard_normal);
                        itemBackground.setElevation(2f);
                        rankTextView.setTextSize(14);
                        break;
                }
                usernameTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
            }
        }
    }
} 