package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.content.Intent;

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
    private LinearLayout boxSection;       // Box section
    private ImageButton pickQrCodeButton;  // QR code scanner button

    private graphs graphObj;

    private int currentPage = 1;
    private int pageSize = 2;  // Number of results per page
    private int totalPages = 0;
    private String lastVisibleStudentId = null;  // Used for pagination


    public dashboard_prefect() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard_prefect, container, false);

        // Initialize the graphs class
        graphObj = new graphs(rootView);

        // Fetch data and set up charts
        graphObj.fetchDataFromFirestore();

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        searchBar = rootView.findViewById(R.id.search_bar);
        searchButton = rootView.findViewById(R.id.search_button);
        searchResultsContainer = rootView.findViewById(R.id.search_results_container);
        referralSection = rootView.findViewById(R.id.referral_section);  // Referral buttons container
        pickQrCodeButton = rootView.findViewById(R.id.pick_qr_code_button);  // QR code scanner button

        Button referToGuidanceButton = rootView.findViewById(R.id.referToGuidance);
        Button viewReporters = rootView.findViewById(R.id.ViewReporters);

        // Set OnClickListener for the search button
        searchButton.setOnClickListener(v -> searchStudents());

        // Set OnClickListener for the QR code scanner button
        pickQrCodeButton.setOnClickListener(v -> openQrScanner());

        referToGuidanceButton.setOnClickListener(v -> openReferralDashboard());
        viewReporters.setOnClickListener(v -> openViewReporters());



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

        // Clear previous results before starting new search
        searchResultsContainer.removeAllViews();

        if (!queryText.isEmpty()) {
            // Reference to the Firestore "students" collection
            CollectionReference studentsRef = db.collection("students");

            // Fetch students from Firestore
            studentsRef.get()  // Fetch all students (you can modify this query if needed)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            boolean foundResults = false;

                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Iterate through the documents and filter by name (case-insensitive and partial match)
                                boolean isFirstItem = true; // Flag to track the first item

                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    String name = document.getString("name");
                                    String studId = document.getString("stud_id");

                                    // Check if the name contains the search query (case-insensitive)
                                    if (name != null && name.toLowerCase().contains(queryText)) {
                                        foundResults = true;

                                        // Create a LinearLayout to contain the student's name and button
                                        LinearLayout studentLayout = new LinearLayout(getContext());
                                        studentLayout.setOrientation(LinearLayout.HORIZONTAL);

                                        // Apply top padding only for the first item
                                        if (isFirstItem) {
                                            studentLayout.setPadding(0, 180, 0, 8);  // Top padding for the first item
                                            isFirstItem = false; // After the first item, set the flag to false
                                        } else {
                                            studentLayout.setPadding(0, 8, 0, 8);  // No top padding for subsequent items
                                        }

                                        studentLayout.setGravity(Gravity.CENTER_VERTICAL); // Align the content vertically in the center

                                        // Create a TextView for the student's ID and name
                                        TextView studentTextView = new TextView(getContext());
                                        studentTextView.setText("ID: " + studId + " - Name: " + name);
                                        studentTextView.setTextSize(16);
                                        studentTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)); // Make it take all the available space

                                        // Add top padding to the TextView itself (if needed)
                                        studentTextView.setPadding(0, 10, 0, 0);  // Apply top padding to the TextView

                                        // Make the TextView clickable
                                        studentTextView.setOnClickListener(v -> {
                                            // Launch the PrefectView (replace with your own method to navigate)
                                            navigateToPrefectView(studId, name);
                                        });

                                        // Create a Button (or ImageButton) for the "Add" action
                                        Button addButton = new Button(getContext());
                                        addButton.setText("+"); // Text for the button (you can use an image as well)
                                        addButton.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                                        // Set the onClickListener for the button
                                        addButton.setOnClickListener(v -> {
                                            // Show the Add Violator dialog when the button is clicked, passing the student details
                                            showAddViolatorDialog(studId, name); // Pass studId and name
                                        });

                                        // Add the TextView and Button to the LinearLayout
                                        studentLayout.addView(studentTextView);
                                        studentLayout.addView(addButton);

                                        // Add the studentLayout to the searchResultsContainer
                                        searchResultsContainer.addView(studentLayout);
                                    }
                                }

                                // If no students matched, show a "No students found" message
                                if (!foundResults) {
                                    TextView noResultsTextView = new TextView(getContext());
                                    noResultsTextView.setText("No students found matching '" + queryText + "'");
                                    noResultsTextView.setPadding(0, 180, 0, 0); // Add top padding
                                    searchResultsContainer.addView(noResultsTextView);
                                }
                            } else {
                                // No students found in Firestore
                                TextView noResultsTextView = new TextView(getContext());
                                noResultsTextView.setText("No students found.");
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

    // Method to navigate to PrefectView (replace with actual navigation code)
    private void navigateToPrefectView(String studId, String name) {
        // Assuming PrefectViewActivity is the activity that shows the details of the student
        Intent intent = new Intent(getContext(), PrefectView.class);
        intent.putExtra("STUDENT_ID", studId); // Pass the student ID
        intent.putExtra("STUDENT_NAME", name); // Pass the student name
        startActivity(intent);  // Start the activity
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
        Spinner violationSpinner = dialogView.findViewById(R.id.violationSpinner);
        EditText remarksEditText = dialogView.findViewById(R.id.remarksEditText);
        Button submitButton = dialogView.findViewById(R.id.submitButton);

        // Set the current date and time automatically in the dateTimeEditText field
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Customize the format as needed
        String currentDateAndTime = sdf.format(calendar.getTime());
        dateTimeEditText.setText(currentDateAndTime);

        // Create a list of violations
        String[] violations = {"-select violation-", "Light Offense", "Serious Offense", "Major Offense"};

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
            if (dateTime.isEmpty() || reporter.isEmpty() || location.isEmpty() || violation.equals("-select violation-")) {
                Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            } else {
                // Prepare the data to be saved to Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Create a new violator entry with the specified fields
                Map<String, Object> violatorData = new HashMap<>();
                violatorData.put("date", dateTime);
                violatorData.put("prefect_referrer", reporter);
                violatorData.put("location", location);
                violatorData.put("violation", violation);
                violatorData.put("remarks", remarks);
                violatorData.put("student_id", studId);  // Add the studentId
                violatorData.put("student_name", name);  // Add the studentName

                // Add the data to the 'prefect_referral_history' collection
                db.collection("prefect_referral_history")  // Ensure this collection name is correct
                        .add(violatorData)  // Auto-generate a document ID
                        .addOnSuccessListener(documentReference -> {
                            // Show a success message
                            Toast.makeText(getContext(), "Violator Added!", Toast.LENGTH_SHORT).show();
                            // Close the dialog
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            // Show an error message if the operation fails
                            Toast.makeText(getContext(), "Error adding violator: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

        reporters ViewReports = new reporters();

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, ViewReports)
                .addToBackStack(null)
                .commit();

    }


}
