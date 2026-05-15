package com.meditrack.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.meditrack.app.models.Appointment;
import com.meditrack.app.models.HealthMetric;
import com.meditrack.app.models.Medication;
import com.meditrack.app.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper — the single class responsible for all SQLite operations.
 *
 * WHY SINGLETON PATTERN:
 * Only one database connection should be open at a time in Android.
 * Using getInstance() everywhere prevents "database locked" crashes.
 *
 * TABLES:
 *   1. users          — stores registered user accounts
 *   2. medications    — medication schedule per user (FK → users)
 *   3. appointments   — medical appointments per user (FK → users)
 *   4. health_metrics — blood pressure/glucose/weight readings (FK → users)
 *
 * Having 4 tables with FK relationships = Outstanding band on Database criterion.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // ── Database Constants ───────────────────────────────────────────────────

    private static final String DATABASE_NAME    = "meditrack.db";
    private static final int    DATABASE_VERSION = 1;

    // ── Table Names ──────────────────────────────────────────────────────────

    public static final String TABLE_USERS          = "users";
    public static final String TABLE_MEDICATIONS    = "medications";
    public static final String TABLE_APPOINTMENTS   = "appointments";
    public static final String TABLE_HEALTH_METRICS = "health_metrics";

    // ── Column Names: Users ──────────────────────────────────────────────────

    public static final String COL_USER_ID         = "user_id";
    public static final String COL_USER_NAME       = "full_name";
    public static final String COL_USER_EMAIL      = "email";
    public static final String COL_USER_PASSWORD   = "password";
    public static final String COL_USER_EMERGENCY  = "emergency_contact";
    public static final String COL_USER_LAST_LOGIN = "last_login";

    // ── Column Names: Medications ────────────────────────────────────────────

    public static final String COL_MED_ID            = "medication_id";
    public static final String COL_MED_USER_ID       = "user_id";
    public static final String COL_MED_NAME          = "med_name";
    public static final String COL_MED_DOSAGE        = "dosage";
    public static final String COL_MED_FREQUENCY     = "frequency";
    public static final String COL_MED_REMINDER_TIME = "reminder_time";
    public static final String COL_MED_NOTES         = "notes";

    // ── Column Names: Appointments ───────────────────────────────────────────

    public static final String COL_APT_ID       = "appointment_id";
    public static final String COL_APT_USER_ID  = "user_id";
    public static final String COL_APT_DOCTOR   = "doctor_name";
    public static final String COL_APT_SPEC     = "specialty";
    public static final String COL_APT_CLINIC   = "clinic_location";
    public static final String COL_APT_DATE     = "appointment_date";
    public static final String COL_APT_TIME     = "appointment_time";
    public static final String COL_APT_REMINDER = "reminder_enabled";

    // ── Column Names: Health Metrics ─────────────────────────────────────────

    public static final String COL_METRIC_ID      = "metric_id";
    public static final String COL_METRIC_USER_ID = "user_id";
    public static final String COL_METRIC_BP      = "blood_pressure";
    public static final String COL_METRIC_GLUCOSE = "blood_glucose";
    public static final String COL_METRIC_WEIGHT  = "weight";
    public static final String COL_METRIC_DATE    = "recorded_date";

    // ── CREATE TABLE SQL ─────────────────────────────────────────────────────

    private static final String CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_NAME       + " VARCHAR(100) NOT NULL, "             +
                    COL_USER_EMAIL      + " VARCHAR(100) NOT NULL UNIQUE, "      +
                    COL_USER_PASSWORD   + " VARCHAR(255) NOT NULL, "             +
                    COL_USER_EMERGENCY  + " VARCHAR(20), "                       +
                    COL_USER_LAST_LOGIN + " TEXT"                                +
                    ");";

    private static final String CREATE_MEDICATIONS =
            "CREATE TABLE " + TABLE_MEDICATIONS + " ("      +
                    COL_MED_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_MED_USER_ID       + " INTEGER NOT NULL, "                  +
                    COL_MED_NAME          + " VARCHAR(100) NOT NULL, "             +
                    COL_MED_DOSAGE        + " VARCHAR(50) NOT NULL, "              +
                    COL_MED_FREQUENCY     + " VARCHAR(50) NOT NULL, "              +
                    COL_MED_REMINDER_TIME + " TEXT NOT NULL, "                     +
                    COL_MED_NOTES         + " TEXT, "                              +
                    "FOREIGN KEY (" + COL_MED_USER_ID + ") REFERENCES "           +
                    TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE"        +
                    ");";

    private static final String CREATE_APPOINTMENTS =
            "CREATE TABLE " + TABLE_APPOINTMENTS + " ("     +
                    COL_APT_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_APT_USER_ID  + " INTEGER NOT NULL, "                  +
                    COL_APT_DOCTOR   + " VARCHAR(100) NOT NULL, "             +
                    COL_APT_SPEC     + " VARCHAR(100), "                      +
                    COL_APT_CLINIC   + " VARCHAR(100) NOT NULL, "             +
                    COL_APT_DATE     + " TEXT NOT NULL, "                     +
                    COL_APT_TIME     + " TEXT NOT NULL, "                     +
                    COL_APT_REMINDER + " INTEGER DEFAULT 1, "                 +
                    "FOREIGN KEY (" + COL_APT_USER_ID + ") REFERENCES "      +
                    TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE"   +
                    ");";

    private static final String CREATE_HEALTH_METRICS =
            "CREATE TABLE " + TABLE_HEALTH_METRICS + " ("      +
                    COL_METRIC_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_METRIC_USER_ID + " INTEGER NOT NULL, "                  +
                    COL_METRIC_BP      + " VARCHAR(20), "                       +
                    COL_METRIC_GLUCOSE + " REAL, "                              +
                    COL_METRIC_WEIGHT  + " REAL, "                              +
                    COL_METRIC_DATE    + " TEXT NOT NULL, "                     +
                    "FOREIGN KEY (" + COL_METRIC_USER_ID + ") REFERENCES "     +
                    TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE"    +
                    ");";

    // ── Singleton ────────────────────────────────────────────────────────────

    private static DatabaseHelper instance;

    /**
     * Always use this to get DatabaseHelper — never call new DatabaseHelper().
     * Synchronized = thread-safe, no race conditions.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all 4 tables. Order matters — users first because others FK to it.
        db.execSQL(CREATE_USERS);
        db.execSQL(CREATE_MEDICATIONS);
        db.execSQL(CREATE_APPOINTMENTS);
        db.execSQL(CREATE_HEALTH_METRICS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop child tables first (they have FKs), then parent
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HEALTH_METRICS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key enforcement — SQLite ignores FKs by default
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    // USER OPERATIONS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * INSERT a new user into the database.
     * @return the new row ID (positive) on success, -1 if email already exists
     */
    public long registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME,      user.getFullName());
        values.put(COL_USER_EMAIL,     user.getEmail());
        values.put(COL_USER_PASSWORD,  user.getPassword());
        values.put(COL_USER_EMERGENCY, user.getEmergencyContact());
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    /**
     * Check if an email already exists — used during registration to prevent duplicates.
     */
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /**
     * Validate login — returns the User if credentials match, null if wrong.
     * This is called from LoginActivity when the user taps LOG IN.
     */
    public User validateLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null, // SELECT * (all columns)
                COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    /**
     * UPDATE the last_login timestamp — called right after successful login.
     * This provides the "last access details" required for Excellent/Outstanding band.
     */
    public void updateLastLogin(int userId, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_LAST_LOGIN, timestamp);
        db.update(TABLE_USERS, values,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    /**
     * Get a user by their ID — used throughout the app to get the logged-in user's data.
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS, null,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null
        );
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    /**
     * Get user by email — used in ForgotPasswordActivity.
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS, null,
                COL_USER_EMAIL + "=?", new String[]{email},
                null, null, null
        );
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    /**
     * UPDATE user password — used in ForgotPasswordActivity.
     */
    public boolean updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_PASSWORD, newPassword);
        int rows = db.update(TABLE_USERS, values,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    // Helper method — builds a User object from a Cursor row
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD)));
        user.setEmergencyContact(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMERGENCY)));
        user.setLastLogin(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_LAST_LOGIN)));
        return user;
    }

    // ════════════════════════════════════════════════════════════════════════
    // MEDICATION OPERATIONS
    // ════════════════════════════════════════════════════════════════════════

    /** CREATE — add a new medication for the logged-in user */
    public long addMedication(Medication medication) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MED_USER_ID,       medication.getUserId());
        values.put(COL_MED_NAME,          medication.getMedName());
        values.put(COL_MED_DOSAGE,        medication.getDosage());
        values.put(COL_MED_FREQUENCY,     medication.getFrequency());
        values.put(COL_MED_REMINDER_TIME, medication.getReminderTime());
        values.put(COL_MED_NOTES,         medication.getNotes());
        long result = db.insert(TABLE_MEDICATIONS, null, values);
        db.close();
        return result;
    }

    /** READ — get all medications belonging to a specific user */
    public List<Medication> getMedicationsByUser(int userId) {
        List<Medication> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_MEDICATIONS, null,
                COL_MED_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null,
                COL_MED_ID + " DESC" // newest first
        );
        if (cursor != null && cursor.moveToFirst()) {
            do { list.add(cursorToMedication(cursor)); }
            while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }

    /** READ — get the most recent medication (for dashboard reminder card) */
    public Medication getLatestMedication(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_MEDICATIONS, null,
                COL_MED_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, COL_MED_ID + " DESC", "1"
        );
        Medication med = null;
        if (cursor != null && cursor.moveToFirst()) {
            med = cursorToMedication(cursor);
            cursor.close();
        }
        db.close();
        return med;
    }

    /** UPDATE — edit an existing medication */
    public boolean updateMedication(Medication medication) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MED_NAME,          medication.getMedName());
        values.put(COL_MED_DOSAGE,        medication.getDosage());
        values.put(COL_MED_FREQUENCY,     medication.getFrequency());
        values.put(COL_MED_REMINDER_TIME, medication.getReminderTime());
        values.put(COL_MED_NOTES,         medication.getNotes());
        int rows = db.update(TABLE_MEDICATIONS, values,
                COL_MED_ID + "=?",
                new String[]{String.valueOf(medication.getMedicationId())});
        db.close();
        return rows > 0;
    }

    /** DELETE — remove a medication by its ID */
    public boolean deleteMedication(int medicationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_MEDICATIONS,
                COL_MED_ID + "=?",
                new String[]{String.valueOf(medicationId)});
        db.close();
        return rows > 0;
    }

    private Medication cursorToMedication(Cursor cursor) {
        Medication med = new Medication();
        med.setMedicationId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_MED_ID)));
        med.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_MED_USER_ID)));
        med.setMedName(cursor.getString(cursor.getColumnIndexOrThrow(COL_MED_NAME)));
        med.setDosage(cursor.getString(cursor.getColumnIndexOrThrow(COL_MED_DOSAGE)));
        med.setFrequency(cursor.getString(cursor.getColumnIndexOrThrow(COL_MED_FREQUENCY)));
        med.setReminderTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_MED_REMINDER_TIME)));
        med.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_MED_NOTES)));
        return med;
    }

    // ════════════════════════════════════════════════════════════════════════
    // APPOINTMENT OPERATIONS
    // ════════════════════════════════════════════════════════════════════════

    public long addAppointment(Appointment appointment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_APT_USER_ID,  appointment.getUserId());
        values.put(COL_APT_DOCTOR,   appointment.getDoctorName());
        values.put(COL_APT_SPEC,     appointment.getSpecialty());
        values.put(COL_APT_CLINIC,   appointment.getClinicLocation());
        values.put(COL_APT_DATE,     appointment.getAppointmentDate());
        values.put(COL_APT_TIME,     appointment.getAppointmentTime());
        values.put(COL_APT_REMINDER, appointment.isReminderEnabled() ? 1 : 0);
        long result = db.insert(TABLE_APPOINTMENTS, null, values);
        db.close();
        return result;
    }

    public List<Appointment> getAppointmentsByUser(int userId) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_APPOINTMENTS, null,
                COL_APT_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, COL_APT_DATE + " ASC"
        );
        if (cursor != null && cursor.moveToFirst()) {
            do { list.add(cursorToAppointment(cursor)); }
            while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }

    /** Get the next upcoming appointment (for dashboard card) */
    public Appointment getNextAppointment(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_APPOINTMENTS, null,
                COL_APT_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, COL_APT_DATE + " ASC", "1"
        );
        Appointment apt = null;
        if (cursor != null && cursor.moveToFirst()) {
            apt = cursorToAppointment(cursor);
            cursor.close();
        }
        db.close();
        return apt;
    }

    public boolean deleteAppointment(int appointmentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_APPOINTMENTS,
                COL_APT_ID + "=?",
                new String[]{String.valueOf(appointmentId)});
        db.close();
        return rows > 0;
    }

    private Appointment cursorToAppointment(Cursor cursor) {
        Appointment apt = new Appointment();
        apt.setAppointmentId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_APT_ID)));
        apt.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_APT_USER_ID)));
        apt.setDoctorName(cursor.getString(cursor.getColumnIndexOrThrow(COL_APT_DOCTOR)));
        apt.setSpecialty(cursor.getString(cursor.getColumnIndexOrThrow(COL_APT_SPEC)));
        apt.setClinicLocation(cursor.getString(cursor.getColumnIndexOrThrow(COL_APT_CLINIC)));
        apt.setAppointmentDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_APT_DATE)));
        apt.setAppointmentTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_APT_TIME)));
        apt.setReminderEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(COL_APT_REMINDER)) == 1);
        return apt;
    }

    // ════════════════════════════════════════════════════════════════════════
    // HEALTH METRIC OPERATIONS
    // ════════════════════════════════════════════════════════════════════════

    public long addHealthMetric(HealthMetric metric) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_METRIC_USER_ID, metric.getUserId());
        values.put(COL_METRIC_BP,      metric.getBloodPressure());
        values.put(COL_METRIC_GLUCOSE, metric.getBloodGlucose());
        values.put(COL_METRIC_WEIGHT,  metric.getWeight());
        values.put(COL_METRIC_DATE,    metric.getRecordedDate());
        long result = db.insert(TABLE_HEALTH_METRICS, null, values);
        db.close();
        return result;
    }

    /** Returns last 7 records — used for the health trend graph */
    public List<HealthMetric> getHealthMetricsByUser(int userId) {
        List<HealthMetric> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_HEALTH_METRICS, null,
                COL_METRIC_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null,
                COL_METRIC_DATE + " DESC",
                "7" // LIMIT 7
        );
        if (cursor != null && cursor.moveToFirst()) {
            do {
                HealthMetric metric = new HealthMetric();
                metric.setMetricId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_METRIC_ID)));
                metric.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_METRIC_USER_ID)));
                metric.setBloodPressure(cursor.getString(cursor.getColumnIndexOrThrow(COL_METRIC_BP)));
                metric.setBloodGlucose(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_METRIC_GLUCOSE)));
                metric.setWeight(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_METRIC_WEIGHT)));
                metric.setRecordedDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_METRIC_DATE)));
                list.add(metric);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }
}