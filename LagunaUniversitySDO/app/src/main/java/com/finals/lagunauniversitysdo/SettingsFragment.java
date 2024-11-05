package com.finals.lagunauniversitysdo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SettingsFragment extends Fragment {
    private TableLayout violationsTable;
    private FirebaseFirestore db;
    private List<String> offenseTypes = new ArrayList<>(); // To hold the offense types
    private ArrayAdapter<String> adapter; // Adapter for spinner

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_settings, container, false);

        violationsTable = view.findViewById(R.id.violations_table);
        Button addButton = view.findViewById(R.id.add_button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up the adapter for offense types spinner
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, offenseTypes);

        // Fetch violation types from Firestore
        fetchViolationTypes();

        addButton.setOnClickListener(v -> showAddViolationDialog());

        return view;
    }

    private void fetchViolationTypes() {
        CollectionReference violationTypesRef = db.collection("violation_type");

        violationTypesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Clear previous entries to avoid duplication of offense types
                offenseTypes.clear();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String violation = document.getString("violation");
                    String type = document.getString("type");

                    if (violation != null && !violation.isEmpty() && type != null && !type.isEmpty()) {
                        offenseTypes.add(type);

                        // Check if the violation already exists in the table before adding
                        if (!violationExistsInTable(violation, type)) {
                            addViolationToTable(type, violation); // Add the violation and its type to the table
                        }
                    } else {
                        Log.e("SettingsFragment", "Violation or type is null or empty for document: " + document.getId());
                    }
                }
                Log.d("SettingsFragment", "Offense types: " + offenseTypes); // Log to check data
                adapter.notifyDataSetChanged(); // Notify adapter of data change
            } else {
                Log.e("SettingsFragment", "Error getting violation types: ", task.getException());
                Toast.makeText(getContext(), "Error getting violation types.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddViolationDialog() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_violation_prefect, null);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        // Initialize dialog components
        Spinner offenseTypeSpinner = dialogView.findViewById(R.id.spinner_offense_type);

        // Create a list of offense types
        List<String> offenseTypes = new ArrayList<>();
        offenseTypes.add("Light Offense");
        offenseTypes.add("Major Offense");
        offenseTypes.add("Serious Offense");

        // Set the adapter for the spinner with the new offense types
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, offenseTypes);
        offenseTypeSpinner.setAdapter(spinnerAdapter);

        EditText violationEditText = dialogView.findViewById(R.id.edit_violation);
        Button closeButton = dialogView.findViewById(R.id.button_close);
        Button saveButton = dialogView.findViewById(R.id.button_save);

        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Set button actions
        closeButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String selectedOffenseType = offenseTypeSpinner.getSelectedItem().toString();
            String violationText = violationEditText.getText().toString().trim();

            if (!violationText.isEmpty()) {
                // Check if the violation already exists in the table before adding
                if (!violationExistsInTable(violationText, selectedOffenseType)) {
                    addViolationToTable(violationText, selectedOffenseType);
                    saveViolationToFirestore(violationText, selectedOffenseType); // Save to Firestore
                    Toast.makeText(getContext(), "Violation saved: " + violationText, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Violation already exists.", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter a violation.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void addViolationToTable(String violation, String offenseType) {
        // Create a new TableRow
        TableRow newRow = new TableRow(getContext());

        // Create TextViews for the violation and offense type
        TextView violationTextView = new TextView(getContext());
        violationTextView.setText(violation);
        violationTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));

        TextView offenseTypeTextView = new TextView(getContext());
        offenseTypeTextView.setText(offenseType);
        offenseTypeTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

        // Create an Edit button
        Button editButton = new Button(getContext());
        editButton.setText("Edit");
        editButton.setBackgroundTintList(getResources().getColorStateList(R.color.yellow)); // Use setBackgroundTintList for API level >= 21
        editButton.setTextColor(getResources().getColor(android.R.color.black));

        // Add views to the row
        newRow.addView(violationTextView);
        newRow.addView(offenseTypeTextView);
        newRow.addView(editButton);

        // Add the new row to the TableLayout
        violationsTable.addView(newRow);

        // Set the click listener for the edit button
        editButton.setOnClickListener(v -> showEditViolationDialog(violationTextView.getText().toString(), offenseTypeTextView.getText().toString(), newRow));

        // Sort the table after adding the new row
        sortTable();
    }

    private void showEditViolationDialog(String offense, String offenseType, TableRow rowToEdit) {
        // Inflate the custom dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_violation_prefect, null);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        // Initialize dialog components
        Spinner offenseTypeSpinner = dialogView.findViewById(R.id.spinner_offense_type);

        // Create a list of offense types
        List<String> offenseTypes = new ArrayList<>();
        offenseTypes.add("Light Offense");
        offenseTypes.add("Major Offense");
        offenseTypes.add("Serious Offense");

        // Set the adapter for the spinner with the offense types
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, offenseTypes);
        offenseTypeSpinner.setAdapter(spinnerAdapter);
        offenseTypeSpinner.setSelection(spinnerAdapter.getPosition(offenseType)); // Set the spinner to the current offense type

        // Set the EditText with the existing offense text
        EditText offenseEditText = dialogView.findViewById(R.id.edit_violation);
        offenseEditText.setText(offense); // Populate the EditText with the current offense

        Button closeButton = dialogView.findViewById(R.id.button_close);
        Button saveButton = dialogView.findViewById(R.id.button_save);

        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Set button actions
        closeButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String selectedOffenseType = offenseTypeSpinner.getSelectedItem().toString();
            String updatedOffenseText = offenseEditText.getText().toString().trim();

            if (!updatedOffenseText.isEmpty()) {
                // Check if the updated offense already exists in the table before saving
                if (!violationExistsInTable(updatedOffenseText, selectedOffenseType)) {
                    // Update the row in the table
                    TextView offenseTextView = (TextView) rowToEdit.getChildAt(0);
                    TextView typeTextView = (TextView) rowToEdit.getChildAt(1);

                    offenseTextView.setText(updatedOffenseText); // Update the offense text
                    typeTextView.setText(selectedOffenseType); // Update the offense type

                    // Save the updated offense to Firestore
                    updateViolationInFirestore(offense, offenseType, updatedOffenseText, selectedOffenseType); // Use a method to update in Firestore
                    Toast.makeText(getContext(), "Offense updated: " + updatedOffenseText, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Offense already exists.", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter an offense.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }


    private void updateViolationInFirestore(String oldViolation, String oldOffenseType, String newViolation, String newOffenseType) {
        // Reference to the Firestore collection
        CollectionReference violationTypesRef = db.collection("violation_type");

        // Find the document based on old values
        violationTypesRef.whereEqualTo("violation", oldOffenseType) // Check by old violation
                .whereEqualTo("type", oldViolation) // Check by old offense type
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Update the document with the new values
                            violationTypesRef.document(document.getId()).update(
                                            "violation", newOffenseType,
                                            "type", newViolation
                                    ).addOnSuccessListener(aVoid -> {
                                        Log.d("SettingsFragment", "Violation updated successfully");
                                        Toast.makeText(getContext(), "Violation updated successfully!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Log.e("SettingsFragment", "Error updating violation: ", e));
                        }
                    } else {
                        Toast.makeText(getContext(), "Violation not found.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void sortTable() {
        List<TableRow> rows = new ArrayList<>();

        // Collect all rows into a list, excluding the header row if it exists
        for (int i = 1; i < violationsTable.getChildCount(); i++) { // Start at 1 to skip header row
            rows.add((TableRow) violationsTable.getChildAt(i));
        }

        // Sort the rows based on the violation text (first TextView)
        rows.sort((row1, row2) -> {
            TextView violationTextView1 = (TextView) row1.getChildAt(0); // Get the violation TextView in the first column
            TextView violationTextView2 = (TextView) row2.getChildAt(0);
            return violationTextView1.getText().toString().compareToIgnoreCase(violationTextView2.getText().toString());
        });

        // Clear the TableLayout
        violationsTable.removeAllViews();

        // Re-add the header row if it exists
        TableRow headerRow = createHeaderRow(); // Method to create your header row
        violationsTable.addView(headerRow);

        // Re-add the sorted rows to the TableLayout
        for (TableRow row : rows) {
            violationsTable.addView(row);
        }
    }

    private TableRow createHeaderRow() {
        // Create your header row here
        TableRow headerRow = new TableRow(getContext());

        TextView violationHeader = new TextView(getContext());
        violationHeader.setText("VIOLATION");
        violationHeader.setTextSize(16);
        violationHeader.setTypeface(null, Typeface.BOLD);
        violationHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));

        TextView typeHeader = new TextView(getContext());
        typeHeader.setText("TYPE");
        typeHeader.setTextSize(16);
        typeHeader.setTypeface(null, Typeface.BOLD);
        typeHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

        TextView actionHeader = new TextView(getContext());
        actionHeader.setText("ACTION");
        actionHeader.setTextSize(16);
        actionHeader.setTypeface(null, Typeface.BOLD);
        actionHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

        headerRow.addView(violationHeader);
        headerRow.addView(typeHeader);
        headerRow.addView(actionHeader);

        return headerRow;
    }

    private void saveViolationToFirestore(String violation, String offenseType) {
        HashMap<String, Object> violationData = new HashMap<>();
        violationData.put("violation", offenseType);
        violationData.put("type", violation);

        db.collection("violation_type").add(violationData)
                .addOnSuccessListener(documentReference -> Log.d("SettingsFragment", "Violation added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e("SettingsFragment", "Error adding violation: ", e));
    }

    private boolean violationExistsInTable(String violation, String offenseType) {
        for (int i = 1; i < violationsTable.getChildCount(); i++) { // Start at 1 to skip header
            TableRow row = (TableRow) violationsTable.getChildAt(i);
            TextView violationTextView = (TextView) row.getChildAt(0);
            TextView offenseTypeTextView = (TextView) row.getChildAt(1);

            if (violationTextView.getText().toString().equalsIgnoreCase(violation) &&
                    offenseTypeTextView.getText().toString().equalsIgnoreCase(offenseType)) {
                return true; // Violation already exists
            }
        }
        return false; // Violation does not exist
    }
}
