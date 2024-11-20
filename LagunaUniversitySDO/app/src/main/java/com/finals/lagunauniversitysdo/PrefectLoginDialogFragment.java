package com.finals.lagunauniversitysdo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
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

public class PrefectLoginDialogFragment extends DialogFragment {

    EditText loginUsername, loginPassword;
    Button loginButton;
    ImageButton closeButton;

    ImageButton  eyeButton; // Added eyeButton
    boolean isPasswordVisible = false; // Track the password visibility


    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dod_login_dialog, container, false);

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

                // Perform login action for prefect
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
        // Check Firestore for matching username and hashed password in 'prefect' collection
        db.collection("prefect")
                .whereEqualTo("username", username)
                // Ideally, passwords should be hashed; for demonstration, we're using plain text here
                .get()
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.firestore.QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("Auth", "Query successful");

                            com.google.firebase.firestore.QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                                // Assuming you are storing hashed passwords
                                String storedPasswordHash = document.getString("password");
                                // Hash the input password (you need to implement your hashing logic here)
                                String inputPasswordHash = hashPassword(password);  // Hashing function should be implemented

                                // Check if the hashed input password matches the stored password hash
                                if (storedPasswordHash.equals(inputPasswordHash)) {
                                    String prefectId = document.getId();  // Get the document ID (prefect ID)
                                    String prefectName = document.getString("name");
                                    String prefectEmail = document.getString("email");
                                    Long prefectContactNum = document.getLong("contactNum");
                                    String prefectDepartment = document.getString("department");

                                    // Store Prefect ID and details in PrefectSession, now with username and password
                                    PrefectSession.setPrefectId(prefectId);
                                    PrefectSession.setPrefectDetails(prefectId, prefectName, prefectEmail, prefectContactNum, prefectDepartment, username, password);

                                    Log.d("Auth", "Login successful for prefect: " + prefectName);
                                    Toast.makeText(getActivity(), "Prefect logged in", Toast.LENGTH_SHORT).show();
                                    logUserActivity(prefectId, "Login");

                                    // Proceed to the prefect main activity
                                    Intent intent = new Intent(getActivity(), Prefect_MainActivity.class);
                                    intent.putExtra("PREFECT_ID", prefectId);
                                    intent.putExtra("PREFECT_NAME", prefectName);
                                    intent.putExtra("PREFECT_EMAIL", prefectEmail);
                                    intent.putExtra("PREFECT_CONTACT_NUM", prefectContactNum);
                                    intent.putExtra("PREFECT_DEPARTMENT", prefectDepartment);
                                    startActivity(intent);
                                    dismiss();
                                } else {
                                    Log.d("Auth", "Invalid credentials: Passwords do not match.");
                                    Toast.makeText(getActivity(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.d("Auth", "Invalid credentials: No documents found.");
                                Toast.makeText(getActivity(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("Auth", "Query failed: " + task.getException());
                            Toast.makeText(getActivity(), "Error fetching prefect: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void logUserActivity(String prefectID, String activityType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Reference to the prefect's ActivityLog subcollection
        CollectionReference activityLogRef = db.collection("prefect").document(prefectID).collection("ActivityLog");

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
                .addOnFailureListener(e -> Log.w("ActivityLog", "Error fetching existing logs for prefect: " + prefectID, e));
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

    // Example hashing function (replace with a real implementation)
    private String hashPassword(String password) {
        // Implement your hashing logic here (e.g., using BCrypt)
        return password; // Placeholder: replace with actual hashed value
    }}