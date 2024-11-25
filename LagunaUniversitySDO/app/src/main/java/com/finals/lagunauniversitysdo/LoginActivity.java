package com.finals.lagunauniversitysdo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class LoginActivity extends AppCompatActivity {

    Button studentLoginButton, PersonnelLoginButton, DODLoginButton;
    private StudentLoginDialogFragment studentDialog;
    private PersonnelLoginDialogFragment personnelDialog;
    private PrefectLoginDialogFragment prefectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if any of the users are already logged in (prefect, personnel, or student)
        if (PrefectSession.isLoggedIn()) {
            // If the prefect is logged in, redirect to Prefect_MainActivity
            Intent intent = new Intent(LoginActivity.this, Prefect_MainActivity.class);
            startActivity(intent);
            finish(); // Close the LoginActivity so the user can't come back
            return; // Exit the onCreate method early
        } else if (PersonnelSession.isLoggedIn()) {
            // If the personnel is logged in, redirect to PersonnelMainActivity
            Intent intent = new Intent(LoginActivity.this, Prefect_MainActivity.class);
            startActivity(intent);
            finish(); // Close the LoginActivity so the user can't come back
            return; // Exit the onCreate method early
        } else if (UserSession.isLoggedIn()) {
            // If the student is logged in, redirect to StudentMainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the LoginActivity so the user can't come back
            return; // Exit the onCreate method early
        }

        setContentView(R.layout.activity_login);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        studentLoginButton = findViewById(R.id.student_login_button);
        PersonnelLoginButton = findViewById(R.id.personnel_login_button);
        DODLoginButton = findViewById(R.id.dod_login_button);

        studentLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the student dialog is already showing
                if (studentDialog == null || !studentDialog.isAdded()) {
                    studentDialog = new StudentLoginDialogFragment();
                    studentDialog.show(getSupportFragmentManager(), "student_login_dialog");
                }
            }
        });

        PersonnelLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the personnel dialog is already showing
                if (personnelDialog == null || !personnelDialog.isAdded()) {
                    personnelDialog = new PersonnelLoginDialogFragment();
                    personnelDialog.show(getSupportFragmentManager(), "personnel_login_dialog");
                }
            }
        });

        DODLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the prefect dialog is already showing
                if (prefectDialog == null || !prefectDialog.isAdded()) {
                    prefectDialog = new PrefectLoginDialogFragment();
                    prefectDialog.show(getSupportFragmentManager(), "prefect_login_dialog");
                }
            }
        });
    }

    // Disable the back button to prevent navigation back to this activity
    @Override
    public void onBackPressed() {
        // Simply override and do nothing to prevent the back button from working
    }
}
