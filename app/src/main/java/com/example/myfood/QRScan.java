package com.example.myfood;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRScan extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etPhone;
    Button btnSubmitAuth;

    String name, email, password, phone;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(QRScan.this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    String qrData = result.getContents();
                    Toast.makeText(QRScan.this, "Scanned: " + qrData, Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        btnSubmitAuth = findViewById(R.id.btnSubmitAuth);

        if (btnSubmitAuth != null) {
            btnSubmitAuth.setOnClickListener(v -> {
                name = etName != null ? etName.getText().toString().trim() : "";
                email = etEmail != null ? etEmail.getText().toString().trim() : "";
                password = etPassword != null ? etPassword.getText().toString().trim() : "";
                phone = etPhone != null ? etPhone.getText().toString().trim() : "";

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(QRScan.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                } else {
                    ScanOptions options = new ScanOptions();
                    options.setPrompt("Scan a QR Code");
                    options.setOrientationLocked(true);
                    barcodeLauncher.launch(options);
                }
            });
        }
    }
}
