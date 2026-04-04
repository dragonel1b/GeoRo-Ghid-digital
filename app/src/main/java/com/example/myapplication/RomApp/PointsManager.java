package com.example.myapplication.RomApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.core.domain.model.UserProfile;
import com.example.myapplication.repository.QuizResultRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PointsManager {
    private static final String TAG = "PointsManager";
    private static final String POINTS_PREFS = "PointsPrefs";
    private static final String TOTAL_POINTS = "totalPoints_";  // Will be appended with userId
    private static final String REGION_POINTS_PREFIX = "points_";  // Will be appended with userId_region
    private static final int POINTS_PER_LANDMARK = 20;

    private SharedPreferences sharedPreferences;
    private static PointsManager instance;
    private QuizResultRepository quizResultRepository;

    // Singleton pattern to ensure same points across activities
    public static synchronized PointsManager getInstance(Context context) {
        if (instance == null) {
            instance = new PointsManager(context.getApplicationContext());
        }
        return instance;
    }

    private PointsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(POINTS_PREFS, Context.MODE_PRIVATE);
        quizResultRepository = QuizResultRepository.getInstance();
    }

    private String getCurrentUserId(Context context) {
        // Verificăm mai întâi dacă utilizatorul este autentificat în Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        
        // Dacă nu este autentificat, folosim ID-ul salvat local
        SharedPreferences userPrefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return userPrefs.getString("current_user_id", "default");
    }

    public int getTotalPoints(Context context) {
        String userId = getCurrentUserId(context);
        return sharedPreferences.getInt(TOTAL_POINTS + userId, 0);
    }

    public String getTotalPointsWithEmoji(Context context) {
        return "" + getTotalPoints(context);
    }

    // Alias for getTotalPoints to maintain backward compatibility
    public int getPoints(Context context) {
        return getTotalPoints(context);
    }

    public String getPointsWithEmoji(Context context) {
        return "" + getPoints(context);
    }

    public void addPoints(Context context, String region, int points) {
        String userId = getCurrentUserId(context);
        int currentTotal = getTotalPoints(context);
        int regionPoints = getRegionPoints(context, region);

        // Actualizăm punctele local
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(TOTAL_POINTS + userId, currentTotal + points);
        editor.putInt(REGION_POINTS_PREFIX + userId + "_" + region, regionPoints + points);
        editor.apply();
        
        // Sincronizăm cu Firebase dacă utilizatorul este autentificat
        syncPointsWithFirebase(context, currentTotal + points);
    }

    public void removePoints(Context context, String region, int points) {
        String userId = getCurrentUserId(context);
        int currentTotal = getTotalPoints(context);
        int regionPoints = getRegionPoints(context, region);
        
        int newTotal = Math.max(0, currentTotal - points);
        int newRegionPoints = Math.max(0, regionPoints - points);

        // Actualizăm punctele local
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(TOTAL_POINTS + userId, newTotal);
        editor.putInt(REGION_POINTS_PREFIX + userId + "_" + region, newRegionPoints);
        editor.apply();
        
        // Sincronizăm cu Firebase dacă utilizatorul este autentificat
        syncPointsWithFirebase(context, newTotal);
    }
    
    /**
     * Sincronizează punctele locale cu profilul utilizatorului în Firebase
     */
    private void syncPointsWithFirebase(Context context, int totalPoints) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Utilizatorul nu este autentificat, nu putem sincroniza
            return;
        }
        
        // Obținem profilul utilizatorului din Firebase
        quizResultRepository.getUserProfile(currentUser.getUid())
            .thenAccept(userProfile -> {
                if (userProfile != null) {
                    // Actualizăm punctele în profil
                    userProfile.setQuizPoints(totalPoints);
                    
                    // Salvăm profilul actualizat
                    quizResultRepository.saveUserProfile(userProfile)
                        .thenAccept(success -> {
                            if (success) {
                                Log.d(TAG, "Punctele au fost sincronizate cu Firebase: " + totalPoints);
                            } else {
                                Log.e(TAG, "Eroare la sincronizarea punctelor cu Firebase");
                            }
                        });
                } else {
                    // Profilul nu există, îl creăm
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUserId(currentUser.getUid());
                    newProfile.setDisplayName(currentUser.getDisplayName() != null ? 
                                             currentUser.getDisplayName() : "Utilizator");
                    newProfile.setEmail(currentUser.getEmail());
                    newProfile.setQuizPoints(totalPoints);
                    
                    // Salvăm noul profil
                    quizResultRepository.saveUserProfile(newProfile)
                        .thenAccept(success -> {
                            if (success) {
                                Log.d(TAG, "Profil nou creat și puncte sincronizate: " + totalPoints);
                            } else {
                                Log.e(TAG, "Eroare la crearea profilului nou");
                            }
                        });
                }
            })
            .exceptionally(e -> {
                Log.e(TAG, "Eroare la obținerea profilului utilizatorului", e);
                return null;
            });
    }

    public int getRegionPoints(Context context, String region) {
        String userId = getCurrentUserId(context);
        return sharedPreferences.getInt(REGION_POINTS_PREFIX + userId + "_" + region, 0);
    }

    public void updateLandmarkStatus(Context context, String region, boolean isChecked) {
        // Standardizăm numele regiunii pentru a evita probleme cu case-sensitivity
        String standardizedRegion = standardizeRegionName(region);
        
        if (isChecked) {
            // Adăugăm exact 20 de puncte
            addPoints(context, standardizedRegion, 20);
            
            // Afișăm un mesaj de confirmare
            Toast.makeText(context, "+" + POINTS_PER_LANDMARK + 
                          " puncte adăugate în " + standardizedRegion + "!", 
                          Toast.LENGTH_SHORT).show();
        } else {
            // Scădem exact 20 de puncte
            removePoints(context, standardizedRegion, 20);
            
            // Afișăm un mesaj de confirmare
            Toast.makeText(context, "-" + POINTS_PER_LANDMARK + 
                          " puncte eliminate din " + standardizedRegion + "!", 
                          Toast.LENGTH_SHORT).show();
        }
    }

    // Metodă pentru standardizarea numelor regiunilor
    public String standardizeRegionName(String region) {
        if (region == null) return "romania";
        
        region = region.toLowerCase().trim();
        
        // Mapăm posibile variante de scriere la numele standard
        if (region.contains("trans") || region.contains("ardeal")) {
            return "transilvania";
        } else if (region.contains("mold")) {
            return "moldova";
        } else if (region.contains("olten")) {
            return "oltenia";
        } else if (region.contains("munte") || region.contains("valah")) {
            return "muntenia";
        } else if (region.contains("dobr")) {
            return "dobrogea";
        } else if (region.contains("bana")) {
            return "banat";
        } else if (region.contains("crisa")) {
            return "crisana";
        } else if (region.contains("mara")) {
            return "maramures";
        } else if (region.contains("buco")) {
            return "bucovina";
        }
        
        return region; // Păstrăm numele original dacă nu se potrivește cu nicio regiune
    }

    public static int getPointsPerLandmark() {
        return POINTS_PER_LANDMARK;
    }
    
    /**
     * Sincronizează toate punctele locale cu Firebase
     * Această metodă ar trebui apelată la autentificare sau la pornirea aplicației
     */
    public void syncAllPointsWithFirebase(Context context) {
        int totalPoints = getTotalPoints(context);
        syncPointsWithFirebase(context, totalPoints);
    }
    
    /**
     * Încarcă punctele din profilul Firebase în stocarea locală
     * Această metodă ar trebui apelată după autentificare
     */
    public void loadPointsFromFirebase(Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        quizResultRepository.getUserProfile(currentUser.getUid())
            .thenAccept(userProfile -> {
                if (userProfile != null) {
                    int firebasePoints = userProfile.getQuizPoints();
                    int localPoints = getTotalPoints(context);
                    
                    // Folosim valoarea maximă dintre punctele locale și cele din Firebase
                    int finalPoints = Math.max(firebasePoints, localPoints);
                    
                    // Actualizăm stocarea locală
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(TOTAL_POINTS + currentUser.getUid(), finalPoints);
                    editor.apply();
                    
                    // Dacă punctele locale sunt mai mari, actualizăm și Firebase
                    if (localPoints > firebasePoints) {
                        syncPointsWithFirebase(context, localPoints);
                    }
                    
                    Log.d(TAG, "Puncte încărcate din Firebase: " + finalPoints);
                }
            })
            .exceptionally(e -> {
                Log.e(TAG, "Eroare la încărcarea punctelor din Firebase", e);
                return null;
            });
    }
}
