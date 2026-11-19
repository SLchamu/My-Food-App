package com.example.myfood;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class SellerDashboardActivity extends AppCompatActivity {

    private static final String TAG = "SellerDashboardActivity";

    private ImageView ivSellerQrCode;
    private TextView tvSellerUidDisplay, tvSellerProfileDetails;
    private Button btnDownloadDashboardQr, btnGoToSellerProfile;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String sellerUid;
    private Bitmap qrCodeBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        Button btnBack = findViewById(R.id.btnSellerDashboardBack);
        ivSellerQrCode = findViewById(R.id.ivSellerQrCode);
        tvSellerUidDisplay = findViewById(R.id.tvSellerUidDisplay);
        tvSellerProfileDetails = findViewById(R.id.tvSellerProfileDetails);
        btnDownloadDashboardQr = findViewById(R.id.btnDownloadDashboardQr);
        btnGoToSellerProfile = findViewById(R.id.btnGoToSellerProfile);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            sellerUid = currentUser.getUid();
        } else {
            sellerUid = "SELLER_UID_12345";
        }

        if (tvSellerUidDisplay != null) {
            tvSellerUidDisplay.setText("Seller UID: " + sellerUid);
        }

        // Generate QR Code containing Seller UID
        generateSellerQrCode(sellerUid);

        // Fetch & display Seller Profile details from Realtime Database
        fetchSellerProfile(sellerUid);

        if (btnDownloadDashboardQr != null) {
            btnDownloadDashboardQr.setOnClickListener(v -> saveBitmapToGallery(qrCodeBitmap, "Seller_QR_" + sellerUid));
        }

        if (btnGoToSellerProfile != null) {
            btnGoToSellerProfile.setOnClickListener(v -> {
                Intent intent = new Intent(SellerDashboardActivity.this, SellerProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void generateSellerQrCode(String uid) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            qrCodeBitmap = barcodeEncoder.encodeBitmap(uid, BarcodeFormat.QR_CODE, 500, 500);
            ivSellerQrCode.setImageBitmap(qrCodeBitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error generating QR code", e);
            Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSellerProfile(String uid) {
        mDatabase.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String shopName = snapshot.child("shopName").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    String profileInfo = "🏪 Shop: " + (shopName != null ? shopName : "N/A") + "\n" +
                            "👤 Seller: " + (name != null ? name : "N/A") + "\n" +
                            "📞 Phone: " + (phoneNumber != null ? phoneNumber : "N/A") + "\n" +
                            "📍 Address: " + (address != null ? address : "N/A") + "\n" +
                            "🗺️ Location: " + (location != null ? location : "N/A") + "\n" +
                            "✉️ Email: " + (email != null ? email : "N/A");

                    tvSellerProfileDetails.setText(profileInfo);
                } else {
                    tvSellerProfileDetails.setText("No profile data found in Firebase for UID:\n" + uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error", error.toException());
                Toast.makeText(SellerDashboardActivity.this, "Failed to load seller profile", Toast.LENGTH_SHORT).show();
            }
        });
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
