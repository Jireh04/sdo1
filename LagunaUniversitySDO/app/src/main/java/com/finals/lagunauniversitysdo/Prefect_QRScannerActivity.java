package com.finals.lagunauniversitysdo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.text.InputFilter;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class Prefect_QRScannerActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private boolean isMultipleScan = false; // Flag for multiple scan mode
    private ArrayList<String> scannedDataList; // List to store scanned QR codes
    private int remainingScans; // Number of scans left to perform

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

        // Retrieve prefect details from PrefectSession
        String prefectName = PrefectSession.getPrefectName();
        String prefectEmail = PrefectSession.getPrefectEmail();
        Long prefectContact = PrefectSession.getPrefectContactNum();
        String prefectDepartment = PrefectSession.getPrefectDepartment();

        // Display welcome message
        Toast.makeText(this, "Welcome Prefect " + prefectName, Toast.LENGTH_SHORT).show();

        scannedDataList = new ArrayList<>(); // Initialize the list for storing scanned QR codes
        showScanModeDialog(); // Show dialog for selecting scan mode
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // Handle when the user cancels the scan
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show();
                // End the activity or navigate back as intended
                finish();
            } else {
                // Handle a successful scan result
                handleScanResult(result.getContents());
            }
        } else {
            // Ensure the parent method doesn't override cancellation handling
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleScanResult(String scannedText) {
        scannedDataList.add(scannedText); // Add scanned text to the list
        remainingScans--; // Decrease the count of remaining scans

        if (!isMultipleScan) {
            // If in single scan mode
            startPrefectFormActivity(scannedText); // Start the personnel form with the scanned data
        } else {
            // If in multiple scan mode
            if (remainingScans > 0) {
                // Show dialog after each scan
                showContinueOrGoToFormDialog(scannedText);
            } else {
                // When all scans are done, send data to the form activity
                startPrefectFormActivity(scannedDataList); // Pass all scanned data to the form activity
            }
        }
    }

    private void showContinueOrGoToFormDialog(String scannedText) {
        new AlertDialog.Builder(this)
                .setTitle("Scan Successful")
                .setMessage("Scanned Student: " + scannedText + "\nDo you want to continue scanning, delete this scan, or go to the form?")
                .setPositiveButton("Continue Scanning", (dialog, which) -> {
                    startQRCodeScanner(); // Start scanning again
                })
                .setNegativeButton("Go to Form", (dialog, which) -> {
                    startPrefectFormActivity(scannedDataList); // Pass all scanned data to the form activity
                })
                .setNeutralButton("Delete Scan", (dialog, which) -> {
                    // Remove the last scanned entry from the list if it's wrong
                    if (!scannedDataList.isEmpty()) {
                        scannedDataList.remove(scannedDataList.size() - 1); // Remove the last scan
                        remainingScans++; // Increase the remaining scans as the user deleted a scan
                    }
                    Toast.makeText(this, "Last scan deleted. Continue scanning.", Toast.LENGTH_SHORT).show();
                    startQRCodeScanner(); // Continue scanning
                })
                .setCancelable(false)
                .show();
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
                    askForNumberOfScans(); // Ask user how many scans they want to perform
                })
                .setCancelable(false) // Prevent accidental dismissal
                .show();
    }

    private void askForNumberOfScans() {
        final EditText input = new EditText(this);
        input.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    String inputText = dest.toString() + source.toString();
                    if (!inputText.matches("\\d*")) return ""; // Reject non-numeric input
                    if (inputText.length() > 2 || (inputText.length() == 2 && Integer.parseInt(inputText) > 50)) {
                        return ""; // Reject input exceeding 50
                    }
                    return null; // Accept input
                }
        });


        new AlertDialog.Builder(this)
                .setTitle("Number of Scans")
                .setMessage("How many QR codes do you want to scan? (Max: 50)")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String inputText = input.getText().toString();
                    try {
                        int numberOfScans = Integer.parseInt(inputText);
                        if (numberOfScans > 0) {
                            remainingScans = numberOfScans; // Set remaining scans
                            scannedDataList.clear(); // Clear previous scans
                            checkCameraPermissionAndStartScanner(); // Start scanning
                        } else {
                            Toast.makeText(Prefect_QRScannerActivity.this, "Please enter a valid number.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(Prefect_QRScannerActivity.this, "Invalid input. Please enter a number.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // End the activity if "Cancel" is pressed
                    finish();
                })
                .show();
    }

    private void checkCameraPermissionAndStartScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
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

    private void startPrefectFormActivity(String scannedData) {
        Intent formIntent = new Intent(this, PrefectForm.class);
        formIntent.putExtra("scannedData", scannedData);
        putPrefectData(formIntent);
        startActivity(formIntent);
        finish(); // Ensure the current activity finishes
    }

    private void startPrefectFormActivity(ArrayList<String> scannedDataList) {
        Intent formIntent = new Intent(this, PrefectForm.class);
        formIntent.putStringArrayListExtra("SCANNED_DATA_LIST_KEY", scannedDataList);
        putPrefectData(formIntent);
        startActivity(formIntent);
        finish(); // Ensure the current activity finishes
    }


    private void putPrefectData(Intent formIntent) {
        formIntent.putExtra("PREFECT_NAME_KEY", PrefectSession.getPrefectName());
        formIntent.putExtra("PREFECT_EMAIL_KEY", PrefectSession.getPrefectEmail());
        formIntent.putExtra("PREFECT_CONTACT_NUM_KEY", PrefectSession.getPrefectContactNum());
        formIntent.putExtra("PREFECT_DEPARTMENT_KEY", PrefectSession.getPrefectDepartment());
        formIntent.putExtra("PREFECT_ID_KEY", PrefectSession.getPrefectId());
        formIntent.putExtra("PREFECT_USERNAME_KEY", PrefectSession.getPrefectUsername());
        formIntent.putExtra("PREFECT_PASSWORD_KEY", PrefectSession.getPrefectPassword());
    }
}
