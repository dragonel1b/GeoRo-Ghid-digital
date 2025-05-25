package com.example.myapplication.Joc1;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages locations and attractions in the game
 */
public class LocationManager {
    private static LocationManager instance;
    private Map<String, List<Location>> cityAttractions = new HashMap<>();
    private Context context;
    
    private LocationManager(Context context) {
        this.context = context.getApplicationContext();
        initializeLocations();
    }
    
    public static synchronized LocationManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocationManager(context);
        }
        return instance;
    }
    
    private void initializeLocations() {
        // Initialize attractions for each city
        addCityAttractions("Sibiu", createSibiuAttractions());
        addCityAttractions("Cluj", createClujAttractions());
        addCityAttractions("Brașov", createBrasovAttractions());
        addCityAttractions("București", createBucharestAttractions());
        addCityAttractions("Iași", createIasiAttractions());
        addCityAttractions("Timișoara", createTimisoaraAttractions());
    }
    
    private List<Location> createSibiuAttractions() {
        List<Location> attractions = new ArrayList<>();
        
        attractions.add(new Location("Piața Mare", "The Large Square is the historic center of Sibiu", 
                45.7971, 24.1499, "sibiu_piata_mare"));
        
        attractions.add(new Location("Podul Minciunilor", "The Bridge of Lies is a cast iron bridge", 
                45.7980, 24.1508, "sibiu_bridge_of_lies"));
        
        attractions.add(new Location("Muzeul Brukenthal", "The Brukenthal National Museum is the oldest museum in Romania", 
                45.7975, 24.1490, "sibiu_brukenthal"));
        
        return attractions;
    }
    
    private List<Location> createClujAttractions() {
        List<Location> attractions = new ArrayList<>();
        
        attractions.add(new Location("Piața Unirii", "Union Square is the central square of Cluj-Napoca", 
                46.7693, 23.5899, "cluj_piata_unirii"));
        
        attractions.add(new Location("Biserica Sfântul Mihail", "St. Michael's Church is a Gothic-style church", 
                46.7698, 23.5899, "cluj_st_michael"));
        
        attractions.add(new Location("Grădina Botanică", "The Botanical Garden is one of the largest in Romania", 
                46.7629, 23.5880, "cluj_botanical_garden"));
        
        return attractions;
    }
    
    private List<Location> createBrasovAttractions() {
        List<Location> attractions = new ArrayList<>();
        
        attractions.add(new Location("Biserica Neagră", "The Black Church is a Gothic-style church", 
                45.6425, 25.5889, "brasov_black_church"));
        
        attractions.add(new Location("Strada Sforii", "Rope Street is one of the narrowest streets in Europe", 
                45.6422, 25.5900, "brasov_rope_street"));
        
        attractions.add(new Location("Cetatea Brașov", "Brasov Citadel offers panoramic views of the city", 
                45.6378, 25.5836, "brasov_citadel"));
        
        return attractions;
    }
    
    private List<Location> createBucharestAttractions() {
        List<Location> attractions = new ArrayList<>();
        
        attractions.add(new Location("Palatul Parlamentului", "The Palace of Parliament is the second largest administrative building in the world", 
                44.4275, 26.0875, "bucharest_parliament"));
        
        attractions.add(new Location("Arcul de Triumf", "The Arch of Triumph commemorates Romania's World War I victory", 
                44.4672, 26.0797, "bucharest_arch"));
        
        attractions.add(new Location("Ateneul Român", "The Romanian Athenaeum is a concert hall", 
                44.4414, 26.0970, "bucharest_athenaeum"));
        
        return attractions;
    }
    
    private List<Location> createIasiAttractions() {
        List<Location> attractions = new ArrayList<>();
        
        attractions.add(new Location("Palatul Culturii", "The Palace of Culture is a cultural complex", 
                47.1560, 27.5883, "iasi_palace_of_culture"));
        
        attractions.add(new Location("Grădina Botanică", "The Botanical Garden is the oldest in Romania", 
                47.1742, 27.5530, "iasi_botanical_garden"));
        
        attractions.add(new Location("Biblioteca Centrală Universitară", "The Central University Library", 
                47.1737, 27.5719, "iasi_library"));
        
        return attractions;
    }
    
    private List<Location> createTimisoaraAttractions() {
        List<Location> attractions = new ArrayList<>();
        
        attractions.add(new Location("Piața Victoriei", "Victory Square is the central square of Timisoara", 
                45.7532, 21.2255, "timisoara_victory_square"));
        
        attractions.add(new Location("Catedrala Mitropolitană", "The Orthodox Cathedral", 
                45.7516, 21.2259, "timisoara_cathedral"));
        
        attractions.add(new Location("Piața Unirii", "Union Square is one of the oldest areas of Timisoara", 
                45.7584, 21.2292, "timisoara_union_square"));
        
        return attractions;
    }
    
    private void addCityAttractions(String cityName, List<Location> attractions) {
        cityAttractions.put(cityName, attractions);
    }
    
    public List<Location> getAttractions(String cityName) {
        return cityAttractions.getOrDefault(cityName, new ArrayList<>());
    }
    
    public List<String> getCityNames() {
        return new ArrayList<>(cityAttractions.keySet());
    }
    
    public Location getLocationById(String cityName, String locationId) {
        List<Location> attractions = getAttractions(cityName);
        for (Location location : attractions) {
            if (location.getId().equals(locationId)) {
                return location;
            }
        }
        return null;
    }
    
    public boolean isLocationVisited(Context context, String locationId) {
        String prefKey = "visited_location_" + locationId;
        return SharedPrefsHelper.getCheckboxState(context, prefKey);
    }
    
    public void markLocationVisited(Context context, String locationId) {
        String prefKey = "visited_location_" + locationId;
        SharedPrefsHelper.setCheckboxState(context, prefKey, true);
    }
    
    public int getVisitedAttractionsCount(Context context, String cityName) {
        List<Location> attractions = getAttractions(cityName);
        int count = 0;
        for (Location location : attractions) {
            if (isLocationVisited(context, location.getId())) {
                count++;
            }
        }
        return count;
    }
    
    public int getTotalAttractionsCount() {
        int count = 0;
        for (List<Location> attractions : cityAttractions.values()) {
            count += attractions.size();
        }
        return count;
    }
    
    public int getTotalVisitedAttractionsCount(Context context) {
        int count = 0;
        for (String cityName : cityAttractions.keySet()) {
            count += getVisitedAttractionsCount(context, cityName);
        }
        return count;
    }
    
    public float getExplorationPercentage(Context context) {
        int total = getTotalAttractionsCount();
        int visited = getTotalVisitedAttractionsCount(context);
        
        if (total == 0) return 0f;
        return (float) visited / total * 100;
    }
} 