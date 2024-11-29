package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class MyRefferal_student extends Fragment {

    private FirebaseFirestore db; // Firestore instance
    private LinearLayout cardContainer; // Layout to hold dynamically added CardViews
    private String userId; // User ID of the logged-in student
    private String studentName; // Name of the logged-in student

    public MyRefferal_student() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        userId = UserSession.getStudentId(); // Get the logged-in user's ID
        studentName = UserSession.getStudentName(); // Get the logged-in student's name
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_refferal_student, container, false);

        // Initialize card container
        cardContainer = view.findViewById(R.id.cardContainer);

        // Fetch and display referral data for the logged-in user
        fetchReferralData();

        return view;
    }

    private void fetchReferralData() {
        // Clear previous views
        cardContainer.removeAllViews();

        // Fetch referral data from Firestore
        db.collection("students")
                .document(userId)
                .collection("student_refferal_history")
                .orderBy("date" , Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Extract data for each referral
                            String dateReported = document.getString("date");
                            String studentNo = document.getString("student_id");
                            String name = document.getString("student_name");
                            String status = document.getString("status");

                            // Add data as a new CardView
                            addCardView(dateReported, studentNo, name, status);
                        }
                    } else {
                        Toast.makeText(getContext(), "No referrals found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addCardView(String dateReported, String studentNo, String name, String status) {
        // Create a CardView
        CardView cardView = new CardView(getContext());
        cardView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardView.setRadius(16f);
        cardView.setCardElevation(8f);
        cardView.setUseCompatPadding(true);
        cardView.setPadding(16, 16, 16, 16);


        // Create a LinearLayout for the card content
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(8, 8, 8, 8);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Add TextViews for referral details
        linearLayout.addView(createTextView("Date Reported: " + dateReported));
        linearLayout.addView(createTextView("Student No.: " + studentNo));
        linearLayout.addView(createTextView("Name: " + name));

        // Add status with special handling for "rejected"
        TextView statusTextView = createTextView("Status: " + status);
        statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        if ("rejected".equalsIgnoreCase(status)) {
            statusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            statusTextView.setOnClickListener(v -> showRejectionReason(dateReported, studentNo));
        }
        linearLayout.addView(statusTextView);

        // Add the LinearLayout to the CardView
        cardView.addView(linearLayout);

        // Add the CardView to the container
        cardContainer.addView(cardView);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(16f);
        textView.setPadding(8, 8, 8, 8);
        return textView;
    }

    private void showRejectionReason(String dateReported, String studentNo) {
        db.collection("students")
                .document(userId)
                .collection("student_refferal_history")
                .whereEqualTo("student_id", studentNo)
                .whereEqualTo("date", dateReported)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String rejectionReason = document.getString("reason_rejecting");
                        if (rejectionReason != null) {
                            new AlertDialog.Builder(getContext())
                                    .setTitle("Rejection Reason")
                                    .setMessage(rejectionReason)
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else {
                            Toast.makeText(getContext(), "No rejection reason available.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Referral not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching rejection reason: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
