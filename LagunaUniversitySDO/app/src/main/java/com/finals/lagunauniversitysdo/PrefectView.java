package com.finals.lagunauniversitysdo;

import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PrefectView extends AppCompatActivity {

    // Declare the TextViews
    private TextView studentNameTextView;
    private TextView studentIdTextView;
    private TextView logEntryTextView;  // New TextView for displaying the log entry
    private TextView referrerTypeTextView;  // New TextView for displaying referrer type
    private TextView referrerNameTextView;  // New TextView for displaying referrer name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefect_view);  // Link the layout XML

        // Initialize the TextViews from the layout
        studentNameTextView = findViewById(R.id.studentNameTextView);
        studentIdTextView = findViewById(R.id.studentIdTextView);
        logEntryTextView = findViewById(R.id.logEntryTextView);  // Initialize new TextView for log entry
        referrerTypeTextView = findViewById(R.id.referrerTypeTextView);  // Initialize new TextView for referrer type
        referrerNameTextView = findViewById(R.id.referrerNameTextView);  // Initialize new TextView for referrer name

        // Get the Intent that started this Activity
        Intent intent = getIntent();

        // Retrieve the student ID, name, log entry, referrer type, and referrer name from the Intent
        String studentId = intent.getStringExtra("STUDENT_ID");
        String studentName = intent.getStringExtra("STUDENT_NAME");
        String logEntry = intent.getStringExtra("log_entry");  // Retrieve the log entry
        String referrerType = intent.getStringExtra("referrer_type");  // Retrieve the referrer type
        String referrerName = intent.getStringExtra("referrer_name");  // Retrieve the referrer name

        // Check if the log entry is available
        if (logEntry != null && !logEntry.isEmpty()) {
            // If log entry is present, hide student details and show log entry
            studentIdTextView.setVisibility(View.GONE);
            studentNameTextView.setVisibility(View.GONE);
            logEntryTextView.setText("Log Entry:\n" + logEntry);
            logEntryTextView.setVisibility(View.VISIBLE);

            // Display referrer type if available
            if (referrerType != null && !referrerType.isEmpty()) {
                referrerTypeTextView.setText("Referrer Type: " + referrerType);
                referrerTypeTextView.setVisibility(View.VISIBLE);
            }

            // Display referrer name if available
            if (referrerName != null && !referrerName.isEmpty()) {
                referrerNameTextView.setText("Referrer Name: " + referrerName);
                referrerNameTextView.setVisibility(View.VISIBLE);
            }
        } else {
            // If log entry is not available, show student details
            studentIdTextView.setVisibility(View.VISIBLE);
            studentNameTextView.setVisibility(View.VISIBLE);
            logEntryTextView.setVisibility(View.GONE);
            referrerTypeTextView.setVisibility(View.GONE);  // Hide referrer type in this case
            referrerNameTextView.setVisibility(View.GONE);  // Hide referrer name in this case

            // Display the student ID and name in the corresponding TextViews
            if (studentId != null && !studentId.isEmpty()) {
                studentIdTextView.setText("ID: " + studentId);
            }
            if (studentName != null && !studentName.isEmpty()) {
                studentNameTextView.setText("Name: " + studentName);
            }
        }
    }
}
