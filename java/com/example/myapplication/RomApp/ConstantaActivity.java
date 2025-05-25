package com.example.myapplication.RomApp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.HapticFeedbackConstants;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class ConstantaActivity extends BaseCityActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int IMAGE_CAPTURE_REQUEST = 1;
    private static final int IMAGE_PICK_REQUEST = 2;

    private SharedPreferences sharedPreferences;
    private String currentPhotoPath;
    private ImageView currentLocationImage;
    private String currentLocationKey;
    private View loadingOverlay;
    private ArrayList<String> cityImages;

    private static final String[] LOCATIONS = {
            "Cazinoul din Constanța",
            "Plaja Modern",
            "Portul Tomis",
            "Moscheea Carol I"
    };

    private static final String[] DESCRIPTIONS = {
            "O clădire emblematică, aflată pe malul mării, cu o arhitectură impresionantă, care atrage turiști din întreaga lume.",
            "Una dintre cele mai populare plaje din Constanța, cu nisip fin și apă curată.",
            "Un port pitoresc cu ambarcațiuni de agrement și restaurante cu specific pescăresc.",
            "O moschee impunătoare, construită în stilul arhitecturii otomane."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize preferences and loading overlay
        sharedPreferences = getSharedPreferences("ConstantaData", MODE_PRIVATE);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }

        // Set up FAB
        FloatingActionButton fabAddPhoto = findViewById(R.id.fabAddPhoto);
        fabAddPhoto.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            currentLocationImage = null; // Reset for new image
            showImageSourceDialog();
        });

        // Set up image adapter listeners
        if (imageAdapter != null) {
            imageAdapter.setOnImageLongClickListener((position, imageView) -> {
                showImageEditDialog(position, imageView);
                return true;
            });

            imageAdapter.setOnImageClickListener((position, imageView) -> {
                // Handle image click if needed
            });
        }

        initializeSpecificContent();
    }

    @Override
    protected String getCityName() {
        return "Constanța";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        if (cityImages == null) {
            // Load saved images from SharedPreferences
            cityImages = new ArrayList<>();
            String savedImages = sharedPreferences.getString("constanta_user_images", "");
            if (!savedImages.isEmpty()) {
                cityImages.addAll(Arrays.asList(savedImages.split(",")));
            }

            // If no saved images, start with an empty list
            if (cityImages.isEmpty()) {
                cityImages = new ArrayList<>();
            }
        }
        return cityImages;
    }

    private void saveUserImages() {
        if (cityImages != null && !cityImages.isEmpty()) {
            String imagesString = String.join(",", cityImages);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("constanta_user_images", imagesString);
            editor.apply();
        }
    }

    private void showImageEditDialog(int position, ImageView imageView) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_image, null);
        ImageView previewImage = dialogView.findViewById(R.id.previewImage);

        // Set the current image in the preview
        String imageName = cityImages.get(position);
        int resourceId = getResources().getIdentifier(
                imageName, "drawable", getPackageName());
        previewImage.setImageResource(resourceId);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true);

        AlertDialog dialog = builder.create();

        // Setup button click listeners
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnModify).setOnClickListener(v -> {
            dialog.dismiss();
            prepareImageCapture(imageView, imageName);
            showImageSourceDialog();
        });

        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            // Show confirmation dialog before deleting
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Confirmare ștergere")
                    .setMessage("Sigur doriți să ștergeți această imagine?")
                    .setPositiveButton("Șterge", (dialogInterface, i) -> {
                        cityImages.remove(position);
                        imageAdapter.removeImage(position);

                        // Show undo snackbar
                        Snackbar.make(findViewById(R.id.cityContentContainer),
                                        "Imagine ștearsă", Snackbar.LENGTH_LONG)
                                .setAction("Anulează", v2 -> {
                                    // Restore the image
                                    cityImages.add(position, imageName);
                                    imageAdapter.notifyItemInserted(position);
                                })
                                .show();

                        dialog.dismiss();
                    })
                    .setNegativeButton("Anulează", null)
                    .show();
        });

        dialog.show();
    }

    @Override
    protected void initializeSpecificContent() {
        LinearLayout container = findViewById(R.id.cityContentContainer);

        // Add history section
        addSection(container, "Istorie",
                "Constanța, cunoscută în antichitate sub numele de Tomis, " +
                        "este cel mai vechi oraș atestat de pe teritoriul României, " +
                        "având o istorie de peste 2500 de ani. Orașul a fost întemeiat " +
                        "de coloniști greci în secolul VI î.Hr.");

        // Add locations section
        for (int i = 0; i < LOCATIONS.length; i++) {
            addLocationCard(container, LOCATIONS[i], DESCRIPTIONS[i]);
        }

        // Add culture section
        addSection(container, "Cultură și Tradiții",
                "Constanța este un oraș multicultural, unde conviețuiesc " +
                        "români, turci, tătari, greci și alte etnii. Această diversitate " +
                        "se reflectă în arhitectură, gastronomie și evenimente culturale.");
    }

    private void addLocationCard(LinearLayout container, String title, String description) {
        View cardView = getLayoutInflater().inflate(R.layout.card_location, container, false);

        TextView titleView = cardView.findViewById(R.id.locationTitle);
        TextView descView = cardView.findViewById(R.id.locationDescription);
        ImageView imageView = cardView.findViewById(R.id.locationImage);
        MaterialButton photoButton = cardView.findViewById(R.id.takePhotoButton);
        TextInputEditText opinionInput = cardView.findViewById(R.id.opinionInput);
        TextInputEditText recommendationInput = cardView.findViewById(R.id.recommendationInput);

        String locationKey = title.toLowerCase().replace(" ", "_");

        titleView.setText(title);
        descView.setText(description);

        // Load saved data
        String savedPhotoUri = sharedPreferences.getString("photo_" + locationKey, "");
        String savedOpinion = sharedPreferences.getString("opinion_" + locationKey, "");
        String savedRecommendation = sharedPreferences.getString("recommendation_" + locationKey, "");

        if (!savedPhotoUri.isEmpty()) {
            imageView.setImageURI(Uri.parse(savedPhotoUri));
            imageView.setVisibility(View.VISIBLE);
        }

        opinionInput.setText(savedOpinion);
        recommendationInput.setText(savedRecommendation);

        photoButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            prepareImageCapture(imageView, locationKey);
            showImageSourceDialog();
        });

        // Save opinion and recommendation when they change
        opinionInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("opinion_" + locationKey, opinionInput.getText().toString());
                editor.apply();
            }
        });

        recommendationInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("recommendation_" + locationKey,
                        recommendationInput.getText().toString());
                editor.apply();
            }
        });

        cardView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));
        container.addView(cardView);
    }

    private void showImageSourceDialog() {
        String[] options = {"Fă o poză", "Alege din galerie"};
        new MaterialAlertDialogBuilder(this)
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
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

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save user images
        saveUserImages();

        // Save opinions and recommendations
        View rootView = findViewById(R.id.cityContentContainer);
        if (rootView instanceof ViewGroup) {
            ViewGroup container = (ViewGroup) rootView;
            SharedPreferences.Editor editor = sharedPreferences.edit();

            for (String location : LOCATIONS) {
                String locationKey = location.toLowerCase().replace(" ", "_");

                // Find the card view for this location
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof ViewGroup) {
                        // Find opinion input
                        TextInputEditText opinionInput = child.findViewById(R.id.opinionInput);
                        if (opinionInput != null && opinionInput.getText() != null) {
                            editor.putString("opinion_" + locationKey,
                                    opinionInput.getText().toString());
                        }

                        // Find recommendation input
                        TextInputEditText recommendationInput =
                                child.findViewById(R.id.recommendationInput);
                        if (recommendationInput != null && recommendationInput.getText() != null) {
                            editor.putString("recommendation_" + locationKey,
                                    recommendationInput.getText().toString());
                        }
                    }
                }
            }

            editor.apply();
        }
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
                if (currentPhotoPath != null) {
                    Uri photoUri = Uri.fromFile(new File(currentPhotoPath));
                    if (currentLocationImage == null) {
                        // This is a new image being added to the carousel
                        addNewImageToCarousel(photoUri);
                    } else {
                        // This is updating an existing image
                        updateLocationImage(photoUri);
                    }
                    saveImageUri(photoUri.toString());
                }
            } else if (requestCode == IMAGE_PICK_REQUEST && data != null) {
                Uri selectedImage = data.getData();
                if (currentLocationImage == null) {
                    // This is a new image being added to the carousel
                    addNewImageToCarousel(selectedImage);
                } else {
                    // This is updating an existing image
                    updateLocationImage(selectedImage);
                }
                saveImageUri(selectedImage.toString());
            }
        }
    }

    private void addNewImageToCarousel(Uri imageUri) {
        String newImageName = imageUri.getLastPathSegment();
        cityImages.add(newImageName);
        imageAdapter.notifyItemInserted(cityImages.size() - 1);
        saveUserImages();

        // Show success message
        Snackbar.make(findViewById(R.id.cityContentContainer),
                "Fotografie adăugată cu succes!",
                Snackbar.LENGTH_SHORT).show();
    }

    private void updateLocationImage(Uri imageUri) {
        if (currentLocationImage != null) {
            String newImageName = imageUri.getLastPathSegment();
            // If this is a carousel image update
            if (currentLocationKey != null && currentLocationKey.contains("dobrogea_constanta")) {
                int position = getCityImages().indexOf(currentLocationKey);
                if (position != -1) {
                    ArrayList<String> images = getCityImages();
                    images.set(position, newImageName);
                    imageAdapter.updateImage(position, newImageName);
                }
            }

            // Show loading overlay
            loadingOverlay.setVisibility(View.VISIBLE);

            // Use a handler to simulate processing time and show the loading indicator
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    // Load and set the image
                    currentLocationImage.setImageURI(imageUri);
                    currentLocationImage.setVisibility(View.VISIBLE);

                    // Add success animation to the image
                    currentLocationImage.startAnimation(
                            AnimationUtils.loadAnimation(this, R.anim.photo_success));

                    // Add haptic feedback
                    currentLocationImage.performHapticFeedback(
                            HapticFeedbackConstants.VIRTUAL_KEY);

                    // Hide loading overlay
                    loadingOverlay.setVisibility(View.GONE);

                    // Show success message
                    Snackbar.make(currentLocationImage,
                                    "Fotografie adăugată cu succes!",
                                    Snackbar.LENGTH_SHORT)
                            .setAction("Anulează", v -> {
                                // Provide undo functionality
                                currentLocationImage.setImageDrawable(null);
                                currentLocationImage.setVisibility(View.GONE);
                                saveImageUri("");
                            })
                            .show();

                } catch (Exception e) {
                    // Hide loading overlay
                    loadingOverlay.setVisibility(View.GONE);

                    // Show error message
                    Snackbar.make(currentLocationImage,
                                    "Eroare la încărcarea fotografiei",
                                    Snackbar.LENGTH_LONG)
                            .setAction("Reîncearcă", v -> updateLocationImage(imageUri))
                            .show();
                }
            }, 800); // Show loading for at least 800ms for better UX
        }
    }

    private void saveImageUri(String uri) {
        if (currentLocationKey != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("photo_" + currentLocationKey, uri);
            editor.apply();
        }
    }

    private void prepareImageCapture(ImageView locationImage, String locationKey) {
        currentLocationImage = locationImage;
        currentLocationKey = locationKey;
    }
}
