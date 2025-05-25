package com.example.myapplication.dobrogeausage;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.model.DobrogeaStoryNode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class CasinoStoryActivity extends AppCompatActivity {
    private TextView storyText;
    private TextView storyTitle;
    private TextView storyContext;
    private MaterialButton redButton;
    private MaterialButton blackButton;
    private MaterialButton nextButton;
    private MaterialButton continueButton;
    private MaterialButton storyButton;
    private MaterialButton exitButton;
    private MaterialCardView feedbackCard;
    private MaterialCardView bettingCard;
    private TextView feedbackText;
    private TextInputEditText betAmountInput;
    private PointsManager pointsManager;
    private int currentSceneIndex = 0;
    private Map<Integer, DobrogeaStoryNode> storyNodes;
    private Random random;
    private boolean isStoryMode = false;
    private int artifactPieces = 0;
    private boolean hasMetDetective = false;
    private boolean hasMetRival = false;
    private boolean hasMetHelper = false;
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;
    private int currentPoints = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_casino_story);

        initializeViews();
        initializeStory();
        initializeTextToSpeech();
        showScene();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("ro", "RO"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Limba română nu este suportată pentru citire", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Eroare la inițializarea text-to-speech", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private void initializeViews() {
        storyText = findViewById(R.id.storyText);
        storyTitle = findViewById(R.id.storyTitle);
        storyContext = findViewById(R.id.storyContext);
        redButton = findViewById(R.id.redButton);
        blackButton = findViewById(R.id.blackButton);
        nextButton = findViewById(R.id.nextButton);
        continueButton = findViewById(R.id.continueButton);
        storyButton = findViewById(R.id.storyButton);
        exitButton = findViewById(R.id.exitButton);
        feedbackCard = findViewById(R.id.feedbackCard);
        bettingCard = findViewById(R.id.bettingCard);
        feedbackText = findViewById(R.id.feedbackText);
        betAmountInput = findViewById(R.id.betAmountInput);
        pointsManager = PointsManager.getInstance(this);
        random = new Random();
        
        // Get current points
        currentPoints = pointsManager.getPoints(this);
        
        // Asigură-te că toate elementele sunt vizibile inițial
        storyText.setVisibility(View.VISIBLE);
        storyTitle.setVisibility(View.VISIBLE);
        storyContext.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        continueButton.setVisibility(View.GONE); // Ascuns inițial
        exitButton.setVisibility(View.VISIBLE);
        storyButton.setVisibility(View.VISIBLE);
        feedbackCard.setVisibility(View.GONE);
        bettingCard.setVisibility(View.GONE);

        // Adaugă listener pentru butonul de poveste
        storyButton.setOnClickListener(v -> toggleStoryReading());
    }

    private void toggleStoryReading() {
        if (isSpeaking) {
            textToSpeech.stop();
            storyButton.setText("Citește povestea");
            isSpeaking = false;
        } else {
            StringBuilder textToRead = new StringBuilder();
            if (storyTitle.getVisibility() == View.VISIBLE) {
                textToRead.append(storyTitle.getText()).append(". ");
            }
            textToRead.append(storyText.getText());
            if (storyContext.getVisibility() == View.VISIBLE) {
                textToRead.append(". ").append(storyContext.getText());
            }
            textToSpeech.speak(textToRead.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
            storyButton.setText("Oprește citirea");
            isSpeaking = true;
        }
    }

    private void initializeStory() {
        storyNodes = new HashMap<>();
        
        // Introduction scene
        storyNodes.put(0, new DobrogeaStoryNode.Builder(0,
            "Bine ai venit la Cazinoul din Constanța! Ești în anul 1920, iar în fața ta se află una dintre cele mai " +
            "impunătoare clădiri ale orașului. Cazinoul, construit în stil Art Nouveau, a fost inaugurat în 1910 și " +
            "a devenit rapid un simbol al orașului. Aici, în această sală elegantă, se întâlneau cei mai bogați oameni " +
            "din întreaga Europă. Ce dorești să faci?")
            .choices(new String[]{"Joc direct", "Ascultă povestea", "Ieși din cazino"})
            .nextNodes(new int[]{10, 1, -1})
            .pointsReward(0)
            .build());

        // Story path - Scene 1 (Architecture)
        storyNodes.put(1, new DobrogeaStoryNode.Builder(1,
            "În timp ce intri în cazino, observi că sala este decorată cu cristale Murano și candelabre de aur. " +
            "Pereții sunt acoperiți cu tapet de mătase, iar tavanul este pictat cu scene mitologice. Cazinoul a fost " +
            "proiectat de arhitecții Petre Antonescu și Radu Dudescu, inspirându-se de la Monte Carlo.\n\n" +
            "Deodată, un bătrân cu pălărie de fetru și baston te abordează. Are un accent dobrogean pronunțat și " +
            "îți spune că este detectivul Constantin Popescu. Te informează despre un artefact istoric de neprețuit " +
            "care a fost pierdut în cazino - o medalie din aur pur, oferită de regele Carol I în 1878, când Dobrogea " +
            "a fost anexată României. Medaliei îi lipsește o piesă, și detectivul crede că este ascunsă undeva în cazino. " +
            "Te roagă să-l ajuți să o găsească, spunându-ți că va trebui să câștigi încrederea personajelor cheie și " +
            "să răspunzi corect la întrebări despre istoria Dobrogei.")
            .choices(new String[]{"Acceptă provocarea", "Refuză și începe să joci", "Ieși din cazino"})
            .nextNodes(new int[]{2, 10, -1})
            .pointsReward(0)
            .build());

        // Story path - Scene 2 (Historical Figures)
        storyNodes.put(2, new DobrogeaStoryNode.Builder(2,
            "Accepti provocarea detectivului. El îți spune că prima persoană pe care trebuie să o convingi este " +
            "directorul cazinoului, domnul Alexandru Ionescu. Detectivul te avertizează că există și un rival - " +
            "un arheolog britanic pe nume James Blackwood, care caută și el medalia pentru muzeul britanic.\n\n" +
            "În timp ce te plimbi prin sală, observi că în această sală au jucat personalități precum " +
            "Mihail Sadoveanu, George Enescu și chiar și membri ai familiei regale. Cazinoul a fost un loc " +
            "de întâlnire pentru artiști, scriitori și diplomați.\n\n" +
            "Deodată, te întâlnești cu domnul Ionescu, care pare sceptic față de tine. Te întreabă despre istoria " +
            "Dobrogei pentru a-ți dovedi cunoștințele. Ce răspundești?")
            .choices(new String[]{"Tomis a fost fondat de greci în secolul al VI-lea î.Hr.", 
                                "Callatis a fost fondat de greci în secolul al VI-lea î.Hr.", 
                                "Histria a fost fondată de greci în secolul al VII-lea î.Hr."})
            .nextNodes(new int[]{3, 5, 3})
            .correctAnswer("Histria a fost fondată de greci în secolul al VII-lea î.Hr. și este cel mai vechi oraș atestat pe teritoriul României.")
            .feedback("Histria a fost fondată de greci în secolul al VII-lea î.Hr. și este cel mai vechi oraș atestat pe teritoriul României.")
            .pointsReward(10)
            .build());

        // Story path - Scene 3 (Historical Events)
        storyNodes.put(3, new DobrogeaStoryNode.Builder(3,
            "Răspundeți corect! Domnul Ionescu este impresionat de cunoștințele tale și îți spune că a auzit " +
            "că o bucată din medalie ar putea fi ascunsă în tavanul pictat al sălii de joc. Te roagă să te " +
            "întâlnești cu Maria, conservatorul cazinoului, care are cheile sălii.\n\n" +
            "În timp ce te pregătești să o cauți, observi că în această sală au avut loc evenimente istorice. " +
            "Aici s-au semnat tratate importante, s-au întâlnit politicieni și s-au luat decizii care au schimbat " +
            "destinul Dobrogea. Cazinoul nu era doar un loc de joc, ci și un centru important al vieții sociale " +
            "și politice din Constanța.\n\n" +
            "Te întâlnești cu Maria, o femeie în vârstă cu ochelari și un zâmbet prietenos. Ea îți spune că " +
            "poți intra în sala de joc, dar trebuie să răspunzi corect la o întrebare despre electrificarea Constanței. " +
            "Ce răspundești?")
            .choices(new String[]{"Constanța a fost primul oraș electrificat din România în 1882", 
                                "Constanța a fost electrificată în 1900", 
                                "Constanța a fost electrificată în 1920"})
            .nextNodes(new int[]{4, 5, 5})
            .correctAnswer("Constanța a fost primul oraș electrificat din România în anul 1882.")
            .feedback("Constanța a fost primul oraș electrificat din România în anul 1882.")
            .pointsReward(10)
            .build());

        // Story path - Scene 4 (First Piece)
        storyNodes.put(4, new DobrogeaStoryNode.Builder(4,
            "Răspundeți corect! Maria este impresionată și îți dă cheile sălii. După o căutare îndelungată, " +
            "găsești o mică piesă din medalie ascunsă într-un colț al tavanului pictat. Este prima piesă din cele " +
            "trei necesare pentru a reconstitui medalia completă.\n\n" +
            "Maria îți spune că a doua piesă ar putea fi în posesia unui jucător bogat care vine des la cazino - " +
            "domnul Vasile Dumitrescu. Pentru a-l convinge să-ți dea piesa, va trebui să câștigi la ruletă. " +
            "Vrei să începi să joci sau să cauți mai întâi a treia piesă?")
            .choices(new String[]{"Începe să joci", "Caută a treia piesă", "Ieși din cazino"})
            .nextNodes(new int[]{10, 6, -1})
            .pointsReward(0)
            .build());

        // Story path - Scene 5 (Wrong Answer)
        storyNodes.put(5, new DobrogeaStoryNode.Builder(5,
            "Răspundeți greșit! Persoana cu care vorbești este dezamăgită de cunoștințele tale despre Dobrogea. " +
            "Îți sugerează să te documentezi mai bine înainte de a continua căutarea. Poți încerca din nou sau " +
            "să începi să joci direct.")
            .choices(new String[]{"Încearcă din nou", "Începe să joci", "Ieși din cazino"})
            .nextNodes(new int[]{1, 10, -1})
            .pointsReward(0)
            .build());

        // Story path - Scene 6 (Library)
        storyNodes.put(6, new DobrogeaStoryNode.Builder(6,
            "Decizi să cauți mai întâi a treia piesă. Maria îți spune că ar putea fi în biblioteca cazinoului, " +
            "care conține cărți rare despre istoria Dobrogei. În bibliotecă, te întâlnești cu bibliotecarul, " +
            "domnul Petre, care te întreabă despre Delta Dunării. Ce răspundești?")
            .choices(new String[]{"Delta Dunării este cea mai mare rezervație de stuf din lume și este inclusă în Patrimoniul Mondial UNESCO", 
                                "Delta Dunării este cea mai mare rezervație de stuf din lume", 
                                "Delta Dunării este inclusă în Patrimoniul Mondial UNESCO"})
            .nextNodes(new int[]{7, 5, 5})
            .correctAnswer("Delta Dunării este cea mai mare rezervație de stuf din lume și este inclusă în Patrimoniul Mondial UNESCO.")
            .feedback("Delta Dunării este cea mai mare rezervație de stuf din lume și este inclusă în Patrimoniul Mondial UNESCO.")
            .pointsReward(10)
            .build());

        // Story path - Scene 7 (Third Piece)
        storyNodes.put(7, new DobrogeaStoryNode.Builder(7,
            "Răspundeți corect! Domnul Petre este impresionat și îți permite să cauți în bibliotecă. După o " +
            "căutare îndelungată, găsești a treia piesă din medalie ascunsă într-o carte veche despre Dobrogea. " +
            "Acum ai două piese din medalie!\n\n" +
            "Domnul Petre îți spune că a doua piesă ar putea fi în posesia unui jucător bogat care vine des la cazino - " +
            "domnul Vasile Dumitrescu. Pentru a-l convinge să-ți dea piesa, va trebui să câștigi la ruletă. " +
            "Vrei să începi să joci?")
            .choices(new String[]{"Începe să joci", "Ieși din cazino"})
            .nextNodes(new int[]{10, -1})
            .pointsReward(0)
            .build());

        // Game scene - First betting (Historical)
        storyNodes.put(10, new DobrogeaStoryNode.Builder(10,
            "Te-ai așezat la masa de ruletă. Croupierul te salută și te invită să joci. Pentru a câștiga încrederea " +
            "domnului Dumitrescu, trebuie să pariezi pe o întrebare istorică: Care oraș a fost fondat primul: " +
            "Tomis sau Callatis? Alege roșu pentru Tomis sau negru pentru Callatis. Câte puncte vrei să pariezi?")
            .nodeType(DobrogeaStoryNode.NodeType.BETTING)
            .betType(DobrogeaStoryNode.BetType.RED_BLACK)
            .betRange(10, 100)
            .correctAnswer("red")
            .pointsReward(0)
            .build());

        // Game scene - After first bet
        storyNodes.put(11, new DobrogeaStoryNode.Builder(11,
            "Ai pariat %d puncte pe %s. Ruleta se învârte...")
            .choices(new String[]{"Continuă", "Ieși din cazino"})
            .nextNodes(new int[]{12, -1})
            .pointsReward(0)
            .build());

        // Game scene - Second betting (Historical)
        storyNodes.put(12, new DobrogeaStoryNode.Builder(12,
            "Vrei să mai încerci o dată? Această dată, pariul este despre o altă întrebare istorică: " +
            "Care a fost primul oraș din Dobrogea care a fost electrificat? Alege roșu pentru Constanța sau " +
            "negru pentru Tulcea. Câte puncte vrei să pariezi?")
            .nodeType(DobrogeaStoryNode.NodeType.BETTING)
            .betType(DobrogeaStoryNode.BetType.RED_BLACK)
            .betRange(20, 200)
            .correctAnswer("black")
            .pointsReward(0)
            .build());

        // Game scene - After second bet
        storyNodes.put(13, new DobrogeaStoryNode.Builder(13,
            "Ai pariat %d puncte pe %s. Ruleta se învârte...")
            .choices(new String[]{"Continuă", "Ieși din cazino"})
            .nextNodes(new int[]{14, -1})
            .pointsReward(0)
            .build());

        // Game scene - Final betting (Historical)
        storyNodes.put(14, new DobrogeaStoryNode.Builder(14,
            "Vrei să mai încerci o ultimă dată? Această dată, pariul este despre o întrebare istorică finală: " +
            "Care a fost primul oraș din Dobrogea care a fost conectat la rețeaua de cale ferată? " +
            "Alege roșu pentru Constanța sau negru pentru Medgidia. Câte puncte vrei să pariezi?")
            .nodeType(DobrogeaStoryNode.NodeType.BETTING)
            .betType(DobrogeaStoryNode.BetType.RED_BLACK)
            .betRange(50, 500)
            .correctAnswer("red")
            .pointsReward(0)
            .build());

        // Final scene - Success
        storyNodes.put(15, new DobrogeaStoryNode.Builder(15,
            "Ai terminat sesiunea de joc. %s Cazinoul din Constanța rămâne una dintre cele mai importante " +
            "clădiri istorice din România, un simbol al perioadei de glorie a orașului. Clădirea a supraviețuit " +
            "tuturor perioadelor istorice și continuă să fie un punct de atracție pentru turiști și localnici.\n\n" +
            "Dacă ai câștigat suficiente puncte, ai reușit să reconstituiești medalia completă și să o predai " +
            "muzeului local, unde va fi expusă pentru generațiile viitoare.")
            .choices(new String[]{"Înapoi la meniu"})
            .nextNodes(new int[]{-1})
            .pointsReward(0)
            .build());

        // Final scene - Failure
        storyNodes.put(16, new DobrogeaStoryNode.Builder(16,
            "Ai terminat sesiunea de joc. %s Din păcate, nu ai reușit să reconstituiești medalia completă. " +
            "James Blackwood, arheologul britanic, a reușit să găsească toate piesele și a predat medalia " +
            "muzeului britanic. Dar nu te descuraja - mai sunt multe mistere istorice de rezolvat în Dobrogea!")
            .choices(new String[]{"Înapoi la meniu"})
            .nextNodes(new int[]{-1})
            .pointsReward(0)
            .build());
    }

    private void showScene() {
        DobrogeaStoryNode currentNode = storyNodes.get(currentSceneIndex);
        if (currentNode == null) {
            finish();
            return;
        }

        // Curăță orice container de opțiuni existent
        LinearLayout mainLayout = findViewById(R.id.buttonContainer);
        for (int i = 0; i < mainLayout.getChildCount(); i++) {
            View child = mainLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                mainLayout.removeView(child);
                break;
            }
        }

        // Setează titlul scenei
        String title = getSceneTitle(currentSceneIndex);
        storyTitle.setText(title);
        storyTitle.setVisibility(View.VISIBLE);

        // Setează textul principal al poveștii
        storyText.setText(currentNode.getStoryText());
        storyText.setVisibility(View.VISIBLE);
        
        // Setează contextul istoric (dacă există)
        String context = getSceneContext(currentSceneIndex);
        if (context != null && !context.isEmpty()) {
            storyContext.setText(context);
            storyContext.setVisibility(View.VISIBLE);
        } else {
            storyContext.setVisibility(View.GONE);
        }

        // Ascunde toate butoanele inițial
        nextButton.setVisibility(View.GONE);
        redButton.setVisibility(View.GONE);
        blackButton.setVisibility(View.GONE);
        betAmountInput.setVisibility(View.GONE);
        bettingCard.setVisibility(View.GONE);
        feedbackCard.setVisibility(View.GONE);
        continueButton.setVisibility(View.GONE);
        
        // Update points display
        currentPoints = pointsManager.getPoints(this);
        
        if (currentSceneIndex == 0) {
            // Prima scenă - afișează cele trei opțiuni principale
            nextButton.setVisibility(View.VISIBLE);
            storyButton.setVisibility(View.VISIBLE);
            exitButton.setVisibility(View.VISIBLE);
            continueButton.setVisibility(View.GONE);

            nextButton.setText("Joc direct");
            storyButton.setText("Citește povestea");
            exitButton.setText("Ieși din cazino");

            nextButton.setOnClickListener(v -> {
                currentSceneIndex = 10; // Mergi la scena de joc
                showScene();
            });

            storyButton.setOnClickListener(v -> {
                currentSceneIndex = 1; // Mergi la începutul poveștii
                showScene();
            });

            exitButton.setOnClickListener(v -> finish());
        } else if (currentNode.isBettingNode()) {
            // Scenă de pariere
            bettingCard.setVisibility(View.VISIBLE);
            redButton.setVisibility(View.VISIBLE);
            blackButton.setVisibility(View.VISIBLE);
            betAmountInput.setVisibility(View.VISIBLE);
            setupBettingButtons(currentNode);
        } else if (currentNode.getCorrectAnswer() != null) {
            // Scenă de quiz
            setupQuizButtons(currentNode);
        } else {
            // Scenă normală de poveste
            nextButton.setVisibility(View.GONE);
            storyButton.setVisibility(View.VISIBLE);
            exitButton.setVisibility(View.VISIBLE);
            continueButton.setVisibility(View.VISIBLE);

            if (currentNode.getChoices() != null && currentNode.getChoices().length > 0) {
                // Configurează butoanele pentru alegeri multiple
                continueButton.setText(currentNode.getChoices()[0]);
                continueButton.setOnClickListener(v -> {
                    currentSceneIndex = currentNode.getNextNodes()[0];
                    showScene();
                });

                storyButton.setText("Citește povestea");
                storyButton.setOnClickListener(v -> toggleStoryReading());
            } else {
                // Configurează pentru continuare simplă
                continueButton.setText("Continuă");
                continueButton.setOnClickListener(v -> {
                    if (currentNode.getNextNodes() != null && currentNode.getNextNodes().length > 0) {
                        currentSceneIndex = currentNode.getNextNodes()[0];
                        showScene();
                    }
                });

                storyButton.setText("Citește povestea");
                storyButton.setOnClickListener(v -> toggleStoryReading());
            }
        }
    }
    
    private void setupQuizButtons(DobrogeaStoryNode node) {
        // Ascunde toate butoanele existente
        nextButton.setVisibility(View.GONE);
        redButton.setVisibility(View.GONE);
        blackButton.setVisibility(View.GONE);
        betAmountInput.setVisibility(View.GONE);
        bettingCard.setVisibility(View.GONE);
        feedbackCard.setVisibility(View.GONE);
        continueButton.setVisibility(View.GONE);
        
        // Șterge orice container de opțiuni existent
        LinearLayout mainLayout = findViewById(R.id.buttonContainer);
        for (int i = 0; i < mainLayout.getChildCount(); i++) {
            View child = mainLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                mainLayout.removeView(child);
                break;
            }
        }
        
        // Create a vertical layout for quiz options
        LinearLayout optionsContainer = new LinearLayout(this);
        optionsContainer.setOrientation(LinearLayout.VERTICAL);
        optionsContainer.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        
        // Add each option as a button
        for (int i = 0; i < node.getChoices().length; i++) {
            MaterialButton optionButton = new MaterialButton(this);
            optionButton.setText(node.getChoices()[i]);
            optionButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            optionButton.setPadding(16, 16, 16, 16);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) optionButton.getLayoutParams();
            params.setMargins(0, 8, 0, 8);
            optionButton.setLayoutParams(params);
            optionButton.setCornerRadius(24);
            optionButton.setTextSize(16);
            optionButton.setTypeface(optionButton.getTypeface(), android.graphics.Typeface.BOLD);
            optionButton.setBackgroundTintList(getResources().getColorStateList(R.color.rom_accent));
            
            final int optionIndex = i;
            optionButton.setOnClickListener(v -> checkQuizAnswer(node, optionIndex));
            
            optionsContainer.addView(optionButton);
        }
        
        // Add the options container to the layout
        mainLayout.addView(optionsContainer, 0);
        
        // Add story and exit buttons
        storyButton.setVisibility(View.VISIBLE);
        exitButton.setVisibility(View.VISIBLE);
        
        storyButton.setText("Citește povestea");
        storyButton.setOnClickListener(v -> toggleStoryReading());
        
        exitButton.setText("Ieși din cazino");
        exitButton.setOnClickListener(v -> finish());
    }
    
    private void checkQuizAnswer(DobrogeaStoryNode node, int selectedOption) {
        String selectedAnswer = node.getChoices()[selectedOption];
        boolean isCorrect = node.isCorrectAnswer(selectedAnswer);
        
        // Ascunde containerul de opțiuni
        LinearLayout mainLayout = findViewById(R.id.buttonContainer);
        // Găsește și ascunde containerul de opțiuni (primul copil al mainLayout)
        if (mainLayout.getChildCount() > 0) {
            View optionsContainer = mainLayout.getChildAt(0);
            if (optionsContainer instanceof LinearLayout) {
                optionsContainer.setVisibility(View.GONE);
            }
        }
        
        // Dezactivează toate butoanele de opțiuni pentru a preveni apăsări multiple
        for (int i = 0; i < mainLayout.getChildCount(); i++) {
            View child = mainLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout optionsContainer = (LinearLayout) child;
                for (int j = 0; j < optionsContainer.getChildCount(); j++) {
                    View optionButton = optionsContainer.getChildAt(j);
                    if (optionButton instanceof MaterialButton) {
                        optionButton.setEnabled(false);
                    }
                }
            }
        }
        
        // Show feedback
        feedbackText.setText(isCorrect ? node.getFeedback() : "Răspuns greșit! Mai încearcă.");
        feedbackCard.setVisibility(View.VISIBLE);
        
        // Add points if correct
        if (isCorrect) {
            pointsManager.addPoints(this, "dobrogea", node.getPointsReward());
            currentPoints = pointsManager.getPoints(this);
            
            // Afișează un mesaj de succes
            Toast.makeText(this, "Răspuns corect! Ai câștigat " + node.getPointsReward() + " puncte!", Toast.LENGTH_SHORT).show();
        } else {
            // Afișează un mesaj de eroare
            Toast.makeText(this, "Răspuns greșit! Mai încearcă.", Toast.LENGTH_SHORT).show();
        }
        
        // Afișează butonul de continuare după un scurt delay
        new android.os.Handler().postDelayed(() -> {
            continueButton.setVisibility(View.VISIBLE);
            continueButton.setText("Continuă");
            continueButton.setOnClickListener(v -> {
                currentSceneIndex = node.getNextNodeForChoice(selectedOption);
                showScene();
            });
        }, 1500);
    }

    private String getSceneTitle(int sceneIndex) {
        switch (sceneIndex) {
            case 0:
                return "Bine ai venit la Cazinoul din Constanța";
            case 1:
                return "Întâlnirea cu Detectivul";
            case 2:
                return "Rivalul Britanic";
            case 3:
                return "Conservatorul Cazinoului";
            case 4:
                return "Prima Piesă din Medalie";
            case 5:
                return "Răspuns Greșit";
            case 6:
                return "Căutarea în Bibliotecă";
            case 7:
                return "A Treia Piesă din Medalie";
            case 10:
                return "Prima Rundă de Ruletă";
            case 11:
                return "Rezultatul Primei Runde";
            case 12:
                return "A Doua Rundă de Ruletă";
            case 13:
                return "Rezultatul Runde a Doua";
            case 14:
                return "Runda Finală de Ruletă";
            case 15:
                return "Final Fericit";
            case 16:
                return "Final Alternativ";
            default:
                return "";
        }
    }

    private String getSceneContext(int sceneIndex) {
        switch (sceneIndex) {
            case 0:
                return "Anul 1920 - Cazinoul din Constanța este una dintre cele mai impunătoare clădiri ale orașului, construit în stil Art Nouveau și inaugurat în 1910.";
            case 1:
                return "Detectivul Constantin Popescu este un personaj istoric fictiv, inspirat de detectivii din perioada interbelică.";
            case 2:
                return "James Blackwood este un personaj fictiv care reprezintă interesele britanice în Dobrogea din perioada interbelică.";
            case 3:
                return "Maria, conservatorul cazinoului, este un personaj fictiv care reprezintă tradiția și istoria locală.";
            case 4:
                return "Medalia din aur pur, oferită de regele Carol I în 1878, este un artefact istoric fictiv care simbolizează importanța Dobrogei pentru România.";
            case 10:
                return "Ruleta este un joc de noroc tradițional în cazinouri, unde jucătorii pariază pe roșu sau negru.";
            case 12:
                return "În această rundă, pariul este legat de istoria electrificării orașelor din Dobrogea.";
            case 14:
                return "Runda finală se concentrează pe istoria căilor ferate din Dobrogea.";
            default:
                return "";
        }
    }

    private void setupChoiceButtons(DobrogeaStoryNode node) {
        // Asigură-te că butonul next este vizibil
        nextButton.setVisibility(View.VISIBLE);
        
        if (node.getChoices() != null && node.getChoices().length > 0) {
            nextButton.setText(node.getChoices()[0]);
            nextButton.setOnClickListener(v -> {
                currentSceneIndex = node.getNextNodes()[0];
                showScene();
            });
        }

    }

    private void setupBettingButtons(DobrogeaStoryNode node) {
        redButton.setOnClickListener(v -> placeBet("roșu", node));
        blackButton.setOnClickListener(v -> placeBet("negru", node));
    }

    private void placeBet(String choice, DobrogeaStoryNode node) {
        String betAmountStr = betAmountInput.getText().toString();
        if (betAmountStr.isEmpty()) {
            Toast.makeText(this, "Te rog introdu suma de pariat", Toast.LENGTH_SHORT).show();
            return;
        }

        int betAmount = Integer.parseInt(betAmountStr);
        if (betAmount < node.getMinBet() || betAmount > node.getMaxBet()) {
            Toast.makeText(this, "Suma trebuie să fie între " + node.getMinBet() + 
                " și " + node.getMaxBet() + " puncte", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentPoints = pointsManager.getPoints(this);
        if (betAmount > currentPoints) {
            Toast.makeText(this, "Nu ai suficiente puncte!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove bet amount from points
        pointsManager.removePoints(this, "dobrogea", betAmount);

        // Determine if player won (50% chance)
        // Generăm un număr aleatoriu între 0 și 1
        boolean won = random.nextBoolean();
        
        // Determinăm culoarea câștigătoare (roșu sau negru)
        String winningColor = random.nextBoolean() ? "roșu" : "negru";
        
        // Verificăm dacă jucătorul a câștigat
        boolean playerWon = choice.equals(winningColor);
        
        String resultText;
        
        if (playerWon) {
            // Player wins double the bet
            pointsManager.addPoints(this, "dobrogea", betAmount * 2);
            resultText = String.format("Ai câștigat! Ruleta a căzut pe %s. Ai primit %d puncte înapoi!", winningColor, betAmount * 2);
            
            // If in story mode and this is the final bet, give the second piece of the artifact
            if (currentSceneIndex == 14) {
                artifactPieces++;
                resultText += "\n\nFelicitări! Ai găsit a doua piesă din medalie!";
            }
        } else {
            resultText = String.format("Ai pierdut! Ruleta a căzut pe %s. Mai încearcă data viitoare!", winningColor);
        }

        // Update story text with bet amount and result
        storyText.setText(String.format(node.getStoryText(), betAmount, choice));
        feedbackText.setText(resultText);
        feedbackCard.setVisibility(View.VISIBLE);
        
        // Show animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        feedbackCard.startAnimation(fadeIn);

        // Move to next scene after a delay
        redButton.setEnabled(false);
        blackButton.setEnabled(false);
        nextButton.setVisibility(View.VISIBLE);
        nextButton.setOnClickListener(v -> {
            currentSceneIndex = node.getNextNodes()[0];
            
            // Check if we should go to success or failure scene
            if (currentSceneIndex == 15) {
                if (artifactPieces >= 2) {
                    currentSceneIndex = 15; // Success scene
                } else {
                    currentSceneIndex = 16; // Failure scene
                }
            }
            
            showScene();
        });
    }

    private void finishGame() {
        int totalPoints = pointsManager.getPoints(this);
        String finalMessage = String.format("Ai terminat sesiunea de joc cu %d puncte. %s",
            totalPoints,
            totalPoints > 0 ? "Felicitări!" : "Mai încearcă data viitoare!");
            
        storyText.setText(finalMessage);
        feedbackCard.setVisibility(View.GONE);
        nextButton.setText("Înapoi");
        nextButton.setOnClickListener(v -> finish());
    }
} 