package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
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


public class PrefectReferralDashboardActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_QR_SCAN = 1;
    private static final int ITEMS_PER_PAGE = 2;

    private EditText searchBar;
    private Button searchButton;
    private FirebaseFirestore db;
    private Set<String> addedUserIds;
    private AtomicInteger currentPage;
    private List<DocumentSnapshot> allDocuments;
    private Map<String, DocumentSnapshot> addedUserDetails; // Store added users

    private Button prevButton, nextButton, proceedToReferral;
    private LinearLayout paginationControls, pageNumberContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefect_referral_dashboard);

        // Initialize UI components
        initUI();

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        addedUserIds = new HashSet<>();
        addedUserDetails = new HashMap<>();
        currentPage = new AtomicInteger(0);
        allDocuments = new ArrayList<>();

        // Set button click listeners
        setButtonListeners();
    }

    private void initUI() {
        searchBar = findViewById(R.id.search_bar);
        searchButton = findViewById(R.id.search_button);
        prevButton = findViewById(R.id.prev_button);
        nextButton = findViewById(R.id.next_button);
        paginationControls = findViewById(R.id.pagination_controls);
        pageNumberContainer = findViewById(R.id.page_number_container);
        proceedToReferral = findViewById(R.id.proceed_to_referral);
    }

    private void setButtonListeners() {
        searchButton.setOnClickListener(v -> performSearch());
        prevButton.setOnClickListener(v -> showPreviousPage());
        nextButton.setOnClickListener(v -> showNextPage());

        ImageButton pickQRCodeButton = findViewById(R.id.pick_qr_code_button);
        pickQRCodeButton.setOnClickListener(v -> {
            Intent intent = new Intent(PrefectReferralDashboardActivity.this, QRScannerActivity.class);
            startActivityForResult(intent, REQUEST_CODE_QR_SCAN);
        });

        proceedToReferral.setOnClickListener(v -> proceedToReferral());
    }

    private void proceedToReferral() {
        // Check if any users have been added
        if (addedUserIds.isEmpty()) {
            Toast.makeText(this, "No users added. Please add at least one user before proceeding.", Toast.LENGTH_SHORT).show();
            return; // Stop execution if no users are added
        }

        String studId = UserSession.getStudId();
        String studentName = UserSession.getStudentName();
        String email = UserSession.getEmail();
        Long contactNum = UserSession.getContactNum();
        String program = UserSession.getProgram();

        if (studId != null) {
            Intent intent = new Intent(PrefectReferralDashboardActivity.this, PrefectForm.class);
            intent.putExtra("STUDENT_NAME", studentName);
            intent.putExtra("EMAIL", email);

            // Handle contact number
            if (contactNum != null) {
                intent.putExtra("CONTACT_NUM", contactNum);
            } else {
                intent.putExtra("CONTACT_NUM", 0L); // Default value
            }

            // Add program and stud_id before contact number
            intent.putExtra("PROGRAM", program);
            intent.putExtra("STUD_ID", studId); // Place stud_id here
            intent.putExtra("CONTACT_NUM", contactNum); // Then contact number

            // Pass the added user details as separate ArrayLists
            if (!addedUserIds.isEmpty()) {
                ArrayList<String> userNames = new ArrayList<>();
                ArrayList<String> userPrograms = new ArrayList<>();
                ArrayList<String> userContacts = new ArrayList<>();
                ArrayList<String> userStudIds = new ArrayList<>(); // New ArrayList for stud_id

                for (String userId : addedUserIds) {
                    DocumentSnapshot userDoc = addedUserDetails.get(userId);
                    if (userDoc != null) {
                        userNames.add(userDoc.getString("name"));
                        userPrograms.add(userDoc.getString("program"));
                        userContacts.add(userDoc.getString("contact"));
                        userStudIds.add(userDoc.getString("stud_id")); // Add stud_id to the list
                    }
                }

                // Pass all the user details to the intent
                intent.putStringArrayListExtra("ADDED_USER_NAMES", userNames);
                intent.putStringArrayListExtra("ADDED_USER_PROGRAMS", userPrograms);
                intent.putStringArrayListExtra("ADDED_USER_CONTACTS", userContacts);
                intent.putStringArrayListExtra("ADDED_USER_STUD_IDS", userStudIds); // Pass the stud_id list
            }

            startActivity(intent);
        } else {
            Toast.makeText(this, "Please log in before proceeding.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addSearchResultToLayout(String name, String studId, String contact, LinearLayout searchResultsContainer, String program) {
        LinearLayout userLayout = new LinearLayout(this);
        userLayout.setOrientation(LinearLayout.HORIZONTAL);
        userLayout.setPadding(10, 10, 10, 10);

        TextView userInfo = new TextView(this);
        userInfo.setText(studId + ", " + name);
        userInfo.setTextSize(18);

        Button actionButton = new Button(this);
        actionButton.setText("+");
        actionButton.setBackgroundResource(R.drawable.round_button);
        actionButton.setTextColor(Color.WHITE);
        actionButton.setAllCaps(false);

        // Set layout parameters for the button with margin and size
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                150,
                150
        );
        buttonLayoutParams.setMargins(16, 0, 0, 0);
        actionButton.setLayoutParams(buttonLayoutParams);

        // Set larger text size and more padding
        actionButton.setTextSize(24); // Larger text size
        actionButton.setPadding(24, 16, 24, 16); // Increased padding

        actionButton.setOnClickListener(v -> handleAddButtonClick(studId, name, contact, program));

        userLayout.addView(userInfo);
        userLayout.addView(actionButton);
        searchResultsContainer.addView(userLayout);
    }

    private void handleAddButtonClick(String studId, String name, String contact, String program) {
        if (addedUserIds.contains(studId)) {
            Toast.makeText(this, "User already added", Toast.LENGTH_SHORT).show();
        } else {
            addedUserIds.add(studId);
            addedUserDetails.put(studId, findUserDocumentById(studId));

            // Corrected parameter order: name, program, studId, contact
            displayStudentDetails(name, program, studId, contact);
        }
    }

    private DocumentSnapshot findUserDocumentById(String userId) {
        for (DocumentSnapshot doc : allDocuments) {
            if (doc.getString("stud_id").equals(userId)) {
                return doc;
            }
        }
        return null;
    }

    private void performSearch() {
        String searchTerm = searchBar.getText().toString().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            paginationControls.setVisibility(View.GONE);
            return;
        }

        LinearLayout searchResultsContainer = findViewById(R.id.search_results_container);
        searchResultsContainer.removeAllViews();

        db.collection("students").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        allDocuments.clear();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String name = document.getString("name");
                            String studId = document.getString("stud_id");
                            String contact = document.getString("contact");

                            if (name != null && name.toLowerCase().contains(searchTerm)) {
                                allDocuments.add(document);
                            }
                        }

                        currentPage.set(0); // Reset currentPage to 0
                        showPage();
                        updatePaginationControls();
                        paginationControls.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(PrefectReferralDashboardActivity.this, "No data available", Toast.LENGTH_SHORT).show();
                        paginationControls.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(PrefectReferralDashboardActivity.this, "Error getting user data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showPage() {
        LinearLayout searchResultsContainer = findViewById(R.id.search_results_container);
        searchResultsContainer.removeAllViews();

        int startIndex = currentPage.get() * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allDocuments.size());

        for (int i = startIndex; i < endIndex; i++) {
            DocumentSnapshot document = allDocuments.get(i);
            String name = document.getString("name");
            String program = document.getString("program");
            String studId = document.getString("stud_id");
            String contact = document.getString("contact");

            addSearchResultToLayout(name, studId, contact, searchResultsContainer, program);
        }

        updatePaginationControls();
    }

    private void updatePaginationControls() {
        pageNumberContainer.removeAllViews();
        int totalPages = (int) Math.ceil(allDocuments.size() / (double) ITEMS_PER_PAGE);

        for (int i = 0; i < totalPages; i++) {
            final int pageIndex = i; // Make 'i' final or effectively final
            TextView pageNumber = new TextView(this);
            pageNumber.setText(String.valueOf(i + 1));
            pageNumber.setPadding(8, 8, 8, 8);
            pageNumber.setTextSize(18);
            pageNumber.setTextColor(Color.BLACK);

            // Use the final 'pageIndex' inside the lambda
            pageNumber.setOnClickListener(v -> {
                currentPage.set(pageIndex);
                showPage();
            });

            pageNumberContainer.addView(pageNumber);
        }
    }


    private void showPreviousPage() {
        int previousPage = currentPage.get() - 1;
        if (previousPage >= 0) {
            currentPage.set(previousPage);
            showPage();
        }
    }

    private void showNextPage() {
        int nextPage = currentPage.get() + 1;
        if (nextPage * ITEMS_PER_PAGE < allDocuments.size()) {
            currentPage.set(nextPage);
            showPage();
        }
    }
    private void displayStudentDetails(final String name, final String program, final String studId, final String contact) {
        // Get the TableLayout using 'findViewById'
        TableLayout detailsTable = findViewById(R.id.details_table);

        // Create a new TableRow
        TableRow newRow = new TableRow(this); // Use 'this' to refer to the current activity context
        newRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Create TextViews for student details and add them to the row
        addTextViewToRow(newRow, name);      // Name
        addTextViewToRow(newRow, program);   // Program
        addTextViewToRow(newRow, studId);    // Student ID
        addTextViewToRow(newRow, contact);   // Contact

        // Create delete button
        ImageButton deleteButton = new ImageButton(this);  // Use 'this' to get the context
        deleteButton.setImageResource(R.drawable.baseline_delete_outline_24);
        deleteButton.setBackgroundResource(android.R.color.transparent);

        // Lambda for delete button
        deleteButton.setOnClickListener(v -> {
            detailsTable.removeView(newRow);  // Remove the row from the TableLayout
            addedUserIds.remove(studId);      // Remove the user ID
            addedUserDetails.remove(studId);  // Remove user details
            Toast.makeText(this, name + " removed", Toast.LENGTH_SHORT).show();  // Show toast
        });

        // Add the delete button to the row and then add the row to the table
        newRow.addView(deleteButton);
        detailsTable.addView(newRow);
    }
    private void addTextViewToRow(TableRow row, String text) {
        TextView textView = new TextView(this);  // Use 'this' to refer to the current Activity context
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setTextSize(16);
        row.addView(textView);
    }

}
