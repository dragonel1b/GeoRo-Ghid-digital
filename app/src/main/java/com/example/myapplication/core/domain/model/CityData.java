package com.example.myapplication.core.domain.model;

import java.util.List;

public class CityData {
    private String id;
    private String name;
    private String region;
    private String description;
    private String mapCoords;
    private String weatherId;
    private List<String> defaultImages;
    private List<String> events;
    private List<String> tips;
    private List<AttractionData> attractions;
    private List<SectionData> sections;

    public CityData(String id, String name, String region, String description, String mapCoords, String weatherId, List<String> defaultImages, List<String> events, List<String> tips, List<AttractionData> attractions) {
        this(id, name, region, description, mapCoords, weatherId, defaultImages, events, tips, attractions, new java.util.ArrayList<>());
    }

    public CityData(String id, String name, String region, String description, String mapCoords, String weatherId, List<String> defaultImages, List<String> events, List<String> tips, List<AttractionData> attractions, List<SectionData> sections) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.description = description;
        this.mapCoords = mapCoords;
        this.weatherId = weatherId;
        this.defaultImages = defaultImages;
        this.events = events;
        this.tips = tips;
        this.attractions = attractions;
        this.sections = sections;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRegion() { return region; }
    public String getDescription() { return description; }
    public String getMapCoords() { return mapCoords; }
    public String getWeatherId() { return weatherId; }
    public List<String> getDefaultImages() { return defaultImages; }
    public List<String> getEvents() { return events; }
    public List<String> getTips() { return tips; }
    public List<AttractionData> getAttractions() { return attractions; }
    public List<SectionData> getSections() { return sections; }

    public static class AttractionData {
        private String name;
        private String imageRes;
        private String prompt;

        public AttractionData(String name, String imageRes, String prompt) {
            this.name = name;
            this.imageRes = imageRes;
            this.prompt = prompt;
        }

        public String getName() { return name; }
        public String getImageRes() { return imageRes; }
        public String getPrompt() { return prompt; }
    }

    public static class SectionData {
        private String title;
        private String content;
        private boolean highlighted;

        public SectionData(String title, String content, boolean highlighted) {
            this.title = title;
            this.content = content;
            this.highlighted = highlighted;
        }

        public String getTitle() { return title; }
        public String getContent() { return content; }
        public boolean isHighlighted() { return highlighted; }
    }
}
