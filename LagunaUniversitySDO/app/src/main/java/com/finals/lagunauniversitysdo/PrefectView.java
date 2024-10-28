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
