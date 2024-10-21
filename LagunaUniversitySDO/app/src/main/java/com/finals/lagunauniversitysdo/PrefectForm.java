package com.finals.lagunauniversitysdo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
public class PrefectForm extends AppCompatActivity {

    private Spinner termSpinner, violationSpinner;
    private EditText dateField, nameField, emailField, contactField, programField;
    private TextView violatorsName, violatorsProgram, violatorsStudID, violatorsContact;
    private TableLayout detailsTable;
    private FirebaseFirestore firestore;
    private String scannedName;
    private String scannedContact;
    private String scannedStudentNo;
    private String scannedProgram;
    private String prefectReferrer; // Change the variable name for Prefect form

    // Keys for intent extras (updated for PrefectForm)
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

    private void populateFieldsFromIntent() {
        // Retrieve data from the current intent
        Intent intent = getIntent();  // Get the current intent

        // Retrieve prefect data from the intent
        String prefectName = intent.getStringExtra("PREFECT_NAME_KEY");
        String prefectEmail = intent.getStringExtra("PREFECT_EMAIL_KEY");
        Long prefectContact = intent.getLongExtra("PREFECT_CONTACT_NUM_KEY", 0L);
        String prefectDepartment = intent.getStringExtra("PREFECT_DEPARTMENT_KEY");

        // Populate prefect fields
        nameField.setText(prefectName != null ? prefectName : "");
        emailField.setText(prefectEmail != null ? prefectEmail : "");
        contactField.setText(prefectContact != 0L ? String.valueOf(prefectContact) : "0");
        programField.setText(prefectDepartment != null ? prefectDepartment : "");

        // Retrieve student data from the intent
        ArrayList<String> userNames = intent.getStringArrayListExtra("ADDED_STUDENT_NAMES");
        ArrayList<String> userEmails = intent.getStringArrayListExtra("ADDED_STUDENT_EMAILS");
        ArrayList<Long> userContacts = (ArrayList<Long>) intent.getSerializableExtra("ADDED_STUDENT_CONTACTS");
        ArrayList<String> userDepartments = intent.getStringArrayListExtra("ADDED_STUDENT_DEPARTMENTS");
        ArrayList<String> userIds = intent.getStringArrayListExtra("ADDED_STUDENT_IDS");

        // Handle displaying the student information in the table
        if (userNames != null && userEmails != null && userContacts != null
                && userDepartments != null && userIds != null) {
            for (int i = 0; i < userNames.size(); i++) {
                // Ensure all lists are of the same size
                if (i < userEmails.size() && i < userContacts.size() &&
                        i < userDepartments.size() && i < userIds.size()) {
                    String name = userNames.get(i);
                    String email = userEmails.get(i);
                    Long contact = userContacts.get(i);
                    String department = userDepartments.get(i);
                    String studId = userIds.get(i);

                    // Display each student's data in the table
                    displayStudentDetails(name, department, studId, contact != null ? String.valueOf(contact) : "N/A");
                }
            }
        }

        // Retrieve scanned data
        String scannedData = intent.getStringExtra("scannedData");
        if (scannedData != null) {
            processScannedData(scannedData);
        } else {
            Toast.makeText(this, "No scanned data found", Toast.LENGTH_SHORT).show();
        }
    }


    private void processScannedData(String scannedData) {
        // Retrieve prefect details from Intent
        Intent intent = getIntent();
        String prefectName = intent.getStringExtra("PREFECT_NAME_KEY");
        String prefectEmail = intent.getStringExtra("PREFECT_EMAIL_KEY");
        String prefectContact = intent.getStringExtra("PREFECT_CONTACT_NUM_KEY");
        String prefectDepartment = intent.getStringExtra("PREFECT_DEPARTMENT_KEY");
        String prefectId = intent.getStringExtra("PREFECT_ID_KEY");
        String prefectUsername = intent.getStringExtra("PREFECT_USERNAME_KEY");
        String prefectPassword = intent.getStringExtra("PREFECT_PASSWORD_KEY");

        // Process the scanned data
        String[] scannedFields = scannedData.split("\n");

        // Validate scanned fields
        if (scannedFields.length >= 3) {
            String studentNo = scannedFields[0];  // Assuming the first line is the student number
            String scannedName = scannedFields[1];  // Assuming the second line is the scanned name
            String block = scannedFields[2];  // Assuming the third line is the block

            // Display the student details in the table without the contact info
            displayStudentDetails(scannedName, block, studentNo, ""); // Pass an empty string for contact

            // Store block for later use
            this.scannedProgram = block; // Store block instead of program

            // Store scanned data in fields or temporary variables for submission
            this.scannedName = scannedName; // Store scanned name for later use
            this.scannedContact = UserSession.getContactNum() != null ? String.valueOf(PersonnelSession.getContactNum()) : ""; // Store scanned contact
            this.scannedStudentNo = studentNo; // Store student number

            // Display prefect details on the UI
            displayPrefectDetails(prefectName, prefectEmail, prefectContact, prefectDepartment);
        } else {
            Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_LONG).show();
        }
    }

    // Method to display prefect details on the UI
    private void displayPrefectDetails(String name, String email, String contact, String department) {
        // Assume you have TextViews in your layout to display the prefect details
        TextView prefectNameTextView = findViewById(R.id.name_field);
        TextView prefectEmailTextView = findViewById(R.id.email_field);
        TextView prefectContactTextView = findViewById(R.id.contact_field);
        TextView prefectDepartmentTextView = findViewById(R.id.department_field);

        // Set the text for each TextView with the prefect details
        prefectNameTextView.setText(name);
        prefectEmailTextView.setText(email);
        prefectContactTextView.setText(contact != null && !contact.isEmpty() ? contact : "0");
        prefectDepartmentTextView.setText(department);
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
                displayStudentDetails(name, program, studId, contact);
            }
        }
    }

    // Helper method to display student details in the table
    private void displayStudentDetails(String name, String program, String studId, String contact) {
        // Create a new TableRow
        TableRow row = new TableRow(this);

        // Create TextViews for each column (Name, Program, Student ID, Contact)
        TextView nameCell = new TextView(this);
        TextView programCell = new TextView(this);
        TextView studIdCell = new TextView(this);
        TextView contactCell = new TextView(this);

        // Set the text for each TextView
        nameCell.setText(name);
        programCell.setText(program);
        studIdCell.setText(studId);
        contactCell.setText(contact);

        // Set layout parameters for the TextViews (optional)
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        nameCell.setLayoutParams(params);
        programCell.setLayoutParams(params);
        studIdCell.setLayoutParams(params);
        contactCell.setLayoutParams(params);

        // Add the TextViews to the row
        row.addView(nameCell);
        row.addView(programCell);
        row.addView(studIdCell);
        row.addView(contactCell);

        // Add the row to the TableLayout
        detailsTable.addView(row);
    }

    // Save data (submit the form)
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

        // Retrieve prefect name for referrer
        String prefectReferrer = getIntent().getStringExtra("PREFECT_NAME_KEY");
        if (prefectReferrer == null) {
            prefectReferrer = ""; // Default to empty if not found
        }

        // Retrieve previously added students for submission
        ArrayList<String> addedUserNames = getIntent().getStringArrayListExtra("ADDED_USER_NAMES");
        ArrayList<String> addedUserContacts = getIntent().getStringArrayListExtra("ADDED_USER_CONTACTS");
        ArrayList<String> addedUserPrograms = getIntent().getStringArrayListExtra("ADDED_USER_PROGRAMS");
        ArrayList<String> addedUserStudIds = getIntent().getStringArrayListExtra("ADDED_USER_STUD_IDS");

        // Check if there are added students and add them to Firestore
        if (addedUserNames != null && addedUserContacts != null && addedUserPrograms != null && addedUserStudIds != null) {
            for (int i = 0; i < addedUserNames.size(); i++) {
                if (i < addedUserContacts.size() && i < addedUserPrograms.size() && i < addedUserStudIds.size()) {
                    Map<String, Object> studentData = new HashMap<>();
                    studentData.put("student_name", addedUserNames.get(i));
                    studentData.put("student_program", addedUserPrograms.get(i));
                    studentData.put("student_id", addedUserStudIds.get(i));
                    studentData.put("term", term);
                    studentData.put("violation", violation);
                    studentData.put("date", date);
                    studentData.put("status", status);
                    studentData.put("remarks", remarks);
                    studentData.put("prefect_referrer", prefectReferrer); // Changed to prefect_referrer

                    // Add each student's data directly to the Firestore collection
                    firestore.collection("prefect_referral_history")
                            .add(studentData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to submit data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("Firestore", "Error adding document", e);
                            });
                }
            }
        }

        // Extract displayed students from the table and save them to Firestore
        saveDisplayedStudentsToFirestore(term, violation, date, status, remarks, prefectReferrer); // Changed to prefect_referrer

        // Add the scanned data to Firestore as well
        if (scannedName != null && scannedStudentNo != null && scannedProgram != null) {
            checkAndSaveScannedStudent(scannedName, scannedProgram, scannedStudentNo, term, violation, date, status, remarks, prefectReferrer); // Changed to prefect_referrer
        }
    }

    private void checkAndSaveScannedStudent(String scannedName, String scannedProgram, String scannedStudentNo, String term, String violation, String date, String status, String remarks, String prefectReferrer) {
        // Query Firestore to check if the student already exists
        firestore.collection("prefect_referral_history")
                .whereEqualTo("student_id", scannedStudentNo) // Check by student ID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // Student does not exist, proceed to save
                            Map<String, Object> scannedStudentData = new HashMap<>();
                            scannedStudentData.put("student_name", scannedName);
                            scannedStudentData.put("student_program", scannedProgram);
                            scannedStudentData.put("student_id", scannedStudentNo); // Use scanned student number
                            scannedStudentData.put("term", term);
                            scannedStudentData.put("violation", violation);
                            scannedStudentData.put("date", date);
                            scannedStudentData.put("status", status);
                            scannedStudentData.put("remarks", remarks); // Add remarks to the scanned student data
                            scannedStudentData.put("prefect_referrer", prefectReferrer); // Changed to prefect_referrer

                            // Add scanned student's data directly to the Firestore collection
                            firestore.collection("prefect_referral_history")
                                    .add(scannedStudentData)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(this, "Scanned student data submitted successfully", Toast.LENGTH_SHORT).show();
                                        finish(); // Finish the current activity to return to the previous one
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to submit scanned student data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("Firestore", "Error adding scanned student document", e);
                                    });
                        } else {
                            // Student already exists
                            Toast.makeText(this, "This student has already been submitted.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error checking student existence: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error checking existence", task.getException());
                    }
                });
    }

    private void saveDisplayedStudentsToFirestore(String term, String violation, String date, String status, String remarks, String prefectReferrer) {
        // Iterate over the rows in the detailsTable
        for (int i = 0; i < detailsTable.getChildCount(); i++) {
            TableRow row = (TableRow) detailsTable.getChildAt(i);
            if (row.getChildCount() == 4) { // Ensure there are 4 children (name, program, studId, contact)
                TextView nameCell = (TextView) row.getChildAt(0);
                TextView programCell = (TextView) row.getChildAt(1);
                TextView studIdCell = (TextView) row.getChildAt(2);
                TextView contactCell = (TextView) row.getChildAt(3);

                // Create a map for the student data
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("student_name", nameCell.getText().toString());
                studentData.put("student_program", programCell.getText().toString());
                studentData.put("student_id", studIdCell.getText().toString());
                studentData.put("term", term);
                studentData.put("violation", violation);
                studentData.put("date", date);
                studentData.put("status", status);
                studentData.put("remarks", remarks); // Add remarks if needed
                studentData.put("prefect_referrer", prefectReferrer); // Changed to prefect_referrer

                // Add each student's data directly to the Firestore collection
                firestore.collection("prefect_referral_history")
                        .add(studentData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Displayed student data submitted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to submit displayed student data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Firestore", "Error adding displayed student document", e);
                        });
            }
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


    // Method to clear input fields and the details table
    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        contactField.setText("");
        programField.setText("");
        termSpinner.setSelection(0); // Reset to first item
        violationSpinner.setSelection(0); // Reset to first item
        dateField.setText("");
        violatorsStudID.setText("");
        ((EditText) findViewById(R.id.remarks_field)).setText("");

        // Reset checkboxes
        ((CheckBox) findViewById(R.id.discipline_concerns)).setChecked(false);
        ((CheckBox) findViewById(R.id.behavioral_concerns)).setChecked(false);
        ((CheckBox) findViewById(R.id.learning_difficulty)).setChecked(false);

        // Clear the details table
        detailsTable.removeAllViews(); // This will remove all rows from the TableLayout
    }
}
