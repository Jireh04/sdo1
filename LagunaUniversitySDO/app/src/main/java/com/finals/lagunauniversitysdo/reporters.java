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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class reporters extends Fragment {

    private RecyclerView recyclerViewReporters;
    private ReporterAdapter reporterAdapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reporters, container, false);

        recyclerViewReporters = view.findViewById(R.id.reportersRecyclerView);
        db = FirebaseFirestore.getInstance();

        // Initialize the adapter with a click listener
        reporterAdapter = new ReporterAdapter(new ArrayList<>(), reporterName -> {
            // Handle "View Logs" click for the specific reporter
            // Add your logic here for viewing logs for this reporter
            System.out.println("View Logs clicked for: " + reporterName);
        });

        recyclerViewReporters.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewReporters.setAdapter(reporterAdapter);

        fetchReporters();

        return view;
    }

    private void fetchReporters() {
        Set<String> reporterSet = new HashSet<>(); // Use a Set to prevent duplicates

        // Fetch data from each main collection and its subcollection
        fetchFromCollection("students", "student_refferal_history", "student_referrer", reporterSet);
        fetchFromCollection("personnel", "personnel_refferal_history", "personnel_referrer", reporterSet);
        fetchFromCollection("prefect", "prefect_referral_history", "prefect_referrer", reporterSet);
    }

    private void fetchFromCollection(String collectionName, String subCollectionName, String referrerField, Set<String> reporterSet) {
        // Fetch documents from the main collection
        db.collection(collectionName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String documentId = document.getId();

                    // Fetch from the subcollection within each document
                    db.collection(collectionName)
                            .document(documentId)
                            .collection(subCollectionName)
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot subDoc : subTask.getResult()) {
                                        String referrerName = subDoc.getString(referrerField);
                                        if (referrerName != null) {
                                            reporterSet.add(referrerName); // Add referrer name to the set
                                        }
                                    }
                                    // Update the RecyclerView after fetching each subcollection
                                    updateRecyclerView(new ArrayList<>(reporterSet));
                                }
                            });
                }
            }
        });
    }

    private void updateRecyclerView(List<String> reporterList) {
        reporterAdapter.updateData(reporterList); // Update adapter with new data
    }
}