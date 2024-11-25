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
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class PrefectLoginDialogFragment extends DialogFragment {

    EditText loginUsername, loginPassword;
    Button loginButton;
    ImageButton closeButton;
    ImageButton eyeButton; // Added eyeButton
    boolean isPasswordVisible = false; // Track the password visibility
    CheckBox rememberMeCheckBox; // Added CheckBox for "Remember Me"

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
        rememberMeCheckBox = view.findViewById(R.id.remember_me_checkbox); // Initialize "Remember Me" checkbox

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve saved credentials if "Remember Me" was checked
        SharedPreferences prefs = getActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String savedUsername = prefs.getString("username", "");
        String savedPassword = prefs.getString("password", "");
        loginUsername.setText(savedUsername);
        loginPassword.setText(savedPassword);


        // If credentials exist, set the "Remember Me" checkbox to checked
        if (!savedUsername.isEmpty() && !savedPassword.isEmpty()) {
            rememberMeCheckBox.setChecked(true);
        } else {
            rememberMeCheckBox.setChecked(false);
        }

        // Set onClickListener for login button
        loginButton.setOnClickListener(v -> {
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
        });

        // Set onClickListener for close button
        closeButton.setOnClickListener(v -> dismiss());

        // Set onClickListener for eye button to toggle password visibility
        eyeButton.setOnClickListener(v -> togglePasswordVisibility());

        return view;
    }

    private void authenticate(String username, String password) {
        // Check Firestore for matching username and hashed password in 'prefect' collection
        db.collection("prefect")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
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
                                PrefectSession.setPrefectDetails(prefectName, prefectEmail, prefectContactNum, prefectDepartment, username, password);

                                Log.d("Auth", "Login successful for prefect: " + prefectName);
                                Toast.makeText(getActivity(), "Prefect logged in", Toast.LENGTH_SHORT).show();
                                logUserActivity(prefectId, "Login");

                                // Save the credentials if "Remember Me" is checked
                                if (rememberMeCheckBox.isChecked()) {
                                    SharedPreferences prefs = getActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("username", username);
                                    editor.putString("password", password);
                                    editor.apply();
                                } else {
                                    // Clear saved credentials if "Remember Me" is not checked
                                    SharedPreferences prefs = getActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.remove("username");
                                    editor.remove("password");
                                    editor.apply();
                                }

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
                });
    }

    private void logUserActivity(String prefectID, String activityType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference activityLogRef = db.collection("prefect").document(prefectID).collection("ActivityLog");

        activityLogRef.whereEqualTo("type", activityType)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size() + 1;
                    String documentId = activityType + ": " + count;

                    Map<String, Object> logEntry = new HashMap<>();
                    logEntry.put("type", activityType);
                    logEntry.put("timestamp", FieldValue.serverTimestamp());

                    activityLogRef.document(documentId)
                            .set(logEntry)
                            .addOnSuccessListener(aVoid -> Log.d("ActivityLog", "Log entry added with ID: " + documentId))
                            .addOnFailureListener(e -> Log.w("ActivityLog", "Error adding log entry", e));
                })
                .addOnFailureListener(e -> Log.w("ActivityLog", "Error fetching logs", e));
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeButton.setImageResource(R.drawable.baseline_remove_red_eye_24); // Change to closed eye icon
        } else {
            loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeButton.setImageResource(R.drawable.baseline_remove_red_eye_24); // Change to open eye icon
        }

        loginPassword.setSelection(loginPassword.length());
        isPasswordVisible = !isPasswordVisible;
    }

    // Example hashing function (replace with a real implementation)
    private String hashPassword(String password) {
        return password; // Placeholder: replace with actual hashed value
    }
}
