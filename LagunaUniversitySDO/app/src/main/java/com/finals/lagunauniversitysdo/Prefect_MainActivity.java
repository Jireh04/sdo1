package com.finals.lagunauniversitysdo;

import static java.lang.String.format;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import android.content.DialogInterface;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java.util.Collections;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.Query;


import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import android.text.method.LinkMovementMethod;
import android.content.SharedPreferences;

public class Prefect_MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private AlertDialog alertDialog;  // Declare the AlertDialog as a class-level variable
    private static final String PREFS_NAME = "PrefectAppPrefs";
    private static final String KEY_PENDING_COUNT = "pending_referrals_count"; // Key to store the count

    private DrawerLayout drawerLayout;
    private Set<String> getPendingReferrals() {
        return pendingReferrals;
    }


    private ImageView userIcon;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String loggedInPrefectId; // Prefect ID
    private String userName;
    private DrawerLayout drawer_layout;
    private Set<String> pendingReferrals = Collections.synchronizedSet(new HashSet<>());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefect_activitymain);
      

        // Retrieve the saved pending count from SharedPreferences
        int savedPendingCount = getSavedPendingCount();
        updateNotificationBadge(savedPendingCount); // Update the badge with the saved count

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve loggedInPrefectId from the Intent
        Intent intent = getIntent();
        loggedInPrefectId = intent.getStringExtra("PREFECT_ID");

        // Check if loggedInPrefectId is retrieved successfully
        if (loggedInPrefectId == null) {
            Toast.makeText(this, "Error: Prefect ID not passed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("Prefect_MainActivity", "Logged in prefect ID: " + loggedInPrefectId);  // Log the prefect ID for debugging

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawer_layout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.open_nav, R.string.close_nav);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new dashboard_prefect()).commit();
            navigationView.setCheckedItem(R.id.Dashboard);
        }

        userIcon = findViewById(R.id.user_icon);

        // Set OnClickListener for userIcon
        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Firestore query to get prefect data based on loggedInPrefectId
                db.collection("prefect").document(loggedInPrefectId).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    // Assuming 'name' is the field for prefect name
                                    userName = documentSnapshot.getString("name");

                                    PopupMenu popup = new PopupMenu(Prefect_MainActivity.this, v);
                                    popup.getMenuInflater().inflate(R.menu.prefect_side, popup.getMenu());

                                    MenuItem menuItem = popup.getMenu().findItem(R.id.menu_user_name);
                                    if (menuItem != null) {
                                        menuItem.setTitle(userName);
                                    }

                                    popup.show();

                                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            int id = item.getItemId();

                                            if (id == R.id.menu_user_name) {
                                                return true;
                                            } else if (id == R.id.menu_logout) {
                                                logUserActivity(loggedInPrefectId, "Logout");
                                                // Clear PrefectSession data
                                                PrefectSession.clearSession();

                                                Toast.makeText(Prefect_MainActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();

                                                // Optionally, add a delay for better UX
                                                new Handler().postDelayed(() -> {
                                                    // Finish the current activity and start the login activity
                                                    Intent intent = new Intent(Prefect_MainActivity.this, LoginActivity.class); // Change this to your login activity
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the back stack
                                                    startActivity(intent);
                                                    finish(); // Close MainActivity
                                                }, 1000); // 1 second delay for user feedback

                                                return true;
                                            } else if (id == R.id.menu_settings) {
                                                // Open the Settings fragment when Settings is clicked
                                                SettingsFragment settingsFragment = new SettingsFragment(); // Create an instance of your SettingsFragment

                                                // Get the FragmentManager and start a transaction
                                                FragmentManager fragmentManager = getSupportFragmentManager();
                                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                                                // Replace the current fragment with the SettingsFragment
                                                fragmentTransaction.replace(R.id.fragment_container, settingsFragment); // Replace 'fragment_container' with the ID of your container layout
                                                fragmentTransaction.addToBackStack(null); // Optional: Add the transaction to the back stack
                                                fragmentTransaction.commit(); // Commit the transaction

                                                return true;
                                            } else if (id == R.id.menu_activity_log) {
                                                String prefectId = loggedInPrefectId; // Replace with actual student ID of the logged-in user
                                                showActivityLogDialog(prefectId);
                                            } else {
                                                return false;
                                            }
                                            return false;
                                        }
                                    });

                                } else {
                                    // Prefect not found in Firestore
                                    Toast.makeText(Prefect_MainActivity.this, "Prefect not found in Firestore.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Prefect_MainActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Set OnClickListener for notification icon
        ImageView notificationIcon = findViewById(R.id.notification_icon);
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the pending referrals dialog when clicked
                showPendingReferralsDialog();
            }
        });


        // Fetch and display pending referrals as soon as the activity is created
        fetchPendingReferrals(); // This will show the notification count as soon as the activity starts
    }
    private void fetchPendingReferrals() {
        AtomicInteger tasksCompleted = new AtomicInteger(0); // Tracks completed tasks
        AtomicInteger pendingCount = new AtomicInteger(0);  // This will count pending referrals

        // Fetch both student and personnel referrals
        fetchStudentReferrals(pendingReferrals, tasksCompleted, pendingCount);
        fetchPersonnelReferrals(pendingReferrals, tasksCompleted, pendingCount);
    }

    private void showPendingReferralsDialog() {
        Set<String> pendingReferrals = getPendingReferrals(); // Now this works fine
        StringBuilder referralDetails = new StringBuilder();

        // Build the referral details string
        if (pendingReferrals.isEmpty()) {
            new AlertDialog.Builder(Prefect_MainActivity.this)
                    .setTitle("No Pending Referrals")
                    .setMessage("There are no pending referrals at the moment.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            for (String referral : pendingReferrals) {
                referralDetails.append(referral).append("\n\n");
            }

            // Create the SpannableString to make the text clickable
            SpannableString spannableMessage = new SpannableString(referralDetails.toString());

            // Set up the clickable span
            ClickableSpan referralClickSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // Dismiss the dialog
                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }

                    // Decrement the badge count after the click
                    decrementBadgeCount();

                    // When the text is clicked, navigate to the refferals_prefect fragment
                    navigateToReferralsFragment();
                }
            };

            // Apply the clickable span to the text (you can apply it to specific part if needed)
            spannableMessage.setSpan(referralClickSpan, 0, spannableMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Show the dialog with clickable text
            alertDialog = new AlertDialog.Builder(Prefect_MainActivity.this)
                    .setTitle("Pending Referrals")
                    .setMessage(spannableMessage)
                    .setPositiveButton("OK", null)
                    .setCancelable(true)
                    .create(); // Creating the alert dialog object

            alertDialog.show();  // Show the dialog

            // Make the text clickable by setting LinkMovementMethod
            TextView textView = alertDialog.findViewById(android.R.id.message);
            if (textView != null) {
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    private void decrementBadgeCount() {
        // Find the TextView that displays the notification badge count
        TextView notificationBadge = findViewById(R.id.notification_badge);

        // Check if the badge exists and is showing a number
        if (notificationBadge != null && notificationBadge.getVisibility() == View.VISIBLE) {
            try {
                // Get the current count from the badge, convert it to an integer
                int currentCount = Integer.parseInt(notificationBadge.getText().toString());

                // If there is more than 0 referrals, decrease the count
                if (currentCount > 0) {
                    currentCount--;  // Decrease the count by 1
                    notificationBadge.setText(String.valueOf(currentCount));  // Update the badge with the new count

                    // Save the updated count to SharedPreferences
                    savePendingCount(currentCount);
                }

                // If the count is 0, hide the badge
                if (currentCount == 0) {
                    notificationBadge.setVisibility(View.GONE);
                }

            } catch (NumberFormatException e) {
                // Handle any parsing errors if the value is not a number
                e.printStackTrace();
            }
        }
    }



// Save the pending count to SharedPreferences
    private void savePendingCount(int count) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_PENDING_COUNT, count); // Save the count
        editor.apply(); // Apply the changes asynchronously
    }

    // Retrieve the saved pending count from SharedPreferences
    private int getSavedPendingCount() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getInt(KEY_PENDING_COUNT, 0); // Default to 0 if no saved count is found
    }

    // Update the notification badge with the current count
    private void updateNotificationBadge(int pendingCount) {
        TextView notificationBadge = findViewById(R.id.notification_badge);

        if (pendingCount > 0) {
            notificationBadge.setText(String.valueOf(pendingCount)); // Set the new count on the badge
            notificationBadge.setVisibility(View.VISIBLE); // Show the badge
        } else {
            notificationBadge.setVisibility(View.GONE); // Hide the badge if no pending referrals
        }
    }




    private void navigateToReferralsFragment() {
        // Create a new instance of the refferals_prefect fragment
        refferals_prefect referralsFragment = new refferals_prefect();

        // Replace the current fragment with the new one
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, referralsFragment) // Replace with your container ID
                .addToBackStack(null) // Optional: Add to back stack so user can navigate back
                .commit();
    }

    // Your existing methods and member variables...



    private void fetchStudentReferrals(Set<String> pendingReferrals, AtomicInteger tasksCompleted, AtomicInteger pendingCount) {
        db.collection("students").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("fetchStudentReferrals", "No students found.");
                        checkAndShowDialog(pendingReferrals, tasksCompleted.incrementAndGet());
                        return;
                    }

                    AtomicInteger studentTasks = new AtomicInteger(0); // Track individual student tasks
                    for (DocumentSnapshot studentSnapshot : queryDocumentSnapshots) {
                        studentTasks.incrementAndGet();
                        db.collection("students")
                                .document(studentSnapshot.getId())
                                .collection("student_refferal_history")
                                .whereEqualTo("status", "pending")
                                .get()
                                .addOnSuccessListener(studentReferralSnapshots -> {
                                    for (DocumentSnapshot referral : studentReferralSnapshots) {
                                        String referralInfo = String.format(
                                                "Student ID: %s\nStudent Name: %s\nViolation: %s\nDate: %s",
                                                referral.getString("referrer_id"),
                                                referral.getString("student_referrer"),
                                                referral.getString("violation"),
                                                referral.getString("date"));
                                        pendingReferrals.add(referralInfo);
                                        pendingCount.incrementAndGet(); // Increment the pending count
                                    }
                                    Log.d("fetchStudentReferrals", "Pending referrals found: " + pendingReferrals.size());
                                })
                                .addOnCompleteListener(task -> {
                                    if (studentTasks.decrementAndGet() == 0) {
                                        checkAndShowDialog(pendingReferrals, tasksCompleted.incrementAndGet());
                                        updateNotificationBadge(pendingCount.get()); // Update badge after processing
                                    }
                                });
                    }
                    if (studentTasks.get() == 0) {
                        checkAndShowDialog(pendingReferrals, tasksCompleted.incrementAndGet());
                        updateNotificationBadge(pendingCount.get()); // Update badge after processing
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("fetchStudentReferrals", "Error fetching students: " + e.getMessage());
                    checkAndShowDialog(pendingReferrals, tasksCompleted.incrementAndGet());
                    updateNotificationBadge(pendingCount.get()); // Update badge after failure
                });
    }

    private void fetchPersonnelReferrals(Set<String> pendingReferrals, AtomicInteger tasksCompleted, AtomicInteger pendingCount) {
        db.collection("personnel").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    AtomicInteger personnelTasks = new AtomicInteger(0); // Track individual personnel tasks
                    for (DocumentSnapshot personnelSnapshot : queryDocumentSnapshots) {
                        personnelTasks.incrementAndGet();
                        db.collection("personnel")
                                .document(personnelSnapshot.getId())
                                .collection("personnel_refferal_history")
                                .whereEqualTo("status", "pending")
                                .get()
                                .addOnSuccessListener(personnelReferralSnapshots -> {
                                    for (DocumentSnapshot referral : personnelReferralSnapshots) {
                                        String referralInfo = String.format(
                                                "Personnel ID: %s\nPersonnel Name: %s\nViolation: %s\nDate: %s",
                                                referral.getString("referrer_id"),
                                                referral.getString("personnel_referrer"),
                                                referral.getString("violation"),
                                                referral.getString("date"));
                                        pendingReferrals.add(referralInfo);
                                        pendingCount.incrementAndGet(); // Increment the pending count
                                    }
                                    Log.d("fetchPersonnelReferrals", "Pending personnel referrals found: " + pendingReferrals.size());
                                })
                                .addOnCompleteListener(task -> {
                                    if (personnelTasks.decrementAndGet() == 0) {
                                        checkAndShowDialog(pendingReferrals, tasksCompleted.incrementAndGet());
                                        updateNotificationBadge(pendingCount.get()); // Update badge after processing
                                    }
                                });
                    }
                    if (personnelTasks.get() == 0) {
                        checkAndShowDialog(pendingReferrals, tasksCompleted.incrementAndGet());
                        updateNotificationBadge(pendingCount.get()); // Update badge after processing
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("fetchPersonnelReferrals", "Error fetching personnel: " + e.getMessage());
                    checkAndShowDialog(pendingReferrals, tasksCompleted.incrementAndGet());
                    updateNotificationBadge(pendingCount.get()); // Update badge after failure
                });
    }




    private void checkAndShowDialog(Set<String> pendingReferrals, int completedTasks) {
        Log.d("DEBUG", "Tasks completed: " + completedTasks + ", Pending referrals size: " + pendingReferrals.size());
        if (completedTasks >= 2) { // Ensures both tasks (student and personnel referrals) are completed
            // Update notification badge with the pending referral count
            updateNotificationBadge(pendingReferrals.size());
        }
    }



    @Override
    public void onBackPressed() {
        // Show a Toast message to inform the user
        Toast.makeText(this, "Please log out first to exit", Toast.LENGTH_SHORT).show();

        // Optionally, you can add a delay before taking any action or preventing the back press
        new Handler().postDelayed(() -> {
            // Prevent back action completely
        }, 2000); // Delay for 2 seconds
    }

    private void logUserActivity(String prefectID, String activityType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Reference to the student's ActivityLog subcollection
        CollectionReference activityLogRef = db.collection("prefect").document(prefectID).collection("ActivityLog");

        // Query to get the existing count of documents for the specific activity type (e.g., "Login" or "Logout")
        activityLogRef.whereEqualTo("type", activityType)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Count the number of existing logs of this type
                    int count = querySnapshot.size() + 1; // Next sequence number
                    String documentId = activityType + ": " + count; // Format the document ID

                    // Create the log entry data
                    Map<String, Object> logEntry = new HashMap<>();
                    logEntry.put("type", activityType); // "Login" or "Logout"
                    logEntry.put("timestamp", FieldValue.serverTimestamp()); // Auto-generated server timestamp

                    // Add the log entry with the custom document ID
                    activityLogRef.document(documentId)
                            .set(logEntry)
                            .addOnSuccessListener(aVoid -> Log.d("ActivityLog", "Log entry added with ID: " + documentId))
                            .addOnFailureListener(e -> Log.w("ActivityLog", "Error adding log entry with ID: " + documentId, e));
                })
                .addOnFailureListener(e -> Log.w("ActivityLog", "Error fetching existing logs for student: " + prefectID, e));
    }



    private void showActivityLogDialog(String prefectID) {
        // Create a dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logs");

        // Create a ScrollView to make the logs scrollable
        ScrollView scrollView = new ScrollView(this);

        // Create a layout for displaying the logs
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // Access the Firestore instance and fetch the logs
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("prefect").document(prefectID)
                .collection("ActivityLog") // Subcollection for logs
                .orderBy("timestamp", Query.Direction.DESCENDING) // Sort by date, newest first
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean hasLogs = false;

                        for (DocumentSnapshot document : task.getResult()) {
                            // Get log details
                            String type = document.getString("type"); // "Login" or "Logout"
                            Timestamp timestamp = document.getTimestamp("timestamp");

                            if (type != null && timestamp != null) {
                                hasLogs = true;

                                // Format the date and time
                                String formattedDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", java.util.Locale.getDefault()).format(timestamp.toDate());
                                String logText = String.format("%s: %s", type, formattedDate);

                                // Create TextView for each log entry
                                TextView logEntry = new TextView(this);
                                logEntry.setText(logText);
                                logEntry.setPadding(10, 10, 10, 10);
                                logEntry.setTextColor(Color.BLACK);

                                // Add separator line
                                View separator = new View(this);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, 2);
                                separator.setLayoutParams(params);
                                separator.setBackgroundColor(Color.LTGRAY);

                                layout.addView(logEntry); // Add log entry to layout
                                layout.addView(separator); // Add separator
                            }
                        }

                        if (!hasLogs) {
                            TextView noLogsText = new TextView(this);
                            noLogsText.setText("No logs available.");
                            noLogsText.setPadding(10, 10, 10, 10);
                            noLogsText.setTextColor(Color.GRAY);
                            layout.addView(noLogsText);
                        }

                    } else {
                        TextView errorText = new TextView(this);
                        errorText.setText("Error fetching logs.");
                        errorText.setPadding(10, 10, 10, 10);
                        errorText.setTextColor(Color.RED);
                        layout.addView(errorText);
                    }

                    // Add the layout to the ScrollView
                    scrollView.addView(layout);

                    // Set the ScrollView as the view for the dialog
                    builder.setView(scrollView);

                    // Add a "Close" button
                    builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

                    // Show the dialog
                    builder.create().show();
                });
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.Dashboard) {
            selectedFragment = new dashboard_prefect();
        } else if (itemId == R.id.Referrals) {
            selectedFragment = new refferals_prefect();
        } else if (itemId == R.id.Reports) {
            selectedFragment = new reports_prefect();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }
}
