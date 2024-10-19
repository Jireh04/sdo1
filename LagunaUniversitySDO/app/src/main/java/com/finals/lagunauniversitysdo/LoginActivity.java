package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

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
                PrefectLoginDialogFragment PrefectDialog = new PrefectLoginDialogFragment();
                PrefectDialog.show(getSupportFragmentManager(), "prefect_login_dialog");
            }
        });

    }
}
