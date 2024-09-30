package com.finals.lagunauniversitysdo;

import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.navigation.NavigationView;

public class personnel_MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer_layout;
    private TextView toolbarTitle;
    private ImageView userIcon;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String loggedInUserId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personnel_main);

        // Retrieve loggedInUserId from the Intent
        Intent intent = getIntent();
        loggedInUserId = intent.getStringExtra("USER_ID");

        // Check if userId is retrieved successfully
        if (loggedInUserId == null) {
            Toast.makeText(this, "Error: User ID not passed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("personnel_MainActivity", "Logged in user ID: " + loggedInUserId);  // Log the user ID for debugging

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle = findViewById(R.id.toolbar_title);
        drawer_layout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.open_nav, R.string.close_nav);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new refferalForm_student()).commit();
            navigationView.setCheckedItem(R.id.personnel_myRefferal);
            toolbarTitle.setText("Refferal Form");
        }

        userIcon = findViewById(R.id.user_icon);

        // Set OnClickListener for userIcon
        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Firestore query to get user data based on loggedInUserId
                db.collection("personnel").document(loggedInUserId).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    userName = documentSnapshot.getString("name");

                                    PopupMenu popup = new PopupMenu(personnel_MainActivity.this, v);
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
                                                // Clear user session data
                                                UserSession.clearSession();

                                                Toast.makeText(personnel_MainActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();

                                                // Optionally, you can add a delay here for better UX
                                                new Handler().postDelayed(() -> {
                                                    // Finish the current activity and start the login activity
                                                    Intent intent = new Intent(personnel_MainActivity.this, LoginActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the back stack
                                                    startActivity(intent);
                                                    finish(); // Close MainActivity
                                                }, 1000); // 1 second delay for user feedback

                                                return true;
                                            } else {
                                                return false;
                                            }
                                        }
                                    });

                                } else {
                                    // User not found in Firestore
                                    Toast.makeText(personnel_MainActivity.this, "User not found in Firestore.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(personnel_MainActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.personnel_refferalForm) {
            selectedFragment = new personnel_refferal_form();
        } else if (itemId == R.id.personnel_myRefferal) {
            selectedFragment = new personnel_myRefferalForm();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }
}
