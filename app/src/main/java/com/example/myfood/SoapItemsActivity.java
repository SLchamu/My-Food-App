package com.example.myfood;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SoapItemsActivity extends AppCompatActivity {

    private int selectedCount = 0;
    private TextView tvSoapCartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soap_items);
        AnimatedBackgroundHelper.setup(this);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        Button btnBackSoap = findViewById(R.id.btnBackSoap);
        tvSoapCartBadge = findViewById(R.id.tvSoapCartBadge);
        Button btnConfirmSoap = findViewById(R.id.btnConfirmSoap);
        View btnSoapCart = findViewById(R.id.btnSoapCart);

        if (btnBackSoap != null) {
            btnBackSoap.setOnClickListener(v -> finish());
        }

        if (btnSoapCart != null) {
            btnSoapCart.setOnClickListener(v ->
                    Toast.makeText(SoapItemsActivity.this, "Selected Soaps: " + selectedCount, Toast.LENGTH_SHORT).show()
            );
        }

        int[] addButtons = {
                R.id.btnAddKohomba,
                R.id.btnAddSunlight,
                R.id.btnAddVelvet,
                R.id.btnAddCheramy,
                R.id.btnAddLux,
                R.id.btnAddLifebuoy
        };

        for (int id : addButtons) {
            Button btnAdd = findViewById(id);
            if (btnAdd != null) {
                btnAdd.setOnClickListener(v -> {
                    selectedCount++;
                    updateCartBadge();
                    Toast.makeText(SoapItemsActivity.this, "Soap added!", Toast.LENGTH_SHORT).show();
                });
            }
        }

        if (btnConfirmSoap != null) {
            btnConfirmSoap.setOnClickListener(v -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ADDED_SOAP_COUNT", selectedCount);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }
    }

    private void updateCartBadge() {
        if (tvSoapCartBadge != null) {
            tvSoapCartBadge.setText(String.valueOf(selectedCount));
        }
    }
}
