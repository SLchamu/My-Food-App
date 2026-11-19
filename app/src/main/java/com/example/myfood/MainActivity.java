package com.example.myfood;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AnimatedBackgroundHelper.setup(this);

        View layoutHeader = findViewById(R.id.layoutHeader);
        View cardBuyer = findViewById(R.id.cardBuyer);
        View cardSeller = findViewById(R.id.cardSeller);
        Button btnViewFoods = findViewById(R.id.btnViewFoods);
        Button btnMyOrders = findViewById(R.id.btnMyOrders);

        // Run Flutter-style entry animations
        animateEntry(layoutHeader, 0);
        animateEntry(cardBuyer, 150);
        animateEntry(cardSeller, 300);

        View.OnClickListener buyerListener = v -> animateClickAndRun(v, this::openBuyerScreen);
        View.OnClickListener sellerListener = v -> animateClickAndRun(v, this::openSellerScreen);

        if (cardBuyer != null) cardBuyer.setOnClickListener(buyerListener);
        if (cardSeller != null) cardSeller.setOnClickListener(sellerListener);
        if (btnViewFoods != null) btnViewFoods.setOnClickListener(buyerListener);
        if (btnMyOrders != null) btnMyOrders.setOnClickListener(sellerListener);
    }

    private void animateEntry(View view, long delay) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setTranslationY(80f);

        ObjectAnimator fade = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator translate = ObjectAnimator.ofFloat(view, "translationY", 80f, 0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fade, translate);
        set.setDuration(600);
        set.setStartDelay(delay);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
    }

    private void animateClickAndRun(View view, Runnable action) {
        if (view == null) {
            action.run();
            return;
        }
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .withEndAction(action)
                        .start())
                .start();
    }

    private void openBuyerScreen() {
        SharedPreferences buyerPrefs = getSharedPreferences("BuyerPrefs", MODE_PRIVATE);
        boolean isBuyerLoggedIn = buyerPrefs.getBoolean("is_buyer_logged_in", false);
        if (isBuyerLoggedIn) {
            Intent intent = new Intent(MainActivity.this, FoodMenuActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this, Buyer.class);
            startActivity(intent);
        }
    }

    private void openSellerScreen() {
        SharedPreferences prefs = getSharedPreferences("SellerPrefs", MODE_PRIVATE);
        boolean isRegistered = prefs.getBoolean("is_registered", false);

        if (isRegistered) {
            Intent intent = new Intent(MainActivity.this, SellerProfileActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this, Seller.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_account) {
            openSellerScreen();
            return true;
        } else if (id == R.id.action_about) {
            Toast.makeText(this, "My Food App v1.0", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
