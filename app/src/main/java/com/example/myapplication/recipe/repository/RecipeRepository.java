package com.example.myapplication.recipe.repository;

import com.example.myapplication.R;
import com.example.myapplication.recipe.model.Ingredient;
import com.example.myapplication.recipe.model.NutritionalInfo;
import com.example.myapplication.recipe.model.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton repository care gestionează datele rețetelor
 */
public class RecipeRepository {
    private static RecipeRepository instance;
    private final List<Recipe> recipes;

    private RecipeRepository() {
        recipes = new ArrayList<>();
        initializeRecipes();
    }

    public static RecipeRepository getInstance() {
        if (instance == null) {
            instance = new RecipeRepository();
        }
        return instance;
    }

    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(recipes);
    }

    public Recipe getRecipeById(int id) {
        for (Recipe recipe : recipes) {
            if (recipe.getId() == id) {
                return recipe;
            }
        }
        return null;
    }

    public List<Recipe> getFavoriteRecipes() {
        return recipes.stream()
                .filter(Recipe::isFavorite)
                .collect(Collectors.toList());
    }

    public List<Recipe> getRecipesByCategory(String category) {
        return recipes.stream()
                .filter(recipe -> recipe.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<Recipe> searchRecipes(String query) {
        String lowerCaseQuery = query.toLowerCase();
        return recipes.stream()
                .filter(recipe -> recipe.getTitle().toLowerCase().contains(lowerCaseQuery)
                        || recipe.getDescription().toLowerCase().contains(lowerCaseQuery)
                        || recipe.getCategory().toLowerCase().contains(lowerCaseQuery)
                        || recipe.getRegion().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());
    }

    public void updateRecipe(Recipe recipe) {
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).getId() == recipe.getId()) {
                recipes.set(i, recipe);
                break;
            }
        }
    }

    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
    }

    public void removeRecipe(Recipe recipe) {
        recipes.removeIf(r -> r.getId() == recipe.getId());
    }

    /**
     * Gets the next available ID for a new recipe
     * @return the next available ID
     */
    public int getNextAvailableId() {
        int maxId = 0;
        for (Recipe recipe : recipes) {
            if (recipe.getId() > maxId) {
                maxId = recipe.getId();
            }
        }
        return maxId + 1;
    }

    private void initializeRecipes() {
        // 1. Sarmale Moldovenești
        Recipe sarmale = new Recipe(
                1,
                "Sarmale Moldovenești",
                "Tradiționala sarmă moldovenească, un preparat nelipsit de pe mesele românești în zilele de sărbătoare.",
                "Fel principal",
                "Moldova",
                "Mediu",
                45,
                180,
                8,
                R.drawable.sarmale);

        sarmale.addIngredient(new Ingredient("carne tocată de porc", "1", "kg"));
        sarmale.addIngredient(new Ingredient("ceapă", "2", "buc"));
        sarmale.addIngredient(new Ingredient("orez", "200", "g"));
        sarmale.addIngredient(new Ingredient("varză murată", "1", "buc"));
        sarmale.addIngredient(new Ingredient("cimbru", "1", "linguriță"));
        sarmale.addIngredient(new Ingredient("mărar", "1", "legătură"));
        sarmale.addIngredient(new Ingredient("foi de dafin", "3", "buc"));
        sarmale.addIngredient(new Ingredient("pastă de tomate", "3", "linguri"));
        sarmale.addIngredient(new Ingredient("ulei", "100", "ml"));
        sarmale.addIngredient(new Ingredient("sare", "1", "linguriță"));
        sarmale.addIngredient(new Ingredient("piper", "1/2", "linguriță"));

        sarmale.addPreparationStep("Desface și spală frunzele de varză, îndepărtând cotoarele groase.");
        sarmale.addPreparationStep("Călește ceapa tocată mărunt în 3 linguri de ulei până devine translucidă.");
        sarmale.addPreparationStep("Amestecă carnea tocată cu ceapa călită, orezul spălat, sare, piper și cimbru.");
        sarmale.addPreparationStep("Pune câte o lingură de compoziție pe fiecare frunză, înfășoară și rulează pentru a forma sarmalele.");
        sarmale.addPreparationStep("Așază un strat de frunze de varză pe fundul unei oale, apoi adaugă sarmalele.");
        sarmale.addPreparationStep("Adaugă pasta de tomate diluată în 500 ml de apă, foile de dafin și restul de frunze de varză deasupra.");
        sarmale.addPreparationStep("Acoperă și fierbe la foc mic aproximativ 3 ore, adăugând apă dacă este necesar.");

        sarmale.setNutritionalInfo(new NutritionalInfo(320, 18.5f, 22.3f, 18.7f, 2.4f, 3.1f, 650f));

        // 2. Mămăligă cu brânză și smântână
        Recipe mamaliga = new Recipe(
                2,
                "Mămăligă cu brânză și smântână",
                "Mămăliga tradițională românească, servită cu brânză de burduf și smântână proaspătă.",
                "Fel principal",
                "Transilvania",
                "Ușor",
                10,
                20,
                6,
                R.drawable.mamaliga);

        mamaliga.addIngredient(new Ingredient("mălai", "500", "g"));
        mamaliga.addIngredient(new Ingredient("apă", "1.5", "l"));
        mamaliga.addIngredient(new Ingredient("sare", "1", "linguriță"));
        mamaliga.addIngredient(new Ingredient("brânză de burduf", "300", "g"));
        mamaliga.addIngredient(new Ingredient("smântână", "200", "g"));
        mamaliga.addIngredient(new Ingredient("unt", "50", "g"));

        mamaliga.addPreparationStep("Pune apa la fiert într-o oală, adaugă sarea.");
        mamaliga.addPreparationStep("Când apa fierbe, adaugă treptat mălaiul, amestecând continuu cu un făcăleț pentru a evita formarea cocoloașelor.");
        mamaliga.addPreparationStep("Continuă să fierbi și să amesteci aproximativ 20 de minute până când mămăliga se desprinde de pe marginile oalei.");
        mamaliga.addPreparationStep("Răstoarnă mămăliga pe un platou și las-o să se răcească puțin.");
        mamaliga.addPreparationStep("Fărâmițează brânza și servește lângă mămăligă.");
        mamaliga.addPreparationStep("Adaugă smântâna și bucata de unt deasupra mămăligii calde.");

        mamaliga.setNutritionalInfo(new NutritionalInfo(380, 12.5f, 45.0f, 16.2f, 1.8f, 2.0f, 320f));

        // 3. Ciorbă de burtă
        Recipe ciorbaBurta = new Recipe(
                3,
                "Ciorbă de burtă",
                "Renumita ciorbă românească de burtă, cunoscută pentru gustul său unic și consistența cremă.",
                "Supă/Ciorbă",
                "Muntenia",
                "Mediu",
                30,
                120,
                10,
                R.drawable.ciorba_burta);

        ciorbaBurta.addIngredient(new Ingredient("burtă de vită fiartă", "1", "kg"));
        ciorbaBurta.addIngredient(new Ingredient("morcovi", "2", "buc"));
        ciorbaBurta.addIngredient(new Ingredient("țelină", "1", "buc"));
        ciorbaBurta.addIngredient(new Ingredient("ceapă", "2", "buc"));
        ciorbaBurta.addIngredient(new Ingredient("usturoi", "5", "căței"));
        ciorbaBurta.addIngredient(new Ingredient("smântână", "400", "ml"));
        ciorbaBurta.addIngredient(new Ingredient("gălbenușuri", "2", "buc"));
        ciorbaBurta.addIngredient(new Ingredient("oțet", "2", "linguri"));
        ciorbaBurta.addIngredient(new Ingredient("sare", "1", "linguriță"));
        ciorbaBurta.addIngredient(new Ingredient("piper", "1/2", "linguriță"));
        ciorbaBurta.addIngredient(new Ingredient("ardei iute", "1", "buc"));

        ciorbaBurta.addPreparationStep("Taie burta fiartă în fâșii subțiri.");
        ciorbaBurta.addPreparationStep("Pune legumele (morcovi, ceapă, țelină) la fiert în 3 litri de apă cu sare.");
        ciorbaBurta.addPreparationStep("După ce legumele sunt fierte, strecoară zeama și adaugă fâșiile de burtă.");
        ciorbaBurta.addPreparationStep("Lasă să fiarbă încă 30 de minute la foc mic.");
        ciorbaBurta.addPreparationStep("Între timp, pisează usturoiul și amestecă-l cu smântâna.");
        ciorbaBurta.addPreparationStep("Bate gălbenușurile și adaugă-le în smântână, amestecând bine.");
        ciorbaBurta.addPreparationStep("Ia o polonic de zeamă fierbinte și adaug-o treptat în amestecul de smântână, amestecând continuu.");
        ciorbaBurta.addPreparationStep("Adaugă acest amestec în oala cu ciorbă, amestecând încet.");
        ciorbaBurta.addPreparationStep("Adaugă oțetul și mai lasă ciorba pe foc încă 5 minute, fără să o lași să fiarbă.");
        ciorbaBurta.addPreparationStep("Servește fierbinte cu ardei iute.");

        ciorbaBurta.setNutritionalInfo(new NutritionalInfo(280, 22.0f, 9.5f, 18.3f, 1.5f, 2.7f, 980f));

        // 4. Cozonac moldovenesc
        Recipe cozonac = new Recipe(
                4,
                "Cozonac moldovenesc",
                "Cozonac tradițional moldovenesc cu umplutură bogată de nuci, stafide și cacao.",
                "Desert",
                "Moldova",
                "Dificil",
                60,
                50,
                12,
                R.drawable.cozonac);

        cozonac.addIngredient(new Ingredient("făină", "1", "kg"));
        cozonac.addIngredient(new Ingredient("zahăr", "300", "g"));
        cozonac.addIngredient(new Ingredient("lapte", "500", "ml"));
        cozonac.addIngredient(new Ingredient("ouă", "5", "buc"));
        cozonac.addIngredient(new Ingredient("unt", "200", "g"));
        cozonac.addIngredient(new Ingredient("drojdie proaspătă", "50", "g"));
        cozonac.addIngredient(new Ingredient("esență de rom", "2", "lingurițe"));
        cozonac.addIngredient(new Ingredient("coajă de lămâie", "1", "buc"));
        cozonac.addIngredient(new Ingredient("nuci măcinate", "400", "g"));
        cozonac.addIngredient(new Ingredient("stafide", "150", "g"));
        cozonac.addIngredient(new Ingredient("cacao", "3", "linguri"));
        cozonac.addIngredient(new Ingredient("zahăr vanilat", "2", "plicuri"));

        cozonac.addPreparationStep("Călduță laptele și dizolvă drojdia în el cu o lingură de zahăr. Lasă să se activeze 10 minute.");
        cozonac.addPreparationStep("Freacă ouăle întregi cu restul zahărului până se albesc.");
        cozonac.addPreparationStep("Încorporează untul topit, esența de rom și coaja de lămâie rasă.");
        cozonac.addPreparationStep("Adaugă treptat făina și amestecul de drojdie, frământând până obții un aluat elastic care se desprinde de pe mâini.");
        cozonac.addPreparationStep("Acoperă aluatul și lasă-l la dospit aproximativ 1 oră, într-un loc călduț, până își dublează volumul.");
        cozonac.addPreparationStep("Între timp, pregătește umplutura: amestecă nucile măcinate cu zahărul rămas, cacaua și stafidele înmuiate în prealabil în rom.");
        cozonac.addPreparationStep("După ce aluatul a crescut, împarte-l în două părți egale.");
        cozonac.addPreparationStep("Întinde fiecare bucată în formă de dreptunghi, pune jumătate din umplutură și rulează strâns.");
        cozonac.addPreparationStep("Așază cozonacii în tăvi unse cu unt și lăsați-i să mai crească încă 30 de minute.");
        cozonac.addPreparationStep("Unge cozonacii cu gălbenuș bătut și coace-i în cuptorul preîncălzit la 180°C pentru 45-50 de minute.");
        cozonac.addPreparationStep("Verifică dacă sunt copți introducând o scobitoare - trebuie să iasă curată.");
        cozonac.addPreparationStep("Lasă cozonacii să se răcească complet înainte de a-i tăia.");

        cozonac.setNutritionalInfo(new NutritionalInfo(435, 8.7f, 52.0f, 22.5f, 2.8f, 28.0f, 150f));

        // 5. Mititei (Mici)
        Recipe mici = new Recipe(
                5,
                "Mititei (Mici)",
                "Celebrii mititei românești, delicioși și aromați, perfecți pentru grătar.",
                "Fel principal",
                "Muntenia",
                "Mediu",
                30,
                15,
                6,
                R.drawable.mici);

        mici.addIngredient(new Ingredient("carne tocată de vită", "500", "g"));
        mici.addIngredient(new Ingredient("carne tocată de miel", "250", "g"));
        mici.addIngredient(new Ingredient("carne tocată de porc", "250", "g"));
        mici.addIngredient(new Ingredient("bicarbonat de sodiu", "1/2", "linguriță"));
        mici.addIngredient(new Ingredient("usturoi", "4", "căței"));
        mici.addIngredient(new Ingredient("cimbru", "1", "linguriță"));
        mici.addIngredient(new Ingredient("chimen măcinat", "1/2", "linguriță"));
        mici.addIngredient(new Ingredient("piper negru", "1", "linguriță"));
        mici.addIngredient(new Ingredient("sare", "1", "linguriță"));
        mici.addIngredient(new Ingredient("supă de carne", "100", "ml"));

        mici.addPreparationStep("Amestecă toate tipurile de carne într-un bol mare.");
        mici.addPreparationStep("Pisează usturoiul și adaugă-l în compoziție împreună cu sarea, piperul, cimbrul și chimenul.");
        mici.addPreparationStep("Dizolvă bicarbonatul în supa de carne și adaugă-l peste amestecul de carne.");
        mici.addPreparationStep("Frământă bine compoziția timp de cel puțin 10 minute până devine omogenă și lipicioasă.");
        mici.addPreparationStep("Acoperă bolul și lasă compoziția la frigider minim 6 ore, preferabil peste noapte.");
        mici.addPreparationStep("Cu mâinile umede, formează mititei de aproximativ 8-10 cm lungime și 2 cm grosime.");
        mici.addPreparationStep("Gătește mititeii pe un grătar bine încins, întorcându-i frecvent până sunt bine rumeniți pe toate părțile (aproximativ 10-15 minute).");
        mici.addPreparationStep("Servește-i fierbinți cu muștar și pâine proaspătă.");

        mici.setNutritionalInfo(new NutritionalInfo(320, 24.5f, 1.2f, 25.0f, 0.0f, 0.5f, 780f));

        // 6. Papanași cu smântână și dulceață
        Recipe papanasi = new Recipe(
                6,
                "Papanași cu smântână și dulceață",
                "Deliciosul desert tradițional românesc, papanașii pufoși cu smântână și dulceață de afine.",
                "Desert",
                "Transilvania",
                "Mediu",
                30,
                20,
                4,
                R.drawable.papanasi);

        papanasi.addIngredient(new Ingredient("brânză de vaci", "500", "g"));
        papanasi.addIngredient(new Ingredient("făină", "250", "g"));
        papanasi.addIngredient(new Ingredient("ouă", "2", "buc"));
        papanasi.addIngredient(new Ingredient("zahăr", "100", "g"));
        papanasi.addIngredient(new Ingredient("coajă de lămâie", "1", "buc"));
        papanasi.addIngredient(new Ingredient("esență de vanilie", "1", "linguriță"));
        papanasi.addIngredient(new Ingredient("praf de copt", "1", "linguriță"));
        papanasi.addIngredient(new Ingredient("un praf de sare", "1", "linguriță"));
        papanasi.addIngredient(new Ingredient("ulei pentru prăjit", "500", "ml"));
        papanasi.addIngredient(new Ingredient("smântână", "200", "g"));
        papanasi.addIngredient(new Ingredient("dulceață de afine", "200", "g"));

        papanasi.addPreparationStep("Scurge bine brânza de vaci de zer, apoi amestec-o cu zahărul, ouăle, coaja de lămâie și esența de vanilie.");
        papanasi.addPreparationStep("Adaugă făina amestecată cu praful de copt și sarea, încorporând treptat până obții un aluat moale dar care își păstrează forma.");
        papanasi.addPreparationStep("Cu mâinile umezite, formează bile de mărimea unei portocale, apoi apasă în mijloc cu degetul pentru a face o gaură.");
        papanasi.addPreparationStep("Din o mică parte din aluat, formează bile mici care vor fi așezate deasupra papanașilor, ca o pălărie.");
        papanasi.addPreparationStep("Încinge uleiul într-o tigaie adâncă și prăjește papanașii pe ambele părți până se rumenesc frumos.");
        papanasi.addPreparationStep("Scoate-i pe un șervețel absorbant pentru a îndepărta excesul de ulei.");
        papanasi.addPreparationStep("Servește papanașii calzi, așezând bilele mici deasupra celor mari, apoi adaugă smântână și dulceață de afine.");

        papanasi.setNutritionalInfo(new NutritionalInfo(420, 14.5f, 48.0f, 19.2f, 1.5f, 24.0f, 180f));

        // 7. Ciorbă de legume
        Recipe ciorbaLegume = new Recipe(
                7,
                "Ciorbă de legume",
                "Ciorbă sănătoasă și aromată de legume, perfectă pentru orice anotimp.",
                "Supă/Ciorbă",
                "Oltenia",
                "Ușor",
                20,
                40,
                6,
                R.drawable.ciorba_legume);

        ciorbaLegume.addIngredient(new Ingredient("morcovi", "3", "buc"));
        ciorbaLegume.addIngredient(new Ingredient("ceapă", "2", "buc"));
        ciorbaLegume.addIngredient(new Ingredient("ardei gras", "2", "buc"));
        ciorbaLegume.addIngredient(new Ingredient("țelină", "1", "bucată"));
        ciorbaLegume.addIngredient(new Ingredient("păstârnac", "1", "buc"));
        ciorbaLegume.addIngredient(new Ingredient("cartofi", "3", "buc"));
        ciorbaLegume.addIngredient(new Ingredient("roșii", "4", "buc"));
        ciorbaLegume.addIngredient(new Ingredient("fasole verde", "200", "g"));
        ciorbaLegume.addIngredient(new Ingredient("mazăre", "100", "g"));
        ciorbaLegume.addIngredient(new Ingredient("leuștean", "1", "legătură"));
        ciorbaLegume.addIngredient(new Ingredient("pătrunjel", "1", "legătură"));
        ciorbaLegume.addIngredient(new Ingredient("borș", "500", "ml"));
        ciorbaLegume.addIngredient(new Ingredient("ulei", "3", "linguri"));
        ciorbaLegume.addIngredient(new Ingredient("sare", "1", "linguriță"));
        ciorbaLegume.addIngredient(new Ingredient("piper", "1/2", "linguriță"));

        ciorbaLegume.addPreparationStep("Curăță și taie toate legumele: morcovii și păstârnacul în rondele, ceapa mărunt, ardeii și țelina cubulețe, cartofii cuburi mai mari.");
        ciorbaLegume.addPreparationStep("Încinge uleiul într-o oală mare și călește ceapa până devine translucidă.");
        ciorbaLegume.addPreparationStep("Adaugă restul legumelor rădăcinoase (morcovi, țelină, păstârnac) și călește-le 5 minute.");
        ciorbaLegume.addPreparationStep("Adaugă 3 litri de apă și lasă să fiarbă la foc mediu aproximativ 15 minute.");
        ciorbaLegume.addPreparationStep("Adaugă cartofii, ardeii, fasolea verde și mazărea și continuă fierberea încă 10 minute.");
        ciorbaLegume.addPreparationStep("Adaugă roșiile tăiate cubulețe sau pasta de roșii și fierbe încă 5 minute.");
        ciorbaLegume.addPreparationStep("Adaugă borșul și mai fierbe 5 minute.");
        ciorbaLegume.addPreparationStep("Condimentează cu sare și piper după gust.");
        ciorbaLegume.addPreparationStep("La final, adaugă verdeața tocată fin (leuștean și pătrunjel).");
        ciorbaLegume.addPreparationStep("Servește ciorba caldă, eventual cu smântână și ardei iute.");

        ciorbaLegume.setNutritionalInfo(new NutritionalInfo(180, 5.5f, 32.0f, 2.8f, 8.2f, 6.5f, 420f));

        // 8. Tochitură moldovenească
        Recipe tochitura = new Recipe(
                8,
                "Tochitură moldovenească",
                "Specialitate tradițională din Moldova, cu bucăți de carne suculentă și mămăliguță.",
                "Fel principal",
                "Moldova",
                "Mediu",
                20,
                60,
                6,
                R.drawable.tochitura);

        tochitura.addIngredient(new Ingredient("carne de porc (pulpă sau spată)", "800", "g"));
        tochitura.addIngredient(new Ingredient("cârnați afumați", "400", "g"));
        tochitura.addIngredient(new Ingredient("ficat de pui", "300", "g"));
        tochitura.addIngredient(new Ingredient("ceapă", "3", "buc"));
        tochitura.addIngredient(new Ingredient("usturoi", "6", "căței"));
        tochitura.addIngredient(new Ingredient("vin roșu", "200", "ml"));
        tochitura.addIngredient(new Ingredient("ulei", "4", "linguri"));
        tochitura.addIngredient(new Ingredient("boia dulce", "1", "linguriță"));
        tochitura.addIngredient(new Ingredient("cimbru", "1", "linguriță"));
        tochitura.addIngredient(new Ingredient("sare", "1", "linguriță"));
        tochitura.addIngredient(new Ingredient("piper", "1", "linguriță"));
        tochitura.addIngredient(new Ingredient("ouă", "6", "buc"));
        tochitura.addIngredient(new Ingredient("brânză de burduf", "200", "g"));
        tochitura.addIngredient(new Ingredient("mălai pentru mămăligă", "500", "g"));
        tochitura.addIngredient(new Ingredient("apă pentru mămăligă", "1.5", "l"));

        tochitura.addPreparationStep("Taie carnea de porc în bucăți de aproximativ 3-4 cm.");
        tochitura.addPreparationStep("Taie cârnații în bucăți de 2-3 cm și ficatul în bucăți mai mici.");
        tochitura.addPreparationStep("Încinge uleiul într-o tigaie adâncă și prăjește bucățile de carne până se rumenesc pe toate părțile.");
        tochitura.addPreparationStep("Adaugă ceapa tocată mărunt și călește până devine translucidă.");
        tochitura.addPreparationStep("Adaugă cârnații și continuă prăjirea încă 5 minute.");
        tochitura.addPreparationStep("Adaugă ficatul și prăjește totul încă 3-4 minute.");
        tochitura.addPreparationStep("Adaugă usturoiul pisat, boia, cimbrul, sarea și piperul.");
        tochitura.addPreparationStep("Toarnă vinul și lasă să fiarbă la foc mic aproximativ 30 de minute până carnea devine fragedă și sosul se reduce.");
        tochitura.addPreparationStep("Între timp, prepară mămăliga conform rețetei tradiționale.");
        tochitura.addPreparationStep("Separat, prăjește ouăle în stil \"ochi\".");
        tochitura.addPreparationStep("Servește tochitura alături de mămăligă, cu ouă prăjite deasupra și brânză de burduf rasă.");

        tochitura.setNutritionalInfo(new NutritionalInfo(680, 42.0f, 24.0f, 45.5f, 1.2f, 3.5f, 1200f));
        
        // Adaugă rețetele la repository
        recipes.add(sarmale);
        recipes.add(mamaliga);
        recipes.add(ciorbaBurta);
        recipes.add(cozonac);
        recipes.add(mici);
        recipes.add(papanasi);
        recipes.add(ciorbaLegume);
        recipes.add(tochitura);
    }
} 