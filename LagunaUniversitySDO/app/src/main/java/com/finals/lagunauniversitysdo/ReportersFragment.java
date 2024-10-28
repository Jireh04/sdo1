package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import android.content.Intent;
import android.widget.Toast;


import java.util.HashMap;
import java.util.Map;

public class ReportersFragment extends Fragment {

    private LinearLayout reportersContainer;
    private FirebaseFirestore db;
    private AlertDialog detailDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reporters, container, false);

        reportersContainer = view.findViewById(R.id.reportersContainer);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Fetch referrer data
        fetchReferrerData();

        return view; // Return the inflated view
    }

    private void fetchReferrerData() {
        Map<String, String> referrerMap = new HashMap<>(); // Store unique referrer names with their types

        // Fetch data from each collection
        fetchFromCollection("prefect_referral_history", "prefect_referrer", "Prefect", referrerMap);
        fetchFromCollection("student_refferal_history", "student_referrer", "Student", referrerMap);
        fetchFromCollection("personnel_refferal_history", "personnel_referrer", "Personnel", referrerMap);
    }

    private void fetchFromCollection(String collectionName, String fieldName, String referrerType, Map<String, String> referrerMap) {
        db.collection(collectionName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String referrerName = document.getString(fieldName); // Fetch using the field name
                        if (referrerName != null && !referrerMap.containsKey(referrerName)) {
                            // Only add if the referrer name isn't already present
                            referrerMap.put(referrerName, referrerType); // Store the type associated with the referrer name
                        }
                    }
                    // After fetching from all collections, update the UI
                    updateUI(referrerMap);
                } else {
                    showNoDataMessage(); // Handle the error case
                }
            }
        });
    }

    private void updateUI(Map<String, String> referrerMap) {
        // Clear existing views except for the title
        reportersContainer.removeViews(1, reportersContainer.getChildCount() - 1);

        if (referrerMap.isEmpty()) {
            showNoDataMessage();
        } else {
            displayReferrerNames(referrerMap);
        }
    }

    private void showNoDataMessage() {
        // Ensure that the title remains visible
        TextView noDataTextView = new TextView(getContext());
        noDataTextView.setText("No referrer names available");
        noDataTextView.setTextSize(18); // Adjust text size as needed
        noDataTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        noDataTextView.setPadding(16, 16, 16, 16); // Optional padding

        // Add the TextView to the container
        reportersContainer.addView(noDataTextView);
    }

    private void displayReferrerNames(Map<String, String> referrerMap) {


        for (Map.Entry<String, String> entry : referrerMap.entrySet()) {
            String referrerName = entry.getKey();
            String referrerType = entry.getValue();

            // Inflate the layout for each referrer item (referrer_item.xml)
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View referrerItemView = inflater.inflate(R.layout.reporter_item, reportersContainer, false);

            // Find the TextView and Button in the inflated layout
            TextView referrerTextView = referrerItemView.findViewById(R.id.reporterName);
            Button viewLogsButton = referrerItemView.findViewById(R.id.viewLogsButton);

            // Set the text for the referrer name
            referrerTextView.setText(referrerType + ": " + referrerName);

            // Set an OnClickListener for the "View Logs" button
            viewLogsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Pass the referrer type and name to viewLogs
                    viewLogs(referrerType, referrerName);
                }
            });

            // Add the inflated referrer item view to the container
            reportersContainer.addView(referrerItemView);
        }
    }


    private void viewLogs(String referrerType, String referrerName) {
        String collectionName;

        // Determine which collection to query based on the referrer type
        switch (referrerType) {
            case "Prefect":
                collectionName = "prefect_referral_history";
                break;
            case "Student":
                collectionName = "student_refferal_history";
                break;
            case "Personnel":
                collectionName = "personnel_refferal_history";
                break;
            default:
                collectionName = "";
                break;
        }

        if (!collectionName.isEmpty()) {
            // Query Firestore to fetch the details for the given referrer name
            db.collection(collectionName)
                    .whereEqualTo(referrerType.toLowerCase() + "_referrer", referrerName)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                String logDetails = ""; // For concatenating log details
                                String violations = "";  // For accumulating violations

                                // Create the Intent here to pass data to the ReporterView
                                Intent intent = new Intent(getActivity(), ReporterView.class);

                                // Add referrer name to the intent
                                intent.putExtra("REFERRER_NAME", referrerName);

                                for (DocumentSnapshot document : task.getResult()) {
                                    // Extract student details from the document
                                    String studentId = document.getString("student_id");
                                    String studentName = document.getString("student_name");
                                    String studentProgram = document.getString("student_program");
                                    String studentContact = document.getString("student_contact");
                                    String studentYear = document.getString("student_year");
                                    String studentBlock = document.getString("block");
                                    String remarks = document.getString("remarks");
                                    String status = document.getString("status");
                                    String userConcern = document.getString("user_concern");
                                    String violation = document.getString("violation");

                                    // Accumulate student details into the intent
                                    intent.putExtra("STUDENT_ID", studentId);
                                    intent.putExtra("STUDENT_NAME", studentName);
                                    intent.putExtra("STUDENT_PROGRAM", studentProgram);
                                    intent.putExtra("STUDENT_CONTACT", studentContact);
                                    intent.putExtra("STUDENT_YEAR", studentYear);
                                    intent.putExtra("STUDENT_BLOCK", studentBlock);

                                    // Create a string for the current log entry's details
                                    logDetails += "Student Name: " + studentName + "\n" +
                                            "Remarks: " + (remarks != null ? remarks : "N/A") + "\n" +
                                            "Status: " + status + "\n" +
                                            "User Concern: " + (userConcern != null ? userConcern : "N/A") + "\n" +
                                            "Violation: " + (violation != null ? violation : "None") + "\n\n";

                                    // Accumulate violations if available
                                    if (violation != null && !violation.isEmpty()) {
                                        violations += violation + "\n";
                                    }
                                }

                                // Store the accumulated violations in the intent
                                intent.putExtra("VIOLATIONS", violations); // Pass all violations as a single string

                                // Show the logs in a dialog
                                showLogDetailsDialog(referrerName, logDetails, intent);
                            } else {
                                // Handle the case where no logs are found
                                TextView noLogsTextView = new TextView(getActivity());
                                noLogsTextView.setText("No logs found for this referrer.");
                                noLogsTextView.setPadding(16, 16, 16, 16);
                                noLogsTextView.setTextSize(16);
                                showLogsDialog(referrerName, noLogsTextView);
                            }
                        }
                    });
        }
    }

    private void showLogDetailsDialog(String referrerName, String logDetails, Intent intent) {
        // Create an AlertDialog to show detailed log information
        AlertDialog.Builder detailDialogBuilder = new AlertDialog.Builder(getContext());
        detailDialogBuilder.setTitle("Log Details for " + referrerName);

        // Create a ScrollView to hold the log details
        ScrollView scrollView = new ScrollView(getContext());

        // Create a LinearLayout to hold the log details and buttons
        LinearLayout dialogLayout = new LinearLayout(getContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(16, 16, 16, 16);

        // Split the log details into individual lines
        String[] logEntries = logDetails.split("\n\n");
        for (String logEntry : logEntries) {
            // Create a TextView for each log entry
            TextView logEntryTextView = new TextView(getContext());
            logEntryTextView.setText(logEntry);
            logEntryTextView.setPadding(0, 0, 0, 10); // Optional spacing between entries
            dialogLayout.addView(logEntryTextView);

            // Extract the student's name from the log entry (assuming it's the first line)
            String studentName = logEntry.split("\n")[0].replace("Student Name: ", "");

            // Extract the violation for this entry (assuming it's the last line)
            String violation = logEntry.split("\n")[logEntry.split("\n").length - 1].replace("Violation: ", "");

            // Create a Button for each log entry
            Button viewEntryButton = new Button(getContext());
            viewEntryButton.setText("View");
            viewEntryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Fetch student details from Firestore based on the student name and the violation
                    fetchStudentDetails(studentName, violation,referrerName);
                }
            });
            dialogLayout.addView(viewEntryButton);
        }

        // Add the LinearLayout to the ScrollView
        scrollView.addView(dialogLayout);

        // Set the ScrollView to the dialog
        detailDialogBuilder.setView(scrollView);
        detailDialogBuilder.setPositiveButton("Close", null);

        // Create and show the detail dialog
        detailDialog = detailDialogBuilder.create();
        detailDialog.show();
    }

    private void fetchStudentDetails(String studentName, String violation, String referrerName) {
        db.collection("student_refferal_history") // Adjust this collection name as necessary
                .whereEqualTo("student_name", studentName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Get the first document found
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);

                            // Create a new instance of ReporterView fragment with arguments
                            ReporterView reporterViewFragment = ReporterView.newInstance(
                                    document.getString("student_id"),
                                    document.getString("student_name"),
                                    document.getString("student_program"),
                                    document.getString("student_contact"),
                                    document.getString("student_year"),
                                    document.getString("block"),
                                    document.getString("remarks"),
                                    violation, // Pass the violation
                                    referrerName // Pass the referrer name
                            );

                            // Replace the current fragment with ReporterView fragment
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, reporterViewFragment) // Make sure to use the correct container ID
                                    .addToBackStack(null) // Optionally add to back stack
                                    .commit();

                            detailDialog.dismiss();
                        } else {
                            // Handle case where no student details were found
                            Toast.makeText(getContext(), "No student details found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void showLogsDialog(String referrerName, TextView noLogsTextView) {
        // Create an AlertDialog to show the message
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Report Logs for " + referrerName);
        dialogBuilder.setView(noLogsTextView);
        dialogBuilder.setPositiveButton("Close", null);

        // Create and show the AlertDialog
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


}
