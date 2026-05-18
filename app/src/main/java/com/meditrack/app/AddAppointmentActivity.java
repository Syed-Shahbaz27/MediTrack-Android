package com.meditrack.app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.meditrack.app.database.DatabaseHelper;
import com.meditrack.app.models.Appointment;
import com.meditrack.app.utils.SessionManager;

import java.util.Calendar;
import java.util.Locale;

/**
 * AddAppointmentActivity — Sub-Activity for booking a new appointment.
 *
 * WHY DatePickerDialog?
 * Prevents the user typing an invalid date like "32/13/2026".
 * Calendar.getInstance() opens the picker on today's date automatically.
 * month + 1 is needed because Calendar months are 0-indexed (January = 0).
 *
 * WHY finish() after saving?
 * finish() pops this activity off the back stack and returns to
 * AppointmentActivity. AppointmentActivity.onResume() then reloads
 * the list from SQLite so the new appointment appears immediately.
 */
public class AddAppointmentActivity extends AppCompatActivity {

    // ── View references ──────────────────────────────────────────────────────
    private TextInputLayout   tilDoctorName, tilClinic;
    private TextInputEditText etDoctorName, etClinic, etAptDate, etAptTime;
    private Spinner           spinnerSpecialty;

    // ── Data helpers ─────────────────────────────────────────────────────────
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);

        dbHelper         = DatabaseHelper.getInstance(this);
        sessionManager   = new SessionManager(this);

        // Bind all views
        tilDoctorName    = findViewById(R.id.tilDoctorName);
        tilClinic        = findViewById(R.id.tilClinic);
        etDoctorName     = findViewById(R.id.etDoctorName);
        etClinic         = findViewById(R.id.etClinic);
        etAptDate        = findViewById(R.id.etAptDate);
        etAptTime        = findViewById(R.id.etAptTime);
        spinnerSpecialty = findViewById(R.id.spinnerSpecialty);

        setupSpecialtySpinner();
        setupDatePicker();
        setupTimePicker();

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        ((MaterialButton) findViewById(R.id.btnSaveAppointment))
                .setOnClickListener(v -> saveAppointment());
    }

    /**
     * Fills the Specialty spinner with common medical specialties.
     * ArrayAdapter bridges our String[] to the Spinner widget.
     * simple_spinner_item     = the single collapsed line when closed.
     * simple_spinner_dropdown = the expanded list when the user taps it.
     */
    private void setupSpecialtySpinner() {
        String[] specialties = {
                "General Physician",
                "Cardiology",
                "Endocrinology",
                "Dermatology",
                "Orthopedics",
                "Neurology",
                "Other"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                specialties
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialty.setAdapter(adapter);
    }

    /**
     * Tapping the date field shows Android's built-in calendar picker.
     * We pre-open it on today's date using Calendar.getInstance().
     * month + 1 converts from 0-indexed (Jan=0) to human-readable (Jan=1).
     */
    private void setupDatePicker() {
        etAptDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(
                    this,
                    (view, year, month, day) -> etAptDate.setText(
                            String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
                    ),
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    /**
     * Tapping the time field shows Android's built-in clock picker.
     * false = 12-hour format with AM/PM, matching the prototype design.
     * Default is 10:00 AM which is a sensible appointment start time.
     */
    private void setupTimePicker() {
        etAptTime.setOnClickListener(v -> {
            new TimePickerDialog(
                    this,
                    (view, hour, minute) -> {
                        String amPm = hour < 12 ? "AM" : "PM";
                        int displayHour = hour % 12;
                        if (displayHour == 0) displayHour = 12;
                        etAptTime.setText(
                                String.format(Locale.getDefault(), "%02d:%02d %s",
                                        displayHour, minute, amPm)
                        );
                    },
                    10, 0, false
            ).show();
        });
    }

    /**
     * Validates all inputs, then saves to SQLite via DatabaseHelper.
     * All errors appear on the specific field that failed — not as a generic Toast.
     * Only after all checks pass do we call dbHelper.addAppointment().
     */
    private void saveAppointment() {
        // Read all field values
        String doctor    = etDoctorName.getText().toString().trim();
        String clinic    = etClinic.getText().toString().trim();
        String date      = etAptDate.getText().toString().trim();
        String time      = etAptTime.getText().toString().trim();
        String specialty = spinnerSpecialty.getSelectedItem().toString();

        // Clear previous errors before re-validating
        tilDoctorName.setError(null);
        tilClinic.setError(null);

        // Validate — stop at first failure
        if (TextUtils.isEmpty(doctor)) {
            tilDoctorName.setError("Doctor name is required");
            return;
        }
        if (TextUtils.isEmpty(clinic)) {
            tilClinic.setError("Clinic location is required");
            return;
        }
        if (date.equals("Select date") || TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        // All valid — build the Appointment object and save
        Appointment apt = new Appointment(
                sessionManager.getUserId(),
                doctor,
                specialty,
                clinic,
                date,
                time,
                true  // reminder enabled by default
        );

        long result = dbHelper.addAppointment(apt);

        if (result != -1) {
            Toast.makeText(this,
                    "Appointment with Dr. " + doctor + " saved!",
                    Toast.LENGTH_SHORT).show();
            finish(); // Return to AppointmentActivity — onResume reloads the list
        } else {
            Toast.makeText(this,
                    "Failed to save. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
