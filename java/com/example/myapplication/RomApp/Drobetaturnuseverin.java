package com.example.myapplication.RomApp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Drobetaturnuseverin extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int IMAGE_CAPTURE_REQUEST = 1;
    private static final int IMAGE_PICK_REQUEST = 2;

    private ImageView currentImageView;
    private String currentPhotoPath;
    private SharedPreferences sharedPreferences;

    private ImageView[] imageViews;
    private ImageView[] checkViews;
    private TextInputEditText[] opinionViews;
    private TextInputEditText[] recommendationViews;

    private String[] locationKeys = {
            "podul_lui_traian",
            "muzeul_regiunii_portilor",
            "castelul_de_apa",
            "parcul_natural_portile",
            "cetatea_drobeta"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drobetaturnuseverin);

        sharedPreferences = getSharedPreferences("DrobetaTurnuData", MODE_PRIVATE);

        initializeViews();
        loadSavedData();
        setImageClickListeners();
    }

    private void initializeViews() {
        imageViews = new ImageView[] {
                findViewById(R.id.imagePod),
                findViewById(R.id.imageMuzeu),
                findViewById(R.id.imageCastel),
                findViewById(R.id.imageParc),
                findViewById(R.id.imageCetate)
        };

        checkViews = new ImageView[] {
                findViewById(R.id.checkPod),
                findViewById(R.id.checkMuzeu),
                findViewById(R.id.checkCastel),
                findViewById(R.id.checkParc),
                findViewById(R.id.checkCetate)
        };

        opinionViews = new TextInputEditText[] {
                findViewById(R.id.notePod),
                findViewById(R.id.noteMuzeu),
                findViewById(R.id.noteCastel),
                findViewById(R.id.noteParc),
                findViewById(R.id.noteCetate)
        };

        recommendationViews = new TextInputEditText[] {
                findViewById(R.id.recommendationPod),
                findViewById(R.id.recommendationMuzeu),
                findViewById(R.id.recommendationCastel),
                findViewById(R.id.recommendationParc),
                findViewById(R.id.recommendationCetate)
        };

        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> goBack());
    }

    private void setImageClickListeners() {
        for (ImageView imageView : imageViews) {
            imageView.setOnClickListener(v -> {
                currentImageView = imageView;
                showImageSourceDialog();
            });
        }
    }

    private void loadSavedData() {
        for (int i = 0; i < locationKeys.length; i++) {
            String imagePath = sharedPreferences.getString("image_" + locationKeys[i], "");
            if (!imagePath.isEmpty()) {
                imageViews[i].setImageURI(Uri.parse(imagePath));
                checkViews[i].setVisibility(View.VISIBLE);
            }

            String opinion = sharedPreferences.getString("opinion_" + locationKeys[i], "");
            String recommendation = sharedPreferences.getString("recommendation_" + locationKeys[i], "");
            opinionViews[i].setText(opinion);
            recommendationViews[i].setText(recommendation);
        }
    }

    private void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (int i = 0; i < locationKeys.length; i++) {
            if (opinionViews[i] != null) {
                editor.putString("opinion_" + locationKeys[i], opinionViews[i].getText().toString());
            }
            if (recommendationViews[i] != null) {
                editor.putString("recommendation_" + locationKeys[i], recommendationViews[i].getText().toString());
            }
        }
        editor.apply();
    }

    private void showImageSourceDialog() {
        String[] options = {"Fă o poză", "Alege din galerie"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Alege sursa imaginii")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        requestCameraPermission();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Eroare la crearea fișierului", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.myapplication.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permisiune refuzată", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE_REQUEST) {
                setPic();
            } else if (requestCode == IMAGE_PICK_REQUEST && data != null) {
                Uri selectedImage = data.getData();
                if (currentImageView != null && selectedImage != null) {
                    currentImageView.setImageURI(selectedImage);
                    saveImageUri(selectedImage.toString());
                    showCheckMark();
                }
            }
        }
    }

    private void setPic() {
        if (currentImageView != null && currentPhotoPath != null) {
            Uri photoUri = Uri.fromFile(new File(currentPhotoPath));
            currentImageView.setImageURI(photoUri);
            saveImageUri(photoUri.toString());
            showCheckMark();
        }
    }

    private void saveImageUri(String uri) {
        int index = getImageViewIndex(currentImageView);
        if (index != -1) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("image_" + locationKeys[index], uri);
            editor.apply();
        }
    }

    private void showCheckMark() {
        int index = getImageViewIndex(currentImageView);
        if (index != -1) {
            checkViews[index].setVisibility(View.VISIBLE);
            checkViews[index].startAnimation(
                    AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        }
    }

    private int getImageViewIndex(ImageView imageView) {
        for (int i = 0; i < imageViews.length; i++) {
            if (imageViews[i] == imageView) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    public void goBack() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
