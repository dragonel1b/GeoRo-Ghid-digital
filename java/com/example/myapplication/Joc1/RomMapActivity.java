package com.example.myapplication.Joc1;

import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.view.MotionEvent;
import java.util.Map;
import java.util.HashMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import androidx.annotation.NonNull;
import android.content.res.Configuration;
import com.google.android.gms.maps.model.MapStyleOptions;
import androidx.appcompat.widget.SearchView;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.Intent;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.Dobrogea;
import com.example.myapplication.RomApp.Transilvania;
import com.example.myapplication.RomApp.Moldova;
import com.example.myapplication.RomApp.Oltenia;
import com.example.myapplication.RomApp.Muntenia;
import com.example.myapplication.RomApp.Banat;
import com.example.myapplication.RomApp.Crisana;
import com.example.myapplication.RomApp.Maramures;
import com.example.myapplication.RomApp.Bucovina;
import java.util.List;
import java.util.ArrayList;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import android.widget.LinearLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import com.google.android.gms.maps.model.Marker;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import android.graphics.Color;
import android.view.ViewGroup;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import android.util.Log;
import com.google.firebase.analytics.FirebaseAnalytics;

public class RomMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final Map<String, Rect> regionBounds = new HashMap<>();
    private MapView mapView;
    private GoogleMap googleMap;
    private RomGameState gameState;
    private MaterialCardView regionInfoCard;
    private TextView regionNameText;
    private TextView regionDescriptionText;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private String currentRegion = null;
    private List<Mission> availableMissions;
    private Map<String, Marker> missionMarkers = new HashMap<>();
    private RecyclerView questRecyclerView;
    private QuestAdapter questAdapter;
    private ExtendedFloatingActionButton discoverFab;
    private boolean isDiscoveryMode = false;
    private final Handler handler = new Handler();
    private final Map<String, Mission> storyMissions = new HashMap<>();
    private final Map<String, NPC> storyCharacters = new HashMap<>();
    private RecyclerView missionsRecyclerView;
    private MaterialCardView storyCard;
    private TextView storyTitleText;
    private TextView storyDescriptionText;
    private TextView characterNameText;
    private TextView characterDialogText;
    private ImageView characterImageView;
    private int currentStoryChapter = 1;
    private int currentStoryStep = 1;
    private boolean isInStoryMode = false;
    private String currentActiveRegion = null;
    private NPC currentSpeakingCharacter = null;
    private List<Mission> activeMissions;
    private List<Mission> completedMissions;
    private Map<String, com.google.android.gms.maps.model.Polygon> regionPolygons = new HashMap<>();
    private boolean regionClickListenerAdded = false;
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_story);

        // Initialize Google Map
        mapView = findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        // Initialize region bounds
        regionBounds.put("banat", new Rect(52, 220, 87, 258));
        regionBounds.put("crisana", new Rect(76, 120, 108, 153));
        regionBounds.put("maramures", new Rect(104, 84, 130, 111));
        regionBounds.put("bucovina", new Rect(200, 112, 223, 138));
        regionBounds.put("transilvania", new Rect(148, 120, 183, 157));
        regionBounds.put("moldova", new Rect(236, 124, 265, 155));
        regionBounds.put("oltenia", new Rect(132, 212, 159, 247));
        regionBounds.put("muntenia", new Rect(200, 256, 236, 290));
        regionBounds.put("dobrogea", new Rect(284, 244, 316, 267));

        // Initialize views and game state
        gameState = RomGameState.getInstance();
        gameState.initialize(this);
        initializeViews();
        setupRegionButtons();
        setupCityMarkers();
        loadAvailableMissions();
        setupQuestList();
        applyEntryAnimations();
        setupSearchBar();

        // Setup story missions and characters
        setupStoryCharacters();
        setupStoryMissions();
        
        // Check if we should resume a story
        checkStoryProgress();
        
        // Load missions
        loadMissions();
        setupMissionsList();

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void initializeViews() {
        // Back button is now an ImageButton
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        
        // Adăugăm un listener pentru apăsare lungă pe butonul Back pentru debugging
        backButton.setOnLongClickListener(v -> {
            // Afișăm informațiile de debugging
            showRegionDebugInfo();
            return true;
        });

        // Missions FAB setup
        FloatingActionButton showMissionsFab = findViewById(R.id.showMissionsFab);
        if (showMissionsFab != null) {
            showMissionsFab.setOnClickListener(v -> toggleMissionsPanel());
        }
        
        // Missions panel close button
        MaterialButton closeMissionsButton = findViewById(R.id.closeMissionsButton);
        if (closeMissionsButton != null) {
            closeMissionsButton.setOnClickListener(v -> hideMissionsPanel());
        }

        // Region info card setup
        View regionInfoCard = findViewById(R.id.regionInfoCard);
        if (regionInfoCard != null) {
            this.regionInfoCard = (MaterialCardView) regionInfoCard;
            this.regionNameText = findViewById(R.id.regionNameText);
            this.regionDescriptionText = findViewById(R.id.regionDescriptionText);
            this.bottomSheetBehavior = BottomSheetBehavior.from(regionInfoCard);
            this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        
        questRecyclerView = findViewById(R.id.questRecyclerView);
        discoverFab = findViewById(R.id.discoverFab);
        
        if (discoverFab != null) {
            discoverFab.setOnClickListener(v -> toggleDiscoveryMode());
            // Adăugăm listener pentru apăsare lungă pentru a afișa informații despre regiuni
            discoverFab.setOnLongClickListener(v -> {
                showRegionDebugInfo();
                return true;
            });
        }

        storyCard = findViewById(R.id.storyCard);
        storyTitleText = findViewById(R.id.storyTitleText);
        storyDescriptionText = findViewById(R.id.storyDescriptionText);
        characterNameText = findViewById(R.id.characterNameText);
        characterDialogText = findViewById(R.id.characterDialogText);
        characterImageView = findViewById(R.id.characterImageView);
        missionsRecyclerView = findViewById(R.id.missionsRecyclerView);
        
        // Setup continue button
        MaterialButton continueButton = findViewById(R.id.continueButton);
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> advanceStory());
        }

        // Setup explore button
        MaterialButton exploreButton = findViewById(R.id.exploreButton);
        if (exploreButton != null) {
            exploreButton.setOnClickListener(v -> {
                if (currentRegion != null) {
                    navigateToRegion(currentRegion);
                }
            });
        }
    }

    private void toggleMissionsPanel() {
        View missionsPanelCard = findViewById(R.id.missionsPanelCard);
        if (missionsPanelCard != null) {
            if (missionsPanelCard.getVisibility() == View.VISIBLE) {
                hideMissionsPanel();
            } else {
                showMissionsPanel();
            }
        }
    }

    private void showMissionsPanel() {
        View missionsPanelCard = findViewById(R.id.missionsPanelCard);
        if (missionsPanelCard != null) {
            missionsPanelCard.setVisibility(View.VISIBLE);
            missionsPanelCard.setTranslationX(missionsPanelCard.getWidth());
            missionsPanelCard.animate()
                    .translationX(0)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void hideMissionsPanel() {
        View missionsPanelCard = findViewById(R.id.missionsPanelCard);
        if (missionsPanelCard != null && missionsPanelCard.getVisibility() == View.VISIBLE) {
            missionsPanelCard.animate()
                    .translationX(missionsPanelCard.getWidth())
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> missionsPanelCard.setVisibility(View.GONE))
                    .start();
        }
    }

    private void toggleDiscoveryMode() {
        isDiscoveryMode = !isDiscoveryMode;
        
        if (isDiscoveryMode) {
            Toast.makeText(this, "Mod de descoperire activat! Apasă pe hartă pentru a descoperi activități.", 
                Toast.LENGTH_SHORT).show();
            
            // Using ExtendedFloatingActionButton
            discoverFab.setIconTint(getColorStateList(R.color.rom_accent_color));
            discoverFab.setText("Activ");
            
            // Animate the FAB
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(discoverFab, "scaleX", 1f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(discoverFab, "scaleY", 1f, 1.1f, 1f);
            scaleX.setDuration(500);
            scaleY.setDuration(500);
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.start();
            
            // Show pulsing animation for missions on map
            pulseMissionMarkers(true);
        } else {
            discoverFab.setIconTint(getColorStateList(R.color.rom_icon_color));
            discoverFab.setText("Descoperă");
            pulseMissionMarkers(false);
        }
    }
    
    private void pulseMissionMarkers(boolean enabled) {
        for (Marker marker : missionMarkers.values()) {
            if (enabled) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        float[] hsv = new float[3];
                        android.graphics.Color.colorToHSV(android.graphics.Color.BLUE, hsv);
                        hsv[0] = (hsv[0] + 10) % 360;
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(hsv[0]));
                        
                        if (isDiscoveryMode) {
                            handler.postDelayed(this, 500);
                        }
                    }
                });
            } else {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            }
        }
    }

    private void setupSearchBar() {
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchBar);
        if (searchView != null) {
            // Make search text more visible
            TextView searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            if (searchText != null) {
                searchText.setTextColor(getResources().getColor(R.color.rom_text_primary));
                searchText.setHintTextColor(getResources().getColor(R.color.rom_text_secondary));
            }
            
            searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Search for regions, cities or activities
                    performSearch(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Real-time filtering could be added here
                    return false;
                }
            });
        }
    }
    
    private void performSearch(String query) {
                // Show search dialog with region and city options
                final String[] searchOptions = new String[] {
                    "Regiuni", "Orașe", "Obiective turistice", "Activități"
                };
                
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Caută în România: " + query)
                       .setItems(searchOptions, (dialog, which) -> {
                           switch (which) {
                               case 0: // Regiuni
                                   showRegionList();
                                   break;
                               case 1: // Orașe
                                   showCityList();
                                   break;
                               case 2: // Obiective turistice
                                   Toast.makeText(this, "Obiective turistice în curând!", Toast.LENGTH_SHORT).show();
                                   break;
                               case 3: // Activități
                                   showQuestList();
                                   break;
                           }
                       });
                builder.create().show();
    }
    
    private void showRegionList() {
        final String[] regions = new String[] {
            "Banat", "Crișana", "Maramureș", "Bucovina", 
            "Transilvania", "Moldova", "Oltenia", "Muntenia", "Dobrogea"
        };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alege o regiune")
               .setItems(regions, (dialog, which) -> {
                   String regionId = regions[which].toLowerCase();
                   navigateToRegion(regionId);
               });
        builder.create().show();
    }
    
    private void showCityList() {
        final String[] cities = new String[] {
            "București", "Cluj-Napoca", "Timișoara", "Iași", 
            "Constanța", "Craiova", "Brașov", "Galați", "Sibiu"
        };
        
        final String[] cityTags = new String[] {
            "bucuresti", "cluj", "timisoara", "iasi", 
            "constanta", "craiova", "brasov", "galati", "sibiu"
        };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alege un oraș")
               .setItems(cities, (dialog, which) -> {
                   navigateToRegion(cityTags[which]);
               });
        builder.create().show();
    }
    
    private void showQuestList() {
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void setupRegionButtons() {
        // Setup story progression regions with interactive buttons
        LinearLayout regionsContainer = findViewById(R.id.regionsContainer);
        if (regionsContainer != null) {
            regionsContainer.removeAllViews(); // Clear any existing buttons
            
            String[] regions = {"transilvania", "moldova", "muntenia", "dobrogea", "oltenia", "banat"};
            String[] regionNames = {"Transilvania", "Moldova", "Muntenia", "Dobrogea", "Oltenia", "Banat"};
            
            for (int i = 0; i < regions.length; i++) {
                final String regionId = regions[i];
                final String regionName = regionNames[i];
                
                MaterialButton regionButton = new MaterialButton(this);
                regionButton.setText(regionName);
                regionButton.setTag(regionId);
                regionButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                regionButton.setPadding(12, 8, 12, 8);
                
                // Use chip style for region buttons
                regionButton.setStrokeWidth(1);
                regionButton.setStrokeColorResource(R.color.rom_accent_color);
                regionButton.setBackgroundTintList(getColorStateList(android.R.color.white));
                regionButton.setTextColor(getResources().getColor(R.color.rom_text_primary));
                
                // Disable regions not yet unlocked in the story
                if (currentStoryChapter < i + 1) {
                    regionButton.setEnabled(false);
                    regionButton.setAlpha(0.5f);
                }
                
                regionButton.setOnClickListener(v -> selectRegion(regionId));
                
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(8, 4, 8, 4);
                regionsContainer.addView(regionButton, params);
            }
        }
    }
    
    private void setupStoryCharacters() {
        // Create main story characters
        NPC dragomir = new NPC("Dragomir", "Călăuza ta prin călătoria din România", 
                R.drawable.ic_character_guide);
        dragomir.addDialog("Bine ai venit în România! Sunt Dragomir și te voi ghida în această călătorie.");
        dragomir.addDialog("Vom explora împreună țara și vom descoperi obiceiuri, tradiții și locuri minunate.");
        dragomir.addDialog("Călătoria noastră începe în Transilvania, ținutul legendelor și castelelor.");
        storyCharacters.put("dragomir", dragomir);
        
        NPC elena = new NPC("Elena", "Cercetătoare în istorie și tradiții populare", 
                R.drawable.ic_character_historian);
        elena.addDialog("Salut! Sunt Elena, pasiunea mea este istoria României.");
        elena.addDialog("Te pot ajuta să înțelegi mai bine tradițiile și obiceiurile noastre străvechi.");
        elena.addDialog("Sunt multe de descoperit despre originile dacice și romane ale poporului nostru.");
        storyCharacters.put("elena", elena);
        
        NPC mihai = new NPC("Mihai", "Bucătar tradițional și povestitor", 
                R.drawable.ic_character_chef);
        mihai.addDialog("Bucătăria românească are o istorie bogată! Eu sunt Mihai și te voi ghida prin gusturile României.");
        mihai.addDialog("De la sarmale la mămăligă, fiecare regiune are specialitățile ei!");
        storyCharacters.put("mihai", mihai);
        
        NPC ioana = new NPC("Ioana", "Artistă populară și meșter", 
                R.drawable.ic_character_artist);
        ioana.addDialog("Arta populară românească este plină de simboluri! Eu sunt Ioana și îți voi arăta tehnicile tradiționale.");
        ioana.addDialog("Fiecare model și culoare are o semnificație specială în cultura noastră.");
        storyCharacters.put("ioana", ioana);
    }
    
    private void setupStoryMissions() {
        availableMissions = new ArrayList<>();
        activeMissions = new ArrayList<>();
        completedMissions = new ArrayList<>();
        
        // Chapter 1: Transilvania
        Mission mission1 = new Mission("Primul pas în călătoria ta", 1, 100, Mission.MissionType.VISIT_ATTRACTIONS, "transilvania");
        mission1.addObjective("Întâlnește-te cu Dragomir, ghidul tău");
        mission1.addObjective("Află despre tradițiile transilvănene");
        mission1.addObjective("Vizitează un castel medieval");
        mission1.setChapter(1);
        mission1.setStep(1);
        storyMissions.put("mission1", mission1);
        
        Mission mission2 = new Mission("Legendele Transilvaniei", 1, 150, Mission.MissionType.ANSWER_QUIZ, "transilvania");
        mission2.addObjective("Află trei legende locale");
        mission2.addObjective("Completează un quiz despre Dracula");
        mission2.addObjective("Găsește artefacte istorice");
        mission2.setChapter(1);
        mission2.setStep(2);
        storyMissions.put("mission2", mission2);
        
        // Chapter 2: Moldova
        Mission mission3 = new Mission("Mânăstirile pictate", 1, 200, Mission.MissionType.TAKE_PHOTO, "moldova");
        mission3.addObjective("Vizitează mânăstirea Voroneț");
        mission3.addObjective("Descoperă secretul albastrul de Voroneț");
        mission3.addObjective("Fotografiază 3 fresce celebre");
        mission3.setChapter(2);
        mission3.setStep(1);
        storyMissions.put("mission3", mission3);
        
        // Chapter 3: Muntenia
        Mission mission4 = new Mission("Vechea capitală", 1, 150, Mission.MissionType.VISIT_ATTRACTIONS, "muntenia");
        mission4.addObjective("Explorează Palatul Parlamentului");
        mission4.addObjective("Vizitează Ateneul Român");
        mission4.addObjective("Descoperă Bucureștiul istoric");
        mission4.setChapter(3);
        mission4.setStep(1);
        storyMissions.put("mission4", mission4);
        
        // Chapter 4: Dobrogea
        Mission mission5 = new Mission("Poarta spre Mare", 1, 200, Mission.MissionType.INTERACT_NPC, "dobrogea");
        mission5.addObjective("Vizitează Cazinoul din Constanța");
        mission5.addObjective("Descoperă cetățile antice");
        mission5.addObjective("Află despre cultura multietnică din Dobrogea");
        mission5.setChapter(4);
        mission5.setStep(1);
        storyMissions.put("mission5", mission5);
        
        // Adaugă misiunile la available sau completed în funcție de progresul salvat
        for (Mission mission : storyMissions.values()) {
            if (mission.getChapter() < currentStoryChapter || 
                (mission.getChapter() == currentStoryChapter && mission.getStep() <= currentStoryStep)) {
                availableMissions.add(mission);
            }
        }
    }
    
    private void checkStoryProgress() {
        // Load progress from game state
        currentStoryChapter = gameState.getStoryChapter();
        currentStoryStep = gameState.getStoryStep();
        
        // If first time, start the intro
        if (currentStoryChapter == 1 && currentStoryStep == 1) {
            startStoryMode("dragomir");
        }
    }
    
    private void startStoryMode(String characterId) {
        isInStoryMode = true;
        currentSpeakingCharacter = storyCharacters.get(characterId);
        
        if (currentSpeakingCharacter != null) {
            storyCard.setVisibility(View.VISIBLE);
            characterNameText.setText(currentSpeakingCharacter.getName());
            characterDialogText.setText(currentSpeakingCharacter.getDialogs().get(0));
            characterImageView.setImageResource(currentSpeakingCharacter.getImageResourceId());
            
            // Hide missions during story
            missionsRecyclerView.setVisibility(View.GONE);
            
            // Show character with animation
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(storyCard, "alpha", 0f, 1f);
            fadeIn.setDuration(500);
            animatorSet.play(fadeIn);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.start();
        }
    }
    
    private void advanceStory() {
        // Check if we're in story mode
        if (!isInStoryMode || currentSpeakingCharacter == null) {
            return;
        }
        
        // Get current dialog index
        int currentDialogIndex = currentSpeakingCharacter.getCurrentDialogIndex();
        
        // Check if there are more dialogs for this character
        if (currentDialogIndex < currentSpeakingCharacter.getDialogs().size() - 1) {
            // Show next dialog
            currentSpeakingCharacter.setCurrentDialogIndex(currentDialogIndex + 1);
            characterDialogText.setText(currentSpeakingCharacter.getDialogs().get(currentDialogIndex + 1));
        } else {
            // End of dialog, exit story mode
            isInStoryMode = false;
            storyCard.setVisibility(View.GONE);
            missionsRecyclerView.setVisibility(View.VISIBLE);
            
            // Activate the first mission if not already active
            if (currentStoryChapter == 1 && currentStoryStep == 1) {
                Mission firstMission = storyMissions.get("mission1");
                if (firstMission != null && !firstMission.isActive()) {
                    firstMission.setActive(true);
                    activeMissions.add(firstMission);
                    Toast.makeText(this, "Misiune nouă disponibilă: " + firstMission.getDescription(), 
                        Toast.LENGTH_LONG).show();
                    updateMissionsList();
                }
            }
        }
    }
    
    private void loadMissions() {
        // Load existing missions based on story progress
        for (Mission mission : storyMissions.values()) {
            if (mission.isCompleted()) {
                completedMissions.add(mission);
            } else if (mission.isActive()) {
                activeMissions.add(mission);
            } else if (mission.getChapter() <= currentStoryChapter) {
                // Only show missions from current or previous chapters
                availableMissions.add(mission);
            }
        }
    }
    
    private void setupMissionsList() {
        if (missionsRecyclerView != null) {
            List<Mission> allMissions = new ArrayList<>();
            allMissions.addAll(activeMissions);
            allMissions.addAll(availableMissions);
            allMissions.addAll(completedMissions);
            
            questAdapter = new QuestAdapter(allMissions, mission -> {
                // Show mission details dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View missionView = getLayoutInflater().inflate(R.layout.dialog_mission_details, null);
                
                TextView titleText = missionView.findViewById(R.id.missionTitleText);
                RecyclerView objectivesRecycler = missionView.findViewById(R.id.objectivesRecyclerView);
                MaterialButton acceptButton = missionView.findViewById(R.id.acceptMissionButton);
                TextView missionTypeText = missionView.findViewById(R.id.missionTypeText);
                
                if (titleText != null) {
                    titleText.setText(mission.getDescription());
                }
                
                if (missionTypeText != null) {
                    missionTypeText.setText("Capitol " + mission.getChapter() + ", Pas " + mission.getStep());
                }
                
                if (objectivesRecycler != null) {
                    ObjectivesAdapter adapter = ObjectivesAdapter.fromMissionObjectives(mission.getObjectives());
                    objectivesRecycler.setLayoutManager(new LinearLayoutManager(this));
                    objectivesRecycler.setAdapter(adapter);
                }
                
                builder.setView(missionView);
                AlertDialog dialog = builder.create();
                
                if (acceptButton != null) {
                    // Change button text based on mission state
                    if (mission.isCompleted()) {
                        acceptButton.setText("Misiune Completată");
                        acceptButton.setEnabled(false);
                    } else if (mission.isActive()) {
                        acceptButton.setText("Continuă Misiunea");
                    } else {
                        acceptButton.setText("Acceptă Misiunea");
                    }
                    
                    acceptButton.setOnClickListener(v -> {
                        if (!mission.isActive() && !mission.isCompleted()) {
                            activateMission(mission);
                        } else if (mission.isActive()) {
                            // Continue mission - go to specific activity
                            continueActiveMission(mission);
                        }
                        dialog.dismiss();
                    });
                }
                
                dialog.show();
            });
            
            missionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            missionsRecyclerView.setAdapter(questAdapter);
        }
    }
    
    private void activateMission(Mission mission) {
        // Check if we can activate this mission based on story progress
        if (mission.getChapter() <= currentStoryChapter) {
            // Start story dialog for this mission if needed
            startMissionDialog(mission);
            
            // Activate mission
            mission.setActive(true);
            
            // Update mission lists
            if (availableMissions.contains(mission)) {
                availableMissions.remove(mission);
                activeMissions.add(mission);
            }
            
            // Update UI
            updateMissionsList();
            
            Toast.makeText(this, "Misiune acceptată: " + mission.getDescription(), 
                Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Trebuie să completezi capitolele anterioare întâi!", 
                Toast.LENGTH_SHORT).show();
        }
    }
    
    private void startMissionDialog(Mission mission) {
        // Show character dialog for this mission
        String characterId = "dragomir"; // Default character
        
        // Get appropriate character based on mission type
        if (mission.getType() == Mission.TYPE_CULTURAL) {
            characterId = "elena"; // Historian for quiz missions
        } else if (mission.getType() == Mission.TYPE_EXPLORATION) {
            characterId = "ioana"; // Artist for cultural missions
        }
        
        startStoryMode(characterId);
    }
    
    private void continueActiveMission(Mission mission) {
        // Navigate to appropriate activity based on mission
        if (mission.getType() == Mission.TYPE_EXPLORATION) {
            navigateToRegion(mission.getCityName());
        } else if (mission.getType() == Mission.TYPE_CULTURAL) {
            startQuizActivity(mission);
        } else if (mission.getType() == Mission.TYPE_CULINARY) {
            startPhotoActivity(mission);
        } else {
            navigateToRegion(mission.getCityName());
        }
    }
    
    private void startQuizActivity(Mission mission) {
        // In a full implementation, this would launch a quiz activity
        Toast.makeText(this, "Se lansează quiz-ul pentru " + mission.getDescription(), 
            Toast.LENGTH_SHORT).show();
            
        // For demo purposes, let's simulate completion
        completeObjectiveWithConfirmation(mission, 0);
    }
    
    private void startPhotoActivity(Mission mission) {
        // In a full implementation, this would launch a camera activity
        Toast.makeText(this, "Se lansează camera pentru " + mission.getDescription(), 
            Toast.LENGTH_SHORT).show();
            
        // For demo purposes, let's simulate completion
        completeObjectiveWithConfirmation(mission, 0);
    }
    
    private void completeObjectiveWithConfirmation(Mission mission, int objectiveIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Simulare completare obiectiv")
               .setMessage("Vrei să marchezi acest obiectiv ca finalizat?")
               .setPositiveButton("Da", (dialog, which) -> {
                   mission.completeObjective(objectiveIndex);
                   checkMissionCompletion(mission);
                   updateMissionsList();
               })
               .setNegativeButton("Nu", null)
               .show();
    }
    
    private void checkMissionCompletion(Mission mission) {
        // Check if all objectives are completed
        boolean allCompleted = true;
        for (Mission.MissionObjective objective : mission.getObjectives()) {
            if (!objective.isCompleted()) {
                allCompleted = false;
                break;
            }
        }
        
        if (allCompleted) {
            mission.setCompleted(true);
            activeMissions.remove(mission);
            completedMissions.add(mission);
            
            // Award points
            gameState.addPuncteIntelepte(mission.getRewardPoints(), this);
            
            // Advance story progress if this was a story mission
            if (mission.getChapter() == currentStoryChapter && mission.getStep() == currentStoryStep) {
                advanceStoryProgress();
            }
            
            // Update UI and show notification
            updateMissionsList();
            showMissionCompletedDialog(mission);
        }
    }
    
    private void advanceStoryProgress() {
        // Find next mission in story
        Mission nextMission = null;
        int nextStep = currentStoryStep + 1;
        int nextChapter = currentStoryChapter;
        
        // Check if there are more steps in current chapter
        for (Mission mission : storyMissions.values()) {
            if (mission.getChapter() == currentStoryChapter && mission.getStep() == nextStep) {
                nextMission = mission;
                break;
            }
        }
        
        // If no more steps, advance to next chapter
        if (nextMission == null) {
            nextChapter++;
            nextStep = 1;
            
            for (Mission mission : storyMissions.values()) {
                if (mission.getChapter() == nextChapter && mission.getStep() == nextStep) {
                    nextMission = mission;
                    break;
                }
            }
        }
        
        // Update story progress
        currentStoryChapter = nextChapter;
        currentStoryStep = nextStep;
        gameState.setStoryProgress(currentStoryChapter, currentStoryStep);
        
        // Activate next mission if available
        if (nextMission != null) {
            // Add to available missions
            if (!availableMissions.contains(nextMission)) {
                availableMissions.add(nextMission);
            }
            
            // Update UI
            updateMissionsList();
        }
    }
    
    private void showMissionCompletedDialog(Mission mission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Misiune Completată!")
               .setMessage("Felicitări! Ai completat misiunea: " + mission.getDescription() + 
                           "\n\nRecompensă: " + mission.getRewardPoints() + " Puncte Înțelepte")
               .setPositiveButton("Excelent!", null)
               .show();
    }
    
    private void updateMissionsList() {
        if (questAdapter != null) {
            List<Mission> allMissions = new ArrayList<>();
            allMissions.addAll(activeMissions);
            allMissions.addAll(availableMissions);
            allMissions.addAll(completedMissions);
            
            questAdapter.updateMissions(allMissions);
        }
    }
    
    private void selectRegion(String regionId) {
        if (regionId == null) return;
        
        // Update the current selected region
        currentRegion = regionId;
        
        // Center map on the region
        centerMapOnRegion(regionId);
        
        // Show information about the region
        showRegionInfo(regionId);
        
        // Visual feedback for selection
        flashRegion(regionId);
        
        // Log analytics event
        try {
            Bundle params = new Bundle();
            params.putString("region_id", regionId);
            firebaseAnalytics.logEvent("region_selected", params);
        } catch (Exception e) {
            // Firebase might not be initialized in some contexts
            Log.e(TAG, "Error logging analytics: " + e.getMessage());
        }
        
        // Update UI elements to reflect the selected region
        updateRegionSelectionUI(regionId);
    }
    
    private void updateRegionSelectionUI(String regionId) {
        // Update the region selection buttons
        LinearLayout regionsContainer = findViewById(R.id.regionsContainer);
        if (regionsContainer != null) {
            for (int i = 0; i < regionsContainer.getChildCount(); i++) {
                View child = regionsContainer.getChildAt(i);
                if (child instanceof MaterialButton) {
                    MaterialButton button = (MaterialButton) child;
                    String buttonRegionId = (String) button.getTag();
                    
                    if (buttonRegionId != null && buttonRegionId.equals(regionId)) {
                        // Highlight the selected region button
                        button.setStrokeColorResource(R.color.rom_accent);
                        button.setStrokeWidth(4);
                        button.setTextColor(getResources().getColor(R.color.rom_accent));
                    } else {
                        // Reset other buttons
                        button.setStrokeColorResource(R.color.rom_accent_color);
                        button.setStrokeWidth(1);
                        button.setTextColor(getResources().getColor(R.color.rom_text_primary));
                    }
                }
            }
        }
        
        // Also update the polygon colors if we have them stored
        if (regionPolygons != null) {
            for (Map.Entry<String, com.google.android.gms.maps.model.Polygon> entry : regionPolygons.entrySet()) {
                String polygonRegionId = entry.getKey();
                com.google.android.gms.maps.model.Polygon polygon = entry.getValue();
                
                if (polygonRegionId.equals(regionId)) {
                    // Highlight the selected region's polygon
                    int originalColor = getRegionColor(polygonRegionId);
                    polygon.setFillColor(originalColor & 0x7FFFFFFF); // More opaque
                    polygon.setStrokeColor(Color.WHITE);
                    polygon.setStrokeWidth(4);
                } else {
                    // Reset other polygons
                    int originalColor = getRegionColor(polygonRegionId);
                    polygon.setFillColor(originalColor & 0x4FFFFFFF); // Original transparency
                    polygon.setStrokeColor(Color.WHITE);
                    polygon.setStrokeWidth(2.5f);
                }
            }
        }
    }
    
    private int getRegionColor(String regionId) {
        switch (regionId.toLowerCase()) {
            case "transilvania": return getResources().getColor(R.color.rom_region_transilvania);
            case "moldova": return getResources().getColor(R.color.rom_region_moldova);
            case "muntenia": return getResources().getColor(R.color.rom_region_muntenia);
            case "dobrogea": return getResources().getColor(R.color.rom_region_dobrogea);
            case "oltenia": return getResources().getColor(R.color.rom_region_oltenia);
            case "banat": return getResources().getColor(R.color.rom_region_banat);
            case "crisana": return getResources().getColor(R.color.rom_region_crisana);
            case "maramures": return getResources().getColor(R.color.rom_region_maramures);
            case "bucovina": return getResources().getColor(R.color.rom_region_bucovina);
            default: return Color.GRAY;
        }
    }
    
    private void centerMapOnRegion(String regionId) {
        // Get coordinates for the region center
        LatLng[] coordinates = getRegionCoordinates(regionId);
        if (coordinates == null || coordinates.length == 0) return;
        
        // Calculate center point of the region
        double latSum = 0, lngSum = 0;
        for (LatLng coord : coordinates) {
            latSum += coord.latitude;
            lngSum += coord.longitude;
        }
        LatLng center = new LatLng(latSum / coordinates.length, lngSum / coordinates.length);
        
        // Set appropriate zoom level based on region size
        float zoom = 7.5f; // Default zoom level
        float tilt = 15f;  // Default tilt
        
        // Adjust zoom for specific regions
        switch (regionId.toLowerCase()) {
            case "transilvania":
                zoom = 7.2f; // Larger region needs less zoom
                break;
            case "dobrogea":
                zoom = 8.0f; // Smaller region can be more zoomed in
                break;
            case "bucuresti":
                zoom = 10.5f; // City needs much more zoom
                tilt = 30f;   // More tilt for city view
                break;
        }
        
        // Animate camera to center on the region
        animateCamera(center, zoom, tilt, 1500);
    }
    
    private void navigateToRegion(String regionId) {
        Class<?> regionActivity = getRegionActivityClass(regionId);
        if (regionActivity != null) {
            try {
                Intent intent = new Intent(this, regionActivity);
                // Add flags for debugging
                intent.putExtra("SOURCE", "ROM_MAP_ACTIVITY");
                intent.putExtra("REGION_ID", regionId);
                
                // Log for debugging
                Log.d("RomMapActivity", "Attempting to navigate to region: " + regionId 
                    + " using class: " + regionActivity.getName());
                
                // Log analytics event
                Bundle params = new Bundle();
                params.putString("region_id", regionId);
                params.putString("region_class", regionActivity.getName());
                firebaseAnalytics.logEvent("navigate_to_region", params);
                
                startActivity(intent);
            } catch (Exception e) {
                // Catch and display any error
                String errorMessage = "Error navigating to " + regionId + ": " + e.getMessage();
                Log.e("RomMapActivity", errorMessage, e);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                
                // Show an error dialog with details to help with debugging
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Navigation Error")
                       .setMessage("Could not navigate to region: " + regionId + "\n\n" +
                                  "Error details: " + e.toString() + "\n\n" +
                                  "Check the following:\n" +
                                  "1. Class exists: " + regionActivity.getName() + "\n" +
                                  "2. Class is a valid activity\n" +
                                  "3. Activity is declared in AndroidManifest.xml")
                       .setPositiveButton("OK", null)
                       .show();
            }
        } else {
            // Show explicit message if region is not found
            String errorMessage = "Region " + regionId + " was not found or is not available.";
            Log.e("RomMapActivity", errorMessage);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }
    
    private void showRegionInfo(String regionId) {
        if (regionInfoCard != null && regionNameText != null && regionDescriptionText != null) {
            // Set region name with proper capitalization
            String regionName = regionId.substring(0, 1).toUpperCase() + regionId.substring(1);
            regionNameText.setText(regionName);
            
            // Set appropriate description based on region
            String description = getRegionDescription(regionId);
            regionDescriptionText.setText(description);
            
            // Show bottom sheet
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            
            // Animate the card appearing
            regionInfoCard.setAlpha(0f);
            regionInfoCard.animate().alpha(1f).setDuration(300).start();
            
            // Update current region
            currentRegion = regionId;
        }
    }
    
    private String getRegionDescription(String regionId) {
        switch (regionId.toLowerCase()) {
            case "banat":
                return "Banat este o regiune istorică în vestul României, renumită pentru multiculturalismul și gastronomia sa. Timișoara, capitala regiunii, este un important centru cultural și istoric.";
            case "crisana":
                return "Crișana este situată în nord-vestul României, fiind traversată de râurile Crișul Alb, Crișul Negru și Crișul Repede. Oradea, principalul oraș, impresionează prin arhitectura Art Nouveau.";
            case "maramures":
                return "Maramureș este una dintre cele mai tradiționale regiuni ale României, faimoasă pentru porțile maramureșene sculptate, bisericile de lemn incluse în patrimoniul UNESCO și tradițiile păstrate neatinse.";
            case "bucovina":
                return "Bucovina este renumită pentru mânăstirile pictate incluse în patrimoniul UNESCO. Peisajele naturale spectaculoase și tradițiile populare fac din această regiune o destinație unică.";
            case "transilvania":
                return "Transilvania este cea mai mare regiune istorică a României, cu o bogată moștenire istorică și culturală. Castelele medievale, orașele fortificate săsești și legendele despre Dracula atrag turiști din întreaga lume.";
            case "moldova":
                return "Moldova este o regiune istorică din estul României, cu o importantă moștenire culturală. Iași, fosta capitală a Moldovei, este un centru spiritual și cultural semnificativ.";
            case "oltenia":
                return "Oltenia este o regiune din sud-vestul României, străbătută de râul Olt. Este cunoscută pentru meșteșugurile tradiționale, mânăstirile medievale și folclorul bogat.";
            case "muntenia":
                return "Muntenia, sau Țara Românească, găzduiește capitala București. Zona combină influențe balcanice și orientale, reflectate în arhitectură, bucătărie și folclor.";
            case "dobrogea":
                return "Dobrogea este situată între Dunăre și Marea Neagră, fiind cea mai veche regiune a României. Delta Dunării, vestigiile romane și influențele multiculturale o fac unică în peisajul românesc.";
            default:
                return "Descoperă frumusețea și diversitatea regiunilor României!";
        }
    }
    
    private void flashRegion(String regionId) {
        // Implementare pentru a evidenția vizual regiunea selectată
        // Define region colors pentru a asigura consistența cu markerii
        final int TRANSILVANIA_COLOR = Color.rgb(76, 175, 80);    // Green
        final int MOLDOVA_COLOR = Color.rgb(33, 150, 243);        // Blue
        final int MUNTENIA_COLOR = Color.rgb(255, 152, 0);        // Orange
        final int DOBROGEA_COLOR = Color.rgb(255, 235, 59);       // Yellow
        final int OLTENIA_COLOR = Color.rgb(156, 39, 176);        // Purple
        final int BANAT_COLOR = Color.rgb(233, 30, 99);           // Pink
        final int CRISANA_COLOR = Color.rgb(0, 188, 212);         // Cyan
        final int MARAMURES_COLOR = Color.rgb(139, 195, 74);      // Light Green
        final int BUCOVINA_COLOR = Color.rgb(121, 85, 72);        // Brown
        
        // Selectează culoarea potrivită pentru regiune
        int regionColor;
        switch (regionId.toLowerCase()) {
            case "transilvania": regionColor = TRANSILVANIA_COLOR; break;
            case "moldova": regionColor = MOLDOVA_COLOR; break;
            case "muntenia": regionColor = MUNTENIA_COLOR; break;
            case "dobrogea": regionColor = DOBROGEA_COLOR; break;
            case "oltenia": regionColor = OLTENIA_COLOR; break;
            case "banat": regionColor = BANAT_COLOR; break;
            case "crisana": regionColor = CRISANA_COLOR; break;
            case "maramures": regionColor = MARAMURES_COLOR; break;
            case "bucovina": regionColor = BUCOVINA_COLOR; break;
            default: regionColor = Color.RED; break;
        }
        
        // Obține coordonatele poligonului regiunii
        LatLng[] regionCoordinates = getRegionCoordinates(regionId);
        if (regionCoordinates == null || regionCoordinates.length == 0) {
            return;
        }
        
        // Creează un poligon temporar care va fi animat
        final PolygonOptions highlightOptions = new PolygonOptions()
                .strokeColor(Color.WHITE)
                .strokeWidth(3)
                .fillColor(Color.argb(200, Color.red(regionColor), Color.green(regionColor), Color.blue(regionColor)));
        
        for (LatLng point : regionCoordinates) {
            highlightOptions.add(point);
        }
        
        // Adaugă poligonul pe hartă
        final com.google.android.gms.maps.model.Polygon highlightPolygon = googleMap.addPolygon(highlightOptions);
        
        // Animează poligonul pentru un efect de pulsare
        final Handler handler = new Handler();
        final Runnable[] animationRunnable = new Runnable[1];
        final int[] alpha = {200};
        final boolean[] increasing = {false};
        
        animationRunnable[0] = new Runnable() {
            @Override
            public void run() {
                if (increasing[0]) {
                    alpha[0] += 10;
                    if (alpha[0] >= 200) {
                        alpha[0] = 200;
                        increasing[0] = false;
                    }
                } else {
                    alpha[0] -= 10;
                    if (alpha[0] <= 50) {
                        alpha[0] = 50;
                        increasing[0] = true;
                    }
                }
                
                highlightPolygon.setFillColor(Color.argb(alpha[0], Color.red(regionColor), Color.green(regionColor), Color.blue(regionColor)));
                
                // Continuă animația
                handler.postDelayed(this, 50);
            }
        };
        
        // Începe animația
        handler.post(animationRunnable[0]);
        
        // Oprește animația după 2 secunde
        handler.postDelayed(() -> {
            handler.removeCallbacks(animationRunnable[0]);
            highlightPolygon.remove();
            
            // Reîmprospătează harta pentru a afișa din nou poligoanele originale
        drawRegionBoundaries();
        }, 2000);
    }
    
    private LatLng[] getRegionCoordinates(String regionId) {
        switch (regionId.toLowerCase()) {
            case "transilvania":
                return new LatLng[]{
                    new LatLng(47.15, 23.0), new LatLng(46.8, 22.8), new LatLng(46.3, 23.2),
                    new LatLng(45.7, 23.0), new LatLng(45.4, 23.7), new LatLng(45.5, 24.8),
                    new LatLng(45.9, 25.8), new LatLng(46.3, 26.0), new LatLng(46.9, 25.5),
                    new LatLng(47.4, 25.2), new LatLng(47.5, 24.8), new LatLng(47.4, 23.5)
                };
            case "moldova":
                return new LatLng[]{
                    new LatLng(48.2, 26.6), new LatLng(47.9, 27.7), new LatLng(46.7, 28.2),
                    new LatLng(46.4, 28.1), new LatLng(46.0, 27.9), new LatLng(45.6, 27.5),
                    new LatLng(45.9, 26.8), new LatLng(46.2, 26.5), new LatLng(46.4, 26.2),
                    new LatLng(46.8, 26.1), new LatLng(47.3, 26.2), new LatLng(47.8, 26.2)
                };
            case "muntenia":
                return new LatLng[]{
                    new LatLng(45.9, 25.7), new LatLng(45.8, 26.8), new LatLng(45.5, 27.5),
                    new LatLng(45.2, 27.8), new LatLng(44.8, 28.0), new LatLng(44.0, 27.9),
                    new LatLng(43.7, 27.2), new LatLng(43.6, 26.2), new LatLng(43.7, 25.5),
                    new LatLng(43.8, 25.0), new LatLng(44.0, 24.5), new LatLng(44.4, 24.3),
                    new LatLng(44.8, 24.8), new LatLng(45.3, 25.2)
                };
            case "dobrogea":
                return new LatLng[]{
                    new LatLng(45.2, 27.8), new LatLng(45.0, 28.0), new LatLng(44.8, 28.7),
                    new LatLng(44.6, 28.9), new LatLng(44.2, 29.0), new LatLng(43.8, 28.6),
                    new LatLng(43.7, 28.1), new LatLng(43.8, 27.7), new LatLng(44.0, 27.9)
                };
            case "oltenia":
                return new LatLng[]{
                    new LatLng(45.0, 23.3), new LatLng(44.8, 24.8), new LatLng(44.4, 24.3),
                    new LatLng(44.0, 24.5), new LatLng(43.8, 25.0), new LatLng(43.7, 24.5),
                    new LatLng(43.9, 23.0), new LatLng(44.1, 22.7), new LatLng(44.4, 22.7),
                    new LatLng(44.7, 22.5), new LatLng(44.9, 22.4), new LatLng(45.1, 22.6)
                };
            case "banat":
                return new LatLng[]{
                    new LatLng(45.8, 21.2), new LatLng(45.1, 22.6), new LatLng(44.9, 22.4),
                    new LatLng(44.7, 22.5), new LatLng(44.4, 22.7), new LatLng(44.2, 22.4),
                    new LatLng(44.2, 21.6), new LatLng(44.8, 21.0), new LatLng(45.2, 21.2)
                };
            case "crisana":
                return new LatLng[]{
                    new LatLng(47.5, 21.5), new LatLng(47.0, 22.2), new LatLng(46.5, 22.6),
                    new LatLng(46.1, 22.1), new LatLng(45.8, 21.8), new LatLng(45.8, 21.2),
                    new LatLng(46.1, 20.8), new LatLng(46.2, 20.4), new LatLng(46.9, 20.3),
                    new LatLng(47.2, 21.0)
                };
            case "maramures":
                return new LatLng[]{
                    new LatLng(47.9, 23.6), new LatLng(47.9, 24.5), new LatLng(47.7, 25.0),
                    new LatLng(47.6, 25.1), new LatLng(47.3, 25.2), new LatLng(47.4, 23.5),
                    new LatLng(47.5, 23.1), new LatLng(47.7, 23.0)
                };
            case "bucovina":
                return new LatLng[]{
                    new LatLng(47.9, 24.5), new LatLng(47.7, 25.0), new LatLng(47.6, 25.1),
                    new LatLng(47.7, 25.5), new LatLng(47.6, 26.1), new LatLng(47.8, 26.2),
                    new LatLng(48.2, 26.6), new LatLng(48.3, 26.4), new LatLng(48.1, 25.5),
                    new LatLng(48.0, 25.2)
                };
            default:
                return null;
        }
    }
    
    private void setupQuestList() {
        if (questRecyclerView != null) {
            // Initialize questAdapter
            List<Mission> allMissions = new ArrayList<>();
            if (availableMissions != null) {
                allMissions.addAll(availableMissions);
            }
            if (activeMissions != null) {
                allMissions.addAll(activeMissions);
            }
            if (completedMissions != null) {
                allMissions.addAll(completedMissions);
            }
            
            questAdapter = new QuestAdapter(allMissions, mission -> {
                // Show mission details when clicked
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View missionView = getLayoutInflater().inflate(R.layout.dialog_mission_details, null);
                
                TextView titleText = missionView.findViewById(R.id.missionTitleText);
                TextView descriptionText = missionView.findViewById(R.id.missionDescriptionText);
                MaterialButton actionButton = missionView.findViewById(R.id.acceptMissionButton);
                
                if (titleText != null) {
                    titleText.setText(mission.getDescription());
                }
                
                if (descriptionText != null) {
                    // Show appropriate text based on mission status
                    if (mission.isCompleted()) {
                        descriptionText.setText("Misiune completată! Ai primit " + 
                            mission.getRewardPoints() + " Puncte Înțelepte.");
                    } else if (mission.isActive()) {
                        descriptionText.setText("Misiune activă. Completează toate obiectivele pentru a primi " + 
                            mission.getRewardPoints() + " Puncte Înțelepte.");
                    } else {
                        descriptionText.setText("Acceptă această misiune pentru a câștiga " + 
                            mission.getRewardPoints() + " Puncte Înțelepte.");
                    }
                }
                
                if (actionButton != null) {
                    // Set appropriate button text based on mission status
                    if (mission.isCompleted()) {
                        actionButton.setText("Misiune completată");
                        actionButton.setEnabled(false);
                    } else if (mission.isActive()) {
                        actionButton.setText("Continuă misiunea");
                        actionButton.setOnClickListener(v -> {
                            continueActiveMission(mission);
                            builder.create().dismiss();
                        });
                    } else {
                        actionButton.setText("Acceptă misiunea");
                        actionButton.setOnClickListener(v -> {
                            activateMission(mission);
                            builder.create().dismiss();
                        });
                    }
                }
                
                builder.setView(missionView);
                builder.create().show();
            });
            
            questRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            questRecyclerView.setAdapter(questAdapter);
        }
    }
    
    // Inner adapter class for missions
    private class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.ViewHolder> {
        private List<Mission> missions;
        private OnMissionClickListener listener;
        
        public interface OnMissionClickListener {
            void onMissionClick(Mission mission);
        }
        
        public QuestAdapter(List<Mission> missions, OnMissionClickListener listener) {
            this.missions = missions;
            this.listener = listener;
        }
        
        public void updateMissions(List<Mission> newMissions) {
            this.missions = newMissions;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_mission, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Mission mission = missions.get(position);
            
            holder.titleText.setText(mission.getDescription());
            
            // Set status text and color based on mission completion status
            if (mission.isCompleted()) {
                holder.statusText.setText("Completat");
                holder.statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                holder.itemCard.setStrokeColor(getResources().getColor(android.R.color.holo_green_light));
            } else if (mission.isActive()) {
                holder.statusText.setText("Activ");
                holder.statusText.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                holder.itemCard.setStrokeColor(getResources().getColor(android.R.color.holo_blue_light));
            } else {
                holder.statusText.setText("Disponibil");
                holder.statusText.setTextColor(getResources().getColor(android.R.color.darker_gray));
                holder.itemCard.setStrokeColor(getResources().getColor(android.R.color.darker_gray));
            }
            
            // Set region text
            holder.regionText.setText("Regiune: " + 
                mission.getCityName().substring(0, 1).toUpperCase() + 
                mission.getCityName().substring(1));
            
            // Set reward text
            holder.rewardText.setText(mission.getRewardPoints() + " Puncte");
            
            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMissionClick(mission);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return missions.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleText;
            TextView statusText;
            TextView regionText;
            TextView rewardText;
            MaterialCardView itemCard;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.missionTitle);
                statusText = itemView.findViewById(R.id.missionStatusText);
                regionText = itemView.findViewById(R.id.missionRegionText);
                rewardText = itemView.findViewById(R.id.missionRewardText);
                itemCard = (MaterialCardView) itemView.findViewById(R.id.missionCardView);
            }
        }
    }

    private Class<?> getRegionActivityClass(String regionId) {
        Class<?> targetClass = null;
        
        try {
            switch (regionId.toLowerCase()) {
                case "banat": 
                    targetClass = Banat.class;
                    break;
                case "crisana": 
                    targetClass = Crisana.class;
                    break;
                case "maramures": 
                    targetClass = Maramures.class;
                    break;
                case "bucovina": 
                    targetClass = Bucovina.class;
                    break;
                case "transilvania": 
                    targetClass = Transilvania.class;
                    break;
                case "moldova": 
                    targetClass = Moldova.class;
                    break;
                case "oltenia": 
                    targetClass = Oltenia.class;
                    break;
                case "muntenia": 
                    targetClass = Muntenia.class;
                    break;
                case "dobrogea": 
                    targetClass = Dobrogea.class;
                    break;
                default:
                    throw new IllegalArgumentException("Regiunea necunoscută: " + regionId);
            }
            
            // Verificăm dacă clasa este o activitate
            boolean isActivity = android.app.Activity.class.isAssignableFrom(targetClass);
            
            if (!isActivity) {
                android.util.Log.w("RomMapActivity", "Atenție: " + targetClass.getName() + 
                    " nu este o Activity! Redirectarea poate eșua.");
            }
            
            return targetClass;
            
        } catch (Exception e) {
            // Logăm eroarea pentru debugging
            android.util.Log.e("RomMapActivity", "Eroare la găsirea clasei pentru regiunea: " 
                + regionId + " - " + e.getMessage());
            
            // Afișăm un mesaj informativ despre eroare
            Toast.makeText(this, "Regiunea " + regionId + " nu este disponibilă momentan: " 
                + e.getMessage(), Toast.LENGTH_LONG).show();
                
            return null;
        }
    }

    // Adăugăm această metodă pentru a afișa informații de debugging despre toate regiunile
    private void showRegionDebugInfo() {
        StringBuilder debugInfo = new StringBuilder("Informații despre regiuni:\n\n");
        
        String[] regions = {"banat", "crisana", "maramures", "bucovina", 
                          "transilvania", "moldova", "oltenia", "muntenia", "dobrogea"};
        
        for (String region : regions) {
            Class<?> regionClass = null;
            boolean isActivity = false;
            boolean isDeclared = false;
            
            try {
                regionClass = getRegionActivityClass(region);
                if (regionClass != null) {
                    isActivity = android.app.Activity.class.isAssignableFrom(regionClass);
                    isDeclared = isActivityDeclaredInManifest(regionClass.getName());
                }
            } catch (Exception e) {
                // Ignorăm erorile aici, le vom afișa în raport
            }
            
            debugInfo.append(region.toUpperCase()).append(":\n");
            debugInfo.append("- Clasă: ").append(regionClass != null ? regionClass.getName() : "NU EXISTĂ").append("\n");
            debugInfo.append("- Este Activity: ").append(isActivity).append("\n");
            debugInfo.append("- Declarată în Manifest: ").append(isDeclared).append("\n\n");
        }
        
        // Afișăm informațiile într-un dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Informații de debugging")
               .setMessage(debugInfo.toString())
               .setPositiveButton("OK", null)
               .show();
    }
    
    // Verifică dacă activitatea este declarată în AndroidManifest.xml
    private boolean isActivityDeclaredInManifest(String className) {
        try {
            PackageManager pm = getPackageManager();
            ActivityInfo info = pm.getActivityInfo(new ComponentName(getPackageName(), className), 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // Init methods called from onCreate
    private void setupCityMarkers() {
        // Add major cities with proper coordinates and region info
        
        // Transilvania
        addCityMarker(new LatLng(46.7712, 23.6236), "Cluj-Napoca", "Transilvania", BitmapDescriptorFactory.HUE_AZURE);
        addCityMarker(new LatLng(45.6427, 25.5887), "Brașov", "Transilvania", BitmapDescriptorFactory.HUE_AZURE);
        addCityMarker(new LatLng(46.2195, 24.7964), "Sighișoara", "Transilvania", BitmapDescriptorFactory.HUE_AZURE);
        addCityMarker(new LatLng(45.7983, 24.1256), "Sibiu", "Transilvania", BitmapDescriptorFactory.HUE_AZURE);
        addCityMarker(new LatLng(46.0470, 23.5858), "Alba Iulia", "Transilvania", BitmapDescriptorFactory.HUE_AZURE);
        
        // Moldova
        addCityMarker(new LatLng(47.1585, 27.6014), "Iași", "Moldova", BitmapDescriptorFactory.HUE_GREEN);
        addCityMarker(new LatLng(46.5667, 26.9145), "Bacău", "Moldova", BitmapDescriptorFactory.HUE_GREEN);
        addCityMarker(new LatLng(47.6426, 26.2499), "Suceava", "Moldova", BitmapDescriptorFactory.HUE_GREEN);
        addCityMarker(new LatLng(46.8273, 26.3706), "Piatra Neamț", "Moldova", BitmapDescriptorFactory.HUE_GREEN);
        
        // Muntenia
        addCityMarker(new LatLng(44.4268, 26.1025), "București", "Muntenia", BitmapDescriptorFactory.HUE_ORANGE);
        addCityMarker(new LatLng(44.9475, 25.6358), "Ploiești", "Muntenia", BitmapDescriptorFactory.HUE_ORANGE);
        addCityMarker(new LatLng(44.4323, 24.3619), "Slatina", "Muntenia", BitmapDescriptorFactory.HUE_ORANGE);
        addCityMarker(new LatLng(44.7677, 26.6802), "Buzău", "Muntenia", BitmapDescriptorFactory.HUE_ORANGE);
        
        // Dobrogea
        addCityMarker(new LatLng(44.1598, 28.6348), "Constanța", "Dobrogea", BitmapDescriptorFactory.HUE_CYAN);
        addCityMarker(new LatLng(44.8998, 28.8041), "Tulcea", "Dobrogea", BitmapDescriptorFactory.HUE_CYAN);
        addCityMarker(new LatLng(44.1700, 28.6319), "Mamaia", "Dobrogea", BitmapDescriptorFactory.HUE_CYAN);
        
        // Oltenia
        addCityMarker(new LatLng(44.3302, 23.7949), "Craiova", "Oltenia", BitmapDescriptorFactory.HUE_VIOLET);
        addCityMarker(new LatLng(44.6994, 22.5456), "Drobeta-Turnu Severin", "Oltenia", BitmapDescriptorFactory.HUE_VIOLET);
        addCityMarker(new LatLng(45.1029, 24.3695), "Râmnicu Vâlcea", "Oltenia", BitmapDescriptorFactory.HUE_VIOLET);
        
        // Banat
        addCityMarker(new LatLng(45.7489, 21.2087), "Timișoara", "Banat", BitmapDescriptorFactory.HUE_YELLOW);
        addCityMarker(new LatLng(45.3088, 21.8900), "Reșița", "Banat", BitmapDescriptorFactory.HUE_YELLOW);
        
        // Crișana
        addCityMarker(new LatLng(47.0465, 21.9189), "Oradea", "Crișana", BitmapDescriptorFactory.HUE_MAGENTA);
        addCityMarker(new LatLng(46.1865, 21.3123), "Arad", "Crișana", BitmapDescriptorFactory.HUE_MAGENTA);
        
        // Maramureș
        addCityMarker(new LatLng(47.6635, 23.5823), "Baia Mare", "Maramureș", BitmapDescriptorFactory.HUE_ROSE);
        addCityMarker(new LatLng(47.9226, 23.8994), "Sighetu Marmației", "Maramureș", BitmapDescriptorFactory.HUE_ROSE);
        
        // Bucovina
        addCityMarker(new LatLng(47.9304, 25.9355), "Rădăuți", "Bucovina", BitmapDescriptorFactory.HUE_BLUE);
        addCityMarker(new LatLng(47.8557, 25.9230), "Gura Humorului", "Bucovina", BitmapDescriptorFactory.HUE_BLUE);
    }
    
    private void addCityMarker(LatLng position, String title, String region, float hue) {
        if (googleMap == null) return;
        
        // Create marker with custom styling
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(title)
                .snippet(region)
                .icon(BitmapDescriptorFactory.defaultMarker(hue))
                .alpha(0.9f);
        
        // Add marker to map
        googleMap.addMarker(markerOptions);
    }

    private void loadAvailableMissions() {
        // Initialize missions list if not already done
        if (availableMissions == null) {
            availableMissions = new ArrayList<>();
        } else {
            availableMissions.clear();
        }
        
        // Add some sample missions (in a real app, these would come from a database or server)
        Mission mission1 = new Mission(
                "descopera_alba_iulia",
                "Descoperă Alba Iulia",
                "Vizitează cetatea Alba Carolina și învață despre istoria acestui important oraș.",
                "transilvania",
                150,
                Mission.TYPE_EXPLORATION);
        mission1.addObjective("Vizitează Cetatea Alba Carolina");
        mission1.addObjective("Fă o fotografie la Poarta a III-a");
        mission1.addObjective("Răspunde la 3 întrebări despre istoria cetății");
        availableMissions.add(mission1);
        
        Mission mission2 = new Mission(
                "traditii_sibiu",
                "Tradiții din Sibiu",
                "Descoperă obiceiurile și tradițiile unice ale regiunii Sibiului și ale comunității săsești.",
                "transilvania",
                100,
                Mission.TYPE_CULTURAL);
        mission2.addObjective("Vizitează Muzeul ASTRA");
        mission2.addObjective("Fotografiază 3 obiecte tradiționale săsești");
        availableMissions.add(mission2);
        
        Mission mission3 = new Mission(
                "bucataria_moldoveneasca",
                "Bucătăria Moldovenească",
                "Explorează aromele specifice bucătăriei moldovenești.",
                "moldova",
                120,
                Mission.TYPE_CULINARY);
        mission3.addObjective("Descoperă 3 rețete tradiționale moldovenești");
        mission3.addObjective("Identifică ingredientele specifice pentru Poale-n brâu");
        availableMissions.add(mission3);
        
        Mission mission4 = new Mission(
                "plimbare_delta",
                "Plimbare în Delta Dunării",
                "Explorează frumusețea naturală a Deltei Dunării, unul dintre cele mai importante ecosisteme din Europa.",
                "dobrogea",
                200,
                Mission.TYPE_EXPLORATION);
        mission4.addObjective("Identifică 5 specii de păsări din Deltă");
        mission4.addObjective("Fotografiază un peisaj cu apus în Deltă");
        mission4.addObjective("Învață despre importanța conservării Deltei");
        availableMissions.add(mission4);
        
        // If map is ready, add markers for missions
        if (googleMap != null) {
            addMissionMarkers();
        }
    }
    
    private void addMissionMarkers() {
        // Clear existing markers
        for (Marker marker : missionMarkers.values()) {
            marker.remove();
        }
        missionMarkers.clear();
        
        // Add new markers
        for (Mission mission : availableMissions) {
            LatLng position = getMissionPosition(mission);
            if (position != null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(mission.getTitle())
                        .snippet(mission.getDescription())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                
                Marker marker = googleMap.addMarker(markerOptions);
                missionMarkers.put(mission.getId(), marker);
            }
        }
    }
    
    private LatLng getMissionPosition(Mission mission) {
        // In a real app, this would be stored with the mission data
        // For now, we'll use some hardcoded positions based on the region
        switch (mission.getRegionId()) {
            case "transilvania":
                if (mission.getId().equals("descopera_alba_iulia")) {
                    return new LatLng(46.0667, 23.5833);
                } else if (mission.getId().equals("traditii_sibiu")) {
                    return new LatLng(45.7892, 24.1450);
                }
                return new LatLng(46.2214, 24.7917); // Default for Transilvania
                
            case "moldova":
                return new LatLng(47.1585, 27.6014); // Iași
                
            case "dobrogea":
                return new LatLng(45.1667, 28.8000); // Tulcea (for Delta)
                
            case "muntenia":
                return new LatLng(44.4268, 26.1025); // București
                
            case "oltenia":
                return new LatLng(44.3189, 23.7967); // Craiova
                
            case "banat":
                return new LatLng(45.7427, 21.2259); // Timișoara
                
            case "crisana":
                return new LatLng(47.1333, 22.0500); // Oradea
                
            case "maramures":
                return new LatLng(47.6626, 23.5686); // Baia Mare
                
            case "bucovina":
                return new LatLng(47.6456, 26.2499); // Suceava
                
            default:
                return null;
        }
    }

    private void applyEntryAnimations() {
        // Animate the search controls
        View topControlsBar = findViewById(R.id.topControlsBar);
        if (topControlsBar != null) {
            topControlsBar.setAlpha(0f);
            topControlsBar.setTranslationY(-100f);
            topControlsBar.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
        
        // Animate the player status card
        View playerStatusCard = findViewById(R.id.playerStatusCard);
        if (playerStatusCard != null) {
            playerStatusCard.setAlpha(0f);
            playerStatusCard.setScaleX(0.8f);
            playerStatusCard.setScaleY(0.8f);
            playerStatusCard.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(700)
                    .setStartDelay(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
        
        // Animate the discover button
        if (discoverFab != null) {
            discoverFab.setScaleX(0f);
            discoverFab.setScaleY(0f);
            discoverFab.setAlpha(0f);
            
            discoverFab.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setStartDelay(400)
                    .setDuration(500)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
        
        // Animate the missions FAB
        FloatingActionButton showMissionsFab = findViewById(R.id.showMissionsFab);
        if (showMissionsFab != null) {
            showMissionsFab.setScaleX(0f);
            showMissionsFab.setScaleY(0f);
            showMissionsFab.setAlpha(0f);
            
            showMissionsFab.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setStartDelay(500)
                    .setDuration(500)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
        
        // Animate region cards (if they're initially visible, which they usually aren't)
        if (regionInfoCard != null && regionInfoCard.getVisibility() == View.VISIBLE) {
            regionInfoCard.setTranslationY(300f);
            regionInfoCard.setAlpha(0f);
            
            regionInfoCard.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setStartDelay(600)
                    .setDuration(700)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }
    
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        
        // Set initial camera position to view all of Romania
        LatLng romaniaCentral = new LatLng(45.9443, 25.0094);
        CameraPosition cameraPosition = new CameraPosition.Builder()
            .target(romaniaCentral)
            .zoom(6.8f)    // Slightly closer zoom for better details
            .tilt(10)     // Slight tilt for 3D effect
            .build();
        
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        
        // Apply different map style based on night mode
        boolean isNightMode = (getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        
        if (isNightMode) {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_night));
        } else {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_day));
        }
        
        // Customize map appearance and UI settings
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true); // Add zoom controls
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setPadding(0, 220, 0, 0); // Add more padding for controls
        
        // Set map type to terrain for better landscape representation
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        
        // Set up map click listener for discovery mode
        googleMap.setOnMapClickListener(latLng -> {
            if (isDiscoveryMode) {
                // Check if click is near any mission marker
                checkClickForNearbyMissions(latLng);
            }
        });
        
        // Set up marker click listener
        googleMap.setOnMarkerClickListener(marker -> {
            // Check if marker is a mission marker
            for (Map.Entry<String, Marker> entry : missionMarkers.entrySet()) {
                if (entry.getValue().equals(marker)) {
                    // Find the mission
                    for (Mission mission : availableMissions) {
                        if (mission.getId().equals(entry.getKey())) {
                            startMissionDialog(mission);
                            return true;
                        }
                    }
                }
            }
            
            // Handle city markers
            String title = marker.getTitle();
            String region = marker.getSnippet();
            if (region != null) {
                showCityInfo(title, region);
                return true;
            }
            
            return false;
        });
        
        // Now that map is ready, we can setup markers and draw region boundaries
        drawRegionBoundaries();
        setupCityMarkers();
        
        // Add mission markers
        if (availableMissions != null && !availableMissions.isEmpty()) {
            addMissionMarkers();
        } else {
            loadAvailableMissions();
        }
        
        // Add visual appeal with initial animation
        animateCamera(romaniaCentral, 7f, 15f, 2000);
    }
    
    // Helper method for animating camera smoothly
    private void animateCamera(LatLng target, float zoom, float tilt, int duration) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
            .target(target)
            .zoom(zoom)
            .tilt(tilt)
            .build();
        
        googleMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition),
            duration,
            null
        );
    }
    
    private void checkClickForNearbyMissions(LatLng clickLatLng) {
        final double CLICK_RADIUS_METERS = 50000; // 50km radius for clicking near a mission marker (large for usability)
        
        for (Map.Entry<String, Marker> entry : missionMarkers.entrySet()) {
            LatLng markerLatLng = entry.getValue().getPosition();
            
            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    clickLatLng.latitude, clickLatLng.longitude,
                    markerLatLng.latitude, markerLatLng.longitude,
                    results);
            
            float distanceInMeters = results[0];
            
            if (distanceInMeters < CLICK_RADIUS_METERS) {
                // Find the mission
                for (Mission mission : availableMissions) {
                    if (mission.getId().equals(entry.getKey())) {
                        startMissionDialog(mission);
                        return;
                    }
                }
            }
        }
        
        // If no mission was found, show a message
        Toast.makeText(this, "Nu ai descoperit nicio misiune în această zonă.", Toast.LENGTH_SHORT).show();
    }
    
    private void showCityInfo(String cityName, String regionName) {
        // Create a dialog with city information
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(cityName);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_city_info, null);
        TextView cityNameText = dialogView.findViewById(R.id.cityNameText);
        TextView regionNameText = dialogView.findViewById(R.id.regionNameText);
        TextView cityDescriptionText = dialogView.findViewById(R.id.cityDescriptionText);
        
        cityNameText.setText(cityName);
        regionNameText.setText(regionName);
        cityDescriptionText.setText(getCityDescription(cityName));
        
        builder.setView(dialogView);
        builder.setPositiveButton("Vizitează", (dialog, which) -> navigateToRegion(regionName.toLowerCase()));
        builder.setNegativeButton("Închide", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private String getCityDescription(String cityName) {
        // In a real app, we would get this from a database or resources
        switch (cityName) {
            case "Cluj-Napoca":
                return "Un important centru cultural, educațional și economic din Transilvania. " +
                       "Este cunoscut pentru universitățile sale și vibranta scenă artistică.";
            case "Timișoara":
                return "Primul oraș din Europa care a introdus iluminatul stradal electric. " +
                       "Revoluția română din 1989 a început aici.";
            case "Alba Iulia":
                return "Locul unde s-a înfăptuit Marea Unire din 1918. " +
                       "Cetatea Alba Carolina este una dintre cele mai impresionante fortificații din România.";
            case "București":
                return "Capitala României, un oraș dinamic cu o bogată istorie și arhitectură variată, " +
                       "de la clădiri în stil neoclasic la construcții din perioada comunistă.";
            case "Constanța":
                return "Cel mai important port maritim al României la Marea Neagră. " +
                       "Are o istorie de peste 2500 de ani, fiind fondat de coloniști greci.";
            case "Iași":
                return "Fosta capitală a Moldovei și un important centru cultural și religios. " +
                       "Aici se află cea mai veche universitate din România.";
            default:
                return "Descoperă frumusețea și istoria acestui oraș fascinant din România.";
        }
    }
    
    private void drawRegionBoundaries() {
        if (googleMap == null) return;
        
        // Draw polygon for each region
        // Transilvania
        drawRegionPolygon("transilvania", getRegionCoordinates("transilvania"), 
                getResources().getColor(R.color.rom_region_transilvania));
        
        // Moldova
        drawRegionPolygon("moldova", getRegionCoordinates("moldova"), 
                getResources().getColor(R.color.rom_region_moldova));
        
        // Muntenia
        drawRegionPolygon("muntenia", getRegionCoordinates("muntenia"), 
                getResources().getColor(R.color.rom_region_muntenia));
        
        // Dobrogea
        drawRegionPolygon("dobrogea", getRegionCoordinates("dobrogea"), 
                getResources().getColor(R.color.rom_region_dobrogea));
        
        // Oltenia
        drawRegionPolygon("oltenia", getRegionCoordinates("oltenia"), 
                getResources().getColor(R.color.rom_region_oltenia));
        
        // Banat
        drawRegionPolygon("banat", getRegionCoordinates("banat"), 
                getResources().getColor(R.color.rom_region_banat));
        
        // Crisana
        drawRegionPolygon("crisana", getRegionCoordinates("crisana"), 
                getResources().getColor(R.color.rom_region_crisana));
        
        // Maramures
        drawRegionPolygon("maramures", getRegionCoordinates("maramures"), 
                getResources().getColor(R.color.rom_region_maramures));
        
        // Bucovina
        drawRegionPolygon("bucovina", getRegionCoordinates("bucovina"), 
                getResources().getColor(R.color.rom_region_bucovina));
    }
    
    private void drawRegionPolygon(String regionId, LatLng[] coordinates, int color) {
        if (googleMap == null || coordinates == null) return;
        
        // Create polygon with improved styling
        PolygonOptions polygonOptions = new PolygonOptions()
                .strokeColor(Color.WHITE)  // White border for better contrast
                .strokeWidth(2.5f)         // Slightly thicker border
                .fillColor(color & 0x4FFFFFFF); // Semi-transparent fill (31% opacity)
        
        for (LatLng coordinate : coordinates) {
            polygonOptions.add(coordinate);
        }
        
        // Add the polygon to the map and store reference
        com.google.android.gms.maps.model.Polygon polygon = googleMap.addPolygon(polygonOptions);
        
        // Set region ID as tag for identifying in click events
        polygon.setTag(regionId);
        
        // Add click listener to the map
        if (!regionClickListenerAdded) {
            regionClickListenerAdded = true;
            googleMap.setOnPolygonClickListener(clickedPolygon -> {
                String clickedRegionId = (String) clickedPolygon.getTag();
                if (clickedRegionId != null) {
                    selectRegion(clickedRegionId);
                }
            });
        }
        
        // Make polygons clickable
        polygon.setClickable(true);
        
        // Optional: Store the polygon reference for later use if needed
        if (regionPolygons == null) {
            regionPolygons = new HashMap<>();
        }
        regionPolygons.put(regionId, polygon);
    }
}
