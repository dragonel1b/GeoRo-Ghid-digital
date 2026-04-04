package com.example.myapplication.olteniausage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Oltenia;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.core.domain.model.OlteniaStoryNode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OlteniaStoryActivity extends AppCompatActivity {
    private static final String KEY_CURRENT_NODE = "current_node";
    private static final String KEY_SCORE = "score";
    private static final String KEY_VISITED_NODES = "visited_nodes";

    // UI Components
    private TextView storyTextView;
    private TextView storyTitleView;
    private TextView storyContextView;
    private ImageView storyImageView;
    private MaterialButton nextButton, backButton, optionOneButton, optionTwoButton;
    private MaterialButton finishButton;
    private MaterialCardView feedbackCard;
    private TextView feedbackText;
    private LinearProgressIndicator progressIndicator;
    private ImageView confettiView;
    
    // State Management
    private int currentNodeIndex = 0;
    private int score = 0;
    private List<Integer> visitedNodes;
    private PointsManager pointsManager;
    private Map<Integer, OlteniaStoryNode> storyNodes;
    private Animation fadeInAnimation;
    private Animation slideInAnimation;
    
    // Media Players
    private MediaPlayer backgroundMusic;
    private MediaPlayer successSound;
    private MediaPlayer failureSound;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oltenia_story);
        
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
        
        initializeViews();
        initializeAnimations();
        initializeMediaPlayers();
        initializeStoryNodes();
        setupButtonListeners();
        
        // Show the first node
        showCurrentNode();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_NODE, currentNodeIndex);
        outState.putInt(KEY_SCORE, score);
        outState.putIntegerArrayList(KEY_VISITED_NODES, new ArrayList<>(visitedNodes));
    }
    
    private void restoreState(Bundle savedInstanceState) {
        currentNodeIndex = savedInstanceState.getInt(KEY_CURRENT_NODE, 0);
        score = savedInstanceState.getInt(KEY_SCORE, 0);
        visitedNodes = new ArrayList<>(savedInstanceState.getIntegerArrayList(KEY_VISITED_NODES));
    }
    
    private void initializeViews() {
        // Initialize views
        storyTextView = findViewById(R.id.storyTextView);
        storyTitleView = findViewById(R.id.storyTitleView);
        storyContextView = findViewById(R.id.storyContextView);
        storyImageView = findViewById(R.id.storyImageView);
        nextButton = findViewById(R.id.nextButton);
        backButton = findViewById(R.id.backButton);
        optionOneButton = findViewById(R.id.optionOneButton);
        optionTwoButton = findViewById(R.id.optionTwoButton);
        finishButton = findViewById(R.id.finishButton);
        feedbackCard = findViewById(R.id.feedbackCard);
        feedbackText = findViewById(R.id.feedbackText);
        
        // Initialize new components
        progressIndicator = findViewById(R.id.progressIndicator);
        confettiView = findViewById(R.id.confettiView);
        
        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);
        
        // Initialize visited nodes list
        visitedNodes = new ArrayList<>();
        
        // Setăm click listener pentru butonul de back din header
        ImageView headerBackButton = findViewById(R.id.headerBackButton);
        headerBackButton.setOnClickListener(v -> goBack());
    }
    
    private void initializeAnimations() {
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        slideInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        // Setăm durata animațiilor pentru o experiență mai fluidă
        fadeInAnimation.setDuration(700);
        slideInAnimation.setDuration(600);
    }
    
    private void initializeMediaPlayers() {
        // Inițializăm muzica de fundal
        backgroundMusic = MediaPlayer.create(this, R.raw.oltenia_ambient);
        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.3f, 0.3f); // Reducem volumul pentru a nu interfera cu povestea
            backgroundMusic.start();
        }
        
        // Inițializăm sunetele pentru feedback
        successSound = MediaPlayer.create(this, R.raw.win_sound);
        failureSound = MediaPlayer.create(this, R.raw.lose_sound);
    }
    
    private void initializeStoryNodes() {
        storyNodes = new HashMap<>();
        
        try {
            // Node 0: Introducere
            storyNodes.put(0, new OlteniaStoryNode.Builder(0,
                "Oltenia, o regiune bogată în istorie și tradiție, se întinde între Carpații Meridionali, Dunăre și râul Olt. " +
                "Aici, peisajele variază de la munți impunători la câmpii fertile, fiecare colț având propria sa poveste de spus.")
                .title("Oltenia - Tărâm de legendă")
                .context("Să pornim într-o călătorie imaginară prin timpul și spațiul Olteniei, descoperind împreună bogățiile acestei regiuni.")
                .imageResource(R.drawable.oltenia_bg_simple)
                .choices(new String[]{"Continuă călătoria", "Află despre istorie"})
                .nextNodes(new int[]{1, 2})
                .build());
            
            // Node 1: Geografie
            storyNodes.put(1, new OlteniaStoryNode.Builder(1,
                "Din punct de vedere geografic, Oltenia este străbătută de râuri importante precum Jiu, Olt și Olteț. " +
                "Munții Parâng și Vâlcan oferă priveliști spectaculoase, iar Defileul Jiului reprezintă una dintre cele mai " +
                "impresionante zone naturale. Câmpia Olteniei, în sud, este una dintre cele mai fertile zone agricole din România.")
                .title("Geografia Olteniei")
                .context("Relieful variat al Olteniei a influențat profund viața și ocupațiile localnicilor de-a lungul istoriei.")
                .imageResource(R.drawable.parang)
                .soundResource(R.raw.nature_sound)
                .nextNodes(new int[]{2})
                .choices(new String[]{"Continuă"})
                .build());
            
            // Node 2: Istorie
            storyNodes.put(2, new OlteniaStoryNode.Builder(2,
                "Regiunea Olteniei a fost locuită încă din antichitate, fiind parte din Dacia și mai târziu a Imperiului Roman. " +
                "Numele regiunii vine de la râul Olt, fiind inițial cunoscută ca 'Țara de peste Olt'. " +
                "În perioada medievală, Oltenia a făcut parte din Țara Românească, iar între 1718-1739 a fost sub ocupație austriacă.")
                .title("Istoria Olteniei")
                .context("Oltenia a fost martora multor evenimente istorice importante care au modelat România de astăzi.")
                .imageResource(R.drawable.brancusi)
                .choices(new String[]{"Continuă", "Testează-ți cunoștințele"})
                .nextNodes(new int[]{3, 10})
                .soundResource(R.raw.medieval_music)
                .build());
            
            // Node 3: Cultură și tradiții
            storyNodes.put(3, new OlteniaStoryNode.Builder(3,
                "Bogăția culturală a Olteniei se reflectă în muzica, dansurile și tradițiile sale unice. " +
                "Folclorul oltenesc este cunoscut pentru vitalitatea și specificul său regional, iar meșteșugurile " +
                "tradiționale precum olăritul de Horezu (inclus în patrimoniul UNESCO) sunt încă practicate.")
                .title("Cultură și tradiții")
                .context("Tradițiile oltenești s-au păstrat de-a lungul generațiilor, fiind o mărturie vie a identității regionale.")
                .imageResource(R.drawable.craiova)
                .soundResource(R.raw.win_sound)
                .nextNodes(new int[]{4})
                .choices(new String[]{"Continuă"})
                .build());
            
            // Node 4: Arhitectură
            storyNodes.put(4, new OlteniaStoryNode.Builder(4,
                "Arhitectura tradițională oltenească este reprezentată de casele cu pridvor și porțile sculptate. " +
                "Culele boierești, construcții defensive specifice Olteniei, reprezintă un element arhitectural unic. " +
                "Mănăstirile din Oltenia, precum Tismana, Horezu și Polovragi, sunt capodopere ale arhitecturii religioase românești.")
                .title("Arhitectura Olteniei")
                .context("Construcțiile tradiționale din Oltenia reflectă atât nevoile practice, cât și sensibilitatea artistică a localnicilor.")
                .imageResource(R.drawable.tismana)
                .nextNodes(new int[]{5})
                .choices(new String[]{"Continuă"})
                .build());
            
            // Node 5: Gastronomie
            storyNodes.put(5, new OlteniaStoryNode.Builder(5,
                "Bucătăria oltenească este renumită pentru preparatele sale: ciorba de fasole cu afumătură, sarmalele și plăcintele. " +
                "Vinurile din podgoriile Drăgășani și Segarcea sunt apreciate pentru calitatea lor deosebită. " +
                "Pâinea coaptă în țest și pastramă de berbec sunt alte specialități culinare specifice regiunii.")
                .title("Gastronomia Olteniei")
                .context("Mâncarea oltenească reflectă bogăția regiunii și influențele culturale diverse care au modelat-o de-a lungul timpului.")
                .imageResource(R.drawable.dragasani)
                .nextNodes(new int[]{6})
                .choices(new String[]{"Continuă"})
                .build());
            
            // Node 6: Personalități
            storyNodes.put(6, new OlteniaStoryNode.Builder(6,
                "Oltenia a dat României personalități remarcabile precum: Constantin Brâncuși - unul dintre cei mai influenți sculptori ai " +
                "secolului XX, Tudor Vladimirescu - liderul Revoluției din 1821, Nicolae Titulescu - diplomat și om politic, " +
                "și Petrache Poenaru - inventatorul stiloului cu rezervor.")
                .title("Personalități ilustre")
                .context("Figurile marcante ale Olteniei au contribuit semnificativ la istoria, cultura și știința românescă și mondială.")
                .imageResource(R.drawable.brancusi)
                .nextNodes(new int[]{7})
                .choices(new String[]{"Continuă"})
                .build());
            
            // Node 7: Orașe importante
            storyNodes.put(7, new OlteniaStoryNode.Builder(7,
                "Principalele orașe din Oltenia sunt: Craiova - 'capitala' Olteniei și important centru cultural și economic, " +
                "Târgu Jiu - orașul lui Brâncuși, unde se află Ansamblul Monumental, Râmnicu Vâlcea - centru religios și termal, " +
                "Drobeta-Turnu Severin - important port la Dunăre și Slatina - centru industrial.")
                .title("Orașe importante")
                .context("Fiecare oraș din Oltenia are propria sa istorie și contribuție la dezvoltarea regiunii.")
                .imageResource(R.drawable.craiova)
                .nextNodes(new int[]{8})
                .choices(new String[]{"Continuă"})
                .build());
            
            // Node 8: Turism
            storyNodes.put(8, new OlteniaStoryNode.Builder(8,
                "Oltenia oferă numeroase atracții turistice: Ansamblul Sculptural Brâncuși din Târgu Jiu, stațiunile " +
                "balneo-climaterice Călimănești-Căciulata și Băile Olănești, Peștera Muierilor, Transalpina - cea mai înaltă " +
                "șosea din România, Parcul Național Domogled-Valea Cernei și impresionantele defilee ale Oltului și Jiului.")
                .title("Destinații turistice")
                .context("Peisajele diverse și patrimoniul cultural bogat fac din Oltenia o destinație turistică atractivă.")
                .imageResource(R.drawable.parang)
                .nextNodes(new int[]{9})
                .choices(new String[]{"Continuă"})
                .build());
            
            // Node 9: Concluzie
            storyNodes.put(9, new OlteniaStoryNode.Builder(9,
                "Oltenia reprezintă o regiune cu o identitate puternică și distinctă în cadrul României. " +
                "Bogăția sa culturală, istorică și naturală o face unică și fascinantă. " +
                "Fie că e vorba de peisaje montane spectaculoase, tradiții străvechi sau ospitalitatea localnicilor, " +
                "Oltenia rămâne un tărâm care merită explorat și apreciat.")
                .title("O regiune de neprețuit")
                .context("Explorarea Olteniei este o călătorie prin timp și prin esența spiritului românesc.")
                .imageResource(R.drawable.targujiu)
                .pointsReward(100)
                .build());
            
            // Node 10: Quiz despre Oltenia
            storyNodes.put(10, new OlteniaStoryNode.Builder(10,
                "Să testăm acum cunoștințele tale despre Oltenia. Care dintre următoarele personalități NU s-a născut în Oltenia?")
                .title("Test de cunoștințe")
                .context("Personalitățile oltenești au contribuit semnificativ la cultura română și universală.")
                .nodeType(OlteniaStoryNode.NodeType.QUIZ)
                .choices(new String[]{"Constantin Brâncuși", "Tudor Vladimirescu", "George Enescu", "Petrache Poenaru"})
                .nextNodes(new int[]{11, 11, 11, 11})
                .correctAnswer("George Enescu")
                .feedback("Corect! George Enescu s-a născut în Moldova, la Liveni, județul Botoșani, nu în Oltenia.")
                .pointsReward(50)
                .build());
            
            // Node 11: Feedback Quiz
            storyNodes.put(11, new OlteniaStoryNode.Builder(11,
                "Felicitări pentru participarea la acest mic test! Să continuăm călătoria noastră prin Oltenia.")
                .title("Continuăm explorarea")
                .context("Mai sunt multe de descoperit despre această regiune fascinantă.")
                .imageResource(R.drawable.oltenia_bg_simple)
                .choices(new String[]{"Continuă explorarea"})
                .nextNodes(new int[]{3})
                .nodeType(OlteniaStoryNode.NodeType.CHOICE)
                .build());
            
            System.out.println("DEBUG: Successfully initialized all story nodes");
            // Verificăm conexiunile între noduri
            for (int i = 0; i < storyNodes.size(); i++) {
                OlteniaStoryNode node = storyNodes.get(i);
                if (node != null) {
                    System.out.println("DEBUG: Node " + i + " (" + node.getTitle() + ") has " + 
                                      (node.getNextNodes() != null ? node.getNextNodes().length : 0) + 
                                      " next nodes defined");
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: Failed to initialize story nodes: " + e.getMessage());
            e.printStackTrace();
        }
            
        // Setăm progress bar-ul
        if (progressIndicator != null) {
            progressIndicator.setMax(storyNodes.size());
            progressIndicator.setProgress(1);
        }
    }
    
    private void setupButtonListeners() {
        nextButton.setOnClickListener(v -> moveToNextNode());
        backButton.setOnClickListener(v -> moveToPreviousNode());
        finishButton.setOnClickListener(v -> finishStory());
        optionOneButton.setOnClickListener(v -> selectOption(0));
        optionTwoButton.setOnClickListener(v -> selectOption(1));
    }
    
    private void showCurrentNode() {
        // Resetăm starea tuturor butoanelor la începutul afișării oricărui nod
        optionOneButton.setVisibility(View.GONE);
        optionTwoButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        finishButton.setVisibility(View.GONE);
        feedbackCard.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
        
        // Resetăm de asemenea starea de enabled/clickable
        optionOneButton.setEnabled(true);
        optionTwoButton.setEnabled(true);
        optionOneButton.setClickable(true);
        optionTwoButton.setClickable(true);
        nextButton.setEnabled(true);
        nextButton.setClickable(true);
        
        // Verificarea specială pentru a preveni buclele infinite
        if (visitedNodes.contains(currentNodeIndex) && visitedNodes.size() > 1 && 
            visitedNodes.get(visitedNodes.size() - 1) == currentNodeIndex && 
            visitedNodes.get(visitedNodes.size() - 2) == currentNodeIndex) {
            System.out.println("DEBUG: ⚠️ DETECTED INFINITE LOOP at node " + currentNodeIndex);
            // Încercăm să recuperăm mergând la următorul nod numeric
            int nextNodeId = currentNodeIndex + 1;
            while (nextNodeId < storyNodes.size() && !storyNodes.containsKey(nextNodeId)) {
                nextNodeId++;
            }
            
            if (nextNodeId < storyNodes.size() && storyNodes.containsKey(nextNodeId)) {
                System.out.println("DEBUG: Loop recovery - moving to node " + nextNodeId);
                currentNodeIndex = nextNodeId;
            } else {
                // Dacă nu găsim nod valid, mergem direct la concluzie
                System.out.println("DEBUG: Loop recovery - moving to conclusion node");
                currentNodeIndex = 9;
            }
        }
        
        // Verificare specială pentru tranziția din quiz (nodul 11) la cultura (nodul 3)
        if (currentNodeIndex == 11) {
            System.out.println("DEBUG: Special handling for node 11 -> 3 transition");
        }
        
        OlteniaStoryNode currentNode = storyNodes.get(currentNodeIndex);
        if (currentNode == null) {
            System.out.println("DEBUG: Node is null at index " + currentNodeIndex);
            finishStory();
            return;
        }
        
        System.out.println("DEBUG: Showing node " + currentNodeIndex + " with title: " + currentNode.getTitle());
        
        // Actualizăm progresul
        if (progressIndicator != null) {
            progressIndicator.setProgress(currentNodeIndex + 1);
        }
        
        // Adăugăm la lista de noduri vizitate
        if (!visitedNodes.contains(currentNodeIndex)) {
            visitedNodes.add(currentNodeIndex);
        }
        
        // Aplicăm animații de tranziție
        animateNodeTransition();
        
        // Setăm titlul și contextul
        if (!currentNode.getTitle().isEmpty()) {
            storyTitleView.setText(currentNode.getTitle());
            storyTitleView.setVisibility(View.VISIBLE);
            storyTitleView.startAnimation(fadeInAnimation);
        } else {
            storyTitleView.setVisibility(View.GONE);
        }
        
        if (!currentNode.getContext().isEmpty()) {
            storyContextView.setText(currentNode.getContext());
            storyContextView.setVisibility(View.VISIBLE);
            storyContextView.startAnimation(fadeInAnimation);
        } else {
            storyContextView.setVisibility(View.GONE);
        }
        
        // Setăm textul poveștii
        storyTextView.setText(currentNode.getStoryText());
        storyTextView.startAnimation(fadeInAnimation);
        
        // Setăm imaginea dacă există
        if (currentNode.getImageResourceId() != 0) {
            storyImageView.setImageResource(currentNode.getImageResourceId());
            storyImageView.setVisibility(View.VISIBLE);
            storyImageView.startAnimation(fadeInAnimation);
        } else {
            storyImageView.setVisibility(View.GONE);
        }
        
        // Configurăm butoanele în funcție de tipul nodului
        setupButtons(currentNode);
        
        // Dacă nodul are recompensă și nu a fost vizitat încă, o acordăm
        if (currentNode.getPointsReward() > 0 && !visitedNodes.contains(currentNodeIndex)) {
            pointsManager.addPoints(this, "Oltenia", currentNode.getPointsReward());
            Toast.makeText(this, "Ai primit " + currentNode.getPointsReward() + " puncte!", Toast.LENGTH_SHORT).show();
            showConfetti();
        }
    }
    
    private void setupButtons(OlteniaStoryNode node) {
        // Ascundem toate butoanele inițial
        nextButton.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
        optionOneButton.setVisibility(View.GONE);
        optionTwoButton.setVisibility(View.GONE);
        finishButton.setVisibility(View.GONE);
        feedbackCard.setVisibility(View.GONE);
        
        System.out.println("DEBUG: Setting up buttons for node " + currentNodeIndex + 
                           " (type: " + node.getNodeType() + ", hasMultipleChoices: " + node.hasMultipleChoices() + ")");
        
        // Setăm vizibilitatea butoanelor în funcție de context
        backButton.setVisibility(currentNodeIndex > 0 ? View.VISIBLE : View.GONE);
        
        // Gestionare specială pentru nodul 11 (după quiz)
        if (currentNodeIndex == 11) {
            System.out.println("DEBUG: Special handling for node 11 (post-quiz)");
            nextButton.setText("Continuă la Cultură și tradiții");
            nextButton.setVisibility(View.VISIBLE);
            nextButton.startAnimation(slideInAnimation);
            nextButton.setOnClickListener(v -> {
                System.out.println("DEBUG: Direct navigation from node 11 to node 3");
                currentNodeIndex = 3;
                showCurrentNode();
            });
            return;
        }
        
        // FIX SPECIAL: Verificăm dacă suntem la nodul 3 și ne asigurăm că avem o modalitate de a continua
        if (currentNodeIndex == 3) {
            System.out.println("DEBUG: Special handling for node 3");
            // Asigurăm-ne că există un buton pentru a continua
            if (!node.hasMultipleChoices() && (node.getNextNodes() == null || node.getNextNodes().length == 0)) {
                // Forțăm afișarea butonului de continuare
                nextButton.setText("Continuă la Arhitectură");
                nextButton.setVisibility(View.VISIBLE);
                nextButton.startAnimation(slideInAnimation);
                nextButton.setOnClickListener(v -> {
                    System.out.println("DEBUG: Force navigation from node 3 to node 4");
                    currentNodeIndex = 4;
                    showCurrentNode();
                });
                return;
            }
        }
        
        if (node.isQuizNode()) {
            // Pentru noduri de quiz, afișăm opțiunile de răspuns
            String[] choices = node.getChoices();
            if (choices != null && choices.length >= 1) {
                optionOneButton.setText(choices[0]);
                optionOneButton.setVisibility(View.VISIBLE);
                optionOneButton.startAnimation(slideInAnimation);
            }
            if (choices != null && choices.length >= 2) {
                optionTwoButton.setText(choices[1]);
                optionTwoButton.setVisibility(View.VISIBLE);
                optionTwoButton.startAnimation(slideInAnimation);
            }
        } else if (node.hasMultipleChoices()) {
            // Pentru noduri cu alegeri multiple
            String[] choices = node.getChoices();
            if (choices != null && choices.length >= 1) {
                optionOneButton.setText(choices[0]);
                optionOneButton.setVisibility(View.VISIBLE);
                optionOneButton.startAnimation(slideInAnimation);
            }
            if (choices != null && choices.length >= 2) {
                optionTwoButton.setText(choices[1]);
                optionTwoButton.setVisibility(View.VISIBLE);
                optionTwoButton.startAnimation(slideInAnimation);
            }
        } else {
            // Pentru noduri simple de poveste
            if (node.getChoices() != null && node.getChoices().length > 0) {
                nextButton.setText(node.getChoices()[0]);
            } else {
                nextButton.setText("Continuă");
            }
            nextButton.setVisibility(View.VISIBLE);
            nextButton.startAnimation(slideInAnimation);
        }
        
        // Verificăm dacă suntem la ultimul nod pentru a afișa butonul de finalizare
        if (currentNodeIndex == 9) { // Ultimul nod de poveste
            finishButton.setVisibility(View.VISIBLE);
            finishButton.startAnimation(slideInAnimation);
            nextButton.setVisibility(View.GONE);
        }
        
        // Verificăm dacă am ajuns la un nod final și nu avem buton de continuare
        boolean hasVisibleNavigationButton = nextButton.getVisibility() == View.VISIBLE || 
                                           optionOneButton.getVisibility() == View.VISIBLE ||
                                           finishButton.getVisibility() == View.VISIBLE;
                                           
        if (!hasVisibleNavigationButton && currentNodeIndex < 9) {
            System.out.println("DEBUG: No navigation buttons visible, adding fallback continue button");
            nextButton.setText("Continuă");
            nextButton.setVisibility(View.VISIBLE);
            nextButton.startAnimation(slideInAnimation);
        }
        
        // Reatașăm event listeners pentru a ne asigura că funcționează corect
        nextButton.setOnClickListener(v -> moveToNextNode());
        backButton.setOnClickListener(v -> moveToPreviousNode());
        finishButton.setOnClickListener(v -> finishStory());
        optionOneButton.setOnClickListener(v -> selectOption(0));
        optionTwoButton.setOnClickListener(v -> selectOption(1));
    }
    
    private void moveToNextNode() {
        System.out.println("DEBUG: moveToNextNode called from node " + currentNodeIndex);
        OlteniaStoryNode currentNode = storyNodes.get(currentNodeIndex);
        if (currentNode != null) {
            System.out.println("DEBUG: Moving from node " + currentNodeIndex);
            
            if (currentNode.getNextNodes() != null && currentNode.getNextNodes().length > 0) {
                int nextNodeId = currentNode.getNextNodes()[0];
                System.out.println("DEBUG: Next node defined: " + nextNodeId);
                
                // Prevenire buclă infinită - dacă următorul nod e același cu cel curent
                if (nextNodeId == currentNodeIndex) {
                    System.out.println("DEBUG: Detected loop! Forcing progression to next sequential node");
                    nextNodeId = currentNodeIndex + 1;
                    // Verificăm dacă nodul există
                    if (!storyNodes.containsKey(nextNodeId)) {
                        for (int i = nextNodeId; i < storyNodes.size() + nextNodeId; i++) {
                            if (storyNodes.containsKey(i)) {
                                nextNodeId = i;
                                break;
                            }
                        }
                    }
                }
                
                // Verificare specifică pentru tranziția de la nodul 3
                if (currentNodeIndex == 3 && !storyNodes.containsKey(nextNodeId)) {
                    System.out.println("DEBUG: Fix applied for node 3 - forcing transition to node 4");
                    nextNodeId = 4;
                }
                
                currentNodeIndex = nextNodeId;
                showCurrentNode();
            } else if (currentNodeIndex < storyNodes.size() - 1) {
                // Dacă nu există noduri definite explicit, trecem la următorul nod numeric
                int nextNodeId = currentNodeIndex + 1;
                System.out.println("DEBUG: No next nodes defined, trying sequential node: " + nextNodeId);
                
                while (nextNodeId < storyNodes.size() && !storyNodes.containsKey(nextNodeId)) {
                    nextNodeId++;
                }
                
                if (storyNodes.containsKey(nextNodeId)) {
                    System.out.println("DEBUG: Found valid next node: " + nextNodeId);
                    currentNodeIndex = nextNodeId;
                    showCurrentNode();
                } else {
                    // Dacă nu se găsește niciun nod valid, trecem la ultimul nod
                    System.out.println("DEBUG: No valid sequential node found, going to conclusion node");
                    currentNodeIndex = 9; // Nodul de concluzie
                    showCurrentNode();
                }
            } else {
                System.out.println("DEBUG: At last node, finishing story");
                finishStory();
            }
        } else {
            System.out.println("ERROR: Current node is null");
            // Încercăm să găsim un nod valid
            for (int i = 0; i < storyNodes.size(); i++) {
                if (storyNodes.containsKey(i)) {
                    System.out.println("DEBUG: Recovered by moving to node " + i);
                    currentNodeIndex = i;
                    showCurrentNode();
                    return;
                }
            }
            finishStory();
        }
    }
    
    private void moveToPreviousNode() {
        if (currentNodeIndex > 0) {
            currentNodeIndex--;
            showCurrentNode();
        }
    }
    
    private void selectOption(int optionIndex) {
        OlteniaStoryNode currentNode = storyNodes.get(currentNodeIndex);
        System.out.println("DEBUG: Selected option " + optionIndex + " on node " + currentNodeIndex);
        
        if (currentNode.isQuizNode()) {
            // Verificăm răspunsul pentru nodurile de quiz
            String selectedAnswer = currentNode.getChoices()[optionIndex];
            boolean isCorrect = currentNode.isCorrectAnswer(selectedAnswer);
            
            System.out.println("DEBUG: Quiz answer selected: " + selectedAnswer + " (correct: " + isCorrect + ")");
            
            // Ascundem toate butoanele în timpul afișării feedback-ului
            optionOneButton.setVisibility(View.GONE);
            optionTwoButton.setVisibility(View.GONE);
            backButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            
            // Afișăm feedback
            feedbackText.setText(currentNode.getFeedback());
            feedbackCard.setVisibility(View.VISIBLE);
            feedbackCard.startAnimation(fadeInAnimation);
            
            // Adăugăm puncte dacă răspunsul este corect
            if (isCorrect && currentNode.getPointsReward() > 0) {
                pointsManager.addPoints(this, "Oltenia", currentNode.getPointsReward());
                Toast.makeText(this, "Răspuns corect! Ai primit " + currentNode.getPointsReward() + " puncte!", Toast.LENGTH_SHORT).show();
                playSuccessSound();
                showConfetti();
            } else {
                playFailureSound();
            }
            
            // Prevenire clickuri multiple
            optionOneButton.setEnabled(false);
            optionTwoButton.setEnabled(false);
            if (optionIndex < 2) { // Dezactivăm doar pentru primele două opțiuni care sunt vizibile
                // Dezactivare temporară pentru a preveni dublu-click
                optionOneButton.setClickable(false);
                optionTwoButton.setClickable(false);
            }
            
            // Trecem la următorul nod după un scurt delay
            final int finalOptionIndex = optionIndex;
            feedbackCard.postDelayed(() -> {
                if (currentNode.getNextNodes() != null && currentNode.getNextNodes().length > finalOptionIndex) {
                    int nextNodeId = currentNode.getNextNodes()[finalOptionIndex];
                    System.out.println("DEBUG: Quiz next node: " + nextNodeId);
                    
                    // Verificăm să nu avem buclă infinită
                    if (nextNodeId == currentNodeIndex) {
                        System.out.println("DEBUG: Detected quiz loop! Forcing next node");
                        nextNodeId = 11; // Forțăm la nodul de feedback
                    }
                    
                    currentNodeIndex = nextNodeId;
                } else {
                    currentNodeIndex++; // Fallback, mergem la următorul nod
                    System.out.println("DEBUG: Quiz fallback to next node: " + currentNodeIndex);
                }
                
                // Reactivăm butoanele pentru o viitoare utilizare
                optionOneButton.setEnabled(true);
                optionTwoButton.setEnabled(true);
                optionOneButton.setClickable(true);
                optionTwoButton.setClickable(true);
                
                feedbackCard.setVisibility(View.GONE);
                showCurrentNode();
            }, 3000);
        } else {
            // Pentru noduri normale cu opțiuni
            if (currentNode.getNextNodes() != null && currentNode.getNextNodes().length > optionIndex) {
                int nextNodeId = currentNode.getNextNodes()[optionIndex];
                System.out.println("DEBUG: Option selected, next node: " + nextNodeId);
                
                // Prevenire bucle - dacă următorul nod e același cu cel curent
                if (nextNodeId == currentNodeIndex) {
                    System.out.println("DEBUG: Detected loop! Moving to sequential next node");
                    nextNodeId = currentNodeIndex + 1;
                    // Verificăm dacă există
                    while (nextNodeId < storyNodes.size() && !storyNodes.containsKey(nextNodeId)) {
                        nextNodeId++;
                    }
                }
                
                currentNodeIndex = nextNodeId;
                showCurrentNode();
            } else {
                // Fallback în caz că nu avem un nod definit
                System.out.println("DEBUG: No next node defined for option, using fallback");
                moveToNextNode();
            }
        }
    }
    
    private void finishStory() {
        // Înainte să finalizăm, oferim utilizatorului un mini-joc interactiv specific oltenesc
        showOlteniaInteractiveElement();
    }
    
    private void showOlteniaInteractiveElement() {
        // Creăm un dialog personalizat pentru mini-joc
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_oltenia_interactive, null);
        builder.setView(view);
        
        // Inițializăm elementele din dialog
        TextView titleText = view.findViewById(R.id.interactiveTitleText);
        TextView instructionsText = view.findViewById(R.id.instructionsText);
        TextView questionText = view.findViewById(R.id.questionText);
        RadioGroup optionsGroup = view.findViewById(R.id.optionsRadioGroup);
        RadioButton option1 = view.findViewById(R.id.option1);
        RadioButton option2 = view.findViewById(R.id.option2);
        RadioButton option3 = view.findViewById(R.id.option3);
        Button submitButton = view.findViewById(R.id.submitButton);
        Button skipButton = view.findViewById(R.id.skipButton);
        TextView resultText = view.findViewById(R.id.resultText);
        
        // Setăm titlul și instrucțiunile
        titleText.setText("Ghicitori Oltenești");
        instructionsText.setText("Testează-ți cunoștințele despre cultura oltenească!");
        
        // Definim ghicitorile și expresiile oltenești
        final String[][] olteniaRiddles = new String[][] {
            // Format: {întrebare, răspuns_corect, opțiune2, opțiune3}
            {"Ce reprezintă \"Masa Tăcerii\" creată de Brâncuși la Târgu Jiu?", 
                "Un simbol al eternității și trecerii timpului", 
                "O masă pentru odihna turiștilor", 
                "Un altar de sacrificiu dacic"},
                
            {"Care dintre următoarele NU este un dans tradițional oltenesc?", 
                "Ardeleana", 
                "Rustemul", 
                "Alunelul"},
                
            {"Ce înseamnă expresia oltenească \"a umbla teleap\"?", 
                "A umbla desculț", 
                "A fi distrat", 
                "A fi dezbrăcat"},
                
            {"Ce instrument muzical este specific Olteniei?", 
                "Cimpoi oltenesc", 
                "Fluierul dobrogean", 
                "Țambalul moldovenesc"},
                
            {"Ce reprezintă \"Poarta Sărutului\" din ansamblul Brâncuși?", 
                "Trecerea de la viață la moarte", 
                "Dragostea dintre un tânăr și o fată", 
                "Intrarea în rai"}
        };
        
        final int[] currentRiddleIndex = {0};
        final int[] correctAnswers = {0};
        
        // Funcție pentru afișarea ghicitorii curente
        Runnable displayRiddle = new Runnable() {
            @Override
            public void run() {
                if (currentRiddleIndex[0] < olteniaRiddles.length) {
                    String[] riddle = olteniaRiddles[currentRiddleIndex[0]];
                    questionText.setText(riddle[0]);
                    
                    // Amestecăm opțiunile
                    List<String> options = new ArrayList<>();
                    options.add(riddle[1]); // răspunsul corect
                    options.add(riddle[2]);
                    options.add(riddle[3]);
                    Collections.shuffle(options);
                    
                    option1.setText(options.get(0));
                    option2.setText(options.get(1));
                    option3.setText(options.get(2));
                    
                    // Resetăm selecția și feedback-ul
                    optionsGroup.clearCheck();
                    resultText.setText("");
                    resultText.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                } else {
                    // Am terminat toate ghicitorile, afișăm rezultatul final
                    questionText.setVisibility(View.GONE);
                    optionsGroup.setVisibility(View.GONE);
                    submitButton.setVisibility(View.GONE);
                    skipButton.setText("Finalizează");
                    
                    titleText.setText("Felicitări!");
                    
                    // Feedback bazat pe numărul de răspunsuri corecte
                    String feedback;
                    int bonusPoints = correctAnswers[0] * 20;
                    
                    if (correctAnswers[0] >= 4) {
                        feedback = "Ești un adevărat expert în cultura Olteniei! Ai răspuns corect la " + 
                                correctAnswers[0] + " din 5 ghicitori. Primești " + bonusPoints + " puncte bonus!";
                        showConfetti();
                    } else if (correctAnswers[0] >= 2) {
                        feedback = "Te descurci bine cu cunoștințele despre Oltenia! Ai răspuns corect la " + 
                                correctAnswers[0] + " din 5 ghicitori. Primești " + bonusPoints + " puncte bonus!";
                    } else {
                        feedback = "Mai ai de învățat despre Oltenia, dar e un început bun! Ai răspuns corect la " + 
                                correctAnswers[0] + " din 5 ghicitori. Primești " + bonusPoints + " puncte bonus!";
                    }
                    
                    instructionsText.setText(feedback);
                    pointsManager.addPoints(OlteniaStoryActivity.this, "Oltenia", bonusPoints);
                    
                    // Arătăm rezultatul
                    resultText.setText("Mulțumim pentru participare!");
                    resultText.setVisibility(View.VISIBLE);
                }
            }
        };
        
        // Afișăm prima ghicitoare
        displayRiddle.run();
        
        // Stilizăm radio butoanele pentru a fi mai vizibile
        option1.setTextSize(16);
        option2.setTextSize(16);
        option3.setTextSize(16);
        
        // Adăugăm padding pentru a mări zona de touch
        option1.setPadding(20, 20, 20, 20);
        option2.setPadding(20, 20, 20, 20);
        option3.setPadding(20, 20, 20, 20);
        
        // Configurăm butoanele
        submitButton.setOnClickListener(v -> {
            // Verificăm răspunsul
            int selectedId = optionsGroup.getCheckedRadioButtonId();
            
            // Debug
            System.out.println("DEBUG: Selected radio ID: " + selectedId);
            System.out.println("DEBUG: Option1 ID: " + option1.getId());
            System.out.println("DEBUG: Option2 ID: " + option2.getId());
            System.out.println("DEBUG: Option3 ID: " + option3.getId());
            
            if (selectedId == -1) {
                Toast.makeText(OlteniaStoryActivity.this, "Te rog selectează un răspuns", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Fixăm problema de selecție
            RadioButton selectedOption;
            String selectedAnswer;
            
            if (selectedId == option1.getId()) {
                selectedOption = option1;
            } else if (selectedId == option2.getId()) {
                selectedOption = option2;
            } else if (selectedId == option3.getId()) {
                selectedOption = option3;
            } else {
                System.out.println("DEBUG: Problemă cu selecția radio button-ului");
                Toast.makeText(OlteniaStoryActivity.this, "Eroare la selecție. Încearcă din nou.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            selectedAnswer = selectedOption.getText().toString();
            System.out.println("DEBUG: Selected answer: " + selectedAnswer);
            
            String correctAnswer = olteniaRiddles[currentRiddleIndex[0]][1];
            
            if (selectedAnswer.equals(correctAnswer)) {
                // Răspuns corect
                resultText.setText("Corect! " + correctAnswer + " este răspunsul corect.");
                resultText.setTextColor(getResources().getColor(R.color.oltenia_accent));
                correctAnswers[0]++;
                playSuccessSound();
            } else {
                // Răspuns greșit
                resultText.setText("Greșit! Răspunsul corect este: " + correctAnswer);
                resultText.setTextColor(Color.RED);
                playFailureSound();
            }
            
            resultText.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);
            
            // Trecem la următoarea ghicitoare după o scurtă pauză
            new Handler().postDelayed(() -> {
                currentRiddleIndex[0]++;
                displayRiddle.run();
            }, 2000);
        });
        
        // Îmbunătățim opțiunile radio pentru a le face mai ușor de selectat
        option1.setOnClickListener(v -> optionsGroup.check(option1.getId()));
        option2.setOnClickListener(v -> optionsGroup.check(option2.getId()));
        option3.setOnClickListener(v -> optionsGroup.check(option3.getId()));
        
        skipButton.setOnClickListener(v -> {
            if (currentRiddleIndex[0] < olteniaRiddles.length) {
                // Trecem la următoarea ghicitoare
                currentRiddleIndex[0]++;
                displayRiddle.run();
            } else {
                // Am terminat jocul, închidem dialogul și continuăm
                AlertDialog dialog = (AlertDialog) skipButton.getTag();
                if (dialog != null) {
                    dialog.dismiss();
                }
                
                // Acum finalizăm cu adevărat povestea
                completeStory();
            }
        });
        
        // Creăm și afișăm dialogul
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        
        // Facem dialogul să ocupe mai mult spațiu pe ecran
        dialog.setOnShowListener(dialogInterface -> {
            // Configurăm opțiunile radio pentru a funcționa corect
            optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
                System.out.println("DEBUG: Radio group selection changed: " + checkedId);
                // Activăm butonul de submit când avem o selecție
                if (checkedId != -1) {
                    submitButton.setEnabled(true);
                }
            });
        });
        
        dialog.show();
        
        // Stocăm referința dialogului în butonul de skip pentru a-l putea închide mai târziu
        skipButton.setTag(dialog);
    }
    
    private void completeStory() {
        // Add points for completing the story if not already awarded
        if (currentNodeIndex == 9 && !visitedNodes.contains(9)) {
            pointsManager.addPoints(this, "Oltenia", 100);
            Toast.makeText(this, "Felicitări! Ai finalizat povestea Olteniei și ai primit 100 puncte!", Toast.LENGTH_LONG).show();
        }
        
        // Return to Oltenia activity
        Intent intent = new Intent(this, Oltenia.class);
        startActivity(intent);
        finish();
    }
    
    private void animateNodeTransition() {
        // Animație mai elegantă pentru tranziția între noduri
        ValueAnimator fadeAnimator = ValueAnimator.ofFloat(0f, 1f);
        fadeAnimator.setDuration(800);
        fadeAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        fadeAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            storyTextView.setAlpha(value);
            if (storyTitleView.getVisibility() == View.VISIBLE) {
                storyTitleView.setAlpha(value);
            }
            if (storyContextView.getVisibility() == View.VISIBLE) {
                storyContextView.setAlpha(value);
            }
            if (storyImageView.getVisibility() == View.VISIBLE) {
                storyImageView.setAlpha(value);
            }
        });
        fadeAnimator.start();
    }
    
    private void showConfetti() {
        if (confettiView != null) {
            confettiView.setVisibility(View.VISIBLE);
            confettiView.setAlpha(0f);
            
            // Animație de apariție graduală pentru confetti
            confettiView.animate()
                .alpha(1f)
                .setDuration(600)
                .start();
            
            // Ascundem confetti cu animație de estompare
            new Handler().postDelayed(() -> {
                if (confettiView.getVisibility() == View.VISIBLE) {
                    confettiView.animate()
                        .alpha(0f)
                        .setDuration(800)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                confettiView.setVisibility(View.GONE);
                            }
                        })
                        .start();
                }
            }, 2000);
        }
    }
    
    private void playSuccessSound() {
        if (successSound != null) {
            if (successSound.isPlaying()) {
                successSound.stop();
                successSound.prepareAsync();
            }
            successSound.start();
        }
    }
    
    private void playFailureSound() {
        if (failureSound != null) {
            if (failureSound.isPlaying()) {
                failureSound.stop();
                failureSound.prepareAsync();
            }
            failureSound.start();
        }
    }
    
    @Override
    public void onBackPressed() {
        // Apelăm mai întâi metoda părinte
        super.onBackPressed();
        
        // Apoi codul nostru specific
        Intent intent = new Intent(this, Oltenia.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
        if (successSound != null) {
            successSound.release();
            successSound = null;
        }
        if (failureSound != null) {
            failureSound.release();
            failureSound = null;
        }
    }
    
    public void goBack(View view) {
        onBackPressed();
    }

    // Adăugăm o nouă metodă goBack fără parametri
    public void goBack() {
        onBackPressed();
    }
} 