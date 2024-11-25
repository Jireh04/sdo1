package com.finals.lagunauniversitysdo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.pdf.PdfDocument;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import android.widget.EditText;
import android.view.View;

import android.widget.Toast;
import android.widget.ImageView;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.app.AlertDialog;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;


import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Map;

public class refferals_prefect extends Fragment {

    private FirebaseFirestore db;



    public refferals_prefect() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_refferals_prefect, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the TabLayout
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        // Set custom background for each tab
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                TextView customTab = new TextView(requireContext());
                customTab.setText(tab.getText());
                customTab.setGravity(Gravity.CENTER);
                customTab.setBackgroundResource(R.drawable.tab_item_background);
                tab.setCustomView(customTab);
            }
        }

        // Clear any default tab selection
        tabLayout.clearOnTabSelectedListeners();

        // Set tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // Pending
                        onPendingClick();
                        tab.getCustomView().setSelected(true);
                        break;
                    case 1: // Accepted
                        onAcceptedClick();
                        tab.getCustomView().setSelected(true);
                        break;
                    case 2: // Rejected
                        onRejectedClick();
                        tab.getCustomView().setSelected(true);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getCustomView() != null) {
                    tab.getCustomView().setSelected(false);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Automatically call onPendingClick() to display the "Pending" section when the fragment is created
        onPendingClick();

        // Optionally, select the "Pending" tab by default
        tabLayout.getTabAt(0).select();
    }




    public void onPendingClick() {
        RecyclerView recyclerViewPending = getView().findViewById(R.id.recycler_view_pending);
        recyclerViewPending.setVisibility(View.VISIBLE);

        // Clear other sections
        RecyclerView recyclerViewAcceptedReferrals = getView().findViewById(R.id.recycler_view_accepted_referrals);
        recyclerViewAcceptedReferrals.setVisibility(View.GONE);
        RecyclerView recyclerViewRejectedReferrals = getView().findViewById(R.id.recycler_view_rejected_referrals);
        recyclerViewRejectedReferrals.setVisibility(View.GONE);

        // Initialize RecyclerView and adapter
        recyclerViewPending.setLayoutManager(new LinearLayoutManager(getActivity()));
        List<referral> referralList = new ArrayList<>();

        // Query Firestore collections for "pending" status
        db.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot studentDocument : task.getResult().getDocuments()) {
                    db.collection("students").document(studentDocument.getId())
                            .collection("student_refferal_history")
                            .orderBy("date", Query.Direction.DESCENDING) // Sort by date in descending order
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        if ("pending".equals(referralDocument.getString("status"))) {
                                            Map<String, Object> referralData = referralDocument.getData();
                                            if (referralData != null) {
                                                String studentReferrer = (String) referralData.get("student_referrer");
                                                String referralDate = (String) referralData.get("date");

                                                // Add data to the referral list
                                                referralList.add(new referral(studentReferrer, referralDate, "Select Action"));
                                            }
                                        }
                                    }
                                    // Set the adapter
                                    PendingAdapter pendingAdapter = new PendingAdapter(referralList, getActivity());
                                    recyclerViewPending.setAdapter(pendingAdapter);
                                }
                            });
                }
            }
        });

        // Fetch pending referrals from personnel
        db.collection("personnel").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot studentDocument : task.getResult().getDocuments()) {
                    db.collection("personnel").document(studentDocument.getId())
                            .collection("personnel_refferal_history")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        if ("pending".equals(referralDocument.getString("status"))) {
                                            Map<String, Object> referralData = referralDocument.getData();
                                            if (referralData != null) {
                                                String studentReferrer = (String) referralData.get("student_referrer");
                                                String referralDate = (String) referralData.get("date");

                                                // Add data to the referral list
                                                referralList.add(new referral(studentReferrer, referralDate, "Select Action"));
                                            }
                                        }
                                    }
                                    // Set the adapter
                                    PendingAdapter pendingAdapter = new PendingAdapter(referralList, getActivity());
                                    recyclerViewPending.setAdapter(pendingAdapter);
                                }
                            });
                }
            }
        });
    }



    public void onAcceptedClick() {
        // Clear other sections (Pending, Rejected)
        getView().findViewById(R.id.recycler_view_pending).setVisibility(View.GONE);
        getView().findViewById(R.id.recycler_view_rejected_referrals).setVisibility(View.GONE);

        Log.d("AcceptedClick", "Accepted button clicked");

        // Show the accepted section
        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view_accepted_referrals);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Prepare a list for accepted referrals
        List<DocumentSnapshot> acceptedReferrals = new ArrayList<>();

        // Fetch accepted referrals from students
        db.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot studentDocument : task.getResult().getDocuments()) {
                    db.collection("students").document(studentDocument.getId())
                            .collection("student_refferal_history")
                            .whereEqualTo("status", "accepted")
                            .orderBy("date", Query.Direction.DESCENDING)
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    acceptedReferrals.addAll(subTask.getResult().getDocuments());
                                    sortAndUpdateAccepted(acceptedReferrals, recyclerView);
                                }
                            });
                }
            }
        });

        // Fetch accepted referrals from personnel
        db.collection("personnel").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot personnelDocument : task.getResult().getDocuments()) {
                    db.collection("personnel").document(personnelDocument.getId())
                            .collection("personnel_refferal_history")
                            .whereEqualTo("status", "accepted")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    acceptedReferrals.addAll(subTask.getResult().getDocuments());
                                    sortAndUpdateAccepted(acceptedReferrals, recyclerView);

                                }
                            });
                }
            }
        });
    }

    private void sortAndUpdateAccepted(List<DocumentSnapshot> acceptedReferrals, RecyclerView recyclerView) {
        // Sort the list based on the "date" field
        Collections.sort(acceptedReferrals, (doc1, doc2) -> {
            String date1 = doc1.getString("date");
            String date2 = doc2.getString("date");

            return date2.compareTo(date1); // Descending order
        });

        // Update the RecyclerView Adapter
        AcceptedReferralsAdapter adapter = new AcceptedReferralsAdapter(acceptedReferrals);
        recyclerView.setAdapter(adapter);
    }


    // Fetch rejected referrals from Firestore
    public void onRejectedClick() {
        // Hide other sections
        getView().findViewById(R.id.recycler_view_pending).setVisibility(View.GONE);
        getView().findViewById(R.id.recycler_view_accepted_referrals).setVisibility(View.GONE);

        // Show the RecyclerView for rejected referrals
        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view_rejected_referrals);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Prepare a list for rejected referrals
        List<DocumentSnapshot> rejectedReferrals = new ArrayList<>();

        // Fetch rejected referrals from students
        db.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot studentDocument : task.getResult().getDocuments()) {
                    db.collection("students").document(studentDocument.getId())
                            .collection("student_refferal_history")
                            .whereEqualTo("status", "rejected")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    rejectedReferrals.addAll(subTask.getResult().getDocuments());
                                    sortAndUpdateRejected(rejectedReferrals, recyclerView);
                                }
                            });
                }
            }
        });

        // Fetch rejected referrals from personnel
        db.collection("personnel").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot personnelDocument : task.getResult().getDocuments()) {
                    db.collection("personnel").document(personnelDocument.getId())
                            .collection("personnel_refferal_history")
                            .whereEqualTo("status", "rejected")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    rejectedReferrals.addAll(subTask.getResult().getDocuments());
                                    sortAndUpdateRejected(rejectedReferrals, recyclerView);
                                }
                            });
                }
            }
        });
    }

    private void sortAndUpdateRejected(List<DocumentSnapshot> rejectedReferrals, RecyclerView recyclerView) {
        // Sort the list based on the "date" field
        Collections.sort(rejectedReferrals, (doc1, doc2) -> {
            String date1 = doc1.getString("date");
            String date2 = doc2.getString("date");

            return date2.compareTo(date1); // Descending order
        });

        // Update the RecyclerView Adapter
        RejectedReferralsAdapter adapter = new RejectedReferralsAdapter(rejectedReferrals);
        recyclerView.setAdapter(adapter);
    }


}