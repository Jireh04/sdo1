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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


public class dashboard_prefect extends Fragment {

    private FirebaseFirestore db;
    private EditText searchBar;
    private Button searchButton;
    private LinearLayout searchResultsContainer;
    private LinearLayout referralSection;  // Referral buttons container
    private ImageButton pickQrCodeButton;  // QR code scanner button

    private graphs graphObj;

    private int currentPage = 1;  // Current page number
    private int pageSize = 5;      // Number of results per page
    private int totalPages = 0;     // Total number of pages
    private String lastVisibleStudentId = null; // For pagination

    private Button prevButton, nextButton;

    public dashboard_prefect() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard_prefect, container, false);

        // Initialize the graphs class
        graphObj = new graphs(rootView);
        graphObj.fetchDataFromFirestore();

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        searchBar = rootView.findViewById(R.id.search_bar);
        searchButton = rootView.findViewById(R.id.search_button);
        searchResultsContainer = rootView.findViewById(R.id.search_results_container);
        referralSection = rootView.findViewById(R.id.referral_section);  // Referral buttons container
        pickQrCodeButton = rootView.findViewById(R.id.pick_qr_code_button);  // QR code scanner button
        prevButton = rootView.findViewById(R.id.prev_button);
        nextButton = rootView.findViewById(R.id.next_button);

        Button referToGuidanceButton = rootView.findViewById(R.id.referToGuidance);
        Button viewReporters = rootView.findViewById(R.id.ViewReporters);

        // Set OnClickListener for the search button
        searchButton.setOnClickListener(v -> searchStudents());

        // Set OnClickListener for the QR code scanner button
        pickQrCodeButton.setOnClickListener(v -> openQrScanner());



        referToGuidanceButton.setOnClickListener(v -> openReferralDashboard());
        viewReporters.setOnClickListener(v -> openViewReporters());

        updatePaginationButtons();

        return rootView;
    }


    private void searchStudents() {
        // Get the search query
        String queryText = searchBar.getText().toString().trim().toLowerCase();

        // Find the graph layout and hide it
        View graphLayout = getView().findViewById(R.id.graph_include);
        if (graphLayout != null) {
            graphLayout.setVisibility(View.GONE);  // Hide the graph layout
        }

        // Clear previous results before starting a new search
        searchResultsContainer.removeAllViews();

        if (!queryText.isEmpty()) {
            // Reference to the Firestore "students" collection
            CollectionReference studentsRef = db.collection("students");

            // Fetch students from Firestore without limiting the number of results
            studentsRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    boolean foundResults = false;

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // Iterate through the documents
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String name = document.getString("name");
                            String studentId = document.getString("student_id");
                            String program = document.getString("program");
                            String year = document.getString("year");
                            String block = document.getString("block");
                            String remarks = document.getString("remarks");
                            String contact = document.getString("student_contact");

                            // Check if the name or student ID contains the search query (case-insensitive)
                            if ((name != null && name.toLowerCase().contains(queryText)) ||
                                    (studentId != null && studentId.toLowerCase().contains(queryText))) {
                                foundResults = true;

                                RelativeLayout userLayout = new RelativeLayout(getActivity());
                                userLayout.setPadding(10, 10, 10, 10);

                                // Create the TextView for displaying student info
                                TextView studentTextView = new TextView(getActivity());
                                studentTextView.setText(studentId + " | " + name + " | " + program);
                                studentTextView.setTextSize(name.length() > 18 ? 14 : 16); // Adjust text size if the name is long
                                studentTextView.setEllipsize(TextUtils.TruncateAt.END); // Truncate with "..." if text is too long
                                studentTextView.setSingleLine(true); // Keep text on a single line
                                studentTextView.setId(View.generateViewId());

                                RelativeLayout.LayoutParams userInfoParams = new RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT
                                );
                                userInfoParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                                userInfoParams.addRule(RelativeLayout.CENTER_VERTICAL);
                                studentTextView.setLayoutParams(userInfoParams);

                                // Create the Button for action
                                Button addButton = new Button(getActivity());
                                addButton.setText("+");
                                addButton.setBackgroundResource(R.drawable.round_button);
                                addButton.setTextColor(Color.WHITE);
                                addButton.setAllCaps(false);
                                addButton.setTextSize(24);
                                addButton.setPadding(20, 13, 20, 13);
                                addButton.setId(View.generateViewId());

                                RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(
                                        140,
                                        140
                                );
                                buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                                buttonLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                                buttonLayoutParams.setMargins(0, 0, 35, 0); // Add right margin for spacing
                                addButton.setLayoutParams(buttonLayoutParams);

                                // Make the TextView clickable
                                studentTextView.setOnClickListener(v -> {
                                    // Fetch violations for the selected student
                                   navigateToPrefectView(studentId, name, program, year, block, remarks, contact); // Use studentId here
                                });

                                // Set the onClickListener for the button
                                addButton.setOnClickListener(v -> {
                                    // Show the Add Violator dialog when the button is clicked
                                    showAddViolatorDialog(studentId, name);
                                });

                                // Add the TextView and Button to the LinearLayout
                                userLayout.addView(studentTextView);
                                userLayout.addView(addButton);

                                // Add the studentLayout to the searchResultsContainer
                                searchResultsContainer.addView(userLayout);
                            }
                        }

                        // If no results are found, show a message
                        if (!foundResults) {
                            TextView noResultsTextView = new TextView(getContext());
                            noResultsTextView.setText("No students found matching '" + queryText + "'");
                            noResultsTextView.setPadding(0, 180, 0, 0); // Add top padding
                            searchResultsContainer.addView(noResultsTextView);
                        }
                    } else {
                        // If no students matched, show a "No students found" message
                        TextView noResultsTextView = new TextView(getContext());
                        noResultsTextView.setText("No students found matching '" + queryText + "'");
                        noResultsTextView.setPadding(0, 180, 0, 0); // Add top padding
                        searchResultsContainer.addView(noResultsTextView);
                    }
                } else {
                    // Handle the error if the query fails
                    TextView errorTextView = new TextView(getContext());
                    errorTextView.setText("Error: " + task.getException().getMessage());
                    errorTextView.setPadding(0, 180, 0, 0); // Add top padding
                    searchResultsContainer.addView(errorTextView);
                }
            });
        } else {
            // If the search query is empty, show a message and clear previous results
            TextView emptySearchTextView = new TextView(getContext());
            emptySearchTextView.setText("Please enter a search query.");
            emptySearchTextView.setPadding(0, 180, 0, 0); // Add top padding
            searchResultsContainer.addView(emptySearchTextView);
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


    // Add this method to manage button visibility and listeners
    private void updatePaginationButtons() {
        if (currentPage > 1) {
            prevButton.setVisibility(View.VISIBLE); // Show previous button
        } else {
            prevButton.setVisibility(View.GONE); // Hide previous button
        }

        if (currentPage < totalPages) {
            nextButton.setVisibility(View.VISIBLE); // Show next button
        } else {
            nextButton.setVisibility(View.GONE); // Hide next button
        }

        // Set onClickListeners for pagination buttons
        prevButton.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--; // Decrement page number
                searchStudents(); // Re-fetch students for the previous page
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++; // Increment page number
                searchStudents(); // Re-fetch students for the next page
            }
        });
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
        // Inflate the custom dialog layout using getContext() instead of this
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.add_violator, null);

        // Find the UI elements in the dialog
        EditText dateTimeEditText = dialogView.findViewById(R.id.dateTimeEditText);
        EditText reporterEditText = dialogView.findViewById(R.id.reporterEditText);
        EditText locationEditText = dialogView.findViewById(R.id.locationEditText);
        EditText reporterIdEditText = dialogView.findViewById(R.id.reporterIdEditText); // New field for Reporter ID
        Spinner violationSpinner = dialogView.findViewById(R.id.violationSpinner);
        EditText remarksEditText = dialogView.findViewById(R.id.remarksEditText);
        Button submitButton = dialogView.findViewById(R.id.submitButton);
        String status = "accepted";

        // Set the current date and time automatically in the dateTimeEditText field
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Customize the format as needed
        String currentDateAndTime = sdf.format(calendar.getTime());
        dateTimeEditText.setText(currentDateAndTime);

        // Fetch the prefect ID and set it in the reporterIdEditText
        String reporterId = PrefectSession.getPrefectId(); // Replace this with your method to get the prefect ID
        reporterIdEditText.setText(reporterId); // Populate the EditText with the fetched ID
        reporterIdEditText.setEnabled(false); // Optionally, disable editing for this field

        // Fetch violation types from Firestore and set up the violation spinner
        fetchViolationTypes(violationSpinner); // Pass the violationSpinner to the method

        // Build the AlertDialog using getActivity() to get the context
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        builder.setTitle("Add Violation");

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Set a listener for the submit button
        submitButton.setOnClickListener(v -> {
            // Get the input values
            String dateTime = dateTimeEditText.getText().toString().trim();
            String reporter = reporterEditText.getText().toString().trim();
            String location = locationEditText.getText().toString().trim();
            String violation = violationSpinner.getSelectedItem().toString();
            String remarks = remarksEditText.getText().toString().trim();

            // Validate input fields for special characters or excessive whitespace
            if (!isValidInput(reporter) || !isValidInput(location) || !isValidInput(remarks)) {
                Toast.makeText(getContext(), "Fields must contain meaningful text and cannot be only special characters or whitespace.", Toast.LENGTH_SHORT).show();
            } else if (dateTime.isEmpty() || reporter.isEmpty() || reporterId.isEmpty() || location.isEmpty() || violation.equals("Select a Violation") || remarks.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            } else {
                // Validate violation selection
                if (violation.equals("Select a Violation")) {
                    Toast.makeText(getContext(), "Please select a valid violation.", Toast.LENGTH_SHORT).show();
                    return; // Exit the method if no valid violation is selected
                }

                // Prepare the data to be saved to Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Create a new violator entry with the specified fields
                Map<String, Object> violatorData = new HashMap<>();
                violatorData.put("date", dateTime);
                violatorData.put("prefect_referrer", reporter);
                violatorData.put("referrer_id", reporterId); // Use the fetched reporter ID
                violatorData.put("location", location);
                violatorData.put("violation", violation);
                violatorData.put("remarks", remarks);
                violatorData.put("student_id", studId);  // Add the studentId
                violatorData.put("student_name", name);  // Add the studentName
                violatorData.put("status", status);  // Add the status

                // Format the document ID as yyyy-MM-dd_HH:mm:ss_studentId
                String formattedDateTime = dateTime.replace(" ", "_"); // Replace space with underscore
                String documentId = formattedDateTime + "_" + studId; // Construct the document ID using studentId

                // Save to the prefect_referral_history subcollection
                db.collection("prefect")
                        .document(reporterId) // Use the reporterId for the main document
                        .collection("prefect_referral_history")
                        .document(documentId) // Use the formatted document ID for history
                        .set(violatorData) // Set the violator data
                        .addOnSuccessListener(documentReference -> {
                            // Show a success message
                            Toast.makeText(getContext(), "Violator Added!", Toast.LENGTH_SHORT).show();

                            // Save the same data to the student's accepted_status sub-collection
                            db.collection("students")
                                    .document(studId) // Use the studentId to get the student's document
                                    .collection("accepted_status")
                                    .document(documentId) // Use the same document ID as for the referral history
                                    .set(violatorData) // Save the same violator data to the accepted_status
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully saved to accepted_status
                                        Log.d("Firestore", "Data saved to accepted_status.");
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure to save to accepted_status
                                        Toast.makeText(getContext(), "Error saving to accepted_status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                            // Close the dialog
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            // Show an error message if the operation fails
                            Toast.makeText(getContext(), "Error adding to referral history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Add a cancel button to close the dialog
        builder.setNegativeButton("Cancel", (dialogInterface, which) -> dialog.dismiss());

        // Show the dialog
        dialog.show();
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
