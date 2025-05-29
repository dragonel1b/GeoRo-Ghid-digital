package com.example.myapplication.crisanausage;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrisanaStoryActivity extends AppCompatActivity {
    private PointsManager pointsManager;
    private TextView storyText;
    private LinearProgressIndicator progressBar;
    private MaterialButton nextButton;
    private MaterialButton prevButton;
    private TextView pageCounter;
    private int currentPage = 0;
    private CardView characterCard;
    private TextView characterName;
    private TextView characterDialog;
    private ImageView characterImage;
    private ImageView backgroundImage;
    
    // Adăugare pentru interactivitate
    private CardView choiceCard;
    private MaterialButton choice1Button;
    private MaterialButton choice2Button;
    private MediaPlayer mediaPlayer;
    private int bonusPoints = 0;
    private Map<Integer, Boolean> discoveredSecrets = new HashMap<>();
    private CardView secretInfoCard;
    private TextView secretInfoText;
    private ImageView secretInfoImage;
    
    // Cale alternativă a poveștii (pentru alegerile utilizatorului)
    private boolean visitedMountains = false;
    private boolean helpedLocalArtisan = false;
    private boolean learnedFolkDance = false;

    private final String[] storyPages = {
        "În inima Crișanei, unde apele termale aduc viață și culoare, se află o lume plină de povești și tradiții milenare. Aceasta este povestea lui Mihai, un tânăr pasionat de istorie, care pornește într-o călătorie de descoperire prin ținuturile Crișanei.",
        
        "Mihai era un student la arhitectură din București, fascinat de frumusețea clădirilor Art Nouveau din Oradea și de istoria Aradului. Într-o zi de vară, el decide să-și petreacă vacanța explorând ținuturile Crișanei, în căutarea unor povești și secrete arhitecturale.",
        
        "Prima oprire: Oradea, orașul de la granița de vest a României. 'Uau, ce frumusețe!' exclamă Mihai privind Palatul Vulturul Negru și clădirile elegante din centrul istoric. Ochii lui sunt atrași de un detaliu neobișnuit pe una din fațade - un simbol ciudat care pare să strălucească în lumina soarelui.",
        
        "În timp ce admira fațadele clădirilor, Mihai este abordat de Ana, o ghidă locală: 'Văd că ești impresionat de arhitectura noastră. Știai că Oradea are una dintre cele mai mari concentrații de clădiri Art Nouveau din Europa? Ai observat simbolul de pe fațadă? Nu mulți vizitatori îl remarcă...'",
        
        "Mihai și Ana pornesc împreună într-un tur al orașului. 'Fiecare clădire are propria poveste,' explică Ana. 'Arhitecții maghiari Franz Lobl și Jakob Stark au transformat orașul la începutul secolului XX, aducând spiritul Belle Époque în inima Crișanei. Dar cei care au ochi de văzut pot descoperi și alte mesaje în aceste ornamente.'",
        
        "Ana îl conduce pe Mihai la Palatul Moskovits Adolf. 'Vezi aceste detalii florale și fețele feminine sculptate? Aceste simboluri ale naturii și frumuseții sunt teme centrale în Art Nouveau. Dincolo de frumusețea lor, aceste clădiri ascund și secrete... Încearcă să atingi al treilea trandafir de pe fațadă.'",
        
        "'Știai că multe dintre aceste clădiri au pasaje secrete și camere ascunse? În timpul celui de-al Doilea Război Mondial, unele dintre ele au fost folosite pentru a ascunde familii persecutate,' îi șoptește Ana, privind în jur ca și cum ar verifica dacă cineva îi ascultă. 'Se spune că unele dintre aceste pasaje formează o rețea subterană care leagă punctele importante din oraș. Poate într-o zi vei descoperi intrarea...'",
        
        "A doua zi, Mihai trebuie să decidă unde să-și continue călătoria. Ana îi sugerează trei opțiuni fascinante: Băile Felix cu apele lor termale vindecătoare, un traseu prin satele tradiționale din Munții Apuseni sau o vizită la Arad, un oraș cu o istorie revoluționară bogată. 'Fiecare loc are propriile mistere,' îi spune ea cu un zâmbet enigmatic.",
        
        "Ajuns la Băile Felix, Mihai este uimit de frumusețea parcului natural și de temperatura plăcută a apelor termale, chiar și în aer liber. Aici întâlnește un bătrân înțelept, Domnul Petru, care îi povestește despre proprietățile vindecătoare ale apelor. Domnul Petru are un medalion ciudat la gât, care pare să strălucească când se apropie de apă.",
        
        "'Aceste ape au vindecat oameni de mii de ani,' spune bătrânul. 'Romanii le numeau «Apele Zeilor» și construiseră băi termale aici. Dacă vei sta liniștit în bazin, vei simți cum istoria curge prin venele pământului.' Bătrânul privește în jur și apoi adaugă în șoaptă: 'Dar nu doar romanii au lăsat urme aici. Sunt legende despre energii ancestrale mult mai vechi, care dau apelor puterea lor...'",
        
        "Mihai observă nuferii tropicali care cresc în bazinele cu apă termală. 'Este uimitor cum aceste plante pot supraviețui în aer liber, chiar și iarna,' îi explică Domnul Petru. 'Este singurul loc din Europa unde acest lucru este posibil. Privește atent la nuferi când se lasă seara - spun localnicii că la miezul nopții, în anumite perioade ale anului, ei încep să strălucească și să emită un sunet melodios. Puțini au norocul să vadă acest fenomen.'",
        
        "Domnul Petru îi arată lui Mihai o monedă veche romană găsită în apropierea izvorului termal. 'Unii spun că dacă pui o monedă în anumite locuri ale izvorului și îți dorești ceva din inimă, dorința ți se va îndeplini. Vrei să încerci?' Bătrânul îi întinde moneda lui Mihai și îi arată un loc specific: 'Aici, unde apa formează un mic vârtej. Dar atenție la ce îți dorești - apele aud și răspund, dar uneori într-un mod neașteptat.'",
        
        "Călătoria continuă spre Munții Apuseni, unde Mihai vizitează Peștera Urșilor. Ghidul local, Ionuț, îi arată fosilele de urs de cavernă vechi de zeci de mii de ani. Pe drum, Mihai zărește o lumină ciudată printre copaci, dar când încearcă să se apropie, aceasta dispare.",
        
        "'Această peșteră a fost descoperită întâmplător în 1975, când o explozie de la o carieră din apropiere a dezvăluit intrarea,' explică Ionuț. 'Este ca o capsulă a timpului, păstrând secretele Erei Glaciare. Dar localnicii știau de existența ei cu mult înainte. Bunicul meu spunea că bătrânii satului evitau zona, considerând-o un loc bântuit de spirite. Unii susțin că au auzit sunetele urșilor care au murit captivi aici.'",
        
        "În adâncul peșterii, camera reflectoarelor se stinge brusc pentru câteva momente. În întunericul absolut, Mihai aude un sunet ciudat, ca un mormăit îndepărtat, și simte o adiere rece pe ceafă. Când luminile se aprind din nou, Ionuț zâmbește misterios: 'Unii vizitatori spun că spiritele urșilor de peșteră încă bântuie aceste galerii. Tu ai auzit ceva? Unii susțin că cei care aud mormăitul sunt binecuvântați cu protecția spiritelor pădurii pentru tot restul vieții.'",
        
        "Continuându-și călătoria, Mihai decide să viziteze Aradul, un oraș cu o istorie bogată și un rol important în Revoluția de la 1848. 'Aradul este cunoscut ca orașul de pe Mureș și a fost un centru important al luptei pentru drepturi naționale,' îi explică Ana prin telefon. 'Există și un muzeu al vinului în apropiere, în podgoria Miniș-Măderat, dacă ești interesat să cunoști și această latură a regiunii.'",
        
        "În Arad, Mihai este întâmpinat de un ghid local pe nume Gabriel, care îl conduce spre centrul istoric. 'Știai că 13 generali ai Revoluției Maghiare din 1848-1849 au fost executați aici? Monumentul din Piața Reconcilierii Româno-Maghiare le este dedicat lor - sunt cunoscuți ca Martirii din Arad.' Gabriel face o pauză și adaugă: 'Unii localnici jură că în anumite nopți, când ceața coboară pe Mureș, se pot vedea siluetele generalilor plimbându-se pe malul râului.'",
        
        "Mihai admiră Palatul Cultural din Arad, o clădire impresionantă în stil eclectic, construită între 1911-1913. 'În interior se află Muzeul de Artă și Filarmonica,' explică Gabriel. 'Clădirea combină elemente de baroc, renascentiste și neoclasice - un adevărat festin vizual pentru un student la arhitectură ca tine! Dacă privești atent la ornamentele din fațadă, vei descoperi simboluri masonice. Se spune că unele elemente de decor conțin coduri și mesaje ascunse, vizibile doar pentru cei inițiați.'",
        
        "După vizita în Arad, Mihai ajunge în Țara Beiușului, o regiune etnografică cu sate tradiționale unde timpul pare să fi stat în loc. Aici, o bătrână țărancă, Mărioara, îl invită să vadă cum se țes covoarele tradiționale. Casa ei este plină de obiecte vechi, iar într-un colț se află o icoană veche care atrage atenția lui Mihai.",
        
        "'Fiecare model are o semnificație,' spune Mărioara, arătând spre motivele geometrice de pe război. 'Aceste simboluri vorbesc despre viață, natură și credințele noastre. Le transmitem din generație în generație. Unele modele sunt atât de vechi încât nimeni nu mai știe exact ce reprezintă, dar continuăm să le țesem, păstrând astfel o legătură cu strămoșii noștri. Bunica mea spunea că anumite modele au puterea de a proteja casa și familia.'",
        
        "Mărioara îi oferă lui Mihai o dilemă interesantă: poate să o ajute la țesutul unui covor, învățând un meșteșug ancestral, sau poate participa la hora satului care tocmai începe în piața centrală, unde tinerii din sat învață dansuri tradiționale. 'Ambele experiențe îți vor deschide sufletul către tradițiile noastre,' îi spune ea. 'Dar alege cu înțelepciune - fiecare cale îți va dezvălui o altă față a Crișanei.'",
        
        "Mihai este invitat la o sărbătoare locală, unde gustă din bucătăria tradițională a Crișanei. Pălinca de prune, gulașul și cozonacul secuiesc (kürtőskalács) îl încântă, în timp ce ascultă muzica populară locală. Un bătrân îi explică procesul de distilare a pălinicii: 'Secretul stă în fructele alese și în răbdare. Și, desigur, în respectarea ritualurilor transmise din tată în fiu. Fiecare familie are propria rețetă secretă.'",
        
        "Un bătrân din sat îi povestește lui Mihai despre legenda comorii ascunse din Crișana: 'Se spune că haiducul Pintea Viteazul ar fi ascuns o comoară în peșterile din Munții Apuseni. Mulți au căutat-o, dar nimeni nu a găsit-o. Poate pentru că nu aveau harta secretă...' bătrânul îi face cu ochiul lui Mihai și scoate un pergament vechi din buzunar. 'Sau poate pentru că comoara este protejată de un blestem. Se spune că doar cel cu inima curată poate să o găsească.'",
        
        "În ultima zi a călătoriei sale, Mihai se întoarce la Oradea pentru a vizita Cetatea Medievală. Ana îl așteaptă acolo: 'Ce ai învățat din călătoria ta prin Crișana?' îl întreabă ea. Pe unul dintre ziduri, Mihai observă un simbol asemănător cu cel pe care l-a văzut în prima zi, pe fațada clădirii din oraș.",
        
        "'Am descoperit că Crișana nu este doar un loc pe hartă, ci o stare de spirit,' răspunde Mihai. 'Este un mozaic de culturi și tradiții, unde trecutul și prezentul dansează împreună într-o armonie perfectă. Am întâlnit oameni minunați și am aflat povești fascinante. Dar simt că sunt încă multe mistere de descoperit, simboluri și conexiuni pe care abia încep să le înțeleg.'",
        
        "Ana îi dezvăluie lui Mihai un ultim secret: 'Călătoria ta abia a început. Crișana are multe alte comori ascunse care te așteaptă să le descoperi. Vei reveni să continui această aventură?' Ea îi înmânează un medalion vechi, similar cu cel purtat de Domnul Petru la Băile Felix. 'Acest obiect a trecut prin multe mâini de-a lungul secolelor. Acum îți aparține ție. Te va ajuta să găsești calea înapoi către noi, când va veni timpul.'",
        
        "În timp ce privește apusul de soare peste zidurile cetății, Mihai știe că va reveni în Crișana. Acum, el are o colecție de povești, prieteni noi și amintiri de neprețuit din această regiune magică a României. Medalionul pulsează cald în palma sa, ca și cum ar fi viu, conectându-l cu spiritul locului și cu toate misterele care îl așteaptă să fie descoperite în viitor."
    };

    private final String[] characterNames = {
        "Narator", "Narator", "Mihai", "Ana", "Ana", "Ana", "Ana", "Narator", 
        "Narator", "Domnul Petru", "Domnul Petru", "Domnul Petru", "Narator", 
        "Ionuț", "Ionuț", "Narator", "Gabriel", "Gabriel", "Mărioara", "Narator",
        "Narator", "Bătrânul satului", "Ana", "Mihai", "Ana", "Narator"
    };
    
    // Pagini alternative pentru decizii
    private final String[] mountainStoryPages = {
        "Mihai decide să exploreze traseul montan. Ana îi oferă o hartă și câteva sfaturi: 'Vizitează satul Remetea și cascada Bulbuci. Sunt locuri magice, dar puțini turiști ajung acolo. Ai grijă pe potecile de munte, uneori marcajele pot dispărea în mod misterios.'",
        
        "Drumul către Munții Apuseni este spectaculos. Pe măsură ce urcă, Mihai observă cum peisajul se transformă: pădurile devin mai dense, aerul mai proaspăt, iar casele mai răsfirate. Deodată, un cerb maiestuos îi taie calea și se oprește, privindu-l fix pentru câteva momente, înainte de a dispărea în vegetație.",
        
        "În satul Remetea, Mihai este întâmpinat de localnici cu pâine proaspătă și pălincă. 'Suntem obișnuiți să vedem foarte puțini vizitatori pe aici,' îi explică gazda sa. 'Cei care ajung sunt considerați oaspeți de onoare.' O bătrână din sat îi șoptește: 'Cerbul te-a ales. Este un semn bun. Te-a considerat demn să descoperi secretele muntelui.'",
        
        "Un bătrân din sat, Moș Gheorghe, se oferă să-i fie ghid până la cascada Bulbuci. 'Acolo se întâlnesc spiritele muntelui,' îi povestește bătrânul. 'Dacă ești atent, le poți auzi șoptind. Și dacă ești foarte norocos, poate vei vedea Lumina Pământului - un fenomen rar, când stâncile de lângă cascadă încep să strălucească în întuneric, ca niște licurici. Bunicul meu spunea că acesta e momentul când munții vorbesc cu cei care știu să asculte.'"
    };
    
    private final String[] artisanHelpPages = {
        "Mihai decide să o ajute pe Mărioara la țesutul covorului. Se așează lângă ea și, sub îndrumarea răbdătoare a bătrânei, învață să mânuiască suveica. 'Respiră încet și lasă mâinile să simtă ritmul țesutului. Este ca un dans între fire,' îi explică ea.",
        
        "'Ai mâini bune pentru meșteșug,' îl laudă Mărioara. 'Poate în tine se ascunde sufletul unui meșter popular din vechime. Țesutul nu e doar un meșteșug, ci o formă de magie - transformăm fire simple în povești vizibile. Fiecare covor este o hartă a sufletului celui care l-a creat.'",
        
        "După ore de muncă atentă, un mic fragment din covor începe să prindă formă sub mâinile lui Mihai. Mărioara îi explică semnificația fiecărui simbol: 'Acest romb reprezintă pământul fertil, iar spirala este simbolul vieții care se reînnoiește mereu. Crucea este protecție divină, iar șarpele ondulat simbolizează apa, fără de care viața nu ar exista. Când toate aceste simboluri sunt țesute împreună, ele formează o rugăciune vizibilă pentru prosperitate și protecție.'",
        
        "'Ține acest fragment de covor,' îi spune Mărioara când termină. 'Este lucrat de mâinile tale și conține energia ta. Te va proteja și îți va aminti mereu de Crișana. Când te vei simți pierdut sau vei avea nevoie de înțelepciune, privește modelele și ascultă ce-ți șoptesc ele. Bătrânii spun că un covor țesut cu intenție bună poate deveni un talisman puternic pentru cel care îl posedă.'"
    };
    
    private final String[] folkDancePages = {
        "Mihai alege să participe la hora satului. În piața centrală, tineri și bătrâni dansează împreună în ritmul muzicii tradiționale cântate de lăutari. În centrul horei se află un stâlp vechi, decorat cu panglici colorate, care pare să fie centrul gravitațional al întregii sărbători.",
        
        "O tânără fată pe nume Ileana îl invită în horă: 'Nu e greu, doar urmărește-mi pașii!' Încurajat, Mihai intră în cerc și încearcă să imite mișcările celorlalți. Treptat, simte cum ritmul muzicii începe să rezoneze cu bătăile inimii sale, iar picioarele par să se miște de la sine.",
        
        "După câteva încercări stângace care stârnesc râsete prietenești, Mihai începe să prindă ritmul. 'Bravo!' strigă lăutarul. 'Se vede că ai sânge de dansator!' În timp ce dansul devine mai intens, Mihai are o senzație ciudată - pentru câteva secunde, parcă timpul se dilată și poate vedea în jurul său siluete transparente dansând alături de participanți, ca niște ecouri din trecut.",
        
        "Până la sfârșitul serii, Mihai învață trei dansuri tradiționale: Roata, Ardeleana și Mânânțălu. 'Acum poți spune că ești adoptat de Crișana,' îi spune Ileana, oferindu-i o batistă brodată manual ca suvenir. 'Această batistă a fost brodată de bunica mea. Se spune că cine o poartă va găsi mereu drumul către casă, oricât de departe ar rătăci. Dansul conectează oamenii între ei, dar și cu pământul și cu strămoșii. Când dansezi, nu ești niciodată singur.'"
    };
    
    // Adăugăm noi informații secrete
    private final String[] secretInfoPages = {
        "NUFĂRUL TERMAL: Nymphaea lotus var. thermalis (nufărul termal) este o relicvă a erei terțiare și o specie endemică, existentă doar în apele termale de la Băile Felix și 1 Mai. Supraviețuiește în aer liber și iarna datorită apei cu temperatură constantă de 30°C. Localnicii povestesc că, în anumite nopți de vară, nuferii emit o lumină slabă, albăstruie, vizibilă doar celor cu inima pură.",
        
        "PEȘTERA URȘILOR: Descoperită accidental în 1975, adăpostește fosile de Ursus spelaeus (urs de cavernă), specie dispărută acum 15.000 de ani. Unele schelete sunt dispuse într-un mod care sugerează că urșii ar fi căutat adăpost în peșteră în timpul unei perioade glaciare și au rămas blocați. Cercetătorii au descoperit recent hieroglife misterioase pe unele dintre pereții peșterii, care nu pot fi atribuite niciunei civilizații cunoscute din zonă.",
        
        "ARHITECTURA ART NOUVEAU DIN ORADEA: Oradea are peste 100 de clădiri în stil Art Nouveau/Secession, fiind al doilea centru ca importanță după Budapesta în fostul Imperiu Austro-Ungar. Multe clădiri au fost proiectate de arhitecții Marcell Komor și Dezső Jakab. Documentele istorice indică faptul că arhitecții erau membri ai unor loje masonice și au încorporat simboluri și coduri secrete în ornamentele clădirilor, creând un mesaj ascuns care poate fi citit doar de cei care cunosc interpretarea corectă a simbolurilor.",
        
        "CETATEA ORADEA: Construită în stil Vauban (stea cu cinci colțuri), este una dintre cele mai mari și mai bine conservate cetăți medievale din Europa Centrală. A fost edificată pe locul unei vechi așezări din epoca bronzului și al unei fortărețe medievale timpurii. Săpăturile arheologice recente au scos la iveală un sistem de tuneluri care se extind mult dincolo de perimetrul cetății, unele ajungând până sub albia râului Crișul Repede. Destinația și scopul acestor tuneluri rămân un mister.",
        
        "KÜRTŐSKALÁCS: Cozonacul secuiesc este un desert tradițional specific zonei, preparat din aluat dulce de formă spiralată, copt pe un cilindru de lemn deasupra jarului, apoi tăvălit prin zahăr și scorțișoară. Numele înseamnă 'cozonac horn' datorită formei sale tubulare. Legenda spune că forma sa spiralată simbolizează drumul vieții și al timpului, iar procesul de pregătire era însoțit în vechime de rugăciuni și incantații pentru prosperitate și sănătate.",
        
        "MARTIRII DIN ARAD: În 6 octombrie 1849, treisprezece generali ai armatei revoluționare maghiare au fost executați în Arad, devenind simboluri ale luptei pentru libertate. Monumentul din Piața Reconcilierii Româno-Maghiare, cunoscut și ca 'Statuia Libertății', comemorează aceste personalități istorice. Localnicii susțin că în noaptea de 6 octombrie, în fiecare an, la miezul nopții, se pot auzi pași de marș și comenzi militare șoptite în jurul monumentului, iar unii afirmă că au văzut siluete în uniforme militare de epocă patrulând în ceață.",
        
        "PODGORIA ARADULUI: Regiunea viticolă a Aradului, cunoscută ca 'Podgoria Aradului', este una dintre cele mai vechi din România, cu o tradiție de peste 1000 de ani. Climatul favorabil și solul special oferă vinurilor locale un caracter unic, fiind apreciate la nivel internațional. Viticultorii din zonă păstrează metode tradiționale secrete de vinificație, transmise din generație în generație, inclusiv ritualuri de binecuvântare a viei și tehnici speciale de fermentare care implică vase de lemn vechi de sute de ani.",
        
        "BISERICA DE LEMN DIN CIZER: Construită în 1773, biserica de lemn din Cizer este un exemplu remarcabil de arhitectură religioasă tradițională din Crișana. Lăcașul este construit integral din lemn, fără a folosi niciun cui de metal, tehnica de îmbinare fiind transmisă din generație în generație. Pictura interioară conține simboluri pre-creștine subtil integrate în iconografia ortodoxă, reprezentând o fuziune unică între credințele străvechi și creștinism.",
        
        "CODEX ROHONCZI: Un manuscris misterios descoperit în secolul al XIX-lea, despre care unii cercetători cred că ar fi fost creat în zona Crișanei. Scris într-o limbă și un alfabet necunoscute, conținând peste 400 de pagini cu text și ilustrații, manuscrisul a rămas nedescifrat până în prezent. Anumite teorii sugerează că ar conține cunoștințe ezoterice ale unei civilizații pre-dacice care ar fi existat în regiunea Crișanei."
    };

    private List<android.graphics.Rect> imageClickableAreas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crisana_story);

        // Initialize points manager
        pointsManager = PointsManager.getInstance(this);

        // Initialize views
        storyText = findViewById(R.id.storyText);
        progressBar = findViewById(R.id.storyProgress);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        pageCounter = findViewById(R.id.pageCounter);
        characterCard = findViewById(R.id.characterCard);
        characterName = findViewById(R.id.characterName);
        characterDialog = findViewById(R.id.characterDialog);
        characterImage = findViewById(R.id.characterImage);
        backgroundImage = findViewById(R.id.backgroundImage);
        
        // Inițializare componente pentru interactivitate
        choiceCard = findViewById(R.id.choiceCard);
        choice1Button = findViewById(R.id.choice1Button);
        choice2Button = findViewById(R.id.choice2Button);
        secretInfoCard = findViewById(R.id.secretInfoCard);
        secretInfoText = findViewById(R.id.secretInfoText);
        secretInfoImage = findViewById(R.id.secretInfoImage);

        // Set up initial page
        progressBar.setMax(storyPages.length - 1);
        updateStoryPage();
        
        // Ascunde cardul de alegeri inițial
        choiceCard.setVisibility(View.GONE);
        secretInfoCard.setVisibility(View.GONE);

        // Set up button listeners
        nextButton.setOnClickListener(v -> {
            if (currentPage < storyPages.length - 1) {
                currentPage++;
                updateStoryPage();
                playPageSound();
            } else {
                // Story completed, award points
                int totalPoints = 100 + bonusPoints;
                pointsManager.addPoints(this, "crisana", totalPoints);
                
                // Show completion dialog
                showCompletionDialog(totalPoints);
            }
        });

        prevButton.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                updateStoryPage();
                playPageSound();
            }
        });
        
        // Configurare butoane de alegere
        choice1Button.setOnClickListener(v -> handleChoice(1));
        choice2Button.setOnClickListener(v -> handleChoice(2));
        
        // Configurare card informații secrete
        secretInfoCard.setOnClickListener(v -> {
            secretInfoCard.setVisibility(View.GONE);
            // Acordă puncte bonus pentru descoperirea secretului
            if (!discoveredSecrets.containsKey(currentPage) || !discoveredSecrets.get(currentPage)) {
                bonusPoints += 10;
                discoveredSecrets.put(currentPage, true);
                Toast.makeText(this, "+10 puncte bonus pentru descoperirea unui secret!", Toast.LENGTH_SHORT).show();
            }
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Cleanup și eliberare resurse
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            finish();
        });
        
        // Adaugă gesturi pentru interactivitate
        setupImageClickListeners();
    }
    
    private void setupImageClickListeners() {
        // Ascultător pentru click pe imagine de fundal pentru a descoperi secrete
        backgroundImage.setOnClickListener(v -> {
            // Verifică dacă pagina curentă conține secrete care pot fi descoperite
            checkForSecrets();
        });
    }
    
    private void checkForSecrets() {
        // Verifică dacă există secrete de descoperit pe pagina curentă
        switch (currentPage) {
            case 2: // Simbolul ciudat de pe fațadă
                if (!discoveredSecrets.containsKey(currentPage)) {
                    imageClickableAreas.add(new android.graphics.Rect(100, 200, 300, 400)); // Zona clickabilă
                    Toast.makeText(this, "Simți că ceva îți atrage atenția pe această pagină...", Toast.LENGTH_SHORT).show();
                }
                break;
            case 5: // Trandafirul de pe fațada Palatului Moskovits
                if (!discoveredSecrets.containsKey(currentPage)) {
                    imageClickableAreas.add(new android.graphics.Rect(200, 300, 400, 500)); // Zona clickabilă
                    Toast.makeText(this, "Ana a menționat ceva despre al treilea trandafir...", Toast.LENGTH_SHORT).show();
                }
                break;
            case 8: // Medalionul lui Domnul Petru
                if (!discoveredSecrets.containsKey(currentPage)) {
                    imageClickableAreas.add(new android.graphics.Rect(250, 350, 450, 550)); // Zona clickabilă
                }
                break;
            case 10: // Nuferii care strălucesc noaptea
                if (!discoveredSecrets.containsKey(currentPage)) {
                    imageClickableAreas.add(new android.graphics.Rect(150, 250, 350, 450)); // Zona clickabilă
                }
                break;
            case 12: // Lumina ciudată din pădure
                if (!discoveredSecrets.containsKey(currentPage)) {
                    imageClickableAreas.add(new android.graphics.Rect(50, 150, 250, 350)); // Zona clickabilă
                }
                break;
            case 17: // Icoana veche din casa Mărioarei
                if (!discoveredSecrets.containsKey(currentPage)) {
                    imageClickableAreas.add(new android.graphics.Rect(300, 400, 500, 600)); // Zona clickabilă
                }
                break;
            case 19: // Procesul de distilare a pălinicii
                if (!discoveredSecrets.containsKey(currentPage)) {
                    imageClickableAreas.add(new android.graphics.Rect(200, 300, 400, 500)); // Zona clickabilă
                }
                break;
            case 22: // Simbolul de pe zidul cetății
                if (!discoveredSecrets.containsKey(currentPage)) {
                    imageClickableAreas.add(new android.graphics.Rect(350, 450, 550, 650)); // Zona clickabilă
                }
                break;
            case 24: // Medalionul de la Ana
                if (!discoveredSecrets.containsKey(currentPage)) {
                    imageClickableAreas.add(new android.graphics.Rect(250, 350, 450, 550)); // Zona clickabilă
                }
                break;
            default:
                // Pagina nu conține secrete
                imageClickableAreas.clear();
                break;
        }
    }
    
    private void showSecretInfo(int infoIndex, int imageResource) {
        // Afișează cardul cu informații secrete
        secretInfoCard.setVisibility(View.VISIBLE);
        secretInfoText.setText(secretInfoPages[infoIndex]);
        secretInfoImage.setImageResource(imageResource);
        
        // Adaugă animație de fade in
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        secretInfoCard.startAnimation(fadeIn);
    }

    private void updateStoryPage() {
        // Actualizează progressBar
        progressBar.setProgress(currentPage);
        
        // Actualizează contor pagină
        String pageText = (currentPage + 1) + "/" + storyPages.length;
        pageCounter.setText(pageText);
        
        // Verifică dacă suntem pe o pagină de poveste alternativă pentru munți
        if (visitedMountains && currentPage >= 8 && currentPage <= 11) {
            int mountainPageIndex = currentPage - 8;
            if (mountainPageIndex < mountainStoryPages.length) {
                storyText.setText(mountainStoryPages[mountainPageIndex]);
                updateCharacterImage();
                updateBackgroundImage();
                
                // Ascunde cardul de alegere pe paginile de poveste alternativă
                choiceCard.setVisibility(View.GONE);
                return;
            }
        }
        
        // Verifică dacă suntem pe o pagină de poveste alternativă pentru ajutorul artizanului
        if (helpedLocalArtisan && currentPage >= 17 && currentPage <= 20) {
            int artisanPageIndex = currentPage - 17;
            if (artisanPageIndex < artisanHelpPages.length) {
                storyText.setText(artisanHelpPages[artisanPageIndex]);
                updateCharacterImage();
                updateBackgroundImage();
                
                // Ascunde cardul de alegere pe paginile de poveste alternativă
                choiceCard.setVisibility(View.GONE);
                return;
            }
        }
        
        // Verifică dacă suntem pe o pagină de poveste alternativă pentru dansul popular
        if (learnedFolkDance && currentPage >= 17 && currentPage <= 20) {
            int dancePageIndex = currentPage - 17;
            if (dancePageIndex < folkDancePages.length) {
                storyText.setText(folkDancePages[dancePageIndex]);
                updateCharacterImage();
                updateBackgroundImage();
                
                // Ascunde cardul de alegere pe paginile de poveste alternativă
                choiceCard.setVisibility(View.GONE);
                return;
            }
        }
        
        // Afișează textul normal al poveștii
        storyText.setText(storyPages[currentPage]);
        
        // Actualizează imaginea caracterului și fundalul
        updateCharacterImage();
        updateBackgroundImage();
        
        // Afișează animation de apariție a textului
        animateTextAppearance();
        
        // Gestionează paginile cu alegeri
        if (currentPage == 7) { // Pagina cu alegerea munților sau băilor termale
            showChoiceCard("Explorează Munții Apuseni", "Vizitează Băile Felix");
        } else if (currentPage == 17 && !helpedLocalArtisan && !learnedFolkDance) { // Pagina cu alegerea activității în sat
            showChoiceCard("Ajută la țesutul covorului", "Participă la hora satului");
        } else {
            choiceCard.setVisibility(View.GONE);
        }
    }
    
    private void animateTextAppearance() {
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        storyText.startAnimation(fadeIn);
    }
    
    private void showChoiceCard(String choice1Text, String choice2Text) {
        // Configurează textul butoanelor
        choice1Button.setText(choice1Text);
        choice2Button.setText(choice2Text);
        
        // Arată cardul de alegeri
        choiceCard.setVisibility(View.VISIBLE);
        
        // Adaugă animație de fade in
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(800);
        choiceCard.startAnimation(fadeIn);
    }
    
    private void handleChoice(int choiceNumber) {
        if (currentPage == 7) { // Alegere pentru Munți vs. Băi termale
            if (choiceNumber == 1) { // Alegere pentru Munți Apuseni
                visitedMountains = true;
                // Puncte bonus pentru alegerea traseului mai puțin cunoscut
                bonusPoints += 15;
                Toast.makeText(this, "+15 puncte bonus pentru explorarea unui traseu mai puțin cunoscut!", Toast.LENGTH_SHORT).show();
                playSound(R.raw.choice_made);
            } else { // Alegere pentru Băile Felix
                visitedMountains = false;
                playSound(R.raw.choice_made);
            }
            // Avansează la următoarea pagină
            currentPage++;
            updateStoryPage();
        } else if (currentPage == 17) { // Alegere pentru activitatea în sat
            if (choiceNumber == 1) { // Alegere pentru ajutorul la țesut
                helpedLocalArtisan = true;
                learnedFolkDance = false;
                // Puncte bonus pentru învățarea meșteșugului
                bonusPoints += 20;
                Toast.makeText(this, "+20 puncte bonus pentru învățarea unui meșteșug tradițional!", Toast.LENGTH_SHORT).show();
                playSound(R.raw.choice_made);
            } else { // Alegere pentru hora satului
                helpedLocalArtisan = false;
                learnedFolkDance = true;
                // Puncte bonus pentru participarea la dans
                bonusPoints += 15;
                Toast.makeText(this, "+15 puncte bonus pentru învățarea dansurilor populare!", Toast.LENGTH_SHORT).show();
                playSound(R.raw.choice_made);
            }
            // Avansează la următoarea pagină
            currentPage++;
            updateStoryPage();
        }
    }
    
    private void playSound(int soundResourceId) {
        try {
            // Eliberează resursele player-ului dacă există
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            
            // Inițializează noul player
            mediaPlayer = MediaPlayer.create(this, soundResourceId);
            
            // Setează volumul
            mediaPlayer.setVolume(0.7f, 0.7f);
            
            // Start playback
            mediaPlayer.start();
            
            // Eliberează resursele după terminarea redării
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void playPageSound() {
        // Eliberează resursele playerului anterior dacă există
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        // Adăugăm sunete specifice pentru momente cheie din poveste
        switch (currentPage) {
            case 0: // Introducere
                playSound(R.raw.nature_sound);
                break;
            case 2: // Oradea
                playSound(R.raw.city_ambience);
                break;
            case 8: // Băile Felix
                playSound(R.raw.water_ambient);
                break;
            case 12: // Peștera Urșilor
                playSound(R.raw.cave_ambient);
                break;
            case 14: // Momentul când luminile se sting în peșteră
                playSound(R.raw.bear_growl);
                break;
            case 19: // Sărbătoarea locală
                playSound(R.raw.folk_music);
                break;
            case 24: // Final cu medalionul
                playSound(R.raw.magical_chimes);
                break;
            default:
                // Sunet ambient general pentru celelalte pagini
                playSound(R.raw.page_turn);
                break;
        }
    }
    
    private void showCompletionDialog(int totalPoints) {
        // Crearea unui dialog personalizat pentru finalizarea poveștii
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View customView = getLayoutInflater().inflate(R.layout.dialog_story_completion, null);
        builder.setView(customView);
        
        // Inițializarea componentelor din dialog
        TextView pointsTextView = customView.findViewById(R.id.pointsEarned);
        TextView secretsFoundTextView = customView.findViewById(R.id.secretsFound);
        Button shareButton = customView.findViewById(R.id.shareButton);
        Button closeButton = customView.findViewById(R.id.closeButton);
        
        // Setarea informațiilor
        pointsTextView.setText(String.format("%d puncte", totalPoints));
        int secretsFound = discoveredSecrets.size();
        int totalSecrets = 9; // Total secrete ascunse în poveste
        secretsFoundTextView.setText(String.format("%d din %d", secretsFound, totalSecrets));
        
        // Mesaj special în funcție de câte secrete a descoperit utilizatorul
        TextView specialMessageTextView = customView.findViewById(R.id.specialMessage);
        if (secretsFound == totalSecrets) {
            specialMessageTextView.setText("Felicitări! Ai descoperit toate secretele Crișanei și ai devenit un adevărat explorator al misterelor acestei regiuni!");
            // Acordă un bonus special pentru descoperirea tuturor secretelor
            pointsManager.addPoints(this, "crisana", 50);
        } else if (secretsFound >= totalSecrets / 2) {
            specialMessageTextView.setText("Foarte bine! Ai descoperit multe din secretele Crișanei, dar mai sunt câteva mistere care te așteaptă să le descoperi!");
        } else {
            specialMessageTextView.setText("Ai descoperit câteva secrete ale Crișanei, dar această regiune are mult mai multe mistere ascunse. Revino pentru a descoperi mai multe!");
        }
        
        // Crearea și afișarea dialogului
        AlertDialog dialog = builder.create();
        
        // Setarea acțiunilor pentru butoane
        shareButton.setOnClickListener(v -> {
            // Implementăm funcționalitatea de share
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Aventura mea în Crișana");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Tocmai am terminat o aventură fascinantă prin Crișana în aplicația RomApp! Am descoperit " + 
                                secretsFound + " secrete și am câștigat " + totalPoints + " puncte! Descoperă și tu misterele Crișanei!");
            startActivity(Intent.createChooser(shareIntent, "Împărtășește aventura ta"));
        });
        
        closeButton.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        
        dialog.show();
    }

    private void updateCharacterImage() {
        // Afișează sau ascunde cardul personajului în funcție de pagină
        if (currentPage < characterNames.length) {
            String name = characterNames[currentPage];
            characterName.setText(name);
            characterDialog.setText(storyPages[currentPage]);
            characterCard.setVisibility(View.VISIBLE);
            
            // Setare imagine caracter în funcție de nume
            switch (name) {
                case "Narator":
                    characterImage.setImageResource(R.drawable.ic_narrator);
                    break;
                case "Mihai":
                    characterImage.setImageResource(R.drawable.character_mihai);
                    break;
                case "Ana":
                    characterImage.setImageResource(R.drawable.character_ana);
                    break;
                case "Domnul Petru":
                    characterImage.setImageResource(R.drawable.character_petru);
                    break;
                case "Ionuț":
                    characterImage.setImageResource(R.drawable.character_ionut);
                    break;
                case "Mărioara":
                    characterImage.setImageResource(R.drawable.character_marioara);
                    break;
                case "Bătrânul satului":
                    characterImage.setImageResource(R.drawable.character_batran);
                    break;
                case "Gabriel":
                    characterImage.setImageResource(R.drawable.character_gabriel);
                    break;
                default:
                    characterImage.setImageResource(R.drawable.ic_narrator);
                    break;
            }
        } else {
            characterCard.setVisibility(View.GONE);
        }
    }

    private void updateBackgroundImage() {
        // Schimbă imaginea de fundal în funcție de pagină
        switch (currentPage) {
            case 0:
            case 1:
            case 24:
            case 25:
                backgroundImage.setImageResource(R.drawable.bg_crisana_landscape);
                break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                backgroundImage.setImageResource(R.drawable.bg_oradea);
                break;
            case 7:
                backgroundImage.setImageResource(R.drawable.bg_crisana_path);
                break;
            case 8:
            case 9:
            case 10:
            case 11:
                backgroundImage.setImageResource(R.drawable.bg_baile_felix);
                break;
            case 12:
            case 13:
            case 14:
            case 15:
                backgroundImage.setImageResource(R.drawable.bg_pestera_ursilor);
                break;
            case 16:
            case 17:
            case 18:
                backgroundImage.setImageResource(R.drawable.bg_arad);
                break;
            case 19:
            case 20:
            case 21:
            case 22:
                backgroundImage.setImageResource(R.drawable.bg_village);
                break;
            case 23:
                backgroundImage.setImageResource(R.drawable.bg_cetatea_oradea);
                break;
            default:
                backgroundImage.setImageResource(R.drawable.baia_mare);
                break;
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Eliberează resursele media player-ului când activitatea intră în pauză
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Eliberează resursele media player-ului când activitatea este distrusă
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
} 