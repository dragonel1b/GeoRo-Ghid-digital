package com.example.myapplication.Joc1;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class RomQuestActivity extends AppCompatActivity {
    private RomGameState gameState;
    private RecyclerView missionsRecyclerView;
    private ProgressBar questProgress;
    private TextView questProgressText;
    private ExtendedFloatingActionButton collaborationFab;
    private List<QuestMission> missions;
    private int completedMissions = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_quest);

        gameState = RomGameState.getInstance();
        gameState.initialize(this);

        initializeViews();
        setupToolbar();
        setupMissions();
        setupRecyclerView();
        updateProgress();
    }

    private void initializeViews() {
        missionsRecyclerView = findViewById(R.id.missionsRecyclerView);
        questProgress = findViewById(R.id.questProgress);
        questProgressText = findViewById(R.id.questProgressText);
        collaborationFab = findViewById(R.id.collaborationFab);

        collaborationFab.setOnClickListener(v -> showCollaborationDialog());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.quest_title);
        }
    }

    private void setupMissions() {
        // In a real app, these would come from a database or REST API
        missions = new ArrayList<>();
        missions.add(new QuestMission(
                getString(R.string.quest1_title),
                getString(R.string.quest1_description),
                "Transilvania",
                new String[]{
                        getString(R.string.quest1_objective1),
                        getString(R.string.quest1_objective2),
                        getString(R.string.quest1_objective3)
                }
        ));
        missions.add(new QuestMission(
                getString(R.string.quest2_title),
                getString(R.string.quest2_description),
                "Maramureș",
                new String[]{
                        getString(R.string.quest2_objective1),
                        getString(R.string.quest2_objective2)
                }
        ));
        missions.add(new QuestMission(
                getString(R.string.quest3_title),
                getString(R.string.quest3_description),
                "Moldova",
                new String[]{
                        getString(R.string.quest3_objective1),
                        getString(R.string.quest3_objective2),
                        getString(R.string.quest3_objective3),
                        getString(R.string.quest3_objective4)
                }
        ));
        missions.add(new QuestMission(
                getString(R.string.quest4_title),
                getString(R.string.quest4_description),
                "Oltenia",
                new String[]{
                        getString(R.string.quest4_objective1),
                        getString(R.string.quest4_objective2)
                }
        ));
        missions.add(new QuestMission(
                getString(R.string.quest5_title),
                getString(R.string.quest5_description),
                "Muntenia",
                new String[]{
                        getString(R.string.quest5_objective1),
                        getString(R.string.quest5_objective2),
                        getString(R.string.quest5_objective3)
                }
        ));
    }

    private void setupRecyclerView() {
        missionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Create a custom adapter that handles QuestMission objects
        QuestAdapter adapter = new QuestAdapter(this.missions, this::handleMissionClick);
        missionsRecyclerView.setAdapter(adapter);
    }

    private void handleMissionClick(QuestMission mission) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(mission.getTitle())
                .setMessage(getString(R.string.quest_objectives_title) + "\n\n" + String.join("\n• ", mission.getObjectives()))
                .setPositiveButton(getString(R.string.quest_submit_answer), (dialog, which) -> startMission(mission))
                .setNegativeButton(getString(R.string.quest_cancel), null)
                .show();
    }

    private void startMission(QuestMission mission) {
        // Simulate mission completion (in a real app, this would be more complex)
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.quest_challenge_title))
                .setMessage(getString(R.string.quest_answer_hint))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> completeMission(mission))
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void completeMission(QuestMission mission) {
        if (!mission.isCompleted()) {
            mission.setCompleted(true);
            completedMissions++;
            updateProgress();

            // Award wisdom points and show feedback
            gameState.addPuncteIntelepte(25, this);

            // Show completion feedback using Material Design components
            View rootView = findViewById(android.R.id.content);
            Snackbar.make(rootView,
                            getString(R.string.quest_points_earned),
                            Snackbar.LENGTH_LONG)
                    .setAction("OK", null)
                    .show();

            // Update RecyclerView
            missionsRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void updateProgress() {
        int totalMissions = missions.size();
        int progress = (completedMissions * 100) / totalMissions;
        questProgress.setProgress(progress);
        questProgressText.setText(String.format("%d/%d taine descoperite", completedMissions, totalMissions));
    }

    private void showCollaborationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sfatul Înțelepților")
                .setMessage("Alătură-te altor călători pe drumul cunoașterii pentru a descoperi taine mai adânci și a dobândi înțelepciune sporită!\n\n" +
                        "(Această cale va fi deschisă în curând)")
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Inner class for quest missions
    public static class QuestMission {
        private final String title;
        private final String description;
        private final String region;
        private final String[] objectives;
        private boolean completed;

        public QuestMission(String title, String description, String region, String[] objectives) {
            this.title = title;
            this.description = description;
            this.region = region;
            this.objectives = objectives;
            this.completed = false;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getRegion() { return region; }
        public String[] getObjectives() { return objectives; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
    
    // Custom adapter for QuestMission that is separate from Mission class
    public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.ViewHolder> {
        private final List<QuestMission> missions;
        private final QuestClickListener listener;
        
        public interface QuestClickListener {
            void onQuestClick(QuestMission mission);
        }
        
        public QuestAdapter(List<QuestMission> missions, QuestClickListener listener) {
            this.missions = missions;
            this.listener = listener;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_rom_quest, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            QuestMission mission = missions.get(position);
            holder.bind(mission);
        }
        
        @Override
        public int getItemCount() {
            return missions.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView titleText;
            private final TextView regionText;
            private final View statusIcon;
            
            public ViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.missionTitle);
                regionText = itemView.findViewById(R.id.missionRegion);
                statusIcon = itemView.findViewById(R.id.missionStatus);
                
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onQuestClick(missions.get(position));
                    }
                });
            }
            
            public void bind(QuestMission mission) {
                titleText.setText(mission.getTitle());
                regionText.setText(mission.getRegion());
                statusIcon.setVisibility(mission.isCompleted() ? View.VISIBLE : View.GONE);
            }
        }
    }
}
