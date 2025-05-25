package com.example.myapplication.Joc1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {

    private List<Mission> missions;
    private OnQuestClickListener listener;

    public interface OnQuestClickListener {
        void onQuestClick(Mission mission);
    }

    public QuestAdapter(List<Mission> missions, OnQuestClickListener listener) {
        this.missions = missions;
        this.listener = listener;
    }
    
    public void updateMissions(List<Mission> newMissions) {
        this.missions = newMissions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quest, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        Mission mission = missions.get(position);
        holder.bind(mission);
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    class QuestViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView typeText;
        private TextView progressText;
        private ProgressBar progressBar;
        private ImageView missionIcon;
        private MaterialCardView cardView;
        private TextView chapterText;

        public QuestViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.questTitle);
            typeText = itemView.findViewById(R.id.questType);
            progressText = itemView.findViewById(R.id.questProgressText);
            progressBar = itemView.findViewById(R.id.questProgress);
            missionIcon = itemView.findViewById(R.id.questIcon);
            cardView = (MaterialCardView) itemView;
            chapterText = itemView.findViewById(R.id.questChapter);
        }

        public void bind(Mission mission) {
            titleText.setText(mission.getDescription());
            
            // Set chapter and step info
            if (chapterText != null) {
                chapterText.setText("Capitol " + mission.getChapter() + " · Pas " + mission.getStep());
            }
            
            // Set mission type text
            String typeString = "Misiune: ";
            int missionType = mission.getType();
            
            if (missionType == Mission.TYPE_EXPLORATION) {
                typeString += "Explorare";
                missionIcon.setImageResource(R.drawable.ic_attraction);
            } else if (missionType == Mission.TYPE_CULTURAL) {
                typeString += "Cunoștințe";
                missionIcon.setImageResource(R.drawable.ic_quiz);
            } else if (missionType == Mission.TYPE_CULINARY) {
                typeString += "Culinară";
                missionIcon.setImageResource(R.drawable.ic_food);
            } else {
                typeString += "Aventură";
                missionIcon.setImageResource(R.drawable.ic_mission);
            }
            
            typeText.setText(typeString);
            
            // Set progress based on mission type
            int progress = 0;
            String progressString = "";
            
            if (mission.isCompleted()) {
                progress = 100;
                progressString = "Completat!";
            } else {
                // For objectives-based missions, count completed objectives
                int completedObjectives = 0;
                int totalObjectives = mission.getObjectives().size();
                
                for (Mission.MissionObjective objective : mission.getObjectives()) {
                    if (objective.isCompleted()) {
                        completedObjectives++;
                    }
                }
                
                if (totalObjectives > 0) {
                    progress = completedObjectives * 100 / totalObjectives;
                    progressString = completedObjectives + "/" + totalObjectives;
                } else {
                    progress = 0;
                    progressString = "Nu a început";
                }
            }
            
            progressBar.setProgress(progress);
            progressText.setText(progressString);
            
            // Special highlight for active story mission
            if (mission.isCompleted()) {
                cardView.setStrokeColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                cardView.setStrokeWidth(3);
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.completed_mission_bg));
            } else if (mission.isActive()) {
                cardView.setStrokeColor(itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
                cardView.setStrokeWidth(3);
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.active_mission_bg));
                
                // Highlight current story mission
                if (isCurrentStoryMission(mission)) {
                    cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.story_mission_bg));
                }
            } else {
                cardView.setStrokeColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                cardView.setStrokeWidth(1);
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.white));
            }
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuestClick(mission);
                }
            });
        }
        
        private boolean isCurrentStoryMission(Mission mission) {
            RomGameState gameState = RomGameState.getInstance();
            return mission.getChapter() == gameState.getStoryChapter() &&
                   mission.getStep() == gameState.getStoryStep();
        }
    }
}
