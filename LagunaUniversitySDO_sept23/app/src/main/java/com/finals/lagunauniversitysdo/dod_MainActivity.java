package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class dod_MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up a TextView with "Hello, World!" programmatically
        TextView textView = new TextView(this);
        textView.setText("Hello, Madam!");
        textView.setTextSize(24);  // Optional: Change text size
        textView.setPadding(16, 16, 16, 16);  // Optional: Add padding

        // Set the TextView as the content of the activity
        setContentView(textView);
    }
}