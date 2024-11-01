package com.finals.lagunauniversitysdo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.pdf.PdfDocument;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.media.MediaScannerConnection;
import android.net.Uri;

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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;


import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
        View view = inflater.inflate(R.layout.fragment_refferals_prefect, container, false);

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

        // Initialize the referral views
        ScrollView scrollViewPending = view.findViewById(R.id.scroll_view_pending);
        ScrollView scrollViewAccepted = view.findViewById(R.id.scroll_view_accepted);
        ScrollView scrollViewRejected = view.findViewById(R.id.scroll_view_rejected);

        // Define the colors
        int colorBlack = getResources().getColor(R.color.black);  // Assuming you have a green color in colors.xml
        int colorWhite = getResources().getColor(R.color.white); // Assuming you have a gray color in colors.xml

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
                tab.getCustomView().setSelected(false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        return view;
    }


    // Fetch pending referrals from Firestore
    public void onPendingClick() {

        // Get the LinearLayout and ScrollView for displaying pending referrals
        LinearLayout linearLayoutPending = getView().findViewById(R.id.linear_layout_pending);
        ScrollView scrollViewPending = getView().findViewById(R.id.scroll_view_pending);

        // Get views for accepted and rejected
        LinearLayout linearLayoutAccepted = getView().findViewById(R.id.linear_layout_accepted);
        ScrollView scrollViewAccepted = getView().findViewById(R.id.scroll_view_accepted);
        LinearLayout linearLayoutRejected = getView().findViewById(R.id.linear_layout_rejected);
        ScrollView scrollViewRejected = getView().findViewById(R.id.scroll_view_rejected);

        // Clear other sections
        linearLayoutAccepted.removeAllViews();
        scrollViewAccepted.setVisibility(View.GONE);
        linearLayoutRejected.removeAllViews();
        scrollViewRejected.setVisibility(View.GONE);

        // Show the ScrollView for pending
        scrollViewPending.setVisibility(View.VISIBLE);
        linearLayoutPending.removeAllViews();

        // Create a HorizontalScrollView to make the TableLayout horizontally scrollable
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        horizontalScrollView.setLayoutParams(new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.MATCH_PARENT,
                HorizontalScrollView.LayoutParams.WRAP_CONTENT
        ));

        // Create a TableLayout for organizing referrals into a table
        TableLayout tableLayout = new TableLayout(getActivity());
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        ));
        tableLayout.setStretchAllColumns(true);

        // Add table header row
        TableRow headerRow = new TableRow(getActivity());
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Create TextViews for table headers
        TextView headerReferrer = new TextView(getActivity());
        headerReferrer.setText("Referrer");
        headerReferrer.setTypeface(null, Typeface.BOLD);
        headerReferrer.setTextSize(16);
        headerReferrer.setPadding(16, 8, 16, 8);
        headerRow.addView(headerReferrer);

        TextView headerDate = new TextView(getActivity());
        headerDate.setText("Date");
        headerDate.setTypeface(null, Typeface.BOLD);
        headerDate.setTextSize(16);
        headerDate.setPadding(16, 8, 16, 8);
        headerRow.addView(headerDate);

        TextView headerActions = new TextView(getActivity());
        headerActions.setText("Actions");
        headerActions.setGravity(Gravity.CENTER);
        headerActions.setTypeface(null, Typeface.BOLD);
        headerActions.setTextSize(16);
        headerActions.setPadding(16, 8, 16, 8);
        headerRow.addView(headerActions);

        // Add the header row to the table layout
        tableLayout.addView(headerRow);

        // Add a divider after the header row
        View headerDivider = new View(getActivity());
        headerDivider.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                2 // Height of the divider
        ));
        headerDivider.setBackgroundColor(Color.GRAY); // Color of the divider
        tableLayout.addView(headerDivider);

        // Query both Firestore collections for "pending" status
        db.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot studentDocument : task.getResult().getDocuments()) {
                    // Get all referral history documents
                    db.collection("students").document(studentDocument.getId())
                            .collection("student_refferal_history")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        // Check status in the document
                                        if ("pending".equals(referralDocument.getString("status"))) {
                                            // Get the data from Firestore (as a Map)
                                            Map<String, Object> referralData = referralDocument.getData();
                                            if (referralData != null) {
                                                String studentReferrer = (String) referralData.get("student_referrer");
                                                String studentName = (String) referralData.get("student_name");
                                                String studentId = (String) referralData.get("refferer_id");
                                                String studentProgram = (String) referralData.get("student_program");
                                                String referralDate = (String) referralData.get("date");

                                                // Create a TableRow for each referral
                                                TableRow tableRow = new TableRow(getActivity());
                                                tableRow.setLayoutParams(new TableRow.LayoutParams(
                                                        TableRow.LayoutParams.MATCH_PARENT,
                                                        TableRow.LayoutParams.WRAP_CONTENT
                                                ));

                                                // Create a TextView for the referrer
                                                TextView referrerTextView = new TextView(getActivity());
                                                referrerTextView.setText(studentReferrer);
                                                referrerTextView.setPadding(16, 8, 16, 8);
                                                referrerTextView.setTextSize(14);
                                                tableRow.addView(referrerTextView);

                                                // Create a TextView for the referral date
                                                TextView dateTextView = new TextView(getActivity());
                                                dateTextView.setText(referralDate);
                                                dateTextView.setPadding(16, 8, 16, 8);
                                                dateTextView.setTextSize(14);
                                                tableRow.addView(dateTextView);

                                                // Create a LinearLayout to hold buttons (View, Accept, Reject)
                                                LinearLayout buttonLayout = new LinearLayout(getActivity());
                                                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

                                                // Create View button
                                                Button btnView = new Button(getActivity());
                                                btnView.setText("View");
                                                btnView.setOnClickListener(v -> {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setTitle("Choose an option")
                                                            .setMessage("Do you want to download or open the PDF?")
                                                            .setPositiveButton("Download", (dialog, which) -> {
                                                                downloadPdf(referralData, studentName, studentId, studentProgram, referralDate);
                                                            })
                                                            .setNegativeButton("Open", (dialog, which) -> {
                                                                generatePdf(referralData, studentName, studentId, studentProgram, referralDate);
                                                            })
                                                            .setCancelable(true)
                                                            .show();
                                                });
                                                buttonLayout.addView(btnView);

                                                // Create Accept button with confirmation dialog
                                                Button btnAccept = new Button(getActivity());
                                                btnAccept.setText("Accept");
                                                btnAccept.setOnClickListener(v -> {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setTitle("Confirmation")
                                                            .setMessage("Are you sure you want to accept this referral?")
                                                            .setPositiveButton("Yes", (dialog, which) -> {
                                                                updateReferralStatusStudent(studentId, referralDocument.getId(), "accepted");
                                                                Log.d("FirestoreSuccess", "Student referral status updated successfully");
                                                            })
                                                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                                            .show();
                                                });
                                                buttonLayout.addView(btnAccept);

                                                // Create Reject button with confirmation dialog
                                                Button btnReject = new Button(getActivity());
                                                btnReject.setText("Reject");
                                                btnReject.setOnClickListener(v -> {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setTitle("Confirmation")
                                                            .setMessage("Are you sure you want to reject this referral?")
                                                            .setPositiveButton("Yes", (dialog, which) -> {
                                                                updateReferralStatusStudent(studentId, referralDocument.getId(), "rejected");
                                                            })
                                                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                                            .show();
                                                });
                                                buttonLayout.addView(btnReject);

                                                // Add the button layout to the table row
                                                tableRow.addView(buttonLayout);

                                                // Add the table row to the table layout
                                                tableLayout.addView(tableRow);
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        });

        horizontalScrollView.addView(tableLayout);
        linearLayoutPending.addView(horizontalScrollView);

        // Repeat the same logic for personnel_refferal_history collection
        db.collection("personnel").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot personnelDocument : task.getResult().getDocuments()) {
                    // Get all referral history documents
                    db.collection("personnel").document(personnelDocument.getId())
                            .collection("personnel_refferal_history")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        // Check status in the document
                                        if ("pending".equals(referralDocument.getString("status"))) {
                                            Map<String, Object> referralData = referralDocument.getData();
                                            if (referralData != null) {
                                                String personnelReferrer = (String) referralData.get("personnel_referrer");
                                                String personnelID = (String) referralData.get("personnel_id");
                                                String studentName = (String) referralData.get("student_name");
                                                String studentId = (String) referralData.get("student_id");
                                                String studentProgram = (String) referralData.get("student_program");
                                                String referralDate = (String) referralData.get("date");

                                                TableRow tableRow = new TableRow(getActivity());
                                                tableRow.setLayoutParams(new TableRow.LayoutParams(
                                                        TableRow.LayoutParams.MATCH_PARENT,
                                                        TableRow.LayoutParams.WRAP_CONTENT
                                                ));

                                                TextView referrerTextView = new TextView(getActivity());
                                                referrerTextView.setText(personnelReferrer);
                                                referrerTextView.setPadding(16, 8, 16, 8);
                                                referrerTextView.setTextSize(14);
                                                tableRow.addView(referrerTextView);

                                                TextView dateTextView = new TextView(getActivity());
                                                dateTextView.setText(referralDate);
                                                dateTextView.setPadding(16, 8, 16, 8);
                                                dateTextView.setTextSize(14);
                                                tableRow.addView(dateTextView);

                                                LinearLayout buttonLayout = new LinearLayout(getActivity());
                                                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

                                                Button btnView = new Button(getActivity());
                                                btnView.setText("View");
                                                btnView.setOnClickListener(v -> {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setTitle("Choose an option")
                                                            .setMessage("Do you want to download or open the PDF?")
                                                            .setPositiveButton("Download", (dialog, which) -> {
                                                                downloadPdf(referralData, studentName, studentId, studentProgram, referralDate);
                                                            })
                                                            .setNegativeButton("Open", (dialog, which) -> {
                                                                generatePdf(referralData, studentName, studentId, studentProgram, referralDate);
                                                            })
                                                            .setCancelable(true)
                                                            .show();
                                                });
                                                buttonLayout.addView(btnView);

                                                Button btnAccept = new Button(getActivity());
                                                btnAccept.setText("Accept");
                                                btnAccept.setOnClickListener(v -> {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setTitle("Confirmation")
                                                            .setMessage("Are you sure you want to accept this referral?")
                                                            .setPositiveButton("Yes", (dialog, which) -> {
                                                                updateReferralStatusPersonnel(referralDocument.getId(), personnelID, "accepted");
                                                            })
                                                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                                            .show();
                                                });
                                                buttonLayout.addView(btnAccept);

                                                Button btnReject = new Button(getActivity());
                                                btnReject.setText("Reject");
                                                btnReject.setOnClickListener(v -> {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setTitle("Confirmation")
                                                            .setMessage("Are you sure you want to reject this referral?")
                                                            .setPositiveButton("Yes", (dialog, which) -> {
                                                                updateReferralStatusPersonnel(referralDocument.getId(), personnelID, "rejected");
                                                            })
                                                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                                            .show();
                                                });
                                                buttonLayout.addView(btnReject);

                                                tableRow.addView(buttonLayout);
                                                tableLayout.addView(tableRow);
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }



    // Define a request code for permission requests
    private static final int REQUEST_PERMISSION = 1;

    public void generatePdf(Map<String, Object> referralData, String studentName, String studentId, String studentProgram, String referralDate) {
        // Check if data is valid
        if (studentName == null || studentId == null || referralDate == null) {
            Toast.makeText(getActivity(), "Invalid data for PDF generation", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new PdfDocument
        PdfDocument document = new PdfDocument();

        // Create a page description (A4 size - 595x842)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        // Get the Canvas object for drawing content
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);

        // Start drawing text
        int yPosition = 100;  // Start position for drawing text

        canvas.drawText("Referral Details", 220, yPosition, paint);
        yPosition += 20; // Move to the next line

        canvas.drawText("Student Name: " + studentName, 50, yPosition, paint);
        yPosition += 20;

        canvas.drawText("Student ID: " + studentId, 50, yPosition, paint);
        yPosition += 20;

        canvas.drawText("Program: " + studentProgram, 50, yPosition, paint);
        yPosition += 20;

        canvas.drawText("Referral Date: " + referralDate, 50, yPosition, paint);
        yPosition += 20;

        // Add referrer details (Ensure this is not null)
        String referrerName = (String) referralData.get("student_referrer");
        if (referrerName != null) {
            canvas.drawText("Referrer: " + referrerName, 50, yPosition, paint);
        }

        // Finish the page
        document.finishPage(page);

        // Define the directory and file for saving the PDF in internal storage
        File directory = getActivity().getFilesDir();  // Save in app's private storage
        File file = new File(directory, studentName + "_referral.pdf");

        try {
            // Ensure the directory exists
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Write the PDF document to the file
            FileOutputStream outputStream = new FileOutputStream(file);
            document.writeTo(outputStream);
            outputStream.close();  // Ensure the stream is properly closed
            Toast.makeText(getActivity(), "PDF generated", Toast.LENGTH_SHORT).show();

            // Open the PDF using Intent
            openPdf(file);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Close the document
            document.close();
        }
    }
    public void downloadPdf(Map<String, Object> referralData, String studentName, String studentId, String studentProgram, String referralDate) {
        // Check if data is valid
        if (studentName == null || studentId == null || referralDate == null) {
            Toast.makeText(getActivity(), "Invalid data for PDF generation", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new PdfDocument
        PdfDocument document = new PdfDocument();

        // Create a page description (A4 size - 595x842)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        // Get the Canvas object for drawing content
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);

        // Start drawing text
        int yPosition = 100;  // Start position for drawing text

        canvas.drawText("Referral Details", 220, yPosition, paint);
        yPosition += 20; // Move to the next line

        canvas.drawText("Student Name: " + studentName, 50, yPosition, paint);
        yPosition += 20;

        canvas.drawText("Student ID: " + studentId, 50, yPosition, paint);
        yPosition += 20;

        canvas.drawText("Program: " + studentProgram, 50, yPosition, paint);
        yPosition += 20;

        canvas.drawText("Referral Date: " + referralDate, 50, yPosition, paint);
        yPosition += 20;

        // Add referrer details (Ensure this is not null)
        String referrerName = (String) referralData.get("student_referrer");
        if (referrerName != null) {
            canvas.drawText("Referrer: " + referrerName, 50, yPosition, paint);
        }

        // Finish the page
        document.finishPage(page);

        // Define the directory and file for saving the PDF in the Downloads folder
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Save to Downloads folder
        File file = new File(downloadsDirectory, studentName + "_referral.pdf");

        try {
            // Ensure the directory exists
            if (!downloadsDirectory.exists()) {
                downloadsDirectory.mkdirs();
            }

            // Write the PDF document to the file
            FileOutputStream outputStream = new FileOutputStream(file);
            document.writeTo(outputStream);
            outputStream.close();  // Ensure the stream is properly closed
            Toast.makeText(getActivity(), "PDF downloaded to Downloads folder", Toast.LENGTH_SHORT).show();

            // Optionally, you can notify the system to refresh Downloads folder or open the file
            // Notify the system to scan the file so it shows up in file managers
            MediaScannerConnection.scanFile(getActivity(), new String[]{file.getAbsolutePath()}, null, null);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Close the document
            document.close();
        }
    }



    public void openPdf(File file) {
        // Create an Intent to open the PDF file
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Get the URI for the file using FileProvider
        Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);

        // Set flags to allow read access to the file
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Set MIME type to ensure it's recognized as a PDF file
        intent.setDataAndType(uri, "application/pdf");

        // Launch the intent to open the PDF
        startActivity(intent);
    }


    // Method to update referral status in Firestore
    // Make sure db is initialized at the top of the fragment or activity

    private void updateReferralStatusPersonnel(String referralDocumentId, String personnelId, String newStatus) {
        if (personnelId == null || referralDocumentId == null) {
            Log.e("UpdateError", "Personnel ID or Referral Document ID is null. Cannot update status.");
            return; // Exit if IDs are invalid
        }

        Log.d("UpdateStatus", "Updating personnel status. ID: " + personnelId + ", Referral ID: " + referralDocumentId + ", New Status: " + newStatus);

        db.collection("personnel").document(personnelId)
                .collection("personnel_refferal_history").document(referralDocumentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreSuccess", "Personnel referral status updated successfully");
                    triggerUIRefresh(); // Custom method to indicate UI needs updating
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error updating personnel referral status", e);
                });
    }

    private void updateReferralStatusStudent(String studentId, String referralDocumentId, String newStatus) {
        if (studentId == null || referralDocumentId == null) {
            Log.e("UpdateError", "Student ID or Referral Document ID is null. Cannot update status.");
            return; // Exit if IDs are invalid
        }

        Log.d("UpdateStatus", "Updating student status. ID: " + studentId + ", Referral ID: " + referralDocumentId + ", New Status: " + newStatus);

        db.collection("students").document(studentId)
                .collection("student_refferal_history").document(referralDocumentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreSuccess", "Student referral status updated successfully");
                    triggerUIRefresh(); // Custom method to indicate UI needs updating
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error updating student referral status", e);
                });
    }

    // New method to refresh UI safely
    private void triggerUIRefresh() {
        if (getActivity() == null || getView() == null) {
            Log.e("UIError", "Activity or view is null, cannot refresh UI.");
            return;
        }

        getActivity().runOnUiThread(() -> {
            try {
                onPendingClick(); // Safely update UI within UI thread
            } catch (Exception e) {
                Log.e("UIError", "Error refreshing UI", e);
            }
        });
    }


    public void onAcceptedClick() {
        // Get views for pending and rejected
        LinearLayout linearLayoutPending = getView().findViewById(R.id.linear_layout_pending);
        ScrollView scrollViewPending = getView().findViewById(R.id.scroll_view_pending);

        LinearLayout linearLayoutAccepted = getView().findViewById(R.id.linear_layout_accepted);
        ScrollView scrollViewAccepted = getView().findViewById(R.id.scroll_view_accepted);
        LinearLayout linearLayoutRejected = getView().findViewById(R.id.linear_layout_rejected);
        ScrollView scrollViewRejected = getView().findViewById(R.id.scroll_view_rejected);

        // Clear other sections
        linearLayoutPending.removeAllViews();
        scrollViewPending.setVisibility(View.GONE);
        linearLayoutRejected.removeAllViews();
        scrollViewRejected.setVisibility(View.GONE);

        // Clear the LinearLayout to remove previous data
        linearLayoutAccepted.removeAllViews();
        scrollViewAccepted.setVisibility(View.VISIBLE);

        // Create a new TableLayout for accepted referrals
        TableLayout tableLayoutAccepted = new TableLayout(getContext());
        tableLayoutAccepted.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create a new HorizontalScrollView for displaying the TableLayout
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        horizontalScrollView.setLayoutParams(new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.MATCH_PARENT,
                HorizontalScrollView.LayoutParams.WRAP_CONTENT
        ));
        horizontalScrollView.addView(tableLayoutAccepted);

        // Add the horizontalScrollView to linearLayoutAccepted, ensuring no parent is set
        if (horizontalScrollView.getParent() != null) {
            ((ViewGroup) horizontalScrollView.getParent()).removeView(horizontalScrollView);
        }
        linearLayoutAccepted.addView(horizontalScrollView);

        // Add headers for the TableLayout
        TableRow headerRow = new TableRow(getContext());
        headerRow.setPadding(16, 16, 16, 16);

        TextView headerStudent = new TextView(getContext());
        headerStudent.setText("Student Name");
        headerStudent.setTextSize(18);
        headerStudent.setTypeface(null, Typeface.BOLD);
        headerStudent.setPadding(16, 8, 16, 8);

        TextView headerDate = new TextView(getContext());
        headerDate.setText("Referral Date");
        headerDate.setTextSize(18);
        headerDate.setTypeface(null, Typeface.BOLD);
        headerDate.setPadding(14, 8, 12, 8);

        TextView headerStatus = new TextView(getContext());
        headerStatus.setText("Status");
        headerStatus.setTextSize(18);
        headerStatus.setTypeface(null, Typeface.BOLD);
        headerStatus.setPadding(12, 8, 16, 8);

        headerRow.addView(headerStudent);
        headerRow.addView(headerDate);
        headerRow.addView(headerStatus);
        tableLayoutAccepted.addView(headerRow);

        // Divider after the header row
        View headerDivider = new View(getActivity());
        headerDivider.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                2
        ));
        headerDivider.setBackgroundColor(Color.GRAY);
        tableLayoutAccepted.addView(headerDivider);

        // Fetch accepted referrals from Firestore (students)
        db.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot studentDocument : task.getResult().getDocuments()) {
                    // Get all referral history documents
                    db.collection("students").document(studentDocument.getId())
                            .collection("student_refferal_history")
                            .whereEqualTo("status", "accepted")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        addReferralRow(tableLayoutAccepted, referralDocument);
                                    }
                                }
                            });
                }
            }
        });

        // Fetch accepted referrals from personnel_refferal_history (personnel)
        db.collection("personnel").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot personnelDocument : task.getResult().getDocuments()) {
                    // Get all referral history documents
                    db.collection("personnel").document(personnelDocument.getId())
                            .collection("personnel_refferal_history")
                            .whereEqualTo("status", "accepted")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        addReferralRow(tableLayoutAccepted, referralDocument);
                                    }
                                }
                            });
                }
            }
        });
    }

    // Helper function to add a row to the table for each referral
    private void addReferralRow(TableLayout tableLayout, DocumentSnapshot referralDocument) {
        String studentName = referralDocument.getString("student_name");
        String referralDate = referralDocument.getString("date");

        // Create a new table row
        TableRow row = new TableRow(getContext());
        row.setPadding(16, 8, 16, 8);

        // Create and add TextViews for each column in the row
        TextView textStudentName = new TextView(getActivity());
        textStudentName.setText(studentName);
        textStudentName.setPadding(16, 8, 16, 8);

        TextView textReferralDate = new TextView(getActivity());
        textReferralDate.setText(referralDate);
        textReferralDate.setPadding(14, 8, 12, 8);

        TextView textStatus = new TextView(getActivity());
        textStatus.setText("Accepted");
        textStatus.setPadding(12, 8, 16, 8);

        // Add the TextViews to the row
        row.addView(textStudentName);
        row.addView(textReferralDate);
        row.addView(textStatus);

        // Add the row to the table
        tableLayout.addView(row);
    }




    // Fetch rejected referrals from Firestore
    public void onRejectedClick() {
        // Get the LinearLayout and ScrollView for displaying rejected referrals
        LinearLayout linearLayoutRejected = getView().findViewById(R.id.linear_layout_rejected);
        ScrollView scrollViewRejected = getView().findViewById(R.id.scroll_view_rejected);

        // Get views for pending and accepted
        LinearLayout linearLayoutPending = getView().findViewById(R.id.linear_layout_pending);
        ScrollView scrollViewPending = getView().findViewById(R.id.scroll_view_pending);
        LinearLayout linearLayoutAccepted = getView().findViewById(R.id.linear_layout_accepted);
        ScrollView scrollViewAccepted = getView().findViewById(R.id.scroll_view_accepted);

        // Clear other sections
        linearLayoutPending.removeAllViews();
        scrollViewPending.setVisibility(View.GONE);
        linearLayoutAccepted.removeAllViews();
        scrollViewAccepted.setVisibility(View.GONE);

        // Show the ScrollView for rejected
        scrollViewRejected.setVisibility(View.VISIBLE);
        linearLayoutRejected.removeAllViews();

        // Create a HorizontalScrollView for the TableLayout
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        horizontalScrollView.setLayoutParams(new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.MATCH_PARENT,
                HorizontalScrollView.LayoutParams.WRAP_CONTENT
        ));

        // Create a TableLayout for rejected referrals
        TableLayout tableLayout = new TableLayout(getActivity());
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        tableLayout.setStretchAllColumns(true);

        // Add TableHeader
        TableRow headerRow = createTableRow("Student Name", "Referral Date", "Status");
        tableLayout.addView(headerRow);

        // Add a divider after the header row
        addDivider(tableLayout);

        // Query Firestore for "rejected" status
        fetchRejectedReferrals(tableLayout);

        // Add the TableLayout to the HorizontalScrollView
        horizontalScrollView.addView(tableLayout);

        // Add the HorizontalScrollView to the LinearLayout for rejected referrals
        linearLayoutRejected.addView(horizontalScrollView);
    }

    private void fetchRejectedReferrals(TableLayout tableLayout) {
        // Fetch rejected referrals for students
        db.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot studentDocument : task.getResult().getDocuments()) {
                    db.collection("students").document(studentDocument.getId())
                            .collection("student_refferal_history")
                            .whereEqualTo("status", "rejected")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        addReferralRow(tableLayout, referralDocument, "Rejected");
                                    }
                                }
                            });
                }
            }
        });

        // Fetch rejected referrals for personnel
        db.collection("personnel").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot personnelDocument : task.getResult().getDocuments()) {
                    db.collection("personnel").document(personnelDocument.getId())
                            .collection("personnel_refferal_history")
                            .whereEqualTo("status", "rejected")
                            .get()
                            .addOnCompleteListener(subTask -> {
                                if (subTask.isSuccessful()) {
                                    for (DocumentSnapshot referralDocument : subTask.getResult().getDocuments()) {
                                        addReferralRow(tableLayout, referralDocument, "Rejected");
                                    }
                                }
                            });
                }
            }
        });
    }

    private void addReferralRow(TableLayout tableLayout, DocumentSnapshot referralDocument, String status) {
        String studentName = referralDocument.getString("student_name");
        String referralDate = referralDocument.getString("date");

        // Create a TableRow for each rejected referral
        TableRow row = createTableRow(studentName, referralDate, status);
        tableLayout.addView(row);
        addDivider(tableLayout); // Add a divider after each row
    }

    private TableRow createTableRow(String studentName, String referralDate, String status) {
        TableRow row = new TableRow(getActivity());
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        row.addView(createTextView(studentName, 16, 16));
        row.addView(createTextView(referralDate, 16, 14));
        row.addView(createTextView(status, 16, 16));

        return row;
    }

    private TextView createTextView(String text, int textSize, int padding) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setPadding(padding, 8, padding, 8);
        return textView;
    }

    private void addDivider(TableLayout tableLayout) {
        View headerDivider = new View(getActivity());
        headerDivider.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                2 // Height of the divider
        ));
        headerDivider.setBackgroundColor(Color.GRAY); // Color of the divider
        tableLayout.addView(headerDivider);
    }

}