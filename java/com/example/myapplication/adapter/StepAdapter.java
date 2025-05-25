package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {
    private final String[] steps;
    private final boolean[] completedSteps;
    private int currentStep = 0;

    public StepAdapter(String[] steps) {
        this.steps = steps;
        this.completedSteps = new boolean[steps.length];
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        String step = steps[position];
        holder.stepNumberText.setText(String.valueOf(position + 1));
        holder.stepInstructionText.setText(step);
        
        // Set card appearance based on step progress
        if (completedSteps[position]) {
            // Completed step
            holder.stepCard.setCardBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.rom_success_light));
            holder.stepNumberText.setTextColor(
                    holder.itemView.getContext().getResources().getColor(R.color.rom_success));
        } else if (position == currentStep) {
            // Current step
            holder.stepCard.setCardBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.rom_primary_light));
            holder.stepNumberText.setTextColor(
                    holder.itemView.getContext().getResources().getColor(R.color.rom_primary));
        } else {
            // Future step
            holder.stepCard.setCardBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.rom_card_background));
            holder.stepNumberText.setTextColor(
                    holder.itemView.getContext().getResources().getColor(R.color.rom_text_secondary));
        }
        
        // Step completion logic
        holder.itemView.setOnClickListener(v -> {
            if (position == currentStep) {
                completedSteps[position] = true;
                if (position < steps.length - 1) {
                    currentStep++;
                }
                notifyDataSetChanged();
            } else if (completedSteps[position]) {
                // Allow toggling completed steps
                completedSteps[position] = false;
                currentStep = position;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return steps.length;
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView stepCard;
        TextView stepNumberText;
        TextView stepInstructionText;

        StepViewHolder(View itemView) {
            super(itemView);
            stepCard = itemView.findViewById(R.id.stepCard);
            stepNumberText = itemView.findViewById(R.id.stepNumberText);
            stepInstructionText = itemView.findViewById(R.id.stepInstructionText);
        }
    }
} 