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
import androidx.fragment.app.FragmentTransaction;

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
        fetchFromCollection("students", "student_refferal_history", "Student", referrerMap);
        fetchFromCollection("personnel", "personnel_refferal_history", "Personnel", referrerMap);
        fetchFromCollection("prefect", "prefect_referral_history", "Prefect", referrerMap);
    }

    private void fetchFromCollection(String collectionName, String subCollectionName, String referrerType, Map<String, String> referrerMap) {
        db.collection(collectionName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String referrerId = document.getId(); // Use the document ID as referrer name

                        // Fetch from the subcollection within each document
                        db.collection(collectionName)
                                .document(referrerId)
                                .collection(subCollectionName)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                            // Add the referrer if not already present in the map
                                            if (!referrerMap.containsKey(referrerId)) {
                                                referrerMap.put(referrerId, referrerType); // Store the type with the referrer ID
                                            }
                                            updateUI(referrerMap); // Update the UI with fetched data
                                        }
                                    }
                                });
                    }
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
            String referrerId = entry.getKey();
            String referrerType = entry.getValue();

            // Inflate the layout for each referrer item (referrer_item.xml)
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View referrerItemView = inflater.inflate(R.layout.reporter_item, reportersContainer, false);

            // Find the TextView and Button in the inflated layout
            TextView referrerTextView = referrerItemView.findViewById(R.id.reporterName);
            Button viewLogsButton = referrerItemView.findViewById(R.id.viewLogsButton);

            // Set the text for the referrer name
            referrerTextView.setText(referrerType + ": " + referrerId);

            // Set an OnClickListener for the "View Logs" button
            viewLogsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewLogs(referrerType, referrerId);
                }
            });

            // Add the inflated referrer item view to the container
            reportersContainer.addView(referrerItemView);
        }
    }

    private void viewLogs(String referrerType, String referrerId) {
        String collectionName;
        String subCollectionName;

        // Determine which collection and subcollection to query based on referrer type
        switch (referrerType) {
            case "Student":
                collectionName = "students";
                subCollectionName = "student_refferal_history";
                break;
            case "Personnel":
                collectionName = "personnel";
                subCollectionName = "personnel_refferal_history";
                break;
            case "Prefect":
                collectionName = "prefect";
                subCollectionName = "prefect_referral_history";
                break;
            default:
                return;
        }

        // Query Firestore to fetch the details for the given referrer
        db.collection(collectionName)
                .document(referrerId)
                .collection(subCollectionName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            AlertDialog.Builder detailDialogBuilder = new AlertDialog.Builder(getContext());
                            detailDialogBuilder.setTitle("Log Details for " + referrerId);

                            LinearLayout logContainer = new LinearLayout(getContext());
                            logContainer.setOrientation(LinearLayout.VERTICAL);
                            ScrollView scrollView = new ScrollView(getContext());
                            scrollView.addView(logContainer);

                            for (DocumentSnapshot document : task.getResult()) {
                                // Extract student details from the document
                                String studentName = document.getString("student_name");
                                String studentId = document.getString("student_id");
                                String studentProgram = document.getString("student_program");
                                String studentContact = document.getString("student_contact");
                                String studentYear = document.getString("student_year");
                                String block = document.getString("block");
                                String violation = document.getString("violation");
                                String status = document.getString("status");
                                String remarks = document.getString("remarks");
                                String date = document.getString("date");
                                String reffer = document.getString("student_reffer");

                                // Create a TextView for each violation summary
                                TextView logSummaryTextView = new TextView(getContext());
                                logSummaryTextView.setText("Student: " + studentName + "\nViolation: " + violation + "\nStatus: " + status + "\n");
                                logContainer.addView(logSummaryTextView);

                                // Create a button for each violation
                                Button viewViolationButton = new Button(getContext());
                                viewViolationButton.setText("View");
                                viewViolationButton.setPadding(16, 8, 16, 8);

                                // Set up button click listener to open ReporterView fragment with violation details
                                // Set up button click listener to open ReporterView fragment with detailed information
                                viewViolationButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Create a new instance of the ReporterView fragment
                                        ReporterView reporterViewFragment = new ReporterView();

                                        // Create a bundle to pass the details to the fragment
                                        Bundle args = new Bundle();
                                        args.putString("STUDENT_ID", studentId);
                                        args.putString("STUDENT_NAME", studentName);
                                        args.putString("STUDENT_PROGRAM", studentProgram);
                                        args.putString("STUDENT_CONTACT", studentContact);
                                        args.putString("STUDENT_YEAR", studentYear);
                                        args.putString("BLOCK", block);
                                        args.putString("VIOLATION", violation);
                                        args.putString("REMARKS", remarks);
                                        args.putString("DATE", date);
                                        args.putString("STUDENT_REFERRER", reffer);

                                        // Set the arguments for the fragment
                                        reporterViewFragment.setArguments(args);

                                        // Replace the current fragment with ReporterView fragment
                                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                        transaction.replace(R.id.fragment_container, reporterViewFragment);
                                        transaction.addToBackStack(null); // Optional: adds to back stack
                                        transaction.commit();

                                        detailDialog.dismiss();// Close the detail dialog
                                    }
                                });

                                logContainer.addView(viewViolationButton);
                            }

                            detailDialogBuilder.setView(scrollView);
                            detailDialogBuilder.setPositiveButton("Close", null);
                            detailDialog = detailDialogBuilder.create();
                            detailDialog.show();
                        } else {
                            showLogsDialog(referrerId, "No logs found for this referrer.");
                        }
                    }
                });
    }


    private void showLogDetailsDialog(String referrerId, String logDetails) {
        AlertDialog.Builder detailDialogBuilder = new AlertDialog.Builder(getContext());
        detailDialogBuilder.setTitle("Log Details for " + referrerId);

        ScrollView scrollView = new ScrollView(getContext());
        TextView logDetailsTextView = new TextView(getContext());
        logDetailsTextView.setText(logDetails);
        scrollView.addView(logDetailsTextView);

        detailDialogBuilder.setView(scrollView);
        detailDialogBuilder.setPositiveButton("Close", null);

        detailDialog = detailDialogBuilder.create();
        detailDialog.show();
    }

    private void showLogsDialog(String referrerId, String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Report Logs for " + referrerId);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton("Close", null);
        dialogBuilder.create().show();
    }
}
