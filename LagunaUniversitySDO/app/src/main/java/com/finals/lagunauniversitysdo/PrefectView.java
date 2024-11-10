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

        // Initialize Firebase Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

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

            // Fetch violations and update logs count
            fetchViolationsData(studentId);
            updateStudentLogsCount(studentId);  // This will update the logs count in logsTextView
        }

        // Set up logsTextView click listener to show violation status
        logsTextView.setOnClickListener(v -> showViolationStatusDialog());

        return view;
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

                                // Populate the TableLayout with the fetched data
                                populateViolationTable(violation, remarks, date, documentSnapshot);
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
    private void populateViolationTable(String violation, String remarks, String date, DocumentSnapshot documentSnapshot) {
        if (violation != null && !violation.isEmpty()) {
            // Create a new TableRow for each violation record
            TableRow row = new TableRow(getContext());

            // Create and add TextViews for violation, remarks, and date
            TextView violationTextView = new TextView(getContext());
            violationTextView.setText(violation);
            violationTextView.setClickable(true);
            violationTextView.setOnClickListener(v -> showViolationDetailsDialog(documentSnapshot));
            row.addView(violationTextView);

            TextView remarksTextView = new TextView(getContext());
            remarksTextView.setText(remarks);
            remarksTextView.setClickable(true);
            remarksTextView.setOnClickListener(v -> showViolationDetailsDialog(documentSnapshot));
            row.addView(remarksTextView);

            TextView dateTextView = new TextView(getContext());
            dateTextView.setText(date);
            dateTextView.setClickable(true);
            dateTextView.setOnClickListener(v -> showViolationDetailsDialog(documentSnapshot));
            row.addView(dateTextView);

            // Add the row to the TableLayout
            violationTable.addView(row);
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
    // Method to show the dialog with all details of the selected violation
    private void showViolationDetailsDialog(DocumentSnapshot documentSnapshot) {
        String violation = documentSnapshot.getString("violation");
        String remarks = documentSnapshot.getString("remarks");
        String date = documentSnapshot.getString("date");

        String fullDetails = "Violation: " + violation + "\nRemarks: " + remarks + "\nDate: " + date;

        // Create and show an AlertDialog with the violation details
        new AlertDialog.Builder(getContext())
                .setTitle("Violation Details")
                .setMessage(fullDetails)
                .setPositiveButton("Settled", (dialog, which) -> {
                    // Show sanction input dialog after setting status to "Settled"
                    DocumentReference violationRef = documentSnapshot.getReference();
                    showSanctionInputDialog(violationRef);

                    // Update the violation status to "Settled"
                    updateViolationStatus(documentSnapshot, "Settled");
                    dialog.dismiss();
                })
                .setNeutralButton("Refer to Guidance", (dialog, which) -> {
                    updateViolationStatus(documentSnapshot, "Referred to Guidance");
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
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

                        violationRef.get().addOnSuccessListener(documentSnapshot -> {
                            int logsCount = documentSnapshot.getLong("logs") != null ? documentSnapshot.getLong("logs").intValue() : 0;
                            update.put("logs", logsCount + 1);  // Increment logs count

                            violationRef.update(update)
                                    .addOnSuccessListener(aVoid -> {
                                        // Immediately set the status to "Settled" and remove the UI element
                                        updateViolationStatus(documentSnapshot, "Settled");
                                        Toast.makeText(getContext(), "Sanction added: " + sanctionText, Toast.LENGTH_SHORT).show();

                                        // Update logs count display
                                        updateStudentLogsCount(violationRef.getParent().getParent().getId());
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

                            // Create a container for each violation
                            LinearLayout violationContainer = new LinearLayout(getContext());
                            violationContainer.setOrientation(LinearLayout.VERTICAL);
                            violationContainer.setPadding(8, 8, 8, 8);
                            violationContainer.setBackgroundResource(android.R.color.darker_gray);
                            violationContainer.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                            // Add violation details to the container
                            TextView violationTextView = new TextView(getContext());
                            violationTextView.setText("Violation: " + violation + "\nRemarks: " + remarks + "\nDate: " + date);
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

                            // Edit Button
                            Button editButton = new Button(getContext());
                            editButton.setText("Edit");
                            editButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                            editButton.setOnClickListener(v -> {
                                showEditViolationDialog(documentSnapshot);
                            });

                            // Refer to Guidance Button
                            Button referButton = new Button(getContext());
                            referButton.setText("Refer to Guidance");
                            referButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                            referButton.setOnClickListener(v -> {
                                referViolationToGuidance(documentSnapshot);
                            });

                            // Unsettle Button
                            Button unsettleButton = new Button(getContext());
                            unsettleButton.setText("Unsettle");
                            unsettleButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                            unsettleButton.setOnClickListener(v -> {
                                unsettleViolation(documentSnapshot);
                            });

                            // Add buttons to the layout
                            buttonsLayout.addView(editButton);
                            buttonsLayout.addView(referButton);
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

    private void referViolationToGuidance(DocumentSnapshot documentSnapshot) {
        DocumentReference docRef = documentSnapshot.getReference();
        Map<String, Object> updates = new HashMap<>();
        updates.put("violation_status", "Referred to Guidance");

        docRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Violation referred to guidance.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error referring to guidance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Violation is now unsettled. Logs reset to 0.", Toast.LENGTH_SHORT).show();

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

        // Create input fields for violation, date, and sanction
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Create Spinner for violation selection
        Spinner violationSpinner = new Spinner(getContext());

        // Fetch the violation types from Firestore and set up the spinner
        fetchViolationTypes(violationSpinner, doc);

        // EditText for date (non-editable)
        EditText dateInput = new EditText(getContext());
        dateInput.setHint("Date");
        dateInput.setText(doc.getString("date"));
        dateInput.setFocusable(false);
        dateInput.setClickable(false);
        dateInput.setCursorVisible(false);  // Optional: hides the cursor
        dateInput.setBackgroundResource(android.R.color.transparent);
        layout.addView(dateInput);

        // EditText for sanction
        EditText sanctionInput = new EditText(getContext());
        sanctionInput.setHint("Sanction");
        sanctionInput.setText(doc.getString("sanction"));
        layout.addView(sanctionInput);

        // Add the spinner to the layout
        layout.addView(violationSpinner);

        // Dynamically determine the referrer type and name
        String referrerType = "";
        String referrerName = "";

        if (doc.contains("personnel_referrer")) {
            referrerType = "Personnel";
            referrerName = doc.getString("personnel_referrer");
        } else if (doc.contains("student_referrer")) {
            referrerType = "Student";
            referrerName = doc.getString("student_referrer");
        } else if (doc.contains("prefect_referrer")) {
            referrerType = "Prefect";
            referrerName = doc.getString("prefect_referrer");
        }

        // Add a TextView to display the referrer information
        TextView referrerTextView = new TextView(getContext());
        referrerTextView.setText("Referrer Type: " + referrerType + "\n" + "Referrer Name: " + referrerName);
        referrerTextView.setTextSize(16); // Set text size to match the date input
        referrerTextView.setPadding(16, 16, 16, 16); // Same padding as the date input
        referrerTextView.setBackgroundResource(android.R.color.transparent); // Same background for consistency
        layout.addView(referrerTextView);

        builder.setView(layout);

        // Set the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Get the selected violation from the Spinner
            String newViolation = violationSpinner.getSelectedItem().toString();
            String newDate = dateInput.getText().toString();
            String newSanction = sanctionInput.getText().toString();

            // Update Firestore with the new values
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("violation", newViolation);
            updatedData.put("date", newDate);
            updatedData.put("sanction", newSanction.isEmpty() ? null : newSanction);

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
