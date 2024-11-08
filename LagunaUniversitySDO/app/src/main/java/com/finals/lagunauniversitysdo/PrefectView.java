package com.finals.lagunauniversitysdo;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
public class PrefectView extends Fragment {

    private TextView studentNameTextView, studentIdTextView, studentProgramTextView, studentContactTextView, studentYearTextView, studentBlockTextView;
    private TableLayout violationTable;

    // Static method to create a new instance of PrefectView fragment
    public static PrefectView newInstance(String studentId, String name, String program, String contact, String year, String block,
                                          String violations, String remarks, String date) {
        PrefectView fragment = new PrefectView();
        Bundle args = new Bundle();

        // Add all the parameters to the arguments
        args.putString("student_id", studentId);
        args.putString("student_name", name);
        args.putString("student_program", program);
        args.putString("student_contact", contact);
        args.putString("student_year", year);
        args.putString("student_block", block);
        args.putString("violations", violations);
        args.putString("remarks", remarks);
        args.putString("date", date);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.prefect_view, container, false);

        // Initialize Firebase Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        studentNameTextView = view.findViewById(R.id.studentNameTextView);
        studentIdTextView = view.findViewById(R.id.studentIDTextView);
        studentProgramTextView = view.findViewById(R.id.studentProgramTextView);
        studentContactTextView = view.findViewById(R.id.studentContactTextView);
        studentYearTextView = view.findViewById(R.id.studentYearTextView);
        studentBlockTextView = view.findViewById(R.id.studentBlockTextView);
        violationTable = view.findViewById(R.id.violationTable);

        // Retrieve arguments passed to the fragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            String studentName = arguments.getString("student_name");
            String studentId = arguments.getString("student_id");
            String studentProgram = arguments.getString("student_program");
            String studentContact = arguments.getString("student_contact");
            String studentYear = arguments.getString("student_year");
            String studentBlock = arguments.getString("student_block");

            // Set the received data to TextViews
            studentNameTextView.setText("Name: " + studentName);
            studentIdTextView.setText("ID: " + studentId);
            studentProgramTextView.setText("Program: " + studentProgram);
            studentContactTextView.setText("Contact No: " + studentContact);
            studentYearTextView.setText("Year: " + studentYear);
            studentBlockTextView.setText("Block: " + studentBlock);

            // Fetch violations, remarks, and date data based on student ID
            fetchViolationsData(studentId);
        }

        return view;
    }

    // Method to fetch violation, remarks, and date data from Firestore based on studentId
    private void fetchViolationsData(String studentId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Get the reference to the specific student's document in the 'students' collection
        firestore.collection("students") // The students collection
                .document(studentId) // The student document using the studentId
                .collection("accepted_status") // The 'student_refferal_history' subcollection
                .get() // Fetch all documents in the subcollection
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check if any documents are found
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Loop through the documents and extract the violation, remarks, and date
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String violation = documentSnapshot.getString("violation");
                            String remarks = documentSnapshot.getString("remarks");
                            String date = documentSnapshot.getString("date");

                            // Populate the TableLayout with the fetched data
                            populateViolationTable(violation, remarks, date);
                        }
                    } else {
                        Toast.makeText(getContext(), "No violation records found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to populate the TableLayout or other UI elements with violation data
    private void populateViolationTable(String violation, String remarks, String date) {
        if (violation != null && !violation.isEmpty()) {
            // Create a new TableRow for each violation record
            TableRow row = new TableRow(getContext());

            // Create and add TextViews for violation, remarks, and date
            TextView violationTextView = new TextView(getContext());
            violationTextView.setText(violation);
            row.addView(violationTextView);

            TextView remarksTextView = new TextView(getContext());
            remarksTextView.setText(remarks);
            row.addView(remarksTextView);

            TextView dateTextView = new TextView(getContext());
            dateTextView.setText(date);
            row.addView(dateTextView);

            // Add the row to the TableLayout
            violationTable.addView(row);
        }
    }
}
