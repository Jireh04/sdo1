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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.QueryDocumentSnapshot;


public class PrefectView extends Fragment {

    private TextView studentNameTextView, studentIdTextView, studentProgramTextView, studentContactTextView, studentYearTextView, studentBlockTextView;
    private TableLayout violationTable;
    private Map<String, TableRow> violationRows = new HashMap<>();
     private Button exportPdfButton;
    private TextView logsTextView;

    private  Button addViolationButton;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

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

        // Initialize UI elements
        studentNameTextView = view.findViewById(R.id.studentNameTextView);
        studentIdTextView = view.findViewById(R.id.studentIDTextView);
        studentProgramTextView = view.findViewById(R.id.studentProgramTextView);
        studentContactTextView = view.findViewById(R.id.studentContactTextView);
        studentYearTextView = view.findViewById(R.id.studentYearTextView);
        studentBlockTextView = view.findViewById(R.id.studentBlockTextView);
        violationTable = view.findViewById(R.id.violationTable);
        logsTextView = view.findViewById(R.id.logsTextView);  // Assuming the logs TextView has this ID
        exportPdfButton = view.findViewById(R.id.exportPdfButton);
        addViolationButton = view.findViewById(R.id.addViolationButton);

        exportPdfButton.setOnClickListener(v -> showExportOptions());

        addViolationButton.setOnClickListener(v -> {
            // Extract raw text from TextViews
            String rawStudentId = studentIdTextView.getText().toString().trim();
            String rawStudentName = studentNameTextView.getText().toString().trim();

            // Remove prefixes (like "ID: " and "Name: ") and trim whitespace
            String studentId = rawStudentId.replace("ID: ", "").trim();
            String studentName = rawStudentName.replace("Name: ", "").trim();

            // Pass cleaned values to the dialog
            showAddViolatorDialog(studentId, studentName);
        });


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

            // Fetch the student details (year, block, contact) using studentId from Firestore
            fetchStudentDetails(studentId);

            // Fetch violations and update logs count
            fetchViolationsData(studentId);
            updateStudentLogsCount(studentId);  // This will update the logs count in logsTextView
        }

        // Set up logsTextView click listener to show violation status
        logsTextView.setOnClickListener(v -> showViolationStatusDialog());

        return view;
    }
    public void showAddViolatorDialog(String studId, String name) {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.add_violator, null);

        // Find the UI elements in the dialog
        EditText dateTimeEditText = dialogView.findViewById(R.id.dateTimeEditText);
        EditText termEditText = dialogView.findViewById(R.id.termEditText); // New Term field
        EditText reporterEditText = dialogView.findViewById(R.id.reporterEditText);
        EditText locationEditText = dialogView.findViewById(R.id.locationEditText);
        EditText reporterIdEditText = dialogView.findViewById(R.id.reporterIdEditText);
        Spinner violationSpinner = dialogView.findViewById(R.id.violationSpinner);
        EditText remarksEditText = dialogView.findViewById(R.id.remarksEditText);
        Button submitButton = dialogView.findViewById(R.id.submitButton);
        String status = "accepted";

        // Set the current date and time automatically
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateAndTime = sdf.format(calendar.getTime());
        dateTimeEditText.setText(currentDateAndTime);

        // Set the current term automatically
        String currentTerm = getCurrentTerm();
        termEditText.setText(currentTerm);
        termEditText.setEnabled(false); // Disable editing
        termEditText.setFocusable(false); // Make it non-focusable
        termEditText.setClickable(false); // Prevent clicking


        // Fetch the prefect ID and set it
        String reporterId = PrefectSession.getPrefectId();
        reporterIdEditText.setText(reporterId);
        reporterIdEditText.setEnabled(false);

        // Fetch violation types from Firestore
        fetchViolationTypes(violationSpinner);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        builder.setTitle("Add Violation");

        AlertDialog dialog = builder.create();

        submitButton.setOnClickListener(v -> {
            // Get input values
            String dateTime = dateTimeEditText.getText().toString().trim();
            String term = termEditText.getText().toString().trim();
            String reporter = reporterEditText.getText().toString().trim();
            String location = locationEditText.getText().toString().trim();
            String remarks = remarksEditText.getText().toString().trim();

            // Validate input
            if (!isValidInput(reporter) || !isValidInput(location) || !isValidInput(remarks)) {
                Toast.makeText(getContext(), "Fields must contain meaningful text.", Toast.LENGTH_SHORT).show();
            } else if (dateTime.isEmpty() || reporter.isEmpty() || reporterId.isEmpty() || location.isEmpty() || remarks.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            } else {
                // Get violation and offense
                CheckboxSpinnerAdapter violationAdapter = (CheckboxSpinnerAdapter) violationSpinner.getAdapter();
                String violation = violationAdapter.getSelectedViolation();
                String offense = violationAdapter.getSelectedType();

                if (violation == null || violation.isEmpty() || offense == null || offense.isEmpty()) {
                    Toast.makeText(getContext(), "Please select a valid violation.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prepare data for Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> violatorData = new HashMap<>();
                violatorData.put("date", dateTime);
                violatorData.put("term", term); // Add term
                violatorData.put("prefect_referrer", reporter);
                violatorData.put("referrer_id", reporterId);
                violatorData.put("location", location);
                violatorData.put("violation", offense);
                violatorData.put("offense", violation);
                violatorData.put("remarks", remarks);
                violatorData.put("student_id", studId);
                violatorData.put("student_name", name);
                violatorData.put("status", status);
                violatorData.put("violation_status", "Unsettled");

                String formattedDateTime = dateTime.replace(" ", "_");
                String documentId = formattedDateTime + "_" + studId;

                // Save to Firestore
                db.collection("prefect")
                        .document(reporterId)
                        .collection("prefect_referral_history")
                        .document(documentId)
                        .set(violatorData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getContext(), "Violator Added!", Toast.LENGTH_SHORT).show();

                            db.collection("students")
                                    .document(studId)
                                    .collection("accepted_status")
                                    .document(documentId)
                                    .set(violatorData)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Data saved to accepted_status."))
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancel", (dialogInterface, which) -> dialog.dismiss());

        dialog.show();
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


    // Method to validate input fields for meaningful text and no special characters/whitespace only
    private boolean isValidInput(String input) {
        return input != null && input.matches(".*[a-zA-Z0-9].*");
    }

    private void fetchViolationTypes(Spinner violationSpinner) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference violationTypesRef = firestore.collection("violation_type");

        violationTypesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, List<String>> violationMap = new HashMap<>();
                List<String> violationDisplayList = new ArrayList<>();
                violationDisplayList.add("Select a Violation"); // Initial prompt

                // Fetch violations and types from Firestore
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String violationName = document.getString("violation");
                    String type = document.getString("type");

                    Log.d("FirestoreData", "Retrieved data: " + document.getId() + " => " + document.getData());

                    if (violationName != null && type != null) {
                        // Group types by violation name
                        violationMap.computeIfAbsent(violationName, k -> new ArrayList<>()).add(type);
                    }
                }

                // Populate the display list with violations and their types
                for (Map.Entry<String, List<String>> entry : violationMap.entrySet()) {
                    String violationName = entry.getKey();
                    List<String> types = entry.getValue();

                    // Add the violation name
                    violationDisplayList.add(violationName);
                    // Add each type below the violation name
                    for (String type : types) {
                        violationDisplayList.add(" " + type); // Indent types for better visibility
                    }
                }

                Log.d("ViolationDisplayList", "Display List: " + violationDisplayList.toString());

                // Create the adapter directly in this method
                CheckboxSpinnerAdapter adapter = new CheckboxSpinnerAdapter(getContext(), violationDisplayList);
                violationSpinner.setAdapter(adapter);

                violationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // Get the selected item text
                        String selectedText = violationDisplayList.get(position);
                        // Update the spinner prompt to show the selected item
                        violationSpinner.setPrompt(selectedText);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Handle case when no item is selected if needed
                    }
                });

                // Show the dropdown when the spinner is clicked
                violationSpinner.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        violationSpinner.performClick();
                        return true;
                    }
                    return false;
                });
            } else {
                Log.w("FormActivity", "Error getting violation types.", task.getException());
                Toast.makeText(getContext(), "Failed to load violation types", Toast.LENGTH_SHORT).show();
            }
        });
    }




    // Method to show export options for PDF
    private void showExportOptions() {
        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose an option");

        // Add options
        String[] options = {"Open PDF", "Download PDF"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Open PDF
                exportPdf(true); // true indicates to open the PDF
            } else if (which == 1) {
                // Download PDF
                exportPdf(false); // false indicates to download the PDF
            }
        });

        // Show the dialog
        builder.show();
    }

    // Modified exportPdf method to accept a boolean parameter
    private void exportPdf(boolean openPdf) {
        // Check if student data is valid
        if (studentNameTextView.getText().toString().isEmpty() || studentIdTextView.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Invalid data for PDF generation", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new PdfDocument
        PdfDocument pdfDocument = new PdfDocument();

        // Create a page description (A4 size - 595x842)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Get the Canvas object for drawing content
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(18);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Start drawing text with proper margins
        int leftMargin = 50;
        int topMargin = 100;
        int yPosition = topMargin;

        // Draw header with bold font and center alignment
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Student Violation Details", canvas.getWidth() / 2, yPosition, paint);
        yPosition += 40;

        // Draw a divider line below the header
        paint.setStrokeWidth(2);
        canvas.drawLine(leftMargin, yPosition, canvas.getWidth() - leftMargin, yPosition, paint);
        yPosition += 30;

        // Reset paint for student details (left-aligned)
        paint.setTextSize(16);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        // Draw student details with proper spacing
        canvas.drawText("Student " + studentNameTextView.getText().toString(), leftMargin, yPosition, paint);
        yPosition += 25;
        canvas.drawText("Student " + studentIdTextView.getText().toString(), leftMargin, yPosition, paint);
        yPosition += 25;
        canvas.drawText(studentProgramTextView.getText().toString(), leftMargin, yPosition, paint);
        yPosition += 25;
        canvas.drawText(studentContactTextView.getText().toString(), leftMargin, yPosition, paint);
        yPosition += 25;
        canvas.drawText(studentYearTextView.getText().toString(), leftMargin, yPosition, paint);
        yPosition += 25;
        canvas.drawText(studentBlockTextView.getText().toString(), leftMargin, yPosition, paint);
        yPosition += 40; // Extra space before violations section

        // Draw violations header
        paint.setTextSize(18);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Violations:", leftMargin, yPosition, paint);
        yPosition += 25;

        // Draw table header for violations
        paint.setTextSize(14);
        paint.setColor(Color.WHITE); // White text for the table header
        float tableStartX = leftMargin;
        float tableWidth = 495;
        float rowHeight = 40;
        float col1Width = 150;
        float col2Width = 345;

        // Draw table header background
        paint.setColor(Color.GRAY); // Gray background for the table header
        canvas.drawRect(tableStartX, yPosition, tableStartX + tableWidth, yPosition + rowHeight, paint);

        // Draw table header text
        paint.setColor(Color.WHITE); // White text for table header
        canvas.drawText("Violation Number", tableStartX + 10, yPosition + 30, paint);
        canvas.drawText("Description", tableStartX + col1Width + 10, yPosition + 30, paint);
        yPosition += rowHeight;

        // Reset text color for table content
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        // Draw violation details in table rows
        for (int i = 0; i < violationTable.getChildCount(); i++) {
            TableRow row = (TableRow) violationTable.getChildAt(i);
            TextView violationNumberTextView = (TextView) row.getChildAt(0);
            TextView violationDescriptionTextView = (TextView) row.getChildAt(1);

            // Alternate row background color for better readability
            if (i % 2 == 0) {
                paint.setColor(Color.LTGRAY); // Light gray background
            } else {
                paint.setColor(Color.WHITE);  // White background
            }
            canvas.drawRect(tableStartX, yPosition, tableStartX + tableWidth, yPosition + rowHeight, paint);

            // Draw text content
            paint.setColor(Color.BLACK); // Reset color for text
            canvas.drawText(violationNumberTextView.getText().toString(), tableStartX + 10, yPosition + 30, paint);
            canvas.drawText(violationDescriptionTextView.getText().toString(), tableStartX + col1Width + 10, yPosition + 30, paint);

            // Move to the next row
            yPosition += rowHeight;
        }

        // Draw final bottom line of the table
        paint.setStrokeWidth(2);
        canvas.drawLine(tableStartX, yPosition, tableStartX + tableWidth, yPosition, paint);

        // Finish the page
        pdfDocument.finishPage(page);

        // Save the PDF
        String filePath;
        if (openPdf) {
            File directory = getContext().getFilesDir();
            filePath = directory + "/" + studentNameTextView.getText().toString() + "_violations.pdf";
        } else {
            filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/" + studentNameTextView.getText().toString() + "_violations.pdf";
        }

        File file = new File(filePath);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            pdfDocument.writeTo(outputStream);
            outputStream.close();
            Toast.makeText(getContext(), openPdf ? "PDF generated and opened." : "PDF downloaded.", Toast.LENGTH_SHORT).show();
            if (openPdf) {
                openPdf(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }


    private void openPdf(File file) {
        // Create an Intent to open the PDF file
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Get the URI for the file using FileProvider
        Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);

        // Set flags to allow read access to the file
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Set MIME type to ensure it's recognized as a PDF file
        intent.setDataAndType(uri, "application/pdf");

        // Launch the intent to open the PDF
        startActivity(intent);
    }


    private void viewPdf(File file) {
        // Create an intent to view the PDF
        Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Check if a PDF viewer is available
        Intent chooser = Intent.createChooser(intent, "Open PDF");
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(getContext(), "No PDF viewer found", Toast.LENGTH_SHORT).show();
        }
    }
    private void fetchStudentDetails(String studentId) {
        // Fetching the student details from Firestore using the studentId
        firestore.collection("students").document(studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot studentDoc = task.getResult();
                        if (studentDoc.exists()) {
                            // Retrieve student details
                            String studentYear = studentDoc.getString("year");
                            String studentBlock = studentDoc.getString("block");
                            String studentContact = String.valueOf(studentDoc.get("contacts"));  // Ensure contact is a string

                            // Update the UI with the fetched details
                            studentYearTextView.setText("Year: " + studentYear);
                            studentBlockTextView.setText("Block: " + studentBlock);
                            studentContactTextView.setText("Contact No: " + studentContact);
                        }
                    } else {
                        Log.d("PrefectView", "Error getting student details: ", task.getException());
                    }
                });
    }


    // Method to fetch violation, remarks, and date data from Firestore based on studentId
    private void fetchViolationsData(String studentId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("students") // The students collection
                .document(studentId) // The student document using the studentId
                .collection("accepted_status") // The 'accepted_status' subcollection
                .get() // Fetch all documents in the subcollection
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check if any documents are found
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String violationStatus = documentSnapshot.getString("violation_status");

                            // Only display rows for violations that are not "Settled"
                            if (violationStatus == null || !violationStatus.equals("Settled")) {
                                String violation = documentSnapshot.getString("violation");
                                String remarks = documentSnapshot.getString("remarks");
                                String date = documentSnapshot.getString("date");
                                String location = documentSnapshot.getString("location");
                                String term = documentSnapshot.getString("term");
                                String offense = documentSnapshot.getString("offense");
                                // Populate the TableLayout with the fetched data
                                populateViolationTable(violation, remarks, date, location, term, offense, documentSnapshot);
                            }
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
    private void populateViolationTable(String violation, String remarks, String date, String location, String term, String offense, DocumentSnapshot documentSnapshot) {
        // Initialize the iteration counter outside the loop
        int iterationCounter = 0;

        if (violation != null && !violation.isEmpty()) {
            // Create a new TableRow for each violation record
            TableRow row = new TableRow(getContext());
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            // Create TextViews for basic fields to display in the table
            TextView numberText = new TextView(getContext());
            if (iterationCounter == 0) {
                numberText.setText(String.valueOf(violationTable.getChildCount()));  // First iteration: -1
            } else {
                numberText.setText(String.valueOf(violationTable.getChildCount() - 2));  // Subsequent iterations: -2
                iterationCounter++;
            }
            numberText.setPadding(16, 16, 16, 16);
            row.addView(numberText);

            TextView violationTextView = new TextView(getContext());
            violationTextView.setText(violation);
            violationTextView.setPadding(8, 8, 8, 8);
            row.addView(violationTextView);

            TextView offenseTextView = new TextView(getContext());
            offenseTextView.setText(offense);
            offenseTextView.setPadding(8, 8, 8, 8);
            row.addView(offenseTextView);

            TextView dateTextView = new TextView(getContext());
            dateTextView.setText(date);
            row.addView(dateTextView);

            // Add the row to the TableLayout
            violationTable.addView(row);

            // Create an additional row for the details (hidden by default)
            TableRow detailsRow = new TableRow(getContext());
            detailsRow.setVisibility(View.GONE); // Initially hidden
            detailsRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            detailsRow.setBackgroundColor(Color.parseColor("#ECECEC"));

            // Details TextView to show reporter, location, semester, etc.
            TextView numTextView = new TextView(getContext());
            String numText = " ";
            numTextView.setText(numText);
            numTextView.setPadding(16, 16, 16, 16);
            detailsRow.addView(numTextView);

            // Details TextView to show reporter, location, semester, etc.
            TextView LocationTextView = new TextView(getContext());
            String locationText = "Location: " + location;
            LocationTextView.setText(locationText);
            LocationTextView.setPadding(16, 16, 16, 16);
            detailsRow.addView(LocationTextView);

            // Details TextView to show reporter, location, semester, etc.
            TextView SemesterTextView = new TextView(getContext());
            String semText = "Semester: " + term;
            SemesterTextView.setText(semText);
            SemesterTextView.setPadding(16, 16, 16, 16);
            detailsRow.addView(SemesterTextView);

            TextView RemarksTextView = new TextView(getContext());
            String remarksText = "Remarks: " + remarks;
            RemarksTextView.setText(remarksText);
            RemarksTextView.setPadding(16, 16, 16, 16);
            detailsRow.addView(RemarksTextView);

            // Create an additional row for the buttons (hidden by default)
            TableRow buttonRow = new TableRow(getContext());
            buttonRow.setVisibility(View.GONE); // Initially hidden
            buttonRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            buttonRow.setBackgroundColor(Color.parseColor("#ECECEC"));

            TextView numTextView2 = new TextView(getContext());
            String numText2 = " ";
            numTextView2.setText(numText2);
            numTextView2.setPadding(16, 16, 16, 16);
            buttonRow.addView(numTextView2);

            Button settledButton = new Button(getContext());
            settledButton.setText("Settled");
            settledButton.setTextSize(12);
            settledButton.setOnClickListener(v -> {
                // Show sanction input dialog after setting status to "Settled"
                DocumentReference violationRef = documentSnapshot.getReference();
                showSanctionInputDialog(violationRef); // Call method for input dialog
            });
            buttonRow.addView(settledButton);

            Button editButton = new Button(getContext());
            editButton.setText("Edit");
            editButton.setOnClickListener(v -> showEditViolationDialog(documentSnapshot));  // Correct reference here
            buttonRow.addView(editButton);

            // Add the details row to the table
            violationTable.addView(detailsRow);
            violationTable.addView(buttonRow);

            // Set an OnClickListener on the main row to toggle details visibility
            row.setOnClickListener(v -> {
                if (detailsRow.getVisibility() == View.GONE && buttonRow.getVisibility() == View.GONE) {
                    detailsRow.setVisibility(View.VISIBLE); // Expand to show details
                    buttonRow.setVisibility(View.VISIBLE); // Show buttons
                } else {
                    detailsRow.setVisibility(View.GONE); // Collapse to hide details
                    buttonRow.setVisibility(View.GONE); // Hide buttons
                }
            });

            iterationCounter++;
        }
    }



    // Method to update the logs count displayed on the logsTextView
    private void updateStudentLogsCount(String studentId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference violationsRef = firestore.collection("students")
                .document(studentId)
                .collection("accepted_status");

        violationsRef.get().addOnSuccessListener(querySnapshot -> {
            int totalLogsCount = 0;

            // Iterate over all violations and sum the logs count
            for (QueryDocumentSnapshot document : querySnapshot) {
                Long logs = document.getLong("logs");
                if (logs != null) {
                    totalLogsCount += logs;
                }
            }

            // Update the logEntryTextView with the new total logs count
            logsTextView.setText("Logs: " + totalLogsCount);  // Update the logs display immediately
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error fetching logs for student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void showSanctionInputDialog(DocumentReference violationRef) {
        EditText sanctionEditText = new EditText(getContext());
        sanctionEditText.setHint("Enter sanction");

        AlertDialog sanctionDialog = new AlertDialog.Builder(getContext())
                .setTitle("Enter Sanction")
                .setView(sanctionEditText)
                .setPositiveButton("OK", (dialog, which) -> {
                    String sanctionText = sanctionEditText.getText().toString().trim();

                    if (!sanctionText.isEmpty()) {
                        Map<String, Object> update = new HashMap<>();
                        update.put("sanction", sanctionText);

                        // Get current logs count and then update the document
                        violationRef.get().addOnSuccessListener(documentSnapshot -> {
                            int logsCount = documentSnapshot.getLong("logs") != null ? documentSnapshot.getLong("logs").intValue() : 0;
                            update.put("logs", logsCount + 1);  // Increment logs count

                            // Update sanction and logs count in Firestore
                            violationRef.update(update)
                                    .addOnSuccessListener(aVoid -> {
                                        // After sanction and logs update, now update the violation status to "Settled"
                                        Map<String, Object> statusUpdate = new HashMap<>();
                                        statusUpdate.put("violation_status", "Settled");

                                        violationRef.update(statusUpdate)
                                                .addOnSuccessListener(aVoidStatus -> {
                                                    // UI feedback for success
                                                    Toast.makeText(getContext(), "Sanction added and status set to Settled", Toast.LENGTH_SHORT).show();

                                                    // Update logs count display
                                                    updateStudentLogsCount(violationRef.getParent().getParent().getId());
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getContext(), "Error updating violation status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Error updating sanction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error fetching logs field: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(getContext(), "Sanction cannot be empty.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        sanctionDialog.show();
    }



    // Method to show the violation status dialog when "Logs" is clicked

    private void showViolationStatusDialog() {
        // Retrieve the student ID to fetch the violation status
        String studentId = getArguments().getString("student_id");

        firestore.collection("students")
                .document(studentId)
                .collection("accepted_status")
                .whereEqualTo("violation_status", "Settled") // Fetch only the "Settled" status
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Create a layout to hold each violation with buttons
                        LinearLayout violationsLayout = new LinearLayout(getContext());
                        violationsLayout.setOrientation(LinearLayout.VERTICAL);
                        violationsLayout.setPadding(16, 16, 16, 16);

                        // Loop through the documents and add each violation to the layout
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Prepare violation details
                            String violation = documentSnapshot.getString("violation");
                            String remarks = documentSnapshot.getString("remarks");
                            String date = documentSnapshot.getString("date");
                            String offense = documentSnapshot.getString("offense");

                            // Create a container for each violation
                            LinearLayout violationContainer = new LinearLayout(getContext());
                            violationContainer.setOrientation(LinearLayout.VERTICAL);
                            violationContainer.setPadding(8, 8, 8, 8);
                            violationContainer.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                            // Add violation details to the container
                            TextView violationTextView = new TextView(getContext());
                            violationTextView.setText("Violation: " + violation + "\n Offense Type: "+ offense + "\nRemarks: " + remarks + "\nDate: " + date);
                            violationTextView.setPadding(8, 8, 8, 8);
                            violationContainer.addView(violationTextView);

                            // Dynamically determine the referrer type and name
                            String referrerType = "";
                            String referrerName = "";
                            if (documentSnapshot.contains("personnel_referrer")) {
                                referrerType = "Personnel";
                                referrerName = documentSnapshot.getString("personnel_referrer");
                            } else if (documentSnapshot.contains("student_referrer")) {
                                referrerType = "Student";
                                referrerName = documentSnapshot.getString("student_referrer");
                            } else if (documentSnapshot.contains("prefect_referrer")) {
                                referrerType = "Prefect";
                                referrerName = documentSnapshot.getString("prefect_referrer");
                            }

                            // Add referrer info if exists
                            if (!referrerName.isEmpty()) {
                                TextView referrerTextView = new TextView(getContext());
                                referrerTextView.setText("Referrer Type: " + referrerType + "\nReferrer Name: " + referrerName);
                                violationContainer.addView(referrerTextView);
                            }

                            // Create action buttons for each violation
                            LinearLayout buttonsLayout = new LinearLayout(getContext());
                            buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
                            buttonsLayout.setWeightSum(3);  // Distribute buttons equally



                            // Unsettle Button
                            Button unsettleButton = new Button(getContext());
                            unsettleButton.setText("Unsettle");
                            unsettleButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                            unsettleButton.setOnClickListener(v -> {
                                unsettleViolation(documentSnapshot);
                            });


                            buttonsLayout.addView(unsettleButton);

                            // Add the buttons layout to the violation container
                            violationContainer.addView(buttonsLayout);

                            // Add the violation container to the main layout
                            violationsLayout.addView(violationContainer);
                        }

                        // Show the dialog with the violations and their actions
                        new AlertDialog.Builder(getContext())
                                .setTitle("Settled Violations")
                                .setView(violationsLayout) // Add the layout with all violations
                                .setNegativeButton("Close", null)
                                .show();
                    } else {
                        Toast.makeText(getContext(), "No settled violations found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching settled violations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void unsettleViolation(DocumentSnapshot documentSnapshot) {
        DocumentReference docRef = documentSnapshot.getReference();

        // Prepare the updates for the violation
        Map<String, Object> updates = new HashMap<>();
        updates.put("violation_status", "Unsettled"); // Set the status to "Unsettled"
        updates.put("logs", 0);  // Reset the logs count to 0

        // Update the document with the new violation status and reset logs to 0
        docRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Violation is now unsettled", Toast.LENGTH_SHORT).show();

                    // After updating the status, you can also add logic here to refresh the UI
                    // For example, updating the table with the "Unsettled" violations or refreshing the view
                    updateStudentLogsCount(documentSnapshot.getReference().getParent().getParent().getId());

                    // Optionally, re-add the violation to the table or list if you need it to show up again
                    // Example:
                   // This will re-query the violations and refresh the UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating violation status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void showEditViolationDialog(DocumentSnapshot doc) {
        // Create an AlertDialog to edit the violation details
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Violation");

        // Create input fields for date, reporter, location, violation, and remarks
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // EditText for date (non-editable)
        EditText dateInput = new EditText(getContext());
        dateInput.setHint("Date");
        dateInput.setText(doc.getString("date"));
        dateInput.setFocusable(false);
        dateInput.setClickable(false);
        dateInput.setCursorVisible(false);  // Optional: hides the cursor
        dateInput.setBackgroundResource(android.R.color.transparent);
        layout.addView(dateInput);

        // TextView for reporter name
        String reporterName = "";
        if (doc.contains("personnel_referrer")) {
            reporterName = doc.getString("personnel_referrer");
        } else if (doc.contains("student_referrer")) {
            reporterName = doc.getString("student_referrer");
        } else if (doc.contains("prefect_referrer")) {
            reporterName = doc.getString("prefect_referrer");
        }
        TextView reporterTextView = new TextView(getContext());
        reporterTextView.setText("Reporter: " + reporterName);
        reporterTextView.setTextSize(16);
        reporterTextView.setPadding(16, 16, 16, 16);
        layout.addView(reporterTextView);

        // EditText for location
        EditText locationInput = new EditText(getContext());
        locationInput.setHint("Location");
        locationInput.setText(doc.getString("location"));  // Set existing location
        layout.addView(locationInput);

        // Spinner for violation selection
        Spinner violationSpinner = new Spinner(getContext());
        fetchViolationTypes(violationSpinner, doc); // Fetch the violation types from Firestore
        layout.addView(violationSpinner);

        // EditText for remarks
        EditText remarksInput = new EditText(getContext());
        remarksInput.setHint("Remarks");
        remarksInput.setText(doc.getString("remarks"));  // Set existing remarks
        layout.addView(remarksInput);

        builder.setView(layout);

        // Set the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Get the selected violation and offense type
            String selectedViolation = ((CheckboxSpinnerAdapter) violationSpinner.getAdapter()).getSelectedViolation();
            String selectedType = ((CheckboxSpinnerAdapter) violationSpinner.getAdapter()).getSelectedType();

            String newDate = dateInput.getText().toString();
            String newLocation = locationInput.getText().toString();
            String newRemarks = remarksInput.getText().toString();

            // Validate inputs (location and remarks)
            String validPattern = "^[a-zA-Z0-9\\s]+$";
            if (!newLocation.matches(validPattern)) {
                Toast.makeText(getContext(), "Location contains invalid characters!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newRemarks.matches(validPattern)) {
                Toast.makeText(getContext(), "Remarks contain invalid characters!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update Firestore with the new values
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("violation", selectedType);
            updatedData.put("offense", selectedViolation);  // Store the selected offense type
            updatedData.put("date", newDate);
            updatedData.put("location", newLocation.isEmpty() ? null : newLocation);
            updatedData.put("remarks", newRemarks.isEmpty() ? null : newRemarks);

            doc.getReference().update(updatedData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Violation Updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error updating violation", Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void fetchViolationTypes(Spinner violationSpinner, DocumentSnapshot doc) {
        // Access the Firestore collection
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

                // Set the current violation in the spinner (if it matches the option)
                String currentViolation = doc.getString("violation");
                int violationIndex = violationDisplayList.indexOf(currentViolation);
                if (violationIndex >= 0) {
                    violationSpinner.setSelection(violationIndex);
                }

            } else {
                Log.w("FormActivity", "Error getting violation types.", task.getException());
                Toast.makeText(getContext(), "Failed to load violation types", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinner(Spinner spinner, List<String> items) {
        // Use your custom CheckboxSpinnerAdapter
        CheckboxSpinnerAdapter adapter = new CheckboxSpinnerAdapter(getContext(), items);
        spinner.setAdapter(adapter);
    }



    private void updateViolationStatus(DocumentSnapshot documentSnapshot, String status) {
        // Get student ID and violation document ID
        String studentId = documentSnapshot.getString("student_id");
        String violationId = documentSnapshot.getId();

        // Get reference to the violation document in Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference violationDocRef = firestore.collection("students")
                .document(studentId)
                .collection("accepted_status")
                .document(violationId);

        // Create a map to update the violation status
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("violation_status", status);

        // Update the violation status in Firestore
        violationDocRef.update(updateData)
                .addOnSuccessListener(aVoid -> {
                    // Fetch the updated document to check the status
                    violationDocRef.get()
                            .addOnSuccessListener(updatedDocSnapshot -> {
                                String updatedStatus = updatedDocSnapshot.getString("violation_status");

                                // If the status is "Settled", remove the row from the table
                                if ("Settled".equals(updatedStatus)) {
                                    removeViolationRow(violationId);
                                }

                                // Show a success toast
                                Toast.makeText(getContext(), "Status updated to: " + updatedStatus, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Handle error fetching the document after the update
                                Toast.makeText(getContext(), "Error fetching document after update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle error updating the status
                    Toast.makeText(getContext(), "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void removeViolationRow(String violationId) {
        // Get the row from the map using the violation ID
        TableRow row = violationRows.get(violationId);

        if (row != null) {
            // Remove the row from the TableLayout
            violationTable.removeView(row);

            // Remove the row from the map as well
            violationRows.remove(violationId);
        }
    }

}
