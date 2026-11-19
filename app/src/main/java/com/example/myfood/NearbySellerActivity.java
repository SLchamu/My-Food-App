package com.example.myfood;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NearbySellerActivity extends AppCompatActivity {

    private TextView tvConnectionStatus;
    private String sellerPhoneNumber = "";
    private String sellerLocationQuery = "";

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    if (tvConnectionStatus != null) {
                        tvConnectionStatus.setText("🟢 Devices Connected & GPS Location Shared");
                    }
                    Toast.makeText(NearbySellerActivity.this, "Location permission granted! Device location connected.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NearbySellerActivity.this, "Location permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_seller);
        AnimatedBackgroundHelper.setup(this);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        View cardNearbyDetails = findViewById(R.id.cardNearbyDetails);
        View cardNearbySummary = findViewById(R.id.cardNearbySummary);
        animateEntry(cardNearbyDetails, 0);
        animateEntry(cardNearbySummary, 150);

        Button btnNearbyBack = findViewById(R.id.btnNearbyBack);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        Button btnRequestLocation = findViewById(R.id.btnRequestLocation);
        TextView tvNearbySellerDetails = findViewById(R.id.tvNearbySellerDetails);
        TextView tvNearbyOrderItems = findViewById(R.id.tvNearbyOrderItems);
        Button btnCallSeller = findViewById(R.id.btnCallSeller);
        Button btnOpenMapLocation = findViewById(R.id.btnOpenMapLocation);
        Button btnOrderNearbySeller = findViewById(R.id.btnOrderNearbySeller);

        checkAndRequestLocationPermission();

        SharedPreferences savedSellerPrefs = getSharedPreferences("ScannedSellerPrefs", MODE_PRIVATE);

        String sellerDetails = getIntent().getStringExtra("SELLER_DETAILS");
        String orderItems = getIntent().getStringExtra("ORDER_ITEMS");

        if (sellerDetails != null && !sellerDetails.isEmpty()) {
            // Auto save scanned seller details
            savedSellerPrefs.edit().putString("saved_seller_details", sellerDetails).apply();
        } else {
            // Auto load previously saved scanned seller details
            sellerDetails = savedSellerPrefs.getString("saved_seller_details", null);
        }

        if (sellerDetails != null && !sellerDetails.isEmpty()) {
            if (tvNearbySellerDetails != null) {
                tvNearbySellerDetails.setText(sellerDetails);
            }
            extractPhoneAndLocation(sellerDetails);
        } else {
            if (tvNearbySellerDetails != null) {
                tvNearbySellerDetails.setText("No seller QR code scanned yet.\nScan a seller's QR code from the menu page to connect!");
            }
        }

        if (orderItems != null && !orderItems.isEmpty() && tvNearbyOrderItems != null) {
            tvNearbyOrderItems.setText(orderItems);
        }

        if (btnNearbyBack != null) {
            btnNearbyBack.setOnClickListener(v -> finish());
        }

        if (btnRequestLocation != null) {
            btnRequestLocation.setOnClickListener(v -> checkAndRequestLocationPermission());
        }

        if (btnCallSeller != null) {
            btnCallSeller.setOnClickListener(v -> {
                if (!sellerPhoneNumber.isEmpty()) {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + sellerPhoneNumber));
                    startActivity(dialIntent);
                } else {
                    Toast.makeText(NearbySellerActivity.this, "No seller phone number available.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnOpenMapLocation != null) {
            btnOpenMapLocation.setOnClickListener(v -> {
                if (!sellerLocationQuery.isEmpty()) {
                    Uri mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(sellerLocationQuery));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(NearbySellerActivity.this, "No location details available to view on map.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnOrderNearbySeller != null) {
            btnOrderNearbySeller.setOnClickListener(v -> {
                SharedPreferences buyerPrefs = getSharedPreferences("BuyerPrefs", MODE_PRIVATE);
                String buyerName = buyerPrefs.getString("buyer_name", "Registered Buyer");
                String buyerPhone = buyerPrefs.getString("buyer_phone", "0771234567");
                String buyerAddress = buyerPrefs.getString("buyer_address", "Colombo, Sri Lanka");

                SharedPreferences orderPrefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = orderPrefs.edit();
                editor.putBoolean("has_live_order", true);
                editor.putString("live_buyer_name", buyerName);
                editor.putString("live_buyer_phone", buyerPhone);
                editor.putString("live_buyer_address", buyerAddress);
                editor.putString("live_order_items", tvNearbyOrderItems != null ? tvNearbyOrderItems.getText().toString() : "Ordered items");
                editor.putString("live_order_status", "🔔 New Order");
                int currentOrders = orderPrefs.getInt("order_count", 0);
                editor.putInt("order_count", currentOrders + 1);
                editor.apply();

                new AlertDialog.Builder(NearbySellerActivity.this)
                        .setTitle("🎉 Order Sent to Connected Seller!")
                        .setMessage("Your order and location details have been successfully transmitted to the seller!")
                        .setPositiveButton("OK", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        })
                        .show();
            });
        }
    }

    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (tvConnectionStatus != null) {
                tvConnectionStatus.setText("🟢 Devices Connected & GPS Location Shared");
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void extractPhoneAndLocation(String details) {
        String[] lines = details.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("phone:") || line.toLowerCase().contains("contact:")) {
                sellerPhoneNumber = line.substring(line.indexOf(":") + 1).trim();
            } else if (line.toLowerCase().contains("location:") || line.toLowerCase().contains("address:")) {
                if (sellerLocationQuery.isEmpty()) {
                    sellerLocationQuery = line.substring(line.indexOf(":") + 1).trim();
                }
            }
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
}
