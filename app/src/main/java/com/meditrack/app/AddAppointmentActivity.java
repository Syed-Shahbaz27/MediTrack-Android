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
 * AddAppointmentActivity — handles both ADD and EDIT appointment.
 * Same dual-mode pattern as AddMedicationActivity.
 * isEditMode = true when "appointment_id" extra is present in the Intent.
 */
public class AddAppointmentActivity extends AppCompatActivity {

    private TextInputLayout   tilDoctorName, tilClinic;
    private TextInputEditText etDoctorName, etClinic, etAptDate, etAptTime;
    private Spinner           spinnerSpecialty;
    private MaterialButton    btnSave;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private boolean isEditMode    = false;
    private int     editAptId     = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);

        dbHelper         = DatabaseHelper.getInstance(this);
        sessionManager   = new SessionManager(this);

        tilDoctorName    = findViewById(R.id.tilDoctorName);
        tilClinic        = findViewById(R.id.tilClinic);
        etDoctorName     = findViewById(R.id.etDoctorName);
        etClinic         = findViewById(R.id.etClinic);
        etAptDate        = findViewById(R.id.etAptDate);
        etAptTime        = findViewById(R.id.etAptTime);
        spinnerSpecialty = findViewById(R.id.spinnerSpecialty);
        btnSave          = findViewById(R.id.btnSaveAppointment);

        setupSpecialtySpinner();
        setupDatePicker();
        setupTimePicker();

        // Check for edit mode
        if (getIntent().hasExtra("appointment_id")) {
            isEditMode = true;
            editAptId  = getIntent().getIntExtra("appointment_id", -1);
            prefillFields();
            btnSave.setText("UPDATE APPOINTMENT");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnSave.setOnClickListener(v -> saveAppointment());
    }

    private void prefillFields() {
        etDoctorName.setText(getIntent().getStringExtra("doctor_name"));
        etClinic.setText(getIntent().getStringExtra("clinic"));
        etAptDate.setText(getIntent().getStringExtra("apt_date"));
        etAptTime.setText(getIntent().getStringExtra("apt_time"));

        String spec = getIntent().getStringExtra("specialty");
        String[] specialties = {
                "General Physician", "Cardiology", "Endocrinology",
                "Dermatology", "Orthopedics", "Neurology", "Other"
        };
        for (int i = 0; i < specialties.length; i++) {
            if (specialties[i].equals(spec)) {
                spinnerSpecialty.setSelection(i);
                break;
            }
        }
    }

    private void setupSpecialtySpinner() {
        String[] specialties = {
                "General Physician", "Cardiology", "Endocrinology",
                "Dermatology", "Orthopedics", "Neurology", "Other"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, specialties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialty.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etAptDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> etAptDate.setText(
                            String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupTimePicker() {
        etAptTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hour, minute) -> {
                String amPm = hour < 12 ? "AM" : "PM";
                int h = hour % 12;
                if (h == 0) h = 12;
                etAptTime.setText(
                        String.format(Locale.getDefault(), "%02d:%02d %s", h, minute, amPm));
            }, 10, 0, false).show();
        });
    }

    private void saveAppointment() {
        String doctor    = etDoctorName.getText().toString().trim();
        String clinic    = etClinic.getText().toString().trim();
        String date      = etAptDate.getText().toString().trim();
        String time      = etAptTime.getText().toString().trim();
        String specialty = spinnerSpecialty.getSelectedItem().toString();

        tilDoctorName.setError(null);
        tilClinic.setError(null);

        if (TextUtils.isEmpty(doctor)) { tilDoctorName.setError("Doctor name required"); return; }
        if (TextUtils.isEmpty(clinic)) { tilClinic.setError("Clinic required"); return; }
        if (TextUtils.isEmpty(date) || date.equals("Select date")) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show(); return; }

        Appointment apt = new Appointment(
                sessionManager.getUserId(), doctor, specialty, clinic, date, time, true);

        if (isEditMode) {
            apt.setAppointmentId(editAptId);
            boolean updated = dbHelper.updateAppointment(apt);
            if (updated) {
                Toast.makeText(this, "Appointment updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show();
            }
        } else {
            long result = dbHelper.addAppointment(apt);
            if (result != -1) {
                Toast.makeText(this, "Appointment saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}