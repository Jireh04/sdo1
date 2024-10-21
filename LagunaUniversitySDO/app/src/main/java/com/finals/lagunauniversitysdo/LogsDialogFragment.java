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
    private static final String ARG_SELECTED_REFERRER = "selected_referrer";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static LogsDialogFragment newInstance(String logs, String referrerType, String selectedReferrer) {
        LogsDialogFragment fragment = new LogsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOGS, logs);
        args.putString(ARG_REFERRER_TYPE, referrerType);
        args.putString(ARG_SELECTED_REFERRER, selectedReferrer);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_logs, null);

        LinearLayout logsContainer = dialogView.findViewById(R.id.logs_container);
        String logs = getArguments() != null ? getArguments().getString(ARG_LOGS) : "";
        String referrerType = getArguments() != null ? getArguments().getString(ARG_REFERRER_TYPE) : "";
        String selectedReferrer = getArguments() != null ? getArguments().getString(ARG_SELECTED_REFERRER) : "";

        // Display logs
        String[] logEntries = logs.split("\n\n");
        for (String logEntry : logEntries) {
            TextView textView = new TextView(getContext());
            textView.setText(logEntry);
            textView.setPadding(0, 0, 0, 16);

            Button viewButton = new Button(getContext());
            viewButton.setText("View");

            viewButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ReporterView.class);

                // Assuming your logEntry is formatted correctly
                String[] details = logEntry.split(",");
                if (details.length >= 6) { // Ensure there are enough details
                    intent.putExtra("student_id", details[0].split(": ")[1].trim());
                    intent.putExtra("student_name", details[1].split(": ")[1].trim());
                    intent.putExtra("student_program", details[2].split(": ")[1].trim());
                    intent.putExtra("student_contact", details[3].split(": ")[1].trim());
                    intent.putExtra("student_year", details[4].split(": ")[1].trim());
                    intent.putExtra("student_block", details[5].split(": ")[1].trim());
                }

                // Add referrer info if needed
                intent.putExtra("referrer_type", referrerType);
                intent.putExtra("referrer_name", selectedReferrer);
                startActivity(intent);
            });

            logsContainer.addView(textView);
            logsContainer.addView(viewButton);

            saveLogData(logEntry, selectedReferrer, referrerType);
        }

        builder.setView(dialogView)
                .setTitle("Report Logs")
                .setPositiveButton("OK", (dialog, id) -> dismiss());

        return builder.create();
    }

    private void saveLogData(String logEntry, String selectedReferrer, String referrerType) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LogsPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("last_log", logEntry);
        editor.putString("selected_referrer", selectedReferrer);
        editor.putString("referrer_type", referrerType);
        editor.apply();
    }
}
