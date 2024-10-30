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

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

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

    // TableLayout for displaying violations
    private TableLayout violationTable;

    // Static method to create a new instance of the fragment and pass arguments
    public static ReporterView newInstance(String studentId, String studentName, String studentProgram,
                                           String studentContact, String studentYear, String block,
                                           String violation, String referrerName, String remarks, String date) {
        ReporterView fragment = new ReporterView();
        Bundle args = new Bundle();
        args.putString("STUDENT_ID", studentId);
        args.putString("STUDENT_NAME", studentName);
        args.putString("STUDENT_PROGRAM", studentProgram);
        args.putString("STUDENT_CONTACT", studentContact);
        args.putString("STUDENT_YEAR", studentYear);
        args.putString("BLOCK", block);
        args.putString("VIOLATION", violation);
        args.putString("REMARKS", remarks);
        args.putString("REFERRER_NAME", referrerName);
        args.putString("DATE", date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.prefect_view, container, false);

        // Initialize the TextViews from the layout
        studentNameTextView = view.findViewById(R.id.studentNameTextView);
        studentIdTextView = view.findViewById(R.id.studentIDTextView);
        studentProgramTextView = view.findViewById(R.id.studentProgramTextView);
        studentContactTextView = view.findViewById(R.id.studentContactTextView);
        studentYearTextView = view.findViewById(R.id.studentYearTextView);
        studentBlockTextView = view.findViewById(R.id.studentBlockTextView);
        logEntryTextView = view.findViewById(R.id.logsTextView);  // For log entry display


        // Initialize buttons
        addViolationButton = view.findViewById(R.id.addViolationButton);
        exportPdfButton = view.findViewById(R.id.exportPdfButton);

        // Initialize the TableLayout for violations
        violationTable = view.findViewById(R.id.violationTable);

        // Retrieve arguments passed to the fragment (if any)
        Bundle arguments = getArguments();
        if (arguments != null) {
            String studentId = arguments.getString("STUDENT_ID");
            String studentName = arguments.getString("STUDENT_NAME");
            String studentProgram = arguments.getString("STUDENT_PROGRAM");
            String studentContact = arguments.getString("STUDENT_CONTACT");
            String studentYear = arguments.getString("STUDENT_YEAR");
            String studentBlock = arguments.getString("STUDENT_BLOCK");
            String violation = arguments.getString("VIOLATION");
            String remarks = arguments.getString("REMARKS");
            String date = arguments.getString("DATE");

            // Use the retrieved data as needed, for example, set them to TextViews
            TextView studentNameTextView = view.findViewById(R.id.studentNameTextView);
            TextView studentIdTextView = view.findViewById(R.id.studentIDTextView);
            TextView programTextView = view.findViewById(R.id.studentProgramTextView);

            // Set text to TextViews
            studentNameTextView.setText(studentName);
            studentIdTextView.setText(studentId);
            programTextView.setText(studentProgram);

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


            // Safely initialize the arrays
            if (violation != null && !violation.isEmpty()) {
                violationArray = violation.split("\n");
            } else {
                violationArray = new String[0]; // Initialize to empty array if null
            }

            if (remarks != null && !remarks.isEmpty()) {
                remarksArray = remarks.split("\n");
            } else {
                remarksArray = new String[0];
            }

            if (date != null && !date.isEmpty()) {
                dateArray = date.split("\n"); // Corrected to split 'date' string
            } else {
                dateArray = new String[0];
            }

        // Populate the table safely
            populateViolationTable(violationArray, remarksArray, dateArray);
        }

        // Handle the Add Violation button click
        addViolationButton.setOnClickListener(v -> {
            // Handle the action to add a new violation
            // (You can start a new activity or open a dialog for adding a violation)
        });

        // Handle the Export as PDF button click
        exportPdfButton.setOnClickListener(v -> {
            // Handle PDF export functionality
            // (You can implement PDF generation here)
            showExportOptions();
        });

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
    private void populateViolationTable(String[] violationArray, String[] remarksArray, String[] dateArray) {

        if (violationArray.length == 0 || remarksArray.length == 0 || dateArray.length == 0) {
            Toast.makeText(getContext(), "No violations to display", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < violationArray.length; i++) {
            TableRow row = new TableRow(getContext());

            TextView numberTextView = new TextView(getContext());
            numberTextView.setText(String.valueOf(i + 1));
            numberTextView.setPadding(8, 8, 8, 8);

            TextView violationTextView = new TextView(getContext());
            violationTextView.setText(violationArray[i]);
            violationTextView.setPadding(8, 8, 8, 8);

            TextView remarksTextView = new TextView(getContext());
            remarksTextView.setText(remarksArray[i]);
            remarksTextView.setPadding(8, 8, 8, 8);

            TextView dateTextView = new TextView(getContext());
            dateTextView.setText(dateArray[i]);
            dateTextView.setPadding(8, 8, 8, 8);

            row.addView(numberTextView);
            row.addView(violationTextView);
            row.addView(remarksTextView);
            row.addView(dateTextView);

            violationTable.addView(row);
        }
    }

}
