package com.finals.lagunauniversitysdo;

import static com.finals.lagunauniversitysdo.ReferralFormGenerator.generateReferralForm;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PrefectView extends Fragment {

    // Declare the TextViews for student details
    private TextView studentNameTextView;
    private TextView studentIdTextView;
    private TextView studentProgramTextView;
    private TextView studentContactTextView;
    private TextView studentYearTextView;
    private TextView studentBlockTextView;
    private TextView logEntryTextView;  // TextView for displaying log entry (if any)

    // Declare buttons
    private Button addViolationButton;
    private Button exportPdfButton;

    // TableLayout for displaying violations
    private TableLayout violationTable;

    // New method to create an instance of the fragment with arguments
    public static PrefectView newInstance(String studentId, String studentName, String studentProgram,
                                          String studentContact, String studentYear, String studentBlock,
                                          String violations) {
        PrefectView fragment = new PrefectView();
        Bundle args = new Bundle();
        args.putString("STUDENT_ID", studentId);
        args.putString("STUDENT_NAME", studentName);
        args.putString("STUDENT_PROGRAM", studentProgram);
        args.putString("STUDENT_CONTACT", studentContact);
        args.putString("STUDENT_YEAR", studentYear);
        args.putString("STUDENT_BLOCK", studentBlock);
        args.putString("VIOLATIONS", violations);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.prefect_view, container, false);

        // Initialize the TextViews from the layout
        studentNameTextView = view.findViewById(R.id.studentNameTextView);
        studentIdTextView = view.findViewById(R.id.studentIDTextView);
        studentProgramTextView = view.findViewById(R.id.studentProgramTextView);
        studentContactTextView = view.findViewById(R.id.studentContactTextView);
        studentYearTextView = view.findViewById(R.id.studentYearTextView);
        studentBlockTextView = view.findViewById(R.id.studentBlockTextView);
        logEntryTextView = view.findViewById(R.id.logsTextView);

        // Initialize buttons
        addViolationButton = view.findViewById(R.id.addViolationButton);
        exportPdfButton = view.findViewById(R.id.exportPdfButton);

        // Initialize the TableLayout for violations
        violationTable = view.findViewById(R.id.violationTable);

        // Retrieve student details from the arguments
        if (getArguments() != null) {
            String studentId = getArguments().getString("STUDENT_ID");
            String studentName = getArguments().getString("STUDENT_NAME");
            String studentProgram = getArguments().getString("STUDENT_PROGRAM");
            String studentContact = getArguments().getString("STUDENT_CONTACT");
            String studentYear = getArguments().getString("STUDENT_YEAR");
            String studentBlock = getArguments().getString("STUDENT_BLOCK");
            String violations = getArguments().getString("VIOLATIONS"); // Retrieve the violations string
            String location = getArguments().getString("LOCATION");

            // Set student details to TextViews
            if (studentId != null && !studentId.isEmpty()) {
                studentIdTextView.setText("ID: " + studentId);
            }
            if (studentName != null && !studentName.isEmpty()) {
                studentNameTextView.setText("Name: " + studentName);
            }
            if (studentProgram != null && !studentProgram.isEmpty()) {
                studentProgramTextView.setText("Program: " + studentProgram);
            }
            if (studentContact != null && !studentContact.isEmpty()) {
                studentContactTextView.setText("Contact No: " + studentContact);
            }
            if (studentYear != null && !studentYear.isEmpty()) {
                studentYearTextView.setText("Year: " + studentYear);
            }
            if (studentBlock != null && !studentBlock.isEmpty()) {
                studentBlockTextView.setText("Block: " + studentBlock);
            }

            // Split violations into an array and populate the table if available
            if (violations != null && !violations.isEmpty()) {
                String[] violationsArray = violations.split("\n"); // Split by newline
                populateViolationTable(violationsArray); // Populate table with violations
            } else {
                logEntryTextView.setText("No violations found."); // Handle no violations case
            }
        }

        // Handle the Add Violation button click
        addViolationButton.setOnClickListener(v -> {
            // Retrieve student details from arguments
            if (getArguments() != null) {
                String studentId = getArguments().getString("STUDENT_ID");
                String studentName = getArguments().getString("STUDENT_NAME");
                // Show the Add Violator dialog, passing the student details
                showAddViolatorDialog(studentId, studentName); // Pass studentId and studentName
            }
        });


        // Handle the Export as PDF button click
        exportPdfButton.setOnClickListener(v -> {
            showExportOptions();
        });

        return view;  // Return the inflated view
    }

    public void showAddViolatorDialog(String studId, String name) {
        // Inflate the custom dialog layout using getContext() instead of this
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.add_violator, null);

        // Find the UI elements in the dialog
        EditText dateTimeEditText = dialogView.findViewById(R.id.dateTimeEditText);
        EditText reporterEditText = dialogView.findViewById(R.id.reporterEditText);
        EditText locationEditText = dialogView.findViewById(R.id.locationEditText);
        Spinner violationSpinner = dialogView.findViewById(R.id.violationSpinner);
        EditText remarksEditText = dialogView.findViewById(R.id.remarksEditText);
        Button submitButton = dialogView.findViewById(R.id.submitButton);

        // Set the current date and time automatically in the dateTimeEditText field
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Customize the format as needed
        String currentDateAndTime = sdf.format(calendar.getTime());
        dateTimeEditText.setText(currentDateAndTime);

        // Create a list of violations
        String[] violations = {"-select violation-", "Light Offense", "Serious Offense", "Major Offense"};

        // Set up an ArrayAdapter to populate the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, violations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        violationSpinner.setAdapter(adapter); // Set the adapter to the Spinner

        // Build the AlertDialog using getActivity() to get the context
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        builder.setTitle("Add Violator");

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Set a listener for the submit button
        submitButton.setOnClickListener(v -> {
            // Get the input values
            String dateTime = dateTimeEditText.getText().toString().trim();
            String reporter = reporterEditText.getText().toString().trim();
            String location = locationEditText.getText().toString().trim();
            String violation = violationSpinner.getSelectedItem().toString();
            String remarks = remarksEditText.getText().toString().trim();

            // Simple validation: check if required fields are empty
            if (dateTime.isEmpty() || reporter.isEmpty() || location.isEmpty() || violation.equals("-select violation-") || remarks.isEmpty() ) {
                Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            } else {
                // Prepare the data to be saved to Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Create a new violator entry with the specified fields
                Map<String, Object> violatorData = new HashMap<>();
                violatorData.put("date", dateTime);
                violatorData.put("prefect_referrer", reporter);
                violatorData.put("location", location);
                violatorData.put("violation", violation);
                violatorData.put("remarks", remarks);
                violatorData.put("student_id", studId);  // Add the studentId
                violatorData.put("student_name", name);  // Add the studentName

                // Add the data to the 'prefect_referral_history' collection
                db.collection("prefect_referral_history")  // Ensure this collection name is correct
                        .add(violatorData)  // Auto-generate a document ID
                        .addOnSuccessListener(documentReference -> {
                            // Show a success message
                            Toast.makeText(getContext(), "Violator Added!", Toast.LENGTH_SHORT).show();
                            // Close the dialog
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            // Show an error message if the operation fails
                            Toast.makeText(getContext(), "Error adding violator: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Add a cancel button to close the dialog
        builder.setNegativeButton("Cancel", (dialogInterface, which) -> dialog.dismiss());

        // Show the dialog
        dialog.show();
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

    private void exportPdf(boolean openPdf) {
        // Check if student data is valid
        String studentName = studentNameTextView.getText().toString();
        String studentId = studentIdTextView.getText().toString();
        String studentProgram = studentProgramTextView.getText().toString();
        String studentContact = studentContactTextView.getText().toString();
        String studentYear = studentYearTextView.getText().toString();
        String studentBlock = studentBlockTextView.getText().toString();
        String violations = logEntryTextView.getText().toString();
        String reporter = getArguments().getString("REPORTER");
        String date = getArguments().getString("DATE");
        String remarks = getArguments().getString("REMARKS");

        if (studentName.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid data for PDF generation", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call the ReferralFormGenerator to create the PDF with all necessary details
        ReferralFormGenerator.generateReferralForm(getContext(), studentName, studentId,
                studentProgram, studentContact, studentYear, studentBlock, violations, reporter, date, remarks);

        // Sanitize the student name to ensure it’s a valid file name
        String sanitizedStudentName = studentName.replaceAll("[\\\\/:*?\"<>|]", "_"); // Replace invalid characters

        // Define the file path for the generated PDF using the student's name
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/" + sanitizedStudentName + "_Referral_Form.pdf";
        File file = new File(filePath);


        // Check if the PDF was generated successfully before attempting to open it
        if (file.exists()) {
            if (openPdf) {
                openPdf(file); // Open the PDF if requested
            } else {
                Toast.makeText(getContext(), "PDF downloaded successfully.", Toast.LENGTH_SHORT).show();
            }
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

        // Check if there is an app that can handle the intent
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "No PDF viewer found", Toast.LENGTH_SHORT).show();
        }
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
    private void populateViolationTable(String[] violations) {
        // For each violation, add a new row to the table
        for (int i = 0; i < violations.length; i++) {
            // Create a new TableRow
            TableRow tableRow = new TableRow(getContext()); // Use getContext() in Fragment

            // Add the violation details to the row
            TextView violationNumberTextView = new TextView(getContext());
            violationNumberTextView.setText(String.valueOf(i + 1));  // Set violation number

            TextView violationDescriptionTextView = new TextView(getContext());
            violationDescriptionTextView.setText(violations[i]);  // Set the violation description

            // Add the TextViews to the TableRow
            tableRow.addView(violationNumberTextView);
            tableRow.addView(violationDescriptionTextView);

            // Add the TableRow to the TableLayout
            violationTable.addView(tableRow);
        }
    }
}
