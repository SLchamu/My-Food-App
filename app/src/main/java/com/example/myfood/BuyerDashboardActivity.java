package com.example.myfood;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class BuyerDashboardActivity extends AppCompatActivity {

    private static final String TAG = "BuyerDashboardActivity";

    private CardView cardConnectedSellerDetails;
    private TextView tvShopName, tvSellerName, tvPhoneNumber, tvAddress, tvLocation;
    private Button btnCallConnectedSeller, btnOpenConnectedSellerMap;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String buyerUid;

    private String currentSellerPhone = "";
    private String currentSellerLocation = "";

    private final ActivityResultLauncher<ScanOptions> qrScanLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(BuyerDashboardActivity.this, "Scan Cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    String scannedSellerUid = result.getContents().trim();
                    validateAndConnectSeller(scannedSellerUid);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_dashboard);

        Button btnBack = findViewById(R.id.btnBuyerDashboardBack);
        Button btnScanSellerQr = findViewById(R.id.btnScanSellerQr);

        cardConnectedSellerDetails = findViewById(R.id.cardConnectedSellerDetails);
        tvShopName = findViewById(R.id.tvShopName);
        tvSellerName = findViewById(R.id.tvSellerName);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvAddress = findViewById(R.id.tvAddress);
        tvLocation = findViewById(R.id.tvLocation);
        btnCallConnectedSeller = findViewById(R.id.btnCallConnectedSeller);
        btnOpenConnectedSellerMap = findViewById(R.id.btnOpenConnectedSellerMap);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            buyerUid = currentUser.getUid();
        } else {
            buyerUid = "BUYER_UID_67890";
        }

        if (btnScanSellerQr != null) {
            btnScanSellerQr.setOnClickListener(v -> {
                ScanOptions options = new ScanOptions();
                options.setPrompt("Scan Seller Connection QR Code");
                options.setOrientationLocked(true);
                qrScanLauncher.launch(options);
            });
        }

        if (btnCallConnectedSeller != null) {
            btnCallConnectedSeller.setOnClickListener(v -> {
                if (!currentSellerPhone.isEmpty()) {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + currentSellerPhone));
                    startActivity(dialIntent);
                } else {
                    Toast.makeText(BuyerDashboardActivity.this, "No phone number available.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnOpenConnectedSellerMap != null) {
            btnOpenConnectedSellerMap.setOnClickListener(v -> {
                if (!currentSellerLocation.isEmpty()) {
                    Uri mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(currentSellerLocation));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(BuyerDashboardActivity.this, "No location details available.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void validateAndConnectSeller(String sellerUid) {
        Toast.makeText(this, "Validating Seller QR Code...", Toast.LENGTH_SHORT).show();

        mDatabase.child(sellerUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);

                    if ("seller".equalsIgnoreCase(role)) {
                        // 1. Save Seller UID under Buyer's connected_sellers node
                        mDatabase.child(buyerUid).child("connected_sellers").child(sellerUid).setValue(true);

                        // 2. Fetch Seller's Details
                        String name = snapshot.child("name").getValue(String.class);
                        String shopName = snapshot.child("shopName").getValue(String.class);
                        String address = snapshot.child("address").getValue(String.class);
                        String location = snapshot.child("location").getValue(String.class);
                        String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);

                        currentSellerPhone = phoneNumber != null ? phoneNumber : "";
                        currentSellerLocation = (location != null && !location.isEmpty()) ? location : (address != null ? address : "");

                        // 3. Display Details in CardView
                        tvShopName.setText("🏪 Shop Name: " + (shopName != null ? shopName : "N/A"));
                        tvSellerName.setText("👤 Seller: " + (name != null ? name : "N/A"));
                        tvPhoneNumber.setText("📞 Phone: " + (phoneNumber != null ? phoneNumber : "N/A"));
                        tvAddress.setText("📍 Address: " + (address != null ? address : "N/A"));
                        tvLocation.setText("🗺️ Location: " + (location != null ? location : "N/A"));

                        cardConnectedSellerDetails.setVisibility(View.VISIBLE);

                        Toast.makeText(BuyerDashboardActivity.this, "🎉 Connected to Seller: " + (shopName != null ? shopName : name), Toast.LENGTH_LONG).show();

                    } else {
                        // Not a seller role
                        cardConnectedSellerDetails.setVisibility(View.GONE);
                        Toast.makeText(BuyerDashboardActivity.this, "❌ Invalid QR Code! This user is not registered as a Seller.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // UID doesn't exist in Users
                    cardConnectedSellerDetails.setVisibility(View.GONE);
                    Toast.makeText(BuyerDashboardActivity.this, "❌ Invalid Seller QR Code! User not found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase validation error", error.toException());
                Toast.makeText(BuyerDashboardActivity.this, "Failed to connect to Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
