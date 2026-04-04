package com.example.myapplication.core.domain.repository;

import android.content.Context;
import com.example.myapplication.core.domain.model.CityData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CityRepository {
    private static CityRepository instance;
    private List<CityData> cachedCities;

    private CityRepository() { }

    public static synchronized CityRepository getInstance() {
        if (instance == null) {
            instance = new CityRepository();
        }
        return instance;
    }

    public List<CityData> getCities(Context context) {
        if (cachedCities != null) return cachedCities;
        
        cachedCities = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("cities_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(json);
            JSONArray citiesArray = jsonObject.getJSONArray("cities");
            
            for (int i = 0; i < citiesArray.length(); i++) {
                JSONObject cityObj = citiesArray.getJSONObject(i);
                
                List<String> images = new ArrayList<>();
                JSONArray imgArr = cityObj.getJSONArray("defaultImages");
                for (int j = 0; j < imgArr.length(); j++) images.add(imgArr.getString(j));
                
                List<String> events = new ArrayList<>();
                JSONArray evArr = cityObj.getJSONArray("events");
                for (int j = 0; j < evArr.length(); j++) events.add(evArr.getString(j));
                
                List<String> tips = new ArrayList<>();
                JSONArray tipArr = cityObj.getJSONArray("tips");
                for (int j = 0; j < tipArr.length(); j++) tips.add(tipArr.getString(j));
                
                List<CityData.AttractionData> attractions = new ArrayList<>();
                JSONArray attArr = cityObj.getJSONArray("attractions");
                for (int j = 0; j < attArr.length(); j++) {
                    JSONObject attObj = attArr.getJSONObject(j);
                    attractions.add(new CityData.AttractionData(
                        attObj.getString("name"),
                        attObj.getString("imageRes"),
                        attObj.getString("prompt")
                    ));
                }
                
                List<CityData.SectionData> sections = new ArrayList<>();
                if (cityObj.has("sections")) {
                    JSONArray secArr = cityObj.getJSONArray("sections");
                    for (int j = 0; j < secArr.length(); j++) {
                        JSONObject secObj = secArr.getJSONObject(j);
                        sections.add(new CityData.SectionData(
                            secObj.getString("title"),
                            secObj.getString("content"),
                            secObj.optBoolean("highlighted", false)
                        ));
                    }
                }
                
                CityData cityData = new CityData(
                    cityObj.getString("id"),
                    cityObj.getString("name"),
                    cityObj.optString("region", ""),
                    cityObj.optString("description", ""),
                    cityObj.optString("mapCoords", ""),
                    cityObj.optString("weatherId", ""),
                    images, events, tips, attractions, sections
                );
                cachedCities.add(cityData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cachedCities;
    }

    public CityData getCityById(Context context, String id) {
        if (cachedCities == null) getCities(context);
        if (cachedCities != null) {
            for (CityData city : cachedCities) {
                if (city.getId().equalsIgnoreCase(id)) return city;
            }
        }
        return null;
    }
}
