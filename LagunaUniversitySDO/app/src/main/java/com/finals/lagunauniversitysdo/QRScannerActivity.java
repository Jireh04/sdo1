package com.finals.lagunauniversitysdo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRScannerActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    // Variables to hold user details
    private String userName;
    private String userEmail;
    private Long userContact;
    private String userProgram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve user details from intent
        Intent intent = getIntent();
        userName = intent.getStringExtra("NAME");
        userEmail = intent.getStringExtra("EMAIL");
        userContact = intent.getLongExtra("CONTACT", 0);
        userProgram = intent.getStringExtra("PROGRAM");

        // Display welcome message
        Toast.makeText(this, "Welcome " + userName, Toast.LENGTH_SHORT).show();

        checkCameraPermissionAndStartScanner();  // Start scanner directly when activity launches
    }

    private void checkCameraPermissionAndStartScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request Camera permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, start QR scanner
            startQRCodeScanner();
        }
    }

    private void startQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setCaptureActivity(CustomScannerActivity.class);  // Custom activity for square overlay
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission was granted
                startQRCodeScanner();
            } else {
                // Camera permission was denied
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // Handle cancellation
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                finish(); // Close the QRScannerActivity and go back to the referral screen
            } else {
                // Handle the scanned result
                String scannedText = result.getContents();

                // Start the form activity and pass the scanned data
                Intent formIntent = new Intent(this,form.class);
                formIntent.putExtra("scannedData", scannedText); // Pass scanned data to the form
                startActivity(formIntent); // Start FormActivity
                finish(); // Close the QRScannerActivity
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
