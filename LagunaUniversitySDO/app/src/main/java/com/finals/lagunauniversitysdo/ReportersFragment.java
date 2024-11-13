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
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import android.content.Intent;
import android.widget.Toast;
import java.util.HashSet;
import java.util.Set;

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

        // Fetch data only from the "students" collection and the "accepted_status" subcollection
        fetchFromAcceptedStatus("students", "accepted_status", "Student", referrerMap);
    }

    private void fetchFromAcceptedStatus(String collectionName, String subCollectionName, String referrerType, Map<String, String> referrerMap) {
        db.collection(collectionName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String studentId = document.getId(); // Use student document ID to access subcollection

                        // Fetch from the "accepted_status" subcollection for each student document
                        db.collection(collectionName)
                                .document(studentId)
                                .collection(subCollectionName)
                                .whereEqualTo("status", "accepted") // Only fetch documents with status "accepted"
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                            for (QueryDocumentSnapshot statusDoc : task.getResult()) {
                                                String referrerId = statusDoc.getString("referrer_id"); // Get referrer ID

                                                if (referrerId != null && !referrerMap.containsKey(referrerId)) {
                                                    referrerMap.put(referrerId, referrerType); // Store type with referrer ID
                                                }
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
        // Clear existing views except for the title
        reportersContainer.removeViews(1, reportersContainer.getChildCount() - 1);

        // Create a TextView for the "No data" message
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
                    // Fetch logs for the selected referrerId
                    viewLogs(referrerId);
                }
            });

            // Add the inflated referrer item view to the container
            reportersContainer.addView(referrerItemView);
        }
    }

    private void viewLogs(String referrerId) {
        String collectionName = "students";  // Collection where student data is stored
        String subCollectionName = "accepted_status";  // Subcollection for student logs

        // Create a HashMap to store logs keyed by studentId
        HashMap<String, String> logMap = new HashMap<>();
        // Create a Set to track student names that have already been processed (to avoid duplicates)
        Set<String> processedStudents = new HashSet<>();

        // Query Firestore for students
        db.collection(collectionName)
                .get()  // Fetch the top-level collection (students)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot studentDoc : task.getResult()) {
                                String studentId = studentDoc.getId();  // Fetch studentId for each student

                                // Fetch the 'year' and 'block' fields from the student document
                                String studentYear = studentDoc.getString("year");
                                String studentBlock = studentDoc.getString("block");

                                // Query the "accepted_status" subcollection for each student
                                db.collection(collectionName)
                                        .document(studentId)
                                        .collection(subCollectionName)
                                        .whereEqualTo("referrer_id", referrerId)  // Filter logs where referrer_id matches
                                        .whereEqualTo("status", "accepted")  // Only fetch accepted status logs
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                    for (DocumentSnapshot document : task.getResult()) {
                                                        // Extract the relevant fields from the document
                                                        String studentName = document.getString("student_name");
                                                        String violation = document.getString("violation");
                                                        String date = document.getString("date");  // Ensure 'date' field is included
                                                        String remarks = document.getString("remarks");

                                                        // Create the log message for each student with date, violation, etc.
                                                        String logMessage = "Student: " + studentName + "\n" +
                                                                "Violation: " + violation + "\n" +
                                                                "Date: " + date + "\n" +
                                                                "Remarks: " + remarks + "\n" +
                                                                "Year: " + studentYear + "\n" +
                                                                "Block: " + studentBlock;

                                                        // Add this log message to the HashMap, keyed by studentId
                                                        logMap.put(studentId, logMessage);

                                                        // Add the studentName to the Set to prevent further processing
                                                        processedStudents.add(studentName);
                                                    }

                                                    // Once all the logs are gathered, create and show the dialog
                                                    showLogsInDialog(logMap);
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Log the case where no students are found
                            Log.d("viewLogs", "No students found for referrer: " + referrerId);
                        }
                    }
                });
    }


    // Method to display logs in a dialog
    private void showLogsInDialog(HashMap<String, String> logMap) {
        // Check if a dialog is already showing and dismiss it before creating a new one
        if (detailDialog != null && detailDialog.isShowing()) {
            detailDialog.dismiss();
        }

        AlertDialog.Builder detailDialogBuilder = new AlertDialog.Builder(getContext());
        detailDialogBuilder.setTitle("Log Details");

        LinearLayout logContainer = new LinearLayout(getContext());
        logContainer.setOrientation(LinearLayout.VERTICAL);
        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(logContainer);

        boolean hasAcceptedLogs = false;

        // Iterate through the HashMap to get each student's logs
        for (Map.Entry<String, String> entry : logMap.entrySet()) {
            String studentId = entry.getKey();
            String logMessage = entry.getValue();

            // Create a TextView for the log message
            TextView logSummaryTextView = new TextView(getContext());
            logSummaryTextView.setText(logMessage);
            logContainer.addView(logSummaryTextView);

            // Create a button to view the violation details
            Button viewViolationButton = new Button(getContext());
            viewViolationButton.setText("View Details");
            viewViolationButton.setPadding(16, 8, 16, 8);

            // Set up the click listener for the button
            viewViolationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Dynamically open a detailed view for this student's violation
                    showStudentViolationDetails(studentId);

                    // Dismiss the dialog after opening the violation details
                    if (detailDialog != null && detailDialog.isShowing()) {
                        detailDialog.dismiss();
                    }
                }
            });

            logContainer.addView(viewViolationButton);
            hasAcceptedLogs = true;  // Mark that we have accepted logs
        }

        if (!hasAcceptedLogs) {
            Log.d("viewLogs", "No accepted logs found for referrer.");
        } else {
            detailDialogBuilder.setView(scrollView);
            detailDialogBuilder.setPositiveButton("Close", null);
            detailDialog = detailDialogBuilder.create();
            detailDialog.show();
        }
    }

    // Method to handle opening the detailed view for student violation
    private void showStudentViolationDetails(String studentId) {
        // Fetch student details and violation data for the provided studentId
        db.collection("students")
                .document(studentId)
                .collection("accepted_status")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0); // Get the first document
                            String studentName = document.getString("student_name");
                            String violation = document.getString("violation");
                            String remarks = document.getString("remarks");
                            String date = document.getString("date");
                            String studentProgram = document.getString("program");
                            String studentContact = document.getString("contact");
                            String studentYear = document.getString("year");
                            String block = document.getString("block");

                            // Create a fragment to show the violation details
                            ReporterView reporterViewFragment = new ReporterView();
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

                            reporterViewFragment.setArguments(args);

                            // Replace the current fragment with ReporterView fragment
                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, reporterViewFragment);
                            transaction.addToBackStack(null); // Optional: adds to back stack
                            transaction.commit();
                        }
                    }
                });
    }



}
