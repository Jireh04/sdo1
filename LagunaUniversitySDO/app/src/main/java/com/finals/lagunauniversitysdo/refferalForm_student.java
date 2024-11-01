package com.finals.lagunauniversitysdo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.graphics.Color;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import android.util.Log;

public class refferalForm_student extends Fragment {
    private static final int REQUEST_CODE_QR_SCAN = 1;
    private static final int ITEMS_PER_PAGE = 5;

    private EditText searchBar;
    private Button searchButton;
    private FirebaseFirestore db;
    private Set<String> addedUserIds;
    private AtomicInteger currentPage;
    private List<DocumentSnapshot> allDocuments;
    private Map<String, DocumentSnapshot> addedUserDetails; // Store added users

    private Button prevButton, nextButton, proceedtoRefferal;
    private LinearLayout paginationControls, pageNumberContainer;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_refferal_form_student, container, false);

        // Initialize UI components
        initUI(rootView);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        addedUserIds = new HashSet<>();
        addedUserDetails = new HashMap<>();
        currentPage = new AtomicInteger(0);
        allDocuments = new ArrayList<>();

        // Set button click listeners
        setButtonListeners(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        searchBar = rootView.findViewById(R.id.search_bar);
        searchButton = rootView.findViewById(R.id.search_button);
        prevButton = rootView.findViewById(R.id.prev_button);
        nextButton = rootView.findViewById(R.id.next_button);
        paginationControls = rootView.findViewById(R.id.pagination_controls);
        pageNumberContainer = rootView.findViewById(R.id.page_number_container);
        proceedtoRefferal = rootView.findViewById(R.id.proceedtoRefferal);
    }

    private void setButtonListeners(View rootView) {
        searchButton.setOnClickListener(v -> performSearch(rootView));
        prevButton.setOnClickListener(v -> showPreviousPage());
        nextButton.setOnClickListener(v -> showNextPage());

        ImageButton pickQRCodeButton = rootView.findViewById(R.id.pick_qr_code_button);
        pickQRCodeButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), QRScannerActivity.class);
            startActivityForResult(intent, REQUEST_CODE_QR_SCAN);
        });

        proceedtoRefferal.setOnClickListener(v -> proceedToReferral());
    }
    private void proceedToReferral() {
        // Check if any users have been added
        if (addedUserIds.isEmpty()) {
            Toast.makeText(getActivity(), "No users added. Please add at least one user before proceeding.", Toast.LENGTH_SHORT).show();
            return; // Stop execution if no users are added
        }

        String studentId = UserSession.getStudentId();
        String studentName = UserSession.getStudentName();
        String email = UserSession.getEmail();
        Long contactNum = UserSession.getContactNum();
        String program = UserSession.getProgram();

        if (studentId != null) {
            Intent intent = new Intent(getActivity(), form.class);
            intent.putExtra("STUDENT_NAME", studentName);
            intent.putExtra("EMAIL", email);
            intent.putExtra("CONTACT_NUM", contactNum != null ? contactNum : 0L); // Default value
            intent.putExtra("PROGRAM", program);
            intent.putExtra("STUDENT_ID", studentId);

            // Prepare user details for added users
            if (!addedUserIds.isEmpty()) {
                ArrayList<String> userNames = new ArrayList<>();
                ArrayList<String> userPrograms = new ArrayList<>();
                ArrayList<String> userContacts = new ArrayList<>();
                ArrayList<String> userStudentIds = new ArrayList<>(); // New ArrayList for student_id

                for (String userId : addedUserIds) {
                    DocumentSnapshot userDoc = addedUserDetails.get(userId);
                    if (userDoc != null) {
                        userNames.add(userDoc.getString("name"));
                        userPrograms.add(userDoc.getString("program"));
                        userContacts.add(userDoc.getString("contact"));
                        userStudentIds.add(userDoc.getString("student_id")); // Add student_id to the list
                    }
                }

                // Pass all the user details to the intent
                intent.putStringArrayListExtra("ADDED_USER_NAMES", userNames);
                intent.putStringArrayListExtra("ADDED_USER_PROGRAMS", userPrograms);
                intent.putStringArrayListExtra("ADDED_USER_CONTACTS", userContacts);
                intent.putStringArrayListExtra("ADDED_USER_STUDENT_IDS", userStudentIds); // Pass the student_id list
            }

            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "Please log in before proceeding.", Toast.LENGTH_SHORT).show();
        }
    }



    private void addSearchResultToLayout(String name, String studId, Object contactObj, LinearLayout searchResultsContainer, String program) {
        // Convert the contact field to String (check if it's a Long)
        String contact;
        if (contactObj instanceof Long) {
            contact = String.valueOf(contactObj); // Convert Long to String
        } else if (contactObj instanceof String) {
            contact = (String) contactObj; // If it's already a String
        } else {
            contact = "Unknown"; // Handle any unexpected cases
        }

        // Create a RelativeLayout for fixed button positioning
        RelativeLayout userLayout = new RelativeLayout(getActivity());
        userLayout.setPadding(10, 10, 10, 10);

        // Create the TextView for displaying student info
        TextView userInfo = new TextView(getActivity());
        userInfo.setText(studId + " | " + name + " | " + program);
        userInfo.setTextSize(name.length() > 18 ? 14 : 16); // Adjust text size if the name is long
        userInfo.setEllipsize(TextUtils.TruncateAt.END); // Truncate with "..." if text is too long
        userInfo.setSingleLine(true); // Keep text on a single line
        userInfo.setId(View.generateViewId());

        RelativeLayout.LayoutParams userInfoParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        userInfoParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        userInfoParams.addRule(RelativeLayout.CENTER_VERTICAL);
        userInfo.setLayoutParams(userInfoParams);

        // Create the Button for action
        Button actionButton = new Button(getActivity());
        actionButton.setText("+");
        actionButton.setBackgroundResource(R.drawable.round_button);
        actionButton.setTextColor(Color.WHITE);
        actionButton.setAllCaps(false);
        actionButton.setTextSize(24);
        actionButton.setPadding(20, 13, 20, 13);
        actionButton.setId(View.generateViewId());

        RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(
                140,
                140
        );
        buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        buttonLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        buttonLayoutParams.setMargins(0, 0, 35, 0); // Add right margin for spacing
        actionButton.setLayoutParams(buttonLayoutParams);

        actionButton.setOnClickListener(v -> handleAddButtonClick(studId, name, contact, program));

        // Add the TextView and Button to the RelativeLayout
        userLayout.addView(userInfo);
        userLayout.addView(actionButton);

        // Add the userLayout to the container
        searchResultsContainer.addView(userLayout);
    }




    private void handleAddButtonClick(String studId, String name, String contact, String program) {
        if (addedUserIds.contains(studId)) {
            Toast.makeText(getActivity(), "User already added", Toast.LENGTH_SHORT).show();
        } else {
            addedUserIds.add(studId);
            addedUserDetails.put(studId, findUserDocumentById(studId));

            // Corrected parameter order: name, program, studId, contact
            displayStudentDetails(name, program, studId, contact);
        }
    }


    private DocumentSnapshot findUserDocumentById(String userId) {
        for (DocumentSnapshot doc : allDocuments) {
            if (doc.getString("student_id").equals(userId)) {
                return doc;
            }
        }
        return null;
    }
    private void performSearch(View rootView) {
        String searchTerm = searchBar.getText().toString().trim().toLowerCase();

        if (searchTerm.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a name or student ID", Toast.LENGTH_SHORT).show();
            paginationControls.setVisibility(View.GONE);
            return;
        }

        LinearLayout searchResultsContainer = rootView.findViewById(R.id.search_results_container);
        searchResultsContainer.removeAllViews();

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
                            // Extract fields safely
                            String name = document.getString("name");
                            String studentId = document.getString("student_id");

                            // Ensure the name or studentId contains the search term (case insensitive)
                            if ((name != null && name.toLowerCase().contains(searchTerm)) ||
                                    (studentId != null && studentId.toLowerCase().contains(searchTerm))) {
                                allDocuments.add(document); // Add matching documents to the list
                            }
                        }

                        if (allDocuments.isEmpty()) {
                            Toast.makeText(getActivity(), "No matching results", Toast.LENGTH_SHORT).show();
                            paginationControls.setVisibility(View.GONE);
                        } else {
                            // Reset to first page and show results
                            currentPage.set(0);
                            showPage(rootView);
                            updatePaginationControls();
                            paginationControls.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(getActivity(), "No data available", Toast.LENGTH_SHORT).show();
                        paginationControls.setVisibility(View.GONE);
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

        int startIndex = currentPage.get() * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allDocuments.size());

        for (int i = startIndex; i < endIndex; i++) {
            DocumentSnapshot document = allDocuments.get(i);
            String name = document.getString("name");
            String program = document.getString("program");
            String studentId = document.getString("student_id");
            String contact = document.getString("contact");

            addSearchResultToLayout(name, studentId, contact, searchResultsContainer, program);
        }

        updatePaginationControls(); // Ensure pagination controls are updated after showing the page
    }

    private void showPreviousPage() {
        if (currentPage.get() > 0) {
            currentPage.decrementAndGet();
            showPage(getView());
        }
    }

    private void showNextPage() {
        if ((currentPage.get() + 1) * ITEMS_PER_PAGE < allDocuments.size()) {
            currentPage.incrementAndGet();
            showPage(getView());
        }
    }

    private void updatePaginationControls() {
        if (prevButton != null && nextButton != null && pageNumberContainer != null) {
            // Enable/disable buttons based on the current page
            prevButton.setEnabled(currentPage.get() > 0);
            nextButton.setEnabled((currentPage.get() + 1) * ITEMS_PER_PAGE < allDocuments.size());

            // Clear previous page number indicators
            pageNumberContainer.removeAllViews();
            int totalPages = (int) Math.ceil((double) allDocuments.size() / ITEMS_PER_PAGE);

            // Show page numbers
            for (int i = 0; i < totalPages; i++) {
                TextView pageNumberTextView = new TextView(getActivity());
                pageNumberTextView.setText(String.valueOf(i + 1));
                pageNumberTextView.setPadding(8, 8, 8, 8);
                // Highlight the current page
                pageNumberTextView.setTextColor(i == currentPage.get() ? Color.BLUE : Color.BLACK);
                pageNumberContainer.addView(pageNumberTextView);
            }

            // Disable buttons if no results are available
            if (allDocuments.isEmpty()) {
                prevButton.setEnabled(false);
                nextButton.setEnabled(false);
            }
        }
    }

    private void displayStudentDetails(String name, String program, String studId, String contact) {
        TableLayout detailsTable = getActivity().findViewById(R.id.details_table);

        // Create a new table row
        TableRow newRow = new TableRow(getActivity());
        newRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Create TextViews for student details
        addTextViewToRow(newRow, name);      // Name
        addTextViewToRow(newRow, program);   // Program
        addTextViewToRow(newRow, studId);    // Student ID
        addTextViewToRow(newRow, contact);    // Contact

        // Create delete button
        ImageButton deleteButton = new ImageButton(getActivity());
        deleteButton.setImageResource(R.drawable.baseline_delete_outline_24);
        deleteButton.setBackgroundResource(android.R.color.transparent);
        deleteButton.setOnClickListener(v -> {
            detailsTable.removeView(newRow);
            addedUserIds.remove(studId);
            addedUserDetails.remove(studId);
            Toast.makeText(getActivity(), name + " removed", Toast.LENGTH_SHORT).show();
        });

        newRow.addView(deleteButton);
        detailsTable.addView(newRow);
    }

    private void addTextViewToRow(TableRow row, String text) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setTextSize(16);
        row.addView(textView);
    }
}