package com.finals.lagunauniversitysdo;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.widget.AdapterView;

import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.view.MotionEvent;
import android.view.Gravity;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.Spinner;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.widget.Button;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

public class dashboard_prefect extends Fragment {

    private FirebaseFirestore db;
    private EditText searchBar;
    private Button searchButton;
    private LinearLayout searchResultsContainer;
    private LinearLayout referralSection;  // Referral buttons container
    private ImageButton pickQrCodeButton;  // QR code scanner button

    private Button prevButton;
    private Button nextButton;
    private graphs graphObj;
    private List<DocumentSnapshot> allDocuments;
    private AtomicInteger currentPage; // To keep track of the current page
    private static final int RESULTS_PER_PAGE = 5; // Changed to 3 results per page

    private LinearLayout paginationLayout, pageNumberContainer;
    private DocumentSnapshot lastVisible; // To track the last document for pagination

    public dashboard_prefect() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard_prefect, container, false);

        // Initialize the graphs class
        graphObj = new graphs(rootView);
        graphObj.fetchDataFromFirestore();

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize the buttons and layout
        prevButton = rootView.findViewById(R.id.previous_button); // Make sure the ID matches the layout XML
        nextButton = rootView.findViewById(R.id.next_button); // Make sure the ID matches the layout XML
        paginationLayout = rootView.findViewById(R.id.pagination_layout);
        pageNumberContainer = rootView.findViewById(R.id.page_number_container);

        // Initialize views
        searchBar = rootView.findViewById(R.id.search_bar);
        searchButton = rootView.findViewById(R.id.search_button);
        searchResultsContainer = rootView.findViewById(R.id.search_results_container);
        referralSection = rootView.findViewById(R.id.referral_section);  // Referral buttons container
        pickQrCodeButton = rootView.findViewById(R.id.pick_qr_code_button);  // QR code scanner button

        Button referToGuidanceButton = rootView.findViewById(R.id.referToGuidance);
        Button viewReporters = rootView.findViewById(R.id.ViewReporters);

        currentPage = new AtomicInteger(0);
        allDocuments = new ArrayList<>();

        // Set OnClickListener for the search button
        searchButton.setOnClickListener(v -> searchStudents());

        // Set OnClickListener for the QR code scanner button
        pickQrCodeButton.setOnClickListener(v -> openQrScanner());

        referToGuidanceButton.setOnClickListener(v -> openReferralDashboard());
        viewReporters.setOnClickListener(v -> openViewReporters());

        // Set onClick listeners for pagination buttons
        prevButton.setOnClickListener(v -> showPreviousPage()); // Using prevButton here
        nextButton.setOnClickListener(v -> showNextPage()); // Using nextButton here

        return rootView;
    }


    private void searchStudents() {
        String queryText = searchBar.getText().toString().trim().toLowerCase();

        if (queryText.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a search term", Toast.LENGTH_SHORT).show();
            paginationLayout.setVisibility(View.GONE);

            // Show graph again if no search term is entered
            showGraph();

            return;
        }

        LinearLayout searchResultsContainer = getView().findViewById(R.id.search_results_container);
        searchResultsContainer.removeAllViews();
        allDocuments.clear(); // Clear previous search results

        // Hide the graph when search is active
        hideGraph();

        // Query Firestore for the students collection
        db.collection("students").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        allDocuments.clear(); // Clear the list of previous results

                        // Loop through each document in the result set
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String name = document.getString("name");
                            String studentId = document.getString("student_id");
                            String program = document.getString("program");
                            String year = document.getString("year");
                            String block = document.getString("block");
                            String remarks = document.getString("remarks");
                            String contact = document.getString("student_contact");

                            // Ensure the name or studentId contains the search term (case insensitive)
                            if ((name != null && name.toLowerCase().contains(queryText)) ||
                                    (studentId != null && studentId.toLowerCase().contains(queryText))) {

                                // Add the document to the allDocuments list
                                allDocuments.add(document);
                            }
                        }

                        if (allDocuments.isEmpty()) {
                            Toast.makeText(getActivity(), "No matching results", Toast.LENGTH_SHORT).show();
                            paginationLayout.setVisibility(View.GONE);
                        } else {
                            currentPage.set(0); // Reset to first page
                            showPage(getView()); // Show first page of results
                            updatePaginationControls(); // Update pagination controls
                            paginationLayout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(getActivity(), "No data available", Toast.LENGTH_SHORT).show();
                        paginationLayout.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(getActivity(), "Error fetching data", Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void showPage(View rootView) {
        LinearLayout searchResultsContainer = rootView.findViewById(R.id.search_results_container);
        searchResultsContainer.removeAllViews();

        int startIndex = currentPage.get() * RESULTS_PER_PAGE;
        int endIndex = Math.min(startIndex + RESULTS_PER_PAGE, allDocuments.size());

        // Loop through the documents for the current page
        for (int i = startIndex; i < endIndex; i++) {
            DocumentSnapshot document = allDocuments.get(i);
            String name = document.getString("name");
            String studentId = document.getString("student_id");
            String program = document.getString("program");
            String year = document.getString("year");
            String block = document.getString("block");
            String remarks = document.getString("remarks");
            String contact = document.getString("student_contact");

            // Create and add each student result to the layout
            addSearchResultToLayout(name, studentId, program, year, block, remarks, contact, searchResultsContainer);
        }

        // Update pagination controls after showing the page
        updatePaginationControls();
    }

    private void addSearchResultToLayout(String name, String studentId, String program, String year, String block, String remarks, String contact, LinearLayout container) {
        RelativeLayout userLayout = new RelativeLayout(getActivity());
        userLayout.setPadding(10, 10, 10, 10);

        // Create TextView for displaying student info
        TextView studentTextView = new TextView(getActivity());
        studentTextView.setPadding(30, 0, 0, 0);
        studentTextView.setText(studentId + " | " + name + " | " + program);
        studentTextView.setTextSize(name.length() > 18 ? 14 : 16);
        studentTextView.setEllipsize(TextUtils.TruncateAt.END);
        studentTextView.setSingleLine(true);
        studentTextView.setId(View.generateViewId());

        RelativeLayout.LayoutParams userInfoParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        userInfoParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        userInfoParams.addRule(RelativeLayout.CENTER_VERTICAL);
        studentTextView.setLayoutParams(userInfoParams);

        // Create the "+" Button
        Button addButton = new Button(getActivity());
        addButton.setText("+");
        addButton.setBackgroundResource(R.drawable.round_button);
        addButton.setTextColor(Color.WHITE);
        addButton.setAllCaps(false);
        addButton.setTextSize(24);
        addButton.setPadding(20, 13, 20, 13);
        addButton.setId(View.generateViewId());

        RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(
                130,
                130
        );
        buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        buttonLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        buttonLayoutParams.setMargins(0, 0, 25, 0); // Add right margin for spacing
        addButton.setLayoutParams(buttonLayoutParams);

        // Set the onClickListener for the TextView (navigate to a detailed view)
        studentTextView.setOnClickListener(v -> {
            // Fetch violations for the selected student and navigate to detailed view
            navigateToPrefectView(studentId, name, program, year, block, remarks, contact); // Use studentId here
        });

        // Set the onClickListener for the Button (Show the Add Violator dialog)
        addButton.setOnClickListener(v -> {
            // Show the Add Violator dialog when the button is clicked
            showAddViolatorDialog(studentId, name);
        });

        // Add the TextView and Button to the RelativeLayout
        userLayout.addView(studentTextView);
        userLayout.addView(addButton);

        // Add the userLayout to the searchResultsContainer
        container.addView(userLayout);
    }

    private void updatePaginationControls() {
        if (prevButton != null && nextButton != null && pageNumberContainer != null) {
            // Enable/disable buttons based on the current page
            prevButton.setEnabled(currentPage.get() > 0);
            nextButton.setEnabled((currentPage.get() + 1) * RESULTS_PER_PAGE < allDocuments.size());

            // Clear previous page number indicators
            pageNumberContainer.removeAllViews();
            int totalPages = (int) Math.ceil((double) allDocuments.size() / RESULTS_PER_PAGE);

            // Show page numbers
            for (int i = 0; i < totalPages; i++) {
                TextView pageNumberTextView = new TextView(getActivity());
                pageNumberTextView.setText(String.valueOf(i + 1));
                pageNumberTextView.setPadding(8, 8, 8, 8);
                pageNumberTextView.setTextColor(i == currentPage.get() ? Color.BLUE : Color.BLACK);
                pageNumberContainer.addView(pageNumberTextView);
            }
        }
    }

    private void showPreviousPage() {
        if (currentPage.get() > 0) {
            currentPage.decrementAndGet();
            showPage(getView());
        }
    }

    private void showNextPage() {
        if ((currentPage.get() + 1) * RESULTS_PER_PAGE < allDocuments.size()) {
            currentPage.incrementAndGet();
            showPage(getView());
        }
    }

    // Method to hide the graph dynamically
    private void hideGraph() {
        View graphContainer = getView().findViewById(R.id.graph_include); // Replace with the actual ID of your graph container
        if (graphContainer != null) {
            graphContainer.setVisibility(View.GONE);
        }
    }

    // Method to show the graph dynamically
    private void showGraph() {
        View graphContainer = getView().findViewById(R.id.graph_include); // Replace with the actual ID of your graph container
        if (graphContainer != null) {
            graphContainer.setVisibility(View.VISIBLE);
        }
    }



    private void openReferralDashboard() {
        // Create a new instance of the PrefectReferralDashboardFragment
        PrefectReferralDashboard referralDashboardFragment = new PrefectReferralDashboard();

        // Use FragmentTransaction to replace the current fragment with the referral dashboard fragment
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, referralDashboardFragment)  // Replace with the correct fragment
                .addToBackStack(null)  // Add to back stack for backward navigation
                .commit();  // Commit the transaction
    }






    private void navigateToPrefectView(String studentId, String name, String program, String year, String block, String remarks, String contact) {
        // Set mock or passed values for violations, referrerId, and date.
        String violations = "No violations found.";  // You can set this to a default message or pass any other value.
           // This can be fetched or set to a default value.
        String date = "Unknown";                     // This can be fetched or set to a default value.

        // Create an instance of the PrefectView fragment with all necessary parameters
        PrefectView prefectViewFragment = PrefectView.newInstance(studentId, name, program, contact, year, block, violations, remarks, date);

        // Replace the current fragment with PrefectView
        getParentFragmentManager().beginTransaction()  // Use getParentFragmentManager() if inside a fragment
                .replace(R.id.fragment_container, prefectViewFragment) // Replace with the ID of your container
                .addToBackStack(null) // Optional: Add to back stack for navigation
                .commit(); // Commit the transaction
    }


    private void openQrScanner() {
        // Create an Intent to launch the PrefectQRScannerActivity
        Intent intent = new Intent(getActivity(), Prefect_QRScannerActivity.class);
        startActivity(intent);
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

    private void openViewReporters() {
        ReportersFragment reportersFragment = new ReportersFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ReportersFragment())
                .commit();

    }

}
