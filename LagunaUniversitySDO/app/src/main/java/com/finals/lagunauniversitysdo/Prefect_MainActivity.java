package com.finals.lagunauniversitysdo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.navigation.NavigationView;
public class Prefect_MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private ImageView userIcon;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String loggedInPrefectId; // Prefect ID
    private String userName;
    private DrawerLayout drawer_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefect_activitymain);

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
                                            } else {
                                                return false;
                                            }
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
