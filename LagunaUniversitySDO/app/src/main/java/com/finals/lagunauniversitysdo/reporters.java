package com.finals.lagunauniversitysdo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            String referrerName = document.getString(referrerType);  // Get the referrer name based on type

            if (referrerName != null) {
                // Initialize the list if it doesn't exist yet
                if (!referrerLogsMap.containsKey(referrerName)) {
                    referrerLogsMap.put(referrerName, new ArrayList<>());
                }

                // Fetch additional data fields from the document
                String studentId = document.getString("student_id");
                String status = document.getString("status");
                String date = document.getString("date");
                String studentName = document.getString("student_name");
                String studentProgram = document.getString("student_program");
                String term = document.getString("term");
                String userConcern = document.getString("user_concern");
                String violation = document.getString("violation");

                // Create a new log object with all the fetched data and referrerType
                ReferralLog log = new ReferralLog(studentId, status, date, studentName, studentProgram, term, userConcern, violation, referrerType);

                // Add the log to the map for the respective referrer
                referrerLogsMap.get(referrerName).add(log);
            }
        }

        // Update the adapter with the unique referrers
        List<String> allReferrers = new ArrayList<>(referrerLogsMap.keySet());
        reporterAdapter.updateData(allReferrers);  // Update the adapter with referrer names
    }

    private void fetchLogsData(String reporterName, View view) {
        List<ReferralLog> referrerLogs = referrerLogsMap.get(reporterName);
        if (referrerLogs != null) {
            StringBuilder logs = new StringBuilder();
            String referrerType = "";  // Define a variable for referrerType

            // Construct the log string with all the relevant data
            for (ReferralLog log : referrerLogs) {
                logs.append("Student ID: ").append(log.getStudentId())
                        .append("\nName: ").append(log.getStudentName())
                        .append("\nProgram: ").append(log.getStudentProgram())
                        .append("\nStatus: ").append(log.getStatus())
                        .append("\nDate: ").append(log.getDate())
                        .append("\nTerm: ").append(log.getTerm())
                        .append("\nUser Concern: ").append(log.getUserConcern())
                        .append("\nViolation: ").append(log.getViolation())
                        .append("\n\n");

                // Retrieve the referrerType directly from the log
                referrerType = log.getReferrerType();
            }

            // If there are logs for the selected referrer, open a dialog
            if (logs.length() > 0) {
                // Pass both logs, referrerType, and the selected referrer (reporterName) to the dialog
                LogsDialogFragment dialog = LogsDialogFragment.newInstance(logs.toString(), referrerType, reporterName);
                dialog.show(getParentFragmentManager(), "logsDialog");
            }
        }
    }

    // Data model to hold referral log information
    public static class ReferralLog {
        private String studentId;
        private String status;
        private String date;
        private String studentName;
        private String studentProgram;
        private String term;
        private String userConcern;
        private String violation;
        private String referrerType;  // New field to track the referrer type

        public ReferralLog(String studentId, String status, String date, String studentName, String studentProgram,
                           String term, String userConcern, String violation, String referrerType) {
            this.studentId = studentId;
            this.status = status;
            this.date = date;
            this.studentName = studentName;
            this.studentProgram = studentProgram;
            this.term = term;
            this.userConcern = userConcern;
            this.violation = violation;
            this.referrerType = referrerType;  // Initialize the referrerType
        }

        // Getters for all fields
        public String getStudentId() {
            return studentId;
        }

        public String getStatus() {
            return status;
        }

        public String getDate() {
            return date;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getStudentProgram() {
            return studentProgram;
        }

        public String getTerm() {
            return term;
        }

        public String getUserConcern() {
            return userConcern;
        }

        public String getViolation() {
            return violation;
        }

        public String getReferrerType() {
            return referrerType;  // Getter for referrerType
        }
    }
}
