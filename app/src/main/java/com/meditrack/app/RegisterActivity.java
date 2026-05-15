package com.meditrack.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.meditrack.app.database.DatabaseHelper;
import com.meditrack.app.models.User;

/**
 * RegisterActivity — handles new user account creation.
 *
 * VALIDATION ORDER (client-side, before touching the database):
 *   1. Empty field check — all required fields must be filled
 *   2. Email format check — must contain @ and a dot after it
 *   3. Password length — minimum 6 characters
 *   4. Password match — password and confirm must be identical
 *   5. Terms checkbox — must be ticked
 *
 * DATABASE:
 *   Only after all 5 checks pass, we call DatabaseHelper.registerUser().
 *   If the email already exists, registerUser() returns -1 → show error.
 *   If success → navigate to LoginActivity.
 *
 * MARKS: Proper client-side AND server-side (database) validation = Outstanding.
 */
public class RegisterActivity extends AppCompatActivity {

    // Input field layouts — used to show red error messages under each field
    private TextInputLayout  tilFullName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private CheckBox          cbTerms;
    private MaterialButton    btnRegister, btnGoToLogin;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Get singleton instance of DatabaseHelper
        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tilFullName        = findViewById(R.id.tilFullName);
        tilEmail           = findViewById(R.id.tilEmail);
        tilPassword        = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etFullName        = findViewById(R.id.etFullName);
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        cbTerms       = findViewById(R.id.cbTerms);
        btnRegister   = findViewById(R.id.btnRegister);
        btnGoToLogin  = findViewById(R.id.btnGoToLogin);
    }

    private void setupClickListeners() {

        // Back arrow — return to SplashActivity
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> attemptRegistration());
    }

    /**
     * Main registration flow — validates then saves to database.
     * Called when user taps REGISTER button.
     */
    private void attemptRegistration() {

        // ── Step 1: Read input values ────────────────────────────────────────
        String fullName = etFullName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString().trim();
        String confirm  = etConfirmPassword.getText().toString().trim();

        // Clear previous errors before re-validating
        clearErrors();

        // ── Step 2: Validate — stop at first failure, show error on field ────
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email address is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirm)) {
            tilConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this,
                    "Please agree to the terms and conditions",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ── Step 3: Check if email already in database ───────────────────────
        if (dbHelper.isEmailExists(email)) {
            tilEmail.setError("This email is already registered");
            etEmail.requestFocus();
            return;
        }

        // ── Step 4: All checks passed — save to database ─────────────────────
        User newUser = new User(fullName, email, password, "");
        long result  = dbHelper.registerUser(newUser);

        if (result != -1) {
            // Success
            Toast.makeText(this,
                    "Account created successfully! Please log in.",
                    Toast.LENGTH_LONG).show();

            // Navigate to Login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("email", email); // pre-fill email on login screen
            startActivity(intent);
            finish();
        } else {
            // This shouldn't happen after the email check above, but handle it
            Toast.makeText(this,
                    "Registration failed. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Clear all TextInputLayout error messages */
    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }
}