package com.finals.lagunauniversitysdo;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.QueryDocumentSnapshot;


public class PrefectView extends Fragment {

    private TextView studentNameTextView, studentIdTextView, studentProgramTextView, studentContactTextView, studentYearTextView, studentBlockTextView;
    private TableLayout violationTable;
    private Map<String, TableRow> violationRows = new HashMap<>();

    private TextView logsTextView;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Static method to create a new instance of PrefectView fragment
    public static PrefectView newInstance(String studentId, String name, String program, String contact, String year, String block,
                                          String violations, String remarks, String date) {
        PrefectView fragment = new PrefectView();
        Bundle args = new Bundle();

        // Add all the parameters to the arguments
        args.putString("student_id", studentId);
        args.putString("student_name", name);
        args.putString("student_program", program);
        args.putString("student_contact", contact);
        args.putString("student_year", year);
        args.putString("student_block", block);
        args.putString("violations", violations);
        args.putString("remarks", remarks);
        args.putString("date", date);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.prefect_view, container, false);

        // Initialize UI elements
        studentNameTextView = view.findViewById(R.id.studentNameTextView);
        studentIdTextView = view.findViewById(R.id.studentIDTextView);
        studentProgramTextView = view.findViewById(R.id.studentProgramTextView);
        studentContactTextView = view.findViewById(R.id.studentContactTextView);
        studentYearTextView = view.findViewById(R.id.studentYearTextView);
        studentBlockTextView = view.findViewById(R.id.studentBlockTextView);
        violationTable = view.findViewById(R.id.violationTable);
        logsTextView = view.findViewById(R.id.logsTextView);  // Assuming the logs TextView has this ID

        // Retrieve arguments passed to the fragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            String studentName = arguments.getString("student_name");
            String studentId = arguments.getString("student_id");
            String studentProgram = arguments.getString("student_program");
            String studentContact = arguments.getString("student_contact");
            String studentYear = arguments.getString("student_year");
            String studentBlock = arguments.getString("student_block");

            // Set the received data to TextViews
            studentNameTextView.setText("Name: " + studentName);
            studentIdTextView.setText("ID: " + studentId);
            studentProgramTextView.setText("Program: " + studentProgram);
            studentContactTextView.setText("Contact No: " + studentContact);
            studentYearTextView.setText("Year: " + studentYear);
            studentBlockTextView.setText("Block: " + studentBlock);

            // Fetch the student details (year, block, contact) using studentId from Firestore
            fetchStudentDetails(studentId);

            // Fetch violations and update logs count
            fetchViolationsData(studentId);
            updateStudentLogsCount(studentId);  // This will update the logs count in logsTextView
        }

        // Set up logsTextView click listener to show violation status
        logsTextView.setOnClickListener(v -> showViolationStatusDialog());

        return view;
    }

    private void fetchStudentDetails(String studentId) {
        // Fetching the student details from Firestore using the studentId
        firestore.collection("students").document(studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot studentDoc = task.getResult();
                        if (studentDoc.exists()) {
                            // Retrieve student details
                            String studentYear = studentDoc.getString("year");
                            String studentBlock = studentDoc.getString("block");
                            String studentContact = String.valueOf(studentDoc.get("contacts"));  // Ensure contact is a string

                            // Update the UI with the fetched details
                            studentYearTextView.setText("Year: " + studentYear);
                            studentBlockTextView.setText("Block: " + studentBlock);
                            studentContactTextView.setText("Contact No: " + studentContact);
                        }
                    } else {
                        Log.d("PrefectView", "Error getting student details: ", task.getException());
                    }
                });
    }


    // Method to fetch violation, remarks, and date data from Firestore based on studentId
    private void fetchViolationsData(String studentId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("students") // The students collection
                .document(studentId) // The student document using the studentId
                .collection("accepted_status") // The 'accepted_status' subcollection
                .get() // Fetch all documents in the subcollection
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check if any documents are found
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String violationStatus = documentSnapshot.getString("violation_status");

                            // Only display rows for violations that are not "Settled"
                            if (violationStatus == null || !violationStatus.equals("Settled")) {
                                String violation = documentSnapshot.getString("violation");
                                String remarks = documentSnapshot.getString("remarks");
                                String date = documentSnapshot.getString("date");
                                String location = documentSnapshot.getString("location");
                                String term = documentSnapshot.getString("term");
                                String offense = documentSnapshot.getString("offense");
                                // Populate the TableLayout with the fetched data
                                populateViolationTable(violation, remarks, date, location, term, offense, documentSnapshot);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "No violation records found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to populate the TableLayout or other UI elements with violation data
    private void populateViolationTable(String violation, String remarks, String date, String location, String term, String offense, DocumentSnapshot documentSnapshot) {
        // Initialize the iteration counter outside the loop
        int iterationCounter = 0;

        if (violation != null && !violation.isEmpty()) {
            // Create a new TableRow for each violation record
            TableRow row = new TableRow(getContext());
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            // Create TextViews for basic fields to display in the table
            TextView numberText = new TextView(getContext());
            if (iterationCounter == 0) {
                numberText.setText(String.valueOf(violationTable.getChildCount()));  // First iteration: -1
            } else {
                numberText.setText(String.valueOf(violationTable.getChildCount() - 2));  // Subsequent iterations: -2
                iterationCounter++;
            }
            numberText.setPadding(16, 16, 16, 16);
            row.addView(numberText);

            TextView violationTextView = new TextView(getContext());
            violationTextView.setText(violation);
            violationTextView.setPadding(8, 8, 8, 8);
            row.addView(violationTextView);

            TextView offenseTextView = new TextView(getContext());
            offenseTextView.setText(offense);
            offenseTextView.setPadding(8, 8, 8, 8);
            row.addView(offenseTextView);

            TextView dateTextView = new TextView(getContext());
            dateTextView.setText(date);
            row.addView(dateTextView);

            // Add the row to the TableLayout
            violationTable.addView(row);

            // Create an additional row for the details (hidden by default)
            TableRow detailsRow = new TableRow(getContext());
            detailsRow.setVisibility(View.GONE); // Initially hidden
            detailsRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            detailsRow.setBackgroundColor(Color.parseColor("#ECECEC"));

            // Details TextView to show reporter, location, semester, etc.
            TextView numTextView = new TextView(getContext());
            String numText = " ";
            numTextView.setText(numText);
            numTextView.setPadding(16, 16, 16, 16);
            detailsRow.addView(numTextView);

            // Details TextView to show reporter, location, semester, etc.
            TextView LocationTextView = new TextView(getContext());
            String locationText = "Location: " + location;
            LocationTextView.setText(locationText);
            LocationTextView.setPadding(16, 16, 16, 16);
            detailsRow.addView(LocationTextView);

            // Details TextView to show reporter, location, semester, etc.
            TextView SemesterTextView = new TextView(getContext());
            String semText = "Semester: " + term;
            SemesterTextView.setText(semText);
            SemesterTextView.setPadding(16, 16, 16, 16);
            detailsRow.addView(SemesterTextView);

            TextView RemarksTextView = new TextView(getContext());
            String remarksText = "Remarks: " + remarks;
            RemarksTextView.setText(remarksText);
            RemarksTextView.setPadding(16, 16, 16, 16);
            detailsRow.addView(RemarksTextView);

            // Create an additional row for the buttons (hidden by default)
            TableRow buttonRow = new TableRow(getContext());
            buttonRow.setVisibility(View.GONE); // Initially hidden
            buttonRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            buttonRow.setBackgroundColor(Color.parseColor("#ECECEC"));

            TextView numTextView2 = new TextView(getContext());
            String numText2 = " ";
            numTextView2.setText(numText2);
            numTextView2.setPadding(16, 16, 16, 16);
            buttonRow.addView(numTextView2);

            Button settledButton = new Button(getContext());
            settledButton.setText("Settled");
            settledButton.setTextSize(12);
            settledButton.setOnClickListener(v -> {
                // Show sanction input dialog after setting status to "Settled"
                DocumentReference violationRef = documentSnapshot.getReference();
                showSanctionInputDialog(violationRef); // Call method for input dialog
            });
            buttonRow.addView(settledButton);

            Button editButton = new Button(getContext());
            editButton.setText("Edit");
            editButton.setOnClickListener(v -> showEditViolationDialog(documentSnapshot));  // Correct reference here
            buttonRow.addView(editButton);

            // Add the details row to the table
            violationTable.addView(detailsRow);
            violationTable.addView(buttonRow);

            // Set an OnClickListener on the main row to toggle details visibility
            row.setOnClickListener(v -> {
                if (detailsRow.getVisibility() == View.GONE && buttonRow.getVisibility() == View.GONE) {
                    detailsRow.setVisibility(View.VISIBLE); // Expand to show details
                    buttonRow.setVisibility(View.VISIBLE); // Show buttons
                } else {
                    detailsRow.setVisibility(View.GONE); // Collapse to hide details
                    buttonRow.setVisibility(View.GONE); // Hide buttons
                }
            });

            iterationCounter++;
        }
    }



    // Method to update the logs count displayed on the logsTextView
    private void updateStudentLogsCount(String studentId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference violationsRef = firestore.collection("students")
                .document(studentId)
                .collection("accepted_status");

        violationsRef.get().addOnSuccessListener(querySnapshot -> {
            int totalLogsCount = 0;

            // Iterate over all violations and sum the logs count
            for (QueryDocumentSnapshot document : querySnapshot) {
                Long logs = document.getLong("logs");
                if (logs != null) {
                    totalLogsCount += logs;
                }
            }

            // Update the logEntryTextView with the new total logs count
            logsTextView.setText("Logs: " + totalLogsCount);  // Update the logs display immediately
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error fetching logs for student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void showSanctionInputDialog(DocumentReference violationRef) {
        EditText sanctionEditText = new EditText(getContext());
        sanctionEditText.setHint("Enter sanction");

        AlertDialog sanctionDialog = new AlertDialog.Builder(getContext())
                .setTitle("Enter Sanction")
                .setView(sanctionEditText)
                .setPositiveButton("OK", (dialog, which) -> {
                    String sanctionText = sanctionEditText.getText().toString().trim();

                    if (!sanctionText.isEmpty()) {
                        Map<String, Object> update = new HashMap<>();
                        update.put("sanction", sanctionText);

                        // Get current logs count and then update the document
                        violationRef.get().addOnSuccessListener(documentSnapshot -> {
                            int logsCount = documentSnapshot.getLong("logs") != null ? documentSnapshot.getLong("logs").intValue() : 0;
                            update.put("logs", logsCount + 1);  // Increment logs count

                            // Update sanction and logs count in Firestore
                            violationRef.update(update)
                                    .addOnSuccessListener(aVoid -> {
                                        // After sanction and logs update, now update the violation status to "Settled"
                                        Map<String, Object> statusUpdate = new HashMap<>();
                                        statusUpdate.put("violation_status", "Settled");

                                        violationRef.update(statusUpdate)
                                                .addOnSuccessListener(aVoidStatus -> {
                                                    // UI feedback for success
                                                    Toast.makeText(getContext(), "Sanction added and status set to Settled", Toast.LENGTH_SHORT).show();

                                                    // Update logs count display
                                                    updateStudentLogsCount(violationRef.getParent().getParent().getId());
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getContext(), "Error updating violation status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Error updating sanction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error fetching logs field: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(getContext(), "Sanction cannot be empty.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        sanctionDialog.show();
    }



    // Method to show the violation status dialog when "Logs" is clicked

    private void showViolationStatusDialog() {
        // Retrieve the student ID to fetch the violation status
        String studentId = getArguments().getString("student_id");

        firestore.collection("students")
                .document(studentId)
                .collection("accepted_status")
                .whereEqualTo("violation_status", "Settled") // Fetch only the "Settled" status
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Create a layout to hold each violation with buttons
                        LinearLayout violationsLayout = new LinearLayout(getContext());
                        violationsLayout.setOrientation(LinearLayout.VERTICAL);
                        violationsLayout.setPadding(16, 16, 16, 16);

                        // Loop through the documents and add each violation to the layout
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Prepare violation details
                            String violation = documentSnapshot.getString("violation");
                            String remarks = documentSnapshot.getString("remarks");
                            String date = documentSnapshot.getString("date");
                            String offense = documentSnapshot.getString("offense");

                            // Create a container for each violation
                            LinearLayout violationContainer = new LinearLayout(getContext());
                            violationContainer.setOrientation(LinearLayout.VERTICAL);
                            violationContainer.setPadding(8, 8, 8, 8);
                            violationContainer.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                            // Add violation details to the container
                            TextView violationTextView = new TextView(getContext());
                            violationTextView.setText("Violation: " + violation + "\n Offense Type: "+ offense + "\nRemarks: " + remarks + "\nDate: " + date);
                            violationTextView.setPadding(8, 8, 8, 8);
                            violationContainer.addView(violationTextView);

                            // Dynamically determine the referrer type and name
                            String referrerType = "";
                            String referrerName = "";
                            if (documentSnapshot.contains("personnel_referrer")) {
                                referrerType = "Personnel";
                                referrerName = documentSnapshot.getString("personnel_referrer");
                            } else if (documentSnapshot.contains("student_referrer")) {
                                referrerType = "Student";
                                referrerName = documentSnapshot.getString("student_referrer");
                            } else if (documentSnapshot.contains("prefect_referrer")) {
                                referrerType = "Prefect";
                                referrerName = documentSnapshot.getString("prefect_referrer");
                            }

                            // Add referrer info if exists
                            if (!referrerName.isEmpty()) {
                                TextView referrerTextView = new TextView(getContext());
                                referrerTextView.setText("Referrer Type: " + referrerType + "\nReferrer Name: " + referrerName);
                                violationContainer.addView(referrerTextView);
                            }

                            // Create action buttons for each violation
                            LinearLayout buttonsLayout = new LinearLayout(getContext());
                            buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
                            buttonsLayout.setWeightSum(3);  // Distribute buttons equally



                            // Unsettle Button
                            Button unsettleButton = new Button(getContext());
                            unsettleButton.setText("Unsettle");
                            unsettleButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                            unsettleButton.setOnClickListener(v -> {
                                unsettleViolation(documentSnapshot);
                            });


                            buttonsLayout.addView(unsettleButton);

                            // Add the buttons layout to the violation container
                            violationContainer.addView(buttonsLayout);

                            // Add the violation container to the main layout
                            violationsLayout.addView(violationContainer);
                        }

                        // Show the dialog with the violations and their actions
                        new AlertDialog.Builder(getContext())
                                .setTitle("Settled Violations")
                                .setView(violationsLayout) // Add the layout with all violations
                                .setNegativeButton("Close", null)
                                .show();
                    } else {
                        Toast.makeText(getContext(), "No settled violations found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching settled violations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void unsettleViolation(DocumentSnapshot documentSnapshot) {
        DocumentReference docRef = documentSnapshot.getReference();

        // Prepare the updates for the violation
        Map<String, Object> updates = new HashMap<>();
        updates.put("violation_status", "Unsettled"); // Set the status to "Unsettled"
        updates.put("logs", 0);  // Reset the logs count to 0

        // Update the document with the new violation status and reset logs to 0
        docRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Violation is now unsettled", Toast.LENGTH_SHORT).show();

                    // After updating the status, you can also add logic here to refresh the UI
                    // For example, updating the table with the "Unsettled" violations or refreshing the view
                    updateStudentLogsCount(documentSnapshot.getReference().getParent().getParent().getId());

                    // Optionally, re-add the violation to the table or list if you need it to show up again
                    // Example:
                   // This will re-query the violations and refresh the UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating violation status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void showEditViolationDialog(DocumentSnapshot doc) {
        // Create an AlertDialog to edit the violation details
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Violation");

        // Create input fields for date, reporter, location, violation, and remarks
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // EditText for date (non-editable)
        EditText dateInput = new EditText(getContext());
        dateInput.setHint("Date");
        dateInput.setText(doc.getString("date"));
        dateInput.setFocusable(false);
        dateInput.setClickable(false);
        dateInput.setCursorVisible(false);  // Optional: hides the cursor
        dateInput.setBackgroundResource(android.R.color.transparent);
        layout.addView(dateInput);

        // TextView for reporter name
        String reporterName = "";
        if (doc.contains("personnel_referrer")) {
            reporterName = doc.getString("personnel_referrer");
        } else if (doc.contains("student_referrer")) {
            reporterName = doc.getString("student_referrer");
        } else if (doc.contains("prefect_referrer")) {
            reporterName = doc.getString("prefect_referrer");
        }
        TextView reporterTextView = new TextView(getContext());
        reporterTextView.setText("Reporter: " + reporterName);
        reporterTextView.setTextSize(16);
        reporterTextView.setPadding(16, 16, 16, 16);
        layout.addView(reporterTextView);

        // EditText for location
        EditText locationInput = new EditText(getContext());
        locationInput.setHint("Location");
        locationInput.setText(doc.getString("location"));  // Set existing location
        layout.addView(locationInput);

        // Spinner for violation selection
        Spinner violationSpinner = new Spinner(getContext());
        fetchViolationTypes(violationSpinner, doc); // Fetch the violation types from Firestore
        layout.addView(violationSpinner);

        // EditText for remarks
        EditText remarksInput = new EditText(getContext());
        remarksInput.setHint("Remarks");
        remarksInput.setText(doc.getString("remarks"));  // Set existing remarks
        layout.addView(remarksInput);

        builder.setView(layout);

        // Set the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Get the selected violation and offense type
            String selectedViolation = ((CheckboxSpinnerAdapter) violationSpinner.getAdapter()).getSelectedViolation();
            String selectedType = ((CheckboxSpinnerAdapter) violationSpinner.getAdapter()).getSelectedType();

            String newDate = dateInput.getText().toString();
            String newLocation = locationInput.getText().toString();
            String newRemarks = remarksInput.getText().toString();

            // Validate inputs (location and remarks)
            String validPattern = "^[a-zA-Z0-9\\s]+$";
            if (!newLocation.matches(validPattern)) {
                Toast.makeText(getContext(), "Location contains invalid characters!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newRemarks.matches(validPattern)) {
                Toast.makeText(getContext(), "Remarks contain invalid characters!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update Firestore with the new values
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("violation", selectedType);
            updatedData.put("offense", selectedViolation);  // Store the selected offense type
            updatedData.put("date", newDate);
            updatedData.put("location", newLocation.isEmpty() ? null : newLocation);
            updatedData.put("remarks", newRemarks.isEmpty() ? null : newRemarks);

            doc.getReference().update(updatedData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Violation Updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error updating violation", Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void fetchViolationTypes(Spinner violationSpinner, DocumentSnapshot doc) {
        // Access the Firestore collection
        CollectionReference violationTypesRef = firestore.collection("violation_type");

        violationTypesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, List<String>> violationMap = new HashMap<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String violationName = document.getString("violation");
                    String type = document.getString("type");
                    if (violationName != null && type != null) {
                        if (!violationMap.containsKey(violationName)) {
                            violationMap.put(violationName, new ArrayList<>());
                        }
                        violationMap.get(violationName).add(type);
                    }
                }

                // Prepare the list for the spinner
                List<String> violationDisplayList = new ArrayList<>();
                violationDisplayList.add("Select a Violation"); // Add a prompt as the first entry
                for (Map.Entry<String, List<String>> entry : violationMap.entrySet()) {
                    String violationName = entry.getKey();
                    List<String> types = entry.getValue();

                    // Add the violation name to the display list
                    violationDisplayList.add(violationName);

                    // Add each type underneath the violation name
                    for (String type : types) {
                        violationDisplayList.add(" " + type); // Indent types for better visibility
                    }
                }

                // Set up the spinner with the combined list
                setupSpinner(violationSpinner, violationDisplayList);

                // Set the current violation in the spinner (if it matches the option)
                String currentViolation = doc.getString("violation");
                int violationIndex = violationDisplayList.indexOf(currentViolation);
                if (violationIndex >= 0) {
                    violationSpinner.setSelection(violationIndex);
                }

            } else {
                Log.w("FormActivity", "Error getting violation types.", task.getException());
                Toast.makeText(getContext(), "Failed to load violation types", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinner(Spinner spinner, List<String> items) {
        // Use your custom CheckboxSpinnerAdapter
        CheckboxSpinnerAdapter adapter = new CheckboxSpinnerAdapter(getContext(), items);
        spinner.setAdapter(adapter);
    }



    private void updateViolationStatus(DocumentSnapshot documentSnapshot, String status) {
        // Get student ID and violation document ID
        String studentId = documentSnapshot.getString("student_id");
        String violationId = documentSnapshot.getId();

        // Get reference to the violation document in Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference violationDocRef = firestore.collection("students")
                .document(studentId)
                .collection("accepted_status")
                .document(violationId);

        // Create a map to update the violation status
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("violation_status", status);

        // Update the violation status in Firestore
        violationDocRef.update(updateData)
                .addOnSuccessListener(aVoid -> {
                    // Fetch the updated document to check the status
                    violationDocRef.get()
                            .addOnSuccessListener(updatedDocSnapshot -> {
                                String updatedStatus = updatedDocSnapshot.getString("violation_status");

                                // If the status is "Settled", remove the row from the table
                                if ("Settled".equals(updatedStatus)) {
                                    removeViolationRow(violationId);
                                }

                                // Show a success toast
                                Toast.makeText(getContext(), "Status updated to: " + updatedStatus, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Handle error fetching the document after the update
                                Toast.makeText(getContext(), "Error fetching document after update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle error updating the status
                    Toast.makeText(getContext(), "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void removeViolationRow(String violationId) {
        // Get the row from the map using the violation ID
        TableRow row = violationRows.get(violationId);

        if (row != null) {
            // Remove the row from the TableLayout
            violationTable.removeView(row);

            // Remove the row from the map as well
            violationRows.remove(violationId);
        }
    }

}
