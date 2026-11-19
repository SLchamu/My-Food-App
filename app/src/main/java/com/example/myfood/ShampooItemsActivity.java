package com.example.myfood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ShampooItemsActivity extends AppCompatActivity {

    private int selectedCount = 0;
    private TextView tvShampooCartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shampoo_items);
        AnimatedBackgroundHelper.setup(this);

        Button btnBack = findViewById(R.id.btnBackShampoo);
        tvShampooCartBadge = findViewById(R.id.tvShampooCartBadge);
        Button btnConfirm = findViewById(R.id.btnConfirmShampoo);
        View btnCart = findViewById(R.id.btnShampooCart);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCart != null) {
            btnCart.setOnClickListener(v ->
                    Toast.makeText(ShampooItemsActivity.this, "Selected Shampoos: " + selectedCount, Toast.LENGTH_SHORT).show()
            );
        }

        int[] addButtons = {
                R.id.btnAddSunsilk,
                R.id.btnAddLifebuoyShampoo,
                R.id.btnAddClear,
                R.id.btnAddDove,
                R.id.btnAddKumarika,
                R.id.btnAddVelvetShampoo,
                R.id.btnAddCheramyShampoo,
                R.id.btnAddEmerald
        };

        for (int id : addButtons) {
            Button btnAdd = findViewById(id);
            if (btnAdd != null) {
                btnAdd.setOnClickListener(v -> {
                    selectedCount++;
                    updateCartBadge();
                    Toast.makeText(ShampooItemsActivity.this, "Shampoo added!", Toast.LENGTH_SHORT).show();
                });
            }
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ADDED_SHAMPOO_COUNT", selectedCount);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }
    }

    private void updateCartBadge() {
        if (tvShampooCartBadge != null) {
            tvShampooCartBadge.setText(String.valueOf(selectedCount));
        }
    }
}
