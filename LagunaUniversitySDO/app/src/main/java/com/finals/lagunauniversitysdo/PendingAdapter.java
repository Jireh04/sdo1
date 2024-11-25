package com.finals.lagunauniversitysdo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.ReferralViewHolder> {

    private FirebaseFirestore db;

    private List<referral> referralList;
    private Context context;

    public PendingAdapter(List<referral> referralList, Context context) {
        this.referralList = referralList;
        this.context = context;
    }

    @NonNull
    @Override
    public ReferralViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.referral_item, parent, false);
        return new ReferralViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReferralViewHolder holder, int position) {
        referral referral = referralList.get(position);
        holder.referrerTextView.setText(referral.getReferrer());
        holder.dateTextView.setText(referral.getDate());
        holder.actionTextView.setText(referral.getAction());

        // Handle Button Clicks
        holder.viewButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);  // Use context here
            builder.setTitle("Choose an option")
                    .setMessage("Do you want to download or open the PDF?")
                    .setPositiveButton("Download", (dialog, which) -> {
                        downloadPdf(referral);
                    })
                    .setNegativeButton("Open", (dialog, which) -> {
                        generatePdf(referral);
                    })
                    .setCancelable(true)
                    .show();

            Toast.makeText(context, "Viewing: " + referral.getReferrer(), Toast.LENGTH_SHORT).show();
        });

        holder.acceptButton.setOnClickListener(v -> {
            // Handle "Accept" button click
            new AlertDialog.Builder(context)  // Use context here
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to accept this referral?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Update status in the student's referral document
                        updateReferralStatusStudent(referral.getReferrer(), referral.getDate(), "accepted");

                        // Create a map to hold all referral details
                        Map<String, Object> acceptedData = new HashMap<>();
                        acceptedData.put("date", referral.getDate());
                        acceptedData.put("referrer_id", referral.getReferrer());
                        acceptedData.put("remarks", referral.getAction()); // Example: "jaii"
                        acceptedData.put("status", "accepted");
                        acceptedData.put("student_id", referral.getReferrer()); // Example: "221-0896"
                        acceptedData.put("student_name", referral.getReferrer()); // Example: "RANIELLE ANTHONY JARAPLASAN LUISTRO"
                        acceptedData.put("student_program", referral.getAction()); // Example: "BSIT-SD"
                        acceptedData.put("student_referrer", referral.getReferrer()); // Example: "ABANTO KATHLEEN LIZETH DORIA"
                        acceptedData.put("term", referral.getAction()); // Example: "First Sem"
                        acceptedData.put("user_concern", referral.getAction()); // Example: "Discipline Concerns"
                        acceptedData.put("violation", referral.getAction()); // Example: "Major Offense"
                        acceptedData.put("offense", referral.getAction()); // Example: "Major Offense"
                        acceptedData.put("violation_status", "Unsettled"); // Add the new field "violation_status"

                        // Save the accepted referral data in the `accepted_status` sub-collection
                        db.collection("students").document(referral.getReferrer())
                                .collection("accepted_status")
                                .document(referral.getDate())  // Use the same document ID for consistency
                                .set(acceptedData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Referral accepted and saved.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to save accepted referral.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        holder.rejectButton.setOnClickListener(v -> {
            // Handle "Reject" button click
            final View rejectView = LayoutInflater.from(context).inflate(R.layout.dialog_reject_reason, null);
            EditText reasonEditText = rejectView.findViewById(R.id.editTextReason);

            // Create the dialog to show the rejection options
            new AlertDialog.Builder(context)  // Use context here
                    .setTitle("Rejection Reason")
                    .setMessage("Please provide a reason for rejecting this referral:")
                    .setView(rejectView) // Attach the custom view to the dialog
                    .setPositiveButton("Reject", (dialog, which) -> {
                        String rejectionReason = reasonEditText.getText().toString().trim();
                        if (!rejectionReason.isEmpty()) {
                            // Proceed to update the Firestore document with the rejection reason
                            updateReferralStatusStudent(referral.getReferrer(), referral.getDate(), "rejected");

                            // Optional: Show a Toast to indicate the referral was rejected successfully
                            Toast.makeText(context, "Referral has been rejected.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Please enter a rejection reason.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // Dismiss the dialog if Cancel is clicked
                    .show();
        });
    }

        @Override
    public int getItemCount() {
        return referralList.size();
    }


    // Define a request code for permission requests
    private static final int REQUEST_PERMISSION = 1;

    private void updateReferralStatusStudent(String referrer, String date, String status) {
        db.collection("students")
                .document(referrer)
                .collection("student_refferal_history")
                .document(date)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Referral status updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update referral status.", Toast.LENGTH_SHORT).show();
                });
    }

    public void generatePdf(Map<String, Object> referralData, String studentName, String studentId, String studentProgram, String referralDate) {
        // Check if data is valid
        if (studentName == null || studentId == null || referralDate == null) {
            Toast.makeText(getContext(), "Invalid data for PDF generation", Toast.LENGTH_SHORT).show();
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
        File directory = getContext().getFilesDir();  // Save in app's private storage
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
            Toast.makeText(getContext(), "PDF generated", Toast.LENGTH_SHORT).show();

            // Open the PDF using Intent
            openPdf(file);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Close the document
            document.close();
        }
    }

    public void downloadPdf(Map<String, Object> referralData, String studentName, String studentId, String studentProgram, String referralDate) {
        // Check if data is valid
        if (studentName == null || studentId == null || referralDate == null) {
            Toast.makeText(getContext(), "Invalid data for PDF generation", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), "PDF downloaded to Downloads folder", Toast.LENGTH_SHORT).show();

            // Optionally, you can notify the system to refresh Downloads folder or open the file
            // Notify the system to scan the file so it shows up in file managers
            MediaScannerConnection.scanFile(getContext(), new String[]{file.getAbsolutePath()}, null, null);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Close the document
            document.close();
        }
    }

    public void openPdf(File file) {
        // Create an Intent to open the PDF file
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Get the URI for the file using FileProvider
        Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);

        // Set flags to allow read access to the file
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Set MIME type to ensure it's recognized as a PDF file
        intent.setDataAndType(uri, "application/pdf");

        // Launch the intent to open the PDF
        startActivity(intent);
    }



    // Method to update referral status in Firestore
    // Make sure db is initialized at the top of the fragment or activity

    private void updateReferralStatusPersonnel(String referrerId, String referralDocumentId, String newStatus) {
        if (referrerId == null || referralDocumentId == null) {
            Log.e("UpdateError", "Referrer ID or Referral Document ID is null. Cannot update status.");
            return; // Exit if IDs are invalid
        }

        Log.d("UpdateStatus", "Updating personnel status. Referrer ID: " + referrerId + ", Referral ID: " + referralDocumentId + ", New Status: " + newStatus);

        // Update the referral status using the referrer_id
        db.collection("personnel")
                .document(referrerId) // Assuming you want to use referrerId to locate the document
                .collection("personnel_refferal_history").document(referralDocumentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreSuccess", "Personnel referral status updated successfully");
                    refreshReferralData(); // Call to refresh data after status update
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error updating personnel referral status", e);
                });
    }

    private void updateReferralStatusPersonnel(String referrerId, String referralDocId, String status, String rejectionReason) {
        Map<String, Object> updatedReferralData = new HashMap<>();
        updatedReferralData.put("status", status); // Update status to rejected
        updatedReferralData.put("reason_rejecting", rejectionReason); // Store rejection reason

        // Update the referral status in personnel collection
        db.collection("personnel")
                .document(referrerId)
                .collection("personnel_refferal_history")
                .document(referralDocId)
                .update(updatedReferralData)
                .addOnSuccessListener(aVoid -> {
                    // Optionally, show a success message or handle UI refresh
                    Toast.makeText(getActivity(), "Referral status updated to rejected.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Show failure message if updating failed
                    Toast.makeText(getActivity(), "Failed to reject referral.", Toast.LENGTH_SHORT).show();
                });
    }


    private void updateReferralStatusStudentWithReason(String referrerId, String referralDocId, String status, String rejectionReason) {
        Map<String, Object> updatedReferralData = new HashMap<>();
        updatedReferralData.put("status", status); // Update status to rejected
        updatedReferralData.put("reason_rejecting", rejectionReason); // Store rejection reason

        // Update the referral status in the student's collection
        db.collection("students")
                .document(referrerId)
                .collection("student_refferal_history")
                .document(referralDocId)
                .update(updatedReferralData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Referral has been rejected.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to reject referral.", Toast.LENGTH_SHORT).show();
                });
    }



    // Method to refresh referral data
    private void refreshReferralData() {
        // Call the onPendingClick method to refresh UI and fetch the latest data
        if (getActivity() != null && getView() != null) {
            getActivity().runOnUiThread(this::onPendingClick);
        } else {
            Log.e("UIError", "Activity or view is null, cannot refresh UI.");
        }
    }

    public static class ReferralViewHolder extends RecyclerView.ViewHolder {
        TextView referrerTextView, dateTextView, actionTextView;
        Button viewButton, acceptButton, rejectButton;

        public ReferralViewHolder(View itemView) {
            super(itemView);
            referrerTextView = itemView.findViewById(R.id.referrerTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            actionTextView = itemView.findViewById(R.id.actionTextView);
            viewButton = itemView.findViewById(R.id.viewButton);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }
}


