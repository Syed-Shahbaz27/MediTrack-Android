package com.meditrack.app;

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
import com.meditrack.app.models.Medication;
import com.meditrack.app.utils.SessionManager;

import java.util.Locale;

/**
 * AddMedicationActivity — Sub-Activity for adding a new medication.
 *
 * WHY Sub-Activity?
 * The assignment distinguishes Group Activities (show lists) from Sub-Activities
 * (show detail or forms). This form is the "detail" of a medication entry.
 * After saving, finish() pops this activity back to the list.
 * MedicationListActivity.onResume() reloads and the new item appears.
 *
 * TimePickerDialog — Android's built-in clock dialog.
 * We use it instead of a text field because it prevents invalid time inputs
 * and gives a better user experience that matches the prototype.
 */
public class AddMedicationActivity extends AppCompatActivity {

    private TextInputLayout tilMedName, tilDosage;
    private TextInputEditText etMedName, etDosage, etReminderTime, etNotes;
    private Spinner spinnerFrequency;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int selectedHour = 8, selectedMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        tilMedName       = findViewById(R.id.tilMedName);
        tilDosage        = findViewById(R.id.tilDosage);
        etMedName        = findViewById(R.id.etMedName);
        etDosage         = findViewById(R.id.etDosage);
        etReminderTime   = findViewById(R.id.etReminderTime);
        etNotes          = findViewById(R.id.etNotes);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);

        setupFrequencySpinner();
        setupTimePicker();

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        ((MaterialButton) findViewById(R.id.btnSaveMedication))
                .setOnClickListener(v -> saveMedication());
    }

    /**
     * Populates the Frequency Spinner.
     * ArrayAdapter bridges String[] to Spinner UI.
     * simple_spinner_item = collapsed display, simple_spinner_dropdown_item = expanded list.
     */
    private void setupFrequencySpinner() {
        String[] frequencies = {
                "Once daily", "Twice daily",
                "Three times daily", "Once nightly", "Every 8 hours"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, frequencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);
    }

    /**
     * Tapping the Reminder Time field shows Android's TimePickerDialog.
     * The picked time is stored in selectedHour/selectedMinute and shown in the field.
     */
    private void setupTimePicker() {
        etReminderTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedHour   = hourOfDay;
                selectedMinute = minute;
                String amPm    = hourOfDay < 12 ? "AM" : "PM";
                int h          = hourOfDay % 12;
                if (h == 0) h = 12;
                etReminderTime.setText(
                        String.format(Locale.getDefault(), "%02d:%02d %s", h, minute, amPm));
            }, selectedHour, selectedMinute, false).show();
        });
    }

    private void saveMedication() {
        String medName   = etMedName.getText().toString().trim();
        String dosage    = etDosage.getText().toString().trim();
        String frequency = spinnerFrequency.getSelectedItem().toString();
        String time      = etReminderTime.getText().toString().trim();
        String notes     = etNotes.getText().toString().trim();

        tilMedName.setError(null);
        tilDosage.setError(null);

        if (TextUtils.isEmpty(medName)) { tilMedName.setError("Name required"); return; }
        if (TextUtils.isEmpty(dosage))  { tilDosage.setError("Dosage required"); return; }

        Medication med = new Medication(
                sessionManager.getUserId(), medName, dosage, frequency, time, notes);
        long result = dbHelper.addMedication(med);

        if (result != -1) {
            Toast.makeText(this, medName + " added!", Toast.LENGTH_SHORT).show();
            finish(); // Return to MedicationListActivity
        } else {
            Toast.makeText(this, "Failed to save. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}

