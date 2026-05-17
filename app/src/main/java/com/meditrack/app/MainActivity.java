package com.meditrack.app;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;


import com.google.android.material.navigation.NavigationView;
import com.meditrack.app.database.DatabaseHelper;
import com.meditrack.app.models.Appointment;
import com.meditrack.app.models.Medication;
import com.meditrack.app.utils.SessionManager;

/**
 * MainActivity — the Home Dashboard. The hub of the entire app.
 *
 * WHY THIS IS THE MOST IMPORTANT ACTIVITY:
 * Every other screen is reachable from here via the nav drawer,
 * bottom nav bar, and the 2x2 feature grid. This is the first
 * screen the user sees after every login.
 *
 * NAVIGATION DRAWER:
 * DrawerLayout contains two children — the main content and the drawer panel.
 * The hamburger (☰) opens it. NavigationView handles the menu items.
 * We implement NavigationView.OnNavigationItemSelectedListener to handle clicks.
 *
 * DATA FLOW:
 * SessionManager.getUserId() → DatabaseHelper.getLatestMedication() → reminder card
 * SessionManager.getUserId() → DatabaseHelper.getNextAppointment() → reminder card
 * loadDashboardData() is called in onResume() so data stays fresh after returning.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvGreeting, tvLastLogin, tvMedReminder, tvAptReminder;
    private CardView cardMedReminder, cardAptReminder;
    private LinearLayout cardMedication, cardAppointments, cardPharmacy, cardEmergency;
    private LinearLayout btnNavHome, btnNavMeds, btnNavAppointments, btnNavProfile;

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        dbHelper       = DatabaseHelper.getInstance(this);

        // Guard — if session was lost somehow, redirect to login
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
            return;
        }

        initViews();
        setupDrawer();
        setupBottomNav();
        setupGridCards();
    }

    /**
     * onResume fires every time this activity becomes visible again —
     * including when the user presses Back from AddMedication.
     * Reloading data here ensures the reminder cards always show fresh info.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void initViews() {
        drawerLayout        = findViewById(R.id.drawerLayout);
        navigationView      = findViewById(R.id.navigationView);
        tvGreeting          = findViewById(R.id.tvGreeting);
        tvLastLogin         = findViewById(R.id.tvLastLogin);
        tvMedReminder       = findViewById(R.id.tvMedReminder);
        tvAptReminder       = findViewById(R.id.tvAptReminder);
        cardMedReminder     = findViewById(R.id.cardMedReminder);
        cardAptReminder     = findViewById(R.id.cardAptReminder);
        cardMedication      = findViewById(R.id.cardMedication);
        cardAppointments    = findViewById(R.id.cardAppointments);
        cardPharmacy        = findViewById(R.id.cardPharmacy);
        cardEmergency       = findViewById(R.id.cardEmergency);
        btnNavHome          = findViewById(R.id.btnNavHome);
        btnNavMeds          = findViewById(R.id.btnNavMeds);
        btnNavAppointments  = findViewById(R.id.btnNavAppointments);
        btnNavProfile       = findViewById(R.id.btnNavProfile);
    }

    private void setupDrawer() {
        // Register this activity to handle drawer menu item clicks
        navigationView.setNavigationItemSelectedListener(this);

        // Populate the drawer header with the logged-in user's details
        View header      = navigationView.getHeaderView(0);
        TextView tvName  = header.findViewById(R.id.tvNavName);
        TextView tvEmail = header.findViewById(R.id.tvNavEmail);
        tvName.setText(sessionManager.getUserName());
        tvEmail.setText(sessionManager.getUserEmail());

        // --- DEBUG TEST ---
        // We add a Toast message to see if the click works
        findViewById(R.id.btnHamburger).setOnClickListener(v -> {
            Toast.makeText(this, "Button Clicked! Opening Drawer...", Toast.LENGTH_SHORT).show();
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // Profile circle → Team Members screen
        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, TeamMembersActivity.class)));
    }
    private void setupBottomNav() {
        btnNavHome.setOnClickListener(v ->
                Toast.makeText(this, "You are on Home", Toast.LENGTH_SHORT).show());
        btnNavMeds.setOnClickListener(v ->
                startActivity(new Intent(this, MedicationListActivity.class)));
        btnNavAppointments.setOnClickListener(v ->
                startActivity(new Intent(this, AppointmentActivity.class)));
        btnNavProfile.setOnClickListener(v ->
                startActivity(new Intent(this, TeamMembersActivity.class)));
    }

    private void setupGridCards() {
        cardMedication.setOnClickListener(v ->
                startActivity(new Intent(this, MedicationListActivity.class)));
        cardAppointments.setOnClickListener(v ->
                startActivity(new Intent(this, AppointmentActivity.class)));
        cardPharmacy.setOnClickListener(v ->
                startActivity(new Intent(this, PharmacyLocatorActivity.class)));
        cardEmergency.setOnClickListener(v ->
                startActivity(new Intent(this, EmergencyAlertActivity.class)));
        cardMedReminder.setOnClickListener(v ->
                startActivity(new Intent(this, MedicationListActivity.class)));
        cardAptReminder.setOnClickListener(v ->
                startActivity(new Intent(this, AppointmentActivity.class)));
    }

    /**
     * Pulls data from SQLite and updates the dashboard UI.
     * SessionManager provides userId → DatabaseHelper fetches data.
     */
    private void loadDashboardData() {
        int userId    = sessionManager.getUserId();
        String name   = sessionManager.getUserName();
        String first  = name.contains(" ") ? name.split(" ")[0] : name;

        tvGreeting.setText("Hello " + first + " 👋");
        tvLastLogin.setText("Last login: " + sessionManager.getLastLogin());

        Medication med = dbHelper.getLatestMedication(userId);
        tvMedReminder.setText(med != null
                ? "💊  " + med.getMedName() + " at " + med.getReminderTime()
                : "💊  No medications added yet");

        Appointment apt = dbHelper.getNextAppointment(userId);
        tvAptReminder.setText(apt != null
                ? "📅  Dr. " + apt.getDoctorName() + " at " + apt.getAppointmentTime()
                : "📅  No appointments scheduled");

        // ADD THIS LINE AT THE BOTTOM
        // This forces the sidebar open so we can see it works
        drawerLayout.openDrawer(GravityCompat.START);
    }

    /**
     * Called when user taps an item in the Navigation Drawer.
     * The drawer closes first, then we navigate to the selected screen.
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int id = item.getItemId();

        if      (id == R.id.nav_home)        { /* already here */ }
        else if (id == R.id.nav_medications) startActivity(new Intent(this, MedicationListActivity.class));
        else if (id == R.id.nav_appointments) startActivity(new Intent(this, AppointmentActivity.class));
        else if (id == R.id.nav_health)      startActivity(new Intent(this, HealthMetricActivity.class));
        else if (id == R.id.nav_pharmacy)    startActivity(new Intent(this, PharmacyLocatorActivity.class));
        else if (id == R.id.nav_emergency)   startActivity(new Intent(this, EmergencyAlertActivity.class));
        else if (id == R.id.nav_team)        startActivity(new Intent(this, TeamMembersActivity.class));
        else if (id == R.id.nav_contact)     startActivity(new Intent(this, ContactUsActivity.class));
        else if (id == R.id.nav_logout)      logout();

        return true;
    }

    private void logout() {
        sessionManager.clearSession();
        Intent i = new Intent(this, SplashActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
