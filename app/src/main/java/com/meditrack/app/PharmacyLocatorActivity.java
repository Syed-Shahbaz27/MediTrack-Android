package com.meditrack.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * PharmacyLocatorActivity — GPS Sub-Activity matching prototype Screen 9.
 *
 * WHY RUNTIME PERMISSION?
 * Location is classified as a "dangerous" permission by Android.
 * Since API 23 (Android 6.0), dangerous permissions must be requested
 * at runtime — declaring them in the Manifest alone is not enough.
 * We check first (checkSelfPermission), then ask (requestPermissions),
 * then handle the answer in onRequestPermissionsResult().
 *
 * WHY HARDCODED PHARMACIES?
 * Google Maps SDK for Android requires a billing account and credit card.
 * That is not practical for a student assignment. The GPS coordinate is
 * still REAL — we get the actual device location and display it.
 * The 3 pharmacy names and distances match the prototype exactly.
 *
 * WHY getLastKnownLocation()?
 * It returns a cached position instantly — no GPS warm-up wait, no battery drain.
 * For showing nearby pharmacies this is sufficient. A production app would use
 * LocationManager.requestLocationUpdates() for live tracking.
 */
public class PharmacyLocatorActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 101;
    private TextView tvGpsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_locator);

        tvGpsStatus = findViewById(R.id.tvGpsStatus);
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        // Start the permission flow
        requestLocationPermission();
    }

    /**
     * Checks if permission is already granted.
     * If yes → get location immediately.
     * If no  → show the system permission dialog to the user.
     */
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );
        }
    }

    /**
     * Android calls this after the user taps Allow or Deny on the dialog.
     * PERMISSION_GRANTED → get location.
     * Denied → show a friendly message; pharmacies still display.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                tvGpsStatus.setText(
                        "📍 Permission denied — showing Muscat area pharmacies");
            }
        }
    }

    /**
     * Gets the last known cached location from the device.
     * Tries GPS first, falls back to Network provider if GPS returns null.
     * Updates tvGpsStatus with real coordinates or a fallback message.
     */
    private void getLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (lm == null) {
                tvGpsStatus.setText("📍 Location service unavailable");
                return;
            }

            Location loc = null;

            // Check permission again before calling getLastKnownLocation
            // Android requires this even after permission was granted
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                // Try GPS provider first — most accurate
                loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                // Fall back to network if GPS has no cached location
                if (loc == null) {
                    loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }

            if (loc != null) {
                // Show real device coordinates
                tvGpsStatus.setText(String.format(
                        "📍 GPS: %.4f° N, %.4f° E",
                        loc.getLatitude(),
                        loc.getLongitude()
                ));
            } else {
                // Emulators often have no cached location
                tvGpsStatus.setText("📍 Using Muscat, Oman — 23.5880° N, 58.3829° E");
            }

        } catch (Exception e) {
            tvGpsStatus.setText("📍 Location unavailable — showing Muscat pharmacies");
        }
    }
}