package com.example.myapplication.transilvaniausage;

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
import com.example.myapplication.model.TransilvaniaStoryNode;
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

public class DraculaStoryActivity extends AppCompatActivity {
    private TextView storyText;
    private TextView storyTitle;
    private TextView storyContext;
    private ImageView storyImageView;
    private MaterialButton redButton;
    private MaterialButton blackButton;
    private MaterialButton nextButton;
    private MaterialButton continueButton;
    private MaterialButton storyButton;
    private MaterialButton exitButton;
    private ImageView headerBackButton;
    private ImageView soundToggleButton;
    private MaterialCardView feedbackCard;
    private MaterialCardView bettingCard;
    private MaterialCardView interactiveCardView;
    private TextView feedbackText;
    private TextView vampireTestTitle;
    private TextView vampireTestDesc;
    private MaterialCardView objectCard1;
    private MaterialCardView objectCard2;
    private MaterialCardView objectCard3;
    private TextInputEditText betAmountInput;
    private LinearProgressIndicator progressIndicator;
    private ImageView batAnimation;
    private PointsManager pointsManager;
    private int currentSceneIndex = 0;
    private Map<Integer, TransilvaniaStoryNode> storyNodes;
    private Random random;
    private boolean isStoryMode = false;
    private int artifactPieces = 0;
    private boolean hasMetDracula = false;
    private boolean hasFoundBlood = false;
    private boolean hasBeenBitten = false;
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;
    private int currentPoints = 0;
    private int totalStoryNodes = 0;
    private MediaPlayer backgroundMusic;
    private MediaPlayer soundEffect;
    private boolean isSoundEnabled = true;
    private Animation fadeInAnimation;
    private Animation batFlyAnimation;
    private Handler handler = new Handler();

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
        initializeAudio();
        initializeAnimations();
        initializeStory();
        initializeTextToSpeech();
        setupButtonListeners();
        showScene();
    }

    private void initializeViews() {
        storyText = findViewById(R.id.storyText);
        storyTitle = findViewById(R.id.storyTitle);
        storyContext = findViewById(R.id.storyContext);
        storyImageView = findViewById(R.id.storyImageView);
        redButton = findViewById(R.id.redButton);
        blackButton = findViewById(R.id.blackButton);
        nextButton = findViewById(R.id.nextButton);
        continueButton = findViewById(R.id.continueButton);
        storyButton = findViewById(R.id.storyButton);
        exitButton = findViewById(R.id.exitButton);
        headerBackButton = findViewById(R.id.headerBackButton);
        soundToggleButton = findViewById(R.id.soundToggleButton);
        feedbackCard = findViewById(R.id.feedbackCard);
        bettingCard = findViewById(R.id.bettingCard);
        interactiveCardView = findViewById(R.id.interactiveCardView);
        feedbackText = findViewById(R.id.feedbackText);
        vampireTestTitle = findViewById(R.id.vampireTestTitle);
        vampireTestDesc = findViewById(R.id.vampireTestDesc);
        objectCard1 = findViewById(R.id.objectCard1);
        objectCard2 = findViewById(R.id.objectCard2);
        objectCard3 = findViewById(R.id.objectCard3);
        betAmountInput = findViewById(R.id.betAmountInput);
        progressIndicator = findViewById(R.id.progressIndicator);
        batAnimation = findViewById(R.id.batAnimation);
        
        pointsManager = PointsManager.getInstance(this);
        random = new Random();
        
        // Get current points
        currentPoints = pointsManager.getPoints(this);
        
        // Make sure all elements have proper initial visibility
        if (storyText != null) storyText.setVisibility(View.VISIBLE);
        if (storyTitle != null) storyTitle.setVisibility(View.VISIBLE);
        if (storyContext != null) storyContext.setVisibility(View.VISIBLE);
        if (nextButton != null) nextButton.setVisibility(View.VISIBLE);
        if (continueButton != null) continueButton.setVisibility(View.GONE);
        if (exitButton != null) exitButton.setVisibility(View.VISIBLE);
        if (storyButton != null) storyButton.setVisibility(View.VISIBLE);
        if (feedbackCard != null) feedbackCard.setVisibility(View.GONE);
        if (bettingCard != null) bettingCard.setVisibility(View.GONE);
        if (interactiveCardView != null) interactiveCardView.setVisibility(View.GONE);
        if (batAnimation != null) batAnimation.setVisibility(View.GONE);
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
        batFlyAnimation = AnimationUtils.loadAnimation(this, R.anim.fly_animation);
        
        fadeInAnimation.setDuration(1000);
        batFlyAnimation.setDuration(1500);
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
                currentSceneIndex++;
                animateSceneTransition();
            });
        }
        
        // Add listener for continue button
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                currentSceneIndex++;
                animateSceneTransition();
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
        
        // Interactive element cards
        if (objectCard1 != null) {
            objectCard1.setOnClickListener(v -> selectVampireTest(1));
        }
        if (objectCard2 != null) {
            objectCard2.setOnClickListener(v -> selectVampireTest(2));
        }
        if (objectCard3 != null) {
            objectCard3.setOnClickListener(v -> selectVampireTest(3));
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
            if (storyButton != null) {
                storyButton.setText("Citește povestea");
            }
            isSpeaking = false;
        } else {
            StringBuilder textToRead = new StringBuilder();
            if (storyTitle != null && storyTitle.getVisibility() == View.VISIBLE) {
                textToRead.append(storyTitle.getText()).append(". ");
            }
            if (storyText != null) {
                textToRead.append(storyText.getText());
            }
            if (storyContext != null && storyContext.getVisibility() == View.VISIBLE) {
                textToRead.append(". ").append(storyContext.getText());
            }
            textToSpeech.speak(textToRead.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
            if (storyButton != null) {
                storyButton.setText("Oprește citirea");
            }
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
        
        // Play bat animation
        if (batAnimation != null) {
            batAnimation.setVisibility(View.VISIBLE);
            batAnimation.startAnimation(batFlyAnimation);
        }
        
        // Play sound effect if enabled
        if (isSoundEnabled && soundEffect != null) {
            try {
                soundEffect.start();
            } catch (Exception e) {
                // ignore if sound can't play
            }
        }
        
        // Fade out current content
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        if (storyText != null) storyText.startAnimation(fadeOut);
        if (storyTitle != null) storyTitle.startAnimation(fadeOut);
        if (storyContext != null) storyContext.startAnimation(fadeOut);
        
        // After animation, show new scene
        handler.postDelayed(() -> {
            showScene();
            if (batAnimation != null) {
                batAnimation.setVisibility(View.GONE);
            }
            
            // Fade in new content
            if (storyText != null) storyText.startAnimation(fadeInAnimation);
            if (storyTitle != null) storyTitle.startAnimation(fadeInAnimation);
            if (storyContext != null) storyContext.startAnimation(fadeInAnimation);
        }, 800);
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
        
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
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
    }

    private void initializeStory() {
        storyNodes = new HashMap<>();
        
        // Introduction scene
        storyNodes.put(0, new TransilvaniaStoryNode.Builder(0,
            "Transilvania, ținutul misterios din inima României, înconjurat de munți maiestuoși și păduri întunecate. " +
            "Ai ajuns în satul Bran, aflat la poalele Munților Carpați, unde ceața învăluie casele tradiționale și " +
            "localnicii vorbesc în șoaptă despre străvechile legende ale locului. Seara se lasă, iar luna plină se ridică " +
            "deasupra munților, aruncând o lumină palidă peste sat. Din depărtare, silueta Castelului Bran se profilează " +
            "amenințător pe cerul întunecat, iar un stol de lilieci zboară în jurul turnurilor sale.")
            .title("Bine ai venit în Transilvania!")
            .context("Transilvania este cunoscută în întreaga lume datorită legendei lui Dracula. Pregătește-te să descoperi " +
                    "secretele întunecate ascunse în inima acestui ținut.")
            .choices(new String[]{"Întreabă localnicii despre legende", "Mergi la castel", "Găsește un loc de cazare"})
            .nextNodes(new int[]{1, 2, 3})
            .build());

        // Node 1: Asking locals about legends
        storyNodes.put(1, new TransilvaniaStoryNode.Builder(1,
            "Te apropii de hanul satului unde câțiva localnici stau la o masă, vorbind în șoaptă. Când te văd intrând, " +
            "conversația încetează brusc, iar privirile lor te urmăresc cu suspiciune. După câteva momente tensionate, " +
            "un bătrân cu fața brăzdată de riduri adânci îți face semn să te așezi.\n\n" +
            "'Ești străin aici, nu-i așa?' întreabă el pe un ton grav. 'Se pare că nu știi despre ce noapte este astăzi. " +
            "Este noapte cu lună plină, iar legendele spun că în astfel de nopți, El se trezește la viață.'\n\n" +
            "Localnicii îți povestesc despre Contele Dracula, stăpânul nemuririi, care își are sălaș în Castelul Bran. " +
            "Spun că acesta a fost cândva un mare voievod, Vlad Țepeș, cunoscut pentru cruzimea sa, dar că un blestem " +
            "l-a transformat într-o creatură a nopții, însetată de sânge și incapabilă să moară.")
            .title("Legendele locului")
            .context("Localnicii par sinceri în temerile lor, dar oare cât adevăr există în aceste povești?")
            .choices(new String[]{"Întreabă despre intrarea în castel", "Ignoră superstițiile și găsește cazare", "Cumpără un crucifix pentru protecție"})
            .nextNodes(new int[]{4, 3, 5})
            .build());

        // Node 2: Going to the castle
        storyNodes.put(2, new TransilvaniaStoryNode.Builder(2,
            "Drumul spre castel șerpuiește prin pădurea întunecată, iar vântul șuieră printre copaci, purtând șoapte " +
            "îndepărtate. Simți că ești urmărit, dar de fiecare dată când te întorci, nu vezi nimic în întuneric.\n\n" +
            "Ajungi în sfârșit la poarta masivă a Castelului Bran. Spre surprinderea ta, aceasta este deschisă, iar " +
            "o lumină slabă pâlpâie în interiorul curții interioare. Când pășești înăuntru, poarta se închide cu un " +
            "zgomot metalic în urma ta, făcându-te să tresari.\n\n" +
            "În mijlocul curții, un bărbat înalt, îmbrăcat într-o capă neagră, stă nemișcat, cu spatele la tine. " +
            "Simțind parcă prezența ta, se întoarce lent, dezvăluindu-și fața palidă și ochii de un roșu intens. " +
            "'Te-am așteptat,' spune el cu o voce adâncă și hipnotică.")
            .title("Primul contact cu Dracula")
            .context("Te afli față în față cu legenda însăși - Contele Dracula, stăpânul nemuririi și al nopții.")
            .choices(new String[]{"Încearcă să fugi", "Acceptă invitația Contelui", "Întreabă-l despre adevărata sa identitate"})
            .nextNodes(new int[]{6, 7, 8})
            .build());

        // Node 3: Finding accommodation
        storyNodes.put(3, new TransilvaniaStoryNode.Builder(3,
            "Decizia de a găsi un loc de cazare pare înțeleaptă, având în vedere ora târzie. Hanul satului pare " +
            "primitor, cu ferestre luminate și sunetul conversațiilor venind dinăuntru.\n\n" +
            "Hangița, o femeie voinică cu ochi blânzi dar vigilenți, te întâmpină cu o privire evaluatoare. " +
            "'Nu primim mulți străini în această perioadă a anului,' spune ea, oferindu-ți cheia unei camere de la etaj. " +
            "'Și fiți prevăzător - nu deschideți fereastra noaptea, indiferent ce auziți sau vedeți.'\n\n" +
            "Camera ta este mică dar confortabilă, cu un pat de lemn masiv și o fereastră care dă spre castel. " +
            "Înainte de a adormi, observi că cineva a atârnat un crucifix deasupra patului și a pus usturoi pe pervazul " +
            "ferestrei.")
            .title("Noaptea la han")
            .context("Superstițiile par adânc înrădăcinate în acest sat, dar poate că există un motiv întemeiat.")
            .choices(new String[]{"Dormi liniștit", "Îndepărtează usturoiul și deschide fereastra", "Stai de pază toată noaptea"})
            .nextNodes(new int[]{9, 10, 11})
            .build());

        // Node 4: Ask about castle entrance
        storyNodes.put(4, new TransilvaniaStoryNode.Builder(4,
            "Bătrânul te privește lung înainte de a răspunde, apoi își coboară vocea aproape la o șoaptă.\n\n" +
            "'Castelul are mai multe intrări, dar doar una e cunoscută de sat. Drumul principal duce la poarta mare, " +
            "dar există și o intrare secretă prin catacombele de sub cimitir. Nimeni nu îndrăznește să meargă acolo " +
            "noaptea, dar se spune că pe acolo au dispărut mulți dintre cei care au avut curiozitatea să exploreze.'\n\n" +
            "El scoate din buzunar o cheie veche, ruginită, și ți-o întinde cu mâna tremurândă. 'Aceasta deschide " +
            "cripta familiei Dracula. Dacă ești hotărât să mergi, ia-o. Dar să știi că odată intrat, puțini s-au mai " +
            "întors ca oameni...'")
            .title("Secretele castelului")
            .context("Alegerea unei căi de intrare în castel ar putea face diferența între viață și un destin mult mai sumbru.")
            .choices(new String[]{"Folosește intrarea principală", "Explorează catacombele", "Renunță și rămâi în sat"})
            .nextNodes(new int[]{2, 12, 3})
            .build());

        // Node 5: Buying a crucifix
        storyNodes.put(5, new TransilvaniaStoryNode.Builder(5,
            "Decizi că ar fi înțelept să iei în serios avertismentele localnicilor. O bătrână care vinde ierburi și " +
            "amulete la marginea satului îți oferă un crucifix de argint frumos lucrat.\n\n" +
            "'Poartă-l mereu la gât,' te sfătuiește ea. 'Argintul și credința sunt primele tale apărări împotriva " +
            "forțelor întunericului. Aici, mai ia și acestea.' Îți oferă și un mic săculeț cu usturoi și o sticluță " +
            "cu apă sfințită.\n\n" +
            "'Pentru protecție,' adaugă ea, fixându-te cu o privire pătrunzătoare. 'Și ține minte - nu-l invita " +
            "niciodată înăuntru. Ei nu pot intra fără invitație.'")
            .title("Pregătit pentru protecție")
            .context("Armele tradiționale împotriva vampirilor: crucifix, usturoi, apă sfințită. Poate par superstiții, dar în această noapte, orice ajutor e binevenit.")
            .choices(new String[]{"Mergi la castel cu protecția", "Caută un loc de cazare", "Întreab-o despre istoria lui Dracula"})
            .nextNodes(new int[]{13, 3, 14})
            .build());

        // Node 6: Trying to escape
        storyNodes.put(6, new TransilvaniaStoryNode.Builder(6,
            "Instinctul îți spune să fugi. Te întorci brusc și alergi spre poarta castelului, dar aceasta rămâne " +
            "nemișcată în ciuda eforturilor tale de a o deschide. În spatele tău, auzi un râs adânc, aproape amuzat.\n\n" +
            "'Nimeni nu pleacă fără permisiunea mea,' spune Contele, aparând brusc lângă tine, deși acum o secundă " +
            "era la câțiva metri distanță. 'De ce să fugi? Ești oaspetele meu, iar eu sunt un gazdă... însetată.'\n\n" +
            "Privirea lui roșie te fixează, și simți cum voința ta începe să se diminueze, cum corpul nu îți mai " +
            "răspunde comenzilor minții tale. Încet, ești atras înapoi spre el, incapabil să reziști.")
            .title("Captiv al Contelui")
            .context("Puterea hipnotică a vampirilor este legendară - și acum experimentezi această putere pe propria piele.")
            .choices(new String[]{"Luptă împotriva influenței", "Cedează", "Arată-i crucifixul (dacă îl ai)"})
            .nextNodes(new int[]{15, 16, 17})
            .build());

        // Node 7: Accepting the Count's invitation
        storyNodes.put(7, new TransilvaniaStoryNode.Builder(7,
            "Cu un amestec de frică și fascinație, accepți invitația Contelui. El zâmbește satisfăcut și face un gest elegant " +
            "cu mâna.\n\n" +
            "'Înțelept din partea ta. Vino, lasă-mă să-ți arăt comorile castelului meu. Am colecționat artefacte și cunoștințe " +
            "timp de secole.'\n\n" +
            "Contele te conduce prin coridoare lungi, luminate de torțe, povestindu-ți despre istoria Transilvaniei și despre " +
            "luptele sale împotriva Imperiului Otoman. Cu fiecare pas, farmecul său magnetic te atrage tot mai adânc în mrejele sale. " +
            "Într-o cameră plină de cărți vechi și artefacte, Contele se oprește și îți arată un portret al său de secole.\n\n" +
            "'Recunoști această înfățișare? Este Vlad Țepeș, voievodul Valahiei, cunoscut pentru cruzimea sa împotriva dușmanilor. " +
            "Unii mă numesc Dracula, fiul dragonului. Timpul pentru mine curge diferit, iar anii sunt doar clipe în existența mea.'")
            .title("Comorile Contelui")
            .context("În fiecare poveste se ascunde un sâmbure de adevăr, iar legendele despre Conte par să se confirme cu fiecare clipă.")
            .choices(new String[]{"Continuă să-l urmezi mai adânc în castel", "Întreabă despre secretul nemuririi", "Caută o cale de scăpare"})
            .nextNodes(new int[]{18, 19, 6})
            .build());
            
        // Node 8: Asking about his identity
        storyNodes.put(8, new TransilvaniaStoryNode.Builder(8,
            "Cu voce tremurândă, întrebi despre adevărata identitate a Contelui. Ochii lui strălucesc cu o intensitate nouă, " +
            "părând mulțumit de curiozitatea ta.\n\n" +
            "'Sunt mai bătrân decât toate legendele despre mine. Am fost Vlad Țepeș, domnitorul Valahiei, temut de dușmani " +
            "și respectat de supuși. Timpul m-a schimbat, dar esența a rămas aceeași - puterea, dorința de control, setea... " +
            "nu doar de sânge, ci de viață eternă.'\n\n" +
            "Contele se apropie de tine, iar aerul din jur pare să devină mai rece. 'Poate te întrebi cum am supraviețuit " +
            "atâtea secole? Nemurirea, călătorule, este atât un dar, cât și un blestem. Unul pe care aș putea să-l împărtășesc " +
            "cu tine, dacă ești dispus să plătești prețul...'")
            .title("Adevărata identitate")
            .context("Vlad Țepeș, Dracula - istorie și legendă se împletesc într-o entitate ce a supraviețuit secolelor.")
            .choices(new String[]{"Întreabă despre prețul nemuririi", "Refuză oferta", "Cere timp de gândire"})
            .nextNodes(new int[]{19, 20, 7})
            .build());
            
        // Node 9: Sleeping peacefully
        storyNodes.put(9, new TransilvaniaStoryNode.Builder(9,
            "Decizi să dormi liniștit, convins că superstițiile localnicilor sunt exagerate. Adormi rapid, obosit după " +
            "călătoria lungă.\n\n" +
            "În mijlocul nopții, te trezești brusc, simțind o prezență în cameră. La început crezi că este doar un vis, " +
            "dar apoi observi o ceață stranie care se strecoară pe sub ușă, materializându-se încet într-o siluetă întunecată.\n\n" +
            "Crucifixul de deasupra patului începe să strălucească slab, iar ceața pare să ezite. După câteva momente " +
            "tensionate, silueta dispare, lăsând în urmă doar un șopot: 'Ne vom întâlni în curând, călătorule...'")
            .title("Vizitator nocturn")
            .context("Protecțiile simple ale hanului par să fi funcționat, dar pentru cât timp te vor ține în siguranță?")
            .choices(new String[]{"Părăsește satul dimineața", "Mergi la castel în zori", "Caută ajutorul localnicilor"})
            .nextNodes(new int[]{21, 22, 23})
            .build());
            
        // Node 10: Removing the garlic
        storyNodes.put(10, new TransilvaniaStoryNode.Builder(10,
            "Consideri că usturoiul și crucifixul sunt doar superstiții absurde și le îndepărtezi, deschizând larg fereastra " +
            "pentru a lăsa aerul proaspăt al nopții să intre.\n\n" +
            "Te culci și adormi rapid, dar somnul îți este agitat, plin de vise tulburi. În miez de noapte, te trezești " +
            "simțind o prezență în cameră. O ceață stranie se strecoară prin fereastra deschisă, materializându-se într-o " +
            "siluetă întunecată și elegantă - Contele Dracula, cu ochii lui roșii strălucind în întuneric.\n\n" +
            "'O invitație înțeleaptă,' șoptește el, apropiindu-se de patul tău. Încerci să strigi, dar niciun sunet nu-ți " +
            "iese din gât. 'Nu te teme, vei deveni parte din ceva mai mare decât ai visat vreodată.'\n\n" +
            "Simți o durere ascuțită în gât, urmată de o senzație ciudată de plăcere întunecoasă, în timp ce Contele îți " +
            "bea sângele. Încet, lumea se estompează în jurul tău...")
            .title("Mușcătura fatală")
            .context("A îndepărta protecțiile împotriva vampirilor în inima Transilvaniei a fost o decizie neinspirată.")
            .choices(new String[]{"Luptă pentru viața ta", "Acceptă transformarea", "Imploră milă"})
            .nextNodes(new int[]{15, 24, 25})
            .build());
            
        // Node 11: Staying vigilant
        storyNodes.put(11, new TransilvaniaStoryNode.Builder(11,
            "Hotărăști să rămâi vigilent și să nu dormi. Aprinzi luminile, îți pregătești crucifixul și aștepți, cu ochii " +
            "ațintiți spre fereastră și ușă.\n\n" +
            "Orele trec lent, și tocmai când oboseala începe să te copleșească, auzi un zgomot ușor la fereastră. O ceață " +
            "stranie încearcă să se strecoare pe sub rama ferestrei, dar usturoiul pare să o împiedice.\n\n" +
            "Apoi, auzi o voce șoptită din întuneric: 'Lasă-mă să intru, călătorule. Doar pentru o conversație. Sunt sigur " +
            "că ai multe întrebări despre mine, despre acest castel, despre legendele care mă înconjoară...' Vocea este hipnotică, " +
            "aproape irezistibilă, dar reușești să te ții tare, refuzând invitația.")
            .title("Noaptea de veghe")
            .context("Primul test împotriva puterilor Contelui a fost trecut cu succes, dar acesta nu va renunța așa ușor.")
            .choices(new String[]{"Continuă să refuzi", "Invită-l înăuntru pentru discuție", "Arată-i crucifixul"})
            .nextNodes(new int[]{23, 10, 26})
            .build());
            
        // Node 12: Exploring the catacombs
        storyNodes.put(12, new TransilvaniaStoryNode.Builder(12,
            "Folosind cheia ruginită primită de la bătrân, te strecori în cimitirul satului și găsești cripta familiei Dracula. " +
            "Cu mâini tremurânde, deschizi ușa grea și cobori în întunericul din interior.\n\n" +
            "Cu ajutorul unei torțe improvizate, descoperi un tunel secret ce duce adânc sub pământ. Tunelul se transformă " +
            "într-un labirint de catacombe pline cu rămășițele celor ce au fost odată nobili și supuși ai regiunii.\n\n" +
            "După ce rătăcești ce pare o eternitate, ajungi la o ușă antică, sculptată cu simboluri ciudate. O deschizi cu grijă " +
            "și te trezești într-o cameră subterană vastă a castelului. În centrul încăperii se află un sicriu de marmură neagră. " +
            "Te apropii precaut și, spre groaza ta, capacul începe să se miște...")
            .title("Catacombele secrete")
            .context("Ai pătruns în inima ascunsă a domeniului lui Dracula, acolo unde puțini muritori au ajuns vreodată.")
            .choices(new String[]{"Fugi spre ieșire", "Deschide capacul sicriului", "Ascunde-te și observă"})
            .nextNodes(new int[]{27, 28, 29})
            .build());
            
        // Node 13: Going to castle with protection
        storyNodes.put(13, new TransilvaniaStoryNode.Builder(13,
            "Înarmat cu crucifixul, usturoiul și apa sfințită, pornești hotărât spre castel. Pe măsură ce te apropii, " +
            "simți o rezistență ciudată, ca și cum aerul din jurul tău ar deveni tot mai greu.\n\n" +
            "Ajuns la poarta castelului, aceasta se deschide singură, cu un scârțâit prelung. În curtea interioară, Contele " +
            "te așteaptă, dar stă la distanță, observându-te cu un amestec de curiozitate și prudență.\n\n" +
            "'Un oaspete pregătit, văd. Rarități în zilele noastre.' Ochii lui roșii observă crucifixul pe care îl porți. " +
            "'Ai venit cu armele potrivite, dar te afli pe domeniul meu acum. Aici, puterile mele sunt la apogeu.'")
            .title("Confruntare prudentă")
            .context("Dracula respectă curajul tău de a-l înfrunta pe propriul teritoriu, dar asta nu înseamnă că ești în siguranță.")
            .choices(new String[]{"Amenință-l cu obiectele sfințite", "Propune un pact", "Întreabă-l despre scopurile sale"})
            .nextNodes(new int[]{30, 19, 8})
            .build());
            
        // Node 14: Asking about Dracula's history
        storyNodes.put(14, new TransilvaniaStoryNode.Builder(14,
            "O întrebi pe bătrână despre istoria adevărată a lui Dracula. Ea îți face semn să te așezi și începe să-ți " +
            "povestească cu o voce joasă:\n\n" +
            "'Dracula nu a fost mereu o creatură a nopții. Cândva, era Vlad Țepeș, un domnitor crud dar drept. A apărat " +
            "aceste ținuturi împotriva turcilor, dar metodele sale erau sângeroase - trăgea în țeapă dușmanii, de unde și " +
            "porecla. Legenda spune că într-o noapte, după o bătălie, a băut din disperare sânge de pe câmpul de luptă, " +
            "jurând că nu va muri niciodată până nu va elibera complet țara de dușmani.'\n\n" +
            "Ea face o pauză, privind în gol. 'Se zice că un demon i-a auzit jurământul și l-a luat în serios. I-a oferit " +
            "nemurirea, dar l-a condamnat la o sete eternă de sânge. Acum, el trăiește în castel, vânând călătorii neștiutori " +
            "și încercând să-și extindă legiunea de nemuritori.'")
            .title("Istoria lui Dracula")
            .context("Dincolo de legenda turistică se ascunde o poveste mult mai întunecată și mai complexă.")
            .choices(new String[]{"Mergi la castel să-l confrunți", "Întreabă despre slăbiciunile lui", "Caută un vânător de vampiri"})
            .nextNodes(new int[]{13, 31, 32})
            .build());
            
        // Node 15: Fighting Dracula's influence
        storyNodes.put(15, new TransilvaniaStoryNode.Builder(15,
            "Cu o voință de fier, lupți împotriva influenței hipnotice a Contelui. Este ca și cum ai încerca să înoți " +
            "împotriva unui curent puternic, dar refuzi să cedezi.\n\n" +
            "În disperare, îți aduci aminte de crucifixul pe care l-ai primit și îl scoți tremurând. La vederea lui, " +
            "Contele șuieră și face un pas înapoi, cu fața contorsionată de furie și durere.\n\n" +
            "'Curajos, dar naiv,' mârâie el. 'Crezi că un simbol te poate proteja pentru totdeauna? Aici, în domeniul meu, " +
            "timpul este de partea mea. Voi aștepta până când vigilența ta va slăbi, până când vei face o singură greșeală...' " +
            "El se retrage în umbre, dar prezența sa rămâne palpabilă, ca o promisiune de revenire.")
            .title("Rezistență temporară")
            .context("Ai câștigat această rundă, dar lupta cu Dracula de-abia a început.")
            .choices(new String[]{"Caută o ieșire din castel", "Caută aliați împotriva lui", "Urmărește-l în întuneric"})
            .nextNodes(new int[]{27, 32, 33})
            .build());
            
        // Node 16: Yielding to Dracula
        storyNodes.put(16, new TransilvaniaStoryNode.Builder(16,
            "Resemnându-te în fața puterii copleșitoare a Contelui, cedezi voinței sale. Un sentiment ciudat de calm te " +
            "cuprinde, ca și cum ai fi eliberat de povara deciziilor proprii.\n\n" +
            "Contele se apropie de tine cu o grație letală și îți ridică bărbia cu degetele sale reci. 'Înțelept din partea ta. " +
            "Nu toți care mi se opun ajung să vadă răsăritul din nou.' Colții lui se apropie de gâtul tău, iar durerea inițială " +
            "este rapid înlocuită de o senzație euforică, în timp ce viața îți este suptă încet.\n\n" +
            "Apoi, într-un gest surprinzător, Contele își taie încheietura și îți oferă sângele său. 'Bea, și devino unul " +
            "dintre ai mei. Împreună vom domni în Transilvania pentru eternitate.'")
            .title("Începutul transformării")
            .context("Cedând în fața lui Dracula, ai pornit pe calea transformării într-o creatură a nopții.")
            .choices(new String[]{"Bea sângele Contelui", "Refuză în ultimul moment", "Cere timp de gândire"})
            .nextNodes(new int[]{24, 25, 34})
            .build());
            
        // Node 17: Showing the crucifix
        storyNodes.put(17, new TransilvaniaStoryNode.Builder(17,
            "Cu mâna tremurândă, scoți crucifixul de argint și îl ridici în fața Contelui. Efectul este imediat și violent - " +
            "Dracula se retrage cu un șuierat dureros, ridicând mâna pentru a-și proteja ochii de strălucirea argintului sfințit.\n\n" +
            "'Nenorocitule!' strigă el, furia și durerea deformându-i fața într-o mască monstruoasă, revelând adevărata sa " +
            "natură de fiară. 'Vei plăti pentru această îndrăzneală!'\n\n" +
            "Profitând de momentul de slăbiciune al Contelui, te retragi spre poarta castelului, ținând crucifixul în față " +
            "ca pe un scut. Spre surprinderea ta, poarta se deschide, permițându-ți să fugi în noapte. Dar în spatele tău, " +
            "auzi clar cuvintele lui Dracula: 'Fugi cât poți. Noaptea este lungă, iar tu ești singur în Transilvania mea...'")
            .title("Evadare temporară")
            .context("Crucifixul ți-a oferit o șansă de scăpare, dar furia Contelui te va urmări oriunde te-ai duce în ținutul său.")
            .choices(new String[]{"Fugi spre sat", "Caută adăpost în pădure", "Înfruntă-l din nou"})
            .nextNodes(new int[]{23, 35, 30})
            .build());
            
        // Node 18: Following deeper into the castle
        storyNodes.put(18, new TransilvaniaStoryNode.Builder(18,
            "Decizi să-l urmezi pe Conte mai adânc în castel, curiozitatea și fascinația învingându-ți frica. Coborâți pe " +
            "scări spiralate de piatră, care par să ducă în inima muntelui pe care este construit castelul.\n\n" +
            "Ajungeți într-o sală vastă, subterană, iluminată de torțe cu flăcări albastre. În centru se află o masă lungă " +
            "de stejar, iar pe pereți sunt portrete vechi de secole, toate înfățișând figura Contelui în diferite epoci. " +
            "Realizezi că privești la o dovadă vizuală a nemuririi sale.\n\n" +
            "'Aceasta este sala Consiliului,' explică Dracula. 'Aici, cei ca mine se adună odată la century pentru a decide " +
            "soarta regiunilor pe care le controlăm. Iar acum, vreau să te invit să te alături nouă – o onoare rară pentru un muritor.'")
            .title("Consiliul Nopții")
            .context("Dracula îți oferă o privire în lumea secretă a nemuritorilor - un privilegiu rar și potențial periculos.")
            .choices(new String[]{"Acceptă invitația", "Întreabă despre ceilalți vampiri", "Refuză"})
            .nextNodes(new int[]{19, 36, 20})
            .build());
            
        // Node 19: The price of immortality
        storyNodes.put(19, new TransilvaniaStoryNode.Builder(19,
            "Întrebi despre prețul nemuririi sau accepți oferta Contelui. Ochii lui strălucesc de satisfacție, simțind " +
            "că pescuirea sa a avut succes.\n\n" +
            "'Prețul nemuririi este simplu și complex în același timp,' spune el, apropiindu-se de tine. 'Trebuie să renunți " +
            "la umanitatea ta - la morala ta, la lumina zilei, la credințele tale de muritor. Trebuie să accepți întunericul " +
            "nu doar în jurul tău, ci și în tine.'\n\n" +
            "Contele își taie încheietura, iar sângele său negru curge într-un potir vechi de argint. 'Bea din sângele meu, " +
            "iar eu voi bea din al tău. Prin acest schimb, vom fi legați pentru eternitate, iar tu vei renaște ca o creatură " +
            "a nopții - puternică, nemuritoare, liberă de constrângerile mortale.'")
            .title("Pactul de sânge")
            .context("Oferta nemuririi este în fața ta, dar prețul este sufletul tău. Ce vei alege?")
            .choices(new String[]{"Acceptă pactul", "Refuză pactul", "Cere timp de gândire"})
            .nextNodes(new int[]{24, 20, 34})
            .build());
            
        // Node 20: Refusing the offer
        storyNodes.put(20, new TransilvaniaStoryNode.Builder(20,
            "Cu un efort considerabil, refuzi oferta Contelui. Fața lui se întunecă de nemulțumire, dar curând este " +
            "înlocuită de un zâmbet rece, calculat.\n\n" +
            "'Puțini au curajul să refuze ceea ce ofer eu. Îți respect decizia, deși o consider... neinspirată.' " +
            "Dracula face un pas înapoi, gesticulând spre ușă. 'Ești liber să pleci. Porțile castelului meu sunt deschise pentru tine.'\n\n" +
            "Oferta pare prea generoasă pentru a fi adevărată, și în timp ce te îndrepți spre ieșire, simți că privirea " +
            "Contelui îți arde spatele. Ajungi la poartă și descoperi că, într-adevăr, aceasta este deschisă. Dar în timp " +
            "ce pășești afară, auzi șoapta lui: 'Ne vom revedea, călătorule. Destinul nostru este legat acum, fie că îți " +
            "place sau nu.'")
            .title("Libertate aparentă")
            .context("Ai reușit să refuzi oferta lui Dracula și să scapi din castel, dar te-ai făcut remarcind pentru eternitate de către el.")
            .choices(new String[]{"Fugi spre sat", "Întoarce-te și înfruntă-l", "Caută ajutor la hanul local"})
            .nextNodes(new int[]{23, 30, 21})
            .build());
            
        // Set the total number of story nodes
        totalStoryNodes = storyNodes.size();
    }

    private void showScene() {
        // Hide all interactive elements first
        hideAllInteractiveElements();
        
        // Update progress indicator
        updateProgressIndicator();
        
        // Check if we're at the end of the story
        if (currentSceneIndex == -1) {
            finish();
            return;
        }

        // Check if we've reached the vampire test scene
        if (currentSceneIndex == 25) {
            showVampireTest();
            return;
        }
        
        // Get current story node
        TransilvaniaStoryNode currentNode = storyNodes.get(currentSceneIndex);
        if (currentNode == null) {
            // If we've run out of defined nodes, show the vampire test
            showVampireTest();
            return;
        }

        // Update story content
        storyTitle.setText(getSceneTitle(currentSceneIndex));
        storyText.setText(currentNode.getContent());
        storyContext.setText(getSceneContext(currentSceneIndex));

        // Update image based on scene if needed
        updateSceneImage();

        // Setup choice buttons for this node
        setupChoiceButtons(currentNode);

        // If this node has rewards, update points
        if (currentNode.getPointsReward() != 0) {
            int newPoints = currentPoints + currentNode.getPointsReward();
            pointsManager.addPoints(this, "transilvania", currentNode.getPointsReward());
            currentPoints = newPoints;
            
            // Show feedback about points
            String message = currentNode.getPointsReward() > 0 
                ? "Ai primit " + currentNode.getPointsReward() + " puncte!" 
                : "Ai pierdut " + Math.abs(currentNode.getPointsReward()) + " puncte!";
            
            showFeedback(message);
        }
        
        // Track special story flags
        if (currentSceneIndex == 2 || currentSceneIndex == 7 || currentSceneIndex == 8) {
            hasMetDracula = true;
        }
        
        if (currentSceneIndex == 10 || currentSceneIndex == 16) {
            hasBeenBitten = true;
        }
        
        if (currentSceneIndex == 17 || currentSceneIndex == 19) {
            hasFoundBlood = true;
        }
    }
    
    private void updateProgressIndicator() {
        if (totalStoryNodes > 0) {
            int progress = (int) (((float) currentSceneIndex / totalStoryNodes) * 100);
            progressIndicator.setProgress(progress);
        }
    }
    
    private void updateSceneImage() {
        // Set different images based on the current scene
        int imageResource = R.drawable.castle_dracula; // Default image
        
        switch (currentSceneIndex) {
            case 0:
                imageResource = R.drawable.transylvania_village;
                break;
            case 1:
                imageResource = R.drawable.tavern;
                break;
            case 2:
            case 7:
            case 8:
                imageResource = R.drawable.dracula_portrait;
                break;
            case 3:
                imageResource = R.drawable.inn_room;
                break;
            case 6:
                imageResource = R.drawable.castle_gates;
                break;
            case 10:
            case 16:
                imageResource = R.drawable.vampire_bite;
                break;
            case 12:
                imageResource = R.drawable.catacombs;
                break;
            default:
                // Use default castle image
                break;
        }
        
        storyImageView.setImageResource(imageResource);
    }
    
    private void hideAllInteractiveElements() {
        if (bettingCard != null) {
            bettingCard.setVisibility(View.GONE);
        }
        if (feedbackCard != null) {
            feedbackCard.setVisibility(View.GONE);
        }
        if (interactiveCardView != null) {
            interactiveCardView.setVisibility(View.GONE);
        }
        if (continueButton != null) {
            continueButton.setVisibility(View.GONE);
        }
    }

    private String getSceneTitle(int sceneIndex) {
        TransilvaniaStoryNode node = storyNodes.get(sceneIndex);
        if (node != null && node.getTitle() != null && !node.getTitle().isEmpty()) {
            return node.getTitle();
        }
        
        // Default titles based on scene index
        switch (sceneIndex) {
            case 0:
                return "Bine ai venit în Transilvania!";
            case 1:
                return "Legendele locului";
            case 2:
                return "Întâlnirea cu Contele";
            case 3:
                return "Noaptea la han";
            case 6:
                return "Prizonier al Castelului";
            case 7:
                return "Invitația lui Dracula";
            case 10:
                return "Vizitator nocturn";
            case 12:
                return "Catacombele secrete";
            case 16:
                return "Mușcătura";
            case 25:
                return "Testul Vampirului";
            default:
                return "Aventura continuă...";
        }
    }

    private String getSceneContext(int sceneIndex) {
        TransilvaniaStoryNode node = storyNodes.get(sceneIndex);
        if (node != null && node.getContext() != null && !node.getContext().isEmpty()) {
            return node.getContext();
        }
        
        // Default context based on scene index
        switch (sceneIndex) {
            case 0:
                return "Fiecare alegere pe care o faci va influența parcursul poveștii și finalul acesteia.";
            case 25:
                return "Ce fel de creatură ai devenit? Testul îți va dezvălui adevărata natură.";
            default:
                return "Aventura ta prin Transilvania continuă, iar fiecare decizie te aduce mai aproape de adevăr.";
        }
    }

    private void setupChoiceButtons(TransilvaniaStoryNode node) {
        // Create a local variable for the container or use an existing one
        LinearLayout choicesContainer = findViewById(R.id.optionsContainer); // Using optionsContainer instead
        
        if (choicesContainer == null) {
            // If optionsContainer doesn't exist, log an error and return
            System.out.println("ERROR: Could not find options container");
            return;
        }
        
        choicesContainer.removeAllViews();
        
        if (node.getChoices() != null && node.getChoices().length > 0) {
            // Has choices - show choice buttons
            nextButton.setVisibility(View.GONE);
            
            for (int i = 0; i < node.getChoices().length; i++) {
                String choice = node.getChoices()[i];
                int nextNode = node.getNextNodes()[i];
                
                MaterialButton choiceButton = new MaterialButton(this);
                choiceButton.setText(choice);
                choiceButton.setBackgroundTintList(getResources().getColorStateList(R.color.design_default_color_secondary));
                
                // Set margin and other properties
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 16);
                choiceButton.setLayoutParams(params);
                
                // Add click listener
                final int choiceIndex = i;
                choiceButton.setOnClickListener(v -> {
                    currentSceneIndex = nextNode;
                    animateSceneTransition();
                });
                
                choicesContainer.addView(choiceButton);
            }
        } else {
            // No choices - show next button
            nextButton.setVisibility(View.VISIBLE);
        }
    }
    
    private void showFeedback(String message) {
        if (feedbackCard != null) {
            feedbackCard.setVisibility(View.VISIBLE);
        }
        if (feedbackText != null) {
            feedbackText.setText(message);
        }
        
        // Auto-hide feedback after delay
        handler.postDelayed(() -> {
            if (feedbackCard != null) {
                feedbackCard.setVisibility(View.GONE);
            }
        }, 3000);
    }
    
    private void showVampireTest() {
        // Hide regular story elements
        if (storyTitle != null) {
            storyTitle.setText("Testul Vampirului");
        }
        if (storyText != null) {
            storyText.setText("După toate aventurile tale prin Transilvania, a venit momentul să descoperi ce impact au avut " +
                    "alegerile tale. Alegând unul dintre obiectele de mai jos, vei descoperi ce fel de creatură ai devenit după " +
                    "experiențele tale cu Contele Dracula.");
        }
        if (storyContext != null) {
            storyContext.setText("Fiecare obiect reprezintă o parte din tine. Care te atrage cel mai mult?");
        }
        
        // Show vampire test interface
        if (interactiveCardView != null) {
            interactiveCardView.setVisibility(View.VISIBLE);
        }
        if (nextButton != null) {
            nextButton.setVisibility(View.GONE);
        }
        
        // Set highlight based on previous choices
        if (hasBeenBitten && objectCard1 != null) {
            objectCard1.setStrokeColor(getResources().getColor(R.color.design_default_color_error));
            objectCard1.setStrokeWidth(5);
        }
        
        if (hasFoundBlood && objectCard3 != null) {
            objectCard3.setStrokeColor(getResources().getColor(R.color.design_default_color_error));
            objectCard3.setStrokeWidth(5);
        }
    }
    
    private void selectVampireTest(int choice) {
        String resultTitle;
        String resultText;
        int pointsEarned = 0;
        
        switch (choice) {
            case 1: // Blood
                resultTitle = "Vampir complet";
                resultText = "Sângele te-a atras cel mai mult, dovadă că transformarea ta este completă. Ai devenit " +
                        "un vampir adevărat, nemuritor și însetat de sânge. Contelui Dracula i-ai devenit un aliat " +
                        "prețios și vei continua să bântui nopțile Transilvaniei pentru eternitate. Puterea ta este " +
                        "acum considerabilă, dar la fel și slăbiciunile tale față de simbolurile sacre și lumina soarelui.";
                pointsEarned = hasBeenBitten ? 100 : 50;
                break;
                
            case 2: // Crucifix
                resultTitle = "Vânător de vampiri";
                resultText = "Alegând crucifixul, ai demonstrat că esența ta umană a rămas intactă, ba chiar s-a " +
                        "întărit. Experiențele tale te-au transformat într-un vânător de vampiri, hotărât să elibereze " +
                        "Transilvania de amenințarea lui Dracula. Cunoștințele acumulate despre vampiri te fac un " +
                        "adversar redutabil pentru creaturile nopții.";
                pointsEarned = hasMetDracula && !hasBeenBitten ? 75 : 40;
                break;
                
            case 3: // Moon
                resultTitle = "Dhampir";
                resultText = "Luna te atrage într-un mod special, semn că ai devenit un dhampir - jumătate om, " +
                        "jumătate vampir. Ai păstrat multe dintre calitățile umane, dar ai dobândit și abilități " +
                        "supranaturale. Poți merge la lumina zilei, dar simți și chemarea nopții. Ești un " +
                        "mediator între cele două lumi, cu avantajele ambelor, dar și cu conflictul interior specific.";
                pointsEarned = hasFoundBlood ? 90 : 60;
                break;
                
            default:
                resultTitle = "Călător schimbat";
                resultText = "Experiențele tale în Transilvania te-au schimbat profund, dar natura exactă a transformării " +
                        "rămâne necunoscută chiar și pentru tine. Te simți diferit, parcă mai conștient de umbrele din jurul tău " +
                        "și de misterele lumii. Timpul va dezvălui ce ai devenit cu adevărat.";
                pointsEarned = 30;
        }
        
        // Hide test interface
        if (interactiveCardView != null) {
            interactiveCardView.setVisibility(View.GONE);
        }
        
        // Show result
        if (storyTitle != null) {
            storyTitle.setText(resultTitle);
        }
        if (storyText != null) {
            storyText.setText(resultText);
        }
        if (storyContext != null) {
            storyContext.setText("Finalul călătoriei tale prin Transilvania, dar poate începutul unei noi aventuri...");
        }
        
        // Award points
        pointsManager.addPoints(this, "transilvania", pointsEarned);
        showFeedback("Felicitări! Ai câștigat " + pointsEarned + " puncte pentru finalizarea aventurii!");
        
        // Show continue button to exit
        if (continueButton != null) {
            continueButton.setText("Încheie aventura");
            continueButton.setVisibility(View.VISIBLE);
            continueButton.setOnClickListener(v -> finish());
        }
    }
} 