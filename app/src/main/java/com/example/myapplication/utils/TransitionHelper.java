package com.example.myapplication.utils;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.util.Pair;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

/**
 * Helper pentru tranziții fluente între activități
 */
public class TransitionHelper {

    /**
     * Pornește o activitate cu tranziție fade
     */
    public static void startActivityWithFade(Activity activity, Intent intent) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(
                activity,
                android.R.anim.fade_in,
                android.R.anim.fade_out);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    /**
     * Pornește o activitate cu tranziție slide
     */
    public static void startActivityWithSlide(Activity activity, Intent intent) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(
                activity,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    /**
     * Pornește o activitate cu tranziție shared element
     */
    public static void startActivityWithSharedElement(Activity activity, Intent intent, View sharedElement, String transitionName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    activity,
                    sharedElement,
                    transitionName);
            activity.startActivity(intent, options.toBundle());
        } else {
            startActivityWithFade(activity, intent);
        }
    }

    /**
     * Pornește o activitate cu mai multe elemente shared
     */
    @SafeVarargs
    public static void startActivityWithMultipleSharedElements(Activity activity, Intent intent, Pair<View, String>... sharedElements) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    activity,
                    sharedElements);
            activity.startActivity(intent, options.toBundle());
        } else {
            startActivityWithFade(activity, intent);
        }
    }
} 