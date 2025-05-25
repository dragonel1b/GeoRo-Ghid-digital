package com.example.myapplication.Joc1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying mission objectives in a RecyclerView
 */
public class ObjectivesAdapter extends RecyclerView.Adapter<ObjectivesAdapter.ViewHolder> {
    private List<String> objectiveTexts = new ArrayList<>();
    private final boolean[] completionStatus;
    private OnObjectiveClickListener listener;

    public interface OnObjectiveClickListener {
        void onObjectiveClick(int position, boolean isCompleted);
    }

    /**
     * Constructor for string objectives
     */
    public ObjectivesAdapter(List<String> objectives) {
        this.objectiveTexts = objectives;
        this.completionStatus = new boolean[objectives.size()];
    }

    /**
     * Static factory method for creating an adapter from Mission.MissionObjective objects
     */
    public static ObjectivesAdapter fromMissionObjectives(List<Mission.MissionObjective> missionObjectives) {
        List<String> texts = new ArrayList<>();
        boolean[] statuses = new boolean[missionObjectives.size()];
        
        for (int i = 0; i < missionObjectives.size(); i++) {
            Mission.MissionObjective objective = missionObjectives.get(i);
            texts.add(objective.getDescription());
            statuses[i] = objective.isCompleted();
        }
        
        return new ObjectivesAdapter(texts, statuses);
    }

    /**
     * Constructor with predefined completion status
     */
    public ObjectivesAdapter(List<String> objectives, boolean[] completionStatus) {
        this.objectiveTexts = objectives;
        this.completionStatus = completionStatus;
    }

    public void setOnObjectiveClickListener(OnObjectiveClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_objective, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String objective = objectiveTexts.get(position);
        boolean isCompleted = completionStatus[position];

        holder.objectiveText.setText(objective);
        holder.completedCheckbox.setChecked(isCompleted);

        // Make the checkbox clickable only if we have a listener
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> {
                boolean newStatus = !completionStatus[position];
                completionStatus[position] = newStatus;
                holder.completedCheckbox.setChecked(newStatus);
                listener.onObjectiveClick(position, newStatus);
            });
            
            holder.completedCheckbox.setOnClickListener(v -> {
                boolean newStatus = holder.completedCheckbox.isChecked();
                completionStatus[position] = newStatus;
                listener.onObjectiveClick(position, newStatus);
            });
        } else {
            // If no listener, make the checkbox not clickable
            holder.completedCheckbox.setClickable(false);
            holder.itemView.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return objectiveTexts.size();
    }

    /**
     * Updates the completion status of an objective
     * 
     * @param position Position of the objective
     * @param isCompleted Whether the objective is completed
     */
    public void updateObjectiveStatus(int position, boolean isCompleted) {
        if (position >= 0 && position < completionStatus.length) {
            completionStatus[position] = isCompleted;
            notifyItemChanged(position);
        }
    }

    /**
     * Gets the current completion status array
     */
    public boolean[] getCompletionStatus() {
        return completionStatus;
    }

    /**
     * Check if all objectives are completed
     */
    public boolean areAllObjectivesCompleted() {
        for (boolean status : completionStatus) {
            if (!status) {
                return false;
            }
        }
        return true;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView objectiveText;
        CheckBox completedCheckbox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            objectiveText = itemView.findViewById(R.id.objectiveText);
            completedCheckbox = itemView.findViewById(R.id.completedCheckbox);
        }
    }
} 