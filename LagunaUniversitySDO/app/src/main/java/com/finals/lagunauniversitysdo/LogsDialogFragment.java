package com.finals.lagunauniversitysdo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

public class LogsDialogFragment extends DialogFragment {

    private static final String ARG_LOGS = "logs";
    private static final String ARG_REFERRER_TYPE = "referrer_type";
    private static final String ARG_SELECTED_REFERRER = "selected_referrer"; // New argument for selected referrer
    private FirebaseFirestore db = FirebaseFirestore.getInstance();  // Firestore instance

    // Create a new instance of LogsDialogFragment
    public static LogsDialogFragment newInstance(String logs, String referrerType, String selectedReferrer) {
        LogsDialogFragment fragment = new LogsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOGS, logs);
        args.putString(ARG_REFERRER_TYPE, referrerType);  // Add referrer type to arguments
        args.putString(ARG_SELECTED_REFERRER, selectedReferrer); // Add selected referrer to arguments
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the builder to create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_logs, null);

        // Find the logs container
        LinearLayout logsContainer = dialogView.findViewById(R.id.logs_container);

        // Get the logs, referrer type, and selected referrer from the arguments
        String logs = getArguments() != null ? getArguments().getString(ARG_LOGS) : "";
        String referrerType = getArguments() != null ? getArguments().getString(ARG_REFERRER_TYPE) : "";
        String selectedReferrer = getArguments() != null ? getArguments().getString(ARG_SELECTED_REFERRER) : "";

        // Split the logs into individual log entries
        String[] logEntries = logs.split("\n\n"); // Assuming logs are separated by two newlines

        // Dynamically add logs and a view button for each entry
        for (String logEntry : logEntries) {
            // Use the selected referrer directly in the logs
            TextView textView = new TextView(getContext());
            textView.setText(logEntry + "\nReferrer: " + selectedReferrer);  // Show the selected referrer with the log entry
            textView.setPadding(0, 0, 0, 16); // Add padding for better UI

            // Create a button for the "View" action
            Button viewButton = new Button(getContext());
            viewButton.setText("View");

            // Modify the onClickListener for the "View" button to include referrerName
            viewButton.setOnClickListener(v -> {
                // Handle view button click and navigate to PrefectView
                Intent intent = new Intent(getContext(), PrefectView.class);

                // Pass the log entry, referrer type, and referrer name to PrefectView
                intent.putExtra("log_entry", logEntry);
                intent.putExtra("referrer_type", referrerType);  // Send the referrer type
                intent.putExtra("referrer_name", selectedReferrer);  // Send the selected referrer name

                // Start the PrefectView activity
                startActivity(intent);
            });

            // Add the TextView and Button to the logs container
            logsContainer.addView(textView);
            logsContainer.addView(viewButton);

            // Save the log entry, referrer name, and referrerType to SharedPreferences
            saveLogData(logEntry, selectedReferrer, referrerType);
        }

        // Set up the dialog builder with the view
        builder.setView(dialogView)
                .setTitle("Report Logs")
                .setPositiveButton("OK", (dialog, which) -> dismiss());

        // Create and return the dialog
        return builder.create();
    }

    // Method to save log data to SharedPreferences
    private void saveLogData(String logEntry, String referrerName, String referrerType) {
        // Get the SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("ReferrerLogs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the log entry, referrer name, and referrerType to SharedPreferences
        String storedLogs = sharedPreferences.getString(referrerType, "");
        storedLogs += logEntry + " Referrer: " + referrerName + "\n\n"; // Append new log entry with referrer name

        // Put the updated string back into SharedPreferences
        editor.putString(referrerType, storedLogs);
        editor.apply();  // Commit the changes
    }
}
