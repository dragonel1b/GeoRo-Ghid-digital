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
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Chisinau extends AppCompatActivity {
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
    private ConstraintLayout[] cardViews;

    private String[] locationKeys = {
            "arcul_de_triumf",
            "catedrala_nasterea_domnului",
            "muzeul_national",
            "gradina_publica",
            "parcul_valea_morilor",
            "centrul_vechi"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chisinau); // Make sure to create this layout file

        sharedPreferences = getSharedPreferences("ChisinauData", MODE_PRIVATE);

        initializeViews();
        loadSavedData();
        setImageClickListeners();
        animateViews();

        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> goBack(v));
    }

    private void initializeViews() {
        cardViews = new ConstraintLayout[] {
                findViewById(R.id.cardArculDeTriumf),
                findViewById(R.id.cardCatedralaNastereaDomnului),
                findViewById(R.id.cardMuzeulNational),
                findViewById(R.id.cardGradinaPublica),
                findViewById(R.id.cardParculValeaMorilor),
                findViewById(R.id.cardCentrulVechi)
        };

        imageViews = new ImageView[] {
                findViewById(R.id.imageArculDeTriumf),
                findViewById(R.id.imageCatedralaNastereaDomnului),
                findViewById(R.id.imageMuzeulNational),
                findViewById(R.id.imageGradinaPublica),
                findViewById(R.id.imageParculValeaMorilor),
                findViewById(R.id.imageCentrulVechi)
        };

        checkViews = new ImageView[] {
                findViewById(R.id.checkArculDeTriumf),
                findViewById(R.id.checkCatedralaNastereaDomnului),
                findViewById(R.id.checkMuzeulNational),
                findViewById(R.id.checkGradinaPublica),
                findViewById(R.id.checkParculValeaMorilor),
                findViewById(R.id.checkCentrulVechi)
        };

        opinionViews = new TextInputEditText[] {
                findViewById(R.id.noteArculDeTriumf),
                findViewById(R.id.noteCatedralaNastereaDomnului),
                findViewById(R.id.noteMuzeulNational),
                findViewById(R.id.noteGradinaPublica),
                findViewById(R.id.noteParculValeaMorilor),
                findViewById(R.id.noteCentrulVechi)
        };

        recommendationViews = new TextInputEditText[] {
                findViewById(R.id.recommendationArculDeTriumf),
                findViewById(R.id.recommendationCatedralaNastereaDomnului),
                findViewById(R.id.recommendationMuzeulNational),
                findViewById(R.id.recommendationGradinaPublica),
                findViewById(R.id.recommendationParculValeaMorilor),
                findViewById(R.id.recommendationCentrulVechi)
        };
    }

    private void animateViews() {
        for (ConstraintLayout cardView : cardViews) {
            cardView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));
            cardView.setAnimation(null); // Prevents animation from repeating
        }
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
            // Load image
            String imagePath = sharedPreferences.getString("image_" + locationKeys[i], "");
            if (!imagePath.isEmpty()) {
                imageViews[i].setImageURI(Uri.parse(imagePath));
                checkViews[i].setVisibility(View.VISIBLE);
            }

            // Load opinions and recommendations
            String opinion = sharedPreferences.getString("opinion_" + locationKeys[i], "");
            String recommendation = sharedPreferences.getString("recommendation_" + locationKeys[i], "");

            if (opinionViews[i] != null) {
                opinionViews[i].setText(opinion);
            }
            if (recommendationViews[i] != null) {
                recommendationViews[i].setText(recommendation);
            }
        }
    }

    private void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (int i = 0; i < locationKeys.length; i++) {
            // Save opinions and recommendations
            if (opinionViews[i] != null && opinionViews[i].getText() != null) {
                editor.putString("opinion_" + locationKeys[i], opinionViews[i].getText().toString());
            }
            if (recommendationViews[i] != null && recommendationViews[i].getText() != null) {
                editor.putString("recommendation_" + locationKeys[i], recommendationViews[i].getText().toString());
            }
        }

        editor.apply();
    }

    private void showImageSourceDialog() {
        String[] options = {"Fotografiază acum", "Alege din galerie"};
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
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
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.myapplication.fileprovider", photoFile);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        if (currentPhotoPath != null && currentImageView != null) {
            currentImageView.setImageURI(Uri.parse("file://" + currentPhotoPath));
            saveImageUri("file://" + currentPhotoPath);
            showCheckMark();
        }
    }

    private void saveImageUri(String uri) {
        int index = getImageViewIndex(currentImageView);
        if (index >= 0 && index < locationKeys.length) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("image_" + locationKeys[index], uri);
            editor.apply();
        }
    }

    private void showCheckMark() {
        int index = getImageViewIndex(currentImageView);
        if (index >= 0 && index < checkViews.length) {
            checkViews[index].setVisibility(View.VISIBLE);
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

    public void goBack(View view) {
        finish();
    }
} 