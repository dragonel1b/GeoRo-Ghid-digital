package com.example.myapplication.viewmodel;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.example.myapplication.R;
import com.example.myapplication.viewmodel.CityViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.RomApp.PointsManager;

public class CityInfoBottomSheet extends BottomSheetDialogFragment {
    private CityViewModel viewModel;
    private String cityName;
    private String description;
    private int imageResId;

    private boolean isImportant;
    private OnCityVisitedListener visitedListener;
    public static final String ARG_CITY_NAME = "city_name";
    public static final String ARG_REGION_NAME = "region_name";
    public static final String ARG_POINTS = "points";

    public static CityInfoBottomSheet newInstance(String cityName, String description,
                                                  int imageResId, boolean isImportant) {
        CityInfoBottomSheet fragment = new CityInfoBottomSheet();
        Bundle args = new Bundle();
        args.putString("cityName", cityName);
        args.putString("description", description);
        args.putInt("imageResId", imageResId);
        args.putBoolean("isImportant", isImportant);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CityViewModel.class);

        if (getArguments() != null) {
            cityName = getArguments().getString("cityName");
            description = getArguments().getString("description");
            imageResId = getArguments().getInt("imageResId");
            isImportant = getArguments().getBoolean("isImportant");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_city_info, container, false);

        Bundle args = getArguments();
        if (args != null) {
            String regionName = args.getString(ARG_REGION_NAME);
            int points = args.getInt(ARG_POINTS);

            TextView cityNameView = view.findViewById(R.id.cityName);
            TextView regionNameView = view.findViewById(R.id.regionName);
            TextView pointsView = view.findViewById(R.id.points);

            cityNameView.setText(cityName);
            regionNameView.setText(regionName);
            pointsView.setText(String.valueOf(points));
        }

        ImageView cityImage = view.findViewById(R.id.cityImage);
        TextView cityTitle = view.findViewById(R.id.cityTitle);
        TextView cityDescription = view.findViewById(R.id.cityDescription);
        MaterialButton visitButton = view.findViewById(R.id.visitButton);

        if (cityImage != null) {
            cityImage.setImageResource(imageResId);
        }
        if (cityTitle != null) {
            cityTitle.setText(cityName);
        }
        if (cityDescription != null) {
            if (description != null) {
                // Use the provided description
                cityDescription.setText(description);
            }
        }

        // Enable image zoom on click
        if (cityImage != null) {
            cityImage.setOnClickListener(v -> {
                // Launch PhotoView activity/dialog
                if (getActivity() != null) {
                    PhotoViewDialog.show(getActivity(), imageResId);
                }
            });
        }

        if (visitButton != null) {
            visitButton.setOnClickListener(v -> {
                if (visitedListener != null) {
                    visitedListener.onCityVisited(cityName);
                }
                viewModel.addPoints(100); // Award points for visiting
                viewModel.incrementVisitedCities();
                dismiss();
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add click listener to close button
        view.findViewById(R.id.closeButton).setOnClickListener(v -> dismiss());
    }

    public void setOnCityVisitedListener(OnCityVisitedListener listener) {
        this.visitedListener = listener;
    }

    public interface OnCityVisitedListener {
        void onCityVisited(String cityName);
    }
}
