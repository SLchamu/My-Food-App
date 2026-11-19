package com.example.myfood;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Buyer extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etWhatsapp, etAddress, etPassword;
    private Button btnSubmitAuth;
    private TextView tvAuthTitle, tvToggleAuth;
    private boolean isSignUpMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer);
        AnimatedBackgroundHelper.setup(this);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        View layoutBuyerHeader = findViewById(R.id.layoutBuyerHeader);
        View cardBuyerForm = findViewById(R.id.cardBuyerForm);
        animateEntry(layoutBuyerHeader, 0);
        animateEntry(cardBuyerForm, 150);

        tvAuthTitle = findViewById(R.id.tvAuthTitle);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etWhatsapp = findViewById(R.id.etWhatsapp);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        btnSubmitAuth = findViewById(R.id.btnSubmitAuth);
        tvToggleAuth = findViewById(R.id.tvToggleAuth);

        if (tvToggleAuth != null) {
            tvToggleAuth.setOnClickListener(v -> toggleAuthMode());
        }

        if (btnSubmitAuth != null) {
            btnSubmitAuth.setOnClickListener(v -> performAuthSubmit());
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

    private void toggleAuthMode() {
        isSignUpMode = !isSignUpMode;
        if (isSignUpMode) {
            if (tvAuthTitle != null) tvAuthTitle.setText("Buyer Sign Up");
            if (btnSubmitAuth != null) btnSubmitAuth.setText("Sign Up");
            if (tvToggleAuth != null) tvToggleAuth.setText("Already have an account? Login Here");

            if (etName != null) etName.setVisibility(View.VISIBLE);
            if (etPhone != null) etPhone.setVisibility(View.VISIBLE);
            if (etWhatsapp != null) etWhatsapp.setVisibility(View.VISIBLE);
            if (etAddress != null) etAddress.setVisibility(View.VISIBLE);
        } else {
            if (tvAuthTitle != null) tvAuthTitle.setText("Buyer Login");
            if (btnSubmitAuth != null) btnSubmitAuth.setText("Login");
            if (tvToggleAuth != null) tvToggleAuth.setText("Don't have an account? Sign Up Here");

            if (etName != null) etName.setVisibility(View.GONE);
            if (etPhone != null) etPhone.setVisibility(View.GONE);
            if (etWhatsapp != null) etWhatsapp.setVisibility(View.GONE);
            if (etAddress != null) etAddress.setVisibility(View.GONE);
        }
    }

    private void performAuthSubmit() {
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword != null ? etPassword.getText().toString().trim() : "";

        SharedPreferences buyerPrefs = getSharedPreferences("BuyerPrefs", MODE_PRIVATE);

        if (isSignUpMode) {
            String name = etName != null ? etName.getText().toString().trim() : "";
            String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
            String whatsapp = etWhatsapp != null ? etWhatsapp.getText().toString().trim() : "";
            String address = etAddress != null ? etAddress.getText().toString().trim() : "";

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || whatsapp.isEmpty() || address.isEmpty() || password.isEmpty()) {
                Toast.makeText(Buyer.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = buyerPrefs.edit();
            editor.putBoolean("is_buyer_logged_in", true);
            editor.putString("buyer_name", name);
            editor.putString("buyer_email", email);
            editor.putString("buyer_phone", phone);
            editor.putString("buyer_whatsapp", whatsapp);
            editor.putString("buyer_address", address);
            editor.putString("buyer_password", password);
            editor.apply();

            // Also keep SellerPrefs in sync so seller profile can use details if needed
            SharedPreferences sellerPrefs = getSharedPreferences("SellerPrefs", MODE_PRIVATE);
            sellerPrefs.edit()
                    .putBoolean("is_registered", true)
                    .putString("seller_name", name)
                    .putString("shop_name", email)
                    .putString("shop_address", address)
                    .putString("shop_location", "WhatsApp: " + whatsapp)
                    .apply();

            Toast.makeText(Buyer.this, "Sign Up Successful! Welcome " + name, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Buyer.this, FoodMenuActivity.class);
            startActivity(intent);
            finish();

        } else {
            // Login Mode
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Buyer.this, "Please enter email and password!", Toast.LENGTH_SHORT).show();
                return;
            }

            String savedPassword = buyerPrefs.getString("buyer_password", null);
            String savedEmail = buyerPrefs.getString("buyer_email", null);

            if (savedEmail != null && savedPassword != null) {
                if (email.equalsIgnoreCase(savedEmail) && password.equals(savedPassword)) {
                    buyerPrefs.edit().putBoolean("is_buyer_logged_in", true).apply();
                    Toast.makeText(Buyer.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Buyer.this, FoodMenuActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Buyer.this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // If first time logging in without sign up history, save and allow
                buyerPrefs.edit()
                        .putBoolean("is_buyer_logged_in", true)
                        .putString("buyer_email", email)
                        .putString("buyer_password", password)
                        .apply();

                Toast.makeText(Buyer.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Buyer.this, FoodMenuActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
