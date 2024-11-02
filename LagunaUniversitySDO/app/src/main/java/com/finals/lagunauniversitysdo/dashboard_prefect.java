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

import android.widget.ArrayAdapter;
import android.widget.ImageButton;

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

            // Fetch students from Firestore
            studentsRef.orderBy("student_id")  // Ensure there's a field to order by
                    .startAfter(lastVisibleStudentId)  // Start after the last visible student ID
                    .limit(pageSize)  // Limit to the number of results per page
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            totalPages = (int) Math.ceil((double) querySnapshot.size() / pageSize);
                            updatePaginationButtons(); // Update button visibility
                            boolean foundResults = false;

                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Iterate through the documents
                                boolean isFirstItem = true; // Flag to track the first item
                                lastVisibleStudentId = null; // Reset for new search

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
                                            fetchViolations(studentId, name, program, year, block, remarks, contact); // Use studentId here
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

                                        // Store the last visible student ID for pagination
                                        lastVisibleStudentId = document.getString("student_id");
                                    }
                                }

                                // Check for pagination
                                totalPages = (int) Math.ceil((double) querySnapshot.size() / pageSize);
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



    private void fetchViolations(String studentId, String name, String program, String year, String block, String remarks, String contact) {
        // Reference to the Firestore collections
        CollectionReference studentReferrals = db.collection("student_refferal_history");
        CollectionReference prefectReferrals = db.collection("prefect_referral_history");
        CollectionReference personnelReferrals = db.collection("personnel_refferal_history");

        // Initialize a StringBuilder to accumulate violations
        StringBuilder violationsBuilder = new StringBuilder();

        // Fetch violations from each collection using student_id
        studentReferrals.whereEqualTo("student_id", studentId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot studentSnapshot = task.getResult();
                if (studentSnapshot != null && !studentSnapshot.isEmpty()) {
                    for (QueryDocumentSnapshot document : studentSnapshot) {
                        String violation = document.getString("violation");
                        String remarksStudent = document.getString("remarks"); // Fetch remarks
                        String date = document.getString("date"); // Fetch date
                        if (violation != null) {
                            // Combine remarksStudent and violation into one string with date last
                            violationsBuilder.append(remarksStudent != null ? "(Remarks: " + remarksStudent + ") " : "")
                                    .append(violation)
                                    .append(date != null ? " [" + date + "]" : "") // Date now comes last
                                    .append("\n");
                        }
                    }
                }
            } else {
                Log.e("FetchViolations", "Error fetching student violations: " + task.getException().getMessage());
            }

            // Fetch prefect referrals
            prefectReferrals.whereEqualTo("student_id", studentId).get().addOnCompleteListener(prefectTask -> {
                if (prefectTask.isSuccessful()) {
                    QuerySnapshot prefectSnapshot = prefectTask.getResult();
                    if (prefectSnapshot != null && !prefectSnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : prefectSnapshot) {
                            String violation = document.getString("violation");
                            String remarksStudent = document.getString("remarks"); // Fetch remarks
                            String date = document.getString("date"); // Fetch date
                            if (violation != null) {
                                // Combine remarks and violation into one string with date last
                                violationsBuilder.append(remarksStudent != null ? "(Remarks: " + remarksStudent + ") " : "")
                                        .append(violation)
                                        .append(date != null ? " [" + date + "]" : "") // Date now comes last
                                        .append("\n");
                            }
                        }
                    }
                } else {
                    Log.e("FetchViolations", "Error fetching prefect violations: " + prefectTask.getException().getMessage());
                }

                // Fetch personnel referrals
                personnelReferrals.whereEqualTo("student_id", studentId).get().addOnCompleteListener(personnelTask -> {
                    if (personnelTask.isSuccessful()) {
                        QuerySnapshot personnelSnapshot = personnelTask.getResult();
                        if (personnelSnapshot != null && !personnelSnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : personnelSnapshot) {
                                String violation = document.getString("violation");
                                String remarksStudent = document.getString("remarks"); // Fetch remarks
                                String date = document.getString("date"); // Fetch date
                                if (violation != null) {
                                    // Combine remarks and violation into one string with date last
                                    violationsBuilder.append(remarksStudent != null ? "(Remarks: " + remarksStudent + ") " : "")
                                            .append(violation)
                                            .append(date != null ? " [" + date + "]" : "") // Date now comes last
                                            .append("\n");
                                }
                            }
                        }
                    } else {
                        Log.e("FetchViolations", "Error fetching personnel violations: " + personnelTask.getException().getMessage());
                    }

                    // Send the violations to PrefectView
                    String violations = violationsBuilder.toString().trim(); // Get final string
                    navigateToPrefectView(studentId, name, program, year, block, violations, remarks, contact); // Changed to studentId
                });
            });
        });
    }

    // Method to navigate to PrefectView
    private void navigateToPrefectView(String studentId, String name, String program, String year, String block, String violations, String contact, String remarks) {
        // Create an instance of the PrefectView fragment
        PrefectView prefectViewFragment = PrefectView.newInstance(studentId, name, program, contact, remarks, year, block, violations);

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

        // Create a list of violations
        String[] violations = { "-select violation-", "Light Offense", "Serious Offense", "Major Offense" };

        // Set up an ArrayAdapter to populate the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, violations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        violationSpinner.setAdapter(adapter); // Set the adapter to the Spinner

        // Build the AlertDialog using getActivity() to get the context
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        builder.setTitle("Add Violator");

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

            // Simple validation: check if required fields are empty
            if (dateTime.isEmpty() || reporter.isEmpty() || reporterId.isEmpty() || location.isEmpty() || violation.equals("-select violation-") || remarks.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            } else {
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


    private void openReferralDashboard() {
        // Create a new instance of the PrefectReferralDashboardFragment
        PrefectReferralDashboard referralDashboardFragment = new PrefectReferralDashboard();

        // Use FragmentTransaction to replace the current fragment with the referral dashboard fragment
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, referralDashboardFragment)  // Make sure R.id.fragment_container is the ID of your container in the layout
                .addToBackStack(null)  // Add to back stack to allow user to go back
                .commit();
    }

    private void openViewReporters() {
        ReportersFragment reportersFragment = new ReportersFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ReportersFragment())
                .commit();

    }

}
