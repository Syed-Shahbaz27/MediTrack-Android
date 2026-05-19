package com.meditrack.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.meditrack.app.database.DatabaseHelper;
import com.meditrack.app.models.User;
import com.meditrack.app.utils.SessionManager;

/**
 * EmergencyAlertActivity — the SOS screen matching prototype Screen 10.
 *
 * SOS BUTTON — PRESS AND HOLD 2 SECONDS:
 * We use Handler.postDelayed() to schedule the SOS action after 2000ms.
 * When the user presses down (ACTION_DOWN) we post the Runnable.
 * If the user releases (ACTION_UP) before 2 seconds, we cancel it.
 * This prevents accidental taps from sending false emergency alerts.
 *
 * HOW THE ALERT IS SENT:
 * Intent.ACTION_SENDTO with "smsto:" URI opens the device's SMS app,
 * pre-filled with the emergency contact number and a message.
 * We do NOT send SMS programmatically (even though SEND_SMS is in the Manifest)
 * because sending without user confirmation is alarming. The SMS app
 * lets the user confirm before sending — correct UX for an emergency feature.
 *
 * EMERGENCY CONTACT SOURCE:
 * Loaded at runtime from the users table using DatabaseHelper.getUserById().
 * This demonstrates the emergency_contact column is connected to real UI.
 */
public class EmergencyAlertActivity extends AppCompatActivity {

    // ── View references ──────────────────────────────────────────────────────
    private MaterialButton btnSOS;
    private TextView tvEmergencyLabel, tvEmergencyNumber;

    // ── State ────────────────────────────────────────────────────────────────
    private String emergencyNumber = "";
    private String userName        = "";

    // ── Handler for the 2-second press-and-hold countdown ───────────────────
    private final Handler holdHandler  = new Handler(Looper.getMainLooper());
    private Runnable      sendSosRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_alert);

        // Get helpers
        SessionManager session = new SessionManager(this);
        DatabaseHelper  db     = DatabaseHelper.getInstance(this);

        // Bind views
        btnSOS             = findViewById(R.id.btnSOS);
        tvEmergencyLabel   = findViewById(R.id.tvEmergencyLabel);
        tvEmergencyNumber  = findViewById(R.id.tvEmergencyNumber);

        // Load emergency contact from database
        loadEmergencyContact(session.getUserId(), db);

        // Build the SOS Runnable — this fires when the 2-second hold completes
        sendSosRunnable = this::sendSosAlert;

        // Touch listener — press down starts timer, release cancels it
        setupSosButton();

        // Back arrow
        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Loads the logged-in user from SQLite and populates the
     * emergency contact label and number TextViews.
     */
    private void loadEmergencyContact(int userId, DatabaseHelper db) {
        User user = db.getUserById(userId);
        if (user != null) {
            userName        = user.getFullName();
            emergencyNumber = user.getEmergencyContact();

            if (emergencyNumber != null && !emergencyNumber.isEmpty()) {
                tvEmergencyLabel.setText("Emergency Contact");
                tvEmergencyNumber.setText(emergencyNumber);
            } else {
                tvEmergencyNumber.setText("No emergency contact saved in profile");
            }
        }
    }

    /**
     * Attaches a touch listener to the SOS button.
     * ACTION_DOWN → post Runnable with 2000ms delay.
     * ACTION_UP or ACTION_CANCEL → remove the pending Runnable.
     * If the Runnable fires it means the user held for 2 full seconds → send SOS.
     */
    private void setupSosButton() {
        btnSOS.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Toast.makeText(this,
                            "Keep holding to send SOS...", Toast.LENGTH_SHORT).show();
                    holdHandler.postDelayed(sendSosRunnable, 2000);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // User released too early — cancel the countdown
                    holdHandler.removeCallbacks(sendSosRunnable);
                    break;
            }
            return true; // consume the touch event
        });
    }

    /**
     * Called after 2 seconds of holding the SOS button.
     * Opens the SMS app with the emergency message pre-filled.
     */
    private void sendSosAlert() {
        if (emergencyNumber == null || emergencyNumber.isEmpty()) {
            Toast.makeText(this,
                    "No emergency contact saved. Add one in your profile.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String message =
                "🆘 SOS EMERGENCY ALERT 🆘\n" +
                        userName + " needs immediate help!\n" +
                        "Please respond urgently.\n" +
                        "— Sent from MediTrack";

        // "smsto:" URI opens the device SMS app pre-filled
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + emergencyNumber));
        smsIntent.putExtra("sms_body", message);

        try {
            startActivity(smsIntent);
            Toast.makeText(this,
                    "Opening SMS to " + emergencyNumber, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this,
                    "No SMS app found on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // CRITICAL: remove pending Runnable to prevent memory leaks.
        // If this activity is destroyed while the countdown is running,
        // the Runnable would otherwise hold a reference to the dead activity.
        holdHandler.removeCallbacks(sendSosRunnable);
    }
}