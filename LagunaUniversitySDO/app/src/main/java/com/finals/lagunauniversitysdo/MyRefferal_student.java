package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyRefferal_student extends Fragment {

    private FirebaseFirestore db; // Firestore instance
    private TableLayout tableLayout; // TableLayout to display data

    public MyRefferal_student() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_refferal_student, container, false);

        // Reference to TableLayout in the XML layout
        tableLayout = view.findViewById(R.id.tableLayout);

        // Fetch and display data from Firestore
        fetchRefferalData();

        return view;
    }

    private void fetchRefferalData() {
        // Clear any existing rows to avoid duplicates
        tableLayout.removeAllViews();

        // Fetch data from Firestore 'student_refferal_history' collection
        db.collection("student_refferal_history")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Iterate through the Firestore documents
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            // Extract the data for each document
                            String dateReported = document.getString("date");
                            String studentNo = document.getString("student_id"); // Ensure this key matches what's in Firestore
                            String name = document.getString("student_name"); // Ensure this key matches what's in Firestore
                            String status = document.getString("status");

                            // Add data as a new row in the table
                            addTableRow(dateReported, studentNo, name, status);
                        }
                    } else {
                        Toast.makeText(getContext(), "No data found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur while fetching data
                    Toast.makeText(getContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Function to add a row dynamically to the table
    private void addTableRow(String dateReported, String studentNo, String name, String status) {
        // Create a new TableRow
        TableRow tableRow = new TableRow(getContext());

        // Create TextViews for each column
        TextView dateReportedTextView = new TextView(getContext());
        dateReportedTextView.setText(dateReported);
        dateReportedTextView.setPadding(8, 8, 8, 8);

        TextView studentNoTextView = new TextView(getContext());
        studentNoTextView.setText(studentNo);
        studentNoTextView.setPadding(8, 8, 8, 8);

        TextView nameTextView = new TextView(getContext());
        nameTextView.setText(name);
        nameTextView.setPadding(8, 8, 8, 8);

        TextView statusTextView = new TextView(getContext());
        statusTextView.setText(status);
        statusTextView.setPadding(8, 8, 8, 8);

        // Add the TextViews to the TableRow
        tableRow.addView(dateReportedTextView);
        tableRow.addView(studentNoTextView);
        tableRow.addView(nameTextView);
        tableRow.addView(statusTextView);

        // Add the TableRow to the TableLayout
        tableLayout.addView(tableRow);
    }
}
