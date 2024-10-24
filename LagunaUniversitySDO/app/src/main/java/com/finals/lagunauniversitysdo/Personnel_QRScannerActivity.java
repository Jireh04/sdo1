package com.finals.lagunauniversitysdo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Personnel_QRScannerActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    // Variables to hold personnel details
    private String personnelName;
    private String personnelEmail;
    private Long personnelContact;
    private String personnelProgram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scanner_overlay); // Make sure to set the correct layout

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scanner_frame), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve personnel details from intent
        Intent intent = getIntent();
        personnelName = intent.getStringExtra("PERSONNEL_NAME"); // Ensure the key matches what was put in the intent
        personnelEmail = intent.getStringExtra("PERSONNEL_EMAIL"); // Ensure the key matches
        personnelContact = intent.getLongExtra("PERSONNEL_CONTACT", 0); // Ensure the key matches
        personnelProgram = intent.getStringExtra("PERSONNEL_PROGRAM"); // Ensure the key matches

        // Display welcome message
        Toast.makeText(this, "Welcome " + personnelName, Toast.LENGTH_SHORT).show();

        // Check camera permission and start the scanner
        checkCameraPermissionAndStartScanner();
    }

    private void checkCameraPermissionAndStartScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, start the QR scanner
            startQRCodeScanner();
        }
    }

    private void startQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setCaptureActivity(CustomScannerActivity.class); // Custom activity for square overlay
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
                String scannedData = result.getContents();

                // Start the form activity and pass the scanned data along with personnel details
                Intent formIntent = new Intent(this, PersonnelForm.class);
                formIntent.putExtra("scannedData", scannedData); // Pass scanned data to the form
                formIntent.putExtra("PERSONNEL_NAME_KEY", personnelName);          // Pass personnel name
                formIntent.putExtra("PERSONNEL_EMAIL_KEY", personnelEmail);        // Pass personnel email
                formIntent.putExtra("PERSONNEL_CONTACT_NUM_KEY", personnelContact); // Pass personnel contact
                formIntent.putExtra("PERSONNEL_DEPARTMENT_KEY", personnelProgram);  // Pass personnel program
                startActivity(formIntent); // Start PersonnelForm activity
                finish(); // Close the QRScannerActivity
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
