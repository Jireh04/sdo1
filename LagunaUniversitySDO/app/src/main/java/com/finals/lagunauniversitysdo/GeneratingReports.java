package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratingReports extends Fragment {

    private Spinner programSpinner, statusSpinner, academicYearSpinner, semesterSpinner, offenseTypeSpinner;
    private EditText dateRangeEditText;
    private Button generateByNameButton, generateByDateButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_reports_prefect, container, false);

        // Initialize the spinners and buttons
        programSpinner = view.findViewById(R.id.program_spinner);
        statusSpinner = view.findViewById(R.id.status_spinner);
        academicYearSpinner = view.findViewById(R.id.academicYearSpinner);
        semesterSpinner = view.findViewById(R.id.semesterSpinner);
        offenseTypeSpinner = view.findViewById(R.id.offenseTypeSpinner);
        dateRangeEditText = view.findViewById(R.id.dateRangeEditText);

        generateByNameButton = view.findViewById(R.id.group_name);
        generateByDateButton = view.findViewById(R.id.group_date);

        // Set onClick listeners for generating reports
        generateByNameButton.setOnClickListener(v -> generateReport("name"));
        generateByDateButton.setOnClickListener(v -> generateReport("date"));

        return view;
    }

    private void generateReport(String groupBy) {
        // Call a method to filter data and generate a report based on the groupBy parameter
        queryFirestoreData(groupBy);
    }

    private void queryFirestoreData(String groupBy) {
        CollectionReference collectionRef = FirebaseFirestore.getInstance()
                .collection("students")
                .document("studentID")
                .collection("student_refferal_history");

        Query query = collectionRef;

        // Add filters based on spinner selections
        String selectedProgram = programSpinner.getSelectedItem().toString();
        if (!selectedProgram.equals("All")) {
            query = query.whereEqualTo("student_program", selectedProgram);
        }

        String selectedStatus = statusSpinner.getSelectedItem().toString();
        if (!selectedStatus.equals("All")) {
            query = query.whereEqualTo("status", selectedStatus.equals("Settled") ? "Accepted_Status" : "Unsettled");
        }

        String selectedAcademicYear = academicYearSpinner.getSelectedItem().toString();
        if (!selectedAcademicYear.equals("All")) {
            query = query.whereEqualTo("academic_year", selectedAcademicYear);
        }

        String selectedSemester = semesterSpinner.getSelectedItem().toString();
        if (!selectedSemester.equals("All")) {
            query = query.whereEqualTo("semester", selectedSemester);
        }

        String selectedOffense = offenseTypeSpinner.getSelectedItem().toString();
        if (!selectedOffense.equals("All")) {
            query = query.whereEqualTo("violation", selectedOffense);
        }

        // Execute the query and process results
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                // Process and display the documents or use them to generate a report
                if (groupBy.equals("name")) {
                    generateGroupedReportByName(documents);
                } else if (groupBy.equals("date")) {
                    generateGroupedReportByDate(documents);
                }
            } else {
                Log.d("Firestore Query", "Error getting documents: ", task.getException());
            }
        });
    }

    private void generateGroupedReportByName(List<DocumentSnapshot> documents) {
        // Create a map to store the count of violations per student
        Map<String, Integer> reportByName = new HashMap<>();

        for (DocumentSnapshot document : documents) {
            // Assuming the student's name is stored in "student_name" field
            String studentName = document.getString("student_name");
            if (studentName != null) {
                // Increment the count for this student
                reportByName.put(studentName, reportByName.getOrDefault(studentName, 0) + 1);
            }
        }

        // Now, you can use this reportByName map to display the data in a list or table
        // Example: Log or Toast the result
        StringBuilder reportText = new StringBuilder("Report Grouped by Name:\n");
        for (Map.Entry<String, Integer> entry : reportByName.entrySet()) {
            reportText.append(entry.getKey()).append(": ").append(entry.getValue()).append(" violations\n");
        }
        Log.d("GroupedByNameReport", reportText.toString());

        // Optionally, you can display this in a UI element, such as a TextView or RecyclerView.
        Toast.makeText(getContext(), reportText.toString(), Toast.LENGTH_LONG).show();
    }

    private void generateGroupedReportByDate(List<DocumentSnapshot> documents) {
        // Create a map to store the count of violations per date
        Map<String, Integer> reportByDate = new HashMap<>();

        for (DocumentSnapshot document : documents) {
            // Assuming the date of violation is stored in "date_reported" field in format "yyyy-MM-dd"
            String dateReported = document.getString("date");
            if (dateReported != null) {
                // Increment the count for this date
                reportByDate.put(dateReported, reportByDate.getOrDefault(dateReported, 0) + 1);
            }
        }

        // Now, you can use this reportByDate map to display the data in a list or table
        // Example: Log or Toast the result
        StringBuilder reportText = new StringBuilder("Report Grouped by Date:\n");
        for (Map.Entry<String, Integer> entry : reportByDate.entrySet()) {
            reportText.append(entry.getKey()).append(": ").append(entry.getValue()).append(" violations\n");
        }
        Log.d("GroupedByDateReport", reportText.toString());

        // Optionally, you can display this in a UI element, such as a TextView or RecyclerView.
        Toast.makeText(getContext(), reportText.toString(), Toast.LENGTH_LONG).show();
    }

}
