package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class reporters extends Fragment {

    private RecyclerView recyclerViewReporters;
    private ReporterAdapter reporterAdapter;
    private FirebaseFirestore db;

    private Map<String, List<ReferralLog>> referrerLogsMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reporters, container, false);

        recyclerViewReporters = view.findViewById(R.id.reportersRecyclerView);
        db = FirebaseFirestore.getInstance();

        // Initialize the HashMap to store referrers and their logs
        referrerLogsMap = new HashMap<>();

        // Initialize the adapter
        reporterAdapter = new ReporterAdapter(new ArrayList<>(), reporterName -> {
            // Handle "View Logs" click for the specific reporter
            fetchLogsData(reporterName, view);
        });

        recyclerViewReporters.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewReporters.setAdapter(reporterAdapter);

        fetchReferrersData();

        return view;
    }

    private void fetchReferrersData() {
        // Fetch data from all three collections concurrently using Firestore
        db.collection("personnel_refferal_history")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        processReferralData(task.getResult(), "personnel_referrer");
                    }
                });

        db.collection("student_refferal_history")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        processReferralData(task.getResult(), "student_referrer");
                    }
                });

        db.collection("prefect_referral_history")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        processReferralData(task.getResult(), "prefect_referrer");
                    }
                });
    }

    private void processReferralData(Iterable<QueryDocumentSnapshot> documents, String referrerType) {
        for (QueryDocumentSnapshot document : documents) {
            String referrerName = document.getString(referrerType);
            if (referrerName != null) {
                // Initialize the list if it doesn't exist yet
                if (!referrerLogsMap.containsKey(referrerName)) {
                    referrerLogsMap.put(referrerName, new ArrayList<>());
                }

                // Get the necessary details (student_id, status, date)
                String studentId = document.getString("student_id");
                String status = document.getString("status");
                String date = document.getString("date");

                // Create a new log object
                ReferralLog log = new ReferralLog(studentId, status, date);

                // Add the log to the map
                referrerLogsMap.get(referrerName).add(log);
            }
        }

        // Update the adapter with the unique referrers
        List<String> allReferrers = new ArrayList<>(referrerLogsMap.keySet());
        reporterAdapter.updateData(allReferrers);
    }

    private void fetchLogsData(String reporterName, View view) {
        // Retrieve all logs from the HashMap for the selected reporter
        List<ReferralLog> referrerLogs = referrerLogsMap.get(reporterName);
        if (referrerLogs != null) {
            StringBuilder logs = new StringBuilder();

            // Build the log string with student_id, status, and date
            for (ReferralLog log : referrerLogs) {
                logs.append("Student ID: ").append(log.getStudentId())
                        .append("\nStatus: ").append(log.getStatus())
                        .append("\nDate: ").append(log.getDate())
                        .append("\n\n");
            }

            // Show logs in a dialog for the chosen reporter
            LogsDialogFragment dialog = LogsDialogFragment.newInstance("Logs:\n" + logs.toString());
            dialog.show(getParentFragmentManager(), "logsDialog");
        }
    }

    // Data model to hold referral log information
    public static class ReferralLog {
        private String studentId;
        private String status;
        private String date;

        public ReferralLog(String studentId, String status, String date) {
            this.studentId = studentId;
            this.status = status;
            this.date = date;
        }

        public String getStudentId() {
            return studentId;
        }

        public String getStatus() {
            return status;
        }

        public String getDate() {
            return date;
        }
    }
}
