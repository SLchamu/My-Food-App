package com.example.myfood;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class SellerQR extends AppCompatActivity {

    private static final String TAG = "SellerQR";
    private Bitmap sellerQrBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_qr);
        AnimatedBackgroundHelper.setup(this);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        View cardQrDetails = findViewById(R.id.cardQrDetails);
        animateEntry(cardQrDetails, 0);

        Button btnQrBack = findViewById(R.id.btnQrBack);
        ImageView ivQrCode = findViewById(R.id.ivQrCode);
        TextView tvSellerInfo = findViewById(R.id.tvSellerInfo);
        Button btnDownloadSellerQr = findViewById(R.id.btnDownloadSellerQr);
        Button btnDone = findViewById(R.id.btnDone);
        View btnNotification = findViewById(R.id.btnNotification);

        if (btnQrBack != null) {
            btnQrBack.setOnClickListener(v -> {
                Intent intent = new Intent(SellerQR.this, SellerProfileActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(SellerQR.this, SellerOrdersActivity.class);
                startActivity(intent);
            });
        }

        String qrData = getIntent().getStringExtra("SELLER_DATA");
        String sellerName = getIntent().getStringExtra("SELLER_NAME");
        String shopName = getIntent().getStringExtra("SHOP_NAME");

        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

        // Generate Seller Details QR Code
        if (qrData != null && !qrData.isEmpty() && ivQrCode != null) {
            try {
                sellerQrBitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 500, 500);
                ivQrCode.setImageBitmap(sellerQrBitmap);
            } catch (Exception e) {
                Log.e(TAG, "Error generating QR Code", e);
                Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
            }

            if (tvSellerInfo != null) {
                String displayInfo = "Shop: " + (shopName != null ? shopName : "") + "\nSeller: " + (sellerName != null ? sellerName : "");
                tvSellerInfo.setText(displayInfo);
            }
        }

        if (btnDownloadSellerQr != null) {
            btnDownloadSellerQr.setOnClickListener(v -> saveBitmapToGallery(sellerQrBitmap, "Seller_Details_QR_" + (shopName != null ? shopName.replaceAll("\\s+", "_") : "Shop")));
        }

        if (btnDone != null) {
            btnDone.setOnClickListener(v -> {
                Intent intent = new Intent(SellerQR.this, SellerOrdersActivity.class);
                startActivity(intent);
                finish();
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

    private void saveBitmapToGallery(Bitmap bitmap, String title) {
        if (bitmap == null) {
            Toast.makeText(this, "QR Code image not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, title + "_" + System.currentTimeMillis() + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyFood");
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (imageUri != null) {
                    try (OutputStream fos = resolver.openOutputStream(imageUri)) {
                        if (fos != null) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        }
                    }
                    Toast.makeText(this, "🎉 QR Code saved to Pictures/MyFood!", Toast.LENGTH_LONG).show();
                }
            } else {
                File imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File image = new File(imagesDir, title + "_" + System.currentTimeMillis() + ".jpg");
                try (FileOutputStream fos = new FileOutputStream(image)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                }
                Toast.makeText(this, "🎉 QR Code saved to Pictures/MyFood!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving QR Code image", e);
            Toast.makeText(this, "Failed to save QR Code image", Toast.LENGTH_SHORT).show();
        }
    }
}
