package com.example.myapplication.viewmodel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Clasă utilitară pentru adăugarea unui buton flotant pentru încărcarea imaginilor
 * în caruselul de imagini al orașelor.
 */
public class FloatingButtonHelper {

    private static final int PICK_IMAGE_REQUEST = 1;

    /**
     * Adaugă un buton flotant pentru încărcarea imaginilor în activitatea specificată,
     * doar dacă aceasta nu este o activitate de regiune
     * @param activity Activitatea în care se adaugă butonul
     */
    public static void addPhotoButton(Activity activity) {
        // Verificăm dacă activitatea este o regiune
        if (isRegionActivity(activity)) {
            return; // Nu adăugăm butonul în regiuni
        }
        
        // Creăm butonul flotant
        FloatingActionButton addPhotoFab = new FloatingActionButton(activity);
        addPhotoFab.setImageResource(android.R.drawable.ic_input_add);
        addPhotoFab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF8F00")));
        addPhotoFab.setSize(FloatingActionButton.SIZE_NORMAL);
        
        // Setăm parametrii de layout pentru poziționarea butonului
        CoordinatorLayout.LayoutParams fabParams = 
                new CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        fabParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        fabParams.setMargins(0, 0, 32, 300);  // Marginea în dreapta și mai sus
        addPhotoFab.setLayoutParams(fabParams);
        
        // Adăugăm listener pentru deschiderea galeriei
        addPhotoFab.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activity.startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
        
        // Obținem ViewGroup-ul root
        ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
        if (rootView != null && rootView.getChildCount() > 0) {
            View firstChild = rootView.getChildAt(0);
            if (firstChild instanceof CoordinatorLayout) {
                // Dacă primul copil este CoordinatorLayout, adăugăm FAB-ul direct
                ((CoordinatorLayout) firstChild).addView(addPhotoFab);
            } else {
                // Dacă nu găsim CoordinatorLayout, adăugăm un buton alternativ în containerul principal
                addFallbackButton(activity);
            }
        }
    }
    
    /**
     * Verifică dacă activitatea este o activitate de regiune
     * @param activity Activitatea de verificat
     * @return true dacă activitatea este o regiune, false în caz contrar
     */
    private static boolean isRegionActivity(Activity activity) {
        // Verificăm dacă numele clasei conține "RegionTemplate" sau numele unei regiuni cunoscute
        String className = activity.getClass().getName();
        return className.contains("RegionTemplate") || 
               className.contains(".Banat") ||
               className.contains(".Crisana") ||
               className.contains(".Dobrogea") ||
               className.contains(".Maramures") ||
               className.contains(".Moldova") ||
               className.contains(".Muntenia") ||
               className.contains(".Oltenia") ||
               className.contains(".Bucovina") ||
               className.contains(".Transilvania");
    }
    
    /**
     * Adaugă un buton normal ca alternativă la FloatingActionButton în cazul în care
     * CoordinatorLayout nu este disponibil
     * @param activity Activitatea în care se adaugă butonul
     */
    private static void addFallbackButton(Activity activity) {
        // Căutăm containerul principal
        LinearLayout mainContainer = activity.findViewById(activity.getResources()
                .getIdentifier("cityContentContainer", "id", activity.getPackageName()));
                
        if (mainContainer != null) {
            // Creăm un buton normal
            Button addPhotoButton = new Button(activity);
            addPhotoButton.setText("Adaugă Fotografie");
            addPhotoButton.setBackgroundColor(Color.parseColor("#FF8F00"));
            addPhotoButton.setTextColor(Color.WHITE);
            
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonParams.gravity = android.view.Gravity.END;
            buttonParams.setMargins(0, 16, 16, 16);
            addPhotoButton.setLayoutParams(buttonParams);
            
            addPhotoButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activity.startActivityForResult(intent, PICK_IMAGE_REQUEST);
            });
            
            mainContainer.addView(addPhotoButton, 0);
        } else {
            Toast.makeText(activity, "Nu s-a putut adăuga butonul pentru încărcarea imaginilor", 
                    Toast.LENGTH_SHORT).show();
        }
    }
} 