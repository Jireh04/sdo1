package com.finals.lagunauniversitysdo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class QRScannerActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private boolean isMultipleScan = false; // Flag for multiple scan mode
    private ArrayList<String> scannedDataList; // List to store scanned QR codes
    private int remainingScans; // Number of scans left to perform

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scanner_overlay);

        // Enable immersive edge-to-edge experience
        EdgeToEdge.enable(this);
        setupEdgeToEdgePadding();

        // Log and display user data
        displayUserWelcomeMessage();

        // Initialize the scanned data list
        scannedDataList = new ArrayList<>();

        // Show dialog to select scan mode
        showScanModeDialog();
    }

    private void setupEdgeToEdgePadding() {
        View scannerFrame = findViewById(R.id.scanner_frame);
        if (scannerFrame != null) {
            ViewCompat.setOnApplyWindowInsetsListener(scannerFrame, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } else {
            Toast.makeText(this, "Scanner frame view not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayUserWelcomeMessage() {
        String userName = getUserFullName();
        if (userName != null && !userName.isEmpty()) {
            Toast.makeText(this, "Welcome " + userName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Welcome User", Toast.LENGTH_SHORT).show();
        }
    }

    private String getUserFullName() {
        String firstName = UserSession.getFirstName();
        String lastName = UserSession.getLastName();
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    private void showScanModeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Select Scan Mode")
                .setMessage("Do you want to scan a single QR code or multiple codes?")
                .setPositiveButton("Single Scan", (dialog, which) -> {
                    isMultipleScan = false;
                    checkCameraPermissionAndStartScanner();
                })
                .setNegativeButton("Multiple Scans", (dialog, which) -> {
                    isMultipleScan = true;
                    askForNumberOfScans();
                })
                .setCancelable(false)
                .setNeutralButton("Cancel", (dialog, which) -> handleCancellation())
                .show();
    }

    private void askForNumberOfScans() {
        final EditText input = new EditText(this);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});

        new AlertDialog.Builder(this)
                .setTitle("Number of Scans")
                .setMessage("How many QR codes do you want to scan? (Max: 50)")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String inputText = input.getText().toString();
                    if (!inputText.isEmpty()) {
                        remainingScans = Integer.parseInt(inputText);
                        scannedDataList.clear(); // Clear any previous scans
                        checkCameraPermissionAndStartScanner();
                    } else {
                        Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> handleCancellation())
                .show();
    }

    private void checkCameraPermissionAndStartScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startQRCodeScanner();
        }
    }

    private void startQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setCaptureActivity(CustomScannerActivity.class);
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
                startQRCodeScanner();
            } else {
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
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                handleCancellation();
            } else {
                handleScanResult(result.getContents());
            }
        }
    }

    private void handleCancellation() {
        startReferralFormActivity();
        finish();
    }

    private void startReferralFormActivity() {
        Intent referralFormIntent = new Intent(QRScannerActivity.this, refferalForm_student.class);
        passUserData(referralFormIntent);
        referralFormIntent.putStringArrayListExtra("scannedDataList", scannedDataList);
        startActivity(referralFormIntent);
    }

    private void handleScanResult(String scannedText) {
        scannedDataList.add(scannedText);
        remainingScans--;

        if (!isMultipleScan) {
            Intent formIntent = new Intent(this, form.class);
            formIntent.putExtra("scannedData", scannedText);
            passUserData(formIntent);
            startActivity(formIntent);
            finish();
        } else {
            if (remainingScans > 0) {
                Toast.makeText(this, "Scan another QR code", Toast.LENGTH_SHORT).show();
                startQRCodeScanner();
            } else {
                Intent formIntent = new Intent(this, form.class);
                formIntent.putStringArrayListExtra("scannedDataList", scannedDataList);
                passUserData(formIntent);
                startActivity(formIntent);
                finish();
            }
        }
    }

    private void passUserData(Intent intent) {
        intent.putExtra("NAME", getUserFullName());
        intent.putExtra("EMAIL", UserSession.getEmail());
        intent.putExtra("CONTACT", UserSession.getContactNum());
        intent.putExtra("PROGRAM", UserSession.getProgram());
    }
}