package com.example.myapplication.moldovausage;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.model.MoldovaStoryNode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.util.Log;
import android.util.SparseArray;
import java.util.List;
import androidx.core.widget.NestedScrollView;
import android.widget.ProgressBar;

public class MoldovaStoryActivity extends AppCompatActivity {
    private static final String TAG = "MoldovaStoryActivity";
    
    private TextView storyText;
    private TextView storyTitle;
    private TextView storyContext;
    private ImageView storyImageView;
    private Button choiceButton1;
    private Button choiceButton2;
    private Button choiceButton3;
    private Button nextButton;
    private Button storyButton;
    private Button exitButton;
    private ImageView headerBackButton;
    private ImageView soundToggleButton;
    private MaterialCardView feedbackCard;
    private MaterialCardView bettingCard;
    private MaterialCardView interactiveCardView;
    private TextView feedbackText;
    private TextView testTitle;
    private TextView testDesc;
    private MaterialCardView objectCard1;
    private MaterialCardView objectCard2;
    private MaterialCardView objectCard3;
    private TextInputEditText betAmountInput;
    private ProgressBar progressIndicator;
    private ImageView animationView;
    private PointsManager pointsManager;
    private int currentSceneIndex = 0;
    private HashMap<Integer, MoldovaStoryNode> storyNodes;
    private Random random;
    private boolean isStoryMode = false;
    private int artifactPieces = 0;
    private boolean hasMetStefan = false;
    private boolean hasFoundTreasure = false;
    private boolean hasJoinedArmy = false;
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;
    private int currentPoints = 0;
    private int totalStoryNodes = 0;
    private MediaPlayer backgroundMusic;
    private MediaPlayer soundEffect;
    private boolean isSoundEnabled = true;
    private Animation fadeInAnimation;
    private Animation heroAnimation;
    private Handler handler = new Handler();
    private LinearLayout buttonContainer;
    private LinearLayout choiceContainer;
    private NestedScrollView scrollView;
    private MaterialCardView storyCardView;
    private MaterialCardView headerCardView;
    private boolean shouldResetScroll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moldova_story);
        
        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        // Initialize views
        initializeViews();
        
        // Initialize audio
        initializeAudio();
        
        // Initialize animations
        initializeAnimations();
        
        // Initialize text-to-speech
        initializeTextToSpeech();
        
        // Setup button listeners
        setupButtonListeners();
        
        // Initialize story data
        initializeStory();
        
        // Ensure layouts are visible
        buttonContainer.setVisibility(View.VISIBLE);
        
        // Show first scene
        showScene(0);
        
        // Debug - show visibility state
        Toast.makeText(this, "Poveste inițializată. Folosește butoanele pentru a naviga.", Toast.LENGTH_SHORT).show();
    }
    
    private void initializeViews() {
        // Story elements
        storyText = findViewById(R.id.storyText);
        storyTitle = findViewById(R.id.storyTitle);
        storyContext = findViewById(R.id.storyContext);
        storyImageView = findViewById(R.id.storyImage);
        
        // Animation elements
        progressIndicator = findViewById(R.id.progressIndicator);
        animationView = findViewById(R.id.batAnimation);
        
        // Buttons
        choiceButton1 = findViewById(R.id.choiceButton1);
        choiceButton2 = findViewById(R.id.choiceButton2);
        choiceButton3 = findViewById(R.id.choiceButton3);
        nextButton = findViewById(R.id.nextButton);
        storyButton = findViewById(R.id.storyButton);
        exitButton = findViewById(R.id.exitButton);
        
        // Containers
        buttonContainer = findViewById(R.id.buttonContainer);
        choiceContainer = findViewById(R.id.choiceButtonsContainer);
        
        // Card views
        feedbackCard = findViewById(R.id.feedbackCard);
        bettingCard = findViewById(R.id.bettingCard);
        interactiveCardView = findViewById(R.id.interactive_card);
        
        // Feedback elements
        feedbackText = findViewById(R.id.feedbackText);
        testTitle = findViewById(R.id.vampireTestTitle);
        testDesc = findViewById(R.id.vampireTestDesc);
        
        // Object cards for interactive tests - these might not exist in the layout yet
        try {
            objectCard1 = findViewById(R.id.objectCard1);
            objectCard2 = findViewById(R.id.objectCard2);
            objectCard3 = findViewById(R.id.objectCard3);
        } catch (Exception e) {
            Log.e(TAG, "Object cards not found in layout: " + e.getMessage());
        }
        
        // Sound-related
        headerBackButton = findViewById(R.id.headerBackButton);
        soundToggleButton = findViewById(R.id.soundToggleButton);
        
        // Initialize points manager
        pointsManager = PointsManager.getInstance(this);
        
        // Set default visibility
        if (buttonContainer != null) buttonContainer.setVisibility(View.VISIBLE);
        if (choiceContainer != null) choiceContainer.setVisibility(View.GONE);
        if (nextButton != null) nextButton.setVisibility(View.VISIBLE);
        
        // Hide interactive cards initially
        if (feedbackCard != null) feedbackCard.setVisibility(View.GONE);
        if (bettingCard != null) bettingCard.setVisibility(View.GONE);
        if (interactiveCardView != null) interactiveCardView.setVisibility(View.GONE);
        if (animationView != null) animationView.setVisibility(View.GONE);
        
        // Container views
        choiceContainer = findViewById(R.id.choiceButtonsContainer);
        interactiveCardView = findViewById(R.id.interactive_card);
        storyCardView = findViewById(R.id.storyCardView);
        headerCardView = findViewById(R.id.headerCard);
        progressIndicator = findViewById(R.id.progressIndicator);
        scrollView = findViewById(R.id.story_scroll_view);
    }
    
    private void initializeAudio() {
        // Initialize background music
        backgroundMusic = MediaPlayer.create(this, R.raw.dark_ambient);
        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.2f, 0.2f);
            backgroundMusic.start();
        }
        
        // Initialize sound effect player
        soundEffect = MediaPlayer.create(this, R.raw.thunder);
    }
    
    private void initializeAnimations() {
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        heroAnimation = AnimationUtils.loadAnimation(this, R.anim.fly_animation);
        
        fadeInAnimation.setDuration(1000);
        heroAnimation.setDuration(1500);
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
    
    private void setupButtonListeners() {
        // Add listener for story reading button
        if (storyButton != null) {
            storyButton.setOnClickListener(v -> toggleStoryReading());
        }
        
        // Add listener for exit button
        if (exitButton != null) {
            exitButton.setOnClickListener(v -> finish());
        }
        
        // Add listener for next button
        if (nextButton != null) {
            nextButton.setOnClickListener(v -> {
                Toast.makeText(this, "Navigare la următoarea scenă", Toast.LENGTH_SHORT).show();
                currentSceneIndex++;
                if (currentSceneIndex >= storyNodes.size()) {
                    // If we've reached the end, go back to the first scene
                    currentSceneIndex = 0;
                }
                
                // Reset scroll on next button
                shouldResetScroll = true;
                
                // Show the scene
                showScene(currentSceneIndex);
            });
        }
        
        // Back button in header
        if (headerBackButton != null) {
            headerBackButton.setOnClickListener(v -> finish());
        }
        
        // Sound toggle
        if (soundToggleButton != null) {
            soundToggleButton.setOnClickListener(v -> toggleSound());
        }
        
        // Interactive element cards - only set up if they exist
        if (objectCard1 != null) {
            objectCard1.setOnClickListener(v -> selectHistoricalTest(1));
        }
        if (objectCard2 != null) {
            objectCard2.setOnClickListener(v -> selectHistoricalTest(2));
        }
        if (objectCard3 != null) {
            objectCard3.setOnClickListener(v -> selectHistoricalTest(3));
        }
    }
    
    private void toggleSound() {
        isSoundEnabled = !isSoundEnabled;
        
        if (isSoundEnabled) {
            soundToggleButton.setImageResource(R.drawable.ic_sound_on);
            if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
                backgroundMusic.start();
            }
        } else {
            soundToggleButton.setImageResource(R.drawable.ic_sound_off);
            if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }
        }
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
    
    private void animateSceneTransition() {
        // Stop any ongoing TTS
        if (isSpeaking) {
            textToSpeech.stop();
            isSpeaking = false;
            if (storyButton != null) {
                storyButton.setText("Citește povestea");
            }
        }
        
        // Play animation
        if (animationView != null) {
            animationView.setVisibility(View.VISIBLE);
            animationView.startAnimation(heroAnimation);
        }
        
        // Play sound effect if enabled
        if (isSoundEnabled && soundEffect != null) {
            soundEffect.start();
        }
        
        // Delay showing next scene to allow animation to play
        handler.postDelayed(() -> {
            if (animationView != null) {
                animationView.clearAnimation();
                animationView.setVisibility(View.GONE);
            }
            
            // Don't call showScene again here to avoid multiple scene transitions
            // and unwanted scroll resets
        }, 1000);
    }
    
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
        
        if (soundEffect != null) {
            soundEffect.release();
            soundEffect = null;
        }
        
        super.onDestroy();
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
        if (backgroundMusic != null && !backgroundMusic.isPlaying() && isSoundEnabled) {
            backgroundMusic.start();
        }
    }
    
    private void initializeStory() {
        storyNodes = new HashMap<>();
        
        // Create root node - Intro to Moldova
        MoldovaStoryNode rootNode = new MoldovaStoryNode.Builder(0, "Bine ați venit în Moldova, o bijuterie ascunsă între România și Ucraina. În timp ce stați la graniță, " +
                "vedeți dealurile blânde acoperite cu vii și sate mici care punctează peisajul. Un ghid local vă invită să explorați " +
                "Moldova, o țară cu tradiții bogate și peisaje pitorești.")
                .title("Bun venit în Moldova")
                .context("La granița Moldovei")
                .sceneType("landscape")
                .addChoice("Explorează folclorul și tradițiile", 1)
                .addChoice("Află despre vinul și bucătăria moldovenească", 5)
                .addChoice("Descoperă istoria și monumentele", 3)
                .build();
        
        // Node 1 - Folklore
        MoldovaStoryNode forestNode = new MoldovaStoryNode.Builder(1, "Ghidul tău te duce într-o pădure străveche lângă Codri. 'Această pădure este locuită de spirite antice,' " +
                "îți spune el în timp ce auzi sunete îndepărtate de fluier. 'Folclorul nostru este plin de povești despre ființe mistice " +
                "care protejează pământul și copacii. Unii localnici încă lasă mici ofrande pentru a menține spiritele mulțumite.'")
                .title("Pădurile Fermecate")
                .context("Codrii Moldovei")
                .sceneType("landscape")
                .addChoice("Urmează sunetul muzicii", 2)
                .addChoice("Întreabă despre Ștefan cel Mare", 3)
                .build();
        
        // Node 2 - Dance
        MoldovaStoryNode danceNode = new MoldovaStoryNode.Builder(2, "Muzica devine mai puternică pe măsură ce te aventurezi mai adânc în pădurea încetoșată. Lumina lunii pătrunde prin coroane, dezvăluind o poiană " +
                "unde localnicii s-au adunat pentru o Hora - un dans tradițional circular. Toată lumea se ține de mâini, mișcându-se în ritmul doinelor melancolice " +
                "urmate de melodii mai vesele și energice ale lăutarilor.")
                .title("Dansul Hora")
                .context("Poiană în Codri")
                .sceneType("village")
                .addChoice("Alătură-te dansului", 4)
                .addChoice("Întreabă despre Ștefan cel Mare", 3)
                .build();
        
        // Node 3 - Stefan cel Mare
        MoldovaStoryNode stefanNode = new MoldovaStoryNode.Builder(3, "'Lasă-mă să-ți povestesc despre cel mai mare erou al Moldovei,' spune ghidul tău, vocea plină de mândrie. 'Ștefan cel Mare a domnit în timpul epocii noastre de aur " +
                "și a apărat țara noastră mică de imperii puternice. A construit cetăți și mănăstiri care încă stau ca martori ai măreției sale. " +
                "După fiecare victorie, ridica o mănăstire pentru a mulțumi lui Dumnezeu.'")
                .title("Ștefan cel Mare")
                .context("Despre istoria Moldovei")
                .sceneType("castle")
                .addChoice("Vizitează o mănăstire istorică", 13)
                .addChoice("Întoarce-te la sat pentru a te odihni", 4)
                .build();
        
        // Node 4 - Night in the village
        MoldovaStoryNode nightNode = new MoldovaStoryNode.Builder(4, "Pe măsură ce întunericul învăluie pădurea, îți instalezi tabăra sub copaci înalți ale căror ramuri par să șoptească secrete străvechi. În jurul focului de tabără, " +
                "ghidul tău începe să spună povești despre strigoi și spirite, despre fete frumoase care dansează noaptea în pădure, " +
                "și despre Ileana Cosânzeana, o zână care apare celor cu inima pură pentru a le oferi daruri și înțelepciune.")
                .title("Povești la Foc de Tabără")
                .context("Noapte în pădurea Codri")
                .sceneType("landscape")
                .addChoice("Întreabă despre bucătăria moldovenească", 5)
                .addChoice("Întreabă despre Mănăstirea Putna", 13)
                .build();
        
        // Node 5 - Food
        MoldovaStoryNode foodNode = new MoldovaStoryNode.Builder(5, "Ghidul tău te duce într-un sat tradițional moldovenesc unde se pregătește o masă pentru o nuntă. Aerul este încărcat cu aroma de " +
                "mămăligă proaspătă, plăcinte fierbinți, și sarmale învelite în frunze de viță. La mesele lungi, localnicii te invită să guști din " +
                "platourile cu brânză de oaie, friptură la proțap, și să bei vin făcut în casă care curge din butoaie mari de stejar.")
                .title("Ospăț Moldovenesc")
                .context("Satul Bălănești")
                .sceneType("village")
                .addChoice("Întreabă despre vinul moldovenesc", 6)
                .addChoice("Explorează pădurea Codri", 1)
                .build();
        
        // Node 6 - Wine
        MoldovaStoryNode wineNode = new MoldovaStoryNode.Builder(6, "Un viticultor în vârstă cu pielea arsă de soare și ochi strălucitori te conduce pe trepte de piatră uzate către o pivniță subterană. Temperatura scade " +
                "pe măsură ce intri în labirintul de tuneluri de la Cricova, unde milioane de sticle se odihnesc în întuneric. 'Vinul este în sângele nostru,' " +
                "explică el, oferindu-ți un pahar de vin roșu adânc de Rară Neagră, 'Este legătura noastră cu strămoșii și pământul.'")
                .title("Crame Subterane")
                .context("Cricova")
                .sceneType("vineyard")
                .addChoice("Participă la culesul viilor", 7)
                .addChoice("Întoarce-te la ospăț", 5)
                .build();
        
        // Node 7 - Harvest
        MoldovaStoryNode harvestNode = new MoldovaStoryNode.Builder(7, "Te alături unui grup de localnici care se îndreaptă spre dealurile acoperite de viță de vie. Aerul dimineții este răcoros, dar soarele promite o zi călduroasă. " +
                "'Recolta este un moment de bucurie și de muncă intensă,' explică o femeie în vârstă în timp ce îți arată cum să tai ciorchinii de struguri și să-i așezi cu grijă în coș. " +
                "'Fiecare strugure conține esența soarelui și a pământului nostru.'")
                .title("Culesul Viilor")
                .context("Dealurile viticole")
                .sceneType("vineyard")
                .addChoice("Întreabă despre ritualuri de recoltă", 8)
                .addChoice("Ajută la transportul strugurilor", 9)
                .build();
        
        // Node 8 - Ritual
        MoldovaStoryNode ritualNode = new MoldovaStoryNode.Builder(8, "Când întrebi despre ritualurile culesului, bătrâna te conduce spre marginea viei unde un grup mic s-a adunat în jurul unui butuc de viță mai bătrân decât celelalte. " +
                "'Înainte de a începe culesul, cerem binecuvântarea zânelor viilor,' explică ea, în timp ce o fată tânără îmbrăcată în alb plasează pâine, " +
                "sare, și un mic vas cu vin la baza butucului. 'Se spune că dacă zânele sunt mulțumite, vinul va fi dulce și abundent.'")
                .title("Rugăciunea către Zânele Viilor")
                .context("Marginea viei")
                .sceneType("vineyard")
                .addChoice("Fă o ofrandă proprie", 10)
                .addChoice("Ajută la transportul strugurilor", 9)
                .build();
        
        // Node 9 - Bride vine
        MoldovaStoryNode brideVineNode = new MoldovaStoryNode.Builder(9, "După ore de cules, coșurile sunt pline cu struguri suculenți. Te oferi să ajuți la transportul lor până la căruțele care așteaptă la marginea dealului. " +
                "În timp ce mergi, observi că un butuc de viță a fost decorat cu panglici și flori. 'Aceasta este mireasa viei,' explică ghidul tău. " +
                "'Ultimul butuc de cules este întotdeauna special decorat și adus în sat cu cântece de sărbătoare pentru a marca sfârșitul recoltei.'")
                .title("Mireasa Viei")
                .context("Finalul culesului")
                .sceneType("vineyard")
                .addChoice("Fă o ofrandă pentru spiritele viei", 10)
                .addChoice("Întoarce-te în sat pentru sărbătoare", 5)
                .build();
        
        // Node 10 - Fairy gift
        MoldovaStoryNode fairyGiftNode = new MoldovaStoryNode.Builder(10, "Îți scoți o mică brățară făcută manual, pe care ai cumpărat-o mai devreme de la un meșteșugar din sat, și o așezi cu grijă pe pământ ca ofrandă. " +
                "Bătrâna zâmbește aprobator. Când te întorci mai târziu, brățara a dispărut, dar în locul ei găsești un mic ciob de ceramică veche cu motive dacice. " +
                "'Un dar de la spiritele acestui pământ,' șoptește bătrâna. 'Un semn bun pentru călătoria ta.'")
                .title("Darul Zânelor")
                .context("Marginea viei")
                .sceneType("vineyard")
                .addChoice("Vizitează Mănăstirea Putna", 13)
                .addChoice("Întoarce-te în sat", 5)
                .build();

        // Node 13 - Monastery
        MoldovaStoryNode monasteryNode = new MoldovaStoryNode.Builder(13, "Ghidul te conduce la Mănăstirea Putna, fondată de Ștefan cel Mare în 1466 după o victorie strălucitoare împotriva invadatorilor. " +
                "Zidurile masive mărginite de turnuri robuste înconjoară complexul mănăstirii. În interiorul bisericii, pereții sunt acoperiți cu fresce vii " +
                "care descriu scene biblice și victorii istorice. În centru se află mormântul lui Ștefan, decorat cu flori proaspete.")
                .title("Mănăstirea Putna")
                .context("La mormântul lui Ștefan cel Mare")
                .sceneType("monastery")
                .interactionType("historical_test")
                .interactive(true)
                .build();
        
        // Add all nodes to the map
        storyNodes.put(0, rootNode);
        storyNodes.put(1, forestNode);
        storyNodes.put(2, danceNode);
        storyNodes.put(3, stefanNode);
        storyNodes.put(4, nightNode);
        storyNodes.put(5, foodNode);
        storyNodes.put(6, wineNode);
        storyNodes.put(7, harvestNode);
        storyNodes.put(8, ritualNode);
        storyNodes.put(9, brideVineNode);
        storyNodes.put(10, fairyGiftNode);
        storyNodes.put(13, monasteryNode);
        
        // Set the total number of nodes
        totalStoryNodes = storyNodes.size();
    }
    
    private void showScene(int index) {
        if (index < 0 || index >= totalStoryNodes) {
            // Invalid index, return to main menu or handle appropriately
            finish();
            return;
        }
        
        currentSceneIndex = index;
        MoldovaStoryNode currentNode = storyNodes.get(currentSceneIndex);
        if (currentNode == null) {
            Log.e(TAG, "Null node at index: " + currentSceneIndex);
            return;
        }
        
        // Animate scene transition if enabled
        animateSceneTransition();
        
        // Update UI for the new scene
        updateStoryScreen();
        updateSceneImage();
        updateProgressIndicator();
        
        // Only scroll to top if requested or for first scene
        if (shouldResetScroll || index == 0) {
            if (scrollView != null) {
                scrollView.post(() -> {
                    scrollView.smoothScrollTo(0, 0);
                });
            }
            // Reset the flag after using it
            shouldResetScroll = false;
        }
        
        // Play sound effect if enabled
        if (isSoundEnabled) {
            playSceneSound();
        }
        
        // If text-to-speech is enabled, read the story
        if (isSpeaking) {
            speakText(currentNode.getStoryText());
        }
    }
    
    private void updateProgressIndicator() {
        if (progressIndicator != null) {
            progressIndicator.setMax(totalStoryNodes);
            progressIndicator.setProgress(currentSceneIndex + 1);
        }
    }
    
    private void updateSceneImage() {
        if (storyImageView == null) return;
        
        MoldovaStoryNode currentNode = storyNodes.get(currentSceneIndex);
        if (currentNode == null) return;
        
        // Set the appropriate image based on the scene type or content
        switch (currentNode.getSceneType()) {
            case "landscape":
                storyImageView.setImageResource(R.drawable.moldova_landscape);
                break;
            case "village":
                storyImageView.setImageResource(R.drawable.moldova_village);
                break;
            case "castle":
                storyImageView.setImageResource(R.drawable.soroca_fortress);
                break;
            case "monastery":
                storyImageView.setImageResource(R.drawable.moldova_monastery);
                break;
            case "battle":
                storyImageView.setImageResource(R.drawable.stephen_battle);
                break;
            case "vineyard":
                storyImageView.setImageResource(R.drawable.moldova_vineyard);
                break;
            default:
                // Default to Moldova landscape if no specific image is defined
                storyImageView.setImageResource(R.drawable.moldova_landscape);
                break;
        }
        
        // Ensure image is visible
        storyImageView.setVisibility(View.VISIBLE);
    }
    
    private void hideAllInteractiveElements() {
        // Hide interactive containers
        if (choiceContainer != null) {
            choiceContainer.setVisibility(View.GONE);
        }
        if (interactiveCardView != null) {
            interactiveCardView.setVisibility(View.GONE);
        }
        if (feedbackCard != null) {
            feedbackCard.setVisibility(View.GONE);
        }
    }
    
    private void setupChoiceButtons(MoldovaStoryNode node) {
        // Hide all interactive layouts initially
        if (feedbackCard != null) feedbackCard.setVisibility(View.GONE);
        if (bettingCard != null) bettingCard.setVisibility(View.GONE);
        
        // Set appropriate interactive elements based on node type
        List<MoldovaStoryNode.Choice> choices = node.getChoices();
        if (choices != null && !choices.isEmpty()) {
            // Make sure choice container is visible
            if (choiceContainer != null) {
                choiceContainer.setVisibility(View.VISIBLE);
            }
            
            // Reset button visibility
            if (choiceButton1 != null) choiceButton1.setVisibility(View.GONE);
            if (choiceButton2 != null) choiceButton2.setVisibility(View.GONE);
            if (choiceButton3 != null) choiceButton3.setVisibility(View.GONE);
            
            if (choices.size() >= 1 && choiceButton1 != null) {
                choiceButton1.setVisibility(View.VISIBLE);
                choiceButton1.setText(choices.get(0).getText());
                choiceButton1.setOnClickListener(v -> {
                    int nextIndex = choices.get(0).getNextSceneIndex();
                    if (nextIndex >= 0 && storyNodes.containsKey(nextIndex)) {
                        currentSceneIndex = nextIndex;
                        shouldResetScroll = false; // Don't reset scroll on choice navigation
                        showScene(currentSceneIndex);
                    } else {
                        Log.e(TAG, "Invalid next scene index: " + nextIndex);
                    }
                });
            }
            
            if (choices.size() >= 2 && choiceButton2 != null) {
                choiceButton2.setVisibility(View.VISIBLE);
                choiceButton2.setText(choices.get(1).getText());
                choiceButton2.setOnClickListener(v -> {
                    int nextIndex = choices.get(1).getNextSceneIndex();
                    if (nextIndex >= 0 && storyNodes.containsKey(nextIndex)) {
                        currentSceneIndex = nextIndex;
                        shouldResetScroll = false; // Don't reset scroll on choice navigation
                        showScene(currentSceneIndex);
                    } else {
                        Log.e(TAG, "Invalid next scene index: " + nextIndex);
                    }
                });
            }
            
            if (choices.size() >= 3 && choiceButton3 != null) {
                choiceButton3.setVisibility(View.VISIBLE);
                choiceButton3.setText(choices.get(2).getText());
                choiceButton3.setOnClickListener(v -> {
                    int nextIndex = choices.get(2).getNextSceneIndex();
                    if (nextIndex >= 0 && storyNodes.containsKey(nextIndex)) {
                        currentSceneIndex = nextIndex;
                        shouldResetScroll = false; // Don't reset scroll on choice navigation
                        showScene(currentSceneIndex);
                    } else {
                        Log.e(TAG, "Invalid next scene index: " + nextIndex);
                    }
                });
            }
        }
    }
    
    private void showFeedback(String message) {
        feedbackCard.setVisibility(View.VISIBLE);
        feedbackText.setText(message);
        feedbackCard.startAnimation(fadeInAnimation);
    }
    
    private void showHistoricalTest() {
        interactiveCardView.setVisibility(View.VISIBLE);
        testTitle.setText("Test Istoric");
        testDesc.setText("Alege obiectul care a aparținut lui Ștefan cel Mare:");
        
        // Simplified version that doesn't rely on objectImage and objectText IDs
        // We'll just use the card views directly without finding internal views
        
        objectCard1.setVisibility(View.VISIBLE);
        objectCard2.setVisibility(View.VISIBLE);
        objectCard3.setVisibility(View.VISIBLE);
        
        interactiveCardView.startAnimation(fadeInAnimation);
    }
    
    private void selectHistoricalTest(int choice) {
        String message;
        switch(choice) {
            case 1:
                message = "Corect! Sabia ceremonială a lui Ștefan cel Mare este păstrată la Muzeul Topkapi din Istanbul. Primești 50 de puncte bonus!";
                artifactPieces++;
                pointsManager.addPoints(this, "moldova", 50);
                break;
            default:
                message = "Incorect! Acest obiect nu este autentic din vremea lui Ștefan cel Mare.";
                break;
        }
        
        showFeedback(message);
        interactiveCardView.setVisibility(View.GONE);
        
        handler.postDelayed(() -> {
            feedbackCard.setVisibility(View.GONE);
            currentSceneIndex++;
            showScene(currentSceneIndex);
        }, 3000);
    }

    private void updateStoryScreen() {
        if (currentSceneIndex < 0 || currentSceneIndex >= totalStoryNodes) {
            return;
        }

        MoldovaStoryNode currentNode = storyNodes.get(currentSceneIndex);
        if (currentNode == null) {
            currentSceneIndex = 0; // Default to first node if current is invalid
            currentNode = storyNodes.get(currentSceneIndex);
            if (currentNode == null) return; // Safety check
        }

        // Update UI with current node data
        storyText.setText(currentNode.getStoryText());
        storyTitle.setText(currentNode.getTitle());
        storyContext.setText(currentNode.getContext());

        // Update progress indicator
        int progress = (int) (((float) currentSceneIndex / (float) totalStoryNodes) * 100);
        progressIndicator.setProgress(progress);

        // Reset choice container visibility
        choiceContainer.setVisibility(View.GONE);
        buttonContainer.setVisibility(View.VISIBLE);
        
        // Hide continue/next buttons initially
        nextButton.setVisibility(View.VISIBLE);
        
        // Handle interactive/choice nodes
        if (currentNode.getChoices() != null && !currentNode.getChoices().isEmpty()) {
            // Update choice buttons
            setupChoiceButtons(currentNode);
        } else {
            // For non-choice nodes, show the continue button
            nextButton.setVisibility(View.VISIBLE);
            choiceContainer.setVisibility(View.GONE);
        }
        
        // Handle interactive elements
        if (currentNode.isInteractive()) {
            switch (currentNode.getInteractionType()) {
                case "historical_test":
                    // Show interactive elements for historical test
                    showHistoricalTest();
                    break;
                // Add other interaction types as needed
            }
        } else {
            // Hide interactive elements when not needed
            hideInteractiveElements();
        }
        
        // Check for end of story
        if (currentSceneIndex == totalStoryNodes - 1) {
            nextButton.setText("Finalizare");
        } else {
            nextButton.setText("Continuă");
        }
        
        // Log for debugging
        Log.d("MoldovaStory", "Updated to node: " + currentSceneIndex + " - " + currentNode.getTitle());
    }

    private void hideInteractiveElements() {
        // Hide all interactive UI elements
        feedbackCard.setVisibility(View.GONE);
        bettingCard.setVisibility(View.GONE);
        interactiveCardView.setVisibility(View.GONE);
    }

    /**
     * Scrolls the story content to ensure the latest content is visible
     * Only use this method when needing to show content at the bottom
     * such as after user interaction or when displaying feedback
     */
    private void scrollToBottom() {
        // Scroll to the bottom of the content to show the newest additions
        scrollView.post(() -> {
            scrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
            
            // Additional post with delay to ensure scrolling completes
            scrollView.postDelayed(() -> {
                scrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
            }, 100);
        });
    }

    private void playSceneSound() {
        try {
            if (soundEffect != null) {
                soundEffect.release();
            }
            
            // Use thunder sound which is already in the resources instead of page_turn
            soundEffect = MediaPlayer.create(this, R.raw.thunder);
            if (soundEffect != null) {
                soundEffect.setVolume(0.5f, 0.5f);
                soundEffect.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing scene sound", e);
        }
    }

    private void speakText(String text) {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
} 