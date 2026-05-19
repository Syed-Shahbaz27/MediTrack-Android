package com.meditrack.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.meditrack.app.database.DatabaseHelper;
import com.meditrack.app.models.User;
import com.meditrack.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * LoginActivity — authenticates returning users.
 *
 * FLOW:
 *   1. User enters email + password → taps LOG IN.
 *   2. Validate fields are not empty.
 *   3. Call DatabaseHelper.validateLogin() — checks against SQLite.
 *   4. If valid:
 *      a. Update last_login timestamp in database.
 *      b. Save session to SharedPreferences via SessionManager.
 *      c. Navigate to MainActivity, clear back stack.
 *   5. If invalid → show error on email field.
 *
 * REMEMBER ME:
 *   Saves the checkbox state. On next app launch SplashActivity reads
 *   session.isLoggedIn() — if true it skips straight to MainActivity.
 *   That IS the Remember Me behaviour in a local SQLite app.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout   tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private CheckBox          cbRememberMe;
    private MaterialButton    btnLogin, btnGoToRegister;
    private TextView          tvForgotPassword;

    private DatabaseHelper  dbHelper;
    private SessionManager  sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        initViews();
        restoreRememberMe();    // Pre-fill email if Remember Me was checked before
        checkPrefilledEmail();  // Pre-fill email if coming from RegisterActivity
        setupClickListeners();
    }

    private void initViews() {
        tilEmail         = findViewById(R.id.tilEmail);
        tilPassword      = findViewById(R.id.tilPassword);
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        cbRememberMe     = findViewById(R.id.cbRememberMe);
        btnLogin         = findViewById(R.id.btnLogin);
        btnGoToRegister  = findViewById(R.id.btnGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    /**
     * If user previously checked Remember Me, restore their email.
     * This gives the "Remember Me" feel — they don't need to re-type email.
     */
    private void restoreRememberMe() {
        if (sessionManager.isRememberMe()) {
            etEmail.setText(sessionManager.getUserEmail());
            cbRememberMe.setChecked(true);
        }
    }

    /**
     * RegisterActivity passes the email via Intent extra so user doesn't
     * have to re-type it after registering.
     */
    private void checkPrefilledEmail() {
        String prefilledEmail = getIntent().getStringExtra("email");
        if (prefilledEmail != null && !prefilledEmail.isEmpty()) {
            etEmail.setText(prefilledEmail);
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    /**
     * Main login flow.
     */
    private void attemptLogin() {
        String email    = etEmail.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString().trim();

        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // ── Validate ─────────────────────────────────────────────────────────
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // ── Database check ───────────────────────────────────────────────────
        User user = dbHelper.validateLogin(email, password);

        if (user != null) {
            // ── SUCCESS ──────────────────────────────────────────────────────

            // 1. Get current timestamp for "last login" display
            String currentTime = new SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a", Locale.getDefault()
            ).format(new Date());

            // 2. Update last_login in the database
            dbHelper.updateLastLogin(user.getUserId(), currentTime);

            // 3. Save session to SharedPreferences
            sessionManager.saveSession(
                    user.getUserId(),
                    user.getFullName(),
                    user.getEmail(),
                    currentTime
            );

            // 4. Save Remember Me state
            sessionManager.setRememberMe(cbRememberMe.isChecked());

            // 5. Navigate to MainActivity — clear back stack so Back exits the app
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } else {
            // ── FAILURE ───────────────────────────────────────────────────────
            tilEmail.setError("Invalid email or password");
            tilPassword.setError("Invalid email or password");
            etPassword.setText("");
            etEmail.requestFocus();
        }
    }
}
