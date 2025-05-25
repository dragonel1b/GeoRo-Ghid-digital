package com.example.myapplication.Joc1.Culinary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

/**
 * Adapter pentru pașii interactivi ai rețetei cu suport Material Design 3
 */
public class InteractiveStepAdapter extends RecyclerView.Adapter<InteractiveStepAdapter.StepViewHolder> {
    
    private final List<RecipeStepByStepActivity.RecipeStep> steps;
    private int currentStepPosition = 0;
    private final OnStepClickListener listener;
    private final Context context;
    
    /**
     * Interfață pentru gestionarea evenimentelor de click pe pași
     */
    public interface OnStepClickListener {
        void onStepClicked(int position);
    }
    
    public InteractiveStepAdapter(Context context, List<RecipeStepByStepActivity.RecipeStep> steps, OnStepClickListener listener) {
        this.context = context;
        this.steps = steps;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step_interactive, parent, false);
        return new StepViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        RecipeStepByStepActivity.RecipeStep step = steps.get(position);
        
        // Setează numărul pasului
        holder.stepNumberText.setText(String.valueOf(step.getStepNumber()));
        
        // Setează descrierea pasului (truncată la două linii pentru aspect mai bun în listă)
        holder.stepInstructionText.setText(step.getDescription());
        
        // Afișează sau ascunde iconițele pentru timer și video
        if (step.hasTimer()) {
            holder.timerIcon.setVisibility(View.VISIBLE);
            holder.timerDurationText.setVisibility(View.VISIBLE);
            
            // Formatează durata timer-ului în format uman
            String timerText;
            long minutes = step.getTimerDuration() / 60000;
            if (minutes > 60) {
                long hours = minutes / 60;
                minutes %= 60;
                timerText = hours + " h " + (minutes > 0 ? minutes + " min" : "");
            } else {
                timerText = minutes + " min";
            }
            holder.timerDurationText.setText(timerText);
        } else {
            holder.timerIcon.setVisibility(View.GONE);
            holder.timerDurationText.setVisibility(View.GONE);
        }
        
        // Afișează iconița video dacă pasul are video
        holder.videoIcon.setVisibility(step.hasVideoTutorial() ? View.VISIBLE : View.GONE);
        
        // Setează starea pasului (finalizat, curent sau viitor)
        updateStepState(holder, step, position);
        
        // Setează click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStepClicked(position);
            }
        });
    }
    
    /**
     * Actualizează pașii pentru a reflecta pasul curent
     * @param currentPosition poziția pasului curent
     */
    public void setCurrentStep(int currentPosition) {
        int oldPosition = currentStepPosition;
        currentStepPosition = currentPosition;
        
        // Notifică doar elementele care au fost afectate de schimbare
        notifyItemChanged(oldPosition);
        notifyItemChanged(currentPosition);
    }
    
    /**
     * Actualizează starea unui pas în funcție de poziția sa relativă la pasul curent
     */
    private void updateStepState(@NonNull StepViewHolder holder, RecipeStepByStepActivity.RecipeStep step, int position) {
        int stepBackground;
        int textColor;
        int iconResource;
        boolean showProgress = false;
        int progress = 0;
        
        if (step.isCompleted()) {
            // Pas finalizat
            stepBackground = R.attr.colorSecondaryContainer;
            textColor = R.attr.colorOnSecondaryContainer;
            iconResource = android.R.drawable.checkbox_on_background;
            holder.cardView.setChecked(true);
        } else if (position == currentStepPosition) {
            // Pas curent
            stepBackground = R.attr.colorPrimaryContainer;
            textColor = R.attr.colorOnPrimaryContainer;
            iconResource = android.R.drawable.ic_media_play;
            showProgress = true;
            progress = 50; // Exemplu - ar trebui calculat real
            holder.cardView.setChecked(true);
        } else {
            // Pas viitor
            stepBackground = R.attr.colorSurfaceVariant;
            textColor = R.attr.colorOnSurfaceVariant;
            iconResource = android.R.drawable.checkbox_off_background;
            holder.cardView.setChecked(false);
        }
        
        // Aplică stilizarea
        holder.stepNumberBackground.setCardBackgroundColor(context.getColor(stepBackground));
        holder.stepNumberText.setTextColor(context.getColor(textColor));
        holder.stepStatusIcon.setImageResource(iconResource);
        
        // Afișează sau ascunde indicatorul de progres
        holder.stepProgressIndicator.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        holder.stepProgressIndicator.setProgress(progress);
    }
    
    @Override
    public int getItemCount() {
        return steps.size();
    }
    
    /**
     * ViewHolder pentru pașii interactivi
     */
    static class StepViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardView;
        final MaterialCardView stepNumberBackground;
        final TextView stepNumberText;
        final TextView stepInstructionText;
        final ImageView timerIcon;
        final TextView timerDurationText;
        final ImageView videoIcon;
        final ImageView stepStatusIcon;
        final LinearProgressIndicator stepProgressIndicator;
        
        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            stepNumberBackground = itemView.findViewById(R.id.stepNumberBackground);
            stepNumberText = itemView.findViewById(R.id.stepNumberText);
            stepInstructionText = itemView.findViewById(R.id.stepInstructionText);
            timerIcon = itemView.findViewById(R.id.timerIcon);
            timerDurationText = itemView.findViewById(R.id.timerDurationText);
            videoIcon = itemView.findViewById(R.id.videoIcon);
            stepStatusIcon = itemView.findViewById(R.id.stepStatusIcon);
            stepProgressIndicator = itemView.findViewById(R.id.stepProgressIndicator);
        }
    }
} 