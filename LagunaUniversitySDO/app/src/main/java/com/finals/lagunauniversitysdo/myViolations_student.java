package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.cardview.widget.CardView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class myViolations_student extends Fragment {

    private FirebaseFirestore db;
    private String currentUserId;

    public myViolations_student() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // Retrieve the user ID from the Intent
        if (getActivity() != null) {
            String userId = getActivity().getIntent().getStringExtra("USER_ID");
            if (userId != null) {
                currentUserId = userId;
                Log.d("CurrentUserID", currentUserId);
            } else {
                Log.e("UserError", "User ID is null.");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_violations_student, container, false);
        fetchViolations(view);
        return view;
    }

    private void fetchViolations(View view) {
        LinearLayout violationsLayout = view.findViewById(R.id.violations);

        // Clear all views except the header (if any)
        if (violationsLayout.getChildCount() > 0) {
            violationsLayout.removeAllViews();
        }

        // Fetch violations with status "accepted" from each specific user's subcollections
        fetchViolationsFromSubcollection("students", "student_refferal_history", violationsLayout);
        fetchViolationsFromSubcollection("personnel", "personnel_refferal_history", violationsLayout);
        fetchViolationsFromSubcollection("prefect", "prefect_refferal_history", violationsLayout);
    }

    private void fetchViolationsFromSubcollection(String collectionName, String subcollectionName, LinearLayout violationsLayout) {
        if (currentUserId == null) {
            Log.e("UserError", "Current user ID is null.");
            return;
        }

        // Reference the subcollection within the specific document (using the currentUserId)
        CollectionReference subcollectionRef = db.collection(collectionName).document(currentUserId).collection(subcollectionName);

        Log.d("FirestoreQuery", "Fetching accepted violations from: " + collectionName + "/" + currentUserId + "/" + subcollectionName);

        subcollectionRef.whereEqualTo("status", "accepted") // Filter by status "accepted"
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            for (QueryDocumentSnapshot document : result) {
                                Log.d("FirestoreData", document.getId() + " => " + document.getData());

                                String dateOfIncident = document.getString("date");
                                String violation = document.getString("remarks");
                                String typeOfOffense = document.getString("violation");
                                String status = document.getString("status");


                                addViolationToLayout(violationsLayout, violation, typeOfOffense, status, dateOfIncident);
                            }
                        } else {
                            Log.d("FirestoreData", "No matching documents found in " + collectionName + "/" + currentUserId + "/" + subcollectionName);
                        }
                    } else {
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void addViolationToLayout(LinearLayout layout, String violation, String typeOfOffense, String status, String dateOfIncident) {
        // Inflate the card view layout
        View violationCard = LayoutInflater.from(getContext()).inflate(R.layout.violation_card, layout, false);

        // Set the data for the violation card
        TextView violationTextView = violationCard.findViewById(R.id.violationTextView);
        TextView offenseTypeTextView = violationCard.findViewById(R.id.typeOfOffenseTextView);
        TextView statusTextView = violationCard.findViewById(R.id.statusTextView);
        TextView dateTextView = violationCard.findViewById(R.id.dateTextView);

        violationTextView.setText(violation != null ? "Violation: " +  violation : "N/A");
        offenseTypeTextView.setText(typeOfOffense != null ? "Offense: " + typeOfOffense : "N/A");
        statusTextView.setText(status != null ? "Status: " + status : "N/A");
        dateTextView.setText(dateOfIncident != null ? "Date: " +  dateOfIncident : "N/A");

        // Add the card view to the layout
        layout.addView(violationCard);
    }

}
