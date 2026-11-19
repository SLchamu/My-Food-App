package com.example.myfood;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class AnimatedBackgroundHelper {

    private static final String TAG = "AnimatedBg";

    public static void setup(Activity activity) {
        if (activity == null) return;
        try {
            View contentRoot = activity.findViewById(android.R.id.content);
            if (contentRoot instanceof ViewGroup) {
                ViewGroup rootGroup = (ViewGroup) contentRoot;

                // Check if background image is already added
                if (rootGroup.findViewWithTag("bg_photo_tag") != null) {
                    return;
                }

                // Create beautiful background ImageView with bgphoto.jpg
                ImageView bgImageView = new ImageView(activity);
                bgImageView.setTag("bg_photo_tag");
                bgImageView.setImageResource(R.drawable.bgphoto);
                bgImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                bgImageView.setAlpha(0.70f); // Beautiful visible photo background

                // Subtle light tint overlay for maximum text contrast
                View overlay = new View(activity);
                overlay.setTag("bg_overlay_tag");
                overlay.setBackgroundColor(Color.parseColor("#40FFF8F0"));

                ViewGroup.LayoutParams matchParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                rootGroup.addView(bgImageView, 0, matchParams);
                if (rootGroup instanceof FrameLayout) {
                    rootGroup.addView(overlay, 1, matchParams);
                }

                // Smooth slow ambient zoom & pulse animation
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(bgImageView, "scaleX", 1.0f, 1.08f);
                scaleX.setDuration(12000);
                scaleX.setRepeatCount(ValueAnimator.INFINITE);
                scaleX.setRepeatMode(ValueAnimator.REVERSE);

                ObjectAnimator scaleY = ObjectAnimator.ofFloat(bgImageView, "scaleY", 1.0f, 1.08f);
                scaleY.setDuration(12000);
                scaleY.setRepeatCount(ValueAnimator.INFINITE);
                scaleY.setRepeatMode(ValueAnimator.REVERSE);

                ObjectAnimator alpha = ObjectAnimator.ofFloat(bgImageView, "alpha", 0.65f, 0.80f);
                alpha.setDuration(9000);
                alpha.setRepeatCount(ValueAnimator.INFINITE);
                alpha.setRepeatMode(ValueAnimator.REVERSE);

                scaleX.start();
                scaleY.start();
                alpha.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up animated background", e);
        }
    }
}
