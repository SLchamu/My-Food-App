package com.example.myfood;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Seller extends AppCompatActivity {

    private EditText etSellerName, etShopName, etShopPhone, etShopAddress, etShopLocation;
    private ImageView ivLocationPhoto, ivShopPhoto;
    private Uri locationPhotoUri, shopPhotoUri;

    private final ActivityResultLauncher<String> selectLocationImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    locationPhotoUri = uri;
                    ivLocationPhoto.setImageURI(uri);
                }
            }
    );

    private final ActivityResultLauncher<String> selectShopImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    shopPhotoUri = uri;
                    ivShopPhoto.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller);
        AnimatedBackgroundHelper.setup(this);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        View layoutSellerHeader = findViewById(R.id.layoutSellerHeader);
        View cardSellerForm = findViewById(R.id.cardSellerForm);
        animateEntry(layoutSellerHeader, 0);
        animateEntry(cardSellerForm, 150);

        etSellerName = findViewById(R.id.etSellerName);
        etShopName = findViewById(R.id.etShopName);
        etShopPhone = findViewById(R.id.etShopPhone);
        etShopAddress = findViewById(R.id.etShopAddress);
        etShopLocation = findViewById(R.id.etShopLocation);
        ivLocationPhoto = findViewById(R.id.ivLocationPhoto);
        ivShopPhoto = findViewById(R.id.ivShopPhoto);

        Button btnUploadLocationPhoto = findViewById(R.id.btnUploadLocationPhoto);
        Button btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        Button btnSaveSeller = findViewById(R.id.btnSaveSeller);

        if (btnUploadLocationPhoto != null) {
            btnUploadLocationPhoto.setOnClickListener(v -> selectLocationImageLauncher.launch("image/*"));
        }

        if (btnUploadPhoto != null) {
            btnUploadPhoto.setOnClickListener(v -> selectShopImageLauncher.launch("image/*"));
        }

        if (btnSaveSeller != null) {
            btnSaveSeller.setOnClickListener(v -> {
                String sellerName = etSellerName != null ? etSellerName.getText().toString().trim() : "";
                String shopName = etShopName != null ? etShopName.getText().toString().trim() : "";
                String shopPhone = etShopPhone != null ? etShopPhone.getText().toString().trim() : "";
                String shopAddress = etShopAddress != null ? etShopAddress.getText().toString().trim() : "";
                String shopLocation = etShopLocation != null ? etShopLocation.getText().toString().trim() : "";

                if (sellerName.isEmpty() || shopName.isEmpty() || shopPhone.isEmpty() || shopAddress.isEmpty() || shopLocation.isEmpty()) {
                    Toast.makeText(Seller.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                } else if (locationPhotoUri == null) {
                    Toast.makeText(Seller.this, "Please upload a location photo!", Toast.LENGTH_SHORT).show();
                } else if (shopPhotoUri == null) {
                    Toast.makeText(Seller.this, "Please upload a shop photo!", Toast.LENGTH_SHORT).show();
                } else {
                    // Save registration in SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("SellerPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("is_registered", true);
                    editor.putString("seller_name", sellerName);
                    editor.putString("shop_name", shopName);
                    editor.putString("shop_phone", shopPhone);
                    editor.putString("shop_address", shopAddress);
                    editor.putString("shop_location", shopLocation);
                    if (shopPhotoUri != null) {
                        editor.putString("shop_photo_uri", shopPhotoUri.toString());
                    }
                    if (locationPhotoUri != null) {
                        editor.putString("location_photo_uri", locationPhotoUri.toString());
                    }
                    editor.apply();

                    Toast.makeText(Seller.this, "Seller Registered Successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Seller.this, SellerDashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
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
