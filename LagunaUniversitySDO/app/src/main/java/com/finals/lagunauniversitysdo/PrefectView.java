package com.finals.lagunauniversitysdo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PrefectView extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefect_view);  // Link to the updated layout

        // Initialize the TextViews from the layout
        studentNameTextView = findViewById(R.id.studentNameTextView);
        studentIdTextView = findViewById(R.id.studentIDTextView);
        studentProgramTextView = findViewById(R.id.studentProgramTextView);
        studentContactTextView = findViewById(R.id.studentContactTextView);
        studentYearTextView = findViewById(R.id.studentYearTextView);
        studentBlockTextView = findViewById(R.id.studentBlockTextView);
        logEntryTextView = findViewById(R.id.logsTextView);  // For log entry display

        // Initialize buttons
        addViolationButton = findViewById(R.id.addViolationButton);
        exportPdfButton = findViewById(R.id.exportPdfButton);

        // Initialize the TableLayout for violations
        violationTable = findViewById(R.id.violationTable);

        // Get the Intent that started this Activity
        Intent intent = getIntent();

        // Retrieve student details and violations from the Intent
        String studentId = intent.getStringExtra("STUDENT_ID");
        String studentName = intent.getStringExtra("STUDENT_NAME");
        String studentProgram = intent.getStringExtra("STUDENT_PROGRAM");
        String studentContact = intent.getStringExtra("STUDENT_CONTACT");
        String studentYear = intent.getStringExtra("STUDENT_YEAR");
        String studentBlock = intent.getStringExtra("STUDENT_BLOCK");
        String[] violations = intent.getStringArrayExtra("VIOLATIONS");  // Assume an array of violations

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

        // Display the violations in the table if available
        if (violations != null && violations.length > 0) {
            populateViolationTable(violations);  // Custom method to populate table
        } else {
            logEntryTextView.setText("No violations found.");
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
        });
    }

    // Method to populate the TableLayout with violation data
    private void populateViolationTable(String[] violations) {
        // For each violation, add a new row to the table
        for (int i = 0; i < violations.length; i++) {
            // Create a new TableRow
            TableRow tableRow = new TableRow(this);

            // Add the violation details to the row
            TextView violationNumberTextView = new TextView(this);
            violationNumberTextView.setText(String.valueOf(i + 1));  // Set violation number

            TextView violationDescriptionTextView = new TextView(this);
            violationDescriptionTextView.setText(violations[i]);  // Set the violation description

            // Add the TextViews to the TableRow
            tableRow.addView(violationNumberTextView);
            tableRow.addView(violationDescriptionTextView);

            // Add the TableRow to the TableLayout
            violationTable.addView(tableRow);
        }
    }
}
