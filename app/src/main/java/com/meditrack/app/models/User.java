package com.meditrack.app.models;

/**
 * Model class representing a registered user in our MediTrack app.
 * Follows MVC pattern  this is the M (Model).
 * Maps directly to the 'users' table in SQLite.
 */
public class User {

    // Fields match the database columns exactly
    private int userId;
    private String fullName;
    private String email;
    private String password;
    private String emergencyContact;
    private String lastLogin;

    // Default constructor — needed by DatabaseHelper when building from Cursor
    public User() {}

    // Constructor for creating a new user during registration
    public User(String fullName, String email, String password, String emergencyContact) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.emergencyContact = emergencyContact;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name=" + fullName + ", email=" + email + "}";
    }
}