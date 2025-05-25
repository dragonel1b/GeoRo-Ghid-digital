package com.example.myapplication.Joc1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {
    private final List<RomQuestActivity.QuestMission> missions;
    private final OnMissionClickListener listener;

    public interface OnMissionClickListener {
        void onMissionClick(RomQuestActivity.QuestMission mission);
    }

    public QuestAdapter(List<RomQuestActivity.QuestMission> missions, OnMissionClickListener listener) {
        this.missions = missions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rom_quest, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        RomQuestActivity.QuestMission mission = missions.get(position);

        holder.missionTitle.setText(mission.getTitle());
        holder.missionRegion.setText(mission.getRegion());
        holder.missionDescription.setText(mission.getDescription());

        // Update mission status
        if (mission.isCompleted()) {
            holder.missionStatus.setVisibility(View.VISIBLE);
            holder.startMissionButton.setText("Misiune Completată");
            holder.startMissionButton.setEnabled(false);
        } else {
            holder.missionStatus.setVisibility(View.GONE);
            holder.startMissionButton.setText("Începe Misiunea");
            holder.startMissionButton.setEnabled(true);
        }

        // Set click listener
        holder.startMissionButton.setOnClickListener(v -> {
            if (!mission.isCompleted() && listener != null) {
                listener.onMissionClick(mission);
            }
        });
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    static class QuestViewHolder extends RecyclerView.ViewHolder {
        TextView missionTitle;
        TextView missionRegion;
        TextView missionDescription;
        ImageView missionStatus;
        MaterialButton startMissionButton;

        QuestViewHolder(View itemView) {
            super(itemView);
            missionTitle = itemView.findViewById(R.id.missionTitle);
            missionRegion = itemView.findViewById(R.id.missionRegion);
            missionDescription = itemView.findViewById(R.id.missionDescription);
            missionStatus = itemView.findViewById(R.id.missionStatus);
            startMissionButton = itemView.findViewById(R.id.startMissionButton);
        }
    }
}
