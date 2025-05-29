package com.example.myapplication.banatusage;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.model.BanatStoryNode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.widget.ProgressBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.List;
import android.app.AlertDialog;
import android.text.TextUtils;

public class BanatStoryActivity extends AppCompatActivity {
    // UI components
    private ProgressBar progressIndicator;
    private TextView progressText;
    private MaterialCardView headerCard;
    private TextView titleTextView, storyTextView, contextTextView;
    private ImageView storyImageView;
    private MaterialCardView feedbackCard, bettingCard;
    private TextView feedbackText;
    private MaterialButton backButton, soundButton, infoButton, nextButton;
    private MaterialButton redButton, blackButton;
    private ImageView option1Image, option2Image, option3Image, option4Image;
    private TextView option1Text, option2Text, option3Text, option4Text;
    private TextView pointsText;
    
    // Game state
    private int currentSceneIndex = 0;
    private int totalPoints = 0;
    private boolean soundEnabled = true;
    private PointsManager pointsManager;
    private MediaPlayer backgroundMusic;
    private Map<Integer, BanatStoryNode> storyNodes;
    private TextView storyText;
    private TextView storyTitle;
    private TextView storyContext;
    private MaterialButton continueButton;
    private MaterialButton storyButton;
    private MaterialButton exitButton;
    private ImageView headerBackButton;
    private ImageView soundToggleButton;
    private MaterialCardView interactiveCardView;
    private TextView interactiveTitle;
    private TextView interactiveDesc;
    private MaterialCardView objectCard1;
    private MaterialCardView objectCard2;
    private MaterialCardView objectCard3;
    private TextInputEditText betAmountInput;
    private ImageView animationView;
    private Random random;
    private boolean isStoryMode = false;
    private int artifactPieces = 0;
    private boolean hasMetGhost = false;
    private boolean hasFoundTreasure = false;
    private boolean hasCompletedQuest = false;
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;
    private int currentPoints = 0;
    private int totalStoryNodes = 0;
    private MediaPlayer soundEffect;
    private boolean isSoundEnabled = true;
    private Animation fadeInAnimation;
    private Animation specialAnimation;
    private Handler handler = new Handler();
    
    // Constanta pentru puncte bonus
    private static final int BONUS_POINTS = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banat_story);

        pointsManager = PointsManager.getInstance(this);
        // Inițializăm punctele actuale
        currentPoints = pointsManager.getPoints(this);
        
        initializeViews();
        initializeAudio();
        initializeAnimations();
        initializeStory();
        initializeTextToSpeech();
        setupClickListeners();
        showScene();
    }

    private void initializeViews() {
        // Initialize all UI elements
        progressIndicator = findViewById(R.id.progressIndicator);
        
        // Header elements
        headerCard = findViewById(R.id.headerLayout);
        headerBackButton = findViewById(R.id.headerBackButton);
        soundToggleButton = findViewById(R.id.soundToggleButton);
        pointsText = findViewById(R.id.pointsText);
        
        // Story display elements
        storyTitle = findViewById(R.id.storyTitle);
        storyText = findViewById(R.id.storyText);
        storyContext = findViewById(R.id.storyContext);
        storyImageView = findViewById(R.id.storyImageView);
        
        // Cards
        feedbackCard = findViewById(R.id.feedbackCard);
        feedbackText = findViewById(R.id.feedbackText);
        bettingCard = findViewById(R.id.bettingCard);
        interactiveCardView = findViewById(R.id.interactiveCardView);
        interactiveTitle = findViewById(R.id.vampireTestTitle);
        interactiveDesc = findViewById(R.id.vampireTestDesc);
        
        // Interactive elements
        objectCard1 = findViewById(R.id.objectCard1);
        objectCard2 = findViewById(R.id.objectCard2);
        objectCard3 = findViewById(R.id.objectCard3);
        
        // Buttons
        redButton = findViewById(R.id.redButton);
        blackButton = findViewById(R.id.blackButton);
        nextButton = findViewById(R.id.nextButton);
        continueButton = findViewById(R.id.continueButton);
        storyButton = findViewById(R.id.storyButton);
        exitButton = findViewById(R.id.exitButton);
        
        // Animation view
        animationView = findViewById(R.id.batAnimation);
        
        // Set the Banat theme colors
        headerCard.setCardBackgroundColor(getResources().getColor(R.color.banat_primary));
        progressIndicator.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.banat_accent)));
        progressIndicator.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.banat_primary_light)));

        // Apply theme colors to buttons
        if (redButton != null) redButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.banat_primary)));
        if (blackButton != null) blackButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.banat_primary)));
        if (nextButton != null) nextButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.banat_primary)));
        if (continueButton != null) continueButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.banat_primary)));
        
        // Initialize PointsManager
        random = new Random();
        
        // Get current points
        currentPoints = pointsManager.getPoints(this);
        updatePointsDisplay();
        
        // Make sure all elements have proper initial visibility
        if (storyText != null) storyText.setVisibility(View.VISIBLE);
        if (storyTitle != null) storyTitle.setVisibility(View.VISIBLE);
        if (storyContext != null) storyContext.setVisibility(View.VISIBLE);
        if (nextButton != null) nextButton.setVisibility(View.VISIBLE);
        if (continueButton != null) continueButton.setVisibility(View.GONE);
        if (exitButton != null) exitButton.setVisibility(View.VISIBLE);
        if (storyButton != null) storyButton.setVisibility(View.VISIBLE);
        feedbackCard.setVisibility(View.GONE);
        bettingCard.setVisibility(View.GONE);
        if (interactiveCardView != null) interactiveCardView.setVisibility(View.GONE);
        if (animationView != null) animationView.setVisibility(View.GONE);
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
        specialAnimation = AnimationUtils.loadAnimation(this, R.anim.fly_animation);
        
        fadeInAnimation.setDuration(1000);
        specialAnimation.setDuration(1500);
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
    
    private void setupClickListeners() {
        // Back button click listener
        if (headerBackButton != null) {
            headerBackButton.setOnClickListener(v -> {
                onBackPressed();
            });
        }

        // Sound toggle button click listener
        if (soundToggleButton != null) {
            soundToggleButton.setOnClickListener(v -> {
                isSoundEnabled = !isSoundEnabled;
                updateSoundButtonState();
                if (!isSoundEnabled) {
                    stopBackgroundMusic();
                } else {
                    playBackgroundMusic();
                }
            });
        }

        // Next button click listener
        if (nextButton != null) {
            nextButton.setOnClickListener(v -> {
                moveToNextScene();
            });
        }

        // Continue button click listener  
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                moveToNextScene();
            });
        }

        // Story button click listener
        if (storyButton != null) {
            storyButton.setOnClickListener(v -> {
                showStoryDialog();
            });
        }

        // Exit button click listener
        if (exitButton != null) {
            exitButton.setOnClickListener(v -> {
                showExitConfirmationDialog();
            });
        }

        // Choice buttons click listeners
        if (redButton != null) {
            redButton.setOnClickListener(v -> {
                handleChoice(0);
            });
        }

        if (blackButton != null) {
            blackButton.setOnClickListener(v -> {
                handleChoice(1);
            });
        }

        // Interactive object cards click listeners
        if (objectCard1 != null) {
            objectCard1.setOnClickListener(v -> {
                handleObjectSelection(0);
            });
        }

        if (objectCard2 != null) {
            objectCard2.setOnClickListener(v -> {
                handleObjectSelection(1);
            });
        }

        if (objectCard3 != null) {
            objectCard3.setOnClickListener(v -> {
                handleObjectSelection(2);
            });
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
        if (isSpeaking && textToSpeech != null) {
            textToSpeech.stop();
            isSpeaking = false;
            if (storyButton != null) {
                storyButton.setText("Citește povestea");
            }
        }
        
        // Play animation
        if (animationView != null) {
            animationView.setVisibility(View.VISIBLE);
            animationView.startAnimation(specialAnimation);
        }
        
        // Fade out current content with animation
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Update the scene after fade out
                showScene();
                updateProgressIndicator();
                
                // Fade in new content
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(500);
                
                if (storyText != null) {
                    storyText.startAnimation(fadeIn);
                }
                if (storyTitle != null) {
                    storyTitle.startAnimation(fadeIn);
                }
                if (storyImageView != null) {
                    storyImageView.startAnimation(fadeIn);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        if (storyText != null) {
            storyText.startAnimation(fadeOut);
        }
        if (storyTitle != null) {
            storyTitle.startAnimation(fadeOut);
        }
        if (storyImageView != null) {
            storyImageView.startAnimation(fadeOut);
        }
        
        // Hide all interactive elements during transition
        hideAllInteractiveElements();
        
        // Delay finishing the animation
        handler.postDelayed(() -> {
            if (animationView != null) {
                animationView.clearAnimation();
                animationView.setVisibility(View.GONE);
            }
        }, 1500);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        // Actualizăm punctele la revenirea în activitate
        currentPoints = pointsManager.getPoints(this);
        updatePointsDisplay();
    }
    
    private void initializeStory() {
        storyNodes = new HashMap<Integer, BanatStoryNode>();
        
        // Node 0: Introducere
        storyNodes.put(0, new BanatStoryNode(
            "Banatul - Tărâm multicultural",
            "Banatul, regiune cu o bogată istorie și diversitate culturală, se întinde între râurile Mureș, Tisa și Dunăre, și Carpații de Vest. Cunoscută pentru multiculturalismul său, această regiune reprezintă o punte între civilizații, cultură și tradiții care au coexistat armonios de-a lungul secolelor.",
            "Pornește într-o călătorie virtuală prin Banat, descoperind bogățiile acestei regiuni unice.",
            "banat_panorama",
            "Continuă călătoria", 
            "Află despre istorie",
            choice -> choice == 0 ? 1 : 2));
        
        // Node 1: Geografie
        storyNodes.put(1, new BanatStoryNode(
            "Geografia Banatului",
            "Din punct de vedere geografic, Banatul prezintă un relief divers, de la munții Semenic și Aninei în est, la dealurile domoale și câmpiile fertile din vest. Regiunea este străbătută de râuri importante precum Timiș, Bega și Cerna. Parcul Național Cheile Nerei-Beușnița și Parcul Național Semenic-Cheile Carașului adăpostesc unele dintre cele mai spectaculoase peisaje naturale din România.",
            "Diversitatea geografică a Banatului a influențat profund dezvoltarea economică și culturală a regiunii.",
            "w",
            "Continuă",
            null,
            choice -> 2));
        
        // Node 2: Istorie
        storyNodes.put(2, new BanatStoryNode(
            "Istoria Banatului",
            "Regiunea Banatului a fost locuită încă din antichitate, fiind parte din Dacia și mai târziu a Imperiului Roman. În Evul Mediu, a fost disputată între Regatul Ungariei și Imperiul Otoman. Sub dominație habsburgică (1718-1918), Banatul a cunoscut o perioadă de colonizare intensă cu diverse populații europene (germani, maghiari, sârbi, slovaci, bulgari), ceea ce a dus la caracterul multicultural distinct al regiunii.",
            "Istoria tumultoasă a Banatului a creat un mozaic cultural unic în Europa.",
            "banat_history",
            "Continuă", 
            "Testează-ți cunoștințele",
            choice -> choice == 0 ? 3 : 10));
        
        // Node 3: Multiculturalism
        storyNodes.put(3, new BanatStoryNode(
            "Banatul multicultural",
            "Banatul este renumit pentru diversitatea sa etnică și culturală. Români, germani (șvabi), maghiari, sârbi, bulgari, slovaci și alte comunități etnice au trăit împreună secole de-a rândul, influențându-se reciproc. Această conviețuire pașnică a dat naștere unui model de toleranță și respect reciproc cunoscut ca 'spiritul bănățean', cu influențe vizibile în arhitectură, gastronomie, muzică și obiceiuri.",
            "Această diversitate culturală reprezintă una dintre cele mai valoroase moșteniri ale Banatului.",
            "banat_multicultural",
            "Continuă",
            null,
            choice -> 4));
        
        // Node 4: Arhitectură
        storyNodes.put(4, new BanatStoryNode(
            "Arhitectura Banatului",
            "Arhitectura Banatului reflectă influențele multiple ale civilizațiilor care s-au succedat în regiune. Timișoara, 'capitala' Banatului, este un adevărat muzeu în aer liber cu clădiri în stil baroc, art nouveau și secession. Palatele din centrul orașului, Domul Romano-Catolic, dar și casele în stil șvăbesc din satele bănățene reprezintă mărturii ale unei bogate moșteniri arhitecturale. De asemenea, fortărețele medievale precum Cetatea Timișoara sau vestigiile romane de la Tibiscum completează acest patrimoniu divers.",
            "Influențele central-europene se regăsesc în arhitectura urbană și rurală din întreaga regiune.",
            "banat_architecture",
            "Continuă",
            null,
            choice -> 5));
        
        // Node 5: Tradiții
        storyNodes.put(5, new BanatStoryNode(
            "Tradiții și obiceiuri",
            "Tradițiile și obiceiurile bănățene reprezintă o îmbinare de elemente românești cu influențe germane, maghiare, sârbești și de alte origini. Sărbătorile populare precum 'Ruga bănățeană', 'Kirchweih'-ul șvăbesc sau carnavalurile sunt evenimente care reunesc întreaga comunitate. Portul popular bănățean se distinge prin colorit și ornamente bogate, iar dansurile tradiționale precum 'Învârtita' sau 'De doi' reflectă vitalitatea și caracterul distinct al folclorului local.",
            "Tradițiile bănățene sunt păstrate cu mândrie și transmise din generație în generație.",
            "banat_traditions",
            "Continuă",
            null,
            choice -> 6));
        
        // Node 6: Gastronomie
        storyNodes.put(6, new BanatStoryNode(
            "Gastronomia Banatului",
            "Bucătăria bănățeană este un amestec fascinant de influențe românești, germane, sârbești și maghiare. Printre preparatele specifice se numără: papricașul bănățean, zupa de găină cu tăiței de casă, 'strudel'-ul (ștrudelul) cu diferite umpluturi, 'măduțul' (un fel de gulaș), plăcintele diverse și celebrii 'cozonacii' (rotați) bănățeni. Vinurile de Recaș și Buziaș sunt apreciate pentru calitatea lor deosebită, iar țuica de Banat are un renume binemeritat.",
            "Gastronomia bănățeană reflectă abundența regiunii și influențele culturale diverse care au modelat-o.",
            "banat_gastronomy",
            "Continuă",
            null,
            choice -> 7));
        
        // Node 7: Personalități
        storyNodes.put(7, new BanatStoryNode(
            "Personalități ilustre",
            "Banatul a dat României și lumii personalități remarcabile precum: Johnny Weissmuller - actor și campion olimpic, născut în Timișoara; Ana Blandiana - poetă de renume; Corneliu Baba - unul dintre cei mai importanți pictori români; Traian Vuia - pionier al aviației mondiale; Ioan Holender - cel mai longeviv director al Operei de Stat din Viena; Herta Müller - scriitoare laureată a Premiului Nobel pentru Literatură.",
            "Contribuțiile acestor personalități au depășit granițele regionale, aducând Banatul pe harta culturală mondială.",
            "banat_personalities",
            "Continuă",
            null,
            choice -> 8));
        
        // Node 8: Orașe importante
        storyNodes.put(8, new BanatStoryNode(
            "Orașe importante",
            "Banatul românesc cuprinde orașe cu o bogată istorie și cultură: Timișoara - 'capitala' Banatului, primul oraș european iluminat electric și locul de unde a izbucnit Revoluția din 1989; Reșița - important centru siderurgic cu o tradiție industrială de peste 250 de ani; Lugoj - important centru cultural, natal al tenorului Traian Grozăvescu; Caransebeș - străvechi centru istoric și cultural; Băile Herculane - stațiune balneară cu o istorie de peste 2000 de ani.",
            "Fiecare oraș bănățean își păstrează individualitatea, contribuind la identitatea regională.",
            "banat_cities",
            "Continuă",
            null,
            choice -> 9));
        
        // Node 9: Turism cu tranziție spre Misterul Banatului
        storyNodes.put(9, new BanatStoryNode(
            "Turismul în Banat",
            "Banatul oferă numeroase atracții turistice: centrele istorice ale Timișoarei și Lugojului, stațiunea Băile Herculane, Parcul Național Cheile Nerei-Beușnița cu cascada Bigăr, lacul Ochiul Beiului și Cheile Carașului, Muntele Semenic pentru sporturi de iarnă, Gaura Ponicovei și alte peșteri spectaculoase, Lacul Surduc pentru activități acvatice, rezervația de zimbri Armeniș și Biserica de lemn Poieni.",
            "Dar dincolo de aceste obiective turistice cunoscute, Banatul ascunde și locuri pline de mister...",
            "banat_tourism",
            "Explorează misterele Banatului", 
            "Finalizează călătoria normală",
            choice -> choice == 0 ? 16 : 15));
        
        // Node 10: Quiz despre Banat - Întrebarea 1
        storyNodes.put(10, new BanatStoryNode(
            "Testul cunoștințelor - Întrebarea 1",
            "Care este 'capitala' istorică și culturală a Banatului?",
            "Alege răspunsul corect:",
            "banat_quiz",
            "Timișoara", 
            "Reșița",
            choice -> choice == 0 ? 11 : 13));
        
        // Node 11: Răspuns corect 1
        storyNodes.put(11, new BanatStoryNode(
            "Răspuns corect!",
            "Exact! Timișoara este considerată capitala istorică și culturală a Banatului. Orașul este unul dintre cele mai importante centre economice și culturale din România și locul unde a izbucnit Revoluția din Decembrie 1989.",
            "Să trecem la următoarea întrebare.",
            "timisoara",
            "Continuă",
            null,
            choice -> 12));
        
        // Node 12: Quiz - Întrebarea 2
        storyNodes.put(12, new BanatStoryNode(
            "Testul cunoștințelor - Întrebarea 2",
            "Pentru ce eveniment cultural european a fost desemnată Timișoara în 2023?",
            "Alege răspunsul corect:",
            "banat_quiz",
            "Capitala Europeană a Culturii", 
            "Capitala Europeană a Tineretului",
            choice -> choice == 0 ? 14 : 13));
        
        // Node 13: Răspuns greșit
        storyNodes.put(13, new BanatStoryNode(
            "Răspuns greșit",
            "Din păcate, acesta nu este răspunsul corect. Ai ocazia să afli mai multe despre Banat din restul călătoriei noastre virtuale.",
            "Să continuăm cu povestea Banatului.",
            "banat_incorrect",
            "Continuă",
            null,
            choice -> 3));
        
        // Node 14: Răspuns corect 2
        storyNodes.put(14, new BanatStoryNode(
            "Răspuns corect!",
            "Exact! Timișoara a fost desemnată Capitala Europeană a Culturii pentru anul 2023, fiind al doilea oraș din România care primește acest titlu, după Sibiu în 2007.",
            "Felicitări pentru cunoștințele tale despre Banat!",
            "banat_culture",
            "Continuă cu povestea",
            null,
            choice -> 3));
        
        // Node 15: Concluzie standard
        storyNodes.put(15, new BanatStoryNode(
            "O regiune de neprețuit",
            "Banatul reprezintă una dintre cele mai valoroase comori culturale și istorice ale României. Caracterul său multicultural, bogăția tradițiilor, frumusețea peisajelor naturale și patrimoniul său arhitectural îl transformă într-o regiune unică nu doar în România, ci și în Europa.",
            "SFÂRȘIT",
            "banat_end",
            null,
            null,
            null));
            
        // NOI NODURI - AVENTURA SECRETĂ
        
        // Node 16: Începutul aventurii speciale
        storyNodes.put(16, new BanatStoryNode(
            "Misterul Comorii din Banat",
            "Există o legendă în Banat despre o comoară ascunsă de sute de ani. Se spune că în timpul ocupației otomane, un pașă a ascuns un tezaur de aur undeva în regiunea Banatului. Mulți au încercat să o găsească, dar nimeni nu a reușit. Vrei să încerci și tu?",
            "O aventură plină de mistere te așteaptă!",
            "banat_treasure_map",
            "Accept provocarea", 
            "Mai bine nu",
            choice -> choice == 0 ? 17 : 15));
            
        // Node 17: Alegerea locației
        storyNodes.put(17, new BanatStoryNode(
            "Pe urmele comorii",
            "Potrivit legendei, comoara ar putea fi în una din aceste locații. Fiecare ascunde indicii importante, dar și posibile capcane. Unde vrei să începi căutarea?",
            "Alege cu înțelepciune!",
            "banat_mystery",
            "Peșterile din Cheile Nerei", 
            "Cetatea Timișoarei",
            choice -> choice == 0 ? 18 : 19));
            
        // Node 18: Calea Peșterilor
        storyNodes.put(18, new BanatStoryNode(
            "Peșterile misterioase",
            "Ai ales să explorezi peșterile din Cheile Nerei. După ore de mers prin tuneluri întunecate, găsești un pasaj secret ce duce spre o cameră ascunsă. În mijloc se află o cutie veche și un pergament cu un mesaj în limba turcă veche.",
            "Trebuie să decizi cum procedezi...",
            "banat_caves",
            "Deschide cutia", 
            "Descifrează pergamentul",
            choice -> choice == 0 ? 20 : 21));
            
        // Node 19: Calea Cetății
        storyNodes.put(19, new BanatStoryNode(
            "Cetatea Timișoarei",
            "Cetatea Timișoarei ascunde multe secrete în zidurile sale vechi. Explorând pivnițele fortăreței, descoperi un tunel secret. La capătul lui găsești o cameră mică cu un cufăr vechi și un portret al unui pașă otoman.",
            "Ce faci mai departe?",
            "banat_fortress",
            "Examinează cufărul", 
            "Studiezi portretul",
            choice -> choice == 0 ? 22 : 23));
            
        // Node 20: Capcana din cutie
        storyNodes.put(20, new BanatStoryNode(
            "O capcană!",
            "În momentul în care deschizi cutia, un mecanism vechi se declanșează și camera începe să se umple cu apă! Trebuie să acționezi rapid pentru a scăpa!",
            "Ce alegi să faci?",
            "banat_trap",
            "Caută o ieșire secretă", 
            "Încearcă să blochezi sursa apei",
            choice -> choice == 0 ? 24 : 25));
            
        // Node 21: Indiciul din pergament
        storyNodes.put(21, new BanatStoryNode(
            "Mesajul secret",
            "Reușești să descifrezi parțial mesajul. Acesta vorbește despre 'comoara ascunsă sub stâncă undeva unde apa din munte întâlnește câmpia'. Pergamentul conține și o hartă parțială.",
            "Care este următorul tău pas?",
            "banat_parchment",
            "Urmează indicațiile din hartă", 
            "Caută alte indicii în peșteră",
            choice -> choice == 0 ? 26 : 27));
        
        // Nodurile 22-29: Continuarea aventurii
        
        // Node 22: Cufărul din cetate
        storyNodes.put(22, new BanatStoryNode(
            "Secretul cufărului",
            "Cufărul este încuiat cu un lacăt complicat. Examinându-l atent, observi că are un mecanism bazat pe simboluri astronomice. Lângă el găsești un medalion cu simboluri similare.",
            "Cum procedezi?",
            "banat_chest",
            "Folosești medalionul pentru a deschide cufărul", 
            "Forțezi deschiderea cufărului",
            choice -> choice == 0 ? 28 : 29));
            
        // Node 23: Portretul pașei
        storyNodes.put(23, new BanatStoryNode(
            "Portretul revelator",
            "Studiind atent portretul, observi că pașa poartă un medalion special la gât. În colțul tabloului este și o inscripție care menționează 'comoara de la izvoarele Timișului'.",
            "Ce faci cu această informație?",
            "banat_portrait",
            "Cauți medalionul în cameră", 
            "Mergi la izvoarele Timișului",
            choice -> choice == 0 ? 30 : 31));
            
        // Node 24-31: Diverse ramificații ale căutării comorii
            
        // Node 32: Finala - Găsirea comorii
        storyNodes.put(32, new BanatStoryNode(
            "Comoara descoperită!",
            "După numeroase provocări și capcane, ai reușit! În fața ta se află legendara comoară a Banatului: o colecție impresionantă de monede de aur, bijuterii și artefacte istorice neprețuite. Descoperirea ta va schimba pentru totdeauna cunoștințele despre istoria regiunii!",
            "Felicitări pentru curajul și inteligența ta!",
            "banat_treasure_found",
            "Donează comoara unui muzeu", 
            "Păstrează comoara pentru tine",
            choice -> choice == 0 ? 33 : 34));
            
        // Node 33: Finalul altruist
        storyNodes.put(33, new BanatStoryNode(
            "Un gest nobil",
            "Ai decis să donezi întreaga comoară Muzeului Banatului. Istoricii și arheologii sunt uimiți de descoperirea ta! Artefactele oferă informații neprețuite despre istoria regiunii. Pentru contribuția ta extraordinară, primești o medalie de onoare și recunoștință eternă.",
            "Numele tău va rămâne în istoria Banatului pentru totdeauna!",
            "banat_museum_donation",
            "Încheie aventura",
            null,
            choice -> 35));
            
        // Node 34: Finalul egoist
        storyNodes.put(34, new BanatStoryNode(
            "Bogăția are prețul ei",
            "Ai decis să păstrezi comoara pentru tine, dar curând realizezi că obiectele sunt prea valoroase și ușor de recunoscut pentru a fi vândute. În plus, legenda spune că cei care fură comoara sunt blestemați. Noaptea începi să auzi șoapte în limba turcă și pași în jurul casei tale...",
            "Poate ar fi mai bine să reconsideri decizia?",
            "banat_curse",
            "Donează comoara în cele din urmă", 
            "Ignoră semnele",
            choice -> choice == 0 ? 33 : 36));
            
        // Node 35: Celebrarea reușitei
        storyNodes.put(35, new BanatStoryNode(
            "Sărbătoarea descoperirii",
            "Municipalitatea organizează o festivitate în cinstea ta. Prieteni și oameni din toate colțurile Banatului vin să te felicite. Un bătrân misterios îți mulțumește pentru că ai adus la lumină adevărata comoară a Banatului - nu aurul, ci istoria și spiritul locului.",
            "Ți se acordă titlul de Cetățean de Onoare al Banatului și 1000 de puncte bonus!",
            "banat_celebration",
            "Mulțumesc pentru această aventură!",
            null,
            choice -> 37));
            
        // Node 36: Consecințele blestemului
        storyNodes.put(36, new BanatStoryNode(
            "Blestemul se împlinește",
            "Ignorând avertismentele, continui să păstrezi comoara. Într-o noapte, te trezești și găsești toate obiectele dispărute. În locul lor, un mesaj scris în turcă veche. Un localnic îți traduce: 'Comoara Banatului nu aparține unui singur om, ci tuturor oamenilor săi'.",
            "Ai pierdut comoara, dar ai învățat o lecție valoroasă despre lăcomie.",
            "banat_empty_chest",
            "Închide aventura",
            null,
            choice -> 37));
            
        // Node 37: Final comun - Recompensa
        storyNodes.put(37, new BanatStoryNode(
            "Aventura Banatului",
            "Călătoria ta prin misterele Banatului s-a încheiat, dar amintirile și lecțiile învățate vor rămâne pentru totdeauna. Această regiune fascinantă continuă să-și păstreze farmecul și misterul, așteptând alți exploratori curajoși ca tine.",
            "Pentru finalizarea acestei aventuri speciale primești 500 de puncte bonus!",
            "banat_end",
            "Încheie călătoria",
            null,
            null));
        
        totalStoryNodes = storyNodes.size();
        updateProgressIndicator();
    }
    
    private void showScene() {
        if (currentSceneIndex >= totalStoryNodes) {
            return;
        }

        BanatStoryNode currentNode = storyNodes.get(currentSceneIndex);
        if (currentNode == null) {
            // Handle the case where there's no node for the current index
            Toast.makeText(this, "Eroare: Nod de poveste lipsă la indexul " + currentSceneIndex, Toast.LENGTH_SHORT).show();
            // Try to recover by moving to the next scene
            currentSceneIndex++;
            if (currentSceneIndex < totalStoryNodes) {
                showScene();
            } else {
                finish();
            }
            return;
        }
        
        // Update text content
        if (storyTitle != null) {
            storyTitle.setText(currentNode.getTitle());
        }
        
        if (storyText != null) {
            storyText.setText(currentNode.getText());
        }
        
        // Update image if available
        if (storyImageView != null && !TextUtils.isEmpty(currentNode.getImageResourceName())) {
            int resourceId = getResources().getIdentifier(
                    currentNode.getImageResourceName(), 
                    "drawable", 
                    getPackageName());
            
            if (resourceId != 0) {
                storyImageView.setImageResource(resourceId);
            } else {
                // Default image if resource not found
                storyImageView.setImageResource(R.drawable.banat_bg_simple);
            }
        }
        
        // Show/hide choices based on scene type
        if (!TextUtils.isEmpty(currentNode.getChoiceText1()) && !TextUtils.isEmpty(currentNode.getChoiceText2())) {
            // This is a choice scene
            if (redButton != null && !TextUtils.isEmpty(currentNode.getChoiceText1())) {
                redButton.setText(currentNode.getChoiceText1());
                redButton.setVisibility(View.VISIBLE);
            }
            
            if (blackButton != null && !TextUtils.isEmpty(currentNode.getChoiceText2())) {
                blackButton.setText(currentNode.getChoiceText2());
                blackButton.setVisibility(View.VISIBLE);
            }
            
            // Hide continue button during choices
            if (continueButton != null) {
                continueButton.setVisibility(View.GONE);
            }
            
        } else if (currentNode.isInteractiveScene()) {
            // This is an interactive object scene
            // Hide continue button until interaction
            if (continueButton != null) {
                continueButton.setVisibility(View.GONE);
            }
            
        } else {
            // This is a standard scene
            if (continueButton != null) {
                continueButton.setVisibility(View.VISIBLE);
            }
        }
        
        // Always show navigation buttons
        if (nextButton != null) {
            nextButton.setVisibility(currentSceneIndex < totalStoryNodes - 1 ? View.VISIBLE : View.GONE);
        }
    }
    
    /**
     * Updates the progress indicator to show the current position in the story
     */
    private void updateProgressIndicator() {
        if (progressIndicator != null && totalStoryNodes > 0) {
            int progress = (int) (((float) currentSceneIndex / (totalStoryNodes - 1)) * 100);
            progressIndicator.setProgress(progress);
            
            if (progressText != null) {
                progressText.setText(String.format("%d%%", progress));
            }
        }
    }
    
    private void updateSceneImage() {
        if (currentSceneIndex >= totalStoryNodes) {
            return;
        }
        
        BanatStoryNode currentNode = storyNodes.get(currentSceneIndex);
        if (currentNode != null && currentNode.getImageResourceName() != null) {
            String imageName = currentNode.getImageResourceName();
            int resourceId = getResources().getIdentifier(
                imageName, "drawable", getPackageName());
            
            if (resourceId != 0) {
                storyImageView.setImageResource(resourceId);
                storyImageView.setVisibility(View.VISIBLE);
            } else {
                // Fallback image if the specific one is not found
                storyImageView.setImageResource(R.drawable.banat_bg_simple);
                storyImageView.setVisibility(View.VISIBLE);
            }
        } else {
            storyImageView.setVisibility(View.INVISIBLE);
        }
    }
    
    private void hideAllInteractiveElements() {
        feedbackCard.setVisibility(View.GONE);
        bettingCard.setVisibility(View.GONE);
        interactiveCardView.setVisibility(View.GONE);
    }
    
    private void setupChoiceButtons(BanatStoryNode node) {
        if (node.getChoiceText1() != null && node.getChoiceText2() != null) {
            // This is a choice node
            redButton.setText(node.getChoiceText1());
            blackButton.setText(node.getChoiceText2());
            
            redButton.setVisibility(View.VISIBLE);
            blackButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.GONE);
            continueButton.setVisibility(View.GONE);
            
            redButton.setOnClickListener(v -> {
                int nextIndex = node.getNextSceneFunction().apply(0);
                currentSceneIndex = nextIndex;
                animateSceneTransition();
            });
            
            blackButton.setOnClickListener(v -> {
                int nextIndex = node.getNextSceneFunction().apply(1);
                currentSceneIndex = nextIndex;
                animateSceneTransition();
            });
        } else {
            // This is a narrative node
            redButton.setVisibility(View.GONE);
            blackButton.setVisibility(View.GONE);
            
            if (currentSceneIndex == totalStoryNodes - 1) {
                // Last scene
                nextButton.setVisibility(View.GONE);
                continueButton.setVisibility(View.VISIBLE);
                continueButton.setText("Finalizează");
                continueButton.setOnClickListener(v -> {
                    pointsManager.addPoints(this, "banat", BONUS_POINTS);
                    finish();
                });
            } else {
                nextButton.setVisibility(View.VISIBLE);
                continueButton.setVisibility(View.GONE);
            }
        }
    }
    
    private void showFeedback(String message) {
        feedbackText.setText(message);
        feedbackCard.setVisibility(View.VISIBLE);
        
        // Auto-hide feedback after delay
        handler.postDelayed(() -> {
            feedbackCard.setVisibility(View.GONE);
        }, 3000);
    }
    
    private void showInteractiveTest() {
        interactiveTitle.setText("Test de curaj");
        interactiveDesc.setText("Alege unul dintre următoarele obiecte pentru a te ajuta în expediția ta:");
        
        TextView item1Text = objectCard1.findViewById(R.id.objectItemText);
        TextView item2Text = objectCard2.findViewById(R.id.objectItemText);
        TextView item3Text = objectCard3.findViewById(R.id.objectItemText);
        
        item1Text.setText("Lampa veche");
        item2Text.setText("Harta misterioasă");
        item3Text.setText("Amuleta protectoare");
        
        ImageView item1Image = objectCard1.findViewById(R.id.objectItemImage);
        ImageView item2Image = objectCard2.findViewById(R.id.objectItemImage);
        ImageView item3Image = objectCard3.findViewById(R.id.objectItemImage);
        
        item1Image.setImageResource(R.drawable.lamp_icon);
        item2Image.setImageResource(R.drawable.map_icon);
        item3Image.setImageResource(R.drawable.amulet_icon);
        
        interactiveCardView.setVisibility(View.VISIBLE);
    }
    
    private void selectInteractiveOption(int choice) {
        String message;
        int points;
        
        switch (choice) {
            case 1:
                message = "Ai ales lampa veche! Lumina ei îți va arăta căile ascunse.";
                points = 15;
                break;
            case 2:
                message = "Ai ales harta misterioasă! Deși greu de descifrat, te va ghida către comori.";
                points = 20;
                break;
            case 3:
                message = "Ai ales amuleta protectoare! Localnicii cred că te va feri de spiritele rele.";
                points = 25;
                break;
            default:
                return;
        }
        
        pointsManager.addPoints(this, "banat", points);
        interactiveCardView.setVisibility(View.GONE);
        showFeedback(message + " (+" + points + " puncte)");
        
        // Continue story after a delay
        handler.postDelayed(() -> {
            currentSceneIndex++;
            animateSceneTransition();
        }, 3000);
    }

    private void updatePoints(int points) {
        pointsManager.addPoints(this, "banat", points);
        currentPoints = pointsManager.getPoints(this);
        updatePointsDisplay();
        
        // Afișăm un toast pentru a notifica utilizatorul
        Toast.makeText(this, String.format("Ai câștigat %d puncte!", points), Toast.LENGTH_SHORT).show();
    }
    
    private void updatePointsDisplay() {
        if (pointsText != null) {
            pointsText.setText("" + currentPoints);
            // Forțăm un refresh al layoutului pentru a aplica schimbările imediat
            pointsText.invalidate();
        }
    }

    // Methods for scene navigation and interaction
    private void moveToNextScene() {
        currentSceneIndex++;
        if (currentSceneIndex < totalStoryNodes) {
            animateSceneTransition();
        } else {
            showCompletionDialog();
        }
    }

    private void handleChoice(int choiceIndex) {
        BanatStoryNode currentNode = storyNodes.get(currentSceneIndex);
        if (currentNode == null) {
            // Handle the case where there's no node for the current index
            Toast.makeText(this, "Eroare: Nod de poveste lipsă", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Determine next scene based on choice
        if (choiceIndex == 0) {
            if (currentNode.getChoiceNextScene1() != -1) {
                currentSceneIndex = currentNode.getChoiceNextScene1();
            } else {
                currentSceneIndex++;
            }
        } else {
            if (currentNode.getChoiceNextScene2() != -1) {
                currentSceneIndex = currentNode.getChoiceNextScene2();
            } else {
                currentSceneIndex++;
            }
        }
        
        animateSceneTransition();
    }

    private void handleObjectSelection(int objectIndex) {
        // Handle interactive object selection
        // You can add specific behavior based on the selected object
        Toast.makeText(this, "You selected object " + (objectIndex + 1), Toast.LENGTH_SHORT).show();
        
        // After interaction, enable the continue button
        if (continueButton != null) {
            continueButton.setVisibility(View.VISIBLE);
        }
    }

    private void showStoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Povestea Banatului");
        builder.setMessage("Banatul este o regiune istorică situată în sud-vestul României, cu o moștenire culturală bogată și diversă. " +
                "Influențată de mai multe culturi de-a lungul secolelor, regiunea este cunoscută pentru ospitalitatea sa, gastronomia sa unică și peisajele pitorești.");
        builder.setPositiveButton("Închide", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ieșire");
        builder.setMessage("Ești sigur că vrei să părăsești povestea? Progresul actual nu va fi salvat.");
        builder.setPositiveButton("Da", (dialog, which) -> finish());
        builder.setNegativeButton("Nu", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showCompletionDialog() {
        // Check if we're at a treasure ending node
        if (currentSceneIndex == 35 || currentSceneIndex == 36 || currentSceneIndex == 37) {
            // Special treasure hunt ending
            int bonusPoints = 0;
            String title = "Aventură completă!";
            String message = "Felicitări! Ai terminat aventura specială a Banatului.";
            
            if (currentSceneIndex == 35) {
                // Generous ending - max points
                bonusPoints = 1000;
                title = "Erou al Banatului!";
                message = "Felicitări! Generozitatea ta a fost răsplătită. Ai primit 1000 de puncte bonus și titlul onorific!";
            } else if (currentSceneIndex == 36) {
                // Cursed ending - fewer points
                bonusPoints = 100;
                title = "Lecție învățată";
                message = "Ai pierdut comoara, dar ai câștigat înțelepciune. Primești 100 de puncte bonus.";
            } else {
                // Standard bonus
                bonusPoints = 500;
                message = "Felicitări pentru finalizarea aventurii Banatului! Primești 500 de puncte bonus!";
            }
            
            pointsManager.addPoints(this, "banat", bonusPoints);
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(title);
            builder.setMessage(message);
            
            // Create special effects for the treasure ending
            View customView = getLayoutInflater().inflate(R.layout.treasure_ending_layout, null);
            if (customView != null) {
                ImageView treasureImageView = customView.findViewById(R.id.treasureImage);
                TextView pointsTextView = customView.findViewById(R.id.pointsTextView);
                
                if (pointsTextView != null) {
                    pointsTextView.setText("+" + bonusPoints + " puncte");
                }
                
                // Animate the points
                if (treasureImageView != null) {
                    treasureImageView.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.treasure_shine));
                }
                
                builder.setView(customView);
            }
            
            builder.setPositiveButton("Înapoi la meniu", (dialog, which) -> finish());
            builder.show();
        } else {
            // Original standard ending for regular path
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Poveste completă!");
            builder.setMessage("Felicitări! Ai terminat povestea Banatului. Ai învățat despre cultura și istoria acestei regiuni fascinante.");
            pointsManager.addPoints(this, "banat", BONUS_POINTS);
            builder.setPositiveButton("Înapoi la meniu", (dialog, which) -> finish());
            builder.show();
        }
    }

    // Sound management methods
    private void updateSoundButtonState() {
        if (soundToggleButton != null) {
            soundToggleButton.setImageResource(isSoundEnabled ? 
                    R.drawable.ic_sound_on : R.drawable.ic_sound_off);
        }
    }

    private void playBackgroundMusic() {
        // Implementation for playing background music
        // This would typically use MediaPlayer
    }

    private void stopBackgroundMusic() {
        // Implementation for stopping background music
        // This would typically use MediaPlayer
    }
} 