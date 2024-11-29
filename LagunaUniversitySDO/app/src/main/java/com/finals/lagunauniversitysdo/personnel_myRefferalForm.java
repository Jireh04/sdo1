package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class personnel_myRefferalForm extends Fragment {

    private FirebaseFirestore db; // Firestore instance
    private LinearLayout linearLayout; // LinearLayout to display CardViews
    private String personnelId; // ID of the logged-in personnel
    private String personnelName; // Full name of the logged-in personnel

    public personnel_myRefferalForm() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Get the currently logged-in personnel's ID and name from PersonnelSession
        personnelId = PersonnelSession.getPersonnelId();  // Assuming you have a PersonnelSession class
        personnelName = PersonnelSession.getPersonnelName();  // Fetch the personnel's full name from session
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_personnel_my_refferal_form, container, false);

        // Reference to LinearLayout in the XML layout
        linearLayout = view.findViewById(R.id.linearLayout);

        // Fetch and display referral data specific to the logged-in personnel
        fetchReferralData();

        return view;
    }

    private void fetchReferralData() {
        // Clear existing views
        linearLayout.removeAllViews();

        // Ensure personnelName is not null
        if (personnelName == null || personnelName.isEmpty()) {
            Toast.makeText(getContext(), "Unable to fetch personnel name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch data from Firestore 'personnel_refferal_history' collection for the logged-in personnel
        db.collection("personnel")
                .document(personnelId)
                .collection("personnel_refferal_history")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.firestore.QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots = task.getResult();
                            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                // Iterate through the Firestore documents
                                for (DocumentSnapshot document : queryDocumentSnapshots) {
                                    // Extract the data for each document
                                    String dateReported = document.getString("date");
                                    String personnelNo = document.getString("student_id"); // Ensure this key matches what's in Firestore
                                    String name = document.getString("student_name"); // Ensure this key matches what's in Firestore
                                    String status = document.getString("status");

                                    // Add data as a new CardView
                                    addCardView(dateReported, personnelNo, name, status);
                                }
                            } else {
                                Toast.makeText(getContext(), "No referral data found for " + personnelName, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle any errors that occur while fetching data
                            Toast.makeText(getContext(), "Error fetching data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Function to add a CardView dynamically
    private void addCardView(String dateReported, String personnelNo, String name, String status) {
        // Create a new CardView
        CardView cardView = new CardView(getContext());
        cardView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardView.setRadius(16f);
        cardView.setCardElevation(8f);
        cardView.setUseCompatPadding(true);
        cardView.setPadding(16, 16, 16, 16);


        // Create a LinearLayout inside the CardView to hold TextViews vertically
        LinearLayout cardContentLayout = new LinearLayout(getContext());
        cardContentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardContentLayout.setOrientation(LinearLayout.VERTICAL); // Set vertical orientation
        cardContentLayout.setPadding(16, 16, 16, 16); // Padding for the whole content layout

        // Create TextViews for each column
        TextView dateReportedTextView = new TextView(getContext());
        dateReportedTextView.setText("Date: " + dateReported);
        dateReportedTextView.setPadding(8, 8, 8, 8);
        dateReportedTextView.setTextSize(16); // Adjust text size if necessary

        TextView personnelNoTextView = new TextView(getContext());
        personnelNoTextView.setText("Student No.: " + personnelNo);
        personnelNoTextView.setPadding(8, 8, 8, 8);
        personnelNoTextView.setTextSize(16);

        TextView nameTextView = new TextView(getContext());
        nameTextView.setText("Name: " + name);
        nameTextView.setPadding(8, 8, 8, 8);
        nameTextView.setTextSize(16);

        TextView statusTextView = new TextView(getContext());
        statusTextView.setText("Status: " + status);
        statusTextView.setPadding(8, 8, 8, 8);
        statusTextView.setTextSize(16);

        // Add the TextViews to the LinearLayout (which is inside the CardView)
        cardContentLayout.addView(dateReportedTextView);
        cardContentLayout.addView(personnelNoTextView);
        cardContentLayout.addView(nameTextView);
        cardContentLayout.addView(statusTextView);

        // If the status is "rejected", make it clickable and show the rejection reason
        if ("rejected".equals(status)) {
            statusTextView.setClickable(true);
            statusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));  // Set text color to red for "rejected"

            // Set click listener to show rejection reason in an AlertDialog
            statusTextView.setOnClickListener(v -> showRejectionReason(dateReported, personnelNo));
        }


        // Add the LinearLayout with the TextViews to the CardView
        cardView.addView(cardContentLayout);

        // Add the CardView to the main LinearLayout (for displaying the cards)
        linearLayout.addView(cardView);

    }

    private void showRejectionReason(String dateReported, String personnelNo) {
        // Fetch the rejection reason from Firestore using the personnelNo and dateReported (or any other identifier you have)
        db.collection("personnel")
                .document(personnelId)
                .collection("personnel_refferal_history")
                .whereEqualTo("student_id", personnelNo)
                .whereEqualTo("date", dateReported) // or any unique identifier for the referral
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            // Get the rejection reason from the document
                            String rejectionReason = document.getString("reason_rejecting");

                            if (rejectionReason != null) {
                                // Show the rejection reason in an AlertDialog
                                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                        .setTitle("Rejection Reason")
                                        .setMessage(rejectionReason)
                                        .setPositiveButton("OK", null)
                                        .show();
                            } else {
                                // Handle the case where there's no rejection reason
                                Toast.makeText(getContext(), "No rejection reason available.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle the case where the referral document is not found
                            Toast.makeText(getContext(), "Referral not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle errors fetching the data
                        Toast.makeText(getContext(), "Error fetching rejection reason: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
