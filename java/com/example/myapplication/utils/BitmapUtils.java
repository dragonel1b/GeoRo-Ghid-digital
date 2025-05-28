package com.example.myapplication.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for handling bitmap operations
 */
public class BitmapUtils {
    private static final String TAG = "BitmapUtils";
    private static final int MAX_IMAGE_SIZE = 1024; // Maximum width or height in pixels

    /**
     * Creates a temporary file in the app's cache directory
     */
    public static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getCacheDir();
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /**
     * Compresses and saves a bitmap to a file
     */
    public static boolean saveBitmapToFile(Bitmap bitmap, File file) {
        try (OutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to file", e);
            return false;
        }
    }

    /**
     * Loads and resizes a bitmap from a Uri
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri, int maxWidth, int maxHeight) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            if (input == null) {
                return null;
            }

            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            input.close();

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            input.close();

            // Rotate bitmap if needed
            return rotateBitmapIfNeeded(context, uri, bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error loading bitmap from uri", e);
            return null;
        }
    }

    /**
     * Convenience method that uses default max dimensions
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        return getBitmapFromUri(context, uri, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
    }

    /**
     * Calculate the optimal inSampleSize value based on required dimensions
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Rotates a bitmap according to the orientation specified in the Exif data
     */
    private static Bitmap rotateBitmapIfNeeded(Context context, Uri uri, Bitmap bitmap) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            if (input == null) {
                return bitmap;
            }

            ExifInterface exif = new ExifInterface(input);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }
            
            input.close();
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            Log.e(TAG, "Error rotating bitmap", e);
            return bitmap;
        }
    }
} 