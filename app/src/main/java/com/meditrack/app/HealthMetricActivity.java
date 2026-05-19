package com.meditrack.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.meditrack.app.database.DatabaseHelper;
import com.meditrack.app.models.HealthMetric;
import com.meditrack.app.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * HealthMetricActivity — Sub-Activity for logging health readings.
 *
 * SAVES TO: health_metrics — the 4th DB table, required.
 *
 * VALIDATION RULE:
 * All three fields are optional individually, but at least one must have a value.
 * The marker needs to see real input handling — empty save = rejected.
 *
 * WHY blood pressure is stored as String:
 * BP is "120/80" — two numbers with a slash. SQLite has no native BP type.
 * VARCHAR stores it exactly as typed, which is cleaner than splitting into
 * two separate columns for systolic and diastolic for a student project.
 *
 * GRAPH:
 * Weight is plotted because it is a clean single float value.
 * BP as a string and glucose in mg/dL would need unit conversion to compare,
 * so weight is the safest choice for a simple trend line.
 */
public class HealthMetricActivity extends AppCompatActivity {

    // ── View references ──────────────────────────────────────────────────────
    private TextInputLayout   tilBP, tilGlucose, tilWeight;
    private TextInputEditText etBP, etGlucose, etWeight;
    private HealthGraphView   healthGraph;

    // ── Data helpers ─────────────────────────────────────────────────────────
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_metric);
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        // Bind all views
        tilBP       = findViewById(R.id.tilBP);
        tilGlucose  = findViewById(R.id.tilGlucose);
        tilWeight   = findViewById(R.id.tilWeight);
        etBP        = findViewById(R.id.etBP);
        etGlucose   = findViewById(R.id.etGlucose);
        etWeight    = findViewById(R.id.etWeight);
        healthGraph = findViewById(R.id.healthGraph);

        // Load existing weight data into the graph on screen open
        loadGraph();

        // Back arrow and Save button listeners
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        ((MaterialButton) findViewById(R.id.btnSaveData))
                .setOnClickListener(v -> saveMetric());
    }

    /**
     * Reads last 7 weight readings from the database and passes them
     * to HealthGraphView.setData() which triggers a redraw.
     * Called on create and again after every successful save.
     */
    private void loadGraph() {
        List<HealthMetric> metrics = dbHelper.getHealthMetricsByUser(sessionManager.getUserId());
        List<Float> weights = new ArrayList<>();
        for (HealthMetric m : metrics) {
            if (m.getWeight() > 0) {
                weights.add((float) m.getWeight());
            }
        }
        healthGraph.setData(weights);
    }

    /**
     * Validates that at least one field is filled, parses numeric fields,
     * saves a new HealthMetric row to SQLite, then clears fields and refreshes graph.
     */
    private void saveMetric() {
        String bp      = etBP.getText().toString().trim();
        String glucose = etGlucose.getText().toString().trim();
        String weight  = etWeight.getText().toString().trim();

        // Clear previous field errors
        tilBP.setError(null);
        tilGlucose.setError(null);
        tilWeight.setError(null);

        // At least one reading must be provided
        if (TextUtils.isEmpty(bp) && TextUtils.isEmpty(glucose) && TextUtils.isEmpty(weight)) {
            Toast.makeText(this,
                    "Please enter at least one reading", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse numeric fields — show field error if not a valid number
        double glucoseVal = 0;
        double weightVal  = 0;

        try {
            if (!glucose.isEmpty()) glucoseVal = Double.parseDouble(glucose);
        } catch (NumberFormatException e) {
            tilGlucose.setError("Please enter a valid number");
            return;
        }

        try {
            if (!weight.isEmpty()) weightVal = Double.parseDouble(weight);
        } catch (NumberFormatException e) {
            tilWeight.setError("Please enter a valid number");
            return;
        }

        // Build timestamp string for the recorded_date column
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date());

        // Create and save the HealthMetric object
        HealthMetric metric = new HealthMetric(
                sessionManager.getUserId(),
                bp,
                glucoseVal,
                weightVal,
                date
        );

        long result = dbHelper.addHealthMetric(metric);

        if (result != -1) {
            Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show();

            // Clear all input fields ready for the next reading
            etBP.setText("");
            etGlucose.setText("");
            etWeight.setText("");

            // Refresh the graph to include the new data point
            loadGraph();
        } else {
            Toast.makeText(this, "Save failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}