package com.finals.lagunauniversitysdo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class form extends AppCompatActivity {

    private Spinner termSpinner, violationSpinner;
    private EditText dateField, nameField, emailField, contactField, programField;
    private TextView violatorsName, violatorsProgram, violatorsStudID, violatorsContact;
    private TableLayout detailsTable;
    private FirebaseFirestore firestore;

    // Keys for intent extras
    private static final String STUDENT_NAME_KEY = "STUDENT_NAME";
    private static final String EMAIL_KEY = "EMAIL";
    private static final String CONTACT_NUM_KEY = "CONTACT_NUM";
    private static final String PROGRAM_KEY = "PROGRAM";
    private static final String STUD_ID_KEY = "STUD_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);

        // Initialize UI elements and Firestore
        initializeUIElements();
        firestore = FirebaseFirestore.getInstance();

        // Populate fields from intent
        populateFieldsFromIntent();

        // Set up spinner options
        setupSpinners();

        // Set current date and time
        setCurrentDateTime();

        // Retrieve and display previously added students
        retrieveAndDisplayAddedStudents();

        // Set up submit button
        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> saveData());
    }

    // Initialize UI elements
    private void initializeUIElements() {
        termSpinner = findViewById(R.id.term_spinner);
        violationSpinner = findViewById(R.id.violation_spinner);
        dateField = findViewById(R.id.date_field);
        nameField = findViewById(R.id.name_field);
        emailField = findViewById(R.id.email_field);
        contactField = findViewById(R.id.contact_field);
        programField = findViewById(R.id.department_field);
        detailsTable = findViewById(R.id.details_table);
        violatorsName = findViewById(R.id.violators_name);
        violatorsProgram = findViewById(R.id.violators_program);
        violatorsStudID = findViewById(R.id.violators_studID);
        violatorsContact = findViewById(R.id.violators_contact);
    }

    // Populate fields from intent
    private void populateFieldsFromIntent() {
        String studentName = getIntent().getStringExtra(STUDENT_NAME_KEY);
        String studentEmail = getIntent().getStringExtra(EMAIL_KEY);
        Long studentContact = getIntent().getLongExtra(CONTACT_NUM_KEY, 0L);
        String studentProgram = getIntent().getStringExtra(PROGRAM_KEY);
        String studId = getIntent().getStringExtra(STUD_ID_KEY);

        nameField.setText(studentName != null ? studentName : "");
        emailField.setText(studentEmail != null ? studentEmail : "");
        contactField.setText(studentContact != 0L ? String.valueOf(studentContact) : "");
        programField.setText(studentProgram != null ? studentProgram : "");
    }

    // Set up spinner options
    private void setupSpinners() {
        String[] terms = {"First Sem", "Second Sem", "Summer"};
        String[] violations = {"Light Offense", "Serious Offense", "Major Offense"};

        setupSpinner(termSpinner, terms);
        setupSpinner(violationSpinner, violations);
    }

    // Helper method to set up a spinner
    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // Set current date and time
    private void setCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentDateTime = dateFormat.format(calendar.getTime());
        dateField.setText(currentDateTime);
    }

    // Retrieve and display previously added students
    private void retrieveAndDisplayAddedStudents() {
        ArrayList<String> addedUserNames = getIntent().getStringArrayListExtra("ADDED_USER_NAMES");
        ArrayList<String> addedUserContacts = getIntent().getStringArrayListExtra("ADDED_USER_CONTACTS");
        ArrayList<String> addedUserPrograms = getIntent().getStringArrayListExtra("ADDED_USER_PROGRAMS");
        ArrayList<String> addedUserStudIds = getIntent().getStringArrayListExtra("ADDED_USER_STUD_IDS");

        if (addedUserNames != null && addedUserContacts != null && addedUserPrograms != null && addedUserStudIds != null) {
            for (int i = 0; i < addedUserNames.size(); i++) {
                String name = addedUserNames.get(i);
                String contact = addedUserContacts.get(i);
                String program = addedUserPrograms.get(i);
                String studId = addedUserStudIds.get(i);

                displayStudentDetails(studId, name, contact, program);
            }
        }
    }


    private void saveData() {
        // Other field values
        String term = termSpinner.getSelectedItem().toString();
        String violation = violationSpinner.getSelectedItem().toString();
        String date = dateField.getText().toString();

        // Loop through the table rows
        for (int i = 0; i < detailsTable.getChildCount(); i++) {
            TableRow tableRow = (TableRow) detailsTable.getChildAt(i);

            // Assuming the table has four columns: studId, name, contact, and program
            TextView studIdView = (TextView) tableRow.getChildAt(0);
            TextView nameView = (TextView) tableRow.getChildAt(1);
            TextView contactView = (TextView) tableRow.getChildAt(2);
            TextView programView = (TextView) tableRow.getChildAt(3);

            // Extract values from the row
            String studId = studIdView.getText().toString();
            String name = nameView.getText().toString();
            String contact = contactView.getText().toString();
            String program = programView.getText().toString();

            // Debug log for tracking
            Log.d("FirestoreSave", "Saving row: " + name);

            // Prepare data to save for each student
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("stud_id", studId);
            studentData.put("name", name);
            studentData.put("contact", contact);
            studentData.put("program", program);
            studentData.put("term", term);
            studentData.put("violation", violation);
            studentData.put("date", date);
            studentData.put("status", "pending");

            // Save the data to Firestore
            firestore.collection("student_refferal_history")
                    .add(studentData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("FirestoreSave", "Row saved successfully: " + name);
                        Toast.makeText(form.this, "Row saved successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreSave", "Error saving row: " + e.getMessage());
                        Toast.makeText(form.this, "Error saving row: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        // After saving all rows, navigate back or do any additional action
        Intent intent = new Intent(form.this, refferalForm_student.class); // Replace with your desired fragment/activity
        startActivity(intent);
        finish();
    }




    // Validate input fields
    private boolean validateFields(String name, String email, String contact, String program) {
        if (TextUtils.isEmpty(name)) {
            nameField.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Valid email is required");
            return false;
        }
        if (TextUtils.isEmpty(contact)) {
            contactField.setError("Contact number is required");
            return false;
        }
        if (TextUtils.isEmpty(program)) {
            programField.setError("Program is required");
            return false;
        }
        return true;
    }

    // Parse contact number safely
    private Long parseContactNumber(String contact) {
        try {
            return Long.parseLong(contact);
        } catch (NumberFormatException e) {
            contactField.setError("Invalid contact number");
            return null; // Invalid number
        }
    }

    // Display student details in the table
    private void displayStudentDetails(String studId, String name, String contact, String program) {
        TableRow tableRow = new TableRow(this);
        TextView studIdView = new TextView(this);
        TextView nameView = new TextView(this);
        TextView contactView = new TextView(this);
        TextView programView = new TextView(this);

        // Set text for TextViews
        studIdView.setText(studId);
        nameView.setText(name);
        contactView.setText(contact);
        programView.setText(program);

        // Add TextViews to TableRow
        tableRow.addView(studIdView);
        tableRow.addView(nameView);
        tableRow.addView(contactView);
        tableRow.addView(programView);

        // Add TableRow to TableLayout
        detailsTable.addView(tableRow);
    }
}
