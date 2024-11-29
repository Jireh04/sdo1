package com.finals.lagunauniversitysdo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
public class PersonnelLoginDialogFragment extends DialogFragment {

    EditText loginUsername, loginPassword;
    Button loginButton;
    ImageButton closeButton;
    ImageButton eyeButton;
    CheckBox rememberMeCheckBox;
    boolean isPasswordVisible = false;

    private FirebaseFirestore db;

    private SharedPreferences sharedPreferences; // To handle SharedPreferences

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personnel_login_dialog, container, false);

        loginUsername = view.findViewById(R.id.login_username);
        loginPassword = view.findViewById(R.id.login_password);
        loginButton = view.findViewById(R.id.login_button);
        closeButton = view.findViewById(R.id.close_button);
        eyeButton = view.findViewById(R.id.eye_button);
        rememberMeCheckBox = view.findViewById(R.id.remember_me_checkbox);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Separate SharedPreferences for personnel
        sharedPreferences = getActivity().getSharedPreferences("PersonnelLoginPrefs", getActivity().MODE_PRIVATE);

        // Check if the credentials are saved in SharedPreferences
        if (sharedPreferences.getBoolean("rememberMe", false)) {
            String savedUsername = sharedPreferences.getString("username", "");
            String savedPassword = sharedPreferences.getString("password", "");

            loginUsername.setText(savedUsername);
            loginPassword.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
        }

        loginButton.setOnClickListener(v -> {
            String username = loginUsername.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter both username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            authenticate(username, password);
        });

        closeButton.setOnClickListener(v -> dismiss());

        eyeButton.setOnClickListener(v -> togglePasswordVisibility());

        return view;
    }

    private void authenticate(String username, String password) {
        // Clear previous session data
        PersonnelSession.clearSession();

        Log.d("Auth", "Attempting to log in personnel with username: " + username);

        db.collection("personnel")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password) // Note: Use a secure way to handle passwords!
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        com.google.firebase.firestore.QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String personnelId = document.getId();
                            String personnelName = document.getString("name");
                            String email = document.getString("email");
                            String contactNum = document.getString("contact");
                            String department = document.getString("department");
                            String personnelUniqueId = document.getString("personnelUniqueId");

                            // Store personnel ID and details in PersonnelSession
                            PersonnelSession.setPersonnelId(personnelId);
                            PersonnelSession.setPersonnelUniqueId(personnelUniqueId);
                            PersonnelSession.setPersonnelDetails(personnelName, email, contactNum, department);

                            Log.d("Auth", "Login successful for personnel: " + personnelName);
                            Toast.makeText(getActivity(), "Personnel logged in", Toast.LENGTH_SHORT).show();
                            logUserActivity(personnelId, "Login");

                            // Save credentials if "Remember Me" is checked
                            if (rememberMeCheckBox.isChecked()) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("username", username);
                                editor.putString("password", password);
                                editor.putBoolean("rememberMe", true);
                                editor.apply();
                            } else {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove("username");
                                editor.remove("password");
                                editor.putBoolean("rememberMe", false);
                                editor.apply();
                            }

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
                            Toast.makeText(getActivity(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error fetching personnel", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void logUserActivity(String personnelID, String activityType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference activityLogRef = db.collection("personnel").document(personnelID).collection("ActivityLog");

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
            eyeButton.setImageResource(R.drawable.baseline_remove_red_eye_24);
        } else {
            loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeButton.setImageResource(R.drawable.baseline_eye);
        }

        loginPassword.setSelection(loginPassword.length());
        isPasswordVisible = !isPasswordVisible;
    }
}
