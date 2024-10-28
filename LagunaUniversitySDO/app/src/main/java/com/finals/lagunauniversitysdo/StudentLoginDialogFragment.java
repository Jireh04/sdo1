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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentLoginDialogFragment extends DialogFragment {

    EditText loginUsername, loginPassword;
    Button loginButton;
    ImageButton closeButton;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_login_dialog, container, false);

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
                                Toast.makeText(getActivity(), "Student logged in", Toast.LENGTH_SHORT).show();

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


}







