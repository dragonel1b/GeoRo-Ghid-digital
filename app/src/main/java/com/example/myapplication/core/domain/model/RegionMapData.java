package com.example.myapplication.core.domain.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Clasa pentru stocarea datelor specifice fiecărei regiuni
 */
public class RegionMapData {
    // Constante pentru tipurile de locații
    public static final int LOCATION_TYPE_CITY = 1;
    public static final int LOCATION_TYPE_NATURE = 2;
    public static final int LOCATION_TYPE_CULTURE = 3;
    public static final int LOCATION_TYPE_HISTORY = 4;
    public static final int LOCATION_TYPE_OTHER = 0;
    
    private String regionName;
    private LatLng centerLocation;
    private float defaultZoom;
    private List<MapLocation> locations;
    private Class<?> storyActivityClass;
    private Class<?> gameActivityClass;

    /**
     * Constructor pentru RegionMapData
     * @param regionName Numele regiunii
     * @param centerLocation Locația centrală pentru hartă
     * @param defaultZoom Nivelul de zoom implicit
     */
    public RegionMapData(String regionName, LatLng centerLocation, float defaultZoom) {
        this.regionName = regionName;
        this.centerLocation = centerLocation;
        this.defaultZoom = defaultZoom;
        this.locations = new ArrayList<>();
    }

    /**
     * Adaugă o locație pe hartă
     * @param title Titlul locației
     * @param description Descrierea locației
     * @param position Poziția pe hartă
     * @param markerColor Culoarea markerului
     * @param id ID-ul locației
     * @param targetActivityClass Clasa activității care se va deschide la click pe marker
     * @return Obiectul RegionMapData pentru a permite înlănțuirea apelurilor
     */
    public RegionMapData addLocation(String title, String description, LatLng position, 
                                    float markerColor, int id, Class<?> targetActivityClass) {
        int locationType = getLocationTypeFromMarkerColor(markerColor);
        locations.add(new MapLocation(title, description, position, markerColor, id, targetActivityClass, locationType));
        return this;
    }

    /**
     * Adaugă o locație cu cityId (pentru CityDetailActivity)
     */
    public RegionMapData addLocation(String title, String description, LatLng position,
                                    float markerColor, int id, String cityId) {
        int locationType = getLocationTypeFromMarkerColor(markerColor);
        locations.add(new MapLocation(title, description, position, markerColor, id, cityId, locationType));
        return this;
    }

    /**
     * Adaugă o locație pe hartă
     * @param title Titlul locației
     * @param description Descrierea locației
     * @param position Poziția pe hartă
     * @param markerColor Culoarea markerului
     * @param id ID-ul locației
     * @return Obiectul RegionMapData pentru a permite înlănțuirea apelurilor
     */
    public RegionMapData addLocation(String title, String description, LatLng position, 
                                    float markerColor, int id) {
        return addLocation(title, description, position, markerColor, id, (Class<?>) null);

    }
    
    /**
     * Adaugă o locație pe hartă cu tip specific
     * @param title Titlul locației
     * @param description Descrierea locației
     * @param position Poziția pe hartă
     * @param markerColor Culoarea markerului
     * @param id ID-ul locației
     * @param targetActivityClass Clasa activității care se va deschide la click pe marker
     * @param locationType Tipul locației (LOCATION_TYPE_*)
     * @return Obiectul RegionMapData pentru a permite înlănțuirea apelurilor
     */
    public RegionMapData addLocation(String title, String description, LatLng position, 
                                    float markerColor, int id, Class<?> targetActivityClass, int locationType) {
        locations.add(new MapLocation(title, description, position, markerColor, id, targetActivityClass, locationType));
        return this;
    }
    
    /**
     * Determină tipul locației pe baza culorii markerului
     * @param markerColor Culoarea markerului
     * @return Tipul locației
     */
    private int getLocationTypeFromMarkerColor(float markerColor) {
        // Folosim culoarea markerului pentru a determina tipul locației
        if (markerColor == com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED) {
            return LOCATION_TYPE_CITY;
        } else if (markerColor == com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN) {
            return LOCATION_TYPE_NATURE;
        } else if (markerColor == com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE) {
            return LOCATION_TYPE_CULTURE;
        } else if (markerColor == com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE) {
            return LOCATION_TYPE_HISTORY;
        } else {
            return LOCATION_TYPE_OTHER;
        }
    }
    
    /**
     * Setează clasa activității pentru o locație specifică
     * @param locationId ID-ul locației
     * @param targetActivityClass Clasa activității care se va deschide la click pe marker
     * @return Obiectul RegionMapData pentru a permite înlănțuirea apelurilor
     */
    public RegionMapData setLocationActivityClass(int locationId, Class<?> targetActivityClass) {
        for (MapLocation location : locations) {
            if (location.getId() == locationId) {
                location.setTargetActivityClass(targetActivityClass);
                break;
            }
        }
        return this;
    }

    /**
     * Setează tipul unei locații specifice
     * @param locationId ID-ul locației
     * @param locationType Tipul locației (LOCATION_TYPE_*)
     * @return Obiectul RegionMapData pentru a permite înlănțuirea apelurilor
     */
    public RegionMapData setLocationType(int locationId, int locationType) {
        for (MapLocation location : locations) {
            if (location.getId() == locationId) {
                location.setLocationType(locationType);
                break;
            }
        }
        return this;
    }

    /**
     * Setează clasa activității de poveste
     * @param storyActivityClass Clasa activității de poveste
     * @return Obiectul RegionMapData pentru a permite înlănțuirea apelurilor
     */
    public RegionMapData setStoryActivityClass(Class<?> storyActivityClass) {
        this.storyActivityClass = storyActivityClass;
        return this;
    }

    /**
     * Setează clasa activității de joc
     * @param gameActivityClass Clasa activității de joc
     * @return Obiectul RegionMapData pentru a permite înlănțuirea apelurilor
     */
    public RegionMapData setGameActivityClass(Class<?> gameActivityClass) {
        this.gameActivityClass = gameActivityClass;
        return this;
    }

    /**
     * Obține numele regiunii
     * @return Numele regiunii
     */
    public String getRegionName() {
        return regionName;
    }

    /**
     * Obține locația centrală pentru hartă
     * @return Locația centrală
     */
    public LatLng getCenterLocation() {
        return centerLocation;
    }

    /**
     * Obține locația centrală pentru hartă (alias pentru getCenterLocation)
     * @return Locația centrală
     */
    public LatLng getCenter() {
        return centerLocation;
    }

    /**
     * Obține nivelul de zoom implicit
     * @return Nivelul de zoom
     */
    public float getDefaultZoom() {
        return defaultZoom;
    }

    /**
     * Obține lista de locații
     * @return Lista de locații
     */
    public List<MapLocation> getLocations() {
        return locations;
    }

    /**
     * Obține clasa activității de poveste
     * @return Clasa activității de poveste
     */
    public Class<?> getStoryActivityClass() {
        return storyActivityClass;
    }

    /**
     * Obține clasa activității de joc
     * @return Clasa activității de joc
     */
    public Class<?> getGameActivityClass() {
        return gameActivityClass;
    }

    /**
     * Clasa pentru stocarea datelor unei locații pe hartă
     */
    public static class MapLocation {
        private String title;
        private String description;
        private LatLng position;
        private float markerColor;
        private int id;
        private Class<?> targetActivityClass;
        private String cityId; // ID pentru CityDetailActivity
        private int locationType;

        /**
         * Constructor pentru MapLocation
         * @param title Titlul locației
         * @param description Descrierea locației
         * @param position Poziția pe hartă
         * @param markerColor Culoarea markerului
         * @param id ID-ul locației
         * @param targetActivityClass Clasa activității care se va deschide la click pe marker
         * @param locationType Tipul locației (LOCATION_TYPE_*)
         */
        public MapLocation(String title, String description, LatLng position, 
                          float markerColor, int id, Class<?> targetActivityClass, int locationType) {
            this.title = title;
            this.description = description;
            this.position = position;
            this.markerColor = markerColor;
            this.id = id;
            this.targetActivityClass = targetActivityClass;
            this.cityId = null;
            this.locationType = locationType;
        }

        /** Constructor cu String cityId pentru CityDetailActivity */
        public MapLocation(String title, String description, LatLng position,
                          float markerColor, int id, String cityId, int locationType) {
            this.title = title;
            this.description = description;
            this.position = position;
            this.markerColor = markerColor;
            this.id = id;
            this.targetActivityClass = null;
            this.cityId = cityId;
            this.locationType = locationType;
        }

        /** Constructor pentru compatibilitate */
        public MapLocation(String title, String description, LatLng position, 
                          float markerColor, int id, Class<?> targetActivityClass) {
            this(title, description, position, markerColor, id, targetActivityClass, LOCATION_TYPE_OTHER);
        }

        /**
         * Obține titlul locației
         * @return Titlul locației
         */
        public String getTitle() {
            return title;
        }

        /**
         * Obține descrierea locației
         * @return Descrierea locației
         */
        public String getDescription() {
            return description;
        }

        /**
         * Obține poziția pe hartă
         * @return Poziția pe hartă
         */
        public LatLng getPosition() {
            return position;
        }

        /**
         * Obține culoarea markerului
         * @return Culoarea markerului
         */
        public float getMarkerColor() {
            return markerColor;
        }

        /**
         * Obține ID-ul locației
         * @return ID-ul locației
         */
        public int getId() {
            return id;
        }

        /**
         * Obține clasa activității care se va deschide la click pe marker
         * @return Clasa activității
         */
        public Class<?> getTargetActivityClass() {
            return targetActivityClass;
        }

        /** Obține cityId pentru CityDetailActivity */
        public String getCityId() {
            return cityId;
        }
        
        /**
         * Setează clasa activității care se va deschide la click pe marker
         * @param targetActivityClass Clasa activității
         */
        public void setTargetActivityClass(Class<?> targetActivityClass) {
            this.targetActivityClass = targetActivityClass;
        }
        
        /**
         * Obține tipul locației
         * @return Tipul locației (LOCATION_TYPE_*)
         */
        public int getLocationType() {
            return locationType;
        }
        
        /**
         * Setează tipul locației
         * @param locationType Tipul locației (LOCATION_TYPE_*)
         */
        public void setLocationType(int locationType) {
            this.locationType = locationType;
        }
    }
} 