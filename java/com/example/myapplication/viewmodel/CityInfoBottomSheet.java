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
import com.google.android.material.chip.Chip;
import com.example.myapplication.R;
import com.example.myapplication.viewmodel.CityViewModel;
import androidx.lifecycle.ViewModelProvider;

public class CityInfoBottomSheet extends BottomSheetDialogFragment {
    private CityViewModel viewModel;
    private String cityName;
    private String description;
    private int imageResId;
    private boolean isImportant;
    private OnCityVisitedListener visitedListener;

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
        return inflater.inflate(R.layout.bottom_sheet_city_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView cityImage = view.findViewById(R.id.cityImage);
        TextView cityTitle = view.findViewById(R.id.cityTitle);
        TextView cityDescription = view.findViewById(R.id.cityDescription);
        Chip cityBadge = view.findViewById(R.id.cityBadge);
        MaterialButton visitButton = view.findViewById(R.id.visitButton);

        cityImage.setImageResource(imageResId);
        cityTitle.setText(cityName);
        cityDescription.setText(description);

        if (isImportant) {
            cityBadge.setVisibility(View.VISIBLE);
            cityBadge.setText("Important");
        } else {
            cityBadge.setVisibility(View.GONE);
        }

        // Enable image zoom on click
        cityImage.setOnClickListener(v -> {
            // Launch PhotoView activity/dialog
            if (getActivity() != null) {
                PhotoViewDialog.show(getActivity(), imageResId);
            }
        });

        visitButton.setOnClickListener(v -> {
            if (visitedListener != null) {
                visitedListener.onCityVisited(cityName);
            }
            viewModel.addPoints(100); // Award points for visiting
            viewModel.incrementVisitedCities();
            dismiss();
        });
    }

    public void setOnCityVisitedListener(OnCityVisitedListener listener) {
        this.visitedListener = listener;
    }

    public interface OnCityVisitedListener {
        void onCityVisited(String cityName);
    }
}
