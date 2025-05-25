package com.example.myapplication.viewmodel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.example.myapplication.R;

/**
 * Helper class for adding attraction views to city activities
 */
public class AttractionHelper {

    /**
     * Adds an attraction view to the specified container
     * 
     * @param context The context
     * @param container The container to add the view to
     * @param attractionName The name of the attraction
     * @param attractionImageRes The resource ID of the attraction image
     * @param opinionHint The hint text for the opinion input field
     * @return The created view
     */
    public static View addAttraction(Context context, ViewGroup container, 
                                   String attractionName, int attractionImageRes, 
                                   String opinionHint) {
        
        LayoutInflater inflater = LayoutInflater.from(context);
        View attractionView = inflater.inflate(R.layout.item_attraction, container, false);
        
        // Set attraction name
        TextView titleView = attractionView.findViewById(R.id.titleAttraction);
        titleView.setText(attractionName);
        
        // Set attraction image
        ImageView imageView = attractionView.findViewById(R.id.imageAttraction);
        imageView.setImageResource(attractionImageRes);
        
        // Set opinion hint
        TextInputLayout opinionLayout = attractionView.findViewById(R.id.opinionAttraction);
        opinionLayout.setHint("Părerea ta despre " + attractionName);
        
        // Add to container
        container.addView(attractionView);
        
        return attractionView;
    }
} 