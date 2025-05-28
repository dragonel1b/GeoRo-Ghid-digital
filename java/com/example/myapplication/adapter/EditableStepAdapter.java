package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class EditableStepAdapter extends RecyclerView.Adapter<EditableStepAdapter.StepViewHolder> {

    private final Context context;
    private final List<String> steps;

    public EditableStepAdapter(Context context, List<String> steps) {
        this.context = context;
        this.steps = steps;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_editable_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        String step = steps.get(position);
        
        // Set step number and description
        holder.stepNumberText.setText(String.format("%d", position + 1));
        holder.stepDescriptionText.setText(step);
        
        // Setup delete button
        holder.deleteButton.setOnClickListener(v -> {
            steps.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, steps.size());
        });
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView stepNumberText;
        TextView stepDescriptionText;
        ImageButton deleteButton;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepNumberText = itemView.findViewById(R.id.step_number_text);
            stepDescriptionText = itemView.findViewById(R.id.step_description_text);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
} 