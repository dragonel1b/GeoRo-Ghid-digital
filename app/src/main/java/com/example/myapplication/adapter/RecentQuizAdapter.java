package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.core.domain.model.QuizResult;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adaptor pentru afișarea activității recente în profilul utilizatorului
 */
public class RecentQuizAdapter extends RecyclerView.Adapter<RecentQuizAdapter.RecentQuizViewHolder> {
    
    private final List<QuizResult> quizResults;
    private final SimpleDateFormat dateFormat;
    
    public RecentQuizAdapter(List<QuizResult> quizResults) {
        this.quizResults = quizResults;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public RecentQuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_quiz, parent, false);
        return new RecentQuizViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecentQuizViewHolder holder, int position) {
        QuizResult result = quizResults.get(position);
        
        // Verificăm dacă este starea goală
        boolean isEmptyState = "empty_state".equals(result.getRegion());
        
        // Normalizez și îmbunătățesc afișarea titlului
        String regionName = normalizeRegionName(result.getRegion());
        String gameTypeName = normalizeGameType(result.getGameType());
        
        if (isEmptyState) {
            holder.quizTitleTextView.setText(regionName);
            holder.quizTitleTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.romania_text_secondary, null));
            holder.quizTitleTextView.setTextSize(18); // Slightly larger for better visibility
            
            holder.quizDateTextView.setText(gameTypeName);
            holder.quizDateTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.romania_text_secondary, null));
            
            holder.accuracyBadge.setVisibility(View.GONE);
            
            // Ascundem complet zona de scor pentru starea goală
            if (holder.scoreContainer != null) {
                holder.scoreContainer.setVisibility(View.GONE);
            } else {
                holder.scoreTextView.setText("");
            }
            
            // Modificăm și iconița clock să nu apară pentru starea goală
            ViewGroup dateContainer = (ViewGroup) holder.itemView.findViewById(R.id.quizDateTextView).getParent();
            if (dateContainer instanceof LinearLayout) {
                View clockImageView = dateContainer.getChildAt(0); // Prima vedere din LinearLayout
                if (clockImageView instanceof ImageView) {
                    clockImageView.setVisibility(View.GONE);
                }
            }
        } else {
            String title = regionName + " - " + gameTypeName;
            holder.quizTitleTextView.setText(title);
            holder.quizTitleTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.romania_blue, null));
            holder.quizTitleTextView.setTextSize(16); // Reset to normal size
            
            // Restaurăm vizibilitatea pentru zona de scor
            if (holder.scoreContainer != null) {
                holder.scoreContainer.setVisibility(View.VISIBLE);
            }
            
            // Restaurăm iconița clock
            ViewGroup dateContainer = (ViewGroup) holder.itemView.findViewById(R.id.quizDateTextView).getParent();
            if (dateContainer instanceof LinearLayout) {
                View clockImageView = dateContainer.getChildAt(0);
                if (clockImageView instanceof ImageView) {
                    clockImageView.setVisibility(View.VISIBLE);
                }
            }
            
            // Set date
            if (result.getCompletedAt() != null) {
                holder.quizDateTextView.setText(dateFormat.format(result.getCompletedAt()));
            } else {
                holder.quizDateTextView.setText("Recent");
            }
            holder.quizDateTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.romania_text_secondary, null));
            
            // Set accuracy cu validare îmbunătățită
            holder.accuracyBadge.setVisibility(View.VISIBLE);
            if (result.getTotalQuestions() > 0) {
                int accuracy = (result.getCorrectAnswers() * 100) / result.getTotalQuestions();
                holder.accuracyBadge.setText(accuracy + "%");
            } else {
                holder.accuracyBadge.setText("N/A");
            }
            
            // Set score cu formatare
            if (result.getScore() > 0) {
                holder.scoreTextView.setText(String.valueOf(result.getScore()));
            } else {
                holder.scoreTextView.setText("0");
            }
        }
        
        // Set quiz type icon based on region or game type
        setQuizTypeIcon(holder.quizTypeIcon, result.getGameType(), result.getRegion());
    }
    
    /**
     * Normalizează numele regiunii pentru afișare
     */
    private String normalizeRegionName(String region) {
        if (region == null || region.isEmpty()) {
            return "Quiz General";
        }
        
        // Gestionăm starea goală
        if ("empty_state".equals(region)) {
            return "Nicio activitate recentă";
        }
        
        switch (region.toLowerCase()) {
            case "transilvania":
            case "transylvania":
                return "Transilvania";
            case "muntenia":
                return "Muntenia";
            case "oltenia":
                return "Oltenia";
            case "moldova":
                return "Moldova";
            case "dobrogea":
                return "Dobrogea";
            case "banat":
                return "Banat";
            case "crisana":
                return "Crișana";
            case "maramures":
            case "maramureș":
                return "Maramureș";
            case "bucovina":
                return "Bucovina";
            default:
                return capitalizeFirstLetter(region);
        }
    }
    
    /**
     * Normalizează tipul de joc pentru afișare
     */
    private String normalizeGameType(String gameType) {
        if (gameType == null || gameType.isEmpty()) {
            return "Quiz";
        }
        
        // Gestionăm starea goală
        if ("info".equals(gameType)) {
            return "Începe să joci pentru a vedea activitatea ta!";
        }
        
        switch (gameType.toLowerCase()) {
            case "quiz":
            case "general":
                return "Quiz Cunoștințe";
            case "story":
                return "Povește Interactivă";
            case "exploration":
            case "explore":
                return "Explorare";
            case "mini-game":
            case "minigame":
                return "Mini-Joc";
            case "leaderboard":
            case "competition":
                return "Competiție";
            case "adventure":
                return "Aventură";
            default:
                return capitalizeFirstLetter(gameType);
        }
    }

    private void setQuizTypeIcon(ImageView icon, String gameType, String region) {
        // Gestionăm starea goală
        if ("empty_state".equals(region)) {
            icon.setImageResource(R.drawable.ic_info);
            return;
        }
        
        // Set different icons based on game type and region
        if (gameType != null) {
            switch (gameType.toLowerCase()) {
                case "quiz":
                case "general":
                    icon.setImageResource(R.drawable.ic_quiz);
                    break;
                case "story":
                    icon.setImageResource(R.drawable.ic_culture_book);
                    break;
                case "exploration":
                case "explore":
                    icon.setImageResource(R.drawable.ic_explore);
                    break;
                case "adventure":
                    icon.setImageResource(R.drawable.ic_mountain);
                    break;
                case "leaderboard":
                case "competition":
                    icon.setImageResource(R.drawable.ic_leaderboard);
                    break;
                default:
                    icon.setImageResource(R.drawable.ic_quiz);
                    break;
            }
        } else {
            icon.setImageResource(R.drawable.ic_quiz);
        }
    }
    
    @Override
    public int getItemCount() {
        return quizResults != null ? quizResults.size() : 0;
    }
    
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
    
    static class RecentQuizViewHolder extends RecyclerView.ViewHolder {
        TextView quizTitleTextView;
        TextView quizDateTextView;
        TextView accuracyBadge;
        TextView scoreTextView;
        ImageView quizTypeIcon;
        LinearLayout scoreContainer; // Container pentru zona de scor
        
        RecentQuizViewHolder(@NonNull View itemView) {
            super(itemView);
            quizTitleTextView = itemView.findViewById(R.id.quizTitleTextView);
            quizDateTextView = itemView.findViewById(R.id.quizDateTextView);
            accuracyBadge = itemView.findViewById(R.id.accuracyBadge);
            scoreTextView = itemView.findViewById(R.id.scoreTextView);
            quizTypeIcon = itemView.findViewById(R.id.quizTypeIcon);
            
            // Găsim containerul de scor pentru a-l putea ascunde în starea goală
            View parent = (View) scoreTextView.getParent();
            if (parent instanceof LinearLayout) {
                scoreContainer = (LinearLayout) parent;
            }
        }
    }
} 