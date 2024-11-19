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
import android.text.InputType;


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

public class StudentLoginDialogFragment extends DialogFragment {

    EditText loginUsername, loginPassword;
    Button loginButton;
    ImageButton closeButton;

    ImageButton  eyeButton; // Added eyeButton
    boolean isPasswordVisible = false; // Track the password visibility

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_login_dialog, container, false);

        loginUsername = view.findViewById(R.id.login_username);
        loginPassword = view.findViewById(R.id.login_password);
        loginButton = view.findViewById(R.id.login_button);
        closeButton = view.findViewById(R.id.close_button);
        eyeButton = view.findViewById(R.id.eye_button); // Initialize eye button
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

                // Perform login action
                authenticate(username, password);
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // Set onClickListener for eye button to toggle password visibility
        eyeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });
        return view;
    }

    private void authenticate(String username, String password) {
        // Clear previous session data
        UserSession.clearSession();

        Log.d("Auth", "Attempting to log in with username: " + username);

        // Validate inputs
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Username and password must not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use Firebase Firestore to authenticate the user
        db.collection("students")
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
                                String studId = document.getId();  // Get the document ID (student ID)
                                String firstName = document.getString("first_name");
                                String lastName = document.getString("last_name");
                                String name = document.getString("name");
                                String email = document.getString("email");
                                Long contactNum = document.getLong("contacts");
                                String program = document.getString("program");

                                // Store user ID and student details in UserSession
                                UserSession.setStudId(studId);
                                UserSession.setStudentDetails(firstName, lastName, email, contactNum, program, name);

                                Log.d("Auth", "Login successful for user: " + name );

                                // After successful login
                                Toast.makeText(getActivity(), "Student logged in", Toast.LENGTH_SHORT).show();
                                logUserActivity(studId, "Login");

                                // Proceed to the main activity
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.putExtra("USER_ID", studId);
                                intent.putExtra("STUDENT_NAME", name);
                                intent.putExtra("FIRST_NAME", firstName);
                                intent.putExtra("LAST_NAME", lastName);
                                startActivity(intent);
                                dismiss();
                            } else {
                                Log.d("Auth", "Invalid credentials: No documents found.");
                                Toast.makeText(getActivity(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("Auth", "Query failed: " + task.getException());
                            Toast.makeText(getActivity(), "Error fetching user: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void logUserActivity(String studentId, String activityType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Reference to the student's ActivityLog subcollection
        CollectionReference activityLogRef = db.collection("students").document(studentId).collection("ActivityLog");

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
                .addOnFailureListener(e -> Log.w("ActivityLog", "Error fetching existing logs for student: " + studentId, e));
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide the password
            loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeButton.setImageResource(R.drawable.baseline_remove_red_eye_24); // Change to closed eye icon
        } else {
            // Show the password
            loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeButton.setImageResource(R.drawable.baseline_remove_red_eye_24); // Change to open eye icon
        }

        // Move cursor to the end after toggling visibility
        loginPassword.setSelection(loginPassword.length());

        // Toggle the visibility state
        isPasswordVisible = !isPasswordVisible;
    }


}







