package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Manager pentru sincronizarea datelor între local (SharedPreferences) și cloud (Firebase)
 * Funcționează offline cu sincronizare automată când internetul revine
 */
public class SyncManager {
    private static final String TAG = "SyncManager";
    private static SyncManager instance;
    
    // SharedPreferences keys
    private static final String PREFS_NAME = "HybridStorage";
    private static final String KEY_PENDING_SYNC = "pending_sync_data";
    private static final String KEY_LAST_SYNC = "last_sync_timestamp";
    private static final String KEY_OFFLINE_CHANGES = "offline_changes";
    
    // Sync collections
    private static final String COLLECTION_ACHIEVEMENTS = "user_achievements";
    private static final String COLLECTION_QUIZ_RESULTS = "quiz_results";
    private static final String COLLECTION_DIFFICULTY = "user_difficulty";
    private static final String COLLECTION_PROGRESS = "user_progress";
    
    private Context context;
    private SharedPreferences prefs;
    private FirebaseFirestore firestore;
    private Gson gson;
    
    // Callback interfaces
    public interface SyncCallback {
        void onSyncComplete(boolean success, String message);
    }
    
    public interface DataCallback<T> {
        void onDataReady(T data, boolean fromCache);
    }
    
    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.firestore = FirebaseFirestore.getInstance();
        this.gson = new Gson();
    }
    
    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context);
        }
        return instance;
    }
    
    /**
     * Verifică dacă există conexiune la internet
     */
    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    // Pentru Android 6.0 (API 23) și mai nou
                    Network network = connectivityManager.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                        boolean hasInternet = capabilities != null && 
                               (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
                        
                        Log.d(TAG, "🌐 Internet check (API 23+): " + hasInternet + 
                              " (WIFI: " + (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) +
                              ", CELLULAR: " + (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) +
                              ", ETHERNET: " + (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) + ")");
                        
                        return hasInternet;
                    }
                } else {
                    // Pentru versiuni mai vechi de Android
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    boolean hasInternet = activeNetworkInfo != null && activeNetworkInfo.isConnected();
                    
                    Log.d(TAG, "🌐 Internet check (API <23): " + hasInternet + 
                          " (Connected: " + (activeNetworkInfo != null && activeNetworkInfo.isConnected()) + ")");
                    
                    return hasInternet;
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Eroare la verificarea internetului", e);
                
                // Fallback: încercăm metoda alternativă
                try {
                    NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
                    for (NetworkInfo networkInfo : networkInfos) {
                        if (networkInfo != null && networkInfo.isConnected()) {
                            Log.d(TAG, "🌐 Internet check (fallback): true");
                            return true;
                        }
                    }
                } catch (Exception fallbackException) {
                    Log.e(TAG, "❌ Eroare și la fallback", fallbackException);
                }
            }
        }
        
        Log.w(TAG, "🌐 Internet check: ConnectivityManager is null sau eroare");
        return false;
    }
    
    /**
     * Verifică dacă utilizatorul este autentificat
     */
    public boolean isUserAuthenticated() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null;
    }
    
    /**
     * Verifică dacă internetul funcționează efectiv (test de conectivitate)
     */
    public boolean isInternetWorking() {
        boolean hasConnection = isInternetAvailable();
        Log.d(TAG, "🌐 Internet working check: " + hasConnection);
        
        // Dacă detectăm o conexiune, returnăm true
        // Nu facem ping real pentru că poate fi lent și poate cauza blocări
        return hasConnection;
    }
    
    /**
     * Testează conexiunea la internet cu un ping real (opțional)
     */
    public void testInternetConnectionWithPing(SyncCallback callback) {
        new Thread(() -> {
            try {
                // Testăm cu Google DNS (8.8.8.8)
                java.net.InetAddress address = java.net.InetAddress.getByName("8.8.8.8");
                boolean isReachable = address.isReachable(3000); // 3 secunde timeout
                
                Log.d(TAG, "🌐 Ping test result: " + isReachable);
                
                if (callback != null) {
                    callback.onSyncComplete(isReachable, 
                        isReachable ? "Conexiune internet funcțională" : "Nu se poate conecta la internet");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Eroare la ping test", e);
                if (callback != null) {
                    callback.onSyncComplete(false, "Eroare la testarea conexiunii: " + e.getMessage());
                }
            }
        }).start();
    }
    
    /**
     * Salvează date ÎNTOTDEAUNA local, și în cloud dacă este posibil
     */
    public void saveData(String collection, String documentId, Map<String, Object> data, SyncCallback callback) {
        // 1. SALVARE LOCALĂ (întotdeauna)
        String localKey = collection + "_" + documentId;
        String jsonData = gson.toJson(data);
        
        prefs.edit()
            .putString(localKey, jsonData)
            .putLong(localKey + "_timestamp", System.currentTimeMillis())
            .apply();
        
        Log.d(TAG, "✅ Data saved locally: " + localKey);
        
        // 2. SALVARE CLOUD (dacă este posibil)
        if (isInternetAvailable() && isUserAuthenticated()) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            firestore.collection(collection)
                .document(userId)
                .collection("data")
                .document(documentId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Data synced to cloud: " + collection + "/" + documentId);
                    if (callback != null) {
                        callback.onSyncComplete(true, "Salvat local și sincronizat cu cloud");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "⚠️ Cloud sync failed, but data is saved locally", e);
                    // Adăugăm la queue pentru sincronizare ulterioară
                    addToPendingSync(collection, documentId, data);
                    if (callback != null) {
                        callback.onSyncComplete(true, "Salvat local (va fi sincronizat când internetul revine)");
                    }
                });
        } else {
            // Offline - adăugăm la queue pentru sincronizare ulterioară
            addToPendingSync(collection, documentId, data);
            if (callback != null) {
                callback.onSyncComplete(true, "Salvat local (offline)");
            }
        }
    }
    
    /**
     * Încarcă date - ÎNTÂI din local (rapid), apoi din cloud (dacă este mai recent)
     */
    public <T> void loadData(String collection, String documentId, Class<T> dataClass, DataCallback<T> callback) {
        String localKey = collection + "_" + documentId;
        
        // 1. ÎNCĂRCARE LOCALĂ (rapidă)
        String localJson = prefs.getString(localKey, null);
        T localData = null;
        long localTimestamp = 0;
        
        if (localJson != null) {
            try {
                localData = gson.fromJson(localJson, dataClass);
                localTimestamp = prefs.getLong(localKey + "_timestamp", 0);
                
                Log.d(TAG, "📱 Local data found: " + localKey);
                if (callback != null) {
                    callback.onDataReady(localData, true);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing local data", e);
            }
        }
        
        // 2. VERIFICARE CLOUD (dacă este disponibil)
        if (isInternetAvailable() && isUserAuthenticated()) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final T finalLocalData = localData;
            final long finalLocalTimestamp = localTimestamp;
            
            firestore.collection(collection)
                .document(userId)
                .collection("data")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            T cloudData = documentSnapshot.toObject(dataClass);
                            Long cloudTimestamp = documentSnapshot.getLong("timestamp");
                            
                            if (cloudTimestamp == null) {
                                cloudTimestamp = 0L;
                            }
                            
                            Log.d(TAG, "☁️ Cloud data found: " + localKey + 
                                  " (local: " + finalLocalTimestamp + ", cloud: " + cloudTimestamp + ")");
                            
                            // Folosim cea mai recentă versiune
                            if (cloudTimestamp > finalLocalTimestamp) {
                                // Cloud-ul este mai recent - salvăm local și returnăm
                                Map<String, Object> cloudMap = documentSnapshot.getData();
                                String cloudJson = gson.toJson(cloudMap);
                                
                                prefs.edit()
                                    .putString(localKey, cloudJson)
                                    .putLong(localKey + "_timestamp", cloudTimestamp)
                                    .apply();
                                
                                Log.d(TAG, "🔄 Updated local data from cloud");
                                if (callback != null) {
                                    callback.onDataReady(cloudData, false);
                                }
                            } else if (finalLocalTimestamp > cloudTimestamp && finalLocalData != null) {
                                // Local-ul este mai recent - sincronizăm cu cloud-ul
                                Map<String, Object> localMap = gson.fromJson(
                                    gson.toJson(finalLocalData), 
                                    new TypeToken<Map<String, Object>>(){}.getType()
                                );
                                localMap.put("timestamp", finalLocalTimestamp);
                                
                                documentSnapshot.getReference().set(localMap);
                                Log.d(TAG, "🔄 Updated cloud data from local");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing cloud data", e);
                        }
                    } else if (finalLocalData == null) {
                        // Nici local, nici cloud - returnăm null
                        if (callback != null) {
                            callback.onDataReady(null, true);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Cloud fetch failed, using local data", e);
                    // Cloud-ul nu e disponibil, dar avem deja datele locale
                });
        } else if (localData == null) {
            // Offline și fără date locale
            if (callback != null) {
                callback.onDataReady(null, true);
            }
        }
    }
    
    /**
     * Adaugă date la queue-ul pentru sincronizare ulterioară
     */
    private void addToPendingSync(String collection, String documentId, Map<String, Object> data) {
        String pendingKey = collection + "_" + documentId;
        
        // Obținem lista curentă de modificări offline
        String existingJson = prefs.getString(KEY_OFFLINE_CHANGES, "{}");
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> offlineChanges = gson.fromJson(existingJson, type);
        
        // Adăugăm timestamp pentru a știi când a fost modificat
        data.put("offline_timestamp", System.currentTimeMillis());
        offlineChanges.put(pendingKey, data);
        
        // Salvăm înapoi
        String updatedJson = gson.toJson(offlineChanges);
        prefs.edit().putString(KEY_OFFLINE_CHANGES, updatedJson).apply();
        
        Log.d(TAG, "📝 Added to pending sync: " + pendingKey);
    }
    
    /**
     * Sincronizează toate modificările offline cu cloud-ul
     */
    public void syncPendingChanges(SyncCallback callback) {
        if (!isInternetAvailable() || !isUserAuthenticated()) {
            if (callback != null) {
                callback.onSyncComplete(false, "Nu există conexiune la internet sau utilizator neautentificat");
            }
            return;
        }
        
        String offlineChangesJson = prefs.getString(KEY_OFFLINE_CHANGES, "{}");
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> offlineChanges = gson.fromJson(offlineChangesJson, type);
        
        if (offlineChanges.isEmpty()) {
            if (callback != null) {
                callback.onSyncComplete(true, "Nu există modificări de sincronizat");
            }
            return;
        }
        
        Log.d(TAG, "🔄 Starting sync of " + offlineChanges.size() + " pending changes");
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        for (Map.Entry<String, Object> entry : offlineChanges.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> data = (Map<String, Object>) entry.getValue();
            
            // Parsăm cheia pentru a obține collection și documentId
            String[] parts = key.split("_", 2);
            if (parts.length >= 2) {
                String collection = parts[0];
                String documentId = parts[1];
                
                firestore.collection(collection)
                    .document(userId)
                    .collection("data")
                    .document(documentId)
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Synced: " + key);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Sync failed: " + key, e);
                    });
            }
        }
        
        // Curățăm lista de modificări offline
        prefs.edit()
            .putString(KEY_OFFLINE_CHANGES, "{}")
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply();
        
        if (callback != null) {
            callback.onSyncComplete(true, "Modificările au fost sincronizate");
        }
    }
    
    /**
     * Forțează sincronizarea completă între local și cloud
     */
    public void forceSyncAll(SyncCallback callback) {
        if (!isInternetAvailable() || !isUserAuthenticated()) {
            if (callback != null) {
                callback.onSyncComplete(false, "Nu există conexiune la internet");
            }
            return;
        }
        
        // Primul pas: sincronizează modificările pending
        syncPendingChanges(new SyncCallback() {
            @Override
            public void onSyncComplete(boolean success, String message) {
                if (success) {
                    // Al doilea pas: verifică și sincronizează toate datele
                    syncAllCollections(callback);
                } else {
                    if (callback != null) {
                        callback.onSyncComplete(false, "Sincronizarea modificărilor pending a eșuat");
                    }
                }
            }
        });
    }
    
    /**
     * Sincronizează toate colecțiile
     */
    private void syncAllCollections(SyncCallback callback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String[] collections = {COLLECTION_ACHIEVEMENTS, COLLECTION_QUIZ_RESULTS, 
                               COLLECTION_DIFFICULTY, COLLECTION_PROGRESS};
        
        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        
        for (String collection : collections) {
            CompletableFuture<Void> task = new CompletableFuture<>();
            tasks.add(task);
            
            firestore.collection(collection)
                .document(userId)
                .collection("data")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String documentId = document.getId();
                        Map<String, Object> cloudData = document.getData();
                        
                        // Salvăm în local dacă este mai recent
                        String localKey = collection + "_" + documentId;
                        long localTimestamp = prefs.getLong(localKey + "_timestamp", 0);
                        Long cloudTimestamp = document.getLong("timestamp");
                        
                        if (cloudTimestamp != null && cloudTimestamp > localTimestamp) {
                            String jsonData = gson.toJson(cloudData);
                            prefs.edit()
                                .putString(localKey, jsonData)
                                .putLong(localKey + "_timestamp", cloudTimestamp)
                                .apply();
                        }
                    }
                    task.complete(null);
                })
                .addOnFailureListener(task::completeExceptionally);
        }
        
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                if (callback != null) {
                    callback.onSyncComplete(true, "Sincronizare completă reușită");
                }
            })
            .exceptionally(throwable -> {
                if (callback != null) {
                    callback.onSyncComplete(false, "Sincronizarea completă a eșuat parțial");
                }
                return null;
            });
    }
    
    /**
     * Returnează informații despre starea sincronizării
     */
    public Map<String, Object> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("hasInternet", isInternetAvailable());
        status.put("isAuthenticated", isUserAuthenticated());
        status.put("lastSync", new Date(prefs.getLong(KEY_LAST_SYNC, 0)));
        
        String offlineChangesJson = prefs.getString(KEY_OFFLINE_CHANGES, "{}");
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> offlineChanges = gson.fromJson(offlineChangesJson, type);
        status.put("pendingChanges", offlineChanges.size());
        
        return status;
    }
    
    /**
     * Curăță toate datele locale (pentru debug/reset)
     */
    public void clearAllLocalData() {
        prefs.edit().clear().apply();
        Log.d(TAG, "🗑️ All local data cleared");
    }
} 