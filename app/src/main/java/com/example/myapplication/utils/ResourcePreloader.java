package com.example.myapplication.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Preîncărcarea resurselor pentru îmbunătățirea performanței UI
 */
public class ResourcePreloader {
    
    private static ResourcePreloader instance;
    private final LruCache<Integer, Drawable> drawableCache;
    private final ExecutorService executorService;
    private final Handler mainThreadHandler;
    
    // Singleton
    public static synchronized ResourcePreloader getInstance() {
        if (instance == null) {
            instance = new ResourcePreloader();
        }
        return instance;
    }
    
    private ResourcePreloader() {
        // Determinăm dimensiunea optimă a memoriei cache (1/8 din memoria heap disponibilă)
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        
        drawableCache = new LruCache<Integer, Drawable>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Drawable drawable) {
                // Calculăm dimensiunea pentru fiecare drawable stocat
                if (drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    return bitmap.getByteCount() / 1024;
                }
                return 1;
            }
        };
        
        // Executor pentru operații pe fire de execuție separate
        executorService = Executors.newFixedThreadPool(3);
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Preîncărcarea unui array de resurse drawable
     */
    public void preloadDrawables(Context context, int[] resourceIds) {
        for (int resourceId : resourceIds) {
            if (drawableCache.get(resourceId) == null) {
                loadDrawableAsync(context, resourceId, null);
            }
        }
    }
    
    /**
     * Încărcarea asincronă a unui drawable
     */
    public void loadDrawableAsync(Context context, int resourceId, OnDrawableLoadedListener listener) {
        // Verificare cache mai întâi
        Drawable cachedDrawable = drawableCache.get(resourceId);
        if (cachedDrawable != null) {
            if (listener != null) {
                mainThreadHandler.post(() -> listener.onDrawableLoaded(cachedDrawable));
            }
            return;
        }
        
        // Încărcare pe un fir de execuție separat
        executorService.execute(() -> {
            try {
                // Încărcăm drawable-ul optimizat
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565; // Optimizare memorie
                
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
                final BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
                
                // Adăugăm în cache
                drawableCache.put(resourceId, drawable);
                
                // Callback pe firul principal
                if (listener != null) {
                    mainThreadHandler.post(() -> listener.onDrawableLoaded(drawable));
                }
            } catch (Exception e) {
                if (listener != null) {
                    mainThreadHandler.post(() -> listener.onDrawableLoadError(e));
                }
            }
        });
    }
    
    /**
     * Curăță cache-ul
     */
    public void clearCache() {
        drawableCache.evictAll();
    }
    
    /**
     * Interfață pentru callback de încărcare
     */
    public interface OnDrawableLoadedListener {
        void onDrawableLoaded(Drawable drawable);
        default void onDrawableLoadError(Exception e) {}
    }
    
    /**
     * Eliberarea resurselor
     */
    public void shutdown() {
        executorService.shutdown();
        drawableCache.evictAll();
    }
} 