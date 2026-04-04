package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.core.domain.model.QuizResult;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adaptor pentru afișarea rezultatelor quiz-urilor în RecyclerView
 */
public class QuizResultAdapter extends RecyclerView.Adapter<QuizResultAdapter.QuizResultViewHolder> {
    
    private final List<QuizResult> quizResults;
    private final SimpleDateFormat dateFormat;
    
    /**
     * Constructor pentru QuizResultAdapter
     * @param quizResults Lista de rezultate ale quiz-urilor
     */
    public QuizResultAdapter(List<QuizResult> quizResults) {
        this.quizResults = quizResults;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public QuizResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_result, parent, false);
        return new QuizResultViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull QuizResultViewHolder holder, int position) {
        QuizResult result = quizResults.get(position);
        
        // Formatăm textul pentru titlu (regiunea și tipul jocului)
        String title = capitalizeFirstLetter(result.getRegion()) + " - " 
                + capitalizeFirstLetter(result.getGameType());
        
        // Formatăm textul pentru detalii (scor, răspunsuri corecte și data)
        String details = result.getCorrectAnswers() + "/" + result.getTotalQuestions() + " corecte";
        
        if (result.getCompletedAt() != null) {
            details += " | " + dateFormat.format(result.getCompletedAt());
        }
        
        // Setăm valorile în ViewHolder
        holder.titleTextView.setText(title);
        holder.detailsTextView.setText(details);
        holder.scoreTextView.setText(String.valueOf(result.getScore()));
        
        // Adăugăm animație la încărcarea elementelor
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(
                holder.itemView.getContext(), R.anim.item_animation_fall_down));
    }
    
    @Override
    public int getItemCount() {
        return quizResults.size();
    }
    
    /**
     * Metodă utilă pentru a capitaliza prima literă a unui string
     */
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
    
    /**
     * ViewHolder pentru rezultatele quiz-urilor
     */
    static class QuizResultViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView detailsTextView;
        TextView scoreTextView;
        ImageView regionIcon;
        
        QuizResultViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            detailsTextView = itemView.findViewById(R.id.detailsTextView);
            scoreTextView = itemView.findViewById(R.id.scoreTextView);
            regionIcon = itemView.findViewById(R.id.regionIcon);
        }
    }
} 