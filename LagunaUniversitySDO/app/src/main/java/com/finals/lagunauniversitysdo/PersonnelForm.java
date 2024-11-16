package com.finals.lagunauniversitysdo;

import java.util.Arrays;
import java.util.Date;
import java.util.Set; // Import the Set class
import java.util.List; // Import the List class
import java.util.ArrayList; // Import the ArrayList class
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import java.util.HashSet;
import java.util.Set;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.widget.CheckBox;


public class PersonnelForm extends AppCompatActivity {

    private Spinner violationSpinner;
    private EditText dateField, nameField, emailField, contactField, programField, remarksField, termField;
    private TextView violatorsName, violatorsProgram, violatorsStudID, violatorsContact;
    private TableLayout detailsTable;
    private FirebaseFirestore firestore;
    private String scannedName;
    private String scannedContact;
    private String scannedStudentNo;
    private String scannedProgram;
    private String personnelReferrer, personnelID; // Change the variable name here
    private ArrayList<Map<String, String>> scannedStudentDataList = new ArrayList<>();


    // Keys for intent extras
    private static final String STUDENT_NAME_KEY = "STUDENT_NAME";
    private static final String EMAIL_KEY = "EMAIL";
    private static final String CONTACT_NUM_KEY = "CONTACT_NUM";
    private static final String PROGRAM_KEY = "PROGRAM";
    private static final String STUDENT_ID_KEY = "STUDENT_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personnel_form);

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

        setupTermText();
    }

    // Declare lists to hold student IDs and their details
    private List<String> addedUserIds = new ArrayList<>();  // Stores student IDs
    private Map<String, String> addedUserDetails = new HashMap<>();  // Maps student ID to details (or other data)



    // Initialize UI elements
    private void initializeUIElements() {
        violationSpinner = findViewById(R.id.violation_spinner);
        dateField = findViewById(R.id.date_field);
        nameField = findViewById(R.id.name_field);
        emailField = findViewById(R.id.email_field);
        contactField = findViewById(R.id.contact_field);
        programField = findViewById(R.id.department_field);
        remarksField = findViewById(R.id.remarks_field);
        detailsTable = findViewById(R.id.details_table);
        violatorsName = findViewById(R.id.violators_name);
        violatorsProgram = findViewById(R.id.violators_program);
        violatorsStudID = findViewById(R.id.violators_studID);
        violatorsContact = findViewById(R.id.violators_contact);

    }



    private void populateFieldsFromIntent() {
        // Retrieve data from the current intent
        Intent intent = getIntent();  // Get the current intent

        // Retrieve personnel data from the intent
        String personnelName = intent.getStringExtra("PERSONNEL_NAME_KEY");
        String personnelEmail = intent.getStringExtra("PERSONNEL_EMAIL_KEY");
        String personnelContact = intent.getStringExtra("PERSONNEL_CONTACT_NUM_KEY");
        String personnelDepartment = intent.getStringExtra("PERSONNEL_DEPARTMENT_KEY");

        // Populate personnel fields
        nameField.setText(personnelName != null ? personnelName : "");
        emailField.setText(personnelEmail != null ? personnelEmail : "");
        contactField.setText(personnelContact != null ? personnelContact : "");
        programField.setText(personnelDepartment != null ? personnelDepartment : "");


        // Retrieve student data from the intent
        ArrayList<String> userNames = intent.getStringArrayListExtra("ADDED_STUDENT_NAMES");
        ArrayList<String> userEmails = intent.getStringArrayListExtra("ADDED_STUDENT_EMAILS");
        ArrayList<String> userContacts = intent.getStringArrayListExtra("ADDED_STUDENT_CONTACTS");
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
                    String contact = userContacts.get(i);
                    String department = userDepartments.get(i);
                    String studId = userIds.get(i);

                    // Display each student's data in the table
                    displayStudentDetails(name, department, studId, contact != null ? String.valueOf(contact) : "N/A");
                }
            }
        }

        // Retrieve scanned data (single scan)
        String scannedData = intent.getStringExtra("scannedData");
        if (scannedData != null) {
            processScannedData(scannedData);
        } else {
            // Retrieve scanned data (multiple scans)
            ArrayList<String> scannedDataList = intent.getStringArrayListExtra("scannedDataList");
            if (scannedDataList != null) {
                processMultipleScannedData(scannedDataList); // Process multiple scanned data
            } else {
                Toast.makeText(this, "No scanned data found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to process a single scanned data input
    private void processScannedData(String scannedData) {
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

            // Store the scanned data in a list for later use
            Map<String, String> studentData = new HashMap<>();
            studentData.put("student_no", studentNo);
            studentData.put("student_name", scannedName);
            studentData.put("student_program", block);
            this.scannedStudentDataList.add(studentData); // Maintain the list of scanned data

        } else {
            Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_LONG).show();
        }
    }

    // New method to process multiple scanned data inputs
    private void processMultipleScannedData(ArrayList<String> scannedDataList) {
        // Create a set to track unique student numbers (or a combination of studentNo, scannedName, and block)
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

                // Check if the student is already added
                if (!uniqueStudentIdentifiers.contains(studentIdentifier)) {
                    // Display the student details in the table without the contact info
                    displayStudentDetails(scannedName, block, studentNo, ""); // Pass an empty string for contact

                    // Add the student identifier to the set to track uniqueness
                    uniqueStudentIdentifiers.add(studentIdentifier);

                    // Store scanned data in fields or temporary variables for submission
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



    private void setupSpinners() {
        // Fetch violation types from Firestore and set up the violation spinner
        fetchViolationTypes();


    }

    // Set up the term spinner
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
    // Example: Correct for Activity context
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

        // Add TextViews to the TableRow
        row.addView(nameCell);
        row.addView(programCell);
        row.addView(studIdCell);
        row.addView(contactCell);

        // Add the TableRow to the TableLayout
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
        String date = dateField.getText().toString().trim();
        String studId = violatorsStudID.getText().toString().trim();
        String status = "pending"; // Default status

        // Retrieve remarks from remarks_field
        String remarks = ((EditText) findViewById(R.id.remarks_field)).getText().toString().trim();

        // Get selected violation and offense from the spinner
        CheckboxSpinnerAdapter violationAdapter = (CheckboxSpinnerAdapter) violationSpinner.getAdapter();
        String violation = violationAdapter.getSelectedViolation();
        String offense = violationAdapter.getSelectedType();

        if (violation == null || violation.equals("Select a Violation")) {
            Toast.makeText(this, "Please select a valid violation.", Toast.LENGTH_SHORT).show();
            return; // Exit the method if no valid violation is selected
        }

        // Check if offense is selected (optional, depending on your requirements)
        if (offense == null || offense.trim().isEmpty()) {
            Toast.makeText(this, "Please select a valid offense type.", Toast.LENGTH_SHORT).show();
            return; // Exit the method if no offense type is selected
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

        // Retrieve personnel ID and name for referrer from PersonnelSession
        String personnelReferrer = PersonnelSession.getPersonnelName(); // Use the name from PersonnelSession
        String personnelID = PersonnelSession.getPersonnelId(); // Use the ID from PersonnelSession

        if (personnelReferrer == null) {
            personnelReferrer = ""; // Default to empty if not found
        }

        // Retrieve previously added students for submission
        ArrayList<String> addedUserNames = getIntent().getStringArrayListExtra("ADDED_USER_NAMES");
        ArrayList<String> addedUserContacts = getIntent().getStringArrayListExtra("ADDED_USER_CONTACTS");
        ArrayList<String> addedUserPrograms = getIntent().getStringArrayListExtra("ADDED_USER_PROGRAMS");
        ArrayList<String> addedUserStudentIds = getIntent().getStringArrayListExtra("ADDED_USER_STUDENT_IDS");

        // Track already added student IDs to prevent duplicates
        Set<String> addedStudentIdsSet = new HashSet<>();

        // Get current term from the setupTermText method
        String term = getCurrentTerm();

        // Check if the term is empty or invalid (if needed)
        if (term == null || term.equals("Select a Term")) {
            Toast.makeText(this, "Please select a valid term.", Toast.LENGTH_SHORT).show();
            return; // Exit the method if no valid term is selected
        }

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
                        studentData.put("violation", offense);
                        studentData.put("offense", violation); // Save the offense type
                        studentData.put("date", date);
                        studentData.put("status", status);
                        studentData.put("user_concern", userConcern); // Add user concern to the student data
                        studentData.put("remarks", remarks);
                        studentData.put("personnel_referrer", personnelReferrer); // Add personnel referrer
                        studentData.put("referrer_id", personnelID); // Add referrer_id
                        studentData.put("term", term); // Add the selected term

                        // Get current date and time to use as the document ID for the referral
                        String dateTimeId = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(new Date()) + "_" + studentId; // Append studentId

                        // Add each student's data directly to the Firestore collection
                        firestore.collection("personnel")
                                .document(personnelID) // Use personnel number as document ID
                                .collection("personnel_refferal_history") // Subcollection for the personnel's referrals
                                .document(dateTimeId) // Document ID based on date, time, and student ID
                                .set(studentData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to submit data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("Firestore", "Error adding document", e);
                                });
                    }
                }
            }
        }

        // Extract displayed students from the table and save them to Firestore
        for (int i = 0; i < detailsTable.getChildCount(); i++) {
            TableRow row = (TableRow) detailsTable.getChildAt(i);
            if (row.getChildCount() == 4) { // Ensure there are 4 children (name, program, studId, contact)
                TextView nameCell = (TextView) row.getChildAt(0);
                TextView programCell = (TextView) row.getChildAt(1);
                TextView studIdCell = (TextView) row.getChildAt(2);
                TextView contactCell = (TextView) row.getChildAt(3);

                String studentId = studIdCell.getText().toString();
                // Check if the student ID has already been added
                if (!addedStudentIdsSet.contains(studentId)) {
                    addedStudentIdsSet.add(studentId); // Mark this ID as added

                    // Create a map for the student data
                    Map<String, Object> studentData = new HashMap<>();
                    studentData.put("student_name", nameCell.getText().toString());
                    studentData.put("student_program", programCell.getText().toString());
                    studentData.put("student_id", studentId);
                    studentData.put("violation", offense);
                    studentData.put("offense", violation); // Save the offense type
                    studentData.put("date", date);
                    studentData.put("status", status);
                    studentData.put("user_concern", userConcern);
                    studentData.put("remarks", remarks); // Add remarks if needed
                    studentData.put("personnel_referrer", personnelReferrer); // Add personnel referrer
                    studentData.put("referrer_id", personnelID); // Add referrer_id
                    studentData.put("term", term); // Add the selected term

                    // Get current date and time to use as the document ID for the referral
                    String dateTimeId = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(new Date()) + "_" + studentId; // Append studentId

                    // Add each student's data directly to the Firestore collection
                    firestore.collection("personnel")
                            .document(personnelID) // Use personnel's ID as the document ID
                            .collection("personnel_refferal_history") // Subcollection for the personnel's referrals
                            .document(dateTimeId) // Document ID based on the current timestamp and student ID
                            .set(studentData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to submit data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("Firestore", "Error adding document", e);
                            });
                }
            }
        }
    }

    // Method to get the current term based on the month
    private String getCurrentTerm() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH); // January = 0, December = 11
        String currentTerm = "";

        if (currentMonth >= Calendar.AUGUST && currentMonth <= Calendar.DECEMBER) {
            currentTerm = "1st Semester"; // August - December
        } else if (currentMonth >= Calendar.JANUARY && currentMonth <= Calendar.MAY) {
            currentTerm = "2nd Semester"; // January - May
        } else if (currentMonth >= Calendar.JUNE && currentMonth <= Calendar.JULY) {
            currentTerm = "Summer"; // June - July
        }

        return currentTerm;
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
            remarksField.setError("Remarks is required");
            return false;
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
        remarksField.setText("");
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
