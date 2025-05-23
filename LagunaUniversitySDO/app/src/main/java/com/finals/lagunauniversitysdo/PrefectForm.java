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

import java.util.Arrays;
import java.util.HashSet; // For HashSet implementation
import java.util.List;
import java.util.Set; // For Set interface
import java.util.Date; // Import Date class


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

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
    private static final String STUDENT_ID_KEY = "STUDENT_ID";
    private ArrayList<Map<String, String>> scannedStudentDataList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefect_form);

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


        // Set up the term text field
        setupTermText();
    }

    // Initialize UI elements
    private void initializeUIElements() {
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
        // In populateFieldsFromIntent()
        String prefectEmail = intent.getStringExtra("PREFECT_EMAIL_KEY");
        Long prefectContact = intent.getLongExtra("PREFECT_CONTACT_NUM_KEY", 0L);
        String prefectDepartment = intent.getStringExtra("PREFECT_DEPARTMENT_KEY");

        // Populate prefect fields
        nameField.setText(prefectName != null ? prefectName : "");
        emailField.setText(prefectEmail != null ? prefectEmail : "");
        contactField.setText(prefectContact != 0L ? String.valueOf(prefectContact) : "0");
        programField.setText(prefectDepartment != null ? prefectDepartment : "");

        // Display prefect details on the UI
        displayPrefectDetails(prefectName, prefectEmail,String.valueOf(prefectContact), prefectDepartment);

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

        // Retrieve multiple scanned data
        ArrayList<String> scannedDataList = intent.getStringArrayListExtra("SCANNED_DATA_LIST_KEY");
        if (scannedDataList != null && !scannedDataList.isEmpty()) {
            processMultipleScannedData(scannedDataList);
        } else {
            Toast.makeText(this, "No scanned data list found", Toast.LENGTH_SHORT).show();
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

    // New method to process multiple scanned data
    private void processMultipleScannedData(ArrayList<String> scannedDataList) {
        // Create a set to track unique student identifiers (based on studentNo, name, and block)
        Set<String> uniqueStudentIdentifiers = new HashSet<>();

        for (String scannedData : scannedDataList) {
            // Process each scanned data string
            String[] scannedFields = scannedData.split("\n");

            // Validate scanned fields
            if (scannedFields.length >= 3) {
                String studentNo = scannedFields[0];  // Assuming the first line is the student number
                String scannedName = scannedFields[1];  // Assuming the second line is the scanned name
                String block = scannedFields[2];  // Assuming the third line is the block

                // Create a unique identifier for each student (based on studentNo, scannedName, and block)
                String studentIdentifier = studentNo + "_" + scannedName + "_" + block;

                // Check if the student is already added based on the identifier
                if (!uniqueStudentIdentifiers.contains(studentIdentifier)) {
                    // Display the student details in the table without the contact info
                    displayStudentDetails(scannedName, block, studentNo, ""); // Pass an empty string for contact

                    // Add the student identifier to the set to track uniqueness
                    uniqueStudentIdentifiers.add(studentIdentifier);

                    // Store scanned data in fields or temporary variables for submission
                    this.scannedProgram = block; // Store block instead of program
                    this.scannedName = scannedName; // Store scanned name for later use
                    this.scannedContact = UserSession.getContactNum() != null ? String.valueOf(UserSession.getContactNum()) : ""; // Store scanned contact
                    this.scannedStudentNo = studentNo; // Store student number

                    // Store the scanned data in a list for later use
                    Map<String, String> studentData = new HashMap<>();
                    studentData.put("student_no", studentNo);
                    studentData.put("student_name", scannedName);
                    studentData.put("student_program", block);
                    this.scannedStudentDataList.add(studentData); // Maintain the list of scanned data
                }
            } else {
                Toast.makeText(this, "Invalid QR code format for data: " + scannedData, Toast.LENGTH_LONG).show();
            }
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


        // Fetch violation types from Firestore and set up the violation spinner
        fetchViolationTypes();
    }

    private void setupTermText() {
        TextView termTextView = findViewById(R.id.term_text);

        // Get the current month
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH); // January = 0, December = 11

        // Set the term based on the current month
        String currentTerm = "";

        if (currentMonth >= Calendar.AUGUST && currentMonth <= Calendar.DECEMBER) {
            currentTerm = "1st Semester"; // August - December
        } else if (currentMonth >= Calendar.JANUARY && currentMonth <= Calendar.MAY) {
            currentTerm = "2nd Semester"; // January - May
        } else if (currentMonth >= Calendar.JUNE && currentMonth <= Calendar.JULY) {
            currentTerm = "Summer"; // June - July
        }

        // Set the text of the TextView to show the current term
        termTextView.setText(currentTerm);
    }
    // Method to fetch violation types from Firestore
    private void fetchViolationTypes() {
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
            } else {
                Log.w("FormActivity", "Error getting violation types.", task.getException());
                Toast.makeText(this, "Failed to load violation types", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Updated setupSpinner method
    private void setupSpinner(Spinner spinner, List<String> items) {
        CheckboxSpinnerAdapter adapter = new CheckboxSpinnerAdapter(this, items);
        spinner.setAdapter(adapter);
    }

    // Set current date and time
    private void setCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
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
        String date = dateField.getText().toString().trim();
        String studId = violatorsStudID.getText().toString().trim();
        String status = "accepted"; // Default status
        String violation_status = "Unsettled"; // Default violation


        // Get selected violation and offense from the spinner
        CheckboxSpinnerAdapter violationAdapter = (CheckboxSpinnerAdapter) violationSpinner.getAdapter();
        String violation = violationAdapter.getSelectedViolation();
        String offense = violationAdapter.getSelectedType();

        // Retrieve remarks from remarks_field
        String remarks = ((EditText) findViewById(R.id.remarks_field)).getText().toString().trim();
        if (violation.equals("Select a Violation")) {
            Toast.makeText(this, "Please select a valid violation.", Toast.LENGTH_SHORT).show();
            return; // Exit the method if no valid violation is selected
        }

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

        // Validate inputs
        if (!validateInputs(name, email, contactString, program, remarks, userConcern)) {
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

        // Retrieve prefect name and ID for referrer
        String prefectReferrer = PrefectSession.getPrefectName();
        String prefectID = PrefectSession.getPrefectId(); // Assuming you have a prefect ID passed as well

        Log.d("PrefectReferrer", "Prefect ID: " + prefectID);

        if (prefectReferrer == null) {
            prefectReferrer = ""; // Default to empty if not found
        }

        // Get the current term
        String term = getCurrentTerm();

        // Retrieve previously added students for submission
        ArrayList<String> addedUserNames = getIntent().getStringArrayListExtra("ADDED_USER_NAMES");
        ArrayList<String> addedUserContacts = getIntent().getStringArrayListExtra("ADDED_USER_CONTACTS");
        ArrayList<String> addedUserPrograms = getIntent().getStringArrayListExtra("ADDED_USER_PROGRAMS");
        ArrayList<String> addedUserStudentIds = getIntent().getStringArrayListExtra("ADDED_USER_STUDENT_IDS");

        // Track already added student IDs to prevent duplicates
        Set<String> addedStudentIdsSet = new HashSet<>();

        // Check if there are added students and add them to Firestore
        if (addedUserNames != null && addedUserContacts != null && addedUserPrograms != null && addedUserStudentIds != null) {
            for (int i = 0; i < addedUserNames.size(); i++) {
                if (i < addedUserContacts.size() && i < addedUserPrograms.size() && i < addedUserStudentIds.size()) {
                    String studentId = addedUserStudentIds.get(i);
                    // Check if the student ID has already been added
                    if (!addedStudentIdsSet.contains(studentId)) {
                        addedStudentIdsSet.add(studentId); // Mark this ID as added
                        Map<String, Object> studentData = new HashMap<>();
                        studentData.put("student_name", addedUserNames.get(i));
                        studentData.put("student_program", addedUserPrograms.get(i));
                        studentData.put("student_id", studentId);
                        studentData.put("term", term);
                        studentData.put("violation", offense);
                        studentData.put("offense", violation); // Add offense
                        studentData.put("date", date);
                        studentData.put("status", status);
                        studentData.put("violation_status", violation_status);
                        studentData.put("user_concern", userConcern);
                        studentData.put("remarks", remarks);
                        studentData.put("prefect_referrer", prefectReferrer); // Add prefect referrer
                        studentData.put("referrer_id", prefectID);
                        // Get current date and time to use as the document ID for the referral
                        String dateTimeId = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(new Date()) + "_" + studentId;

                        // Add each student's data directly to the Firestore collection (Prefect collection)
                        firestore.collection("prefect")
                                .document(prefectID)
                                .collection("prefect_referral_history")
                                .document(dateTimeId)
                                .set(studentData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to submit data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("Firestore", "Error adding document", e);
                                });

                        // Save the same data to the students/{studentId}/accepted_status collection
                        firestore.collection("students")
                                .document(studentId)
                                .collection("accepted_status")
                                .document(dateTimeId)
                                .set(studentData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Accepted status saved for student: " + studentId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Failed to save accepted status for student: " + studentId, e);
                                });
                    }
                }
            }
        }

        // Repeat the same process for scanning and table data if needed

        // Extract displayed students from the table and save them to Firestore
        for (int i = 0; i < detailsTable.getChildCount(); i++) {
            TableRow row = (TableRow) detailsTable.getChildAt(i);
            if (row.getChildCount() == 4) { // Ensure there are 4 children (name, program, studId, contact)
                TextView nameCell = (TextView) row.getChildAt(0);
                TextView programCell = (TextView) row.getChildAt(1);
                TextView studIdCell = (TextView) row.getChildAt(2);
                TextView contactCell = (TextView) row.getChildAt(3);

                String studentId = studIdCell.getText().toString();
                if (!addedStudentIdsSet.contains(studentId)) {
                    addedStudentIdsSet.add(studentId); // Mark this ID as added

                    Map<String, Object> studentData = new HashMap<>();
                    studentData.put("student_name", nameCell.getText().toString());
                    studentData.put("student_program", programCell.getText().toString());
                    studentData.put("student_id", studentId);
                    studentData.put("term", term);
                    studentData.put("violation", offense);
                    studentData.put("offense", violation); // Add offense
                    studentData.put("date", date);
                    studentData.put("status", status);
                    studentData.put("violation_status", violation_status);
                    studentData.put("remarks", remarks);
                    studentData.put("user_concern", userConcern);
                    studentData.put("referrer_id", prefectID);
                    studentData.put("prefect_referrer", prefectReferrer);

                    String dateTimeId = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(new Date()) + "_" + studentId;

                    firestore.collection("prefect")
                            .document(prefectID)
                            .collection("prefect_referral_history")
                            .document(dateTimeId)
                            .set(studentData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to submit data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("Firestore", "Error adding document", e);
                            });

                    firestore.collection("students")
                            .document(studentId)
                            .collection("accepted_status")
                            .document(dateTimeId)
                            .set(studentData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Firestore", "Accepted status saved for student: " + studentId);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Failed to save accepted status for student: " + studentId, e);
                            });
                }
            }
        }

        for (Map<String, String> scannedData : scannedStudentDataList) {
            String scannedName = scannedData.get("student_name");
            String scannedStudentNo = scannedData.get("student_no");
            String scannedProgram = scannedData.get("student_program");

            if (!addedStudentIdsSet.contains(scannedStudentNo)) {
                String scannedDateTimeId = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(new Date()) + "_" + scannedStudentNo;

                Map<String, Object> scannedStudentData = new HashMap<>();
                scannedStudentData.put("student_name", scannedName);
                scannedStudentData.put("student_program", scannedProgram);
                scannedStudentData.put("student_id", scannedStudentNo);
                scannedStudentData.put("term", term);
                scannedStudentData.put("violation", offense);
                scannedStudentData.put("offense", violation); // Add offense
                scannedStudentData.put("date", date);
                scannedData.put("user_concern", userConcern);
                scannedStudentData.put("status", status);
                scannedData.put("violation_status", violation_status);
                scannedStudentData.put("remarks", remarks);
                scannedStudentData.put("prefect_referrer", prefectReferrer);
                scannedStudentData.put("prefect_id", prefectID);

                firestore.collection("prefect")
                        .document(prefectID)
                        .collection("prefect_referral_history")
                        .document(scannedDateTimeId)
                        .set(scannedStudentData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to submit data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Firestore", "Error adding document", e);
                        });

                firestore.collection("students")
                        .document(scannedStudentNo)
                        .collection("accepted_status")
                        .document(scannedDateTimeId)
                        .set(scannedStudentData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firestore", "Accepted status saved for student: " + scannedStudentNo);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Failed to save accepted status for student: " + scannedStudentNo, e);
                        });
            }
        }
    }

    // Method to get the current term
    private String getCurrentTerm() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH); // January = 0, December = 11

        if (currentMonth >= Calendar.AUGUST && currentMonth <= Calendar.DECEMBER) {
            return "1st Semester"; // August - December
        } else if (currentMonth >= Calendar.JANUARY && currentMonth <= Calendar.MAY) {
            return "2nd Semester"; // January - May
        } else {
            return "Summer"; // June - July
        }
    }





    private boolean validateInputs(String name, String email, String contact, String program, String remarks, String userConcern) {
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
        if (TextUtils.isEmpty(remarks)) {

        }
        if (userConcern.isEmpty()) {
            Toast.makeText(this, "Please select at least one concern.", Toast.LENGTH_SHORT).show();
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
