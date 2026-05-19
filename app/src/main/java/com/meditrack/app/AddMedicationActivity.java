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
 * AddMedicationActivity — handles both ADD (new) and EDIT (existing) medication.
 *
 * HOW IT KNOWS WHICH MODE:
 * MedicationListActivity passes extras via Intent when editing.
 * If "medication_id" extra exists → EDIT mode → pre-fill fields → UPDATE on save.
 * If no extras → ADD mode → INSERT on save.
 * This is the standard Android pattern for reusing a form screen.
 */
public class AddMedicationActivity extends AppCompatActivity {

    private TextInputLayout   tilMedName, tilDosage;
    private TextInputEditText etMedName, etDosage, etReminderTime, etNotes;
    private Spinner           spinnerFrequency;
    private MaterialButton    btnSave;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    // Edit mode state
    private boolean isEditMode  = false;
    private int     editMedId   = -1;
    private int     selectedHour = 8, selectedMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        // Bind views
        tilMedName     = findViewById(R.id.tilMedName);
        tilDosage      = findViewById(R.id.tilDosage);
        etMedName      = findViewById(R.id.etMedName);
        etDosage       = findViewById(R.id.etDosage);
        etReminderTime = findViewById(R.id.etReminderTime);
        etNotes        = findViewById(R.id.etNotes);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        btnSave        = findViewById(R.id.btnSaveMedication);

        setupFrequencySpinner();
        setupTimePicker();

        // Check if we are in EDIT mode
        // If medication_id was passed in the Intent, this is an edit
        if (getIntent().hasExtra("medication_id")) {
            isEditMode = true;
            editMedId  = getIntent().getIntExtra("medication_id", -1);
            prefillFields(); // fill the form with existing data
            btnSave.setText("UPDATE MEDICATION");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        btnSave.setOnClickListener(v -> saveMedication());
    }

    /**
     * Pre-fills form fields with existing medication data passed from the list screen.
     * Uses Intent extras because passing data between activities in Android
     * uses the Intent bundle — you cannot pass objects directly.
     */
    private void prefillFields() {
        etMedName.setText(getIntent().getStringExtra("med_name"));
        etDosage.setText(getIntent().getStringExtra("dosage"));
        etReminderTime.setText(getIntent().getStringExtra("reminder_time"));
        etNotes.setText(getIntent().getStringExtra("notes"));

        // Pre-select the correct frequency in the spinner
        String freq = getIntent().getStringExtra("frequency");
        String[] frequencies = {
                "Once daily", "Twice daily",
                "Three times daily", "Once nightly", "Every 8 hours"
        };
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i].equals(freq)) {
                spinnerFrequency.setSelection(i);
                break;
            }
        }
    }

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

    private void setupTimePicker() {
        etReminderTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedHour   = hourOfDay;
                selectedMinute = minute;
                String amPm    = hourOfDay < 12 ? "AM" : "PM";
                int h          = hourOfDay % 12;
                if (h == 0) h  = 12;
                etReminderTime.setText(
                        String.format(Locale.getDefault(), "%02d:%02d %s", h, minute, amPm));
            }, selectedHour, selectedMinute, false).show();
        });
    }

    /**
     * Handles both INSERT (add mode) and UPDATE (edit mode).
     * Same validation runs for both modes.
     * In edit mode, calls updateMedication() instead of addMedication().
     */
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

        if (isEditMode) {
            // UPDATE existing record
            med.setMedicationId(editMedId);
            boolean updated = dbHelper.updateMedication(med);
            if (updated) {
                Toast.makeText(this, medName + " updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // INSERT new record
            long result = dbHelper.addMedication(med);
            if (result != -1) {
                Toast.makeText(this, medName + " added!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}