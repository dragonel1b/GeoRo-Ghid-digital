package com.example.myapplication.transilvaniausage;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.core.domain.model.TransilvaniaStoryNode;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

/**
 * ViewModel pentru DraculaStoryActivity pentru a separa logica de afaceri de UI
 */
public class DraculaStoryViewModel extends AndroidViewModel {
    
    private final MutableLiveData<TransilvaniaStoryNode> currentNode = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPoints = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> currentSceneIndex = new MutableLiveData<>(0);
    private final MutableLiveData<String> feedbackMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showVampireTest = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> showFinalScreen = new MutableLiveData<>(false);
    private final MutableLiveData<String> finalStoryOutcome = new MutableLiveData<>();
    
    private Map<Integer, TransilvaniaStoryNode> storyNodes;
    private PointsManager pointsManager;
    
    // Story flags
    private boolean hasMetDracula = false;
    private boolean hasBeenBitten = false;
    private boolean hasFoundBlood = false;
    private boolean hasFoundArtifact = false;
    private int artifactPieces = 0;
    
    // Constants for story endings
    public static final int ENDING_VAMPIRE = 1;
    public static final int ENDING_HUNTER = 2;
    public static final int ENDING_DHAMPIR = 3;
    public static final int ENDING_ESCAPE = 4;
    
    public DraculaStoryViewModel(@NonNull Application application) {
        super(application);
        pointsManager = PointsManager.getInstance(application);
        initializeStory();
        loadCurrentNode();
    }
    
    public LiveData<TransilvaniaStoryNode> getCurrentNode() {
        return currentNode;
    }
    
    public LiveData<Integer> getCurrentPoints() {
        return currentPoints;
    }
    
    public LiveData<Integer> getCurrentSceneIndex() {
        return currentSceneIndex;
    }
    
    public LiveData<String> getFeedbackMessage() {
        return feedbackMessage;
    }
    
    public LiveData<Boolean> getShowVampireTest() {
        return showVampireTest;
    }
    
    public LiveData<Boolean> getShowFinalScreen() {
        return showFinalScreen;
    }
    
    public LiveData<String> getFinalStoryOutcome() {
        return finalStoryOutcome;
    }
    
    public boolean hasMetDracula() {
        return hasMetDracula;
    }
    
    public boolean hasBeenBitten() {
        return hasBeenBitten;
    }
    
    public boolean hasFoundBlood() {
        return hasFoundBlood;
    }
    
    public boolean hasFoundArtifact() {
        return hasFoundArtifact;
    }
    
    public int getArtifactPieces() {
        return artifactPieces;
    }
    
    /**
     * Returnează numărul total de noduri ale poveștii pentru calculul progresului
     */
    public int getTotalStoryNodes() {
        try {
            return storyNodes != null ? storyNodes.size() : 0;
        } catch (Exception e) {
            Log.e("DraculaStoryViewModel", "Error getting total story nodes: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Inițializează datele poveștii
     */
    private void initializeStory() {
        try {
            storyNodes = new HashMap<>();
            
            // Nodul 0: Introducere
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

            // Nodul 1: Localnicii povestesc
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
                .pointsReward(10)
                .build());

            // Nodul 2: La castel
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
                .pointsReward(15)
                .build());
            
            // Nodul 3: Cazare la han
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
                .pointsReward(5)
                .build());
            
            // Nodul 4: Secretele castelului
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
                .pointsReward(10)
                .build());
            
            // Nodul 5: Pregătiri pentru protecție
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
                .pointsReward(10)
                .build());
            
            // Nodul 6: Încercare de fugă
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
                .pointsReward(-5)
                .build());
            
            // Mai multe noduri ale poveștii...
            // Nodul 7: Acceptarea invitației
            storyNodes.put(7, new TransilvaniaStoryNode.Builder(7,
                "Cu un amestec de frică și fascinație, accepți invitația Contelui. El zâmbește satisfăcut și face un gest elegant " +
                "cu mâna.\n\n" +
                "'Înțelept din partea ta. Vino, lasă-mă să-ți arăt comorile castelului meu. Am colecționat artefacte și cunoștințe " +
                "timp de secole.'\n\n" +
                "Contele te conduce prin coridoare lungi, luminate de torțe, povestindu-ți despre istoria Transilvaniei și despre " +
                "luptele sale împotriva Imperiului Otoman. Cu fiecare pas, farmecul său magnetic te atrage tot mai adânc în mrejele sale.")
                .title("Comorile Contelui")
                .context("În fiecare poveste se ascunde un sâmbure de adevăr, iar legendele despre Conte par să se confirme cu fiecare clipă.")
                .choices(new String[]{"Continuă să-l urmezi mai adânc în castel", "Întreabă despre secretul nemuririi", "Caută o cale de scăpare"})
                .nextNodes(new int[]{18, 19, 6})
                .build());
            
            // Noduri pentru finalurile poveștii
            
            // Nodul 90: Final - Transformare în vampir
            storyNodes.put(90, new TransilvaniaStoryNode.Builder(90,
                "După toate încercările prin care ai trecut, nu ai putut rezista seducției întunericului. Sângele Contelui " +
                "curge acum prin venele tale, transformându-te lent dar sigur într-o creatură a nopții.\n\n" +
                "Pe măsură ce ultima rămășiță a umanității tale se estompează, simți cum simțurile îți devin mai ascuțite, " +
                "cum puterea curge prin tine. Foamea de sânge devine o companie constantă, dar și capacitatea de a trăi secole, " +
                "de a vedea cum se schimbă lumea.\n\n" +
                "Contele te privește cu satisfacție. 'Bine ai venit în eternitate, copilul meu. Acum ești cu adevărat liber.'\n\n" +
                "Castelul Bran a câștigat un nou stăpân, iar legendele Transilvaniei, un nou capitol.")
                .title("Nemuritor în întuneric")
                .context("Ai devenit ceea ce ai vânat - un vampir, o ființă a nopții, blestemată cu nemurirea și binecuvântată cu putere dincolo de imaginație.")
                .pointsReward(100)
                .build());
            
            // Nodul 91: Final - Vânător de vampiri
            storyNodes.put(91, new TransilvaniaStoryNode.Builder(91,
                "După confruntarea cu întunericul și supraviețuirea față în față cu Dracula, te-ai întors la sat schimbat pentru totdeauna. " +
                "Cunoștințele despre vampiri și abilitățile dobândite în timpul aventurii tale te-au transformat într-un vânător respectat.\n\n" +
                "Locuitorii satului te privesc acum cu respect și recunoștință, știind că veghezi asupra lor în nopțile cu lună plină. " +
                "Ți-ai dedicat viața protejării celor nevinovați de amenințările din umbre.\n\n" +
                "Castelul încă se profilează amenințător pe cerul nopții, iar Contele încă bântuie coridoarele sale. Dar acum, " +
                "el știe că are un adversar demn, iar lupta continuă, într-un dans etern între lumină și întuneric.")
                .title("Gardianul luminii")
                .context("Ai devenit un vânător de vampiri, folosind cunoștințele dobândite pentru a proteja nevinovații de amenințările din umbră.")
                .pointsReward(75)
                .build());
            
            // Nodul 92: Final - Dhampir
            storyNodes.put(92, new TransilvaniaStoryNode.Builder(92,
                "Confruntarea cu Dracula te-a lăsat marcat pentru totdeauna. Mușcătura sa și sângele său care ți-a pătruns în sistem " +
                "te-au transformat într-o ființă unică - nici om, nici vampir, ci ceva între cele două lumi.\n\n" +
                "Ca dhampir, te bucuri de avantajele ambelor lumi - forța și simțurile ascuțite ale vampirilor, dar fără setea " +
                "insațiabilă de sânge și vulnerabilitatea la lumina zilei. Ești un mediator între lumea oamenilor și cea a nopții.\n\n" +
                "În timpul zilei, ajuți localnicii cu cunoștințele tale despre supranatural. În timpul nopții, păstrezi un echilibru " +
                "fragil între tărâmul muritorilor și cel al nemuritorilor. Dracula însuși te respectă, deși niciodată nu va admite acest lucru.")
                .title("Între două lumi")
                .context("Nici om, nici vampir - un dhampir, o punte între două lumi, cu puterea de a proteja umanitatea de amenințările întunericului.")
                .pointsReward(90)
                .build());
            
            // Nodul 93: Final - Scăpare
            storyNodes.put(93, new TransilvaniaStoryNode.Builder(93,
                "Prin inteligență, curaj și poate puțin noroc, ai reușit să scapi din mrejele Contelui Dracula și să părăsești " +
                "Transilvania cu viața intactă. Deși ai supraviețuit, experiențele trăite te-au schimbat profund.\n\n" +
                "Te-ai întors în lumea obișnuită, dar nu mai ești aceeași persoană. Nopțile cu lună plină îți aduc amintiri " +
                "vii ale castelului întunecat și ale stăpânului său nemuritor. Uneori, ți se pare că auzi șoaptele lui Dracula " +
                "pe vânt, chemându-te înapoi.\n\n" +
                "Știi că secretele Transilvaniei te vor urmări mereu, dar cel puțin acum ai propria poveste de spus. O poveste " +
                "pe care puțini o vor crede, dar care va rămâne mereu adevărată pentru tine.")
                .title("Întoarcerea acasă")
                .context("Ai scăpat cu viață din Transilvania, dar amintirile și experiențele trăite te vor urmări mereu, schimbându-ți perspectiva asupra lumii.")
                .pointsReward(50)
                .build());
            
            // Noduri pentru testul vampirului la final
            storyNodes.put(25, new TransilvaniaStoryNode.Builder(25,
                "După toate aventurile tale prin Transilvania, a venit momentul să descoperi ce impact au avut alegerile tale. " +
                "Privind înapoi la călătoria ta, te întrebi ce ai devenit după întâlnirea cu forțele întunericului.\n\n" +
                "În fața ta se află trei obiecte, fiecare reprezentând o cale diferită. Sângele, simbolizând acceptarea completă " +
                "a naturii vampirice. Crucifixul, reprezentând rezistența și lupta împotriva întunericului. Și luna, simbolizând " +
                "echilibrul fragil între două lumi.\n\n" +
                "Alegerea pe care o faci acum va defini cine ești cu adevărat și ce cale vei urma în viitor.")
                .title("Testul Vampirului")
                .context("Fiecare obiect reprezintă o parte din tine. Care te atrage cel mai mult?")
                .build());
            
            // Nodul 100: Butonul de încheiere
            storyNodes.put(100, new TransilvaniaStoryNode.Builder(100,
                "Mulțumim că ai explorat legendele Transilvaniei și misterele Contelui Dracula!\n\n" +
                "Călătoria ta prin tărâmul întunericului s-a încheiat, dar amintirile și experiențele dobândite te vor însoți " +
                "mult timp de acum înainte. Poate într-o zi te vei întoarce să descoperi alte secrete ascunse în inima României.\n\n" +
                "Până atunci, ai grijă pe unde mergi noaptea și nu uita niciodată lecțiile învățate în umbra Castelului Bran.")
                .title("Călătoria s-a încheiat")
                .context("Finalul călătoriei tale prin Transilvania, dar poate începutul unei noi aventuri...")
                .build());
        } catch (Exception e) {
            Log.e("DraculaStoryViewModel", "Error initializing story: " + e.getMessage());
            // Create at least one node to prevent crashes
            storyNodes = new HashMap<>();
            storyNodes.put(0, new TransilvaniaStoryNode.Builder(0, 
                "A apărut o eroare la încărcarea poveștii. Te rugăm să încerci din nou.")
                .title("Eroare")
                .context("Încearcă să repornești aplicația.")
                .build());
        }
    }
    
    /**
     * Încarcă nodul curent
     */
    private void loadCurrentNode() {
        try {
            int index = currentSceneIndex.getValue() != null ? currentSceneIndex.getValue() : 0;
            
            if (index == -1) {
                // Sfârșitul poveștii - arată ecranul de final
                showFinalScreen.postValue(true);
                return;
            }
            
            if (index == 25) {
                // Arată testul vampir
                showVampireTest.postValue(true);
                return;
            }
            
            // Verifică dacă nodul este un final al poveștii (90-99)
            if (index >= 90 && index < 100) {
                finalizeStory(index);
                return;
            }
            
            // Verifică dacă nodul este ecranul de încheiere (100)
            if (index == 100) {
                showFinalScreen.postValue(true);
                return;
            }
            
            TransilvaniaStoryNode node = storyNodes.get(index);
            if (node == null) {
                // Arată testul vampir dacă nu mai avem noduri definite
                Log.w("DraculaStoryViewModel", "Story node not found for index: " + index + ", showing vampire test instead");
                showVampireTest.postValue(true);
                return;
            }
            
            currentNode.postValue(node);
            updateStoryFlags(index);
            
            // Dacă nodul oferă puncte, actualizează scorul
            if (node.getPointsReward() != 0) {
                addPoints(node.getPointsReward());
            }
        } catch (Exception e) {
            Log.e("DraculaStoryViewModel", "Error loading current node: " + e.getMessage());
            // Try to recover by showing the final screen
            showFinalScreen.postValue(true);
        }
    }
    
    /**
     * Finalizează povestea cu un anumit final
     */
    private void finalizeStory(int endingIndex) {
        TransilvaniaStoryNode endingNode = storyNodes.get(endingIndex);
        if (endingNode != null) {
            currentNode.setValue(endingNode);
            
            // Determină tipul finalului
            int endingType;
            if (endingIndex == 90) {
                endingType = ENDING_VAMPIRE;
                finalStoryOutcome.setValue("Ai devenit un vampir, o creatură a nopții.");
            } else if (endingIndex == 91) {
                endingType = ENDING_HUNTER;
                finalStoryOutcome.setValue("Ai devenit un vânător de vampiri, protejând umanitatea.");
            } else if (endingIndex == 92) {
                endingType = ENDING_DHAMPIR;
                finalStoryOutcome.setValue("Ai devenit un dhampir, jumătate om, jumătate vampir.");
            } else {
                endingType = ENDING_ESCAPE;
                finalStoryOutcome.setValue("Ai scăpat din Transilvania, dar experiențele te-au schimbat pentru totdeauna.");
            }
            
            // Adaugă puncte în funcție de final
            if (endingNode.getPointsReward() != 0) {
                addPoints(endingNode.getPointsReward());
            }
            
            // Arată ecranul de final după 5 secunde
            // Acest lucru se va face în Activity prin observarea LiveData
        }
    }
    
    /**
     * Avansează la următoarea scenă
     */
    public void moveToNextScene(int choice) {
        TransilvaniaStoryNode node = currentNode.getValue();
        if (node != null && node.getNextNodes() != null && choice >= 0 && choice < node.getNextNodes().length) {
            int nextSceneIndex = node.getNextNodes()[choice];
            currentSceneIndex.setValue(nextSceneIndex);
            loadCurrentNode();
        }
    }
    
    /**
     * Avansează la următoarea scenă folosind indexul butonului
     */
    public void moveToNextScene() {
        int index = currentSceneIndex.getValue() != null ? currentSceneIndex.getValue() : 0;
        currentSceneIndex.setValue(index + 1);
        loadCurrentNode();
    }
    
    /**
     * Adaugă puncte și actualizează mesajul de feedback
     */
    private void addPoints(int points) {
        Context context = getApplication().getApplicationContext();
        pointsManager.addPoints(context, "transilvania", points);
        
        int current = currentPoints.getValue() != null ? currentPoints.getValue() : 0;
        current += points;
        currentPoints.setValue(current);
        
        String message = points > 0 
            ? "Ai primit " + points + " puncte!" 
            : "Ai pierdut " + Math.abs(points) + " puncte!";
        feedbackMessage.setValue(message);
    }
    
    /**
     * Actualizează flagurile poveștii pe baza scenei curente
     */
    private void updateStoryFlags(int sceneIndex) {
        if (sceneIndex == 2 || sceneIndex == 7 || sceneIndex == 8) {
            hasMetDracula = true;
        }
        
        if (sceneIndex == 10 || sceneIndex == 16) {
            hasBeenBitten = true;
        }
        
        if (sceneIndex == 17 || sceneIndex == 19) {
            hasFoundBlood = true;
        }
        
        // Adăugăm logică pentru a găsi artefactul
        if (sceneIndex == 12 || sceneIndex == 18) {
            artifactPieces++;
            if (artifactPieces >= 3) {
                hasFoundArtifact = true;
            }
        }
    }
    
    /**
     * Procesează selecția testului vampir
     */
    public void selectVampireTest(int choice) {
        int storyEndingIndex;
        
        switch (choice) {
            case 1: // Blood - Final vampir
                storyEndingIndex = 90; // Final: Transformare în vampir
                break;
                
            case 2: // Crucifix - Final vânător
                storyEndingIndex = 91; // Final: Vânător de vampiri
                break;
                
            case 3: // Moon - Final dhampir
                storyEndingIndex = 92; // Final: Dhampir
                break;
                
            default: // Scăpare
                storyEndingIndex = 93; // Final: Scăpare
        }
        
        // Încarcă finalul corespunzător
        currentSceneIndex.setValue(storyEndingIndex);
        loadCurrentNode();
    }
    
    /**
     * Închide povestea și arată ecranul final
     */
    public void finishStory() {
        currentSceneIndex.setValue(100);
        loadCurrentNode();
    }
} 