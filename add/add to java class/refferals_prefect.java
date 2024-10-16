package com.finals.lagunauniversitysdo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

        // Set click listeners for each button
        btnPending.setOnClickListener(v -> onPendingClick());
        btnAccepted.setOnClickListener(v -> onAcceptedClick());
        btnRejected.setOnClickListener(v -> onRejectedClick());

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
                                    String studentName = (String) referralData.get("student_name"); // Get student_name
                                    String studentId = (String) referralData.get("student_id"); // Get student_id
                                    String studentProgram = (String) referralData.get("student_program"); // Get student_program
                                    String referralDate = (String) referralData.get("date");

                                    // Create a container for each pending referral
                                    LinearLayout referralLayout = new LinearLayout(getActivity());
                                    referralLayout.setOrientation(LinearLayout.VERTICAL);

                                    // Create a TextView for student referrer
                                    TextView textView = new TextView(getActivity());
                                    textView.setText("Student Referrer: " + studentReferrer + "\nDate: " + referralDate);
                                    textView.setPadding(16, 8, 16, 8);
                                    textView.setTextSize(16);
                                    referralLayout.addView(textView);

                                    // Create another TextView for student details (hidden by default)
                                    TextView studentDetailsTextView = new TextView(getActivity());
                                    studentDetailsTextView.setText("Student Name: " + studentName + "\nID: " + studentId + "\nProgram: " + studentProgram);
                                    studentDetailsTextView.setVisibility(View.GONE); // Initially hidden
                                    referralLayout.addView(studentDetailsTextView);

                                    // Create a button ("v") to toggle the visibility of student details
                                    Button toggleButton = new Button(getActivity());
                                    toggleButton.setText("v");
                                    referralLayout.addView(toggleButton);

                                    // Toggle visibility of student details when button is clicked
                                    toggleButton.setOnClickListener(v -> {
                                        if (studentDetailsTextView.getVisibility() == View.GONE) {
                                            studentDetailsTextView.setVisibility(View.VISIBLE); // Show student details
                                        } else {
                                            studentDetailsTextView.setVisibility(View.GONE); // Hide student details
                                        }
                                    });

                                    // Create Accept and Reject buttons
                                    Button btnAccept = new Button(getActivity());
                                    btnAccept.setText("Accept");
                                    Button btnReject = new Button(getActivity());
                                    btnReject.setText("Reject");

                                    // Add buttons to the layout
                                    referralLayout.addView(btnAccept);
                                    referralLayout.addView(btnReject);

                                    // Add the entire layout to the pending section
                                    linearLayoutPending.addView(referralLayout);

                                    // Set onClick listeners for Accept and Reject buttons
                                    btnAccept.setOnClickListener(v -> updateReferralStatus(document.getId(), "accepted"));
                                    btnReject.setOnClickListener(v -> updateReferralStatus(document.getId(), "rejected"));
                                    // Inside onPendingClick(), after creating Accept and Reject buttons:
                                    Button btnView = new Button(getActivity());
                                    btnView.setText("View"); // Name of the button
                                    referralLayout.addView(btnView);

// Inside the onPendingClick() method, within the btnView.setOnClickListener
                                    btnView.setOnClickListener(v -> {
                                        // Show a dialog to ask if the user wants to download or open the PDF
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setTitle("Choose an option")
                                                .setMessage("Do you want to download or open the PDF?")
                                                .setPositiveButton("Download", (dialog, which) -> {
                                                    // If user selects "Download", generate and download the PDF
                                                    downloadPdf(referralData, studentName, studentId, studentProgram, referralDate);
                                                })
                                                .setNegativeButton("Open", (dialog, which) -> {
                                                    // If user selects "Open", just open the PDF
                                                    generatePdf(referralData, studentName, studentId, studentProgram, referralDate);
                                                })
                                                .setCancelable(true) // Optionally make the dialog cancellable
                                                .show();
                                    });


                                }
                            }
                        }
                    }
                });

        db.collection("personnel_refferal_history")
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
                                    String personnelReferrer = (String) referralData.get("personnel_referrer");
                                    String studentName = (String) referralData.get("student_name"); // Get student_name
                                    String studentId = (String) referralData.get("student_id"); // Get student_id
                                    String studentProgram = (String) referralData.get("student_program"); // Get student_program
                                    String referralDate = (String) referralData.get("date");

                                    // Create a container for each pending referral
                                    LinearLayout referralLayout = new LinearLayout(getActivity());
                                    referralLayout.setOrientation(LinearLayout.VERTICAL);

                                    // Create a TextView for personnel referrer
                                    TextView textView = new TextView(getActivity());
                                    textView.setText("Personnel Referrer: " + personnelReferrer + "\nDate: " + referralDate);
                                    textView.setPadding(16, 8, 16, 8);
                                    textView.setTextSize(16);
                                    referralLayout.addView(textView);

                                    // Create another TextView for student details (hidden by default)
                                    TextView studentDetailsTextView = new TextView(getActivity());
                                    studentDetailsTextView.setText("Student Name: " + studentName + "\nID: " + studentId + "\nProgram: " + studentProgram);
                                    studentDetailsTextView.setVisibility(View.GONE); // Initially hidden
                                    referralLayout.addView(studentDetailsTextView);

                                    // Create a button ("v") to toggle the visibility of student details
                                    Button toggleButton = new Button(getActivity());
                                    toggleButton.setText("v");
                                    referralLayout.addView(toggleButton);

                                    // Toggle visibility of student details when button is clicked
                                    toggleButton.setOnClickListener(v -> {
                                        if (studentDetailsTextView.getVisibility() == View.GONE) {
                                            studentDetailsTextView.setVisibility(View.VISIBLE); // Show student details
                                        } else {
                                            studentDetailsTextView.setVisibility(View.GONE); // Hide student details
                                        }
                                    });

                                    // Create Accept and Reject buttons
                                    Button btnAccept = new Button(getActivity());
                                    btnAccept.setText("Accept");
                                    Button btnReject = new Button(getActivity());
                                    btnReject.setText("Reject");

                                    // Add buttons to the layout
                                    referralLayout.addView(btnAccept);
                                    referralLayout.addView(btnReject);

                                    // Add the entire layout to the pending section
                                    linearLayoutPending.addView(referralLayout);

                                    // Set onClick listeners for Accept and Reject buttons
                                    btnAccept.setOnClickListener(v -> updateReferralStatus(document.getId(), "accepted"));
                                    btnReject.setOnClickListener(v -> updateReferralStatus(document.getId(), "rejected"));
                                    // Inside onPendingClick(), after creating Accept and Reject buttons:
                                    Button btnView = new Button(getActivity());
                                    btnView.setText("View"); // Name of the button
                                    referralLayout.addView(btnView);


                                    // Inside the onPendingClick() method, within the btnView.setOnClickListener
                                    btnView.setOnClickListener(v -> {
                                        // Show a dialog to ask if the user wants to download or open the PDF
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setTitle("Choose an option")
                                                .setMessage("Do you want to download or open the PDF?")
                                                .setPositiveButton("Download", (dialog, which) -> {
                                                    // If user selects "Download", generate and download the PDF
                                                    downloadPdf(referralData, studentName, studentId, studentProgram, referralDate);
                                                })
                                                .setNegativeButton("Open", (dialog, which) -> {
                                                    // If user selects "Open", just open the PDF
                                                    generatePdf(referralData, studentName, studentId, studentProgram, referralDate);
                                                })
                                                .setCancelable(true) // Optionally make the dialog cancellable
                                                .show();
                                    });



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
        // Get the LinearLayout and ScrollView for displaying accepted referrals
        LinearLayout linearLayoutAccepted = getView().findViewById(R.id.linear_layout_accepted);
        ScrollView scrollViewAccepted = getView().findViewById(R.id.scroll_view_accepted);

        // Get views for pending and rejected
        LinearLayout linearLayoutPending = getView().findViewById(R.id.linear_layout_pending);
        ScrollView scrollViewPending = getView().findViewById(R.id.scroll_view_pending);
        LinearLayout linearLayoutRejected = getView().findViewById(R.id.linear_layout_rejected);
        ScrollView scrollViewRejected = getView().findViewById(R.id.scroll_view_rejected);

        // Clear other sections
        linearLayoutPending.removeAllViews();
        scrollViewPending.setVisibility(View.GONE);
        linearLayoutRejected.removeAllViews();
        scrollViewRejected.setVisibility(View.GONE);

        // Show the ScrollView for accepted
        scrollViewAccepted.setVisibility(View.VISIBLE);
        linearLayoutAccepted.removeAllViews();

        // Query both Firestore collections for "accepted" status
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

                                    // Create a TextView for each accepted referral
                                    TextView textView = new TextView(getActivity());
                                    textView.setText("Student: " + studentName + "\nDate: " + referralDate + "\nStatus: Accepted");
                                    textView.setPadding(16, 8, 16, 8);
                                    textView.setTextSize(16);
                                    linearLayoutAccepted.addView(textView);
                                }
                            }
                        }
                    }
                });

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

                                    // Create a TextView for each accepted referral
                                    TextView textView = new TextView(getActivity());
                                    textView.setText("Student: " + studentName + "\nDate: " + referralDate + "\nStatus: Accepted");
                                    textView.setPadding(16, 8, 16, 8);
                                    textView.setTextSize(16);
                                    linearLayoutAccepted.addView(textView);
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

                                    // Create a TextView for each rejected referral
                                    TextView textView = new TextView(getActivity());
                                    textView.setText("Student: " + studentName + "\nDate: " + referralDate + "\nStatus: Rejected");
                                    textView.setPadding(16, 8, 16, 8);
                                    textView.setTextSize(16);
                                    linearLayoutRejected.addView(textView);
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

                                    // Create a TextView for each rejected referral
                                    TextView textView = new TextView(getActivity());
                                    textView.setText("Student: " + studentName + "\nDate: " + referralDate + "\nStatus: Rejected");
                                    textView.setPadding(16, 8, 16, 8);
                                    textView.setTextSize(16);
                                    linearLayoutRejected.addView(textView);
                                }
                            }
                        }
                    }
                });
    }
}

