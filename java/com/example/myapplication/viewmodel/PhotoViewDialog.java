package com.example.myapplication.viewmodel;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import com.example.myapplication.R;
import com.github.chrisbanes.photoview.PhotoView;

public class PhotoViewDialog extends DialogFragment {
    private static final String ARG_IMAGE_RES_ID = "image_res_id";
    private int imageResId;

    public static void show(FragmentActivity activity, int imageResId) {
        PhotoViewDialog dialog = new PhotoViewDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_RES_ID, imageResId);
        dialog.setArguments(args);
        dialog.show(activity.getSupportFragmentManager(), "photo_view");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageResId = getArguments().getInt(ARG_IMAGE_RES_ID);
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_photo_view, container, false);

        PhotoView photoView = view.findViewById(R.id.photo_view);
        ImageView closeButton = view.findViewById(R.id.close_button);

        photoView.setImageResource(imageResId);

        // Add zoom animation
        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoView.setMinimumScale(1f);
        photoView.setMaximumScale(3f);
        photoView.setMediumScale(1.75f);

        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }
}
