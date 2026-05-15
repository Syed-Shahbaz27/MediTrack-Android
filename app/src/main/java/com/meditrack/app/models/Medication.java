package com.meditrack.app.models;

/**
 * Model class representing a medication entry.
 * Maps to the 'medications' table in SQLite.
 * Has a foreign key relationship to User (one user → many medications).
 */
public class Medication {

    private int medicationId;
    private int userId;          // FK → users.user_id
    private String medName;
    private String dosage;
    private String frequency;
    private String reminderTime; // Stored as "HH:mm" string e.g. "08:00"
    private String notes;

    public Medication() {}

    public Medication(int userId, String medName, String dosage,
                      String frequency, String reminderTime, String notes) {
        this.userId = userId;
        this.medName = medName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.reminderTime = reminderTime;
        this.notes = notes;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public int getMedicationId() { return medicationId; }
    public void setMedicationId(int medicationId) { this.medicationId = medicationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getMedName() { return medName; }
    public void setMedName(String medName) { this.medName = medName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /**
     * Returns display-friendly string e.g. "Metformin 500mg Twice Daily"
     */
    public String getDisplayName() {
        return medName + " " + dosage + " " + frequency;
    }
}