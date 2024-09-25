package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class form extends AppCompatActivity {

    private Spinner termSpinner;  // Declare the Spinner
    private Spinner violationSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);


        termSpinner = findViewById(R.id.term_spinner);
        violationSpinner = findViewById(R.id.violation_spinner);

        // Create an array of choices for the Spinner
        String[] terms = {"First Term", "Second Term", "Summer", "Special Term"};
        String[] violation = {"Light Offense", "Serious Offense", "Major Offense"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> termAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, terms);
        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> violationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, violation);
        violationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the Spinner
        termSpinner.setAdapter(termAdapter);
        violationSpinner.setAdapter(violationAdapter);
    }
}
