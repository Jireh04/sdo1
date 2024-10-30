package com.finals.lagunauniversitysdo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class personnel_refferal_form extends Fragment {
    private static final int REQUEST_CODE_QR_SCAN = 1;
    private static final int ITEMS_PER_PAGE = 5;

    private EditText searchBar;
    private Button searchButton, prevButton, nextButton, proceedToReferral;
    private FirebaseFirestore db;
    private Set<String> addedUserIds;
    private AtomicInteger currentPage;
    private List<DocumentSnapshot> allDocuments;
    private Map<String, DocumentSnapshot> addedUserDetails; // Store added users

    private LinearLayout paginationControls, pageNumberContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_personnel_refferal_form, container, false);

        // Initialize UI components
        initUI(view);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        addedUserIds = new HashSet<>();
        addedUserDetails = new HashMap<>();
        currentPage = new AtomicInteger(0);
        allDocuments = new ArrayList<>();

        // Set button click listeners
        setButtonListeners(view);

        return view; // Return the inflated view
    }

    private void initUI(View view) {
        searchBar = view.findViewById(R.id.search_bar);
        searchButton = view.findViewById(R.id.search_button);
        prevButton = view.findViewById(R.id.prev_button);
        nextButton = view.findViewById(R.id.next_button);
        paginationControls = view.findViewById(R.id.pagination_controls);
        pageNumberContainer = view.findViewById(R.id.page_number_container);
        proceedToReferral = view.findViewById(R.id.proceedtoRefferal);
    }
    private void setButtonListeners(View rootView) {
        searchButton.setOnClickListener(v -> performSearch());
        prevButton.setOnClickListener(v -> showPreviousPage());
        nextButton.setOnClickListener(v -> showNextPage());

        ImageButton pickQRCodeButton = rootView.findViewById(R.id.pick_qr_code_button);
        pickQRCodeButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), Personnel_QRScannerActivity.class);

            // Pass personnel details
            intent.putExtra("PERSONNEL_ID", PersonnelSession.getPersonnelId());
            intent.putExtra("PERSONNEL_UNIQUE_ID", PersonnelSession.getPersonnelUniqueId());
            intent.putExtra("PERSONNEL_NAME", PersonnelSession.getPersonnelName());
            intent.putExtra("PERSONNEL_EMAIL", PersonnelSession.getEmail());
            intent.putExtra("PERSONNEL_CONTACT", PersonnelSession.getContactNum());
            intent.putExtra("PERSONNEL_PROGRAM", PersonnelSession.getDepartment());

            startActivityForResult(intent, REQUEST_CODE_QR_SCAN);
        });

        proceedToReferral.setOnClickListener(v -> proceedToReferral());
    }



    private void proceedToReferral() {
        // Retrieve personnel ID and check login status
        String personnelId = PersonnelSession.getPersonnelId();
        if (personnelId == null || personnelId.isEmpty()) {
            Toast.makeText(getActivity(), "Please log in as personnel before proceeding.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve personnel details
        String uniqueId = PersonnelSession.getPersonnelUniqueId();
        String personnelName = PersonnelSession.getPersonnelName();
        String personnelEmail = PersonnelSession.getEmail();
        Long personnelContactNum = PersonnelSession.getContactNum();
        String personnelProgram = PersonnelSession.getDepartment();

        Log.d("ContactNum", "Personnel contact number: " + personnelContactNum);


        // Prepare the intent to pass personnel data
        Intent intent = new Intent(getActivity(), PersonnelForm.class);
        intent.putExtra("PERSONNEL_ID", personnelId);
        intent.putExtra("PERSONNEL_UNIQUE_ID", uniqueId);
        intent.putExtra("PERSONNEL_NAME_KEY", personnelName);
        intent.putExtra("PERSONNEL_EMAIL_KEY", personnelEmail);

        // Pass personnel contact number directly as a Long
        if (personnelContactNum != null) {
            intent.putExtra("PERSONNEL_CONTACT_NUM_KEY", personnelContactNum);
        } else {
            intent.putExtra("PERSONNEL_CONTACT_NUM_KEY", 0L); // Default value if contact number is null
            Log.d("PersonnelForm", "Retrieved contact: " + personnelContactNum);
        }

        intent.putExtra("PERSONNEL_DEPARTMENT_KEY", personnelProgram);

        // Check if at least one user is added
        if (addedUserIds.isEmpty()) {
            Toast.makeText(getActivity(), "No users added. Please add at least one user.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the details of all added users
        ArrayList<String> userNames = new ArrayList<>();
        ArrayList<String> userDepartments = new ArrayList<>();
        ArrayList<String> userEmails = new ArrayList<>();
        ArrayList<Long> userContacts = new ArrayList<>();
        ArrayList<String> userIds = new ArrayList<>();

        for (String userId : addedUserIds) {
            DocumentSnapshot userDoc = addedUserDetails.get(userId);
            if (userDoc != null) {
                userNames.add(userDoc.getString("name"));
                userDepartments.add(userDoc.getString("program"));
                userEmails.add(userDoc.getString("email"));
                userContacts.add(userDoc.getLong("contacts"));
                userIds.add(userDoc.getString("student_id"));
            }
        }

        // Retrieve student session details if they exist
        String studentId = PersonnelSession.getStudentId(); // Assuming a similar session class exists for students
        if (studentId != null && !studentId.isEmpty()) {
            String studentName = PersonnelSession.getStudentName();
            String studentEmail = PersonnelSession.getEmail();
            Long studentContact = PersonnelSession.getContactNum();
            String studentDepartment = PersonnelSession.getDepartment();

            // Pass student details to the intent
            intent.putExtra("STUDENT_ID", studentId);
            intent.putExtra("STUDENT_NAME", studentName);
            intent.putExtra("STUDENT_EMAIL", studentEmail);

            // Prepare student contact information as an ArrayList
            ArrayList<Long> studentContactList = new ArrayList<>();
            studentContactList.add(studentContact != null ? studentContact : 0L);
            intent.putExtra("STUDENT_CONTACT_NUM", studentContactList);
            intent.putExtra("STUDENT_DEPARTMENT", studentDepartment);
        }

        // Pass added user details to the intent
        intent.putExtra("ADDED_STUDENT_NAMES", userNames);
        intent.putExtra("ADDED_STUDENT_DEPARTMENTS", userDepartments);
        intent.putExtra("ADDED_STUDENT_EMAILS", userEmails);
        intent.putExtra("ADDED_STUDENT_CONTACTS", userContacts);
        intent.putExtra("ADDED_STUDENT_IDS", userIds);

        // Start the PersonnelForm activity with all the data
        startActivity(intent);
    }




    private void performSearch() {
        String searchTerm = searchBar.getText().toString().trim().toLowerCase();

        // Check if the search term is empty
        if (searchTerm.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a name or student ID", Toast.LENGTH_SHORT).show();
            paginationControls.setVisibility(View.GONE);
            return;
        }

        // Get the root view
        LinearLayout searchResultsContainer = getView().findViewById(R.id.search_results_container);
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

                            // Check if the search term matches either the name or student_id (case insensitive)
                            if ((name != null && name.toLowerCase().contains(searchTerm)) ||
                                    (studentId != null && studentId.toLowerCase().contains(searchTerm))) {
                                allDocuments.add(document); // Add matching documents to the list
                                Log.d("SearchResults", "Found Student: " + name + " with ID: " + studentId);
                            }
                        }

                        if (allDocuments.isEmpty()) {
                            Toast.makeText(getActivity(), "No matching results", Toast.LENGTH_SHORT).show();
                            paginationControls.setVisibility(View.GONE);
                        } else {
                            // Reset to first page and show results
                            currentPage.set(0);
                            showPage(); // No need for rootView, as it's using getView()
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


    private void showPage() {
        LinearLayout searchResultsContainer = getView().findViewById(R.id.search_results_container);
        searchResultsContainer.removeAllViews();

        int startIndex = currentPage.get() * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allDocuments.size());

        for (int i = startIndex; i < endIndex; i++) {
            DocumentSnapshot document = allDocuments.get(i);
            String name = document.getString("name");
            String program = document.getString("program");
            String studId = document.getString("student_id");
            String contact = document.getString("contact");

            addSearchResultToLayout(name, studId, contact, searchResultsContainer, program);
        }

        updatePaginationControls();
    }

    private void addSearchResultToLayout(String name, String studId, String contact, LinearLayout searchResultsContainer, String program) {
        LinearLayout userLayout = new LinearLayout(getActivity());
        userLayout.setOrientation(LinearLayout.HORIZONTAL);
        userLayout.setPadding(10, 10, 10, 10);

        TextView userInfo = new TextView(getActivity());
        userInfo.setText(studId + ", " + name);
        userInfo.setTextSize(18);

        Button actionButton = new Button(getActivity());
        actionButton.setText("+");
        actionButton.setBackgroundResource(R.drawable.round_button);
        actionButton.setTextColor(Color.WHITE);
        actionButton.setAllCaps(false);
        actionButton.setTextSize(24);
        actionButton.setPadding(24, 16, 24, 16);

        actionButton.setOnClickListener(v -> handleAddButtonClick(studId, name, contact, program));

        userLayout.addView(userInfo);
        userLayout.addView(actionButton);
        searchResultsContainer.addView(userLayout);
    }

    private void handleAddButtonClick(String studId, String name, String contact, String program) {
        if (addedUserIds.contains(studId)) {
            Toast.makeText(getActivity(), "User already added", Toast.LENGTH_SHORT).show();
        } else {
            addedUserIds.add(studId);
            DocumentSnapshot userDoc = findUserDocumentById(studId);
            if (userDoc != null) {
                addedUserDetails.put(studId, userDoc);
                Log.d("AddedUser", "User added: " + name + " with ID: " + studId); // Add logging here
                displayStudentDetails(name, program, studId, contact);
            } else {
                Log.e("UserAddition", "Document not found for ID: " + studId); // Log document not found
            }
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

    private void showPreviousPage() {
        if (currentPage.get() > 0) {
            currentPage.decrementAndGet();
            showPage();
        }
    }

    private void showNextPage() {
        if ((currentPage.get() + 1) * ITEMS_PER_PAGE < allDocuments.size()) {
            currentPage.incrementAndGet();
            showPage();
        }
    }

    private void updatePaginationControls() {
        if (prevButton != null && nextButton != null && pageNumberContainer != null) {
            prevButton.setEnabled(currentPage.get() > 0);
            nextButton.setEnabled((currentPage.get() + 1) * ITEMS_PER_PAGE < allDocuments.size());

            pageNumberContainer.removeAllViews();
            int totalPages = (int) Math.ceil((double) allDocuments.size() / ITEMS_PER_PAGE);

            for (int i = 0; i < totalPages; i++) {
                TextView pageNumberTextView = new TextView(getActivity());
                pageNumberTextView.setText(String.valueOf(i + 1));
                pageNumberTextView.setPadding(8, 8, 8, 8);
                pageNumberTextView.setTextColor(i == currentPage.get() ? Color.BLUE : Color.BLACK);
                pageNumberContainer.addView(pageNumberTextView);
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

        // Add TextViews for student details
        addTextViewToRow(newRow, name);      // Name
        addTextViewToRow(newRow, program);   // Program
        addTextViewToRow(newRow, studId);    // Student ID
        addTextViewToRow(newRow, contact);   // Contact

        // Create delete button
        ImageButton deleteButton = new ImageButton(getActivity());
        deleteButton.setImageResource(R.drawable.baseline_delete_outline_24);
        deleteButton.setBackgroundResource(android.R.color.transparent); // Make button background transparent
        deleteButton.setOnClickListener(v -> {
            detailsTable.removeView(newRow); // Remove the row when button is clicked
            addedUserIds.remove(studId);
            addedUserDetails.remove(studId);
            Toast.makeText(getActivity(), name + " removed", Toast.LENGTH_SHORT).show(); // Show confirmation
        });

        // Add the delete button to the row
        newRow.addView(deleteButton);
        // Add the row to the table
        detailsTable.addView(newRow);
    }

    // Helper method to add TextViews to the row
    private void addTextViewToRow(TableRow row, String text) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setTextSize(16);
        row.addView(textView);
    }



}
