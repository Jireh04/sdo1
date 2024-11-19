package com.finals.lagunauniversitysdo;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;
import android.graphics.Typeface;
import java.util.Arrays;

import java.util.List;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import java.util.Calendar;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;



public class reports_prefect extends Fragment {

    private EditText studentNoEditText;
    private EditText dateRangeEditText;  // Declare the new EditText for Date Range
    private ImageButton searchButton;
    private ImageButton pdfButton;
    private TextView studentNameTextView;
    private Button groupDateButton;  // Declare the new Button for Group by Date

    private FirebaseFirestore db;
    private String studentName;
    private String studentId;
    private long logsCount;
    private List<String> offenses = new ArrayList<>();
    private List<String> violations = new ArrayList<>();
    private List<String> date = new ArrayList<>();
    private List<String> location = new ArrayList<>();
    private List<String> referrer_id = new ArrayList<>();

    // Spinners for selection
    private Spinner programSpinner;
    private Spinner statusSpinner;

    private Spinner academicYearSpinner;
    private Spinner semesterSpinner;
    private Spinner offenseTypeSpinner;
    private Button groupNameButton;

    private Button useDateRangeButton;
    private EditText startDateEditText, endDateEditText;
    private LinearLayout dateRangeLayout, resultLayout;

    public reports_prefect() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports_prefect, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        studentNoEditText = view.findViewById(R.id.studentNoEditText);
        searchButton = view.findViewById(R.id.searchButton);
        studentNameTextView = view.findViewById(R.id.studentNameTextView);
        pdfButton = view.findViewById(R.id.pdfButton);
        programSpinner = view.findViewById(R.id.program_spinner);
        statusSpinner = view.findViewById(R.id.status_spinner);

        // Initialize new spinners
        academicYearSpinner = view.findViewById(R.id.academicYearSpinner);
        semesterSpinner = view.findViewById(R.id.semesterSpinner);
        offenseTypeSpinner = view.findViewById(R.id.offenseTypeSpinner);





        // Set up spinners
        setupSpinner(programSpinner, new String[]{"BSCS", "BSIT", "BEED"});
        setupSpinner(statusSpinner, new String[]{"All", "Settled", "Unsettled"});
        setupSpinner(academicYearSpinner, new String[]{"2023-2024", "2024-2025"});
        setupSpinner(semesterSpinner, new String[]{"1st Semester", "2nd Semester", "Summer"});
        setupSpinner(offenseTypeSpinner, new String[]{"All","Light Offense", "Major Offense", "Serious Offense"});

        // Initially disable the PDF button
        pdfButton.setEnabled(false);

        // Set up listeners
        searchButton.setOnClickListener(v -> searchStudent());

        // Handle PDF Button click
        pdfButton.setOnClickListener(v -> openPDF());

        // Set up Group Name button click listener
        groupNameButton = view.findViewById(R.id.group_name);
        groupNameButton.setOnClickListener(v -> fetchGroupByProgram());

        // Set up Group by Date button click listener
        groupDateButton = view.findViewById(R.id.group_date);
        groupDateButton.setOnClickListener(v -> generateGroupByDate());

        // Toggle visibility for date range fields
        useDateRangeButton = view.findViewById(R.id.useDateRangeButton);
        dateRangeLayout = view.findViewById(R.id.dateRangeLayout);

        resultLayout = view.findViewById(R.id.resultLayout);

        useDateRangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dateRangeLayout.getVisibility() == View.GONE) {
                    dateRangeLayout.setVisibility(View.VISIBLE); // Show date range fields
                } else {
                    dateRangeLayout.setVisibility(View.GONE); // Hide date range fields
                }
            }
        });

        // Initialize date edit texts and set listeners
        startDateEditText = view.findViewById(R.id.startDateEditText);
        endDateEditText = view.findViewById(R.id.endDateEditText);

        startDateEditText.setOnClickListener(v -> showDatePickerDialog(true)); // true for start date
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(false)); // false for end date

        return view;
    }

    // Show DatePickerDialog for start or end date
    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendar.set(year, monthOfYear, dayOfMonth);
                    String selectedDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                            .format(calendar.getTime());
                    if (isStartDate) {
                        startDateEditText.setText(selectedDate);
                    } else {
                        endDateEditText.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }



    // Generate grouped data based on selected date range and filters
    private void generateGroupByDate() {
        String startDateString = startDateEditText.getText().toString().trim();
        String endDateString = endDateEditText.getText().toString().trim();

        if (startDateString.isEmpty() || endDateString.isEmpty()) {
            Toast.makeText(getContext(), "Please select both start and end dates.", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Date startDate, endDate;

        try {
            startDate = sdf.parse(startDateString);
            endDate = sdf.parse(endDateString);
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid date format. Use: MM/dd/yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Normalize start and end dates to full-day ranges
        Calendar calendar = Calendar.getInstance();

        // Start Date
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date normalizedStartDate = calendar.getTime();

        // End Date
        calendar.setTime(endDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date normalizedEndDate = calendar.getTime();

            // Get the selected program and status from the spinner
            String selectedProgram = programSpinner.getSelectedItem().toString().trim();
            String selectedStatus = statusSpinner.getSelectedItem().toString().trim();

            // Get the selected semester and offense type (add these to the spinners)
            String selectedSemester = semesterSpinner.getSelectedItem().toString().trim();
            String selectedOffenseType = offenseTypeSpinner.getSelectedItem().toString().trim();

            List<String> allReferralDetails = new ArrayList<>();
            final int[] processedCount = {0};
            final int[] totalEntities = {0};

            if (selectedStatus.equals("Settled")) {
                // When "Settled" is selected, fetch only from the students collection and filter by "accepted_status" sub-collection
                fetchReferralDataForSettled("students", "accepted_status", normalizedStartDate, normalizedEndDate, selectedProgram, selectedStatus, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
            } else if (selectedStatus.equals("Unsettled")) {
                // When "Unsettled" is selected, fetch from the students, personnel, and prefect collections with unsettled status
                fetchReferralDataForUnsettled("students", "student_refferal_history", normalizedStartDate, normalizedEndDate, selectedProgram, selectedStatus, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
                fetchReferralDataForUnsettled("personnel", "personnel_refferal_history", normalizedStartDate, normalizedEndDate, selectedProgram, selectedStatus, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
                fetchReferralDataForUnsettled("prefect", "prefect_referral_history", normalizedStartDate, normalizedEndDate, selectedProgram, selectedStatus, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
            } else {
                // Otherwise, fetch data from "students", "personnel", and "prefect collections" with proper filtering.
                fetchReferralData("students", "student_refferal_history", normalizedStartDate, normalizedEndDate, selectedProgram, selectedStatus, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
                fetchReferralData("personnel", "personnel_refferal_history", normalizedStartDate, normalizedEndDate, selectedProgram, selectedStatus, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
                fetchReferralData("prefect", "prefect_referral_history", normalizedStartDate, normalizedEndDate, selectedProgram, selectedStatus, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
            }
        }

    private void fetchReferralDataForUnsettled(String collection, String referralHistoryCollection, Date startDate, Date endDate,
                                               String selectedProgram, String selectedStatus, String selectedSemester, String selectedOffenseType,
                                               List<String> allReferralDetails, final int[] processedCount, final int[] totalEntities) {

        db.collection(collection)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            final int totalEntitiesCount = querySnapshot.size();
                            totalEntities[0] += totalEntitiesCount;

                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                // Query referral history for "unsettled_status"
                                Query referralQuery = db.collection(collection)
                                        .document(doc.getId())
                                        .collection(referralHistoryCollection)
                                        .whereEqualTo("status", "accepted")  // Fetch unsettled referrals
                                        .whereEqualTo("student_program", selectedProgram)
                                        .whereEqualTo("term", selectedSemester)
                                        .whereGreaterThanOrEqualTo("date", startDate)
                                        .whereLessThanOrEqualTo("date", endDate);

                                // Apply offense filter
                                referralQuery = applyOffenseTypeFilter(referralQuery, selectedOffenseType);

                                referralQuery = applyStatusFilter(referralQuery, "Unsettled");
                                // Fetch the filtered data based on the status and offense type
                                referralQuery.get()
                                        .addOnCompleteListener(referralTask -> {
                                            if (referralTask.isSuccessful()) {
                                                QuerySnapshot referralQuerySnapshot = referralTask.getResult();
                                                if (referralQuerySnapshot != null && !referralQuerySnapshot.isEmpty()) {
                                                    for (DocumentSnapshot referralDoc : referralQuerySnapshot.getDocuments()) {
                                                        String name = referralDoc.getString("student_name");
                                                        String offense = referralDoc.getString("offense");
                                                        String violation = referralDoc.getString("violation");
                                                        String referrer = referralDoc.getString(getReferrerField(collection));  // Get the appropriate referrer field
                                                        String referralStatus = referralDoc.getString("status");
                                                        String term = referralDoc.getString("term");


                                                        String referralDetails = buildReferralDetails(name, offense, violation, referrer, referralStatus, term);
                                                        allReferralDetails.add(getEntityTypeName(collection) + " Referral:\n" + referralDetails);
                                                    }
                                                }
                                            }
                                            processedCount[0]++;
                                            if (processedCount[0] == totalEntities[0]) {
                                                generatePDFFromReferralDetails(allReferralDetails);
                                            }
                                        });
                            }
                        }
                    }
                });
    }


    private void fetchReferralDataForSettled(String collection, String subCollection, Date startDate, Date endDate, String selectedProgram,
                                             String selectedStatus, String selectedSemester, String selectedOffenseType,
                                             List<String> allReferralDetails, final int[] processedCount, final int[] totalEntities) {

        db.collection(collection)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            final int totalEntitiesInCollection = querySnapshot.size();
                            totalEntities[0] += totalEntitiesInCollection;

                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                // Query settled referrals with "accepted" status from the sub-collection
                                Query referralQuery = db.collection(collection)
                                        .document(doc.getId())
                                        .collection(subCollection)
                                        .whereEqualTo("status", "accepted"); // Fixed status as "accepted"

                                // Fetch data based on the filters
                                referralQuery.get()
                                        .addOnCompleteListener(referralTask -> {
                                            if (referralTask.isSuccessful()) {
                                                QuerySnapshot referralQuerySnapshot = referralTask.getResult();
                                                if (referralQuerySnapshot != null && !referralQuerySnapshot.isEmpty()) {
                                                    for (DocumentSnapshot referralDoc : referralQuerySnapshot.getDocuments()) {
                                                        String referralId = referralDoc.getId();

                                                        // Extract and parse the referral date from referralId
                                                        String dateString = referralId.split("_")[0] + " " + referralId.split("_")[1];
                                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                                        Date referralDate;
                                                        try {
                                                            referralDate = dateFormat.parse(dateString);
                                                        } catch (ParseException e) {
                                                            referralDate = null; // Skip invalid date format
                                                        }

                                                        // Check if the date is within range
                                                        if (referralDate != null && referralDate.after(startDate) && referralDate.before(endDate)) {
                                                            // Apply additional filters
                                                            String program = referralDoc.getString("student_program");
                                                            String term = referralDoc.getString("term");
                                                            String offense = referralDoc.getString("offense");

                                                            boolean programMatches = program != null && program.equals(selectedProgram);
                                                            boolean termMatches = selectedSemester.equals("All") || (term != null && term.equals(selectedSemester));
                                                            boolean offenseMatches = selectedOffenseType.equals("All") || (offense != null && offense.equals(selectedOffenseType));

                                                            // If all filters match, collect the details
                                                            if (programMatches && termMatches && offenseMatches) {
                                                                String name = referralDoc.getString("student_name");
                                                                String violation = referralDoc.getString("violation");
                                                                String referrer = referralDoc.getString("student_referrer");
                                                                String status = referralDoc.getString("status");

                                                                StringBuilder referralDetails = new StringBuilder();
                                                                referralDetails.append("Name: ").append(name != null ? name : "N/A")
                                                                        .append("\nOffense: ").append(offense != null ? offense : "N/A")
                                                                        .append("\nViolation: ").append(violation != null ? violation : "N/A")
                                                                        .append("\nReferrer: ").append(referrer != null ? referrer : "N/A")
                                                                        .append("\nStatus: ").append(status != null ? status : "N/A")
                                                                        .append("\nTerm: ").append(term != null ? term : "N/A")
                                                                        .append("\nProgram: ").append(program != null ? program : "N/A")
                                                                        .append("\n");

                                                                // Add details to the list
                                                                allReferralDetails.add(collection.substring(0, 1).toUpperCase() + collection.substring(1) + " Settled Referral:\n" + referralDetails.toString());
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            processedCount[0]++;
                                            if (processedCount[0] == totalEntities[0]) {
                                                generatePDFFromReferralDetails(allReferralDetails);
                                            }
                                        });
                            }
                        }
                    }
                });
    }



    private void fetchReferralData(String collection, String subCollection, Date startDate, Date endDate, String selectedProgram,
                                   String selectedStatus, String selectedSemester, String selectedOffenseType,
                                   List<String> allReferralDetails, final int[] processedCount, final int[] totalEntities) {

        db.collection(collection)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            final int totalEntitiesInCollection = querySnapshot.size();
                            totalEntities[0] += totalEntitiesInCollection;

                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                // Query referral history within the given date range and program from the respective sub-collection
                                Query referralQuery = db.collection(collection)
                                        .document(doc.getId())
                                        .collection(subCollection);

                                // Apply status filter using the applyStatusFilter method
                                applyStatusFilter(referralQuery, selectedStatus);

                                // Apply offense type filtering (same as previous logic)
                                applyOffenseTypeFilter(referralQuery, selectedOffenseType);

                                // Fetch the filtered data based on the status and offense type
                                referralQuery.get()
                                        .addOnCompleteListener(referralTask -> {
                                            if (referralTask.isSuccessful()) {
                                                QuerySnapshot referralQuerySnapshot = referralTask.getResult();
                                                if (referralQuerySnapshot != null && !referralQuerySnapshot.isEmpty()) {
                                                    for (DocumentSnapshot referralDoc : referralQuerySnapshot.getDocuments()) {
                                                        String referralId = referralDoc.getId();  // Referral ID (includes date)

                                                        // Extract date from referralId (e.g., "2024-11-14_18:03:53_221-0896")
                                                        String dateString = referralId.split("_")[0] + " " + referralId.split("_")[1];  // Extract date from the referral_id

                                                        // Parse the extracted date string
                                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                                        Date referralDate;
                                                        try {
                                                            referralDate = dateFormat.parse(dateString);  // Parse the referral date
                                                        } catch (ParseException e) {
                                                            referralDate = null; // Invalid date format, skip this referral
                                                        }

                                                        // Check if referral date is within the date range
                                                        if (referralDate != null && referralDate.after(startDate) && referralDate.before(endDate)) {
                                                            // Match the program with the selected program
                                                            String referralProgram = referralDoc.getString("student_program");
                                                            String term = referralDoc.getString("term");
                                                            String offense = referralDoc.getString("offense");

                                                            // Apply program, term, and offense filters
                                                            boolean programMatches = referralProgram != null && referralProgram.equals(selectedProgram);
                                                            boolean termMatches = selectedSemester.equals("All") || term != null && term.equals(selectedSemester);
                                                            boolean offenseMatches = selectedOffenseType.equals("All") || offense != null && offense.equals(selectedOffenseType);

                                                            // If all filters match, add details
                                                            if (programMatches && termMatches && offenseMatches) {
                                                                String name = referralDoc.getString("student_name");
                                                                String violation = referralDoc.getString("violation");
                                                                String referrer = referralDoc.getString("student_referrer");
                                                                String status = referralDoc.getString("status");

                                                                StringBuilder referralDetails = new StringBuilder();
                                                                referralDetails.append("Name : ").append(name != null ? name : "N/A")
                                                                        .append("\nOffense: ").append(offense != null ? offense : "N/A")
                                                                        .append("\nViolation: ").append(violation != null ? violation : "N/A")
                                                                        .append("\nReferrer: ").append(referrer != null ? referrer : "N/A")
                                                                        .append("\nStatus: ").append(status != null ? status : "N/A")
                                                                        .append("\nTerm: ").append(term != null ? term : "N/A")
                                                                        .append("\nProgram: ").append(referralProgram != null ? referralProgram : "N/A")
                                                                        .append("\n");

                                                                // Add the referral details to the list
                                                                allReferralDetails.add(collection.substring(0, 1).toUpperCase() + collection.substring(1) + " Referral:\n" + referralDetails.toString());
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            processedCount[0]++;
                                            if (processedCount[0] == totalEntities[0]) {
                                                generatePDFFromReferralDetails(allReferralDetails);
                                            }
                                        });
                            }
                        }
                    }
                });
    }






    private void fetchGroupByProgram() {
        String selectedProgram = programSpinner.getSelectedItem().toString().trim();
        String selectedStatus = statusSpinner.getSelectedItem().toString().trim();
        String selectedSemester = semesterSpinner.getSelectedItem().toString().trim();
        String selectedOffenseType = offenseTypeSpinner.getSelectedItem().toString().trim();

        String programWithoutSuffix = selectedProgram.split("-")[0]; // Process program name
        List<String> allReferralDetails = new ArrayList<>(); // Store referral details
        final int[] processedCount = {0};  // Track total processed entities
        final int[] totalEntities = {0};   // Total entities to wait for

        // Handle settled/unsettled cases
        if (selectedStatus.equals("Settled")) {
            // Fetch referral history for students with accepted status
            fetchReferralHistoryForSettledStudents("students", "accepted_status", programWithoutSuffix, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
        } else if (selectedStatus.equals("Unsettled")) {
            // Fetch "accepted" referrals from students, personnel, and prefect collections
            fetchReferralHistoryForUnsettled("students", "student_refferal_history", programWithoutSuffix, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
            fetchReferralHistoryForUnsettled("personnel", "personnel_refferal_history", programWithoutSuffix, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
            fetchReferralHistoryForUnsettled("prefect", "prefect_referral_history", programWithoutSuffix, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
        } else {
            // Otherwise, fetch referral history for each group: students, personnel, and prefects
            fetchReferralHistory("students", "student_refferal_history", programWithoutSuffix, selectedStatus, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
            fetchReferralHistory("personnel", "personnel_refferal_history", programWithoutSuffix, selectedStatus, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
            fetchReferralHistory("prefect", "prefect_referral_history", programWithoutSuffix, selectedStatus, selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);
        }
    }


    private void fetchReferralHistoryForAllStatus(String collection, String referralHistoryCollection, String acceptedStatusCollection,
                                                  String programWithoutSuffix, String selectedSemester, String selectedOffenseType,
                                                  List<String> allReferralDetails, final int[] processedCount, final int[] totalEntities) {

        // Query from the regular referral history
        fetchReferralHistory(collection, referralHistoryCollection, programWithoutSuffix, "All", selectedSemester, selectedOffenseType, allReferralDetails, processedCount, totalEntities);

        // Query from the accepted_status sub-collection for "accepted" referrals
        db.collection(collection)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                Query referralQuery = db.collection(collection)
                                        .document(doc.getId())
                                        .collection(acceptedStatusCollection)  // Query the accepted_status sub-collection
                                        .whereEqualTo("status", "accepted")
                                        .whereEqualTo("student_program", programWithoutSuffix)
                                        .whereEqualTo("term", selectedSemester);

                                // Apply offense filter
                                referralQuery = applyOffenseTypeFilter(referralQuery, selectedOffenseType);

                                // Fetch the filtered data
                                referralQuery.get()
                                        .addOnCompleteListener(referralTask -> {
                                            if (referralTask.isSuccessful()) {
                                                QuerySnapshot referralQuerySnapshot = referralTask.getResult();
                                                if (referralQuerySnapshot != null && !referralQuerySnapshot.isEmpty()) {
                                                    for (DocumentSnapshot referralDoc : referralQuerySnapshot.getDocuments()) {
                                                        String name = referralDoc.getString("student_name");
                                                        String offense = referralDoc.getString("offense");
                                                        String violation = referralDoc.getString("violation");
                                                        String referrer = referralDoc.getString(getReferrerField(collection));  // Get the appropriate referrer field
                                                        String referralStatus = referralDoc.getString("status");
                                                        String term = referralDoc.getString("term");

                                                        String referralDetails = buildReferralDetails(name, offense, violation, referrer, referralStatus, term);
                                                        allReferralDetails.add(getEntityTypeName(collection) + " Referral (from accepted_status):\n" + referralDetails);
                                                    }
                                                }
                                            }
                                            processedCount[0]++;
                                            if (processedCount[0] == totalEntities[0]) {
                                                generatePDFFromReferralDetails(allReferralDetails);
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void fetchReferralHistoryForUnsettled(String collection, String referralHistoryCollection, String programWithoutSuffix,
                                                  String selectedSemester, String selectedOffenseType,
                                                  List<String> allReferralDetails, final int[] processedCount, final int[] totalEntities) {

        db.collection(collection)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            final int totalEntitiesCount = querySnapshot.size();
                            totalEntities[0] += totalEntitiesCount;

                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                // Query referral history for "pending" status in the given sub-collection
                                Query referralQuery = db.collection(collection)
                                        .document(doc.getId())
                                        .collection(referralHistoryCollection)
                                        .whereEqualTo("student_program", programWithoutSuffix)
                                        .whereEqualTo("term", selectedSemester);

                                // Apply offense filter
                                referralQuery = applyOffenseTypeFilter(referralQuery, selectedOffenseType);

                                // Apply status filter (this will handle only "pending" referrals for Unsettled)
                                referralQuery = applyStatusFilter(referralQuery, "Unsettled");

                                // Fetch the filtered data
                                referralQuery.get()
                                        .addOnCompleteListener(referralTask -> {
                                            if (referralTask.isSuccessful()) {
                                                QuerySnapshot referralQuerySnapshot = referralTask.getResult();
                                                if (referralQuerySnapshot != null && !referralQuerySnapshot.isEmpty()) {
                                                    for (DocumentSnapshot referralDoc : referralQuerySnapshot.getDocuments()) {
                                                        String name = referralDoc.getString("student_name");
                                                        String offense = referralDoc.getString("offense");
                                                        String violation = referralDoc.getString("violation");
                                                        String referrer = referralDoc.getString(getReferrerField(collection));  // Get the appropriate referrer field
                                                        String referralStatus = referralDoc.getString("status");
                                                        String term = referralDoc.getString("term");

                                                        String referralDetails = buildReferralDetails(name, offense, violation, referrer, referralStatus, term);
                                                        allReferralDetails.add(getEntityTypeName(collection) + " Referral:\n" + referralDetails);
                                                    }
                                                }
                                            }
                                            processedCount[0]++;
                                            if (processedCount[0] == totalEntities[0]) {
                                                generatePDFFromReferralDetails(allReferralDetails);
                                            }
                                        });
                            }
                        }
                    }
                });
    }



    private void fetchReferralHistoryForSettledStudents(String collection, String referralHistoryCollection, String programWithoutSuffix,
                                                        String selectedSemester, String selectedOffenseType,
                                                        List<String> allReferralDetails, final int[] processedCount, final int[] totalEntities) {

        db.collection(collection)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            final int totalEntitiesCount = querySnapshot.size();
                            totalEntities[0] += totalEntitiesCount;

                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                // Query the accepted_status sub-collection for students only
                                Query referralQuery = db.collection(collection)
                                        .document(doc.getId())
                                        .collection(referralHistoryCollection)  // This should point to the accepted_status sub-collection
                                        .whereEqualTo("status", "accepted")  // Filter for accepted status
                                        .whereEqualTo("student_program", programWithoutSuffix)  // Match program (without suffix)
                                        .whereEqualTo("term", selectedSemester);  // Match term

                                // Apply offense filter
                                referralQuery = applyOffenseTypeFilter(referralQuery, selectedOffenseType);


                                // Fetch the filtered data based on the status and offense type
                                referralQuery.get()
                                        .addOnCompleteListener(referralTask -> {
                                            if (referralTask.isSuccessful()) {
                                                QuerySnapshot referralQuerySnapshot = referralTask.getResult();
                                                if (referralQuerySnapshot != null && !referralQuerySnapshot.isEmpty()) {
                                                    for (DocumentSnapshot referralDoc : referralQuerySnapshot.getDocuments()) {
                                                        String name = referralDoc.getString("student_name");
                                                        String offense = referralDoc.getString("offense");
                                                        String violation = referralDoc.getString("violation");
                                                        String referrer = referralDoc.getString("student_referrer"); // Assuming the referrer field for students
                                                        String referralStatus = referralDoc.getString("status");
                                                        String term = referralDoc.getString("term");

                                                        String referralDetails = buildReferralDetails(name, offense, violation, referrer, referralStatus, term);
                                                        allReferralDetails.add("Student Referral:\n" + referralDetails);
                                                    }
                                                }
                                            }
                                            processedCount[0]++;
                                            if (processedCount[0] == totalEntities[0]) {
                                                generatePDFFromReferralDetails(allReferralDetails);
                                            }
                                        });
                            }
                        }
                    }
                });
    }


    private void fetchReferralHistory(String collection, String referralHistoryCollection, String programWithoutSuffix,
                                      String selectedStatus, String selectedSemester, String selectedOffenseType,
                                      List<String> allReferralDetails, final int[] processedCount, final int[] totalEntities) {

        db.collection(collection)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            final int totalEntitiesCount = querySnapshot.size();
                            totalEntities[0] += totalEntitiesCount;

                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                // Query referral history from the given collection and sub-collection
                                Query referralQuery = db.collection(collection)
                                        .document(doc.getId())
                                        .collection(referralHistoryCollection)
                                        .whereEqualTo("student_program", programWithoutSuffix)
                                        .whereEqualTo("term", selectedSemester);

                                // Apply status and offense filters
                                applyStatusFilter(referralQuery, selectedStatus);
                                referralQuery = applyOffenseTypeFilter(referralQuery, selectedOffenseType);


                                // Fetch the filtered data based on the status and offense type
                                referralQuery.get()
                                        .addOnCompleteListener(referralTask -> {
                                            if (referralTask.isSuccessful()) {
                                                QuerySnapshot referralQuerySnapshot = referralTask.getResult();
                                                if (referralQuerySnapshot != null && !referralQuerySnapshot.isEmpty()) {
                                                    for (DocumentSnapshot referralDoc : referralQuerySnapshot.getDocuments()) {
                                                        String name = referralDoc.getString("student_name");
                                                        String offense = referralDoc.getString("offense");
                                                        String violation = referralDoc.getString("violation");
                                                        String referrer = referralDoc.getString(getReferrerField(collection));  // Get the appropriate referrer field
                                                        String referralStatus = referralDoc.getString("status");
                                                        String term = referralDoc.getString("term");

                                                        String referralDetails = buildReferralDetails(name, offense, violation, referrer, referralStatus, term);
                                                        allReferralDetails.add(getEntityTypeName(collection) + " Referral:\n" + referralDetails);
                                                    }
                                                }
                                            }
                                            processedCount[0]++;
                                            if (processedCount[0] == totalEntities[0]) {
                                                generatePDFFromReferralDetails(allReferralDetails);
                                            }
                                        });
                            }
                        }
                    }
                });
    }


    private Query applyOffenseTypeFilter(Query query, String selectedOffenseType) {
        if (selectedOffenseType.equals("Serious Offense")) {
            return query.whereEqualTo("offense", "Serious Offense");
        } else if (selectedOffenseType.equals("Major Offense")) {
            return query.whereEqualTo("offense", "Major Offense");
        } else if (selectedOffenseType.equals("Light Offense")) {
            return query.whereEqualTo("offense", "Light Offense");
        } else {
            // For "All", fetch all offenses.
            return query.whereIn("offense", Arrays.asList("Serious Offense", "Major Offense", "Light Offense"));
        }
    }




    private Query applyStatusFilter(Query query, String selectedStatus) {
        if (selectedStatus.equals("Settled")) {
            return query.whereEqualTo("status", "accepted");
        } else if (selectedStatus.equals("Unsettled")) {
            // If "Unsettled", fetch only "pending" status
            return query.whereEqualTo("status", "pending");
        } else if (selectedStatus.equals("All")) {
            // For "All", fetch both "accepted" and "pending" statuses
            return query.whereIn("status", Arrays.asList("accepted", "pending"));
        }
        return query;  // Default return the original query if no matching status
    }




    private String getReferrerField(String collection) {
        // Return the appropriate referrer field based on the collection type
        if (collection.equals("students")) {
            return "student_referrer";
        } else if (collection.equals("personnel")) {
            return "personnel_referrer";
        } else {
            return "prefect_referrer";
        }
    }

    private String getEntityTypeName(String collection) {
        // Return the appropriate entity name for the referral type
        if (collection.equals("students")) {
            return "Student";
        } else if (collection.equals("personnel")) {
            return "Personnel";
        } else {
            return "Prefect";
        }
    }

    private String buildReferralDetails(String name, String offense, String violation, String referrer, String referralStatus, String term) {
        return new StringBuilder()
                .append("Name: ").append(name != null ? name : "N/A")
                .append("\nOffense: ").append(offense != null ? offense : "N/A")
                .append("\nViolation: ").append(violation != null ? violation : "N/A")
                .append("\nReferrer: ").append(referrer != null ? referrer : "N/A")
                .append("\nStatus: ").append(referralStatus != null ? referralStatus : "N/A")
                .append("\nTerm: ").append(term != null ? term : "N/A")
                .append("\n")
                .toString();
    }

    // Method to generate a PDF from the referral details
    private void generatePDFFromReferralDetails(List<String> allReferralDetails) {
        // Create the PDF document
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 800, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Create a canvas to draw text
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        paint.setTypeface(Typeface.create("Arial", Typeface.NORMAL));  // Set a reliable font
        paint.setAntiAlias(true); // Optional: improves text rendering quality

        // Add header
        int yPosition = 50;
        canvas.drawText("Referral Report", 50, yPosition, paint);
        yPosition += 20;

        // Loop through all referral details and add them to the PDF
        for (String referralDetail : allReferralDetails) {
            // Clean the referral text by removing non-ASCII characters (if needed)
            String cleanedReferralDetail = referralDetail.replaceAll("[^\\x20-\\x7e]", "");  // Removes non-ASCII characters

            // Wrap the cleaned text for proper line breaking
            String[] lines = wrapText(cleanedReferralDetail, paint, 500);  // 500 is the max width for text
            for (String line : lines) {
                canvas.drawText(line, 50, yPosition, paint);
                yPosition += 20;  // Move to the next line
            }
            yPosition += 10;  // Add space between different referral entries
        }

        // Finish the page and document
        pdfDocument.finishPage(page);

        // Save the PDF to a file
        File pdfFile = new File(getContext().getFilesDir(), "referral_report.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            pdfDocument.close();

            // Open the PDF file
            Uri path = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".fileprovider", pdfFile);
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(pdfIntent);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error generating PDF", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to wrap text into lines based on max width
    private String[] wrapText(String text, Paint paint, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" "); // Split text into words

        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            // Check if the word fits on the current line
            if (paint.measureText(currentLine + " " + word) <= maxWidth) {
                currentLine.append(" ").append(word);
            } else {
                // If the word does not fit, add the current line to the list and start a new line
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word); // Start new line with the current word
            }
        }
        // Add the last line
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        // Return the wrapped lines as an array
        return lines.toArray(new String[0]);
    }




    // Method to search student based on Student ID
    private void searchStudent() {
        String studentNo = studentNoEditText.getText().toString().trim();

        if (studentNo.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a student ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the Firestore collection
        CollectionReference studentsRef = db.collection("students");

        // Query to fetch the student document with the given student ID from the accepted_status sub-collection
        Query query = studentsRef.document(studentNo).collection("accepted_status");

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    logsCount = 0;
                    studentName = "";
                    studentId = studentNo;
                    offenses.clear();
                    violations.clear();
                    date.clear();
                    location.clear();
                    referrer_id.clear();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Long logs = document.getLong("logs");

                        if (logs != null && logs == 1) {
                            logsCount++;

                            String offenseField = document.getString("offense");
                            String violationField = document.getString("violation");
                            String dateField = document.getString("date");
                            String locationField = document.getString("location");
                            String refferer_idField = document.getString("referrer_id");

                            if (offenseField != null) {
                                offenses.add(offenseField);
                            }
                            if (violationField != null) {
                                violations.add(violationField);
                            }
                            if (dateField != null) {
                                date.add(dateField);
                            }
                            if (locationField != null) {
                                location.add(locationField);
                            }
                            if (refferer_idField != null) {
                                referrer_id.add(refferer_idField);
                            }
                        }

                        if (document.contains("student_name")) {
                            studentName = document.getString("student_name");
                        }
                    }

                    if (logsCount > 0) {
                        resultLayout.setVisibility(View.VISIBLE); // Show result layout
                        studentNameTextView.setVisibility(View.VISIBLE);
                        pdfButton.setVisibility(View.VISIBLE);
                        pdfButton.setEnabled(true);

                        // Update studentNameTextView with styled content
                        String styledText = "Student Name: <b>" + studentName + "</b><br>" +
                                "Student ID: <b>" + studentId + "</b><br>" +
                                "Violation count: <b>" + logsCount + "</b>";
                        studentNameTextView.setText(Html.fromHtml(styledText, Html.FROM_HTML_MODE_COMPACT));

                    } else {
                        resultLayout.setVisibility(View.GONE); // Hide result layout
                        Toast.makeText(getActivity(), "No logs with value 1 found for this student ID", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    resultLayout.setVisibility(View.GONE); // Hide result layout
                    Toast.makeText(getActivity(), "No data found for this student ID", Toast.LENGTH_SHORT).show();
                }
            } else {
                Exception e = task.getException();
                if (e != null) {
                    e.printStackTrace();
                }
                resultLayout.setVisibility(View.GONE); // Hide result layout
                Toast.makeText(getActivity(), "Error fetching student data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openPDF() {
        if (studentName.isEmpty() || studentId.isEmpty() || offenses.isEmpty() || violations.isEmpty() || date.isEmpty() || location.isEmpty() || referrer_id.isEmpty()) {
            Toast.makeText(getActivity(), "Please search for a student first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate the PDF
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 800, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Create a canvas to draw text and shapes
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);

        // Add Table Title
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("STUDENT DISCIPLINARY CASES", pageInfo.getPageWidth() / 2, 30, paint);

        // Add student details under the title
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Name: " + studentName, 30, 60, paint);
        canvas.drawText("ID: " + studentId, 30, 80, paint);

        // Set up table dimensions
        int startX = 30;
        int startY = 100;

        // Column headers and table content
        String[] headers = {"Offense", "Violation", "Date", "Location", "Referrer ID"};
        List<String[]> dataColumns = Arrays.asList(
                offenses.toArray(new String[0]),
                violations.toArray(new String[0]),
                date.toArray(new String[0]),
                location.toArray(new String[0]),
                referrer_id.toArray(new String[0])
        );

        // Dynamically calculate column widths
        int[] columnWidths = calculateColumnWidths(paint, headers, dataColumns);


        // Draw the table header row
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.LTGRAY);
        canvas.drawRect(startX, startY, startX + Arrays.stream(columnWidths).sum(), startY + 30, paint);

        // Write header text centered in each column
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        int currentX = startX;
        for (int i = 0; i < headers.length; i++) {
            // Calculate text width
            float textWidth = paint.measureText(headers[i]);
            // Center text horizontally in the column
            float textX = currentX + (columnWidths[i] - textWidth) / 2;
            // Adjust the text Y position for vertical alignment
            float textY = startY + 20; // Adjust as needed based on font size
            canvas.drawText(headers[i], textX, textY, paint);
            currentX += columnWidths[i];
        }

        // Draw table header grid lines
        paint.setStyle(Paint.Style.STROKE);
        currentX = startX;
        for (int i = 0; i <= headers.length; i++) {
            canvas.drawLine(currentX, startY, currentX, startY + 30, paint);
            currentX += (i < headers.length) ? columnWidths[i] : 0;
        }
        canvas.drawLine(startX, startY + 30, startX + Arrays.stream(columnWidths).sum(), startY + 30, paint);

        // Draw table content
        int currentY = startY + 30;
        paint.setStyle(Paint.Style.FILL);
        for (int row = 0; row < offenses.size(); row++) {

            currentX = startX;
            for (int col = 0; col < headers.length; col++) {
                String content = dataColumns.get(col)[row];
                canvas.drawText(content, currentX + 10, currentY + 20, paint);
                currentX += columnWidths[col];
            }

            // Draw horizontal grid line
            canvas.drawLine(startX, currentY + 25, startX + Arrays.stream(columnWidths).sum(), currentY + 25, paint);

            currentY += 25;
        }

        // Draw vertical grid lines
        currentX = startX;
        for (int i = 0; i <= headers.length; i++) {
            canvas.drawLine(currentX, startY, currentX, currentY, paint);
            currentX += (i < headers.length) ? columnWidths[i] : 0;
        }

        // Finish the page
        pdfDocument.finishPage(page);

        // Save the PDF to internal storage
        File pdfFile = new File(getContext().getFilesDir(), "student_report_" + studentId + ".pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            pdfDocument.close();

            // Use FileProvider to get URI for the PDF file
            Uri path = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".fileprovider", pdfFile);

            // Open the generated PDF file
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(pdfIntent);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error generating PDF", Toast.LENGTH_SHORT).show();
        }
    }

    // Dynamically calculate column widths
    private int[] calculateColumnWidths(Paint paint, String[] headers, List<String[]> dataColumns) {
        int[] colWidths = new int[headers.length];

        // Calculate width for headers
        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = Math.max(colWidths[i], (int) paint.measureText(headers[i]) + 20);
        }

        // Calculate width for data
        for (int col = 0; col < dataColumns.size(); col++) {
            for (String content : dataColumns.get(col)) {
                colWidths[col] = Math.max(colWidths[col], (int) paint.measureText(content) + 20);
            }
        }

        return colWidths;
    }


    // Method to set up the spinner with a string array
    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(),
                android.R.layout.simple_spinner_item,
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

}
