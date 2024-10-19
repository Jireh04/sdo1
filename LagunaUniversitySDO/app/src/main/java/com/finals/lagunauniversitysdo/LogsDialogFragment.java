package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class LogsDialogFragment extends DialogFragment {

    private static final String ARG_LOGS = "logs";

    // Static method to create a new instance of LogsDialogFragment with the logs data
    public static LogsDialogFragment newInstance(String logs) {
        LogsDialogFragment fragment = new LogsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOGS, logs);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the dialog layout
        View view = inflater.inflate(R.layout.dialog_logs, container, false);

        // Find the TextView in the layout and set the logs text
        TextView logsTextView = view.findViewById(R.id.logsTextView);

        // Get the logs from the arguments and set it to the TextView
        String logs = getArguments() != null ? getArguments().getString(ARG_LOGS) : "No logs available";
        logsTextView.setText(logs);

        return view;
    }
}
