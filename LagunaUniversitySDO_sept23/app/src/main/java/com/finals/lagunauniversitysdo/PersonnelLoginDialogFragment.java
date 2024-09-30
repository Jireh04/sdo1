package com.finals.lagunauniversitysdo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonnelLoginDialogFragment extends DialogFragment {

    EditText loginUsername, loginPassword;
    Button loginButton;
    ImageButton closeButton;

    private FirebaseFirestore db;
    private LoginDialogListener listener; // Declare the listener

    public interface LoginDialogListener {
        void onLoginSuccess(String userId); // Define the callback method
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure that the host activity implements the interface
        if (context instanceof LoginDialogListener) {
            listener = (LoginDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement LoginDialogListener");
        }
    }

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
        // Check Firestore for matching username and password in 'personnel' collection
        db.collection("personnel")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password) // Ideally, passwords should be hashed
                .get()
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.firestore.QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            com.google.firebase.firestore.QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Authentication successful
                                Toast.makeText(getActivity(), "Personnel logged in", Toast.LENGTH_SHORT).show();

                                // Get the user ID (document ID)
                                String userId = querySnapshot.getDocuments().get(0).getId(); // Get the ID of the first matched document

                                // Notify the listener about the login success
                                if (listener != null) {
                                    listener.onLoginSuccess(userId);
                                }

                                dismiss(); // Dismiss the dialog
                            } else {
                                // No match found
                                Toast.makeText(getActivity(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Error occurred
                            Toast.makeText(getActivity(), "Error fetching user: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
