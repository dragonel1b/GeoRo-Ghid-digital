package com.example.myapplication.Joc1;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.core.domain.model.Quest;
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
    
    // ActivityResultLauncher for quest challenges
    private final ActivityResultLauncher<Intent> questChallengeResultLauncher = 
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int missionId = result.getData().getIntExtra("mission_id", -1);
                    if (missionId >= 0 && missionId < missions.size()) {
                        completeMission(missions.get(missionId));
                    }
                }
            }
        );

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
        loadSavedProgress();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh UI in case anything has changed
        updateProgress();
        if (missionsRecyclerView.getAdapter() != null) {
            missionsRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void initializeViews() {
        missionsRecyclerView = findViewById(R.id.missionsRecyclerView);
        questProgress = findViewById(R.id.questProgress);
        questProgressText = findViewById(R.id.questProgressText);
        collaborationFab = findViewById(R.id.collaborationFab);

        collaborationFab.setOnClickListener(v -> showCollaborationDialog());
        
        // Add animation to the FAB
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        collaborationFab.startAnimation(pulse);
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
        // În cadrul unei aplicații reale, acestea ar proveni dintr-o bază de date sau API REST
        missions = new ArrayList<>();
        missions.add(new QuestMission(
                0,
                getString(R.string.quest1_title),
                getString(R.string.quest1_description),
                "Transilvania",
                new String[]{
                        getString(R.string.quest1_objective1),
                        getString(R.string.quest1_objective2),
                        getString(R.string.quest1_objective3)
                },
                "cultura"
        ));
        missions.add(new QuestMission(
                1,
                getString(R.string.quest2_title),
                getString(R.string.quest2_description),
                "Maramureș",
                new String[]{
                        getString(R.string.quest2_objective1),
                        getString(R.string.quest2_objective2)
                },
                "istorie"
        ));
        missions.add(new QuestMission(
                2,
                getString(R.string.quest3_title),
                getString(R.string.quest3_description),
                "Moldova",
                new String[]{
                        getString(R.string.quest3_objective1),
                        getString(R.string.quest3_objective2),
                        getString(R.string.quest3_objective3),
                        getString(R.string.quest3_objective4)
                },
                "culinara"
        ));
        missions.add(new QuestMission(
                3,
                getString(R.string.quest4_title),
                getString(R.string.quest4_description),
                "Oltenia",
                new String[]{
                        getString(R.string.quest4_objective1),
                        getString(R.string.quest4_objective2)
                },
                "explorare"
        ));
        missions.add(new QuestMission(
                4,
                getString(R.string.quest5_title),
                getString(R.string.quest5_description),
                "Muntenia",
                new String[]{
                        getString(R.string.quest5_objective1),
                        getString(R.string.quest5_objective2),
                        getString(R.string.quest5_objective3)
                },
                "provocare"
        ));
    }

    private void setupRecyclerView() {
        missionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Creăm un adaptor personalizat care gestionează obiectele QuestMission
        QuestAdapter adapter = new QuestAdapter(this.missions, this::handleMissionClick);
        missionsRecyclerView.setAdapter(adapter);
    }

    private void handleMissionClick(QuestMission mission) {
        if (mission.isCompleted()) {
            // Show completion details dialog for completed missions
            new MaterialAlertDialogBuilder(this)
                    .setTitle(mission.getTitle())
                    .setMessage(getString(R.string.quest_completed_message))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show();
            return;
        }
        
        new MaterialAlertDialogBuilder(this)
                .setTitle(mission.getTitle())
                .setMessage(getString(R.string.quest_objectives_title) + "\n\n• " + String.join("\n• ", mission.getObjectives()))
                .setPositiveButton(getString(R.string.quest_start_challenge), (dialog, which) -> startMission(mission))
                .setNegativeButton(getString(R.string.quest_cancel), null)
                .show();
    }

    private void startMission(QuestMission mission) {
        // Launch mission challenge activity
        Intent intent = new Intent(this, RomQuestChallengeActivity.class);
        intent.putExtra("mission_id", mission.getId());
        intent.putExtra("mission_title", mission.getTitle());
        intent.putExtra("mission_description", mission.getDescription());
        intent.putExtra("mission_objectives", mission.getObjectives());
        intent.putExtra("mission_type", mission.getType());
        
        // Set appropriate correct answer based on mission type
        switch (mission.getType()) {
            case "cultura":
                intent.putExtra("correct_answer", "ardeal");
                break;
            case "istorie":
                intent.putExtra("correct_answer", "1918");
                break;
            case "culinara":
                intent.putExtra("correct_answer", "sarmale");
                break;
            case "explorare":
                intent.putExtra("correct_answer", "dunare");
                break;
            case "provocare":
                intent.putExtra("correct_answer", "brancoveanu");
                break;
            default:
                intent.putExtra("correct_answer", "romania");
                break;
        }
        
        questChallengeResultLauncher.launch(intent);
    }

    private void completeMission(QuestMission mission) {
        if (!mission.isCompleted()) {
            mission.setCompleted(true);
            completedMissions++;
            updateProgress();

            // Award wisdom points and save progress
            gameState.addPuncteIntelepte(25, this);
            saveProgress();

            // Show completion feedback using Material Design components
            View rootView = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(rootView,
                    getString(R.string.quest_points_earned),
                    Snackbar.LENGTH_LONG);
            snackbar.setAction("OK", null);
            
            // Apply snackbar style customization
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(getResources().getColor(R.color.rom_success, null));
            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(getResources().getColor(R.color.white, null));
            
            snackbar.show();

            // Update RecyclerView
            missionsRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void updateProgress() {
        int totalMissions = missions.size();
        completedMissions = 0;
        
        // Count completed missions
        for (QuestMission mission : missions) {
            if (mission.isCompleted()) {
                completedMissions++;
            }
        }
        
        int progress = (completedMissions * 100) / totalMissions;
        questProgress.setProgress(progress);
        questProgressText.setText(String.format("%d/%d taine descoperite", completedMissions, totalMissions));
    }

    private void showCollaborationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sfatul Înțelepților")
                .setMessage("Alătură-te altor călători pe drumul cunoașterii pentru a descoperi taine mai adânci și a dobândi înțelepciune sporită!\n\n" +
                        "Completează cel puțin 3 misiuni pentru a debloca această funcționalitate.")
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }
    
    private void saveProgress() {
        // Save progress to SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("quest_progress", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        
        // Save completion status for each mission
        for (int i = 0; i < missions.size(); i++) {
            QuestMission mission = missions.get(i);
            editor.putBoolean("mission_" + i + "_completed", mission.isCompleted());
        }
        
        editor.apply();
    }
    
    private void loadSavedProgress() {
        android.content.SharedPreferences prefs = getSharedPreferences("quest_progress", MODE_PRIVATE);
        
        // Load completion status for each mission
        for (int i = 0; i < missions.size(); i++) {
            QuestMission mission = missions.get(i);
            boolean isCompleted = prefs.getBoolean("mission_" + i + "_completed", false);
            mission.setCompleted(isCompleted);
        }
        
        // Update progress display
        updateProgress();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Clasă internă pentru misiunile de quest
    public static class QuestMission {
        private final int id;
        private final String title;
        private final String description;
        private final String region;
        private final String[] objectives;
        private final String type;
        private boolean completed;

        public QuestMission(int id, String title, String description, String region, String[] objectives, String type) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.region = region;
            this.objectives = objectives;
            this.type = type;
            this.completed = false;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getRegion() { return region; }
        public String[] getObjectives() { return objectives; }
        public String getType() { return type; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
    
    // Adaptor personalizat pentru QuestMission
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
            private final View typeIndicator;
            
            public ViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.questTitleText);
                regionText = itemView.findViewById(R.id.questRegionText);
                statusIcon = itemView.findViewById(R.id.questStatusIcon);
                typeIndicator = itemView.findViewById(R.id.questTypeIndicator);
                
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onQuestClick(missions.get(position));
                    }
                });
            }
            
            public void bind(QuestMission mission) {
                titleText.setText(mission.getTitle());
                regionText.setText(mission.getRegion());
                
                // Set the status icon based on completion
                if (mission.isCompleted()) {
                    statusIcon.setBackgroundResource(R.drawable.ic_check_circle);
                    itemView.setAlpha(0.7f); // Slightly fade out completed missions
                } else {
                    statusIcon.setBackgroundResource(R.drawable.ic_explore);
                    itemView.setAlpha(1.0f);
                }
                
                // Set background color based on mission type
                int backgroundResId;
                switch (mission.getType()) {
                    case "cultura":
                        backgroundResId = R.drawable.quest_type_cultural_bg;
                        break;
                    case "istorie":
                        backgroundResId = R.drawable.quest_type_historical_bg;
                        break;
                    case "culinara":
                        backgroundResId = R.drawable.quest_type_culinary_bg;
                        break;
                    case "explorare":
                        backgroundResId = R.drawable.quest_type_bg;
                        break;
                    case "provocare":
                        backgroundResId = R.drawable.quest_type_challenge_bg;
                        break;
                    default:
                        backgroundResId = R.drawable.quest_type_bg;
                        break;
                }
                typeIndicator.setBackgroundResource(backgroundResId);
            }
        }
    }
}
