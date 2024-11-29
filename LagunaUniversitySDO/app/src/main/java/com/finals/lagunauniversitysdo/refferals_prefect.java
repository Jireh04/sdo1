package com.finals.lagunauniversitysdo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.pdf.PdfDocument;
import android.content.Intent;
import android.net.Uri;

import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import android.media.MediaScannerConnection;
import android.net.Uri;
import java.util.HashMap;
import android.widget.EditText;
import android.view.View;

import android.widget.Toast;
import android.widget.ImageView;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.app.AlertDialog;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import androidx.fragment.app.Fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;


import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class refferals_prefect extends Fragment {

    private FirebaseFirestore db;

    public refferals_prefect() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_refferals_prefect, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the TabLayout
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        // Set custom background for each tab
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                TextView customTab = new TextView(requireContext());
                customTab.setText(tab.getText());
                customTab.setGravity(Gravity.CENTER);
                customTab.setBackgroundResource(R.drawable.tab_item_background);
                tab.setCustomView(customTab);
            }
        }

        // Clear any default tab selection
        tabLayout.clearOnTabSelectedListeners();

        // Set tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // Pending
                        onPendingClick();
                        tab.getCustomView().setSelected(true);
                        break;
                    case 1: // Accepted
                        onAcceptedClick();
                        tab.getCustomView().setSelected(true);
                        break;
                    case 2: // Rejected
                        onRejectedClick();
                        tab.getCustomView().setSelected(true);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getCustomView() != null) {
                    tab.getCustomView().setSelected(false);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Automatically call onPendingClick() to display the "Pending" section when the fragment is created
        onPendingClick();

        // Optionally, select the "Pending" tab by default
        tabLayout.getTabAt(0).select();
    }




    // Fetch pending referrals from Firestore
    public void onPendingClick() {
        LinearLayout linearLayoutPending = getView().findViewById(R.id.linear_layout_pending);
        ScrollView scrollViewPending = getView().findViewById(R.id.scroll_view_pending);

        // Clear other sections
        LinearLayout linearLayoutAccepted = getView().findViewById(R.id.linear_layout_accepted);
        ScrollView scrollViewAccepted = getView().findViewById(R.id.scroll_view_accepted);
        LinearLayout linearLayoutRejected = getView().findViewById(R.id.linear_layout_rejected);
        ScrollView scrollViewRejected = getView().findViewById(R.id.scroll_view_rejected);

        linearLayoutAccepted.removeAllViews();
        scrollViewAccepted.setVisibility(View.GONE);
        linearLayoutRejected.removeAllViews();
        scrollViewRejected.setVisibility(View.GONE);

        // Show the ScrollView for pending
        scrollViewPending.setVisibility(View.VISIBLE);
        linearLayoutPending.removeAllViews();

        // Query Firestore collections for "pending" status
        db.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot studentDocument : task.getResult().getDocuments()) {
                    db.collection("students").document(studentDocument.getId())
                            .collection("student_refferal_history")
                            .orderBy("date", Query.Direction.DESCENDING)
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        if ("pending".equals(referralDocument.getString("status"))) {
                                            Map<String, Object> referralData = referralDocument.getData();
                                            if (referralData != null) {
                                                String studentReferrer = (String) referralData.get("student_referrer");
                                                String referralDate = (String) referralData.get("date");
                                                String referrerId = (String) referralData.get("referrer_id");
                                                String studentName = (String) referralData.get("student_name");
                                                String studentId = (String) referralData.get("student_id");

                                                // Create CardView
                                                CardView cardView = new CardView(getActivity());
                                                cardView.setLayoutParams(new LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                                ));
                                                cardView.setRadius(8f);
                                                cardView.setCardElevation(6f);
                                                cardView.setUseCompatPadding(true);
                                                cardView.setPadding(16, 16, 16, 16);
                                                cardView.setContentPadding(16, 16, 16, 16);
                                                cardView.setClickable(true);
                                                cardView.setFocusable(true);

                                                // Create LinearLayout inside CardView
                                                LinearLayout cardLayout = new LinearLayout(getActivity());
                                                cardLayout.setOrientation(LinearLayout.VERTICAL);
                                                cardLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                                ));

                                                // Add TextViews for referral details
                                                TextView referrerTextView = new TextView(getActivity());
                                                referrerTextView.setText("Referrer: " + studentReferrer);
                                                referrerTextView.setTextSize(16);
                                                referrerTextView.setPadding(8, 8, 8, 4);
                                                cardLayout.addView(referrerTextView);

                                                TextView dateTextView = new TextView(getActivity());
                                                dateTextView.setText("Date: " + referralDate);
                                                dateTextView.setTextSize(16);
                                                dateTextView.setPadding(8, 4, 8, 4);
                                                cardLayout.addView(dateTextView);

                                                // Create Spinner for actions
                                                Spinner actionsSpinner = new Spinner(getActivity());
                                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                                                        android.R.layout.simple_spinner_item,
                                                        new String[]{"Select Action", "View", "Accept", "Reject"});
                                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                actionsSpinner.setAdapter(adapter);

                                                // Set up listener for action selection
                                                actionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        String selectedAction = (String) parent.getItemAtPosition(position);
                                                        switch (selectedAction) {
                                                            case "View":
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                                builder.setTitle("Choose an option")
                                                                        .setMessage("Do you want to download or open the PDF?")
                                                                        .setPositiveButton("Download", (dialog, which) -> {
                                                                            downloadPdf(referralData, studentReferrer, referralDate, studentId, studentName);
                                                                        })
                                                                        .setNegativeButton("Open", (dialog, which) -> {
                                                                            generatePdf(referralData, studentReferrer, referralDate, studentId, studentName);
                                                                        })
                                                                        .setCancelable(true)
                                                                        .show();
                                                                break;
                                                            case "Accept":
                                                                new AlertDialog.Builder(getActivity())
                                                                        .setTitle("Confirmation")
                                                                        .setMessage("Are you sure you want to accept this referral?")
                                                                        .setPositiveButton("Yes", (dialog, which) -> {
                                                                            updateReferralStatusStudent(referrerId, referralDocument.getId(), "accepted");
                                                                            saveAcceptedReferral(referralData, studentId, referralDocument.getId());

                                                                        })
                                                                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                                                        .show();
                                                                break;
                                                            case "Reject":
                                                                View rejectView = getLayoutInflater().inflate(R.layout.reject_reason, null);
                                                                EditText reasonEditText = rejectView.findViewById(R.id.editTextReason);

                                                                new AlertDialog.Builder(getActivity())
                                                                        .setTitle("Rejection Reason")
                                                                        .setMessage("Please provide a reason for rejecting this referral:")
                                                                        .setView(rejectView)
                                                                        .setPositiveButton("Reject", (dialog, which) -> {
                                                                            String rejectionReason = reasonEditText.getText().toString().trim();
                                                                            if (!rejectionReason.isEmpty()) {
                                                                                updateReferralStatusStudentWithReason(referrerId, referralDocument.getId(), "rejected", rejectionReason);
                                                                            } else {
                                                                                Toast.makeText(getActivity(), "Please enter a rejection reason.", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        })
                                                                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                                                        .show();
                                                                break;
                                                        }
                                                        actionsSpinner.setSelection(0);
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {
                                                        // No action needed
                                                    }
                                                });

                                                // Add Spinner to CardLayout
                                                cardLayout.addView(actionsSpinner);

                                                // Add the LinearLayout to CardView
                                                cardView.addView(cardLayout);

                                                // Add the CardView to the pending layout
                                                linearLayoutPending.addView(cardView);
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        });


        // Repeat the same logic for personnel_refferal_history collection
        db.collection("personnel").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot personnelDocument : task.getResult().getDocuments()) {
                    // Get all referral history documents
                    db.collection("personnel").document(personnelDocument.getId())
                            .collection("personnel_refferal_history")
                            .orderBy("date", Query.Direction.DESCENDING) // Sort by date in descending order
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        // Check status in the document
                                        if ("pending".equals(referralDocument.getString("status"))) {
                                            // Get the data from Firestore (as a Map)
                                            Map<String, Object> referralData = referralDocument.getData();
                                            if (referralData != null) {
                                                // Retrieve referrer_id and store it in referralData
                                                String referrerId = referralDocument.getString("referrer_id");
                                                referralData.put("referrer_id", referrerId);

                                                String personnelReferrer = (String) referralData.get("personnel_referrer");
                                                String personnelID = (String) referralData.get("personnel_id");
                                                String studentName = (String) referralData.get("student_name");
                                                String studentId = (String) referralData.get("student_id");
                                                String studentProgram = (String) referralData.get("student_program");
                                                String referralDate = (String) referralData.get("date");

                                                // Create a TableRow for each referral
                                                TableRow tableRow = new TableRow(getActivity());
                                                tableRow.setLayoutParams(new TableRow.LayoutParams(
                                                        TableRow.LayoutParams.MATCH_PARENT,
                                                        TableRow.LayoutParams.WRAP_CONTENT
                                                ));

                                                // Create a TextView for the referrer
                                                TextView referrerTextView = new TextView(getActivity());
                                                referrerTextView.setText(personnelReferrer);
                                                referrerTextView.setPadding(16, 8, 16, 8);
                                                referrerTextView.setTextSize(14);
                                                tableRow.addView(referrerTextView);

                                                // Create a TextView for the referral date
                                                TextView dateTextView = new TextView(getActivity());
                                                dateTextView.setText(referralDate);
                                                dateTextView.setPadding(16, 8, 16, 8);
                                                dateTextView.setTextSize(14);
                                                tableRow.addView(dateTextView);

                                                // Create a Spinner for action selection
                                                Spinner actionSpinner = new Spinner(getActivity());
                                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                                                        android.R.layout.simple_spinner_item,
                                                        new String[]{"Select Action", "View", "Accept", "Reject"});
                                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                actionSpinner.setAdapter(adapter);

                                                // Set up listener for the selected action
                                                actionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        String selectedAction = (String) parent.getItemAtPosition(position);

                                                        switch (selectedAction) {
                                                            case "View":
                                                                // Show dialog to choose between Download and Open
                                                                new AlertDialog.Builder(getActivity())
                                                                        .setTitle("Choose an option")
                                                                        .setMessage("Do you want to download or open the PDF?")
                                                                        .setPositiveButton("Download", (dialog, which) -> {
                                                                            downloadPdf(referralData, studentName, studentId, studentProgram, referralDate);
                                                                        })
                                                                        .setNegativeButton("Open", (dialog, which) -> {
                                                                            generatePdf(referralData, studentName, studentId, studentProgram, referralDate);
                                                                        })
                                                                        .setCancelable(true)
                                                                        .show();
                                                                break;

                                                            case "Accept":
                                                                // Confirmation dialog to accept the referral
                                                                new AlertDialog.Builder(getActivity())
                                                                        .setTitle("Confirmation")
                                                                        .setMessage("Are you sure you want to accept this referral?")
                                                                        .setPositiveButton("Yes", (dialog, which) -> {
                                                                            // Update status in personnel and save accepted data in students collection
                                                                            updateReferralStatusPersonnel(referrerId, referralDocument.getId(), "accepted");

                                                                            // Prepare a Map to save the data in the 'accepted_status' sub-collection
                                                                            Map<String, Object> acceptedData = new HashMap<>();
                                                                            acceptedData.put("date", referralData.get("date"));
                                                                            acceptedData.put("referrer_id", referralData.get("referrer_id"));
                                                                            acceptedData.put("remarks", referralData.get("remarks"));
                                                                            acceptedData.put("status", "accepted");
                                                                            acceptedData.put("student_id", referralData.get("student_id"));
                                                                            acceptedData.put("student_name", referralData.get("student_name"));
                                                                            acceptedData.put("student_program", referralData.get("student_program"));
                                                                            acceptedData.put("personnel_referrer", referralData.get("personnel_referrer"));
                                                                            acceptedData.put("term", referralData.get("term"));
                                                                            acceptedData.put("user_concern", referralData.get("user_concern"));
                                                                            acceptedData.put("violation", referralData.get("violation"));
                                                                            acceptedData.put("offense", referralData.get("offense"));
                                                                            acceptedData.put("violation_status", "Unsettled");

                                                                            // Save the accepted data in personnel's accepted_status sub-collection
                                                                            db.collection("personnel")
                                                                                    .document(personnelID)
                                                                                    .collection("accepted_status")
                                                                                    .document(referralDocument.getId())
                                                                                    .set(acceptedData)
                                                                                    .addOnSuccessListener(aVoid -> {
                                                                                        Toast.makeText(getActivity(), "Referral accepted and saved.", Toast.LENGTH_SHORT).show();
                                                                                    })
                                                                                    .addOnFailureListener(e -> {
                                                                                        Toast.makeText(getActivity(), "Failed to save accepted referral.", Toast.LENGTH_SHORT).show();
                                                                                    });
                                                                        })
                                                                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                                                        .show();
                                                                break;

                                                            case "Reject":
                                                                // Create a custom layout for rejection reason
                                                                View rejectView = getLayoutInflater().inflate(R.layout.reject_reason, null);
                                                                EditText reasonEditText = rejectView.findViewById(R.id.editTextReason);

                                                                new AlertDialog.Builder(getActivity())
                                                                        .setTitle("Rejection Reason")
                                                                        .setMessage("Please provide a reason for rejecting this referral:")
                                                                        .setView(rejectView)
                                                                        .setPositiveButton("Reject", (dialog, which) -> {
                                                                            String rejectionReason = reasonEditText.getText().toString().trim();
                                                                            if (!rejectionReason.isEmpty()) {
                                                                                updateReferralStatusPersonnel(referrerId, referralDocument.getId(), "rejected", rejectionReason);
                                                                                Toast.makeText(getActivity(), "Referral has been rejected.", Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                Toast.makeText(getActivity(), "Please enter a rejection reason.", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        })
                                                                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                                                        .show();
                                                                break;
                                                        }

                                                        actionSpinner.setSelection(0); // Reset selection to "Select Action"
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {
                                                        // Do nothing
                                                    }
                                                });

                                                // Add the spinner to the table row
                                                tableRow.addView(actionSpinner);
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        });


    }

    private void updateReferralStatusPersonnel(String referrerId, String referralDocId, String status, String rejectionReason) {
        Map<String, Object> updatedReferralData = new HashMap<>();
        updatedReferralData.put("status", status); // Update status to rejected
        updatedReferralData.put("reason_rejecting", rejectionReason); // Store rejection reason

        // Update the referral status in personnel collection
        db.collection("personnel")
                .document(referrerId)
                .collection("personnel_refferal_history")
                .document(referralDocId)
                .update(updatedReferralData)
                .addOnSuccessListener(aVoid -> {
                    // Optionally, show a success message or handle UI refresh
                    Toast.makeText(getActivity(), "Referral status updated to rejected.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Show failure message if updating failed
                    Toast.makeText(getActivity(), "Failed to reject referral.", Toast.LENGTH_SHORT).show();
                });
    }


    private void updateReferralStatusStudentWithReason(String referrerId, String referralDocId, String status, String rejectionReason) {
        Map<String, Object> updatedReferralData = new HashMap<>();
        updatedReferralData.put("status", status); // Update status to rejected
        updatedReferralData.put("reason_rejecting", rejectionReason); // Store rejection reason

        // Update the referral status in the student's collection
        db.collection("students")
                .document(referrerId)
                .collection("student_refferal_history")
                .document(referralDocId)
                .update(updatedReferralData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Referral has been rejected.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to reject referral.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveAcceptedReferral(Map<String, Object> referralData, String studentId, String referralId) {
        // Add necessary fields to the referral data
        referralData.put("status", "accepted"); // Update the status to "accepted"
        referralData.put("violation_status", "Unsettled"); // Additional field for violation status

        // Navigate to the accepted_status subcollection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("students").document(studentId)
                .collection("accepted_status")
                .document(referralId) // Use the referral ID as the document ID
                .set(referralData) // Save the referral data to Firestore
                .addOnSuccessListener(aVoid -> {
                    // Notify user of success
                    Toast.makeText(getActivity(), "Referral accepted and saved.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Notify user of failure
                    Toast.makeText(getActivity(), "Failed to save accepted referral: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    // Define a request code for permission requests
    private static final int REQUEST_PERMISSION = 1;

    public void generatePdf(Map<String, Object> referralData, String studentName, String studentId, String studentProgram, String referralDate) {
        // Check if data is valid
        if (studentName == null || studentId == null || referralDate == null) {
            Toast.makeText(getActivity(), "Invalid data for PDF generation", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new PdfDocument
        PdfDocument document = new PdfDocument();

        // Create a page description (A4 size - 595x842)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        // Get the Canvas object for drawing content
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);

        // Start drawing text
        int yPosition = 100;  // Start position for drawing text

        canvas.drawText("Referral Details", 220, yPosition, paint);
        yPosition += 20; // Move to the next line

        canvas.drawText("Student Name: " + studentName, 50, yPosition, paint);
        yPosition += 20;

        canvas.drawText("Student ID: " + studentId, 50, yPosition, paint);
        yPosition += 20;

        canvas.drawText("Program: " + studentProgram, 50, yPosition, paint);
        yPosition += 20;

        canvas.drawText("Referral Date: " + referralDate, 50, yPosition, paint);
        yPosition += 20;

        // Add referrer details (Ensure this is not null)
        String referrerName = (String) referralData.get("student_referrer");
        if (referrerName != null) {
            canvas.drawText("Referrer: " + referrerName, 50, yPosition, paint);
        }

        // Finish the page
        document.finishPage(page);

        // Define the directory and file for saving the PDF in internal storage
        File directory = getActivity().getFilesDir();  // Save in app's private storage
        File file = new File(directory, studentName + "_referral.pdf");

        try {
            // Ensure the directory exists
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Write the PDF document to the file
            FileOutputStream outputStream = new FileOutputStream(file);
            document.writeTo(outputStream);
            outputStream.close();  // Ensure the stream is properly closed
            Toast.makeText(getActivity(), "PDF generated", Toast.LENGTH_SHORT).show();

            // Open the PDF using Intent
            openPdf(file);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Close the document
            document.close();
        }
    }
    public void downloadPdf(Map<String, Object> referralData, String studentName, String studentId, String studentProgram, String referralDate) {
        // Check if data is valid
        if (studentName == null || studentId == null || referralDate == null) {
            Toast.makeText(getActivity(), "Invalid data for PDF generation", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new PdfDocument
        PdfDocument document = new PdfDocument();

        // Create a page description (A4 size - 595x842)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        // Get the Canvas object for drawing content
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);

        // Start drawing text
        int yPosition = 100;  // Start position for drawing text

        canvas.drawText("Referral Details", 220, yPosition, paint);
        yPosition += 20; // Move to the next line

        canvas.drawText("Student Name: " + studentName, 50, yPosition, paint);
        yPosition += 20;

        canvas.drawText("Student ID: " + studentId, 50, yPosition, paint);
        yPosition += 20;

        canvas.drawText("Program: " + studentProgram, 50, yPosition, paint);
        yPosition += 20;

        canvas.drawText("Referral Date: " + referralDate, 50, yPosition, paint);
        yPosition += 20;

        // Add referrer details (Ensure this is not null)
        String referrerName = (String) referralData.get("student_referrer");
        if (referrerName != null) {
            canvas.drawText("Referrer: " + referrerName, 50, yPosition, paint);
        }

        // Finish the page
        document.finishPage(page);

        // Define the directory and file for saving the PDF in the Downloads folder
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Save to Downloads folder
        File file = new File(downloadsDirectory, studentName + "_referral.pdf");

        try {
            // Ensure the directory exists
            if (!downloadsDirectory.exists()) {
                downloadsDirectory.mkdirs();
            }

            // Write the PDF document to the file
            FileOutputStream outputStream = new FileOutputStream(file);
            document.writeTo(outputStream);
            outputStream.close();  // Ensure the stream is properly closed
            Toast.makeText(getActivity(), "PDF downloaded to Downloads folder", Toast.LENGTH_SHORT).show();

            // Optionally, you can notify the system to refresh Downloads folder or open the file
            // Notify the system to scan the file so it shows up in file managers
            MediaScannerConnection.scanFile(getActivity(), new String[]{file.getAbsolutePath()}, null, null);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Close the document
            document.close();
        }
    }



    public void openPdf(File file) {
        // Create an Intent to open the PDF file
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Get the URI for the file using FileProvider
        Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".fileprovider", file);

        // Set flags to allow read access to the file
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Set MIME type to ensure it's recognized as a PDF file
        intent.setDataAndType(uri, "application/pdf");

        // Launch the intent to open the PDF
        startActivity(intent);
    }


    // Method to update referral status in Firestore
    // Make sure db is initialized at the top of the fragment or activity

    private void updateReferralStatusPersonnel(String referrerId, String referralDocumentId, String newStatus) {
        if (referrerId == null || referralDocumentId == null) {
            Log.e("UpdateError", "Referrer ID or Referral Document ID is null. Cannot update status.");
            return; // Exit if IDs are invalid
        }

        Log.d("UpdateStatus", "Updating personnel status. Referrer ID: " + referrerId + ", Referral ID: " + referralDocumentId + ", New Status: " + newStatus);

        // Update the referral status using the referrer_id
        db.collection("personnel")
                .document(referrerId) // Assuming you want to use referrerId to locate the document
                .collection("personnel_refferal_history").document(referralDocumentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreSuccess", "Personnel referral status updated successfully");
                    refreshReferralData(); // Call to refresh data after status update
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error updating personnel referral status", e);
                });
    }

    private void updateReferralStatusStudent(String referrerId, String referralDocumentId, String newStatus) {
        if (referrerId == null || referralDocumentId == null) {
            Log.e("UpdateError", "Referrer ID or Referral Document ID is null. Cannot update status.");
            return; // Exit if IDs are invalid
        }

        Log.d("UpdateStatus", "Updating student status. Referrer ID: " + referrerId + ", Referral ID: " + referralDocumentId + ", New Status: " + newStatus);

        // Update the referral status using the referrer_id
        db.collection("students")
                .document(referrerId) // Assuming you want to use referrerId to locate the document
                .collection("student_refferal_history").document(referralDocumentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreSuccess", "Student referral status updated successfully");
                    refreshReferralData(); // Call to refresh data after status update
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error updating student referral status", e);
                });
    }

    // Method to refresh referral data
    private void refreshReferralData() {
        // Call the onPendingClick method to refresh UI and fetch the latest data
        if (getActivity() != null && getView() != null) {
            getActivity().runOnUiThread(this::onPendingClick);
        } else {
            Log.e("UIError", "Activity or view is null, cannot refresh UI.");
        }
    }


    public void onAcceptedClick() {
        // Get views for pending and rejected
        LinearLayout linearLayoutPending = getView().findViewById(R.id.linear_layout_pending);
        ScrollView scrollViewPending = getView().findViewById(R.id.scroll_view_pending);

        LinearLayout linearLayoutAccepted = getView().findViewById(R.id.linear_layout_accepted);
        ScrollView scrollViewAccepted = getView().findViewById(R.id.scroll_view_accepted);
        LinearLayout linearLayoutRejected = getView().findViewById(R.id.linear_layout_rejected);
        ScrollView scrollViewRejected = getView().findViewById(R.id.scroll_view_rejected);

        // Clear other sections
        linearLayoutPending.removeAllViews();
        scrollViewPending.setVisibility(View.GONE);
        linearLayoutRejected.removeAllViews();
        scrollViewRejected.setVisibility(View.GONE);

        // Clear the LinearLayout for accepted referrals
        linearLayoutAccepted.removeAllViews();
        scrollViewAccepted.setVisibility(View.VISIBLE);

        // Fetch accepted referrals from the "accepted_status" sub-collection under "students"
        db.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot studentDocument : task.getResult().getDocuments()) {
                    // Access the student's referral history for 'accepted' status from the 'accepted_status' sub-collection
                    db.collection("students").document(studentDocument.getId())
                            .collection("accepted_status")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        // Add a CardView for each referral
                                        addReferralCard(linearLayoutAccepted, referralDocument);
                                    }
                                } else {
                                    Log.e("Firestore Error", "Error fetching referral documents: ", subTask.getException());
                                }
                            });
                }
            } else {
                Log.e("Firestore Error", "Error fetching student documents: ", task.getException());
            }
        });

        // Fetch accepted referrals from personnel
        db.collection("personnel").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot personnelDocument : task.getResult().getDocuments()) {
                    db.collection("personnel").document(personnelDocument.getId())
                            .collection("personnel_refferal_history")
                            .whereEqualTo("status", "accepted")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                    // Add a CardView for each referral
                                    addReferralCard(linearLayoutAccepted, referralDocument);
                                }
                            });
                }
            }
        });
    }

    // Helper function to add a CardView for each referral
    private void addReferralCard(LinearLayout parentLayout, DocumentSnapshot referralDocument) {
        // Create a new CardView
        CardView cardView = new CardView(getContext());
        cardView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardView.setRadius(8f);
        cardView.setCardElevation(6f);
        cardView.setUseCompatPadding(true);
        cardView.setPadding(16, 16, 16, 16);
        cardView.setContentPadding(16, 16, 16, 16);
        cardView.setClickable(true);
        cardView.setFocusable(true);

        // Create a LinearLayout inside the CardView
        LinearLayout cardContent = new LinearLayout(getContext());
        cardContent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardContent.setOrientation(LinearLayout.VERTICAL);

        // Add referral details to the card content
        String studentName = referralDocument.getString("student_name");
        String referralDate = referralDocument.getString("date");
        String status = referralDocument.getString("status");

        // Create and add TextViews for each detail
        TextView textStudentName = new TextView(getContext());
        textStudentName.setText("Student Name: " + studentName);
        textStudentName.setTextSize(16);
        textStudentName.setPadding(8, 8, 8, 4);

        TextView textReferralDate = new TextView(getContext());
        textReferralDate.setText("Referral Date: " + referralDate);
        textReferralDate.setTextSize(16);
        textReferralDate.setPadding(8, 4, 8, 4);

        TextView textStatus = new TextView(getContext());
        textStatus.setText("Status: " + status);
        textStatus.setTextColor(Color.GREEN);
        textStatus.setTextSize(16);
        textStatus.setPadding(8, 4, 8, 8);

        // Add TextViews to the card content
        cardContent.addView(textStudentName);
        cardContent.addView(textReferralDate);
        cardContent.addView(textStatus);

        // Add content to the CardView
        cardView.addView(cardContent);

        // Set click listener for showing detailed information
        cardView.setOnClickListener(v -> showReferralDetailsDialog(referralDocument));

        // Add the CardView to the parent layout
        parentLayout.addView(cardView);

        // Add a margin between cards
        View divider = new View(getContext());
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                8
        );
        divider.setLayoutParams(dividerParams);
        parentLayout.addView(divider);
    }

    // Method to show the referral details in a dialog
    private void showReferralDetailsDialog(DocumentSnapshot referralDocument) {
        // Get the details from the referral document
        String date = referralDocument.getString("date");
        String offense = referralDocument.getString("offense");
        String prefectReferrer = referralDocument.getString("prefect_referrer");
        String referrerId = referralDocument.getString("referrer_id");
        String remarks = referralDocument.getString("remarks");
        String status = referralDocument.getString("status");
        String studentId = referralDocument.getString("student_id");
        String studentName = referralDocument.getString("student_name");
        String studentProgram = referralDocument.getString("student_program");
        String term = referralDocument.getString("term");
        String userConcern = referralDocument.getString("user_concern");
        String violation = referralDocument.getString("violation");
        String violationStatus = referralDocument.getString("violation_status");
        String location = referralDocument.getString("location");
        String personnelReferrer = referralDocument.getString("personnel_referrer");
        String sanction = referralDocument.getString("sanction");

        // Build the string for the dialog content
        String details = "Date: " + date + "\n\n" +
                "Offense: " + offense + "\n\n" +
                "Prefect Referrer: " + prefectReferrer + "\n" +
                "Referrer ID: " + referrerId + "\n\n" +
                "Remarks: " + remarks + "\n\n" +
                "Status: " + status + "\n\n" +
                "Student ID: " + studentId + "\n" +
                "Student Name: " + studentName + "\n" +
                "Program: " + studentProgram + "\n" +
                "Term: " + term + "\n\n" +
                "User Concern: " + (userConcern != null ? userConcern : "N/A") + "\n\n" +
                "Violation: " + violation + "\n" +
                "Violation Status: " + violationStatus + "\n\n" +
                "Location: " + location + "\n" +
                "Personnel Referrer: " + personnelReferrer + "\n" +
                "Sanction: " + sanction;

        // Create an AlertDialog to show the details
        new AlertDialog.Builder(getContext())
                .setTitle("Referral Details")
                .setMessage(details)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }



    // Fetch rejected referrals from Firestore
    public void onRejectedClick() {
        // Get the LinearLayout and ScrollView for displaying rejected referrals
        LinearLayout linearLayoutRejected = getView().findViewById(R.id.linear_layout_rejected);
        ScrollView scrollViewRejected = getView().findViewById(R.id.scroll_view_rejected);

        // Get views for pending and accepted
        LinearLayout linearLayoutPending = getView().findViewById(R.id.linear_layout_pending);
        ScrollView scrollViewPending = getView().findViewById(R.id.scroll_view_pending);
        LinearLayout linearLayoutAccepted = getView().findViewById(R.id.linear_layout_accepted);
        ScrollView scrollViewAccepted = getView().findViewById(R.id.scroll_view_accepted);

        // Clear other sections
        linearLayoutPending.removeAllViews();
        scrollViewPending.setVisibility(View.GONE);
        linearLayoutAccepted.removeAllViews();
        scrollViewAccepted.setVisibility(View.GONE);

        // Show the ScrollView for rejected
        scrollViewRejected.setVisibility(View.VISIBLE);
        linearLayoutRejected.removeAllViews();

        // Query Firestore for "rejected" status
        fetchRejectedReferrals(linearLayoutRejected);
    }

    private void fetchRejectedReferrals(LinearLayout linearLayoutRejected) {
        // Fetch rejected referrals for students
        db.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot studentDocument : task.getResult().getDocuments()) {
                    db.collection("students").document(studentDocument.getId())
                            .collection("student_refferal_history")
                            .whereEqualTo("status", "rejected")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        addReferralCard(linearLayoutRejected, referralDocument, "Rejected");
                                    }
                                }
                            });
                }
            }
        });

        // Fetch rejected referrals for personnel
        db.collection("personnel").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot personnelDocument : task.getResult().getDocuments()) {
                    db.collection("personnel").document(personnelDocument.getId())
                            .collection("personnel_refferal_history")
                            .whereEqualTo("status", "rejected")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        addReferralCard(linearLayoutRejected, referralDocument, "Rejected");
                                    }
                                }
                            });
                }
            }
        });
    }

    private void addReferralCard(LinearLayout parentLayout, DocumentSnapshot referralDocument, String status) {
        String studentName = referralDocument.getString("student_name");
        String referralDate = referralDocument.getString("date");

        // Create a CardView
        CardView cardView = new CardView(getContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(16);
        cardView.setCardElevation(8);

        // Create a LinearLayout inside the CardView
        LinearLayout cardContent = new LinearLayout(getContext());
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(32, 32, 32, 32);

        // Add TextViews to display referral details
        TextView nameView = new TextView(getContext());
        nameView.setText("Student Name: " + studentName);
        nameView.setTextSize(16);
        nameView.setTypeface(null, Typeface.BOLD);

        TextView dateView = new TextView(getContext());
        dateView.setText("Referral Date: " + referralDate);
        dateView.setTextSize(14);

        TextView statusView = new TextView(getContext());
        statusView.setText("Status: " + status);
        statusView.setTextColor(Color.RED);
        statusView.setTextSize(14);

        // Add TextViews to the card content layout
        cardContent.addView(nameView);
        cardContent.addView(dateView);
        cardContent.addView(statusView);

        // Add click listener to the CardView to show details in a dialog
        cardView.setOnClickListener(v -> {
            showRejectedReferralDetailsDialog(referralDocument);
        });

        // Add the card content to the CardView
        cardView.addView(cardContent);

        // Add the CardView to the parent layout
        parentLayout.addView(cardView);
    }

    private void showRejectedReferralDetailsDialog(DocumentSnapshot referralDocument) {
        // Extract data from the referral document
        String date = referralDocument.getString("date");
        String offense = referralDocument.getString("offense");
        String personnelReferrer = referralDocument.getString("personnel_referrer");
        String reasonRejecting = referralDocument.getString("reason_rejecting");
        String remarks = referralDocument.getString("remarks");
        String status = referralDocument.getString("status");
        String studentId = referralDocument.getString("student_id");
        String studentName = referralDocument.getString("student_name");
        String studentProgram = referralDocument.getString("student_program");
        String term = referralDocument.getString("term");
        String userConcern = referralDocument.getString("user_concern");
        String violation = referralDocument.getString("violation");

        // Build the string for the dialog content
        String details = "Date: " + date + "\n\n" +
                "Offense: " + offense + "\n\n" +
                "Personnel Referrer: " + personnelReferrer + "\n\n" +
                "Reason for Rejection: " + reasonRejecting + "\n\n" +
                "Remarks: " + remarks + "\n\n" +
                "Status: " + status + "\n\n" +
                "Student ID: " + studentId + "\n" +
                "Student Name: " + studentName + "\n" +
                "Program: " + studentProgram + "\n" +
                "Term: " + term + "\n\n" +
                "User Concern: " + (userConcern != null ? userConcern : "N/A") + "\n\n" +
                "Violation: " + violation;

        // Create and show an AlertDialog with the referral details
        new AlertDialog.Builder(getContext())
                .setTitle("Rejected Referral Details")
                .setMessage(details)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private TableRow createTableRow(String studentName, String referralDate, String status) {
        TableRow row = new TableRow(getActivity());
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        row.addView(createTextView(studentName, 16, 16));
        row.addView(createTextView(referralDate, 16, 14));
        row.addView(createTextView(status, 16, 16));

        return row;
    }

    private TextView createTextView(String text, int textSize, int padding) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setPadding(padding, 8, padding, 8);
        return textView;
    }

    private void addDivider(TableLayout tableLayout) {
        View headerDivider = new View(getActivity());
        headerDivider.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                2 // Height of the divider
        ));
        headerDivider.setBackgroundColor(Color.GRAY); // Color of the divider
        tableLayout.addView(headerDivider);
    }

}