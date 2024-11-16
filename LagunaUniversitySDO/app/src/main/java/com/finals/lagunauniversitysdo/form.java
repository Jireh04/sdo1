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
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import android.widget.ImageButton;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.widget.CheckBox;


public class form extends AppCompatActivity {

    private Spinner violationSpinner;
    private EditText dateField, nameField, emailField, contactField, programField, remarksField, termField;
    private TextView violatorsName, violatorsProgram, violatorsStudID, violatorsContact;
    private TableLayout detailsTable;
    private FirebaseFirestore firestore;
    private String scannedName;
    private String scannedContact;
    private String scannedStudentNo;
    private String scannedProgram;
    private String studentReferrer; // Declare a variable to hold the student referrer
    private String selectedType = null;
    private String firstName, lastName, studentId;

    // Keys for intent extras
    private static final String STUDENT_NAME = "NAME";
    private static final String EMAIL_KEY = "EMAIL";
    private static final String CONTACT_NUM_KEY = "CONTACT_NUM";
    private static final String PROGRAM_KEY = "PROGRAM";
    private static final String TERM_KEY = "TERM";
    private static final String STUDENT_ID_KEY = "STUDENT_ID";
    private ArrayList<Map<String, String>> scannedStudentDataList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);


        // Initialize UI elements and Firestore
        initializeUIElements();
        firestore = FirebaseFirestore.getInstance();

        // Populate fields from intent
        populateFieldsFromIntent();

        // Set current date and time
        setCurrentDateTime();

        // Set up spinner options
        setupSpinners();

        // Retrieve and display previously added students
        retrieveAndDisplayAddedStudents();

        // Set up submit button
        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> saveData());

        setupTermText();

    }

    // Declare the lists for storing student IDs and details
    List<String> addedUserIds = new ArrayList<>();
    List<Map<String, String>> addedUserDetails = new ArrayList<>();

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
        Intent intent = getIntent();

        // Retrieve standard student data from the intent
        String studentName = intent.getStringExtra("STUDENT_NAME");
        String studentEmail = intent.getStringExtra(EMAIL_KEY);
        Long studentContact = intent.getLongExtra(CONTACT_NUM_KEY, 0L);
        String studentProgram = intent.getStringExtra(PROGRAM_KEY);
        studentId = intent.getStringExtra(STUDENT_ID_KEY);

        // Logging the received data for debugging purposes
        Log.d("FormActivity", "Received Student Name: " + studentName);
        Log.d("FormActivity", "Received Student Email: " + studentEmail);

        // Populate the fields with the received data
        nameField.setText(studentName != null ? studentName : "");
        emailField.setText(studentEmail != null ? studentEmail : "");
        contactField.setText(studentContact != 0L ? String.valueOf(studentContact) : "");
        programField.setText(studentProgram != null ? studentProgram : "");


        this.studentReferrer = UserSession.getStudentName();
        Log.d("FormActivity", "Student Referrer: " + this.studentReferrer);

        // Retrieve and process multiple scanned data if available
        ArrayList<String> scannedDataList = intent.getStringArrayListExtra("scannedDataList");
        if (scannedDataList != null && !scannedDataList.isEmpty()) {
            Log.d("FormActivity", "Scanned Data Found: " + scannedDataList); // Debugging line
            processMultipleScannedData(scannedDataList);
        } else {
            Log.w("FormActivity", "No scanned data found in the intent"); // Warning log
            Toast.makeText(this, "No scanned data found", Toast.LENGTH_SHORT).show();
        }

        // Optionally, process a single scanned data item if needed
        String scannedData = intent.getStringExtra("scannedData");
        if (scannedData != null) {
            processScannedData(scannedData); // Process the scanned data
        }
    }

    private void processMultipleScannedData(ArrayList<String> scannedDataList) {
        // Create a set to track student numbers and prevent duplicates
        Set<String> uniqueStudentNos = new HashSet<>();

        for (String scannedData : scannedDataList) {
            String[] scannedFields = scannedData.split("\n");

            if (scannedFields.length >= 3) {
                String studentNo = scannedFields[0];  // Assuming the first line is the student number
                String scannedName = scannedFields[1];  // Assuming the second line is the scanned name
                String block = scannedFields[2];  // Assuming the third line is the block

                // Check if the student has already been added based on student number
                if (!uniqueStudentNos.contains(studentNo)) {
                    // Display the student details in the table
                    displayStudentDetails(scannedName, block, studentNo, ""); // Pass an empty string for contact

                    // Add the student number to the set to track uniqueness
                    uniqueStudentNos.add(studentNo);

                    // Store the scanned data in a list for later use
                    Map<String, String> studentData = new HashMap<>();
                    studentData.put("student_no", studentNo);
                    studentData.put("student_name", scannedName);
                    studentData.put("student_program", block);
                    this.scannedStudentDataList.add(studentData); // Maintain the list of scanned data

                    // Optional: Display the latest scanned data in the input fields
                    this.scannedProgram = block; // Store block instead of program

                    // Retrieve student data from UserSession
                    String studentName = UserSession.getStudentName();
                    String studentEmail = UserSession.getEmail();
                    Long studentContact = UserSession.getContactNum();
                    String studentProgram = UserSession.getProgram();
                    studentId = UserSession.getStudentId();

                    // Populate fields with standard student data
                    nameField.setText(studentName != null ? studentName : "");
                    emailField.setText(studentEmail != null ? studentEmail : "");
                    contactField.setText(studentContact != null ? String.valueOf(studentContact) : "");
                    programField.setText(studentProgram != null ? studentProgram : "");

                    // Store the latest scanned data for submission
                    this.scannedName = scannedName; // Store scanned name for later use
                    this.scannedContact = studentContact != null ? String.valueOf(studentContact) : ""; // Store scanned contact
                    this.scannedStudentNo = studentNo; // Store student number
                }
            } else {
                Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_LONG).show();
            }
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
            studentId = UserSession.getStudentId();

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

            // Create a map to hold the current scanned student's data
            Map<String, String> studentData = new HashMap<>();
            studentData.put("student_no", studentNo);
            studentData.put("student_name", scannedName);
            studentData.put("student_program", block);

            // Add the current student's data to the list
            scannedStudentDataList.add(studentData); // Maintain the list of scanned data

        } else {
            Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_LONG).show();
        }
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
    // Define a class to hold student details
    class StudentDetails {
        String name;
        String program;
        String contact;

        public StudentDetails(String name, String program, String contact) {
            this.name = name;
            this.program = program;
            this.contact = contact;
        }

        // Getters (you can add setters if needed)
        public String getName() {
            return name;
        }

        public String getProgram() {
            return program;
        }

        public String getContact() {
            return contact;
        }
    }

    // Initialize the HashMap to store students by their ID
    private HashMap<String, StudentDetails> studentMap = new HashMap<>();

    private void displayStudentDetails(String name, String program, String studId, String contact) {
        TableRow row = new TableRow(this);

        // Create TextViews for the student's details
        TextView nameCell = new TextView(this);
        TextView programCell = new TextView(this);
        TextView studIdCell = new TextView(this);
        TextView contactCell = new TextView(this);

        // Set the text for the cells
        nameCell.setText(name);
        programCell.setText(program);
        studIdCell.setText(studId);
        contactCell.setText(contact);

        // Add the text views to the row
        row.addView(nameCell);
        row.addView(programCell);
        row.addView(studIdCell);
        row.addView(contactCell);

        // Create delete button
        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setImageResource(R.drawable.baseline_delete_outline_24); // Set the delete icon
        deleteButton.setBackgroundResource(android.R.color.transparent); // Remove default background
        deleteButton.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT
        ));

        // Set the delete button click listener
        deleteButton.setOnClickListener(v -> {
            // Remove the row from the TableLayout
            detailsTable.removeView(row);

            // Remove student from the studentMap using student ID
            studentMap.remove(studId);  // This will not affect other students

            // Show a toast message to inform the user
            Toast.makeText(this, name + " removed", Toast.LENGTH_SHORT).show();

            // Optionally disable submit button if necessary
            toggleSubmitButton();
        });

        // Add the delete button to the row
        row.addView(deleteButton);

        // Finally, add the row to the TableLayout
        detailsTable.addView(row);

        // Save the student in the map
        studentMap.put(studId, new StudentDetails(name, program, contact));
    }

    // Method to enable or disable the submit button based on the presence of students
    private void toggleSubmitButton() {
        Button submitButton = findViewById(R.id.submit_button);
        if (studentMap.isEmpty()) {
            submitButton.setEnabled(false);  // Disable submit if no students are left
        } else {
            submitButton.setEnabled(true);  // Enable submit if there are students in the map
        }
    }

    // Submit logic: Here we can get the remaining students in the map and save/submit them
    private void submitStudentDetails() {
        // For example, you could print out the remaining students:
        for (String studId : studentMap.keySet()) {
            StudentDetails student = studentMap.get(studId);
            // Handle the submission logic (e.g., sending to server or saving locally)
            // You can access student.name, student.program, student.contact
        }
        // Show confirmation or proceed with further submission process
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
        String studId = studentId; // Assuming this is the student's ID being referred
        String status = "pending"; // Default status

        // Retrieve remarks from remarks_field
        String remarks = ((EditText) findViewById(R.id.remarks_field)).getText().toString().trim();

        // Retrieve violation and type from CheckboxSpinnerAdapter
        CheckboxSpinnerAdapter adapter = (CheckboxSpinnerAdapter) violationSpinner.getAdapter();
        String violation = adapter.getSelectedViolation();
        String selectedType = adapter.getSelectedType();

        if (violation == null || violation.equals("Select a Violation")) {
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

        // Get the current term using setupTermText method
        String selectedTerm = getCurrentTerm();

        // Generate dateTimeId for the main student data here

        // Retrieve previously added students for submission
        ArrayList<String> addedUserNames = getIntent().getStringArrayListExtra("ADDED_USER_NAMES");
        ArrayList<String> addedUserContacts = getIntent().getStringArrayListExtra("ADDED_USER_CONTACTS");
        ArrayList<String> addedUserPrograms = getIntent().getStringArrayListExtra("ADDED_USER_PROGRAMS");
        ArrayList<String> addedUserStudentIds = getIntent().getStringArrayListExtra("ADDED_USER_STUDENT_IDS");

        // Check if there are added students and add them to Firestore
        if (addedUserNames != null && addedUserContacts != null && addedUserPrograms != null && addedUserStudentIds != null) {
            for (int i = 0; i < addedUserNames.size(); i++) {
                // Concatenate added student ID to dateTimeId
                String addedStudentId = addedUserStudentIds.get(i);
                String dateTimeId = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(new Date()) + "_" + addedStudentId;

                Map<String, Object> studentData = new HashMap<>();
                studentData.put("student_name", addedUserNames.get(i));
                studentData.put("student_program", addedUserPrograms.get(i));
                studentData.put("student_id", addedStudentId); // Use added student ID
                studentData.put("offense", violation);  // Save violation in "offense" field
                studentData.put("violation", selectedType);  // Save selected type in "violation" field
                studentData.put("date", date);
                studentData.put("status", status);
                studentData.put("user_concern", userConcern); // Add user concern to the student data
                studentData.put("remarks", remarks); // Add remarks to the student data
                studentData.put("student_referrer", studentReferrer); // Add student_referrer to Firestore data
                studentData.put("referrer_id", studId); // Add referrer_id to Firestore data
                studentData.put("term", selectedTerm); // Add the selected term to Firestore data

                Log.d("Firestore", "Student ID: " + addedStudentId);

                firestore.collection("students")
                        .document(studId) // Use the main student number as document ID
                        .collection("student_refferal_history") // Subcollection for the student's referrals
                        .document(dateTimeId) // Document ID based on date, time, and added student ID
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

        // Add the scanned data to Firestore as well using the scanned student ID
        for (Map<String, String> scannedData : scannedStudentDataList) { // Loop through all scanned students
            String scannedName = scannedData.get("student_name");
            String scannedStudentNo = scannedData.get("student_no");
            String scannedProgram = scannedData.get("student_program");

            // Generate a unique dateTimeId for each scanned student
            String scannedDateTimeId = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(new Date()) + "_" + scannedStudentNo;

            Map<String, Object> scannedStudentData = new HashMap<>();
            scannedStudentData.put("student_name", scannedName);
            scannedStudentData.put("student_program", scannedProgram);
            scannedStudentData.put("student_id", scannedStudentNo); // Use scanned student number
            scannedStudentData.put("offense", violation);  // Save violation in "offense" field
            scannedStudentData.put("violation", selectedType);  // Save selected type in "violation" field
            scannedStudentData.put("date", date);
            scannedStudentData.put("status", status);
            scannedStudentData.put("user_concern", userConcern); // Add user concern to scanned student data
            scannedStudentData.put("remarks", remarks); // Add remarks to scanned student data
            scannedStudentData.put("student_referrer", studentReferrer); // Add student_referrer to Firestore data
            scannedStudentData.put("referrer_id", studId); // Add referrer_id to scanned student data
            scannedStudentData.put("term", selectedTerm); // Add the selected term to Firestore data

            // Save each scanned student's data into their respective sub-collection
            firestore.collection("students")
                    .document(studId) // Use the main student ID as document ID
                    .collection("student_refferal_history") // Subcollection for the scanned students' referrals
                    .document(scannedDateTimeId) // Use a unique dateTimeId for each scanned student
                    .set(scannedStudentData)
                    .addOnSuccessListener(aVoid -> {
                        // Notify success for each scanned student
                        Toast.makeText(this, "Scanned student data submitted successfully", Toast.LENGTH_SHORT).show();
                        // After successful submission, navigate to ReferralFormActivity
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to submit scanned student data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error adding scanned student document", e);
                    });
        }
    }

    private String getCurrentTerm() {
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


    // Clear input fields
    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        contactField.setText("");
        programField.setText("");
        violationSpinner.setSelection(0);
        setCurrentDateTime();
    }
}

