package com.meditrack.app.models;

/**
 * Model class representing a medical appointment.
 * Maps to the 'appointments' table in SQLite.
 * One user can have many appointments (1-to-many via user_id FK = Foreign Key).
 */
public class Appointment {

    private int appointmentId;
    private int userId;             // FK → users.user_id
    private String doctorName;
    private String specialty;       // e.g. "Cardiology", "General Physician"
    private String clinicLocation;
    private String appointmentDate; // Stored as "dd/MM/yyyy" string
    private String appointmentTime; // Stored as "HH:mm" string
    private boolean reminderEnabled;

    public Appointment() {}

    public Appointment(int userId, String doctorName, String specialty,
                       String clinicLocation, String appointmentDate,
                       String appointmentTime, boolean reminderEnabled) {
        this.userId = userId;
        this.doctorName = doctorName;
        this.specialty = specialty;
        this.clinicLocation = clinicLocation;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reminderEnabled = reminderEnabled;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getClinicLocation() { return clinicLocation; }
    public void setClinicLocation(String clinicLocation) { this.clinicLocation = clinicLocation; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    /**
     * Returns display name e.g. "Dr Suraj Cariology"
     */
    public String getDisplayName() {
        return doctorName + " " + specialty;
    }
}