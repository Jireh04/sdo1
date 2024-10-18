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
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class reporters extends Fragment {

    private RecyclerView recyclerViewReporters;
    private ReporterAdapter reporterAdapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reporters, container, false); // Update layout resource

        recyclerViewReporters = view.findViewById(R.id.reportersRecyclerView);
        db = FirebaseFirestore.getInstance();

        // Initialize the adapter
        reporterAdapter = new ReporterAdapter(new ArrayList<>(), reporterName -> {
            // Handle "View Logs" click for the specific reporter
            // Add your logic here for what happens when "View Logs" is clicked
            System.out.println("View Logs clicked for: " + reporterName);
        });

        recyclerViewReporters.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewReporters.setAdapter(reporterAdapter);

        fetchReporters();

        return view;
    }

    private void fetchReporters() {
        List<String> reporterList = new ArrayList<>();

        // Fetch from "personnel_refferal_history"
        db.collection("personnel_refferal_history")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String referrerName = document.getString("personnel_referrer");
                            if (referrerName != null && !reporterList.contains(referrerName)) {
                                reporterList.add(referrerName);
                            }
                        }
                        fetchStudentReferrers(reporterList);
                    }
                });
    }

    private void fetchStudentReferrers(List<String> reporterList) {
        // Fetch from "student_refferal_history"
        db.collection("student_refferal_history")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String referrerName = document.getString("student_referrer");
                            if (referrerName != null && !reporterList.contains(referrerName)) {
                                reporterList.add(referrerName);
                            }
                        }
                        reporterAdapter.updateData(reporterList); // Update adapter with the new data
                    }
                });
    }
}
