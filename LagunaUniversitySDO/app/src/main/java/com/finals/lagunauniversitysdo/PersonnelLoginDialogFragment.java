package com.finals.lagunauniversitysdo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;
import android.text.TextUtils;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PersonnelLoginDialogFragment extends DialogFragment {

    EditText loginUsername, loginPassword;
    Button loginButton;
    ImageButton closeButton;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personnel_login_dialog, container, false);

        loginUsername = view.findViewById(R.id.login_username);
        loginPassword = view.findViewById(R.id.login_password);
        loginButton = view.findViewById(R.id.login_button);
        closeButton = view.findViewById(R.id.close_button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = loginUsername.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();
                UserSession.init(getActivity());

                // Check if the input fields are empty
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter both username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Perform login action for personnel
                authenticate(username, password);
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }
    private void authenticate(String username, String password) {
        // Clear previous session data
        PersonnelSession.clearSession();

        Log.d("Auth", "Attempting to log in personnel with username: " + username);

        // Validate inputs
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Username and password must not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use Firebase Firestore to authenticate the personnel user
        db.collection("personnel")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password) // Note: Use a secure way to handle passwords!
                .get()
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.firestore.QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("Auth", "Query successful");

                            com.google.firebase.firestore.QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                String personnelId = document.getId();  // Get the document ID (personnel ID)
                                String personnelName = document.getString("name");
                                String email = document.getString("email");
                                Long contactNum = document.getLong("contactNum");
                                String department = document.getString("department");
                                String personnelUniqueId = document.getString("personnelUniqueId");

                                // Store personnel ID and details in PersonnelSession
                                PersonnelSession.setPersonnelId(personnelId);
                                PersonnelSession.setPersonnelUniqueId(personnelUniqueId); // Set unique identifier for personnel
                                PersonnelSession.setPersonnelDetails(personnelName, email, contactNum, department);

                                Log.d("Auth", "Login successful for personnel: " + personnelName);
                                Toast.makeText(getActivity(), "Personnel logged in", Toast.LENGTH_SHORT).show();
                                logUserActivity(personnelId, "Login");

                                // Proceed to the personnel main activity
                                Intent intent = new Intent(getActivity(), personnel_MainActivity.class);
                                intent.putExtra("PERSONNEL_ID", personnelId);
                                intent.putExtra("PERSONNEL_NAME", personnelName);
                                intent.putExtra("PERSONNEL_EMAIL", email);
                                intent.putExtra("PERSONNEL_CONTACT_NUM", contactNum);
                                intent.putExtra("PERSONNEL_DEPARTMENT", department);
                                startActivity(intent);
                                dismiss();
                            } else {
                                Log.d("Auth", "Invalid credentials: No documents found.");
                                Toast.makeText(getActivity(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("Auth", "Query failed: " + task.getException());
                            Toast.makeText(getActivity(), "Error fetching personnel: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void logUserActivity(String personnelID, String activityType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Reference to the personnel's ActivityLog subcollection
        CollectionReference activityLogRef = db.collection("personnel").document(personnelID).collection("ActivityLog");

        // Query to get the existing count of documents for the specific activity type (e.g., "Login" or "Logout")
        activityLogRef.whereEqualTo("type", activityType)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Count the number of existing logs of this type
                    int count = querySnapshot.size() + 1; // Next sequence number
                    String documentId = activityType + ": " + count; // Format the document ID

                    // Create the log entry data
                    Map<String, Object> logEntry = new HashMap<>();
                    logEntry.put("type", activityType); // "Login" or "Logout"
                    logEntry.put("timestamp", FieldValue.serverTimestamp()); // Auto-generated server timestamp

                    // Add the log entry with the custom document ID
                    activityLogRef.document(documentId)
                            .set(logEntry)
                            .addOnSuccessListener(aVoid -> Log.d("ActivityLog", "Log entry added with ID: " + documentId))
                            .addOnFailureListener(e -> Log.w("ActivityLog", "Error adding log entry with ID: " + documentId, e));
                })
                .addOnFailureListener(e -> Log.w("ActivityLog", "Error fetching existing logs for personnel: " + personnelID, e));
    }


}


