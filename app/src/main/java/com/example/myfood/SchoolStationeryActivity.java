package com.example.myfood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SchoolStationeryActivity extends AppCompatActivity {

    private int selectedCount = 0;
    private TextView tvStationeryCartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_stationery);
        AnimatedBackgroundHelper.setup(this);

        Button btnBack = findViewById(R.id.btnBackStationery);
        tvStationeryCartBadge = findViewById(R.id.tvStationeryCartBadge);
        Button btnConfirm = findViewById(R.id.btnConfirmStationery);
        View btnCart = findViewById(R.id.btnStationeryCart);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCart != null) {
            btnCart.setOnClickListener(v ->
                    Toast.makeText(SchoolStationeryActivity.this, "Selected School Items: " + selectedCount, Toast.LENGTH_SHORT).show()
            );
        }

        int[] addButtons = {
                R.id.btnAddCrBook,
                R.id.btnAddExerciseBook,
                R.id.btnAddBluePen,
                R.id.btnAddRedPen,
                R.id.btnAddBlackPen,
                R.id.btnAddColorPenBox,
                R.id.btnAddFootRuler,
                R.id.btnAddHalfRuler,
                R.id.btnAddPencil,
                R.id.btnAddGeometryBox,
                R.id.btnAddColors,
                R.id.btnAddSchoolBag,
                R.id.btnAddLunchBox
        };

        for (int id : addButtons) {
            Button btnAdd = findViewById(id);
            if (btnAdd != null) {
                btnAdd.setOnClickListener(v -> {
                    selectedCount++;
                    updateCartBadge();
                    Toast.makeText(SchoolStationeryActivity.this, "Item added!", Toast.LENGTH_SHORT).show();
                });
            }
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ADDED_STATIONERY_COUNT", selectedCount);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }
    }

    private void updateCartBadge() {
        if (tvStationeryCartBadge != null) {
            tvStationeryCartBadge.setText(String.valueOf(selectedCount));
        }
    }
}
