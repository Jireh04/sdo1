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

public class Prefect_QRScannerActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scanner_overlay); // Make sure to set the correct layout

        // Retrieve prefect details from PrefectSession
        String prefectName = PrefectSession.getPrefectName();
        String prefectEmail = PrefectSession.getPrefectEmail();
        Long prefectContact = PrefectSession.getPrefectContactNum();
        String prefectDepartment = PrefectSession.getPrefectDepartment();
        String prefectId = PrefectSession.getPrefectId();
        String prefectUsername = PrefectSession.getPrefectUsername(); // You need to add a getter for username in PrefectSession
        String prefectPassword = PrefectSession.getPrefectPassword(); // You need to add a getter for password in PrefectSession

        // Display welcome message
        Toast.makeText(this, "Welcome Prefect " + prefectName, Toast.LENGTH_SHORT).show();

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

                // Start the form activity and pass the scanned data along with prefect details
                Intent formIntent = new Intent(this, PrefectForm.class);
                formIntent.putExtra("scannedData", scannedData); // Pass scanned data to the form

                // You can now get prefect details directly from PrefectSession
                formIntent.putExtra("PREFECT_NAME_KEY", PrefectSession.getPrefectName());
                formIntent.putExtra("PREFECT_EMAIL_KEY", PrefectSession.getPrefectEmail());
                formIntent.putExtra("PREFECT_CONTACT_NUM_KEY", PrefectSession.getPrefectContactNum());
                formIntent.putExtra("PREFECT_DEPARTMENT_KEY", PrefectSession.getPrefectDepartment());
                formIntent.putExtra("PREFECT_ID_KEY", PrefectSession.getPrefectId());
                formIntent.putExtra("PREFECT_USERNAME_KEY", PrefectSession.getPrefectUsername());
                formIntent.putExtra("PREFECT_PASSWORD_KEY", PrefectSession.getPrefectPassword());

                startActivity(formIntent); // Start PrefectForm activity
                finish(); // Close the QRScannerActivity
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
