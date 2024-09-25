package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class form extends AppCompatActivity {

    private Spinner termSpinner;
    private Spinner violationSpinner;
    private EditText dateField, nameField, emailField, contactField, programField;
    private TableLayout detailsTable;
    private DatabaseReference databaseReference;


    private static final String STUDENT_NAME_KEY = "STUDENT_NAME";
    private static final String EMAIL_KEY = "EMAIL";
    private static final String CONTACT_NUM_KEY = "CONTACT_NUM";
    private static final String PROGRAM_KEY = "PROGRAM";
    private static final String ADDED_USER_NAMES_KEY = "ADDED_USER_NAMES";
    private static final String ADDED_USER_CONTACTS_KEY = "ADDED_USER_CONTACTS";
    private static final String ADDED_USER_PROGRAMS_KEY = "ADDED_USER_PROGRAMS";
    private static final String STUD_ID_KEY = "STUD_ID"; // Add key for stud_id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);


        initializeUIElements();


        populateFieldsFromIntent();

        setupSpinners();


        databaseReference = FirebaseDatabase.getInstance().getReference("Students");

        setCurrentDateTime();


        retrieveAndDisplayAddedStudents();
    }


    private void initializeUIElements() {
        termSpinner = findViewById(R.id.term_spinner);
        violationSpinner = findViewById(R.id.violation_spinner);
        dateField = findViewById(R.id.date_field);
        nameField = findViewById(R.id.name_field);
        emailField = findViewById(R.id.email_field);
        contactField = findViewById(R.id.contact_field);
        programField = findViewById(R.id.department_field);
        detailsTable = findViewById(R.id.details_table);
    }

    private void populateFieldsFromIntent() {
        String studentName = getIntent().getStringExtra(STUDENT_NAME_KEY);
        String studentEmail = getIntent().getStringExtra(EMAIL_KEY);
        Long studentContact = getIntent().getLongExtra(CONTACT_NUM_KEY, 0L);
        String studentProgram = getIntent().getStringExtra(PROGRAM_KEY);

        String studId = getIntent().getStringExtra(STUD_ID_KEY); // Use the constant

        nameField.setText(studentName != null ? studentName : "");
        emailField.setText(studentEmail != null ? studentEmail : "");
        contactField.setText(studentContact != 0L ? String.valueOf(studentContact) : "");
        programField.setText(studentProgram != null ? studentProgram : "");
    }


    private void setupSpinners() {
        String[] terms = {"First Term", "Second Term", "Summer", "Special Term"};
        String[] violations = {"Light Offense", "Serious Offense", "Major Offense"};

        setupSpinner(termSpinner, terms);
        setupSpinner(violationSpinner, violations);
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    private void setCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -2); // Adjust for timezone if needed

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentDateTime = dateFormat.format(calendar.getTime());
        dateField.setText(currentDateTime);
    }


    private void retrieveAndDisplayAddedStudents() {
        ArrayList<String> addedUserNames = getIntent().getStringArrayListExtra(ADDED_USER_NAMES_KEY);
        ArrayList<String> addedUserContacts = getIntent().getStringArrayListExtra(ADDED_USER_CONTACTS_KEY);
        ArrayList<String> addedUserPrograms = getIntent().getStringArrayListExtra(ADDED_USER_PROGRAMS_KEY);
        ArrayList<String> addedUserStudIds = getIntent().getStringArrayListExtra("ADDED_USER_STUD_IDS"); // Retrieve stud_id

        if (addedUserNames != null && addedUserContacts != null && addedUserPrograms != null && addedUserStudIds != null) {
            for (int i = 0; i < addedUserNames.size(); i++) {
                String name = addedUserNames.get(i);
                String contact = addedUserContacts.get(i);
                String program = addedUserPrograms.get(i);
                String studId = addedUserStudIds.get(i); // Get stud_id

                displayStudentDetails(studId, name, contact, program); // Include stud_id in the display
            }
        }
    }


    private void saveData() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String contactString = contactField.getText().toString().trim();
        String program = programField.getText().toString().trim();
        String term = termSpinner.getSelectedItem().toString();
        String violation = violationSpinner.getSelectedItem().toString();
        String date = dateField.getText().toString();


        if (!validateFields(name, email, contactString, program)) return;


        Long contactNumber = parseContactNumber(contactString);
        if (contactNumber == null) return; // Error message already shown


        Map<String, Object> studentData = new HashMap<>();
        studentData.put("name", name);
        studentData.put("email", email);
        studentData.put("contact", contactNumber);
        studentData.put("program", program);
        studentData.put("term", term);
        studentData.put("violation", violation);
        studentData.put("date", date);

        // Retrieve stud_id from intent to save
        String studId = getIntent().getStringExtra(STUD_ID_KEY); // Use the constant
        studentData.put("stud_id", studId); // Save stud_id to Firebase


        databaseReference.push().setValue(studentData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(form.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                        displayStudentDetails(studId, name, contactString, program); // Display with stud_id
                    } else {
                        Toast.makeText(form.this, "Failed to save data", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private boolean validateFields(String name, String email, String contact, String program) {
        if (TextUtils.isEmpty(name)) {
            nameField.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Email is required");
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


    private Long parseContactNumber(String contact) {
        try {
            return Long.parseLong(contact);
        } catch (NumberFormatException e) {
            contactField.setError("Invalid contact number");
            return null;
        }
    }


    private void retrieveAndDisplayAllStudents() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                detailsTable.removeAllViews();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String studId = snapshot.child("stud_id").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String contact = snapshot.child("contact").getValue(Long.class).toString();
                    String program = snapshot.child("program").getValue(String.class);


                    displayStudentDetails(studId, name, contact, program);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(form.this, "Failed to retrieve data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update displayStudentDetails method to include stud_id
    private void displayStudentDetails(String studId, String name, String contact, String program) {
        TableRow newRow = new TableRow(this);
        newRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));


        addTextViewToRow(newRow, name);


        addTextViewToRow(newRow, program);


        addTextViewToRow(newRow, studId);


        addTextViewToRow(newRow, contact);


        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setImageResource(R.drawable.baseline_delete_outline_24);
        deleteButton.setBackgroundResource(android.R.color.transparent);
        deleteButton.setOnClickListener(v -> {
            detailsTable.removeView(newRow);
            Toast.makeText(form.this, name + " removed", Toast.LENGTH_SHORT).show();
        });

        newRow.addView(deleteButton);
        detailsTable.addView(newRow);
    }


    private void addTextViewToRow(TableRow row, String text) {
        TextView textView = new TextView(form.this); // Ensure 'form.this' is used for context
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        row.addView(textView);
    }
}
