package com.finals.lagunauniversitysdo;

import static java.lang.String.format;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer_layout;
    private ImageView userIcon;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String loggedInUserId, UserName, firstName, lastName;
    private String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true); // Save login state
        editor.putString("userId", loggedInUserId); // Save the user ID (or any other user data)
        editor.apply();

        // Retrieve loggedInUserId from the Intent
        Intent intent = getIntent();
        loggedInUserId = intent.getStringExtra("USER_ID");
        UserName = intent.getStringExtra("STUDENT_NAME");
        firstName = intent.getStringExtra("FIRST_NAME");
        lastName = intent.getStringExtra("LAST_NAME");

        // Check if userId is retrieved successfully
        if (loggedInUserId == null) {
            Toast.makeText(this, "Error: User ID not passed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("MainActivity", "Logged in user ID: " + loggedInUserId);  // Log the user ID for debugging
        Log.d("MainActivity", "User name: " + UserName);
        Log.d("MainActivity", "First name: " + firstName);
        Log.d("MainActivity", "Last name: " + lastName);


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
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new refferalForm_student()).commit();
            navigationView.setCheckedItem(R.id.refferalForm);
        }

        userIcon = findViewById(R.id.user_icon);

        // Set OnClickListener for userIcon
        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Firestore query to get user data based on loggedInUserId
                db.collection("students").document(loggedInUserId).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    userName = documentSnapshot.getString("name");

                                    PopupMenu popup = new PopupMenu(MainActivity.this, v);
                                    popup.getMenuInflater().inflate(R.menu.user_menu, popup.getMenu());

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
                                                logUserActivity(loggedInUserId, "Logout");
                                                // Clear user session data
                                                UserSession.clearSession();

                                                Toast.makeText(MainActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();

                                                // Optionally, you can add a delay here for better UX
                                                new Handler().postDelayed(() -> {
                                                    // Finish the current activity and start the login activity
                                                    Intent intent = new Intent(MainActivity.this, LoginActivity.class); // Change this to your actual login activity
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the back stack
                                                    startActivity(intent);
                                                    finish(); // Close MainActivity
                                                }, 1000); // 1 second delay for user feedback

                                                return true;
                                            } else if (id == R.id.menu_activity_log) {
                                                String studentId = loggedInUserId; // Replace with actual student ID of the logged-in user
                                                showActivityLogDialog(studentId);

                                            } else {
                                                return false;
                                            }
                                            return false;
                                        }

                                    });

                                } else {
                                    // User not found in Firestore
                                    Toast.makeText(MainActivity.this, "User not found in Firestore.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        });
            }
        });

    }

    private void logUserActivity(String studentId, String activityType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Reference to the student's ActivityLog subcollection
        CollectionReference activityLogRef = db.collection("students").document(studentId).collection("ActivityLog");

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
                .addOnFailureListener(e -> Log.w("ActivityLog", "Error fetching existing logs for student: " + studentId, e));
    }



    private void showActivityLogDialog(String studentId) {
        // Create a dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Activity Log");

        // Create a layout for displaying the logs
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // Access the Firestore instance and fetch the logs
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("students").document(studentId)
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
                                String logText = format("%s: %s", type, formattedDate);

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

                    builder.setView(layout);
                    builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
                    builder.create().show(); // Show the dialog
                });
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

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause called");
        // Do not clear session here unless logging out
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume called");

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            // Redirect to login screen if user is not logged in
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;


        int itemId = item.getItemId();
        if (itemId == R.id.refferalForm) {
            selectedFragment = new refferalForm_student();
        } else if (itemId == R.id.myRefferal) {
            selectedFragment = new MyRefferal_student();
        } else if (itemId == R.id.myViolation) {
            selectedFragment = new myViolations_student();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }
}


