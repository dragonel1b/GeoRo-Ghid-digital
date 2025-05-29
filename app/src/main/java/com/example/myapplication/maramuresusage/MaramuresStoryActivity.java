package com.example.myapplication.maramuresusage;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.Maramures;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.databinding.ActivityMaramuresStoryBinding;
import com.example.myapplication.model.MaramuresStoryNode;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaramuresStoryActivity extends AppCompatActivity {
    private int currentNodeIndex = 0;
    private int score = 0;
    private List<Integer> visitedNodes = new ArrayList<>();
    private Map<Integer, MaramuresStoryNode> storyNodes = new HashMap<>();
    private int totalNodes = 0;
    private PointsManager pointsManager;
    private ActivityMaramuresStoryBinding binding;

    private MediaPlayer backgroundMusic;
    private MediaPlayer soundEffectPlayer;
    private boolean isMusicPlaying = false;

    // State Keys for saving instance state
    private static final String KEY_CURRENT_NODE = "current_node";
    private static final String KEY_SCORE = "score";
    private static final String KEY_VISITED_NODES = "visited_nodes";
    private static final String REGION = "maramures";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMaramuresStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Initialize UI components
        initializeViews();

        // Initialize story nodes
        initializeStoryNodes();
        totalNodes = storyNodes.size();

        // Restore state if available
        if (savedInstanceState != null) {
            currentNodeIndex = savedInstanceState.getInt(KEY_CURRENT_NODE, 0);
            score = savedInstanceState.getInt(KEY_SCORE, 0);
            visitedNodes = savedInstanceState.getIntegerArrayList(KEY_VISITED_NODES);
        }

        // Set up Media Players
        setupMediaPlayers();

        // Display the first node
        displayCurrentNode();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_NODE, currentNodeIndex);
        outState.putInt(KEY_SCORE, score);
        outState.putIntegerArrayList(KEY_VISITED_NODES, new ArrayList<>(visitedNodes));
    }

    private void initializeViews() {
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        binding.btnChoice1.setOnClickListener(v -> handleChoice(0));
        binding.btnChoice2.setOnClickListener(v -> handleChoice(1));
        binding.btnChoice3.setOnClickListener(v -> handleChoice(2));
        binding.btnSubmitAnswer.setOnClickListener(v -> handleQuizSubmission());

        binding.btnBack.setOnClickListener(v -> showExitConfirmationDialog());
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Ieșire")
                .setMessage("Ești sigur că vrei să ieși din poveste? Progresul tău va fi salvat.")
                .setPositiveButton("Da", (dialog, which) -> {
                    // Save points earned
                    pointsManager.addPoints(this, REGION, score);
                    
                    // Release media players
                    releaseMediaPlayers();
                    finish();
                })
                .setNegativeButton("Nu", null)
                .show();
    }

    private void setupMediaPlayers() {
        try {
            backgroundMusic = MediaPlayer.create(this, R.raw.maramures_background);
            if (backgroundMusic != null) {
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.5f, 0.5f);
                // Start background music
                startBackgroundMusic();
            }
        } catch (Exception e) {
            // Handle exception if the music file doesn't exist
            e.printStackTrace();
        }
    }

    private void startBackgroundMusic() {
        if (backgroundMusic != null && !isMusicPlaying) {
            backgroundMusic.start();
            isMusicPlaying = true;
        }
    }

    private void pauseBackgroundMusic() {
        if (backgroundMusic != null && isMusicPlaying) {
            backgroundMusic.pause();
            isMusicPlaying = false;
        }
    }

    private void playSoundEffect(int resourceId) {
        if (resourceId == 0) return;
        
        try {
            if (soundEffectPlayer != null) {
                soundEffectPlayer.release();
            }
            
            soundEffectPlayer = MediaPlayer.create(this, resourceId);
            if (soundEffectPlayer != null) {
                soundEffectPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    soundEffectPlayer = null;
                });
                soundEffectPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseMediaPlayers() {
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
        
        if (soundEffectPlayer != null) {
            soundEffectPlayer.release();
            soundEffectPlayer = null;
        }
    }

    private void initializeStoryNodes() {
        // Node 0: Introduction
        storyNodes.put(0, new MaramuresStoryNode.Builder(0, 
                "Bine ai venit în Maramureș, tărâmul unde tradiția, natura și istoria se împletesc într-un mod unic. " +
                "Te invit să pornești într-o călătorie prin această regiune fascinantă a României, unde vei descoperi biserici de lemn impresionante, " +
                "porți sculptate, obiceiuri străvechi și oameni primitori. Maramureșul este locul unde tradiția este încă vie și unde natura își etalează cele mai frumoase peisaje.")
                .title("Începutul călătoriei în Maramureș")
                .imageResource(R.drawable.maramures)
                .build());

        // Node 1: First choice
        storyNodes.put(1, new MaramuresStoryNode.Builder(1, 
                "Te afli la intrarea în Maramureș. În fața ta se deschid mai multe drumuri, fiecare cu propriile sale povești și descoperiri. " +
                "Poți alege să vizitezi bisericile de lemn, monumente UNESCO de o valoare inestimabilă, " +
                "sau poți merge spre Săpânța pentru a vedea celebrul Cimitir Vesel, un loc unic în lume unde moartea este privită cu seninătate și umor.")
                .title("La răscruce de drumuri")
                .context("Dimineața, pe drumul spre inima Maramureșului")
                .imageResource(R.drawable.drum_ras)
                .nodeType(MaramuresStoryNode.NodeType.CHOICE)
                .choices(new String[]{
                        "Vizitează bisericile de lemn",
                        "Mergi spre Cimitirul Vesel"
                })
                .nextNodes(new int[]{2, 3})
                .build());

        // Node 2: Wooden Churches path
        storyNodes.put(2, new MaramuresStoryNode.Builder(2,
                "Ai ales să vizitezi bisericile de lemn din Maramureș. Prima oprire este la Mănăstirea Bârsana, " +
                "unde se află una dintre cele mai frumoase biserici din zonă. " +
                "Construită în stil maramureșean tradițional, biserica impresionează prin măiestria sculpturilor în lemn și prin înălțimea turnului său. " +
                "Aici, timpul pare să stea pe loc, iar liniștea te învăluie în timp ce admiri arta meșterilor locali.")
                .title("Bisericile de lemn din Maramureș")
                .context("La Mănăstirea Bârsana")
                .imageResource(R.drawable.maramures_wooden_church)
                .pointsReward(10)
                .nextNodes(new int[]{5})
                .build());

        // Node 3: Merry Cemetery path
        storyNodes.put(3, new MaramuresStoryNode.Builder(3,
                "Ai ales să mergi la Săpânța, unde se află celebrul Cimitir Vesel. " +
                "Acest loc unic în lume te întâmpină cu cruci colorate pe care sunt sculptate scene din viața celor plecați, " +
                "însoțite de epitafuri pline de umor care descriu viața și ocupația defunctului. " +
                "Creat de meșterul Stan Ioan Pătraș, Cimitirul Vesel transformă tristețea într-o celebrare a vieții, reflectând filozofia localnicilor despre moarte ca o trecere firească.")
                .title("Cimitirul Vesel din Săpânța")
                .context("În satul Săpânța")
                .imageResource(R.drawable.cimitir_vesel)
                .pointsReward(10)
                .nextNodes(new int[]{6})
                .build());

        // Node 4: Quiz about Wooden Churches
        storyNodes.put(4, new MaramuresStoryNode.Builder(4,
                "Călăuzitorul tău local îți povestește despre istoria regiunii și îți oferă o provocare: " +
                "Care este cea mai înaltă construcție de lemn din Europa care se află în Maramureș?")
                .title("Test de cunoștințe: Bisericile din Maramureș")
                .nodeType(MaramuresStoryNode.NodeType.QUIZ)
                .correctAnswer("Biserica din Șurdești")
                .feedback("Corect! Biserica de lemn din Șurdești are un turn de 72 de metri înălțime, fiind cea mai înaltă construcție de lemn din Europa.")
                .pointsReward(15)
                .nextNodes(new int[]{7})
                .build());

        // Node 5: More about wooden churches
        storyNodes.put(5, new MaramuresStoryNode.Builder(5,
                "Te plimbi printre bisericile de lemn ale Maramureșului, oprindu-te la Desești, Ieud și Budești. " +
                "Aceste capodopere arhitecturale, incluse în patrimoniul UNESCO, impresionează prin tehnica de construcție fără cuie, doar îmbinări de lemn. " +
                "Picturile interioare, realizate în stil bizantin, spun povești biblice adaptate la realitățile locale. " +
                "Ghidul îți explică simbolistica porților maramureșene, cu motive solare și frânghia împletită ce reprezintă infinitul.")
                .title("Comori de lemn")
                .context("Pe drumul bisericilor UNESCO")
                .imageResource(R.drawable.drum_bis)
                .pointsReward(10)
                .nextNodes(new int[]{4})
                .build());

        // Node 6: Quiz about Merry Cemetery
        storyNodes.put(6, new MaramuresStoryNode.Builder(6,
                "În timp ce explorezi Cimitirul Vesel, ghidul local îți arată o cruce specială și te întreabă: " +
                "Cine a fost creatorul Cimitirului Vesel din Săpânța?")
                .title("Test de cunoștințe: Cimitirul Vesel")
                .nodeType(MaramuresStoryNode.NodeType.QUIZ)
                .correctAnswer("Stan Ioan Pătraș")
                .feedback("Corect! Stan Ioan Pătraș a fost meșterul popular care a creat Cimitirul Vesel în 1935, sculptând și pictând crucile cu scene din viața celor trecuți în neființă, continuând tradiția dacică a morții vesele.")
                .pointsReward(15)
                .nextNodes(new int[]{8})
                .build());

        // Node 7: Traditional crafts
        storyNodes.put(7, new MaramuresStoryNode.Builder(7,
                "Călătoria ta continuă spre un atelier tradițional de țesut. Aici, meșterițele locale îți arată cum se realizează celebrele covoare și ștergare maramureșene, " +
                "cu motive geometrice și florale transmise din generație în generație. Poți vedea războaiele de țesut vechi de peste 100 de ani, încă funcționale. " +
                "Lâna este vopsită cu coloranți naturali extrași din plante, iar fiecare model are propria semnificație în cultura locală.")
                .title("Meșteșuguri tradiționale")
                .context("Într-un atelier de țesut din Maramureș")
                .imageResource(R.drawable.maramures_crafts)
                .pointsReward(10)
                .nextNodes(new int[]{9})
                .build());

        // Node 8: Traditional cuisine
        storyNodes.put(8, new MaramuresStoryNode.Builder(8,
                "După vizita la Cimitirul Vesel, ești invitat la o masă tradițională maramureșeană. Gazdele îți servesc balmoș, un preparat cremios din mămăligă cu brânză de burduf, " +
                "carne afumată de porc, slănină cu ceapă roșie și pâine de casă coaptă în cuptor. Totul este stropit cu horincă de prune, băutura tradițională a regiunii, " +
                "distilată după metode străvechi. În timpul mesei, asculți povești despre viața de zi cu zi a localnicilor și obiceiurile lor.")
                .title("Bucătăria tradițională")
                .context("La o masă maramureșeană")
                .imageResource(R.drawable.maramures_cook)
                .pointsReward(10)
                .nextNodes(new int[]{9})
                .build());

        // Node 9: Mocănița Challenge
        storyNodes.put(9, new MaramuresStoryNode.Builder(9,
                "Ai ajuns la Vișeu de Sus, unde se află celebra Mocăniță, trenul cu aburi care străbate Valea Vaserului. " +
                "Înainte de a urca în tren, ghidul îți testează cunoștințele: " +
                "Ce transporta inițial Mocănița pe Valea Vaserului?")
                .title("Test de cunoștințe: Mocănița")
                .nodeType(MaramuresStoryNode.NodeType.QUIZ)
                .correctAnswer("Lemn")
                .feedback("Corect! Mocănița a fost construită în perioada austro-ungară pentru a transporta lemnul exploatat din pădurile dense ale Văii Vaserului, fiind una dintre ultimele căi ferate forestiere cu aburi funcționale din Europa.")
                .pointsReward(15)
                .nextNodes(new int[]{10})
                .build());

        // Node 10: Mocanita Journey
        storyNodes.put(10, new MaramuresStoryNode.Builder(10,
                "Te-ai urcat în vagoanele rustice ale Mocăniței și ai pornit într-o călătorie fascinantă prin Valea Vaserului. " +
                "Locomotiva cu aburi pufăie ritmic în timp ce trenul se strecoară printre munți, urmând cursul apei. " +
                "Pe traseu, admiri peisaje sălbatice de o frumusețe răpitoare, păduri dense de conifere și stânci abrupte. " +
                "Trenul oprește la 'Paltin', unde poți gusta produse locale la un picnic în mijlocul naturii. Un cioban local îți povestește despre viața la stână și tradițiile păstorești.")
                .title("Călătorie cu Mocănița")
                .context("Pe Valea Vaserului")
                .imageResource(R.drawable.vaser)
                .pointsReward(10)
                .nextNodes(new int[]{11})
                .build());

        // Node 11: Folk traditions
        storyNodes.put(11, new MaramuresStoryNode.Builder(11,
                "În ultima zi a călătoriei tale prin Maramureș, ai șansa de a participa la o șezătoare tradițională într-un sat de munte. " +
                "Localnicii îmbrăcați în costume populare cântă doine și balade vechi de sute de ani, acompaniați de viori și ceteră. " +
                "Dansurile populare, precum 'Învârtita' și 'Bărbătescul', umplu curtea cu energie și veselie. " +
                "Un bătrân satului îți povestește legende locale despre haiduci, comori ascunse și spirite ale pădurii, transmise din generație în generație.")
                .title("Șezătoare tradițională")
                .context("Într-un sat maramureșean")
                .imageResource(R.drawable.sat_maramuresean)
                .pointsReward(10)
                .nextNodes(new int[]{12})
                .build());

        // Node 12: Final quiz and conclusion
        storyNodes.put(12, new MaramuresStoryNode.Builder(12,
                "Înainte de a-ți încheia călătoria prin Maramureș, localnicii îți pun o ultimă întrebare: " +
                "Care este cel mai important simbol arhitectural al gospodăriilor tradiționale maramureșene?")
                .title("Test final: Simbolurile Maramureșului")
                .nodeType(MaramuresStoryNode.NodeType.QUIZ)
                .correctAnswer("Poarta maramureșeană")
                .feedback("Corect! Poarta maramureșeană, înaltă și bogat ornamentată, este simbolul identitar al regiunii. Decorată cu simboluri solare, frânghii împletite și motive geometrice, aceasta reprezintă trecerea dintre spațiul public și cel privat, dar și statutul social al familiei.")
                .pointsReward(20)
                .nextNodes(new int[]{13})
                .build());

        // Node 13: Conclusion
        storyNodes.put(13, new MaramuresStoryNode.Builder(13,
                "Călătoria ta prin Maramureș se apropie de sfârșit, dar amintirile vor dăinui. Ai descoperit un tărâm unde tradițiile sunt vii, " +
                "unde oamenii trăiesc în armonie cu natura și unde trecutul și prezentul coexistă în mod firesc. " +
                "Portul popular, meșteșugurile, arhitectura în lemn, bucătăria tradițională și ospitalitatea localnicilor te-au făcut să înțelegi " +
                "de ce Maramureșul este considerat una dintre cele mai autentice regiuni ale României și ale Europei. " +
                "Pleci îmbogățit spiritual, purtând cu tine un fragment din sufletul acestui ținut de poveste.")
                .title("Sfârșit de călătorie")
                .context("Cu amintiri de neuitat")
                .imageResource(R.drawable.memories)
                .pointsReward(15)
                .build());
    }

    private void displayCurrentNode() {
        MaramuresStoryNode currentNode = storyNodes.get(currentNodeIndex);
        if (currentNode == null) {
            Toast.makeText(this, "Eroare: Nod invalid în poveste.", Toast.LENGTH_SHORT).show();
            showEndingDialog();
            return;
        }

        if (!visitedNodes.contains(currentNodeIndex)) {
            visitedNodes.add(currentNodeIndex);
            score += currentNode.getPointsReward();
        }

        updateProgressIndicator();

        playSoundEffect(currentNode.getSoundResourceId());

        animateTextChange(binding.tvStoryText, currentNode.getStoryText());
        binding.tvContext.setText(currentNode.getContext());
        binding.tvNodeTitle.setText(currentNode.getTitle());

        if (currentNode.getImageResourceId() != 0) {
            binding.ivStory.setVisibility(View.VISIBLE);
            binding.ivStory.setImageResource(currentNode.getImageResourceId());
            binding.imageCard.setVisibility(View.VISIBLE);
        } else {
            binding.ivStory.setVisibility(View.GONE);
            binding.imageCard.setVisibility(View.GONE);
        }

        if (currentNode.isQuizNode()) {
            setupQuizNode(currentNode);
        } else if (currentNode.isChoiceNode()) {
            setupChoiceNode(currentNode);
        } else {
            // For simple story nodes, show a continue button
            binding.quizInputLayout.setVisibility(View.GONE);
            binding.btnSubmitAnswer.setVisibility(View.GONE);
            binding.feedbackCard.setVisibility(View.GONE);
            
            binding.btnChoice1.setVisibility(View.GONE);
            binding.btnChoice2.setVisibility(View.GONE);
            binding.btnChoice3.setVisibility(View.GONE);
            
            // Show continue button if there's a next node
            if (currentNode.getNextNodes() != null && currentNode.getNextNodes().length > 0) {
                binding.btnChoice1.setText("Continuă");
                binding.btnChoice1.setVisibility(View.VISIBLE);
                binding.btnChoice1.setOnClickListener(v -> moveToNextNode(currentNode.getNextNodes()[0]));
            } else if (currentNodeIndex == 13) { // Final node
                binding.btnChoice1.setText("Încheie aventura");
                binding.btnChoice1.setVisibility(View.VISIBLE);
                binding.btnChoice1.setOnClickListener(v -> showEndingDialog());
            }
        }

        binding.scrollView.smoothScrollTo(0, 0);
    }

    private void updateProgressIndicator() {
        int maxProgress = totalNodes > 0 ? totalNodes : 1;
        int currentProgress = (int) (((float) visitedNodes.size() / maxProgress) * 100);
        
        binding.progressIndicator.setProgressCompat(currentProgress, true);
    }

    private void setupQuizNode(MaramuresStoryNode node) {
        binding.btnChoice1.setVisibility(View.GONE);
        binding.btnChoice2.setVisibility(View.GONE);
        binding.btnChoice3.setVisibility(View.GONE);
        
        binding.quizInputLayout.setVisibility(View.VISIBLE);
        binding.btnSubmitAnswer.setVisibility(View.VISIBLE);
        
        binding.feedbackCard.setVisibility(View.GONE);
        binding.etQuizAnswer.setText("");
        binding.etQuizAnswer.setEnabled(true);
        binding.btnSubmitAnswer.setText("Verifică");
        binding.btnSubmitAnswer.setEnabled(true);
        binding.btnSubmitAnswer.setOnClickListener(v -> handleQuizSubmission());
    }

    private void setupChoiceNode(MaramuresStoryNode node) {
        binding.quizInputLayout.setVisibility(View.GONE);
        binding.btnSubmitAnswer.setVisibility(View.GONE);
        binding.feedbackCard.setVisibility(View.GONE);
        
        String[] choices = node.getChoices();
        
        MaterialButton[] buttons = {binding.btnChoice1, binding.btnChoice2, binding.btnChoice3};
        
        for (int i = 0; i < buttons.length; i++) {
            if (i < choices.length && choices[i] != null && !choices[i].isEmpty()) {
                buttons[i].setText(choices[i]);
                buttons[i].setVisibility(View.VISIBLE);
                int choiceIndex = i; // Need a final variable for the lambda
                buttons[i].setOnClickListener(v -> handleChoice(choiceIndex));
            } else {
                buttons[i].setVisibility(View.GONE);
            }
        }
    }

    private void handleChoice(int choiceIndex) {
        MaramuresStoryNode currentNode = storyNodes.get(currentNodeIndex);
        if (currentNode == null) return;
        
        if (choiceIndex < currentNode.getChoices().length) {
            int nextNodeId = currentNode.getNextNodeForChoice(choiceIndex);
            moveToNextNode(nextNodeId);
        }
    }

    private void handleQuizSubmission() {
        String userAnswer = binding.etQuizAnswer.getText().toString().trim();
        MaramuresStoryNode currentNode = storyNodes.get(currentNodeIndex);

        if (currentNode == null || !currentNode.isQuizNode()) return;
        
        if (userAnswer.isEmpty()) {
            Toast.makeText(this, "Te rog introdu un răspuns", Toast.LENGTH_SHORT).show();
            return;
        }
        
        boolean isCorrect = currentNode.isCorrectAnswer(userAnswer);
        String feedback = currentNode.getFeedback();
        
        binding.feedbackCard.setVisibility(View.VISIBLE);
        binding.tvFeedback.setText(feedback != null ? feedback : "Corect! Continuă călătoria.");
        
        if (isCorrect) {
            score += currentNode.getPointsReward();
            if (!visitedNodes.contains(-currentNodeIndex)) {
                visitedNodes.add(-currentNodeIndex);
            }
            
            binding.btnSubmitAnswer.setText("Continuă");
            binding.btnSubmitAnswer.setOnClickListener(v -> {
                if (currentNode.getNextNodes() != null && currentNode.getNextNodes().length > 0) {
                    moveToNextNode(currentNode.getNextNodes()[0]);
                } else {
                    moveToNextNode(currentNodeIndex + 1);
                }
            });
        } else {
            binding.tvFeedback.setText("Răspuns incorect. Încearcă din nou!");
            binding.btnSubmitAnswer.setText("Încearcă din nou");
            binding.btnSubmitAnswer.setOnClickListener(v -> {
                binding.etQuizAnswer.setText("");
                binding.etQuizAnswer.setEnabled(true);
                binding.feedbackCard.setVisibility(View.GONE);
                binding.btnSubmitAnswer.setText("Verifică");
                binding.btnSubmitAnswer.setOnClickListener(v2 -> handleQuizSubmission());
            });
        }

        binding.etQuizAnswer.setEnabled(false);
    }

    private void moveToNextNode(int nextNodeId) {
        currentNodeIndex = nextNodeId;
        displayCurrentNode();
    }

    private void animateTextChange(final TextView textView, final String newText) {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(300);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                textView.setText(newText);
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(300);
                textView.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        textView.startAnimation(fadeOut);
    }

    private void showEndingDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Călătoria s-a încheiat")
                .setMessage("Felicitări! Ai finalizat povestea Maramureșului și ai câștigat " + score + " puncte!\n\nAi descoperit frumusețea bisericilor de lemn UNESCO, Cimitirul Vesel, ai călătorit cu Mocănița și ai învățat despre tradițiile și obiceiurile acestei regiuni unice.")
                .setPositiveButton("Înapoi la meniu", (dialog, which) -> {
                    // Save points earned
                    pointsManager.addPoints(this, REGION, score);
                    
                    // Release media players
                    releaseMediaPlayers();
                    
                    // Finish activity
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseBackgroundMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayers();
    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }
} 