package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class personnel_myRefferalForm extends Fragment {

    private FirebaseFirestore db; // Firestore instance
    private TableLayout tableLayout; // TableLayout to display data
    private String personnelId; // ID of the logged-in personnel
    private String personnelName; // Full name of the logged-in personnel

    public personnel_myRefferalForm() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Get the currently logged-in personnel's ID and name from PersonnelSession
        personnelId = PersonnelSession.getPersonnelId();  // Assuming you have a PersonnelSession class
        personnelName = PersonnelSession.getPersonnelName();  // Fetch the personnel's full name from session
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_personnel_my_refferal_form, container, false);

        // Reference to TableLayout in the XML layout
        tableLayout = view.findViewById(R.id.tableLayout);

        // Fetch and display referral data specific to the logged-in personnel
        fetchReferralData();

        return view;
    }

    private void fetchReferralData() {
        // Clear any existing rows to avoid duplicates
        tableLayout.removeAllViews();

        // Ensure personnelName is not null
        if (personnelName == null || personnelName.isEmpty()) {
            Toast.makeText(getContext(), "Unable to fetch personnel name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch data from Firestore 'personnel_refferal_history' collection for the logged-in personnel
        db.collection("personnel_refferal_history")
                .whereEqualTo("personnel_referrer", personnelName) // Filter by the logged-in personnel's full name
                .get()
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.firestore.QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots = task.getResult();
                            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                // Iterate through the Firestore documents
                                for (DocumentSnapshot document : queryDocumentSnapshots) {
                                    // Extract the data for each document
                                    String dateReported = document.getString("date");
                                    String personnelNo = document.getString("student_id"); // Ensure this key matches what's in Firestore
                                    String name = document.getString("student_name"); // Ensure this key matches what's in Firestore
                                    String status = document.getString("status");

                                    // Add data as a new row in the table
                                    addTableRow(dateReported, personnelNo, name, status);
                                }
                            } else {
                                Toast.makeText(getContext(), "No referral data found for " + personnelName, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle any errors that occur while fetching data
                            Toast.makeText(getContext(), "Error fetching data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Function to add a row dynamically to the table
    private void addTableRow(String dateReported, String personnelNo, String name, String status) {
        // Create a new TableRow
        TableRow tableRow = new TableRow(getContext());

        // Create TextViews for each column
        TextView dateReportedTextView = new TextView(getContext());
        dateReportedTextView.setText(dateReported);
        dateReportedTextView.setPadding(8, 8, 8, 8);

        TextView personnelNoTextView = new TextView(getContext());
        personnelNoTextView.setText(personnelNo);
        personnelNoTextView.setPadding(8, 8, 8, 8);

        TextView nameTextView = new TextView(getContext());
        nameTextView.setText(name);
        nameTextView.setPadding(8, 8, 8, 8);

        TextView statusTextView = new TextView(getContext());
        statusTextView.setText(status);
        statusTextView.setPadding(8, 8, 8, 8);

        // Add the TextViews to the TableRow
        tableRow.addView(dateReportedTextView);
        tableRow.addView(personnelNoTextView);
        tableRow.addView(nameTextView);
        tableRow.addView(statusTextView);

        // Add the TableRow to the TableLayout
        tableLayout.addView(tableRow);
    }
}
