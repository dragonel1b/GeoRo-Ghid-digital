package com.example.myapplication.Joc1;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setupMissions() {
        missions = new ArrayList<>();

        // Add missions for different regions
        missions.add(new QuestMission(
                "Misterele Sibiului Medieval",
                "Descoperă secretele Podului Minciunilor și ale Pieței Mari",
                "Sibiu",
                new String[] {
                        "Găsește și fotografiază cele mai vechi 3 case din Piața Mare",
                        "Descoperă legenda Podului Minciunilor",
                        "Identifică simbolurile breslelor medievale"
                }
        ));

        missions.add(new QuestMission(
                "Comoara Bisericii Negre",
                "Explorează cel mai mare edificiu gotic din Europa de Est",
                "Brașov",
                new String[] {
                        "Descoperă simbolistica vitraliilor",
                        "Găsește covorul oriental cel mai vechi",
                        "Află povestea clopotului mare"
                }
        ));

        missions.add(new QuestMission(
                "Legendele Clujului",
                "Descoperă poveștile ascunse ale orașului",
                "Cluj",
                new String[] {
                        "Vizitează casa lui Matei Corvin",
                        "Găsește statuia Sf. Gheorghe",
                        "Explorează criptele Bisericii Sf. Mihail"
                }
        ));

        missions.add(new QuestMission(
                "Bucureștiul Interbelic",
                "Călătorește în timp prin arhitectura capitalei",
                "București",
                new String[] {
                        "Identifică stilurile arhitecturale de pe Calea Victoriei",
                        "Descoperă poveștile Ateneului Român",
                        "Explorează Micul Paris al României"
                }
        ));

        missions.add(new QuestMission(
                "Drumul Mănăstirilor",
                "Descoperă arta și spiritualitatea Moldovei",
                "Iași",
                new String[] {
                        "Vizitează Mănăstirea Trei Ierarhi",
                        "Studiază frescele bisericești",
                        "Află tehnicile de restaurare folosite"
                }
        ));

        missions.add(new QuestMission(
                "Timișoara Multiculturală",
                "Explorează diversitatea culturală a orașului",
                "Timișoara",
                new String[] {
                        "Descoperă influențele arhitecturale austro-ungare",
                        "Vizitează cartierul Fabric",
                        "Identifică elementele Art Nouveau"
                }
        ));
    }

    private void setupRecyclerView() {
        missionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        QuestAdapter adapter = new QuestAdapter(missions, this::handleMissionClick);
        missionsRecyclerView.setAdapter(adapter);
        missionsRecyclerView.setNestedScrollingEnabled(false);
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
}
