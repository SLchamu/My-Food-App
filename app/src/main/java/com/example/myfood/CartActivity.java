package com.example.myfood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    public static final String EXTRA_CART_ITEMS = "EXTRA_CART_ITEMS";
    public static final String EXTRA_CLEAR_CART = "EXTRA_CLEAR_CART";

    private ArrayList<String> cartItemsList;
    private LinearLayout layoutCartItemsList;
    private TextView tvTotalItemsCount;
    private TextView tvEmptyCartMessage;
    private Button btnPlaceOrder;
    private Button btnClearCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Button btnBack = findViewById(R.id.btnCartBack);
        tvTotalItemsCount = findViewById(R.id.tvTotalItemsCount);
        layoutCartItemsList = findViewById(R.id.layoutCartItemsList);
        tvEmptyCartMessage = findViewById(R.id.tvEmptyCartMessage);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnClearCart = findViewById(R.id.btnClearCart);

        btnBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_CART_ITEMS)) {
            cartItemsList = intent.getStringArrayListExtra(EXTRA_CART_ITEMS);
        }

        if (cartItemsList == null) {
            cartItemsList = new ArrayList<>();
        }

        displayCartItems();

        btnClearCart.setOnClickListener(v -> {
            if (cartItemsList.isEmpty()) {
                Toast.makeText(CartActivity.this, "Cart is already empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(CartActivity.this)
                    .setTitle("Clear Cart")
                    .setMessage("Are you sure you want to clear all items from your cart?")
                    .setPositiveButton("Yes, Clear", (dialog, which) -> {
                        cartItemsList.clear();
                        displayCartItems();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(EXTRA_CLEAR_CART, true);
                        setResult(RESULT_OK, returnIntent);
                        Toast.makeText(CartActivity.this, "Cart cleared successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnPlaceOrder.setOnClickListener(v -> {
            if (cartItemsList.isEmpty()) {
                Toast.makeText(CartActivity.this, "Your cart is empty! Add items first.", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder itemsSummary = new StringBuilder();
            itemsSummary.append("Selected Items from Cart:\n");
            for (String item : cartItemsList) {
                itemsSummary.append("• ").append(item).append("\n");
            }
            itemsSummary.append("\nTotal Items: ").append(cartItemsList.size());

            Intent intentNearby = new Intent(CartActivity.this, NearbySellerActivity.class);
            intentNearby.putExtra("SELLER_DETAILS", "Shop: Fresh Food Market\nSeller: Verified Seller\nLocation: Nearby Store\nContact: +94 77 123 4567");
            intentNearby.putExtra("ORDER_ITEMS", itemsSummary.toString());

            Intent returnIntent = new Intent();
            returnIntent.putExtra(EXTRA_CLEAR_CART, true);
            setResult(RESULT_OK, returnIntent);

            startActivity(intentNearby);
            finish();
        });
    }

    private void displayCartItems() {
        int totalItems = cartItemsList.size();
        tvTotalItemsCount.setText("Total Items: " + totalItems);

        if (totalItems == 0) {
            tvEmptyCartMessage.setVisibility(View.VISIBLE);
            btnPlaceOrder.setEnabled(false);
            btnClearCart.setEnabled(false);
            layoutCartItemsList.removeAllViews();
            layoutCartItemsList.addView(tvEmptyCartMessage);
        } else {
            tvEmptyCartMessage.setVisibility(View.GONE);
            btnPlaceOrder.setEnabled(true);
            btnClearCart.setEnabled(true);
            layoutCartItemsList.removeAllViews();

            for (int i = 0; i < cartItemsList.size(); i++) {
                String item = cartItemsList.get(i);
                TextView itemTextView = new TextView(this);
                itemTextView.setText((i + 1) + ". " + item);
                itemTextView.setTextSize(16.0f);
                itemTextView.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
                itemTextView.setPadding(16, 16, 16, 16);

                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 2));
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.card_border));

                layoutCartItemsList.addView(itemTextView);
                if (i < cartItemsList.size() - 1) {
                    layoutCartItemsList.addView(divider);
                }
            }
        }
    }
}
