package com.finals.lagunauniversitysdo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private EditText dateField, remarksField;
    private TextView violatorsName, violatorsProgram, violatorsStudID, violatorsContact;
    private TableLayout detailsTable;
    private FirebaseFirestore firestore;

    // Prefect-related fields
    private String prefectName;

    // Keys for intent extras
    private static final String STUDENT_NAME_KEY = "STUDENT_NAME";
    private static final String PROGRAM_KEY = "PROGRAM";
    private static final String STUD_ID_KEY = "STUD_ID";
    private static final String PREFECT_NAME_KEY = "PREFECT_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefect_form);

        // Initialize UI elements and Firestore
        initializeUIElements();
        firestore = FirebaseFirestore.getInstance();

        // Set up spinner options
        setupSpinners();

        // Set current date and time
        setCurrentDateTime();

        // Populate prefect data
        populatePrefectData();

        // Set up submit button
        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> saveData());
    }

    // Initialize UI elements
    private void initializeUIElements() {
        termSpinner = findViewById(R.id.term_spinner);
        violationSpinner = findViewById(R.id.violation_spinner);
        dateField = findViewById(R.id.date_field);
        remarksField = findViewById(R.id.remarks_field);
        detailsTable = findViewById(R.id.details_table);
        violatorsName = findViewById(R.id.violators_name);
        violatorsProgram = findViewById(R.id.violators_program);
        violatorsStudID = findViewById(R.id.violators_studID);
        violatorsContact = findViewById(R.id.violators_contact);
    }

    // Populate data for the prefect from intent
    private void populatePrefectData() {
        Intent intent = getIntent();
        prefectName = intent.getStringExtra(PREFECT_NAME_KEY);

        // You can also populate the prefect's other fields if necessary
    }

    // Set up spinner options
    private void setupSpinners() {
        String[] terms = {"First Sem", "Second Sem", "Summer"};
        String[] violations = {"Light Offense", "Major Offense", "Serious Offense"};

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

    // Save data to Firestore
    private void saveData() {
        // Retrieve values from input fields
        String name = violatorsName.getText().toString().trim();
        String program = violatorsProgram.getText().toString().trim();
        String studId = violatorsStudID.getText().toString().trim();
        String contact = violatorsContact.getText().toString().trim();
        String term = termSpinner.getSelectedItem().toString();
        String violation = violationSpinner.getSelectedItem().toString();
        String date = dateField.getText().toString().trim();
        String remarks = remarksField.getText().toString().trim();
        String status = "Pending";  // Default status

        // Validate inputs
        if (!validateInputs(name, program, studId, contact)) {
            return;  // Exit if validation fails
        }

        // Create a Map to store data
        Map<String, Object> violationData = new HashMap<>();
        violationData.put("student_name", name);
        violationData.put("student_program", program);
        violationData.put("student_id", studId);
        violationData.put("student_contact", contact);
        violationData.put("term", term);
        violationData.put("violation", violation);
        violationData.put("date", date);
        violationData.put("remarks", remarks);
        violationData.put("status", status);
        violationData.put("prefect_name", prefectName);  // Store the prefect's name

        // Save data to Firestore
        firestore.collection("prefect_violation_history")
                .add(violationData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Violation saved successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save violation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error adding document", e);
                });
    }

    // Validate inputs
    private boolean validateInputs(String name, String program, String studId, String contact) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Student name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(program)) {
            Toast.makeText(this, "Student program is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(studId)) {
            Toast.makeText(this, "Student ID is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(contact)) {
            Toast.makeText(this, "Student contact is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Clear fields after saving
    private void clearFields() {
        violatorsName.setText("");
        violatorsProgram.setText("");
        violatorsStudID.setText("");
        violatorsContact.setText("");
        remarksField.setText("");
    }
}
