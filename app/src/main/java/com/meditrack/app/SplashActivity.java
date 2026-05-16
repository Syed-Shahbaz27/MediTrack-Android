/**
 * SplashActivity — the Welcome/Landing screen. The app's entry point.
 *
 * FLOW LOGIC:
 *   1. User opens the app.
 *   2. We immediately check if they're already logged in (SessionManager).
 *   3. If YES → skip this screen, go straight to MainActivity.
 *   4. If NO → show this welcome screen with LOG IN and REGISTER buttons.
 *
 * WHY THIS IS THE LAUNCHER:
 * The AndroidManifest declares this as MAIN/LAUNCHER — it's the first
 * thing Android starts when the app icon is tapped.
 */
package com.meditrack.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.meditrack.app.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            goToMain();
            return;
        }

        // Show the welcome screen
        setContentView(R.layout.activity_splash);
        setupButtons();
    }

    private void setupButtons() {
        MaterialButton btnLogin = findViewById(R.id.btnGoToLogin);
        MaterialButton btnRegister = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

    /**
     * Navigate to the Main Dashboard and remove SplashActivity from back stack.
     * finish() means pressing Back from MainActivity will EXIT the app, not return here.
     */


