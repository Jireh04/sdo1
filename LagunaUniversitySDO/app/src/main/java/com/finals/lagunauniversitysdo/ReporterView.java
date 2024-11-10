package com.finals.lagunauniversitysdo;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import java.util.ArrayList;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import java.util.List;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;
import java.util.List;
import android.widget.ArrayAdapter;
import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;

import android.widget.Spinner;

import java.util.Map;
import java.util.HashMap;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import java.util.Map;
import java.util.HashMap;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReporterView extends Fragment {

    // Declare the TextViews for student details and remarks
    private TextView studentNameTextView;
    private TextView studentIdTextView;
    private TextView studentProgramTextView;
    private TextView studentContactTextView;
    private TextView studentYearTextView;
    private TextView studentBlockTextView;
    private TextView logEntryTextView;  // TextView for displaying log entry (if any)

    private String[] violationArray, remarksArray, dateArray;

    // Declare buttons
    private Button addViolationButton;
    private Button exportPdfButton;
    private FirebaseFirestore firestore;
    // TableLayout for displaying violations
    private TableLayout violationTable;

    private Button violationCountButton;

    // Static method to create a new instance of the fragment and pass arguments
    public static ReporterView newInstance(String studentId, String studentName, String studentProgram,
                                           String studentContact, String studentYear, String block,
                                           String violation, String referrerId, String remarks, String date) {
        ReporterView fragment = new ReporterView();
        Bundle args = new Bundle();
        args.putString("STUDENT_ID", studentId);
        args.putString("STUDENT_NAME", studentName);
        args.putString("STUDENT_PROGRAM", studentProgram);
        args.putString("STUDENT_CONTACT", studentContact);
        args.putString("STUDENT_YEAR", studentYear);
        args.putString("BLOCK", block);
        args.putString("VIOLATION", violation);
        args.putString("REFERRER_ID", referrerId);  // Added referrer_id
        args.putString("REMARKS", remarks);
        args.putString("DATE", date);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.prefect_view, container, false);
        firestore = FirebaseFirestore.getInstance();
        // Initialize the TextViews and Button
        studentNameTextView = view.findViewById(R.id.studentNameTextView);
        studentIdTextView = view.findViewById(R.id.studentIDTextView);
        studentProgramTextView = view.findViewById(R.id.studentProgramTextView);
        studentContactTextView = view.findViewById(R.id.studentContactTextView);
        studentYearTextView = view.findViewById(R.id.studentYearTextView);
        studentBlockTextView = view.findViewById(R.id.studentBlockTextView);
        logEntryTextView = view.findViewById(R.id.logsTextView);

        // Initialize the violation count button
        violationCountButton = view.findViewById(R.id.logsButton);

        // Initialize buttons
        addViolationButton = view.findViewById(R.id.addViolationButton);
        exportPdfButton = view.findViewById(R.id.exportPdfButton);
        violationTable = view.findViewById(R.id.violationTable);

        // Retrieve arguments passed to the fragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            String studentId = arguments.getString("STUDENT_ID");
            String studentName = arguments.getString("STUDENT_NAME");
            String studentProgram = arguments.getString("STUDENT_PROGRAM");
            String studentContact = arguments.getString("STUDENT_CONTACT");
            String studentYear = arguments.getString("STUDENT_YEAR");
            String studentBlock = arguments.getString("BLOCK");

            // Set the retrieved data to TextViews
            studentNameTextView.setText("Name: " + studentName);
            studentIdTextView.setText("ID: " + studentId);
            studentProgramTextView.setText("Program: " + studentProgram);
            studentContactTextView.setText("Contact No: " + studentContact);
            studentYearTextView.setText("Year: " + studentYear);
            studentBlockTextView.setText("Block: " + studentBlock);

            // Call method to populate the violation table and update the count
            populateViolationTableFromFirestore(studentId);
        }

        return view;
    }



    // Method to show export options for PDF
    private void showExportOptions() {
        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose an option");

        // Add options
        String[] options = {"Open PDF", "Download PDF"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Open PDF
                exportPdf(true); // true indicates to open the PDF
            } else if (which == 1) {
                // Download PDF
                exportPdf(false); // false indicates to download the PDF
            }
        });

        // Show the dialog
        builder.show();
    }

    // Modified exportPdf method to accept a boolean parameter
    private void exportPdf(boolean openPdf) {
        // Check if student data is valid
        if (studentNameTextView.getText().toString().isEmpty() || studentIdTextView.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Invalid data for PDF generation", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new PdfDocument
        PdfDocument pdfDocument = new PdfDocument();

        // Create a page description (A4 size - 595x842)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Get the Canvas object for drawing content
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(18);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Start drawing text with proper margins
        int leftMargin = 50;
        int topMargin = 100;
        int yPosition = topMargin;

        // Draw header with bold font and center alignment
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Student Violation Details", canvas.getWidth() / 2, yPosition, paint);
        yPosition += 40;

        // Draw a divider line below the header
        paint.setStrokeWidth(2);
        canvas.drawLine(leftMargin, yPosition, canvas.getWidth() - leftMargin, yPosition, paint);
        yPosition += 30;

        // Reset paint for student details (left-aligned)
        paint.setTextSize(16);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        // Draw student details with proper spacing
        canvas.drawText("Student " + studentNameTextView.getText().toString(), leftMargin, yPosition, paint);
        yPosition += 25;
        canvas.drawText("Student " + studentIdTextView.getText().toString(), leftMargin, yPosition, paint);
        yPosition += 25;
        canvas.drawText(studentProgramTextView.getText().toString(), leftMargin, yPosition, paint);
        yPosition += 25;
        canvas.drawText(studentContactTextView.getText().toString(), leftMargin, yPosition, paint);
        yPosition += 25;
        canvas.drawText(studentYearTextView.getText().toString(), leftMargin, yPosition, paint);
        yPosition += 25;
        canvas.drawText(studentBlockTextView.getText().toString(), leftMargin, yPosition, paint);
        yPosition += 40; // Extra space before violations section

        // Draw violations header
        paint.setTextSize(18);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Violations:", leftMargin, yPosition, paint);
        yPosition += 25;

        // Draw table header for violations
        paint.setTextSize(14);
        paint.setColor(Color.WHITE); // White text for the table header
        float tableStartX = leftMargin;
        float tableWidth = 495;
        float rowHeight = 40;
        float col1Width = 150;
        float col2Width = 345;

        // Draw table header background
        paint.setColor(Color.GRAY); // Gray background for the table header
        canvas.drawRect(tableStartX, yPosition, tableStartX + tableWidth, yPosition + rowHeight, paint);

        // Draw table header text
        paint.setColor(Color.WHITE); // White text for table header
        canvas.drawText("Violation Number", tableStartX + 10, yPosition + 30, paint);
        canvas.drawText("Description", tableStartX + col1Width + 10, yPosition + 30, paint);
        yPosition += rowHeight;

        // Reset text color for table content
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        // Draw violation details in table rows
        for (int i = 0; i < violationTable.getChildCount(); i++) {
            TableRow row = (TableRow) violationTable.getChildAt(i);
            TextView violationNumberTextView = (TextView) row.getChildAt(0);
            TextView violationDescriptionTextView = (TextView) row.getChildAt(1);

            // Alternate row background color for better readability
            if (i % 2 == 0) {
                paint.setColor(Color.LTGRAY); // Light gray background
            } else {
                paint.setColor(Color.WHITE);  // White background
            }
            canvas.drawRect(tableStartX, yPosition, tableStartX + tableWidth, yPosition + rowHeight, paint);

            // Draw text content
            paint.setColor(Color.BLACK); // Reset color for text
            canvas.drawText(violationNumberTextView.getText().toString(), tableStartX + 10, yPosition + 30, paint);
            canvas.drawText(violationDescriptionTextView.getText().toString(), tableStartX + col1Width + 10, yPosition + 30, paint);

            // Move to the next row
            yPosition += rowHeight;
        }

        // Draw final bottom line of the table
        paint.setStrokeWidth(2);
        canvas.drawLine(tableStartX, yPosition, tableStartX + tableWidth, yPosition, paint);

        // Finish the page
        pdfDocument.finishPage(page);

        // Save the PDF
        String filePath;
        if (openPdf) {
            File directory = getContext().getFilesDir();
            filePath = directory + "/" + studentNameTextView.getText().toString() + "_violations.pdf";
        } else {
            filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/" + studentNameTextView.getText().toString() + "_violations.pdf";
        }

        File file = new File(filePath);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            pdfDocument.writeTo(outputStream);
            outputStream.close();
            Toast.makeText(getContext(), openPdf ? "PDF generated and opened." : "PDF downloaded.", Toast.LENGTH_SHORT).show();
            if (openPdf) {
                openPdf(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }


    private void openPdf(File file) {
        // Create an Intent to open the PDF file
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Get the URI for the file using FileProvider
        Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file);

        // Set flags to allow read access to the file
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Set MIME type to ensure it's recognized as a PDF file
        intent.setDataAndType(uri, "application/pdf");

        // Launch the intent to open the PDF
        startActivity(intent);
    }


    private void viewPdf(File file) {
        // Create an intent to view the PDF
        Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Check if a PDF viewer is available
        Intent chooser = Intent.createChooser(intent, "Open PDF");
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(getContext(), "No PDF viewer found", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to populate the TableLayout with violation data
    private void populateViolationTableFromFirestore(String studentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference violationsRef = db.collection("students")
                .document(studentId)
                .collection("accepted_status");

        violationsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int settledViolationCount = 0;  // Variable to count Settled violations
                List<QueryDocumentSnapshot> settledViolations = new ArrayList<>();  // List to store settled violations

                // Clear the table before populating it
                violationTable.removeAllViews();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Fetch all details from the document
                    String violation = document.getString("violation");
                    String date = document.getString("date");
                    String location = document.getString("location");
                    String referrerId = document.getString("referrer_id");
                    String remarks = document.getString("remarks");
                    String status = document.getString("status");
                    String studentName = document.getString("student_name");
                    String studentProgram = document.getString("student_program");
                    String term = document.getString("term");
                    String userConcern = document.getString("user_concern");
                    String sanction = document.getString("sanction"); // Fetching the sanction field

                    // Dynamically determine the referrer type based on available field
                    String referrerType;
                    if (document.contains("personnel_referrer")) {
                        referrerType = document.getString("personnel_referrer");
                    } else if (document.contains("student_referrer")) {
                        referrerType = document.getString("student_referrer");
                    } else {
                        referrerType = document.getString("prefect_referrer");
                    }

                    // Get the violationId (document ID)
                    String violationId = document.getId();

                    // Check if the violation status is Settled
                    String violationStatus = document.getString("violation_status");
                    if ("Settled".equals(violationStatus)) {
                        settledViolationCount++;
                        settledViolations.add(document);  // Add the settled violation to the list
                        continue; // Skip adding settled violations to the table
                    }

                    // Create a new row for each non-settled violation
                    TableRow row = new TableRow(getContext());
                    row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    // Create TextViews for basic fields to display in the table
                    TextView violationTextView = new TextView(getContext());
                    violationTextView.setText(violation != null ? violation : "N/A");
                    violationTextView.setPadding(16, 16, 16, 16);

                    TextView dateTextView = new TextView(getContext());
                    dateTextView.setText(date != null ? date : "N/A");
                    dateTextView.setPadding(16, 16, 16, 16);

                    // Add these TextViews to the row
                    row.addView(violationTextView);
                    row.addView(dateTextView);

                    // Set an onClickListener to show details when clicked
                    row.setOnClickListener(v -> showViolationDetails(
                            studentId,  // Pass the studentId here
                            violation,
                            date,
                            location,
                            referrerType,
                            referrerId,
                            remarks,
                            status,
                            studentName,
                            studentProgram,
                            term,
                            userConcern,
                            sanction,
                            violationId
                    ));

                    violationTable.addView(row); // Add row to the table
                }

                // Display the count of only "Settled" violations
                String settledViolationsText = settledViolationCount + " Settled Violations";
                violationCountButton.setText(settledViolationsText);
                logEntryTextView.setText("Logs: " + settledViolationCount);  // Log the settled violation count

                // Set both buttons to perform the same action when clicked
                View.OnClickListener settledViolationsClickListener = v -> {
                    showSettledViolations(settledViolations);  // Show the settled violations
                    removeSettledViolationsFromTable(settledViolations);  // Remove them from the table
                };

                // Set the OnClickListener to both buttons
                violationCountButton.setOnClickListener(settledViolationsClickListener);
                logEntryTextView.setOnClickListener(settledViolationsClickListener);

            } else {
                Toast.makeText(getContext(), "Error getting violations: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showSettledViolations(List<QueryDocumentSnapshot> settledViolations) {
        // Create a ScrollView to hold the content if there are many violations
        ScrollView scrollView = new ScrollView(getContext());
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        scrollView.addView(layout);

        // Iterate over each settled violation to add it to the layout
        for (QueryDocumentSnapshot doc : settledViolations) {
            String violation = doc.getString("violation");
            String date = doc.getString("date");
            String sanction = doc.getString("sanction");
            String violationId = doc.getId();

            // Create a TextView for the violation details
            TextView violationDetails = new TextView(getContext());
            violationDetails.setText("Violation: " + violation + "\n" +
                    "Date: " + date + "\n" +
                    "Sanction: " + (sanction != null ? sanction : "N/A"));
            violationDetails.setPadding(0, 10, 0, 10);
            layout.addView(violationDetails);

            // Create a LinearLayout for the buttons
            LinearLayout buttonLayout = new LinearLayout(getContext());
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

            // "Edit" button to modify violation details
            Button editButton = new Button(getContext());
            editButton.setText("Edit");
            editButton.setOnClickListener(v -> showEditViolationDialog(doc));

            // "Unsettle" button to mark the violation as unsettled
            Button unsettleButton = new Button(getContext());
            unsettleButton.setText("Unsettle");
            unsettleButton.setOnClickListener(v -> unsettleViolation((DocumentSnapshot) doc));

            // "Refer to Guidance" button for any custom referral action
            Button referButton = new Button(getContext());
            referButton.setText("Refer to Guidance");
            referButton.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Referred to Guidance", Toast.LENGTH_SHORT).show();
                // Optional: Add custom logic here for referring to guidance
            });

            // Add buttons to the button layout and the layout to the main layout
            buttonLayout.addView(editButton);
            buttonLayout.addView(unsettleButton);
            buttonLayout.addView(referButton);
            layout.addView(buttonLayout);
        }

        // Show the dialog with the ScrollView as content
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Settled Violations")
                .setView(scrollView)  // Add the scrollable view to the dialog
                .setPositiveButton("OK", null)
                .show();
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


    // Show a dialog to edit violation details
    private void showEditViolationDialog(QueryDocumentSnapshot doc) {
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

    // Method to fetch violation types from Firestore and populate the spinner
    private void fetchViolationTypes(Spinner violationSpinner, QueryDocumentSnapshot doc) {
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

    // Updated setupSpinner method
    private void setupSpinner(Spinner spinner, List<String> items) {
        // Use your custom CheckboxSpinnerAdapter
        CheckboxSpinnerAdapter adapter = new CheckboxSpinnerAdapter(getContext(), items);
        spinner.setAdapter(adapter);
    }



    private void removeSettledViolationsFromTable(List<QueryDocumentSnapshot> settledViolations) {
        // Iterate through the list of settled violations and remove them from the table
        for (QueryDocumentSnapshot settledDoc : settledViolations) {
            String violationId = settledDoc.getId();

            // Find the table row corresponding to this violation and remove it
            for (int i = 0; i < violationTable.getChildCount(); i++) {
                TableRow row = (TableRow) violationTable.getChildAt(i);
                TextView violationTextView = (TextView) row.getChildAt(0);  // Assuming the first child is the violation TextView

                if (violationTextView.getText().toString().equals(settledDoc.getString("violation"))) {
                    violationTable.removeViewAt(i);  // Remove the row
                    break;
                }
            }
        }
    }

    private void showViolationDetails(String studentId, String violation, String date, String location, String referrerType,
                                      String referrerId, String remarks, String status, String studentName,
                                      String studentProgram, String term, String userConcern, String sanction, String violationId) {

        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Violation Details");

        // Construct the message with all details, including the new Sanction field
        String message =
                "\nLocation: " + (location != null ? location : "N/A") +
                        "\nReferrer: " + (referrerType != null ? referrerType : "N/A") +
                        "\nRemarks: " + (remarks != null ? remarks : "N/A") +
                        "\nTerm: " + (term != null ? term : "N/A") +
                        "\nSanction: " + (sanction != null ? sanction : "N/A"); // New Sanction field

        builder.setMessage(message);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference violationRef = db.collection("students")
                .document(studentId)  // Use the 'studentId' passed to the method
                .collection("accepted_status")
                .document(violationId);  // You now have 'violationId' from the document ID

        // Fetch the current status of the violation
        violationRef.get().addOnSuccessListener(documentSnapshot -> {
            String currentStatus = documentSnapshot.getString("violation_status");

            // Determine the appropriate button label based on current 'violation_status' value
            String buttonText = "Settled".equals(currentStatus) ? "Unsettled" : "Settled";

            // Set the button text dynamically based on current status
            builder.setPositiveButton(buttonText, (dialog, which) -> {
                // Toggle the 'violation_status' field between "Settled" and "Unsettled"
                String newStatus = "Settled".equals(currentStatus) ? "Unsettled" : "Settled";

                if ("Settled".equals(newStatus)) {
                    // If Settled is clicked, show dialog to add Sanction
                    showSanctionInputDialog(violationRef);
                } else {
                    // Update Firestore with the new status without sanction
                    updateViolationStatus(violationRef, newStatus);
                }
            });

            builder.setNeutralButton("Refer to Guidance", (dialog, which) -> {
                // Optional action for referring to guidance
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }

    private void updateViolationStatus(DocumentReference violationRef, String newStatus) {
        Map<String, Object> update = new HashMap<>();
        update.put("violation_status", newStatus);

        violationRef.update(update)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Violation marked as " + newStatus, Toast.LENGTH_SHORT).show();

                    // If the new status is "Settled," remove the violation from the UI
                    if ("Settled".equals(newStatus)) {
                        removeRowFromTableByViolationId(violationRef.getId());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating violation status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Helper method to remove a row from the table by violation ID
    private void removeRowFromTableByViolationId(String violationId) {
        for (int i = 0; i < violationTable.getChildCount(); i++) {
            TableRow row = (TableRow) violationTable.getChildAt(i);
            TextView violationIdTextView = (TextView) row.getTag(); // Assuming you set the row tag as the violation ID

            if (violationIdTextView != null && violationIdTextView.getText().toString().equals(violationId)) {
                violationTable.removeViewAt(i);  // Remove the row immediately
                break;
            }
        }
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
                                        updateViolationStatus(violationRef, "Settled");
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


    private void updateStudentLogsCount(String studentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference violationsRef = db.collection("students")
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
            logEntryTextView.setText("Logs: " + totalLogsCount);  // Update the logs display immediately
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error fetching logs for student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

}

