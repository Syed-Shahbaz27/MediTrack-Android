package com.meditrack.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * ContactUsActivity — lets users send a message to the MediTrack team.
 *
 * WHY Intent.ACTION_SENDTO instead of sending email directly:
 * Sending email programmatically requires an SMTP server and credentials.
 * Intent.ACTION_SENDTO delegates to the device's installed email app (Gmail, Outlook etc.)
 * which already has the user's credentials. No server needed, no API keys needed.
 * This is the correct Android approach for contact forms.
 */
public class ContactUsActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilMessage;
    private TextInputEditText etName, etEmail, etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        tilName    = findViewById(R.id.tilName);
        tilEmail   = findViewById(R.id.tilEmail);
        tilMessage = findViewById(R.id.tilMessage);
        etName     = findViewById(R.id.etName);
        etEmail    = findViewById(R.id.etEmail);
        etMessage  = findViewById(R.id.etMessage);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        ((MaterialButton) findViewById(R.id.btnSend)).setOnClickListener(v -> sendEmail());
    }

    private void sendEmail() {
        String name    = etName.getText().toString().trim();
        String email   = etEmail.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        tilName.setError(null);
        tilEmail.setError(null);
        tilMessage.setError(null);

        if (TextUtils.isEmpty(name))    { tilName.setError("Name required"); return; }
        if (TextUtils.isEmpty(email))   { tilEmail.setError("Email required"); return; }
        if (TextUtils.isEmpty(message)) { tilMessage.setError("Message required"); return; }

        // Build the email intent — opens device email app pre-filled
        String subject = "MediTrack Contact — " + name;
        String body    = "From: " + name + " (" + email + ")\n\n" + message;

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:meditrack.support@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            startActivity(Intent.createChooser(intent, "Send via..."));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "No email app installed on this device.", Toast.LENGTH_LONG).show();
        }
    }
}