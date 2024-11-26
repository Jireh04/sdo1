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

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        horizontalScrollView.setLayoutParams(new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.MATCH_PARENT,
                HorizontalScrollView.LayoutParams.WRAP_CONTENT
        ));

        TableLayout tableLayout = new TableLayout(getActivity());
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        ));
        tableLayout.setStretchAllColumns(true);

        // Add table header row
        TableRow headerRow = new TableRow(getActivity());
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Create TextViews for table headers
        TextView headerReferrer = new TextView(getActivity());
        headerReferrer.setText("Referrer");
        headerReferrer.setTypeface(null, Typeface.BOLD);
        headerReferrer.setTextSize(16);
        headerReferrer.setPadding(16, 8, 16, 8);
        headerRow.addView(headerReferrer);

        TextView headerDate = new TextView(getActivity());
        headerDate.setText("Date");
        headerDate.setGravity(Gravity.CENTER);
        headerDate.setTypeface(null, Typeface.BOLD);
        headerDate.setTextSize(16);
        headerDate.setPadding(16, 8, 16, 8);
        headerRow.addView(headerDate);

        TextView headerActions = new TextView(getActivity());
        headerActions.setText("Actions");
        headerActions.setGravity(Gravity.CENTER);
        headerActions.setTypeface(null, Typeface.BOLD);
        headerActions.setTextSize(16);
        headerActions.setPadding(16, 8, 16, 8);
        headerRow.addView(headerActions);

        // Add the header row to the table layout
        tableLayout.addView(headerRow);

        // Add a divider after the header row
        View headerDivider = new View(getActivity());
        headerDivider.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                2 // Height of the divider
        ));
        headerDivider.setBackgroundColor(Color.GRAY); // Color of the divider
        tableLayout.addView(headerDivider);

        // Query Firestore collections for "pending" status
        db.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot studentDocument : task.getResult().getDocuments()) {
                    db.collection("students").document(studentDocument.getId())
                            .collection("student_refferal_history")
                            .orderBy("date", Query.Direction.DESCENDING) // Sort by date in descending order

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

                                                // Create a TableRow for each referral
                                                TableRow tableRow = new TableRow(getActivity());
                                                tableRow.setLayoutParams(new TableRow.LayoutParams(
                                                        TableRow.LayoutParams.MATCH_PARENT,
                                                        TableRow.LayoutParams.WRAP_CONTENT
                                                ));

                                                // Create a TextView for the referrer
                                                TextView referrerTextView = new TextView(getActivity());
                                                referrerTextView.setText(studentReferrer);
                                                referrerTextView.setPadding(16, 8, 16, 8);
                                                referrerTextView.setTextSize(14);
                                                tableRow.addView(referrerTextView);

                                                // Create a TextView for the referral date
                                                TextView dateTextView = new TextView(getActivity());
                                                dateTextView.setText(referralDate);
                                                dateTextView.setPadding(16, 8, 16, 8);
                                                dateTextView.setTextSize(14);
                                                tableRow.addView(dateTextView);

                                                // Create a Spinner (dropdown menu) for actions
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
                                                                            // Update status in the student's referral document
                                                                            updateReferralStatusStudent(referrerId, referralDocument.getId(), "accepted");

                                                                            // Create a map to hold all referral details
                                                                            Map<String, Object> acceptedData = new HashMap<>();
                                                                            acceptedData.put("date", referralData.get("date")); // Example: "2024-11-02 13:12"
                                                                            acceptedData.put("referrer_id", referralData.get("referrer_id")); // Example: "221-2424"
                                                                            acceptedData.put("remarks", referralData.get("remarks")); // Example: "jaii"
                                                                            acceptedData.put("status", "accepted"); // Update status to "accepted"
                                                                            acceptedData.put("student_id", referralData.get("student_id")); // Example: "221-0896"
                                                                            acceptedData.put("student_name", referralData.get("student_name")); // Example: "RANIELLE ANTHONY JARAPLASAN LUISTRO"
                                                                            acceptedData.put("student_program", referralData.get("student_program")); // Example: "BSIT-SD"
                                                                            acceptedData.put("student_referrer", referralData.get("student_referrer")); // Example: "ABANTO KATHLEEN LIZETH DORIA"
                                                                            acceptedData.put("term", referralData.get("term")); // Example: "First Sem"
                                                                            acceptedData.put("user_concern", referralData.get("user_concern")); // Example: "Discipline Concerns"
                                                                            acceptedData.put("violation", referralData.get("violation")); // Example: "Major Offense"
                                                                            acceptedData.put("offense", referralData.get("offense")); // Example: "Major Offense"
                                                                            acceptedData.put("violation_status", "Unsettled"); // Add the new field "violation_status"


                                                                            // Save the accepted referral data in the `accepted_status` sub-collection
                                                                            db.collection("students").document(studentId)
                                                                                    .collection("accepted_status")
                                                                                    .document(referralDocument.getId())  // Use the same document ID for consistency
                                                                                    .set(acceptedData)  // Save all details in the acceptedData map
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

                                                                // Create the EditText where the user will input the reason
                                                                EditText reasonEditText = rejectView.findViewById(R.id.editTextReason);

                                                                new AlertDialog.Builder(getActivity())
                                                                        .setTitle("Rejection Reason")
                                                                        .setMessage("Please provide a reason for rejecting this referral:")
                                                                        .setView(rejectView) // Attach the custom view to the dialog
                                                                        .setPositiveButton("Reject", (dialog, which) -> {
                                                                            String rejectionReason = reasonEditText.getText().toString().trim();
                                                                            if (!rejectionReason.isEmpty()) {
                                                                                // Proceed to update the Firestore document with the rejection reason
                                                                                updateReferralStatusStudentWithReason(referrerId, referralDocument.getId(), "rejected", rejectionReason);
                                                                            } else {
                                                                                Toast.makeText(getActivity(), "Please enter a rejection reason.", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        })
                                                                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                                                        .show();
                                                                break;

                                                        }
                                                        // Reset spinner to default after action
                                                        actionsSpinner.setSelection(0);
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {
                                                        // No action needed
                                                    }
                                                });

                                                // Add the Spinner to the table row
                                                tableRow.addView(actionsSpinner);

                                                // Add the table row to the table layout
                                                tableLayout.addView(tableRow);
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        });



        horizontalScrollView.addView(tableLayout);
        linearLayoutPending.addView(horizontalScrollView);

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

                                                                            // Now save the accepted data in students/{studentId}/accepted_status
                                                                            // Prepare a Map to save the data in the 'accepted_status' sub-collection
                                                                            Map<String, Object> acceptedData = new HashMap<>();
                                                                            acceptedData.put("date", referralData.get("date")); // Example: "2024-11-02 13:12"
                                                                            acceptedData.put("referrer_id", referralData.get("referrer_id")); // Example: "221-2424"
                                                                            acceptedData.put("remarks", referralData.get("remarks")); // Example: "jaii"
                                                                            acceptedData.put("status", "accepted"); // Update status to "accepted"
                                                                            acceptedData.put("student_id", referralData.get("student_id")); // Example: "221-0896"
                                                                            acceptedData.put("student_name", referralData.get("student_name")); // Example: "RANIELLE ANTHONY JARAPLASAN LUISTRO"
                                                                            acceptedData.put("student_program", referralData.get("student_program")); // Example: "BSIT-SD"
                                                                            acceptedData.put("personnel_referrer", referralData.get("personnel_referrer")); // Example: "ABANTO KATHLEEN LIZETH DORIA"
                                                                            acceptedData.put("term", referralData.get("term")); // Example: "First Sem"
                                                                            acceptedData.put("user_concern", referralData.get("user_concern")); // Example: "Discipline Concerns"
                                                                            acceptedData.put("violation", referralData.get("violation")); // Example: "Major Offense"
                                                                            acceptedData.put("offense", referralData.get("offense")); // Example: "Major Offense"
                                                                            acceptedData.put("violation_status", "Unsettled"); // Add the new field "violation_status"


                                                                            // Ensure that the student_id is a String when using it as a document ID
                                                                            String studentId = (String) referralData.get("student_id");  // Ensure the student_id is treated as a String

                                                                            // Save the accepted data in the 'accepted_status' sub-collection for the student
                                                                            db.collection("students")  // Access the students collection
                                                                                    .document(studentId)  // Use the student's ID (ensuring it's a String)
                                                                                    .collection("accepted_status")  // Target the 'accepted_status' sub-collection
                                                                                    .document(referralDocument.getId())  // Use the referral document ID for consistency
                                                                                    .set(acceptedData)  // Save the details in the map
                                                                                    .addOnSuccessListener(aVoid -> {
                                                                                        // Show success message when data is successfully saved
                                                                                        Toast.makeText(getActivity(), "Referral accepted and saved.", Toast.LENGTH_SHORT).show();
                                                                                    })
                                                                                    .addOnFailureListener(e -> {
                                                                                        // Show failure message if saving fails
                                                                                        Toast.makeText(getActivity(), "Failed to save accepted referral.", Toast.LENGTH_SHORT).show();
                                                                                    });
                                                                        })
                                                                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                                                        .show();
                                                                break;


                                                            case "Reject":
                                                                // Create a custom layout for rejection reason
                                                                View rejectView = getLayoutInflater().inflate(R.layout.reject_reason, null);

                                                                // Create the EditText where personnel will input the rejection reason
                                                                EditText reasonEditText = rejectView.findViewById(R.id.editTextReason);

                                                                // Create the dialog to show the rejection options
                                                                new AlertDialog.Builder(getActivity())
                                                                        .setTitle("Rejection Reason")
                                                                        .setMessage("Please provide a reason for rejecting this referral:")
                                                                        .setView(rejectView) // Attach the custom view to the dialog
                                                                        .setPositiveButton("Reject", (dialog, which) -> {
                                                                            String rejectionReason = reasonEditText.getText().toString().trim();
                                                                            if (!rejectionReason.isEmpty()) {
                                                                                // Proceed to update the Firestore document with the rejection reason
                                                                                updateReferralStatusPersonnel(referrerId, referralDocument.getId(), "rejected", rejectionReason);

                                                                                // Optional: Show a Toast to indicate the referral was rejected successfully
                                                                                Toast.makeText(getActivity(), "Referral has been rejected.", Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                Toast.makeText(getActivity(), "Please enter a rejection reason.", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        })
                                                                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // Dismiss the dialog if Cancel is clicked
                                                                        .show();
                                                                break;

                                                        }

                                                        // Reset selection to "Select Action" after handling the action
                                                        actionSpinner.setSelection(0);
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {
                                                        // Do nothing
                                                    }
                                                });

                                                // Add the spinner to the table row
                                                tableRow.addView(actionSpinner);

                                                // Add the table row to the table layout
                                                tableLayout.addView(tableRow);
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

        // Clear the LinearLayout to remove previous data
        linearLayoutAccepted.removeAllViews();
        scrollViewAccepted.setVisibility(View.VISIBLE);

        // Create a new TableLayout for accepted referrals
        TableLayout tableLayoutAccepted = new TableLayout(getContext());
        tableLayoutAccepted.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create a new HorizontalScrollView for displaying the TableLayout
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        horizontalScrollView.setLayoutParams(new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.MATCH_PARENT,
                HorizontalScrollView.LayoutParams.WRAP_CONTENT
        ));
        horizontalScrollView.addView(tableLayoutAccepted);

        // Add the horizontalScrollView to linearLayoutAccepted, ensuring no parent is set
        if (horizontalScrollView.getParent() != null) {
            ((ViewGroup) horizontalScrollView.getParent()).removeView(horizontalScrollView);
        }
        linearLayoutAccepted.addView(horizontalScrollView);

        // Add headers for the TableLayout
        TableRow headerRow = new TableRow(getContext());
        headerRow.setPadding(16, 16, 16, 16);

        TextView headerStudent = new TextView(getContext());
        headerStudent.setText("Student Name");
        headerStudent.setTextSize(18);
        headerStudent.setTypeface(null, Typeface.BOLD);
        headerStudent.setPadding(16, 8, 16, 8);

        TextView headerDate = new TextView(getContext());
        headerDate.setText("Referral Date");
        headerDate.setTextSize(18);
        headerDate.setTypeface(null, Typeface.BOLD);
        headerDate.setPadding(14, 8, 12, 8);

        TextView headerStatus = new TextView(getContext());
        headerStatus.setText("Status");
        headerStatus.setTextSize(18);
        headerStatus.setTypeface(null, Typeface.BOLD);
        headerStatus.setPadding(12, 8, 16, 8);

        headerRow.addView(headerStudent);
        headerRow.addView(headerDate);
        headerRow.addView(headerStatus);
        tableLayoutAccepted.addView(headerRow);

        // Divider after the header row
        View headerDivider = new View(getActivity());
        headerDivider.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                2
        ));
        headerDivider.setBackgroundColor(Color.GRAY);
        tableLayoutAccepted.addView(headerDivider);

        // Fetch accepted referrals from the "accepted_status" sub-collection under "students"
        db.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot studentDocument : task.getResult().getDocuments()) {
                    // Access the student's referral history for 'accepted' status from the 'accepted_status' sub-collection
                    db.collection("students").document(studentDocument.getId())
                            .collection("accepted_status")  // Fetch from the new sub-collection 'accepted_status'
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        // Add a row in the accepted table layout for each referral
                                        addReferralRow(tableLayoutAccepted, referralDocument);
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
    }


    // Helper function to add a row to the table for each referral
    // Helper function to add a row to the table for each referral
    private void addReferralRow(TableLayout tableLayout, DocumentSnapshot referralDocument) {
        String studentName = referralDocument.getString("student_name");
        String referralDate = referralDocument.getString("date");
        String offense = referralDocument.getString("offense");
        String prefectReferrer = referralDocument.getString("prefect_referrer");
        String referrerId = referralDocument.getString("referrer_id");
        String remarks = referralDocument.getString("remarks");
        String status = referralDocument.getString("status");
        String studentId = referralDocument.getString("student_id");
        String studentProgram = referralDocument.getString("student_program");
        String term = referralDocument.getString("term");
        String userConcern = referralDocument.getString("user_concern");
        String violation = referralDocument.getString("violation");
        String violationStatus = referralDocument.getString("violation_status");
        String location = referralDocument.getString("location");
        String personnelReferrer = referralDocument.getString("personnel_referrer");
        String sanction = referralDocument.getString("sanction");

        // Create a new table row
        TableRow row = new TableRow(getContext());
        row.setPadding(16, 8, 16, 8);

        // Create and add TextViews for each column in the row
        TextView textStudentName = new TextView(getActivity());
        textStudentName.setText(studentName);
        textStudentName.setPadding(16, 8, 16, 8);

        TextView textReferralDate = new TextView(getActivity());
        textReferralDate.setText(referralDate);
        textReferralDate.setPadding(14, 8, 12, 8);

        TextView textStatus = new TextView(getActivity());
        textStatus.setText(status);
        textStatus.setPadding(12, 8, 16, 8);

        // Add the TextViews to the row
        row.addView(textStudentName);
        row.addView(textReferralDate);
        row.addView(textStatus);

        // Set the click listener for the row to show the dialog
        row.setOnClickListener(v -> {
            // Show a dialog with the referral details
            showReferralDetailsDialog(referralDocument);
        });

        // Add the row to the table
        tableLayout.addView(row);
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

        // Create a HorizontalScrollView for the TableLayout
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        horizontalScrollView.setLayoutParams(new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.MATCH_PARENT,
                HorizontalScrollView.LayoutParams.WRAP_CONTENT
        ));

        // Create a TableLayout for rejected referrals
        TableLayout tableLayout = new TableLayout(getActivity());
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        tableLayout.setStretchAllColumns(true);

        // Add headers for the TableLayout
        TableRow headerRow = new TableRow(getContext());
        headerRow.setPadding(16, 16, 16, 16);

        TextView headerStudent = new TextView(getContext());
        headerStudent.setText("Student Name");
        headerStudent.setTextSize(18);
        headerStudent.setTypeface(null, Typeface.BOLD);
        headerStudent.setPadding(16, 8, 16, 8);

        TextView headerDate = new TextView(getContext());
        headerDate.setText("Referral Date");
        headerDate.setTextSize(18);
        headerDate.setTypeface(null, Typeface.BOLD);
        headerDate.setPadding(14, 8, 12, 8);

        TextView headerStatus = new TextView(getContext());
        headerStatus.setText("Status");
        headerStatus.setTextSize(18);
        headerStatus.setTypeface(null, Typeface.BOLD);
        headerStatus.setPadding(12, 8, 16, 8);

        headerRow.addView(headerStudent);
        headerRow.addView(headerDate);
        headerRow.addView(headerStatus);
        tableLayout.addView(headerRow);

        // Add a divider after the header row
        addDivider(tableLayout);

        // Query Firestore for "rejected" status
        fetchRejectedReferrals(tableLayout);

        // Add the TableLayout to the HorizontalScrollView
        horizontalScrollView.addView(tableLayout);

        // Add the HorizontalScrollView to the LinearLayout for rejected referrals
        linearLayoutRejected.addView(horizontalScrollView);
    }

    private void fetchRejectedReferrals(TableLayout tableLayout) {
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
                                        addReferralRow(tableLayout, referralDocument, "Rejected");
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
                                        addReferralRow(tableLayout, referralDocument, "Rejected");
                                    }
                                }
                            });
                }
            }
        });
    }

    private void addReferralRow(TableLayout tableLayout, DocumentSnapshot referralDocument, String status) {
        String studentName = referralDocument.getString("student_name");
        String referralDate = referralDocument.getString("date");

        // Create a TableRow for each rejected referral
        TableRow row = createTableRow(studentName, referralDate, status);

        // Add click listener to show dialog with referral details
        row.setOnClickListener(v -> {
            // Show a dialog with the rejected referral details
            showRejectedReferralDetailsDialog(referralDocument);
        });

        tableLayout.addView(row);
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