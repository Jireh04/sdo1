package com.finals.lagunauniversitysdo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity implements PersonnelLoginDialogFragment.LoginDialogListener {

    Button studentLoginButton, PersonnelLoginButton, DODLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        studentLoginButton = findViewById(R.id.student_login_button);
        PersonnelLoginButton = findViewById(R.id.personnel_login_button);
        DODLoginButton = findViewById(R.id.dod_login_button);

        studentLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StudentLoginDialogFragment studentDialog = new StudentLoginDialogFragment();
                studentDialog.show(getSupportFragmentManager(), "student_login_dialog");
            }
        });

        PersonnelLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonnelLoginDialogFragment personnelDialog = new PersonnelLoginDialogFragment();
                personnelDialog.show(getSupportFragmentManager(), "personnel_login_dialog");
            }
        });

        DODLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DODLoginDialogFragment DODDialog = new DODLoginDialogFragment();
                DODDialog.show(getSupportFragmentManager(), "dod_login_dialog");
            }
        });
    }

    @Override
    public void onLoginSuccess(String userId) {
        // Start personnel_MainActivity and pass the USER_ID
        Intent intent = new Intent(LoginActivity.this, personnel_MainActivity.class);
        intent.putExtra("USER_ID", userId); // Pass the user ID
        startActivity(intent);
        finish(); // Close the login activity
    }
}
