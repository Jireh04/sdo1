package com.finals.lagunauniversitysdo;
import android.graphics.Typeface;
import android.os.Bundle;
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

        // Initialize buttons
        Button btnPending = view.findViewById(R.id.btn_pending);
        Button btnAccepted = view.findViewById(R.id.btn_accepted);
        Button btnRejected = view.findViewById(R.id.btn_rejected);

// Define the colors
        int colorGreen = getResources().getColor(R.color.green);  // Assuming you have a green color in colors.xml
        int colorGray = getResources().getColor(R.color.light_grey);    // Assuming you have a gray color in colors.xml

// Set click listeners for each button
        btnPending.setOnClickListener(v -> {
            onPendingClick();

            // Change button colors
            btnPending.setBackgroundColor(colorGreen);  // Set clicked button to green
            btnAccepted.setBackgroundColor(colorGray);  // Set other buttons to gray
            btnRejected.setBackgroundColor(colorGray);
        });

        btnAccepted.setOnClickListener(v -> {
            onAcceptedClick();

            // Change button colors
            btnPending.setBackgroundColor(colorGray);   // Set other buttons to gray
            btnAccepted.setBackgroundColor(colorGreen); // Set clicked button to green
            btnRejected.setBackgroundColor(colorGray);
        });

        btnRejected.setOnClickListener(v -> {
            onRejectedClick();

            // Change button colors
            btnPending.setBackgroundColor(colorGray);   // Set other buttons to gray
            btnAccepted.setBackgroundColor(colorGray);  // Set other buttons to gray
            btnRejected.setBackgroundColor(colorGreen); // Set clicked button to green
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
        headerReferrer.setTextSize(16);
        headerReferrer.setPadding(16, 8, 16, 8);
        headerRow.addView(headerReferrer);

        TextView headerDate = new TextView(getActivity());
        headerDate.setText("Date");
        headerDate.setTextSize(16);
        headerDate.setPadding(16, 8, 16, 8);
        headerRow.addView(headerDate);

        TextView headerActions = new TextView(getActivity());
        headerActions.setText("Actions");
        headerActions.setTextSize(16);
        headerActions.setPadding(16, 8, 16, 8);
        headerRow.addView(headerActions);

        // Add the header row to the table layout
        tableLayout.addView(headerRow);

        // Query both Firestore collections for "pending" status
        db.collection("student_refferal_history")
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                // Get the data from Firestore (as a Map)
                                Map<String, Object> referralData = document.getData();
                                if (referralData != null) {
                                    String studentReferrer = (String) referralData.get("student_referrer");
                                    String studentName = (String) referralData.get("student_name");
                                    String studentId = (String) referralData.get("student_id");
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
                                                    updateReferralStatus(document.getId(), "accepted");
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
                                                    updateReferralStatus(document.getId(), "rejected");
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

        // Add the table layout to the HorizontalScrollView
        horizontalScrollView.addView(tableLayout);

        // Add the HorizontalScrollView to the pending section
        linearLayoutPending.addView(horizontalScrollView);

        // Repeat the same logic for personnel_refferal_history collection
        db.collection("personnel_refferal_history")
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Map<String, Object> referralData = document.getData();
                                if (referralData != null) {
                                    String personnelReferrer = (String) referralData.get("personnel_referrer");
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
                                                    updateReferralStatus(document.getId(), "accepted");
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
                                                    updateReferralStatus(document.getId(), "rejected");
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
    private void updateReferralStatus(String documentId, String newStatus) {
        // Update the student referral history
        db.collection("student_refferal_history").document(documentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Status updated successfully, reload the pending list
                    onPendingClick(); // Refresh the pending referrals
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });

        // Update the personnel referral history (if applicable)
        db.collection("personnel_refferal_history").document(documentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Status updated successfully, reload the pending list
                    onPendingClick(); // Refresh the pending referrals
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }


    // Fetch accepted referrals from Firestore
    public void onAcceptedClick() {
        // Get the TableLayout and ScrollView for displaying accepted referrals
        TableLayout tableLayoutAccepted = new TableLayout(getContext());
        tableLayoutAccepted.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        ));

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        horizontalScrollView.setLayoutParams(new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.MATCH_PARENT,
                HorizontalScrollView.LayoutParams.WRAP_CONTENT
        ));

        // Create a new LinearLayout for accepted referrals
        LinearLayout linearLayoutAccepted = getView().findViewById(R.id.linear_layout_accepted);
        linearLayoutAccepted.removeAllViews();  // Clear previous views

        // Get views for pending and accepted
        LinearLayout linearLayoutPending = getView().findViewById(R.id.linear_layout_pending);
        ScrollView scrollViewPending = getView().findViewById(R.id.scroll_view_pending);
        LinearLayout linearLayoutRejected = getView().findViewById(R.id.linear_layout_rejected);
        ScrollView scrollViewRejected = getView().findViewById(R.id.scroll_view_rejected);

        // Clear other sections
        linearLayoutPending.removeAllViews();
        scrollViewPending.setVisibility(View.GONE);
        linearLayoutRejected.removeAllViews();
        scrollViewRejected.setVisibility(View.GONE);

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

        // Add headers to the row
        headerRow.addView(headerStudent);
        headerRow.addView(headerDate);
        headerRow.addView(headerStatus);

        // Add header row to the table
        tableLayoutAccepted.addView(headerRow);

        // Show the HorizontalScrollView for accepted
        linearLayoutAccepted.addView(horizontalScrollView);
        horizontalScrollView.addView(tableLayoutAccepted);

        // Query Firestore for accepted referrals
        db.collection("student_refferal_history")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                // Get the data from Firestore (as a Map)
                                Map<String, Object> referralData = document.getData();
                                if (referralData != null) {
                                    String studentName = (String) referralData.get("student_name");
                                    String referralDate = (String) referralData.get("referral_date");

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
                                    tableLayoutAccepted.addView(row);
                                }
                            }
                        }
                    }
                });

        // Fetch from personnel_refferal_history if needed
        db.collection("personnel_refferal_history")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                // Get the data from Firestore (as a Map)
                                Map<String, Object> referralData = document.getData();
                                if (referralData != null) {
                                    String studentName = (String) referralData.get("student_name");
                                    String referralDate = (String) referralData.get("date");

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
                                    tableLayoutAccepted.addView(row);
                                }
                            }
                        }
                    }
                });
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
        TableRow headerRow = new TableRow(getActivity());
        TextView headerStudent = new TextView(getActivity());
        headerStudent.setText("Student Name");
        headerStudent.setTextSize(16);
        headerStudent.setPadding(16, 8, 16, 8);
        headerStudent.setTypeface(null, Typeface.BOLD);
        headerRow.addView(headerStudent);

        TextView headerDate = new TextView(getActivity());
        headerDate.setText("Referral Date");
        headerDate.setTextSize(16);
        headerDate.setPadding(14, 8, 14, 8);
        headerDate.setTypeface(null, Typeface.BOLD);
        headerRow.addView(headerDate);

        TextView headerStatus = new TextView(getActivity());
        headerStatus.setText("Status");
        headerStatus.setTextSize(16);
        headerStatus.setPadding(16, 8, 16, 8);
        headerStatus.setTypeface(null, Typeface.BOLD);
        headerRow.addView(headerStatus);

        // Add the header row to the table
        tableLayout.addView(headerRow);

        // Query both Firestore collections for "rejected" status
        db.collection("student_refferal_history")
                .whereEqualTo("status", "rejected")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                // Get the data from Firestore (as a Map)
                                Map<String, Object> referralData = document.getData();
                                if (referralData != null) {
                                    String studentName = (String) referralData.get("student_name");
                                    String referralDate = (String) referralData.get("referral_date");

                                    // Create a TableRow for each rejected referral
                                    TableRow row = new TableRow(getActivity());
                                    row.setLayoutParams(new TableRow.LayoutParams(
                                            TableRow.LayoutParams.MATCH_PARENT,
                                            TableRow.LayoutParams.WRAP_CONTENT));

                                    TextView studentTextView = new TextView(getActivity());
                                    studentTextView.setText(studentName);
                                    studentTextView.setPadding(16, 8, 16, 8);
                                    row.addView(studentTextView);

                                    TextView dateTextView = new TextView(getActivity());
                                    dateTextView.setText(referralDate);
                                    dateTextView.setPadding(14, 8, 14, 8);
                                    row.addView(dateTextView);

                                    TextView statusTextView = new TextView(getActivity());
                                    statusTextView.setText("Rejected");
                                    statusTextView.setPadding(16, 8, 16, 8);
                                    row.addView(statusTextView);

                                    // Add the row to the table
                                    tableLayout.addView(row);
                                }
                            }
                        }
                    }
                });

        db.collection("personnel_refferal_history")
                .whereEqualTo("status", "rejected")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                // Get the data from Firestore (as a Map)
                                Map<String, Object> referralData = document.getData();
                                if (referralData != null) {
                                    String studentName = (String) referralData.get("student_name");
                                    String referralDate = (String) referralData.get("date");

                                    // Create a TableRow for each rejected referral
                                    TableRow row = new TableRow(getActivity());
                                    row.setLayoutParams(new TableRow.LayoutParams(
                                            TableRow.LayoutParams.MATCH_PARENT,
                                            TableRow.LayoutParams.WRAP_CONTENT));

                                    TextView studentTextView = new TextView(getActivity());
                                    studentTextView.setText(studentName);
                                    studentTextView.setPadding(16, 8, 16, 8);
                                    row.addView(studentTextView);

                                    TextView dateTextView = new TextView(getActivity());
                                    dateTextView.setText(referralDate);
                                    dateTextView.setPadding(14, 8, 14, 8);
                                    row.addView(dateTextView);

                                    TextView statusTextView = new TextView(getActivity());
                                    statusTextView.setText("Rejected");
                                    statusTextView.setPadding(16, 8, 16, 8);
                                    row.addView(statusTextView);

                                    // Add the row to the table
                                    tableLayout.addView(row);
                                }
                            }
                        }
                    }
                });

        // Add the TableLayout to the HorizontalScrollView
        horizontalScrollView.addView(tableLayout);

        // Add the HorizontalScrollView to the LinearLayout for rejected referrals
        linearLayoutRejected.addView(horizontalScrollView);
    }


}