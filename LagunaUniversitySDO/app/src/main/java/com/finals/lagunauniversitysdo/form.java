package com.finals.lagunauniversitysdo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.widget.CheckBox;


public class form extends AppCompatActivity {

    private Spinner termSpinner, violationSpinner;
    private EditText dateField, nameField, emailField, contactField, programField;
    private TextView violatorsName, violatorsProgram, violatorsStudID, violatorsContact;
    private TableLayout detailsTable;
    private FirebaseFirestore firestore;
    private String scannedName;
    private String scannedContact;
    private String scannedStudentNo;
    private String scannedProgram;
    private String studentReferrer; // Declare a variable to hold the student referrer

    private String firstName, lastName;

    // Keys for intent extras
    private static final String STUDENT_NAME = "NAME";
    private static final String EMAIL_KEY = "EMAIL";
    private static final String CONTACT_NUM_KEY = "CONTACT_NUM";
    private static final String PROGRAM_KEY = "PROGRAM";
    private static final String STUDENT_ID_KEY = "STUDENT_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.form_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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


    private void populateFieldsFromIntent() {
        Intent intent = getIntent();  // Get the current intent

        // Retrieve standard student data from the intent
        String studentName = intent.getStringExtra("STUDENT_NAME");
        String studentEmail = intent.getStringExtra(EMAIL_KEY);
        Long studentContact = intent.getLongExtra(CONTACT_NUM_KEY, 0L);
        String studentProgram = intent.getStringExtra(PROGRAM_KEY);
        String studId = intent.getStringExtra(STUDENT_ID_KEY);

        // Logging the received data for debugging purposes
        Log.d("FormActivity", "Received Student Name: " + studentName);
        Log.d("FormActivity", "Received Student Email: " + studentEmail);

        firstName = intent.getStringExtra("FIRST_NAME");
        lastName = intent.getStringExtra("LAST_NAME");

        Log.d("FormActivity", "First name: " + firstName);
        Log.d("FormActivity", "Last name: " + lastName);

        // Assign the studentName to the studentReferrer variable
        studentReferrer = (studentName != null) ? studentName : "";

        // Populate the fields with the received data, checking for null and default values
        nameField.setText(studentName != null ? studentName : "");  // Set name if not null
        emailField.setText(studentEmail != null ? studentEmail : "");  // Set email if not null
        contactField.setText(studentContact != 0L ? String.valueOf(studentContact) : "");  // Set contact if not default value
        programField.setText(studentProgram != null ? studentProgram : "");  // Set program if not null

        // Retrieve and process scanned data if available
        String scannedData = intent.getStringExtra("scannedData");
        if (scannedData != null && !scannedData.isEmpty()) {
            Log.d("FormActivity", "Scanned Data Found: " + scannedData); // Debugging line
            processScannedData(scannedData);
        } else {
            Log.w("FormActivity", "No scanned data found in the intent"); // Warning log
            Toast.makeText(this, "No scanned data found", Toast.LENGTH_SHORT).show();
        }
    }


    private void processScannedData(String scannedData) {
        // Process the scanned data
        String[] scannedFields = scannedData.split("\n");

        if (scannedFields.length >= 3) {
            String studentNo = scannedFields[0];  // Assuming the first line is the student number
            String scannedName = scannedFields[1];  // Assuming the second line is the scanned name
            String block = scannedFields[2];  // Assuming the third line is the block

            // Display the student details in the table without the contact info
            displayStudentDetails(scannedName, block, studentNo, ""); // Pass an empty string for contact

            // Store block for later use
            this.scannedProgram = block; // Store block instead of program

            // Retrieve student data from UserSession
            String studentName = UserSession.getStudentName();
            String firstName = UserSession.getFirstName();
            String lastName = UserSession.getLastName();
            String studentEmail = UserSession.getEmail();
            Long studentContact = UserSession.getContactNum();
            String studentProgram = UserSession.getProgram();
            String studentId = UserSession.getStudentId();

            // Set studentReferrer to the name retrieved from UserSession
            this.studentReferrer = studentName != null ? studentName : "";

            // Populate fields with standard student data
            nameField.setText(studentName != null ? studentName : "");
            emailField.setText(studentEmail != null ? studentEmail : "");
            contactField.setText(studentContact != null ? String.valueOf(studentContact) : "");
            programField.setText(studentProgram != null ? studentProgram : "");

            // Optionally store scanned data in fields or a temporary variable for submission
            this.scannedName = scannedName; // Store scanned name for later use
            this.scannedContact = studentContact != null ? String.valueOf(studentContact) : ""; // Store scanned contact
            this.scannedStudentNo = studentNo; // Store student number

        } else {
            Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_LONG).show();
        }
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
        ArrayList<String> addedUserStudentIds = getIntent().getStringArrayListExtra("ADDED_USER_STUDENT_IDS");

        if (addedUserNames != null && addedUserContacts != null && addedUserPrograms != null && addedUserStudentIds != null) {
            for (int i = 0; i < addedUserNames.size(); i++) {
                String name = addedUserNames.get(i);
                String contact = addedUserContacts.get(i);
                String program = addedUserPrograms.get(i);
                String studId = addedUserStudentIds.get(i);
                displayStudentDetails(name, program, studId, contact);
            }
        }
    }

    // Helper method to display student details in the table
    private void displayStudentDetails(String name, String program, String studId, String contact) {
        TableRow row = new TableRow(this);

        TextView nameCell = new TextView(this);
        TextView programCell = new TextView(this);
        TextView studIdCell = new TextView(this);
        TextView contactCell = new TextView(this);

        nameCell.setText(name);
        programCell.setText(program);
        studIdCell.setText(studId);
        contactCell.setText(contact);

        row.addView(nameCell);
        row.addView(programCell);
        row.addView(studIdCell);
        row.addView(contactCell);

        detailsTable.addView(row);
    }


    private void saveData() {
        // Initialize CheckBox for privacy consent
        CheckBox privacyConsentCheckbox = findViewById(R.id.privacy_consent);

        // Check if the privacy consent checkbox is checked
        if (!privacyConsentCheckbox.isChecked()) {
            Toast.makeText(this, "You must agree to the privacy policy to proceed.", Toast.LENGTH_SHORT).show();
            return; // Exit the method if checkbox is not checked
        }

        // Retrieve values from input fields
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String contactString = contactField.getText().toString().trim();
        String program = programField.getText().toString().trim();
        String term = termSpinner.getSelectedItem().toString();
        String violation = violationSpinner.getSelectedItem().toString();
        String date = dateField.getText().toString().trim();
        String studId = violatorsStudID.getText().toString().trim();
        String status = "pending"; // Default status

        // Retrieve remarks from remarks_field
        String remarks = ((EditText) findViewById(R.id.remarks_field)).getText().toString().trim();

        // Validate inputs
        if (!validateInputs(name, email, contactString, program)) {
            return; // Exit the method if validation fails
        }

        // Parse the contact string safely
        Long contact;
        try {
            contact = Long.parseLong(contactString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid contact number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve previously added students for submission
        ArrayList<String> addedUserNames = getIntent().getStringArrayListExtra("ADDED_USER_NAMES");
        ArrayList<String> addedUserContacts = getIntent().getStringArrayListExtra("ADDED_USER_CONTACTS");
        ArrayList<String> addedUserPrograms = getIntent().getStringArrayListExtra("ADDED_USER_PROGRAMS");
        ArrayList<String> addedUserStudentIds = getIntent().getStringArrayListExtra("ADDED_USER_STUDENT_IDS");

        // Retrieve checkbox states and create a single string for user concerns
        String userConcern = "";
        CheckBox disciplineCheckbox = findViewById(R.id.discipline_concerns);
        CheckBox behavioralCheckbox = findViewById(R.id.behavioral_concerns);
        CheckBox learningCheckbox = findViewById(R.id.learning_difficulty);

        // Build the user concern string based on checked checkboxes
        if (disciplineCheckbox.isChecked()) {
            userConcern = "Discipline Concerns";
        } else if (behavioralCheckbox.isChecked()) {
            userConcern = "Behavioral Concerns";
        } else if (learningCheckbox.isChecked()) {
            userConcern = "Learning Difficulty";
        }

        // Check if there are added students and add them to Firestore
        if (addedUserNames != null && addedUserContacts != null && addedUserPrograms != null && addedUserStudentIds != null) {
            for (int i = 0; i < addedUserNames.size(); i++) {
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("student_name", addedUserNames.get(i));
                studentData.put("student_program", addedUserPrograms.get(i));
                studentData.put("student_id", addedUserStudentIds.get(i)); // Added Student ID
                studentData.put("term", term);
                studentData.put("violation", violation);
                studentData.put("date", date);
                studentData.put("status", status);
                studentData.put("user_concern", userConcern); // Add user concern to the student data
                studentData.put("remarks", remarks); // Add remarks to the student data
                studentData.put("student_referrer", studentReferrer); // Add student_referrer to Firestore data

                // Add each student's data directly to the Firestore collection
                firestore.collection("student_refferal_history")
                        .add(studentData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                            // Just finish the current activity to return to the previous one
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to submit data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Firestore", "Error adding document", e);
                        });
            }
        }

        // Add the scanned data to Firestore as well without contact info
        if (scannedName != null && scannedStudentNo != null && scannedProgram != null) {
            Map<String, Object> scannedStudentData = new HashMap<>();
            scannedStudentData.put("student_name", scannedName);
            scannedStudentData.put("student_program", scannedProgram);
            scannedStudentData.put("student_id", scannedStudentNo); // Use scanned student number
            scannedStudentData.put("term", term);
            scannedStudentData.put("violation", violation);
            scannedStudentData.put("date", date);
            scannedStudentData.put("status", status);
            scannedStudentData.put("user_concern", userConcern); // Add user concern to scanned student data
            scannedStudentData.put("remarks", remarks); // Add remarks to scanned student data
            scannedStudentData.put("student_referrer", studentReferrer); // Add student_referrer to Firestore data

            // Add scanned student's data directly to the Firestore collection
            firestore.collection("student_refferal_history")
                    .add(scannedStudentData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Scanned student data submitted successfully", Toast.LENGTH_SHORT).show();
                        // Just finish the current activity to return to the previous one
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to submit scanned student data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error adding scanned student document", e);
                    });
        }
    }



    private boolean validateInputs(String name, String email, String contact, String program) {
        if (TextUtils.isEmpty(name)) {
            nameField.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Valid email is required");
            return false;
        }
        if (TextUtils.isEmpty(contact)) {
            contactField.setError("Contact is required");
            return false;
        }
        if (TextUtils.isEmpty(program)) {
            programField.setError("Program is required");
            return false;
        }
        return true;
    }


    // Clear input fields
    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        contactField.setText("");
        programField.setText("");
        termSpinner.setSelection(0);
        violationSpinner.setSelection(0);
        setCurrentDateTime();
    }
}
