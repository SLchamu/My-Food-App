package com.example.myfood;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SellerOrdersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_orders);
        AnimatedBackgroundHelper.setup(this);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        Button btnOrdersBack = findViewById(R.id.btnOrdersBack);
        View cardEmptyOrders = findViewById(R.id.cardEmptyOrders);
        View cardLiveOrder = findViewById(R.id.cardLiveOrder);

        TextView tvLiveStatus = findViewById(R.id.tvLiveStatus);
        TextView tvLiveBuyerName = findViewById(R.id.tvLiveBuyerName);
        TextView tvLiveBuyerPhone = findViewById(R.id.tvLiveBuyerPhone);
        TextView tvLiveBuyerAddress = findViewById(R.id.tvLiveBuyerAddress);
        TextView tvLiveOrderItems = findViewById(R.id.tvLiveOrderItems);

        Button btnLiveAccept = findViewById(R.id.btnLiveAccept);
        Button btnLiveComplete = findViewById(R.id.btnLiveComplete);
        Button btnLiveReject = findViewById(R.id.btnLiveReject);

        if (btnOrdersBack != null) {
            btnOrdersBack.setOnClickListener(v -> finish());
        }

        SharedPreferences orderPrefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
        boolean hasLiveOrder = orderPrefs.getBoolean("has_live_order", false);

        if (hasLiveOrder) {
            if (cardEmptyOrders != null) cardEmptyOrders.setVisibility(View.GONE);
            if (cardLiveOrder != null) {
                cardLiveOrder.setVisibility(View.VISIBLE);
                animateEntry(cardLiveOrder, 0);
            }

            String buyerName = orderPrefs.getString("live_buyer_name", "Registered Buyer");
            String buyerPhone = orderPrefs.getString("live_buyer_phone", "0771234567");
            String buyerAddress = orderPrefs.getString("live_buyer_address", "Colombo, Sri Lanka");
            String orderItems = orderPrefs.getString("live_order_items", "No items listed");
            String orderStatus = orderPrefs.getString("live_order_status", "🔔 New Order");

            if (tvLiveBuyerName != null) tvLiveBuyerName.setText("👤 Buyer Name: " + buyerName);
            if (tvLiveBuyerPhone != null) tvLiveBuyerPhone.setText("📞 Phone: " + buyerPhone);
            if (tvLiveBuyerAddress != null) tvLiveBuyerAddress.setText("📍 Address: " + buyerAddress);
            if (tvLiveOrderItems != null) tvLiveOrderItems.setText(orderItems);
            if (tvLiveStatus != null) tvLiveStatus.setText(orderStatus);

            if (btnLiveAccept != null) {
                btnLiveAccept.setOnClickListener(v -> {
                    if (tvLiveStatus != null) {
                        tvLiveStatus.setText("🟡 Accepted");
                        tvLiveStatus.setTextColor(ContextCompat.getColor(this, R.color.primary_orange));
                    }
                    orderPrefs.edit().putString("live_order_status", "🟡 Accepted").apply();
                    notifyBuyer("Your order has been ACCEPTED by the seller and is being prepared!");
                    Toast.makeText(SellerOrdersActivity.this, "Order Accepted!", Toast.LENGTH_SHORT).show();
                });
            }

            if (btnLiveComplete != null) {
                btnLiveComplete.setOnClickListener(v -> {
                    if (tvLiveStatus != null) {
                        tvLiveStatus.setText("✅ Completed");
                        tvLiveStatus.setTextColor(ContextCompat.getColor(this, R.color.seller_green));
                    }
                    orderPrefs.edit().putString("live_order_status", "✅ Completed").apply();
                    notifyBuyer("🎉 Your order is COMPLETE and ready for pickup/delivery!");
                    Toast.makeText(SellerOrdersActivity.this, "🎉 Order Marked Complete!", Toast.LENGTH_LONG).show();
                });
            }

            if (btnLiveReject != null) {
                btnLiveReject.setOnClickListener(v -> {
                    if (tvLiveStatus != null) {
                        tvLiveStatus.setText("❌ Rejected");
                        tvLiveStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                    }
                    orderPrefs.edit().putString("live_order_status", "❌ Rejected").apply();
                    notifyBuyer("❌ Your order was REJECTED by the seller.");
                    Toast.makeText(SellerOrdersActivity.this, "Order Rejected", Toast.LENGTH_SHORT).show();
                });
            }

        } else {
            // No real orders received from another device yet
            if (cardLiveOrder != null) cardLiveOrder.setVisibility(View.GONE);
            if (cardEmptyOrders != null) {
                cardEmptyOrders.setVisibility(View.VISIBLE);
                animateEntry(cardEmptyOrders, 0);
            }
        }
    }

    private void notifyBuyer(String message) {
        SharedPreferences orderPrefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
        orderPrefs.edit().putString("buyer_notification", message).apply();
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
