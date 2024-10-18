package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
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
        currentUserId = getCurrentUserId();
        Log.d("CurrentUserID", currentUserId != null ? currentUserId : "No user logged in");
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

        // Fetch violations from all collections
        fetchViolationsFromCollection("student_refferal_history", violationsLayout);
        fetchViolationsFromCollection("personnel_refferal_history", violationsLayout);
        fetchViolationsFromCollection("prefect_refferal_history", violationsLayout);
    }

    private void fetchViolationsFromCollection(String collectionName, LinearLayout violationsLayout) {
        Log.d("FirestoreQuery", "Fetching violations from: " + collectionName + " for user: " + currentUserId);

        // Check if the user ID is null
        if (currentUserId == null) {
            Log.e("UserError", "Current user ID is null.");
            return; // Early exit if user is not logged in
        }

        db.collection(collectionName)
                .whereEqualTo("student_id", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            for (QueryDocumentSnapshot document : result) {
                                // Log document data for debugging
                                Log.d("FirestoreData", document.getId() + " => " + document.getData());

                                String violation = document.getString("violation");
                                String typeOfOffense = document.getString("type_of_offense");
                                String status = document.getString("status");
                                String dateOfIncident = document.getString("date"); // Ensure field exists

                                // Dynamically create views for each violation
                                addViolationToLayout(violationsLayout, violation, typeOfOffense, status, dateOfIncident);
                            }
                        } else {
                            Log.d("FirestoreData", "No matching documents found in " + collectionName);
                        }
                    } else {
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void addViolationToLayout(LinearLayout layout, String violation, String typeOfOffense, String status, String dateOfIncident) {
        // Inflate the violation_row.xml layout for each violation
        TableRow violationRow = (TableRow) LayoutInflater.from(getContext()).inflate(R.layout.violation_row, layout, false);

        // Use the correct method to find views within the violationRow
        TextView violationIndex = (TextView) violationRow.getChildAt(0);
        TextView violationTextView = (TextView) violationRow.getChildAt(1);
        TextView offenseTypeTextView = (TextView) violationRow.getChildAt(2);
        TextView statusTextView = (TextView) violationRow.getChildAt(3);
        TextView dateTextView = (TextView) violationRow.getChildAt(4);

        // Set the data
        violationIndex.setText(String.valueOf(layout.getChildCount() + 1)); // Set index starting from 1
        violationTextView.setText(violation != null ? violation : "N/A");
        offenseTypeTextView.setText(typeOfOffense != null ? typeOfOffense : "N/A");
        statusTextView.setText(status != null ? status : "N/A");
        dateTextView.setText(dateOfIncident != null ? dateOfIncident : "N/A");

        // Add the violation row to the layout
        layout.addView(violationRow);
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            // Handle the case when there is no logged-in user
            return null;
        }
    }
}
