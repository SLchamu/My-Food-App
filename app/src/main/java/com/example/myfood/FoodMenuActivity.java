package com.example.myfood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FoodMenuActivity extends AppCompatActivity {

    private static final String TAG = "FoodMenuActivity";
    private int cartItemCount = 0;
    private TextView tvCartBadge;
    private final List<String> selectedItemsList = new ArrayList<>();

    // 1. Camera QR Scanner Launcher
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(FoodMenuActivity.this, "QR Scan Cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    openNearbySellerPage(result.getContents());
                }
            }
    );

    // 2. Upload QR Photo Launcher
    private final ActivityResultLauncher<String> selectQrImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (inputStream != null) inputStream.close();

                        String qrText = decodeQrFromBitmap(bitmap);
                        if (qrText != null && !qrText.isEmpty()) {
                            Toast.makeText(FoodMenuActivity.this, "QR Code Decoded Successfully!", Toast.LENGTH_SHORT).show();
                            openNearbySellerPage(qrText);
                        } else {
                            Toast.makeText(FoodMenuActivity.this, "No valid QR code found in selected photo.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing uploaded QR image", e);
                        Toast.makeText(FoodMenuActivity.this, "Failed to read QR image photo.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> soapLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int addedSoaps = result.getData().getIntExtra("ADDED_SOAP_COUNT", 0);
                    if (addedSoaps > 0) {
                        cartItemCount += addedSoaps;
                        selectedItemsList.add("සබන් (Soaps) x " + addedSoaps);
                        updateCartBadge();
                        Toast.makeText(FoodMenuActivity.this, addedSoaps + " Soap item(s) added to cart!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> shampooLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int addedShampoos = result.getData().getIntExtra("ADDED_SHAMPOO_COUNT", 0);
                    if (addedShampoos > 0) {
                        cartItemCount += addedShampoos;
                        selectedItemsList.add("ෂැම්පු (Shampoo) x " + addedShampoos);
                        updateCartBadge();
                        Toast.makeText(FoodMenuActivity.this, addedShampoos + " Shampoo item(s) added to cart!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> stationeryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int addedItems = result.getData().getIntExtra("ADDED_STATIONERY_COUNT", 0);
                    if (addedItems > 0) {
                        cartItemCount += addedItems;
                        selectedItemsList.add("පාසල් උපකරණ (School Items) x " + addedItems);
                        updateCartBadge();
                        Toast.makeText(FoodMenuActivity.this, addedItems + " School item(s) added to cart!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> cartLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean clearCart = result.getData().getBooleanExtra(CartActivity.EXTRA_CLEAR_CART, false);
                    if (clearCart) {
                        cartItemCount = 0;
                        selectedItemsList.clear();
                        updateCartBadge();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buyer_food_menu);
        AnimatedBackgroundHelper.setup(this);

        tvCartBadge = findViewById(R.id.tvCartBadge);
        View btnBuyerProfile = findViewById(R.id.btnBuyerProfile);
        View btnBuyerSettings = findViewById(R.id.btnBuyerSettings);
        View btnCart = findViewById(R.id.btnCart);
        View btnScanSellerQr = findViewById(R.id.btnScanSellerQr);
        View btnUploadQrPhoto = findViewById(R.id.btnUploadQrPhoto);

        if (btnBuyerProfile != null) {
            btnBuyerProfile.setOnClickListener(v -> showBuyerProfileDialog());
        }

        if (btnBuyerSettings != null) {
            btnBuyerSettings.setOnClickListener(v -> showBuyerSettingsDialog());
        }

        if (btnScanSellerQr != null) {
            btnScanSellerQr.setOnClickListener(v -> {
                ScanOptions options = new ScanOptions();
                options.setPrompt("Scan Seller QR Code to Open Nearby Seller Page");
                options.setOrientationLocked(true);
                barcodeLauncher.launch(options);
            });
        }

        if (btnUploadQrPhoto != null) {
            btnUploadQrPhoto.setOnClickListener(v -> selectQrImageLauncher.launch("image/*"));
        }

        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                Intent intent = new Intent(FoodMenuActivity.this, CartActivity.class);
                intent.putStringArrayListExtra(CartActivity.EXTRA_CART_ITEMS, new ArrayList<>(selectedItemsList));
                cartLauncher.launch(intent);
            });
        }

        // 1. Soaps button launches SoapItemsActivity page
        Button btnSoap = findViewById(R.id.btnBurger);
        if (btnSoap != null) {
            btnSoap.setOnClickListener(v -> {
                Intent intent = new Intent(FoodMenuActivity.this, SoapItemsActivity.class);
                soapLauncher.launch(intent);
            });
        }

        // 2. Shampoo button launches ShampooItemsActivity page
        Button btnShampoo = findViewById(R.id.btnShampoo);
        if (btnShampoo != null) {
            btnShampoo.setOnClickListener(v -> {
                Intent intent = new Intent(FoodMenuActivity.this, ShampooItemsActivity.class);
                shampooLauncher.launch(intent);
            });
        }

        // 3. School Stationery button launches SchoolStationeryActivity page
        Button btnStationery = findViewById(R.id.btnStationery);
        if (btnStationery != null) {
            btnStationery.setOnClickListener(v -> {
                Intent intent = new Intent(FoodMenuActivity.this, SchoolStationeryActivity.class);
                stationeryLauncher.launch(intent);
            });
        }

        // 4. Map other item buttons with item names
        setupItemButton(R.id.btnPizza, "පිලිහුඩු");
        setupItemButton(R.id.btnChicken, "කුළුබඩු");
        setupItemButton(R.id.btnFries, "ක්ෂණික ආහාර");
        setupItemButton(R.id.btnDrinks, "බිම වර්ග");
    }

    private void showBuyerProfileDialog() {
        SharedPreferences buyerPrefs = getSharedPreferences("BuyerPrefs", MODE_PRIVATE);
        String name = buyerPrefs.getString("buyer_name", "Registered Buyer");
        String email = buyerPrefs.getString("buyer_email", "N/A");
        String phone = buyerPrefs.getString("buyer_phone", "N/A");
        String whatsapp = buyerPrefs.getString("buyer_whatsapp", "N/A");
        String address = buyerPrefs.getString("buyer_address", "N/A");

        String profileDetails = "👤 Name: " + name + "\n\n" +
                "✉️ Email: " + email + "\n\n" +
                "📞 Phone: " + phone + "\n\n" +
                "💬 WhatsApp: " + whatsapp + "\n\n" +
                "📍 Address: " + address;

        new AlertDialog.Builder(this)
                .setTitle("👤 Buyer Profile Details")
                .setMessage(profileDetails)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Logout", (dialog, which) -> {
                    buyerPrefs.edit().putBoolean("is_buyer_logged_in", false).apply();
                    Toast.makeText(FoodMenuActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(FoodMenuActivity.this, Buyer.class);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showBuyerSettingsDialog() {
        String[] options = {"🏪 View Connected Nearby Seller", "🚪 Logout", "Close"};
        new AlertDialog.Builder(this)
                .setTitle("⚙️ Buyer Settings")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(FoodMenuActivity.this, NearbySellerActivity.class);
                        startActivity(intent);
                    } else if (which == 1) {
                        SharedPreferences buyerPrefs = getSharedPreferences("BuyerPrefs", MODE_PRIVATE);
                        buyerPrefs.edit().putBoolean("is_buyer_logged_in", false).apply();
                        Toast.makeText(FoodMenuActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(FoodMenuActivity.this, Buyer.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBuyerNotification();
    }

    private void checkBuyerNotification() {
        SharedPreferences orderPrefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
        String notification = orderPrefs.getString("buyer_notification", null);
        if (notification != null && !notification.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("🔔 Order Status Notification")
                    .setMessage(notification)
                    .setPositiveButton("OK", (dialog, which) -> {
                        orderPrefs.edit().remove("buyer_notification").apply();
                        dialog.dismiss();
                    })
                    .show();
        }
    }

    private void openNearbySellerPage(String qrData) {
        // Auto-save scanned seller details for future loads
        SharedPreferences savedSellerPrefs = getSharedPreferences("ScannedSellerPrefs", MODE_PRIVATE);
        savedSellerPrefs.edit().putString("saved_seller_details", qrData).apply();

        Intent intent = new Intent(FoodMenuActivity.this, NearbySellerActivity.class);
        intent.putExtra("SELLER_DETAILS", qrData);

        StringBuilder itemsSummary = new StringBuilder();
        if (!selectedItemsList.isEmpty()) {
            itemsSummary.append("Selected Items:\n");
            for (String item : selectedItemsList) {
                itemsSummary.append("• ").append(item).append("\n");
            }
            itemsSummary.append("\nTotal Items: ").append(cartItemCount);
        } else {
            itemsSummary.append("No items selected from menu yet. You can order items directly!");
        }
        intent.putExtra("ORDER_ITEMS", itemsSummary.toString());

        startActivity(intent);
    }

    private String decodeQrFromBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(binaryBitmap);
            return result.getText();
        } catch (Exception e) {
            Log.e(TAG, "Error decoding QR from bitmap", e);
            return null;
        }
    }

    private void setupItemButton(int buttonId, String itemName) {
        Button btnSelect = findViewById(buttonId);
        if (btnSelect != null) {
            btnSelect.setOnClickListener(v -> {
                cartItemCount++;
                selectedItemsList.add(itemName);
                updateCartBadge();
                Toast.makeText(FoodMenuActivity.this, itemName + " added to cart!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateCartBadge() {
        if (tvCartBadge != null) {
            tvCartBadge.setText(String.valueOf(cartItemCount));
        }
    }
}
