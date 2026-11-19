package com.example.myfood;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SellerProfileActivity extends AppCompatActivity {

    private static final String TAG = "SellerProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_profile);
        AnimatedBackgroundHelper.setup(this);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        Button btnProfileBack = findViewById(R.id.btnProfileBack);
        ImageView ivProfileShopPhoto = findViewById(R.id.ivProfileShopPhoto);
        TextView tvProfileShopName = findViewById(R.id.tvProfileShopName);
        TextView tvProfileSellerName = findViewById(R.id.tvProfileSellerName);
        TextView tvProfileShopAddress = findViewById(R.id.tvProfileShopAddress);
        TextView tvProfileShopLocation = findViewById(R.id.tvProfileShopLocation);

        View cardShopPhoto = findViewById(R.id.cardShopPhoto);
        View cardDetails = findViewById(R.id.cardDetails);
        View cardOrdersAction = findViewById(R.id.cardOrdersAction);
        View cardQrAction = findViewById(R.id.cardQrAction);
        View cardEditAction = findViewById(R.id.cardEditAction);

        Button btnViewOrders = findViewById(R.id.btnViewOrders);
        Button btnViewQr = findViewById(R.id.btnViewQr);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        View btnProfileNotification = findViewById(R.id.btnProfileNotification);

        // Run entrance animations
        animateEntry(cardShopPhoto, 0);
        animateEntry(cardDetails, 150);
        animateEntry(cardOrdersAction, 300);
        animateEntry(cardQrAction, 450);
        animateEntry(cardEditAction, 600);

        SharedPreferences prefs = getSharedPreferences("SellerPrefs", MODE_PRIVATE);
        String sellerName = prefs.getString("seller_name", "N/A");
        String shopName = prefs.getString("shop_name", "N/A");
        String shopPhone = prefs.getString("shop_phone", "N/A");
        String shopAddress = prefs.getString("shop_address", "N/A");
        String shopLocation = prefs.getString("shop_location", "N/A");
        String shopPhotoUriStr = prefs.getString("shop_photo_uri", null);

        if (tvProfileShopName != null) tvProfileShopName.setText(shopName);
        if (tvProfileSellerName != null) tvProfileSellerName.setText("👤 Seller: " + sellerName + " | 📞 " + shopPhone);
        if (tvProfileShopAddress != null) tvProfileShopAddress.setText("📍 Address: " + shopAddress);
        if (tvProfileShopLocation != null) tvProfileShopLocation.setText("🗺️ Location: " + shopLocation);

        if (ivProfileShopPhoto != null && shopPhotoUriStr != null) {
            try {
                ivProfileShopPhoto.setImageURI(Uri.parse(shopPhotoUriStr));
            } catch (Throwable t) {
                Log.e(TAG, "Error loading shop photo URI", t);
                ivProfileShopPhoto.setImageResource(R.mipmap.ic_launcher_round);
            }
        }

        if (btnProfileBack != null) {
            btnProfileBack.setOnClickListener(v -> finish());
        }

        // Open Orders page
        Runnable openOrdersAction = () -> {
            Intent intent = new Intent(SellerProfileActivity.this, SellerOrdersActivity.class);
            startActivity(intent);
        };

        if (cardOrdersAction != null) {
            cardOrdersAction.setOnClickListener(v -> animateClickAndRun(v, openOrdersAction));
        }
        if (btnViewOrders != null) {
            btnViewOrders.setOnClickListener(v -> openOrdersAction.run());
        }
        if (btnProfileNotification != null) {
            btnProfileNotification.setOnClickListener(v -> openOrdersAction.run());
        }

        // Open QR page
        Runnable openQrAction = () -> {
            String qrPayload = "Shop Name: " + shopName + "\n" +
                    "Seller Name: " + sellerName + "\n" +
                    "Phone: " + shopPhone + "\n" +
                    "Address: " + shopAddress + "\n" +
                    "Location: " + shopLocation;

            Intent intent = new Intent(SellerProfileActivity.this, SellerQR.class);
            intent.putExtra("SELLER_DATA", qrPayload);
            intent.putExtra("SELLER_NAME", sellerName);
            intent.putExtra("SHOP_NAME", shopName);
            intent.putExtra("SHOP_PHONE", shopPhone);
            intent.putExtra("SHOP_ADDRESS", shopAddress);
            intent.putExtra("SHOP_LOCATION", shopLocation);
            startActivity(intent);
        };

        if (cardQrAction != null) {
            cardQrAction.setOnClickListener(v -> animateClickAndRun(v, openQrAction));
        }
        if (btnViewQr != null) {
            btnViewQr.setOnClickListener(v -> openQrAction.run());
        }

        // Open Edit page
        Runnable openEditAction = () -> {
            Intent intent = new Intent(SellerProfileActivity.this, Seller.class);
            startActivity(intent);
        };

        if (cardEditAction != null) {
            cardEditAction.setOnClickListener(v -> animateClickAndRun(v, openEditAction));
        }
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> openEditAction.run());
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        TextView tvProfileBadge = findViewById(R.id.tvProfileBadge);
        if (tvProfileBadge != null) {
            SharedPreferences orderPrefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
            int count = orderPrefs.getInt("order_count", 3);
            tvProfileBadge.setText(String.valueOf(count));
            tvProfileBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }
}
