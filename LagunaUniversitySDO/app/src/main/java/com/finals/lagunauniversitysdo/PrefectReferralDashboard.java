package com.finals.lagunauniversitysdo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import androidx.fragment.app.Fragment;

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


public class PrefectReferralDashboard extends Fragment {
    private static final int REQUEST_CODE_QR_SCAN = 1;
    private static final int ITEMS_PER_PAGE = 5;

    private EditText searchBar;
    private Button searchButton;
    private FirebaseFirestore db;
    private Set<String> addedUserIds;
    private AtomicInteger currentPage;
    private List<DocumentSnapshot> allDocuments;
    private Map<String, DocumentSnapshot> addedUserDetails;

    private Button prevButton, nextButton, proceedToReferral;
    private LinearLayout paginationControls, pageNumberContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.activity_prefect_referral_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
    }

    private void initUI(View view) {
        searchBar = view.findViewById(R.id.search_bar);
        searchButton = view.findViewById(R.id.search_button);
        prevButton = view.findViewById(R.id.prev_button);
        nextButton = view.findViewById(R.id.next_button);
        paginationControls = view.findViewById(R.id.pagination_controls);
        pageNumberContainer = view.findViewById(R.id.page_number_container);
        proceedToReferral = view.findViewById(R.id.proceed_to_referral);
    }

    private void setButtonListeners(View view) {
        searchButton.setOnClickListener(v -> performSearch());
        prevButton.setOnClickListener(v -> showPreviousPage());
        nextButton.setOnClickListener(v -> showNextPage());

        ImageButton pickQRCodeButton = view.findViewById(R.id.pick_qr_code_button);
        pickQRCodeButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Prefect_QRScannerActivity.class);
            startActivityForResult(intent, REQUEST_CODE_QR_SCAN);
        });

        proceedToReferral.setOnClickListener(v -> proceedToReferral());
    }

    private void proceedToReferral() {
        // Retrieve prefect ID and check login status
        String prefectId = PrefectSession.getPrefectId();
        if (prefectId == null || prefectId.isEmpty()) {
            Toast.makeText(getActivity(), "Please log in as prefect before proceeding.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve prefect details
        String prefectName = PrefectSession.getPrefectName();
        String prefectEmail = PrefectSession.getPrefectEmail();
        Long prefectContactNum = PrefectSession.getPrefectContactNum();
        String prefectDepartment = PrefectSession.getPrefectDepartment();

        // Prepare the intent to pass prefect data
        Intent intent = new Intent(getActivity(), PrefectForm.class);
        intent.putExtra("PREFECT_ID", prefectId);
        intent.putExtra("PREFECT_NAME_KEY", prefectName);
        intent.putExtra("PREFECT_EMAIL_KEY", prefectEmail);

        // Pass prefect contact number directly as a Long
        if (prefectContactNum != null) {
            intent.putExtra("PREFECT_CONTACT_NUM_KEY", prefectContactNum);
        } else {
            intent.putExtra("PREFECT_CONTACT_NUM_KEY", 0L); // Default value if contact number is null
            Log.d("PrefectForm", "Retrieved contact: " + prefectContactNum);
        }

        intent.putExtra("PREFECT_DEPARTMENT_KEY", prefectDepartment);

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

        // Start the PrefectForm activity with all the data
        startActivity(intent);
    }

    private void addSearchResultToLayout(String name, String studId, String contact, LinearLayout searchResultsContainer, String program) {
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

        userLayout.addView(userInfo);
        userLayout.addView(actionButton);
        searchResultsContainer.addView(userLayout);
    }

    private void handleAddButtonClick(String studId, String name, String contact, String program) {
        if (addedUserIds.contains(studId)) {
            Toast.makeText(getActivity(), "User already added", Toast.LENGTH_SHORT).show();
        } else {
            addedUserIds.add(studId);
            addedUserDetails.put(studId, findUserDocumentById(studId));

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


    private void performSearch() {
        String searchTerm = searchBar.getText().toString().trim().toLowerCase();

        // Check if the search term is empty
        if (searchTerm.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a name or student ID", Toast.LENGTH_SHORT).show();
            paginationControls.setVisibility(View.GONE);
            return;
        }

        // Get the root view's search results container and clear it
        LinearLayout searchResultsContainer = getView().findViewById(R.id.search_results_container);
        searchResultsContainer.removeAllViews();

        // Query Firestore for the students collection
        db.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    allDocuments.clear(); // Clear previous results

                    // Loop through each document in the result set
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String name = document.getString("name");
                        String studId = document.getString("student_id");
                        String contact = document.getString("contact");

                        // Check if the search term is in either the name or student ID (case insensitive)
                        if ((name != null && name.toLowerCase().contains(searchTerm)) ||
                                (studId != null && studId.toLowerCase().contains(searchTerm))) {
                            allDocuments.add(document); // Add matching documents to the list
                            Log.d("SearchResults", "Found Student: " + name + " with ID: " + studId + " and Contact: " + contact);
                        }
                    }

                    // Handle case where no matching documents were found
                    if (allDocuments.isEmpty()) {
                        Toast.makeText(getActivity(), "No matching results", Toast.LENGTH_SHORT).show();
                        paginationControls.setVisibility(View.GONE);
                    } else {
                        // Reset to the first page and show results
                        currentPage.set(0);
                        showPage(); // No need for rootView, using getView()
                        updatePaginationControls();
                        paginationControls.setVisibility(View.VISIBLE);
                    }
                } else {
                    // No data available case
                    Toast.makeText(getActivity(), "No data available", Toast.LENGTH_SHORT).show();
                    paginationControls.setVisibility(View.GONE);
                }
            } else {
                // Error while fetching data
                Toast.makeText(getActivity(), "Error fetching data", Toast.LENGTH_SHORT).show();
                Log.e("FirestoreError", "Error getting documents: ", task.getException());
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
            String studId = document.getString("student_id");
            String contact = document.getString("contact");
            String program = document.getString("program");

            addSearchResultToLayout(name, studId, contact, searchResultsContainer, program);
        }

        updatePaginationControls();
    }

    private void updatePaginationControls() {
        int totalPages = (int) Math.ceil(allDocuments.size() / (double) ITEMS_PER_PAGE);
        prevButton.setEnabled(currentPage.get() > 0);
        nextButton.setEnabled(currentPage.get() < totalPages - 1);
    }

    private void showPreviousPage() {
        if (currentPage.get() > 0) {
            currentPage.decrementAndGet();
            showPage();
        }
    }

    private void showNextPage() {
        int totalPages = (int) Math.ceil(allDocuments.size() / (double) ITEMS_PER_PAGE);
        if (currentPage.get() < totalPages - 1) {
            currentPage.incrementAndGet();
            showPage();
        }
    }

    private void displayStudentDetails(String name, String program, String studId, String contact) {
        TableLayout tableLayout = getView().findViewById(R.id.details_table);
        TableRow row = new TableRow(getActivity());
        row.setPadding(16, 16, 16, 16);

        addTextViewToRow(row, name);
        addTextViewToRow(row, program);
        addTextViewToRow(row, studId);
        addTextViewToRow(row, contact);

        Button deleteButton = new Button(getActivity());
        deleteButton.setText("Delete");
        deleteButton.setOnClickListener(v -> tableLayout.removeView(row));

        row.addView(deleteButton);
        tableLayout.addView(row);
    }

    private void addTextViewToRow(TableRow row, String text) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setTextSize(16);
        textView.setPadding(8, 8, 8, 8);
        row.addView(textView);
    }
}
