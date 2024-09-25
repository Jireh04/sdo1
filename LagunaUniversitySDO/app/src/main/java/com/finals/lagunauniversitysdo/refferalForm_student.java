package com.finals.lagunauniversitysdo;

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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;



public class refferalForm_student extends Fragment {
    private static final int REQUEST_CODE_QR_SCAN = 1;
    private EditText searchBar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Set<String> addedUserIds = new HashSet<>();
    private AtomicInteger currentPage = new AtomicInteger(0);
    private static final int ITEMS_PER_PAGE = 2;
    private List<DocumentSnapshot> allDocuments = new ArrayList<>();
    private Button prevButton, nextButton, proceedtoRefferal;
    private LinearLayout paginationControls, pageNumberContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_refferal_form_student, container, false);

        searchBar = rootView.findViewById(R.id.search_bar);
        Button searchButton = rootView.findViewById(R.id.search_button);
        prevButton = rootView.findViewById(R.id.prev_button);
        nextButton = rootView.findViewById(R.id.next_button);
        paginationControls = rootView.findViewById(R.id.pagination_controls);
        pageNumberContainer = rootView.findViewById(R.id.page_number_container);

        searchButton.setOnClickListener(v -> performSearch(rootView));
        prevButton.setOnClickListener(v -> showPreviousPage());
        nextButton.setOnClickListener(v -> showNextPage());

        ImageButton pickQRCodeButton = rootView.findViewById(R.id.pick_qr_code_button);
        pickQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), QRScannerActivity.class);
                startActivityForResult(intent, REQUEST_CODE_QR_SCAN);
            }
        });

        proceedtoRefferal = rootView.findViewById(R.id.proceedtoRefferal); // Assuming you've set the ID in your XML
        proceedtoRefferal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the new activity that contains form.xml
                Intent intent = new Intent(getActivity(), form.class);  // Replace FormActivity with your actual activity class
                startActivity(intent);
            }
        });

        return rootView;
    }


    private void addSearchResultToLayout(String name, String studId, String contact, LinearLayout searchResultsContainer, String program) {
        LinearLayout userLayout = new LinearLayout(getActivity());
        userLayout.setOrientation(LinearLayout.HORIZONTAL);
        userLayout.setPadding(10, 10, 10, 10);

        TextView userInfo = new TextView(getActivity());
        userInfo.setText(studId + ", " + name);
        userInfo.setTextSize(16);

        Button actionButton = new Button(getActivity());
        actionButton.setText("Add");

        actionButton.setOnClickListener(v -> {
            if (addedUserIds.contains(studId)) {
                Toast.makeText(getActivity(), "User already added", Toast.LENGTH_SHORT).show();
            } else {
                addedUserIds.add(studId);
                displayStudentDetails(name, program,  studId, contact); // Use 'program' here
            }
        });

        userLayout.addView(userInfo);
        userLayout.addView(actionButton);
        searchResultsContainer.addView(userLayout);
    }

    private void performSearch(View rootView) {
        String searchTerm = searchBar.getText().toString().trim().toLowerCase();

        if (!searchTerm.isEmpty()) {
            LinearLayout searchResultsContainer = rootView.findViewById(R.id.search_results_container);
            searchResultsContainer.removeAllViews();

            db.collection("students")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                    showPage(rootView);
                                    updatePaginationControls();
                                    paginationControls.setVisibility(View.VISIBLE); // Show pagination controls
                                } else {
                                    Toast.makeText(getActivity(), "No data available", Toast.LENGTH_SHORT).show();
                                    paginationControls.setVisibility(View.GONE); // Hide pagination controls if no results
                                }
                            } else {
                                Toast.makeText(getActivity(), "Error getting user data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "Please enter a name", Toast.LENGTH_SHORT).show();
            paginationControls.setVisibility(View.GONE); // Hide pagination controls if search term is empty
        }
    }

    private void showPage(View rootView) {
        // Get the LinearLayout from the layout
        LinearLayout searchResultsContainer = rootView.findViewById(R.id.search_results_container);
        searchResultsContainer.removeAllViews();

        // Calculate the start and end index for pagination
        int startIndex = currentPage.get() * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allDocuments.size());

        // Iterate over the documents to add search results
        for (int i = startIndex; i < endIndex; i++) {
            DocumentSnapshot document = allDocuments.get(i);

            // Retrieve values from the document
            String name = document.getString("name");
            String studId = document.getString("stud_id");
            String program = document.getString("program"); // Ensure "program" is a valid field in your document
            String contact = document.getString("contact");

            // Add the search result to the layout
            addSearchResultToLayout(name, studId, contact, searchResultsContainer, program);
        }

        // Update pagination controls
        updatePaginationControls();
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
            prevButton.setEnabled(currentPage.get() > 0);
            nextButton.setEnabled((currentPage.get() + 1) * ITEMS_PER_PAGE < allDocuments.size());

            pageNumberContainer.removeAllViews(); // Clear previous page numbers
            int totalPages = (int) Math.ceil((double) allDocuments.size() / ITEMS_PER_PAGE);

            for (int i = 0; i < totalPages; i++) {
                TextView pageNumberTextView = new TextView(getActivity());
                pageNumberTextView.setText(String.valueOf(i + 1));
                pageNumberTextView.setPadding(8, 8, 8, 8);
                pageNumberTextView.setTextSize(16);
                pageNumberTextView.setClickable(true);
                final int pageIndex = i; // Capture the page index in a final variable

                pageNumberTextView.setOnClickListener(v -> {
                    currentPage.set(pageIndex);
                    showPage(getView());
                });

                // Highlight the current page
                if (i == currentPage.get()) {
                    pageNumberTextView.setTextColor(Color.BLUE);
                } else {
                    pageNumberTextView.setTextColor(Color.BLACK);
                }

                pageNumberContainer.addView(pageNumberTextView);
            }
        }
    }

    private void displayStudentDetails(String name, String studId, String contact, String program) {
        TableLayout detailsTable = getActivity().findViewById(R.id.details_table);

        // Create a new table row
        TableRow newRow = new TableRow(getActivity());
        newRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Create TextView for name
        TextView nameView = new TextView(getActivity());
        nameView.setText(name);
        nameView.setPadding(8, 8, 8, 8);
        nameView.setTextSize(16);
        newRow.addView(nameView);

        // Create TextView for student ID
        TextView studIdView = new TextView(getActivity());
        studIdView.setText(studId);
        studIdView.setPadding(8, 8, 8, 8);
        studIdView.setTextSize(16);
        newRow.addView(studIdView);

        // Create TextView for contact
        TextView contactView = new TextView(getActivity());
        contactView.setText(contact);
        contactView.setPadding(8, 8, 8, 8);
        contactView.setTextSize(16);
        newRow.addView(contactView);

        // Create TextView for program (assuming you want to show it as well)
        TextView programView = new TextView(getActivity());
        programView.setText(program);
        programView.setPadding(8, 8, 8, 8);
        programView.setTextSize(16);
        newRow.addView(programView);

        // Create delete button
        ImageButton deleteButton = new ImageButton(getActivity());
        deleteButton.setImageResource(R.drawable.baseline_delete_outline_24);
        deleteButton.setBackgroundResource(android.R.color.transparent); // Transparent background for button
        deleteButton.setOnClickListener(v -> {
            // Remove the row from the table
            detailsTable.removeView(newRow);

            // Remove the student ID from the set of added user IDs
            addedUserIds.remove(studId);

            // Optionally, show a toast message confirming the removal
            Toast.makeText(getActivity(), name + " removed", Toast.LENGTH_SHORT).show();
        });

        // Add button to the row
        newRow.addView(deleteButton);

        // Add the new row to the table
        detailsTable.addView(newRow);
    }
}
