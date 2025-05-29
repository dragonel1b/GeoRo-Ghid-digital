package com.example.myapplication.Joc1;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter pentru afișarea obiectivelor unei misiuni într-un RecyclerView
 */
public class ObjectivesAdapter extends RecyclerView.Adapter<ObjectivesAdapter.ViewHolder> {
    private List<ObjectiveItem> objectives;

    /**
     * Constructor pentru adapter
     * 
     * @param objectives Lista de obiective ce trebuie afișate
     */
    public ObjectivesAdapter(List<ObjectiveItem> objectives) {
        this.objectives = objectives;
    }

    /**
     * Creează un adapter din lista de obiective a unei misiuni
     */
    public static ObjectivesAdapter fromMissionObjectives(List<Mission.MissionObjective> missionObjectives) {
        List<ObjectiveItem> items = new ArrayList<>();
        for (Mission.MissionObjective missionObjective : missionObjectives) {
            items.add(new ObjectiveItem(
                    missionObjective.getDescription(),
                    missionObjective.isCompleted()
            ));
        }
        return new ObjectivesAdapter(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mission_objective, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ObjectiveItem objective = objectives.get(position);
        holder.objectiveText.setText(objective.getText());

        // Aplică stilul în funcție de starea de completare
        if (objective.isCompleted()) {
            holder.objectiveText.setPaintFlags(holder.objectiveText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.objectiveText.setAlpha(0.7f);
        } else {
            holder.objectiveText.setPaintFlags(holder.objectiveText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.objectiveText.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return objectives.size();
    }

    /**
     * ViewHolder pentru elementele din listă
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView objectiveText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            objectiveText = itemView.findViewById(R.id.objectiveText);
        }
    }

    /**
     * Clasa pentru un element obiectiv
     */
    public static class ObjectiveItem {
        private String text;
        private boolean completed;

        public ObjectiveItem(String text, boolean completed) {
            this.text = text;
            this.completed = completed;
        }

        public String getText() {
            return text;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }
} 