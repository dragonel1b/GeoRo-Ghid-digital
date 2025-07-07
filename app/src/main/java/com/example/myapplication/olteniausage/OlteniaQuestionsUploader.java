package com.example.myapplication.olteniausage;

import android.content.Context;
import android.util.Log;
import com.example.myapplication.models.FirestoreQuestionModel;
import com.example.myapplication.repository.FirestoreQuestionRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Clasă pentru încărcarea întrebărilor specifice despre Oltenia în Firebase
 */
public class OlteniaQuestionsUploader {
    private static final String TAG = "OlteniaQuestionsUploader";
    private static final String REGION = "oltenia";
    private static final String GAME_TYPE = "quiz";
    
    private final FirestoreQuestionRepository repository;
    private final Context context;
    
    public OlteniaQuestionsUploader(Context context) {
        this.context = context;
        this.repository = FirestoreQuestionRepository.getInstance();
    }
    
    /**
     * Încarcă toate întrebările despre Oltenia în Firebase
     */
    public CompletableFuture<Void> uploadAllQuestions() {
        Log.d(TAG, "🚀 Începe încărcarea întrebărilor despre Oltenia în Firebase...");
        
        List<FirestoreQuestionModel> allQuestions = new ArrayList<>();
        
        // Adăugăm toate categoriile de întrebări
        allQuestions.addAll(createHistoryQuestions());
        allQuestions.addAll(createGeographyQuestions());
        allQuestions.addAll(createCultureQuestions());
        allQuestions.addAll(createArchitectureQuestions());
        allQuestions.addAll(createGastronomyQuestions());
        allQuestions.addAll(createPersonalitiesQuestions());
        allQuestions.addAll(createNatureQuestions());
        allQuestions.addAll(createLegendsQuestions());
        
        Log.d(TAG, "📊 Total întrebări pentru încărcare: " + allQuestions.size());
        
        // Încărcăm toate întrebările în Firebase
        return uploadQuestionsBatch(allQuestions);
    }
    
    /**
     * Încarcă o listă de întrebări în Firebase în batches
     */
    private CompletableFuture<Void> uploadQuestionsBatch(List<FirestoreQuestionModel> questions) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        // Împărțim în batch-uri de câte 10 pentru a evita limitele Firebase
        List<List<FirestoreQuestionModel>> batches = createBatches(questions, 10);
        
        uploadBatchRecursive(batches, 0, future);
        
        return future;
    }
    
    /**
     * Încarcă batch-urile recursiv
     */
    private void uploadBatchRecursive(List<List<FirestoreQuestionModel>> batches, int currentBatch, 
                                    CompletableFuture<Void> mainFuture) {
        if (currentBatch >= batches.size()) {
            Log.d(TAG, "✅ Toate întrebările au fost încărcate cu succes!");
            mainFuture.complete(null);
            return;
        }
        
        List<FirestoreQuestionModel> batch = batches.get(currentBatch);
        Log.d(TAG, "📤 Încărcare batch " + (currentBatch + 1) + "/" + batches.size() + 
                " (" + batch.size() + " întrebări)");
        
        // Încărcăm batch-ul curent
        uploadSingleBatch(batch)
            .thenRun(() -> {
                Log.d(TAG, "✅ Batch " + (currentBatch + 1) + " încărcat cu succes");
                // Continuăm cu următorul batch
                uploadBatchRecursive(batches, currentBatch + 1, mainFuture);
            })
            .exceptionally(throwable -> {
                Log.e(TAG, "❌ Eroare la încărcarea batch-ului " + (currentBatch + 1), throwable);
                mainFuture.completeExceptionally(throwable);
                return null;
            });
    }
    
    /**
     * Încarcă un singur batch de întrebări
     */
    private CompletableFuture<Void> uploadSingleBatch(List<FirestoreQuestionModel> questions) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        List<CompletableFuture<Void>> uploadTasks = new ArrayList<>();
        
        for (FirestoreQuestionModel question : questions) {
            CompletableFuture<Void> task = new CompletableFuture<>();
            uploadTasks.add(task);
            
            repository.addQuestion(question)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "✅ Întrebare încărcată: " + question.getQuestion().substring(0, 
                            Math.min(50, question.getQuestion().length())) + "...");
                    task.complete(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Eroare la încărcarea întrebării: " + question.getQuestion(), e);
                    task.completeExceptionally(e);
                });
        }
        
        // Așteptăm să se termine toate task-urile din batch
        CompletableFuture.allOf(uploadTasks.toArray(new CompletableFuture[0]))
            .thenRun(() -> future.complete(null))
            .exceptionally(throwable -> {
                future.completeExceptionally(throwable);
                return null;
            });
        
        return future;
    }
    
    /**
     * Creează batch-uri din lista de întrebări
     */
    private List<List<FirestoreQuestionModel>> createBatches(List<FirestoreQuestionModel> questions, int batchSize) {
        List<List<FirestoreQuestionModel>> batches = new ArrayList<>();
        
        for (int i = 0; i < questions.size(); i += batchSize) {
            int end = Math.min(i + batchSize, questions.size());
            batches.add(new ArrayList<>(questions.subList(i, end)));
        }
        
        return batches;
    }
    
    // === CATEGORII DE ÎNTREBĂRI ===
    
    /**
     * Întrebări despre istoria Olteniei
     */
    private List<FirestoreQuestionModel> createHistoryQuestions() {
        List<FirestoreQuestionModel> questions = new ArrayList<>();
        
        questions.add(new FirestoreQuestionModel(
            "În ce secol a fost construită Biserica Domnească din Craiova?",
            "Secolul al XVI-lea",
            Arrays.asList("Secolul al XV-lea", "Secolul al XVII-lea", "Secolul al XVIII-lea"),
            "Biserica Domnească din Craiova a fost construită între 1552-1554, în timpul domniei lui Radu Paisie.",
            "Gândește-te la perioada în care Oltenia era sub influența Țării Românești.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Care a fost primul nume al orașului Craiova?",
            "Craiu Nou",
            Arrays.asList("Drobeta", "Pelendava", "Sucidava"),
            "Numele Craiova derivă din 'Craiu Nou', fiind menționat pentru prima dată în documentele din secolul al XIV-lea.",
            "Numele vine de la cuvântul 'craiu' care înseamnă rege sau domn.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Cine a fost Tudor Vladimirescu?",
            "Liderul revoluției din 1821",
            Arrays.asList("Un voievod muntean", "Un negustor din Craiova", "Un mitropolit ortodox"),
            "Tudor Vladimirescu a fost pandur și apoi căpitan, care a condus revoluția din 1821 împotriva fanarioților și turcilor.",
            "A fost din Vladimiri, Gorj, și a luptat pentru drepturile țăranilor.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "În ce an a fost înființată Universitatea din Craiova?",
            "1966",
            Arrays.asList("1948", "1956", "1974"),
            "Universitatea din Craiova a fost înființată în 1966 și poartă numele de Universitatea din Craiova.",
            "A fost înființată în perioada comunistă, în anii '60.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Care a fost numele antic al orașului Drobeta-Turnu Severin?",
            "Drobeta",
            Arrays.asList("Tibiscum", "Dierna", "Sucidava"),
            "Drobeta a fost o cetate dacică și apoi romană, situată la Dunăre, lângă podul lui Traian.",
            "Era o cetate importantă pe limes-ul dunărean al Imperiului Roman.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        return questions;
    }
    
    /**
     * Întrebări despre geografia Olteniei
     */
    private List<FirestoreQuestionModel> createGeographyQuestions() {
        List<FirestoreQuestionModel> questions = new ArrayList<>();
        
        questions.add(new FirestoreQuestionModel(
            "Care este cel mai înalt vârf din Munții Parâng?",
            "Vârful Parângul Mare",
            Arrays.asList("Vârful Mohoru", "Vârful Setea Mare", "Vârful Cârja"),
            "Parângul Mare are 2.519 metri altitudine și este cel mai înalt vârf din masivul Parâng.",
            "Este situat în partea de nord a Olteniei, la granița cu Transilvania.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Prin care orașe trece râul Jiu?",
            "Târgu Jiu și Craiova",
            Arrays.asList("Slatina și Caracal", "Râmnicu Vâlcea și Pitești", "Drobeta și Calafat"),
            "Râul Jiu izvorăște din Munții Parâng și traversează Oltenia de la nord la sud, trecând prin Târgu Jiu și Craiova.",
            "Este unul dintre principalele râuri ale Olteniei.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Care este cel mai mare lac din Oltenia?",
            "Lacul de acumulare Gura Apei",
            Arrays.asList("Lacul Izvorul Muntelui", "Lacul Vidraru", "Lacul Pecineagu"),
            "Lacul de acumulare Gura Apei, pe râul Lotru, este cel mai mare lac artificial din județul Vâlcea.",
            "Se află în zona montană, fiind folosit pentru producerea de energie electrică.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Care este reședința județului Gorj?",
            "Târgu Jiu",
            Arrays.asList("Craiova", "Motru", "Rovinari"),
            "Târgu Jiu este reședința județului Gorj și unul dintre cele mai importante orașe din Oltenia.",
            "Este cunoscut pentru ansamblul sculptural al lui Constantin Brâncuși.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Pe care râu se află orașul Calafat?",
            "Dunărea",
            Arrays.asList("Jiul", "Oltul", "Argeșul"),
            "Calafat este un port important la Dunăre, în sudul Olteniei, cu traversare către Bulgaria.",
            "Este situat la granița cu Bulgaria, pe malul Dunării.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        return questions;
    }
    
    /**
     * Întrebări despre cultura Olteniei
     */
    private List<FirestoreQuestionModel> createCultureQuestions() {
        List<FirestoreQuestionModel> questions = new ArrayList<>();
        
        questions.add(new FirestoreQuestionModel(
            "Care este dansul tradițional specific Olteniei?",
            "Olteneasca",
            Arrays.asList("Hora", "Căluș", "Brâul"),
            "Olteneasca este un dans popular specific regiunii Oltenia, caracterizat prin ritmul viu și mișcările energice.",
            "Este unul dintre dansurile reprezentative ale folclorului românesc.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Ce festival important se desfășoară anual la Târgu Jiu?",
            "Festivalul International Brâncuși",
            Arrays.asList("Festivalul George Enescu", "Festivalul Maria Tănase", "Festivalul Ion Creangă"),
            "Festivalul Internațional Brâncuși celebrează opera sculptorului Constantin Brâncuși, născut în Hobița, Gorj.",
            "Se desfășoară în memoria celui mai important sculptor român.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Care este costumul popular tradițional al femeilor din Oltenia?",
            "Ie cu mâneci largi și fotă",
            Arrays.asList("Cămeșă și poloboc", "Ii și catrinți", "Cămașă și fustă plisată"),
            "Costumul popular oltean se caracterizează prin ie cu mâneci largi, fotă și diferite ornamente specifice.",
            "Fota este o piesă vestimentară specifică zonei de câmpie.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Ce muzeu important există în Craiova?",
            "Muzeul de Artă",
            Arrays.asList("Muzeul Satului", "Muzeul de Istorie Naturală", "Muzeul Tehnicii"),
            "Muzeul de Artă din Craiova este unul dintre cele mai importante muzee de artă din România, cu colecții valoroase.",
            "Găzduiește opere importante ale artei românești și universale.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Care este cea mai cunoscută baladă populară din Oltenia?",
            "Miorița",
            Arrays.asList("Toma Alimoș", "Novac și Kira Chiralina", "Meșterul Manole"),
            "Deși nu este specifică doar Olteniei, Miorița este o baladă fundamentală în folclorul din această regiune.",
            "Este considerată una dintre capodoperele literaturii populare românești.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        return questions;
    }
    
    /**
     * Întrebări despre arhitectura Olteniei
     */
    private List<FirestoreQuestionModel> createArchitectureQuestions() {
        List<FirestoreQuestionModel> questions = new ArrayList<>();
        
        questions.add(new FirestoreQuestionModel(
            "Cine a proiectat ansamblul sculptural din Târgu Jiu?",
            "Constantin Brâncuși",
            Arrays.asList("Auguste Rodin", "Dimitrie Paciurea", "Ion Jalea"),
            "Constantin Brâncuși a creat celebrul ansamblu sculptural din Târgu Jiu în memoria eroilor din Primul Război Mondial.",
            "Include Coloana fără Sfârșit, Masa Tăcerii și Poarta Sărutului.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Ce înălțime are Coloana fără Sfârșit din Târgu Jiu?",
            "29,35 metri",
            Arrays.asList("25,5 metri", "32,8 metri", "27,2 metri"),
            "Coloana fără Sfârșit are 29,35 metri înălțime și este realizată din fontă și oțel.",
            "Este una dintre cele mai cunoscute opere ale lui Brâncuși.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "În ce stil este construită Biserica Domnească din Craiova?",
            "Stil brâncovenesc",
            Arrays.asList("Stil bizantin", "Stil gotic", "Stil neoclasic"),
            "Biserica Domnească din Craiova este un exemplu remarcabil al stilului brâncovenesc, cu elemente decorative specifice.",
            "A fost renovată în timpul lui Constantin Brâncoveanu.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Care este cea mai veche biserică din Craiova?",
            "Biserica Madona Dudu",
            Arrays.asList("Biserica Domnească", "Biserica Sf. Dimitrie", "Biserica Cosuna"),
            "Biserica Madona Dudu din Craiova datează din secolul al XVI-lea și este cea mai veche biserică păstrată din oraș.",
            "A fost construită în vremea lui Radu de la Afumați.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Ce castru roman se află la Drobeta-Turnu Severin?",
            "Castrul Drobeta",
            Arrays.asList("Castrul Dierna", "Castrul Tibiscum", "Castrul Acidava"),
            "Castrul Drobeta era o fortificație romană importantă, situată la capătul podului lui Traian peste Dunăre.",
            "Ruinele sale pot fi vizitate și astăzi în parcul arheologic.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        return questions;
    }
    
    /**
     * Întrebări despre gastronomia Olteniei
     */
    private List<FirestoreQuestionModel> createGastronomyQuestions() {
        List<FirestoreQuestionModel> questions = new ArrayList<>();
        
        questions.add(new FirestoreQuestionModel(
            "Care este mâncarea tradițională specifică Olteniei?",
            "Ciorbă de burtă oltenească",
            Arrays.asList("Mici", "Papanași", "Mămăliguța cu brânză"),
            "Ciorbă de burtă oltenească se deosebește prin condimentarea specifică și modul de preparare tradițional.",
            "Este preparată cu smântână și usturoi, într-un stil specific regiunii.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Ce fel de brânză este specifică Olteniei?",
            "Brânza de Vâlcea",
            Arrays.asList("Telemea", "Cașcaval", "Urdă"),
            "Brânza de Vâlcea este o specialitate tradițională din zona montană a Olteniei, cu gust specific.",
            "Se produce în zona Vâlcea din lapte de oaie și vacă.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Care este băutura tradițională din Oltenia?",
            "Țuica de prune",
            Arrays.asList("Pălinca", "Rachiu de tescovină", "Horincă"),
            "Țuica de prune din Oltenia este renumită pentru calitatea sa, fiind produsă în zonele deluroase.",
            "Prunele din zonă sunt deosebit de aromate și dulci.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Ce desert tradițional se prepară în Oltenia de Paște?",
            "Cozonac cu nucă și rahat",
            Arrays.asList("Papanași", "Plăcinte", "Gogoși"),
            "Cozonacul oltean de Paște se caracterizează prin umplutura generoasă de nucă și rahat turcesc.",
            "Este o tradiție păstrată din vremurile străvechi.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Ce fel de mămăligă se prepară în Oltenia?",
            "Mămăliguța cu jumări",
            Arrays.asList("Mămăliga cu brânză", "Mămăliga cu lapte", "Mămăliga cu ou"),
            "Mămăliguța cu jumări este o specialitate oltenească, servită cu smântână și brânză proaspătă.",
            "Jumările se fac din slănină de porc, tăiată cubulețe.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        return questions;
    }
    
    /**
     * Întrebări despre personalitățile Olteniei
     */
    private List<FirestoreQuestionModel> createPersonalitiesQuestions() {
        List<FirestoreQuestionModel> questions = new ArrayList<>();
        
        questions.add(new FirestoreQuestionModel(
            "În ce comună din Gorj s-a născut Constantin Brâncuși?",
            "Hobița",
            Arrays.asList("Peștișani", "Tismana", "Bumbești-Jiu"),
            "Constantin Brâncuși s-a născut în 1876 în comuna Hobița, județul Gorj, fiind cel mai mare sculptor român.",
            "Casa natală a fost transformată în muzeu memorial.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Cine a fost Ecaterina Teodoroiu?",
            "Eroină din Primul Război Mondial",
            Arrays.asList("Regină a României", "Scriitoare", "Pictoriță"),
            "Ecaterina Teodoroiu, din Vădeni, Gorj, a fost sublocotenent în armata română și erou de război.",
            "A murit în luptă în 1917, la doar 23 de ani.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Cine a fost Nicolae Titulescu?",
            "Diplomat și jurist",
            Arrays.asList("Scriitor", "Pictor", "Muzician"),
            "Nicolae Titulescu, născut în Craiova, a fost un renumit diplomat român și președinte al Societății Națiunilor.",
            "A reprezentat România în multe negocieri internaționale importante.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Cine a fost Elena Farago?",
            "Poetă și traducătoare",
            Arrays.asList("Pictoriță", "Actriță", "Profesoară"),
            "Elena Farago, din Craiova, a fost o poetă și traducătoare renumită, prima femeie membră a Academiei Române.",
            "A tradus din literatura franceză și a scris poezii patriotice.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Cine a fost Amza Pellea?",
            "Actor de teatru și film",
            Arrays.asList("Regizor", "Scriitor", "Pictor"),
            "Amza Pellea, născut în Băilești, Dolj, a fost unul dintre cei mai mari actori români, cunoscut pentru rolurile din filme.",
            "A jucat în filme precum 'Mihai Viteazul' și 'Dacii'.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        return questions;
    }
    
    /**
     * Întrebări despre natura din Oltenia
     */
    private List<FirestoreQuestionModel> createNatureQuestions() {
        List<FirestoreQuestionModel> questions = new ArrayList<>();
        
        questions.add(new FirestoreQuestionModel(
            "Care este parcul național din Oltenia?",
            "Parcul Național Domogled-Valea Cernei",
            Arrays.asList("Parcul Național Retezat", "Parcul Național Cozia", "Parcul Național Piatra Craiului"),
            "Parcul Național Domogled-Valea Cernei se află în Munții Mehedinți și este cunoscut pentru biodiversitatea sa.",
            "Include Vârful Domogled și izvoarele termale de la Băile Herculane.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Ce floare este simbolul Olteniei?",
            "Bujorul",
            Arrays.asList("Narcisa", "Crinul", "Trandafirul"),
            "Bujorul este considerat floarea simbolică a Olteniei, crescând natural în zonele de câmpie și deal.",
            "Înflorește primăvara și este folosit în decorațiunile tradiționale.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Ce animal sălbatic se găsește în Munții Parâng?",
            "Ursul brun",
            Arrays.asList("Lupul", "Râsul", "Cerbul"),
            "Ursul brun trăiește în pădurile din Munții Parâng, fiind una dintre speciile protejate din zonă.",
            "Populația de urși din Parâng este una dintre cele mai importante din România.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Ce lac glaciar se află în Munții Parâng?",
            "Lacul Gâlcescu",
            Arrays.asList("Lacul Oașa", "Lacul Bucura", "Lacul Bâlea"),
            "Lacul Gâlcescu este un lac glaciar situat în Munții Parâng, la o altitudine de aproximativ 2.000 de metri.",
            "Este unul dintre cele mai frumoase lacuri montane din România.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Ce pădure este cunoscută în Oltenia pentru stejarii seculari?",
            "Pădurea Călimănești",
            Arrays.asList("Pădurea Horezu", "Pădurea Tismana", "Pădurea Novaci"),
            "Pădurea Călimănești din Vâlcea este cunoscută pentru stejarii seculari și pentru biodiversitatea sa excepțională.",
            "Unii stejari au vârste de peste 300 de ani.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        return questions;
    }
    
    /**
     * Întrebări despre legendele Olteniei
     */
    private List<FirestoreQuestionModel> createLegendsQuestions() {
        List<FirestoreQuestionModel> questions = new ArrayList<>();
        
        questions.add(new FirestoreQuestionModel(
            "Care este legenda despre numele orașului Craiova?",
            "Vine de la Craiul Jianu",
            Arrays.asList("Vine de la regina Craisa", "Vine de la râul Crai", "Vine de la cetatea Craia"),
            "Legenda spune că numele Craiova vine de la Craiul Jianu, un căpitan de haiduci care și-ar fi avut tabăra în zonă.",
            "Jianu a fost un haiduc legendar din secolele XVII-XVIII.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Ce legendă se spune despre Cheile Sohodolului?",
            "Că au fost săpate de un dragon",
            Arrays.asList("Că sunt mormintele unor războinici", "Că ascund o comoară", "Că sunt porțile raiului"),
            "Legenda spune că Cheile Sohodolului au fost săpate de un dragon uriaș care trăia în munți.",
            "Dragonul ar fi folosit ghearele pentru a-și face un drum spre vale.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Care este legenda Mănăstirii Tismana?",
            "Că a fost ctitorită de Nicodim de la Tismana",
            Arrays.asList("Că ascunde o comoară", "Că a fost construită de îngeri", "Că vindecă orice boală"),
            "Legenda spune că Sfântul Nicodim de la Tismana a construit mănăstirea cu ajutor divin, în secolul al XIV-lea.",
            "Nicodim a fost canonizat și este considerat sfântul protector al Gorjului.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Care este legenda despre Vârful Parângul Mare?",
            "Că pe vârf locuiește Iele",
            Arrays.asList("Că ascunde o comoară dacă", "Că este locul unde se nasc norii", "Că este poarta spre cer"),
            "Legenda spune că pe Vârful Parângul Mare locuiesc Iele, zâne care dansează în nopțile cu lună plină.",
            "Localnicii spun că se aud cântece mistice în noaptea de Sânziene.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        questions.add(new FirestoreQuestionModel(
            "Care este legenda despre podul lui Traian?",
            "Că arhitectul a fost aruncat în Dunăre",
            Arrays.asList("Că s-a construit într-o noapte", "Că este blestemat", "Că ascunde o comoară"),
            "Legenda spune că împăratul Traian l-a aruncat pe arhitectul Apolodor în Dunăre ca să nu mai poată construi un pod mai frumos.",
            "Arhitectul Apolodor din Damasc a fost într-adevăr constructorul podului.",
            "",
            REGION,
            GAME_TYPE
        ));
        
        return questions;
    }
}