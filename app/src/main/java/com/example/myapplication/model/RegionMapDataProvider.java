package com.example.myapplication.model;

import com.example.myapplication.RomApp.Brasov;
import com.example.myapplication.RomApp.ClujNapoca;
import com.example.myapplication.RomApp.Sibiu;
import com.example.myapplication.RomApp.AlbaIulia;
import com.example.myapplication.RomApp.Sighetu;
import com.example.myapplication.RomApp.BaiaMare;
import com.example.myapplication.RomApp.Craiova;
import com.example.myapplication.RomApp.TarguJiu;
import com.example.myapplication.RomApp.Drobetaturnuseverin;
import com.example.myapplication.RomApp.Iasi;
import com.example.myapplication.RomApp.PiatraNeamt;
import com.example.myapplication.RomApp.Suceava;
import com.example.myapplication.RomApp.Radauti;
import com.example.myapplication.RomApp.GuraHumorului;
import com.example.myapplication.RomApp.Constanta;
import com.example.myapplication.RomApp.Tulcea;
import com.example.myapplication.RomApp.Timisoara;
import com.example.myapplication.RomApp.Resita;
import com.example.myapplication.RomApp.BaileHerculane;
import com.example.myapplication.RomApp.Oradea;
import com.example.myapplication.RomApp.Arad;
import com.example.myapplication.RomApp.Bucuresti;
import com.example.myapplication.RomApp.Targoviste;
import com.example.myapplication.RomApp.Craiova;

import com.example.myapplication.RomApp.Crisana;
import com.example.myapplication.RomApp.Maramures;
import com.example.myapplication.RomApp.Dobrogea;
import com.example.myapplication.RomApp.Bucovina;
import com.example.myapplication.RomApp.Moldova;
import com.example.myapplication.RomApp.Oltenia;
import com.example.myapplication.RomApp.Banat;
import com.example.myapplication.RomApp.Transilvania;
import com.example.myapplication.RomApp.Muntenia;

import com.example.myapplication.banatusage.BanatGameActivity;
import com.example.myapplication.banatusage.BanatStoryActivity;
import com.example.myapplication.bucovinausage.BucovinaGameActivity;
import com.example.myapplication.bucovinausage.BucovinaStoryActivity;
import com.example.myapplication.crisanausage.CrisanaGameActivity;
import com.example.myapplication.crisanausage.CrisanaStoryActivity;
import com.example.myapplication.dobrogeausage.DobrogeaGameActivity;
import com.example.myapplication.dobrogeausage.CasinoStoryActivity;
import com.example.myapplication.maramuresusage.MaramuresGameActivity;
import com.example.myapplication.maramuresusage.MaramuresStoryActivity;
import com.example.myapplication.moldovausage.MoldovaGameActivity;
import com.example.myapplication.moldovausage.MoldovaStoryActivity;
import com.example.myapplication.olteniausage.OlteniaGameActivity;
import com.example.myapplication.olteniausage.OlteniaStoryActivity;
import com.example.myapplication.munteniausage.MunteniaGameActivity;
import com.example.myapplication.munteniausage.MunteniaTourActivity;
import com.example.myapplication.transilvaniausage.TransilvaniaGameActivity;
import com.example.myapplication.transilvaniausage.DraculaStoryActivity;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 * Provider pentru datele hărților regiunilor
 * Furnizează date preconfigurate pentru fiecare regiune
 */
public class RegionMapDataProvider {
    private static RegionMapDataProvider instance;
    private Map<String, RegionMapData> regionDataMap;
    
    private RegionMapDataProvider() {
        regionDataMap = new HashMap<>();
        initializeRegionData();
    }
    
    /**
     * Obține instanța singleton a provider-ului
     * @return Instanța RegionMapDataProvider
     */
    public static synchronized RegionMapDataProvider getInstance() {
        if (instance == null) {
            instance = new RegionMapDataProvider();
        }
        return instance;
    }
    
    /**
     * Inițializează datele pentru toate regiunile
     */
    private void initializeRegionData() {
        // Inițializăm datele pentru Transilvania
        RegionMapData transilvaniaData = new RegionMapData(
            "Transilvania",
            new LatLng(46.7712, 23.6236), // Cluj-Napoca ca centru
            7.5f
        );
        
        transilvaniaData.setStoryActivityClass(DraculaStoryActivity.class)
                       .setGameActivityClass(TransilvaniaGameActivity.class)
                       .addLocation(
                           "Brașov", 
                           "Orașul de la poalele Tâmpei", 
                           new LatLng(45.6427, 25.5887), 
                           BitmapDescriptorFactory.HUE_RED,
                           1,
                           Brasov.class
                       )
                       .addLocation(
                           "Cluj-Napoca", 
                           "Capitala neoficială a Transilvaniei", 
                           new LatLng(46.7712, 23.6236), 
                           BitmapDescriptorFactory.HUE_RED,
                           2,
                           ClujNapoca.class
                       )
                       .addLocation(
                           "Sighișoara", 
                           "Cetate medievală UNESCO", 
                           new LatLng(46.2197, 24.7922), 
                           BitmapDescriptorFactory.HUE_ORANGE,
                           3
                       )
                       .addLocation(
                           "Sibiu", 
                           "Fost Capitală Culturală Europeană", 
                           new LatLng(45.7983, 24.1469), 
                           BitmapDescriptorFactory.HUE_RED,
                           4,
                           Sibiu.class
                       )
                       .addLocation(
                           "Alba Iulia", 
                           "Cetatea Unirii", 
                           new LatLng(46.0711, 23.5788), 
                           BitmapDescriptorFactory.HUE_ORANGE,
                           5,
                           AlbaIulia.class
                       )
                       .addLocation(
                           "Castelul Bran", 
                           "Cunoscut ca Castelul lui Dracula", 
                           new LatLng(45.5149, 25.3672), 
                           BitmapDescriptorFactory.HUE_VIOLET,
                           6
                       )
                       .addLocation(
                           "Castelul Corvinilor", 
                           "Unul dintre cele mai frumoase castele din Europa", 
                           new LatLng(45.7489, 22.8881), 
                           BitmapDescriptorFactory.HUE_VIOLET,
                           7
                       )
                       .addLocation(
                           "Salina Turda", 
                           "Mină de sare transformată în atracție turistică", 
                           new LatLng(46.5847, 23.7875), 
                           BitmapDescriptorFactory.HUE_CYAN,
                           8
                       );
        
        // Inițializăm datele pentru Maramureș
        RegionMapData maramuresData = new RegionMapData(
            "Maramureș",
            new LatLng(47.6635, 23.5861), // Baia Mare ca centru
            8.0f
        );
        
        maramuresData.setStoryActivityClass(MaramuresStoryActivity.class)
                    .setGameActivityClass(MaramuresGameActivity.class)
                    .addLocation(
                        "Baia Mare", 
                        "Capitala județului Maramureș", 
                        new LatLng(47.6635, 23.5861), 
                        BitmapDescriptorFactory.HUE_RED,
                        1,
                        BaiaMare.class
                    )
                    .addLocation(
                        "Sighetu Marmației", 
                        "Oraș istoric important", 
                        new LatLng(47.9275, 23.8890), 
                        BitmapDescriptorFactory.HUE_RED,
                        2,
                        Sighetu.class
                    )
                    .addLocation(
                        "Săpânța", 
                        "Locul Cimitirului Vesel", 
                        new LatLng(47.9736, 23.6964), 
                        BitmapDescriptorFactory.HUE_ORANGE,
                        3
                    )
                    .addLocation(
                        "Biserica de lemn din Șurdești", 
                        "Una dintre cele mai înalte biserici de lemn din lume", 
                        new LatLng(47.6903, 23.7408), 
                        BitmapDescriptorFactory.HUE_AZURE,
                        4
                    )
                    .addLocation(
                        "Mănăstirea Bârsana", 
                        "Complex monastic impresionant", 
                        new LatLng(47.8111, 24.0639), 
                        BitmapDescriptorFactory.HUE_AZURE,
                        5
                    )
                    .addLocation(
                        "Mocănița de pe Valea Vaserului", 
                        "Trenul cu aburi pe cale ferată forestieră", 
                        new LatLng(47.7131, 24.4450), 
                        BitmapDescriptorFactory.HUE_GREEN,
                        6
                    );
        
        // Inițializăm datele pentru Oltenia
        RegionMapData olteniaData = new RegionMapData(
            "Oltenia",
            new LatLng(44.3167, 23.8000), // Craiova ca centru
            7.5f
        );
        
        olteniaData.setStoryActivityClass(OlteniaStoryActivity.class)
                  .setGameActivityClass(OlteniaGameActivity.class)
                  .addLocation(
                      "Craiova", 
                      "Capitala Olteniei", 
                      new LatLng(44.3167, 23.8000), 
                      BitmapDescriptorFactory.HUE_RED,
                      1,
                      Craiova.class
                  )
                  .addLocation(
                      "Târgu Jiu", 
                      "Orașul lui Brâncuși", 
                      new LatLng(45.0333, 23.2833), 
                      BitmapDescriptorFactory.HUE_RED,
                      2,
                      TarguJiu.class
                  )
                  .addLocation(
                      "Drobeta-Turnu Severin", 
                      "Podul lui Traian", 
                      new LatLng(44.6333, 22.6667), 
                      BitmapDescriptorFactory.HUE_ORANGE,
                      3,
                      Drobetaturnuseverin.class
                  )
                  .addLocation(
                      "Mănăstirea Tismana", 
                      "Una dintre cele mai vechi mănăstiri din țară", 
                      new LatLng(45.0667, 22.9500), 
                      BitmapDescriptorFactory.HUE_AZURE,
                      4
                  )
                  .addLocation(
                      "Horezu", 
                      "Centru de ceramică tradițională", 
                      new LatLng(45.1333, 24.0000), 
                      BitmapDescriptorFactory.HUE_GREEN,
                      5
                  );
        
        // Inițializăm datele pentru Moldova
        RegionMapData moldovaData = new RegionMapData(
            "Moldova",
            new LatLng(47.1585, 27.6014), // Iași ca centru
            7.5f
        );
        
        moldovaData.setStoryActivityClass(MoldovaStoryActivity.class)
                  .setGameActivityClass(MoldovaGameActivity.class)
                  .addLocation(
                      "Iași", 
                      "Capitala culturală a Moldovei", 
                      new LatLng(47.1585, 27.6014), 
                      BitmapDescriptorFactory.HUE_RED,
                      1,
                      Iasi.class
                  )
                  .addLocation(
                      "Piatra Neamț", 
                      "Perla Moldovei", 
                      new LatLng(46.9275, 26.3708), 
                      BitmapDescriptorFactory.HUE_RED,
                      2,
                      PiatraNeamt.class
                  )
                  .addLocation(
                      "Cetatea Neamț", 
                      "Cetate medievală impresionantă", 
                      new LatLng(47.2583, 26.3750), 
                      BitmapDescriptorFactory.HUE_ORANGE,
                      3
                  )
                  .addLocation(
                      "Mănăstirea Voroneț", 
                      "Biserica cu albastrul de Voroneț", 
                      new LatLng(47.5167, 25.8667), 
                      BitmapDescriptorFactory.HUE_AZURE,
                      4
                  )
                  .addLocation(
                      "Bacău", 
                      "Centru economic important", 
                      new LatLng(46.5667, 26.9167), 
                      BitmapDescriptorFactory.HUE_RED,
                      5
                  );
        
        // Inițializăm datele pentru Bucovina
        RegionMapData bucovinaData = new RegionMapData(
            "Bucovina",
            new LatLng(47.6500, 25.8833), // Suceava ca centru
            8.0f
        );
        
        bucovinaData.setStoryActivityClass(BucovinaStoryActivity.class)
                   .setGameActivityClass(BucovinaGameActivity.class)
                   .addLocation(
                       "Suceava", 
                       "Capitala Bucovinei", 
                       new LatLng(47.6500, 25.8833), 
                       BitmapDescriptorFactory.HUE_RED,
                       1,
                       Suceava.class
                   )
                   .addLocation(
                       "Mănăstirea Moldovița", 
                       "Mănăstire fortificată UNESCO", 
                       new LatLng(47.6500, 25.6333), 
                       BitmapDescriptorFactory.HUE_AZURE,
                       2
                   )
                   .addLocation(
                       "Mănăstirea Sucevița", 
                       "Mănăstire cu pictură exterioară", 
                       new LatLng(47.7667, 25.7167), 
                       BitmapDescriptorFactory.HUE_AZURE,
                       3
                   )
                   .addLocation(
                       "Câmpulung Moldovenesc", 
                       "Stațiune montană", 
                       new LatLng(47.5333, 25.5667), 
                       BitmapDescriptorFactory.HUE_GREEN,
                       4
                   )
                   .addLocation(
                       "Vatra Dornei", 
                       "Stațiune balneară", 
                       new LatLng(47.3500, 25.3667), 
                       BitmapDescriptorFactory.HUE_GREEN,
                       5
                   );
        
        // Inițializăm datele pentru Dobrogea
        RegionMapData dobrogeaData = new RegionMapData(
            "Dobrogea",
            new LatLng(44.1667, 28.6333), // Constanța ca centru
            8.0f
        );
        
        dobrogeaData.setStoryActivityClass(CasinoStoryActivity.class)
                   .setGameActivityClass(DobrogeaGameActivity.class)
                   .addLocation(
                       "Constanța", 
                       "Cel mai mare port la Marea Neagră", 
                       new LatLng(44.1667, 28.6333), 
                       BitmapDescriptorFactory.HUE_RED,
                       1,
                       Constanta.class
                   )
                   .addLocation(
                       "Tulcea", 
                       "Poarta către Delta Dunării", 
                       new LatLng(45.1667, 28.8000), 
                       BitmapDescriptorFactory.HUE_RED,
                       2,
                       Tulcea.class
                   )
                   .addLocation(
                       "Delta Dunării", 
                       "Rezervație a Biosferei UNESCO", 
                       new LatLng(45.0833, 29.5000), 
                       BitmapDescriptorFactory.HUE_GREEN,
                       3
                   )
                   .addLocation(
                       "Histria", 
                       "Cea mai veche așezare din România", 
                       new LatLng(44.5500, 28.7667), 
                       BitmapDescriptorFactory.HUE_ORANGE,
                       4
                   )
                   .addLocation(
                       "Mănăstirea Dervent", 
                       "Important loc de pelerinaj", 
                       new LatLng(44.0833, 27.9500), 
                       BitmapDescriptorFactory.HUE_AZURE,
                       5
                   );
        
        // Inițializăm datele pentru Banat
        RegionMapData banatData = new RegionMapData(
            "Banat",
            new LatLng(45.7500, 21.2333), // Timișoara ca centru
            8.0f
        );
        
        banatData.setStoryActivityClass(BanatStoryActivity.class)
                .setGameActivityClass(BanatGameActivity.class)
                .addLocation(
                    "Timișoara", 
                    "Primul oraș iluminat electric din Europa", 
                    new LatLng(45.7500, 21.2333), 
                    BitmapDescriptorFactory.HUE_RED,
                    1,
                    Timisoara.class
                )
                .addLocation(
                    "Reșița", 
                    "Centru industrial istoric", 
                    new LatLng(45.3000, 21.8833), 
                    BitmapDescriptorFactory.HUE_RED,
                    2,
                    Resita.class
                )
                .addLocation(
                    "Băile Herculane", 
                    "Stațiune balneară antică", 
                    new LatLng(44.8833, 22.4167), 
                    BitmapDescriptorFactory.HUE_GREEN,
                    3,
                    BaileHerculane.class
                )
                .addLocation(
                    "Parcul Național Cheile Nerei", 
                    "Rezervație naturală spectaculoasă", 
                    new LatLng(44.9167, 21.8000), 
                    BitmapDescriptorFactory.HUE_GREEN,
                    4
                )
                .addLocation(
                    "Castelul Huniade", 
                    "Cel mai vechi monument medieval din Timișoara", 
                    new LatLng(45.7500, 21.2333), 
                    BitmapDescriptorFactory.HUE_VIOLET,
                    5
                );
        
        // Inițializăm datele pentru Crișana
        RegionMapData crisanaData = new RegionMapData(
            "Crișana",
            new LatLng(47.0500, 21.9333), // Oradea ca centru
            8.0f
        );
        
        crisanaData.setStoryActivityClass(CrisanaStoryActivity.class)
                  .setGameActivityClass(CrisanaGameActivity.class)
                  .addLocation(
                      "Oradea", 
                      "Oraș cu arhitectură Art Nouveau", 
                      new LatLng(47.0500, 21.9333), 
                      BitmapDescriptorFactory.HUE_RED,
                      1,
                      Oradea.class
                  )
                  .addLocation(
                      "Arad", 
                      "Oraș important la granița de vest", 
                      new LatLng(46.1833, 21.3167), 
                      BitmapDescriptorFactory.HUE_RED,
                      2,
                      Arad.class
                  )
                  .addLocation(
                      "Stâna de Vale", 
                      "Stațiune montană", 
                      new LatLng(46.6833, 22.6167), 
                      BitmapDescriptorFactory.HUE_GREEN,
                      3
                  )
                  .addLocation(
                      "Băile Felix", 
                      "Stațiune balneară renumită", 
                      new LatLng(47.0167, 21.9167), 
                      BitmapDescriptorFactory.HUE_GREEN,
                      4
                  )
                  .addLocation(
                      "Cetatea Șoimoș", 
                      "Cetate medievală", 
                      new LatLng(46.1167, 21.4333), 
                      BitmapDescriptorFactory.HUE_ORANGE,
                      5
                  );
        
        // Inițializăm datele pentru Muntenia
        RegionMapData munteniaData = new RegionMapData(
            "Muntenia",
            new LatLng(44.4333, 26.1000), // București ca centru
            7.5f
        );
        
        munteniaData.setStoryActivityClass(MunteniaTourActivity.class)
                   .setGameActivityClass(MunteniaGameActivity.class)
                   .addLocation(
                       "București", 
                       "Capitala României", 
                       new LatLng(44.4333, 26.1000), 
                       BitmapDescriptorFactory.HUE_RED,
                       1,
                       Bucuresti.class
                   )
                   .addLocation(
                       "Sinaia", 
                       "Perla Carpaților", 
                       new LatLng(45.3500, 25.5500), 
                       BitmapDescriptorFactory.HUE_GREEN,
                       2
                   )
                   .addLocation(
                       "Castelul Peleș", 
                       "Fostă reședință regală", 
                       new LatLng(45.3600, 25.5425), 
                       BitmapDescriptorFactory.HUE_VIOLET,
                       3
                   )
                   .addLocation(
                       "Târgoviște", 
                       "Fostă capitală a Țării Românești", 
                       new LatLng(44.9333, 25.4500), 
                       BitmapDescriptorFactory.HUE_ORANGE,
                       4,
                       Targoviste.class
                   )
                   .addLocation(
                       "Curtea de Argeș", 
                       "Mănăstirea Episcopală", 
                       new LatLng(45.1500, 24.6667), 
                       BitmapDescriptorFactory.HUE_AZURE,
                       5
                   );
        
        // Adăugăm datele în mapă
        regionDataMap.put("transilvania", transilvaniaData);
        regionDataMap.put("maramures", maramuresData);
        regionDataMap.put("oltenia", olteniaData);
        regionDataMap.put("moldova", moldovaData);
        regionDataMap.put("bucovina", bucovinaData);
        regionDataMap.put("dobrogea", dobrogeaData);
        regionDataMap.put("banat", banatData);
        regionDataMap.put("crisana", crisanaData);
        regionDataMap.put("muntenia", munteniaData);
    }
    
    /**
     * Obține datele pentru o regiune
     * @param regionName Numele regiunii
     * @return Datele regiunii sau null dacă nu există
     */
    public RegionMapData getRegionData(String regionName) {
        if (regionName == null) return null;
        
        // Convertim numele regiunii la lowercase pentru a evita probleme de case sensitivity
        String normalizedName = regionName.toLowerCase();
        
        return regionDataMap.get(normalizedName);
    }
} 