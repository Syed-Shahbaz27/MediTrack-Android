package com.meditrack.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.meditrack.app.database.DatabaseHelper;
import com.meditrack.app.models.Medication;
import com.meditrack.app.utils.SessionManager;

import java.util.List;

/**
 * MedicationListActivity — Group Activity showing all user medications.
 *
 * WHAT IS A GROUP ACTIVITY?
 * The assignment calls screens that show a collection of data "Group Activities."
 * This screen shows all medications as a scrollable list.
 *
 * WHAT IS RecyclerView?
 * RecyclerView is more efficient than ListView because it reuses ("recycles")
 * the view objects as you scroll. When a row scrolls off screen, its View is
 * not destroyed — it is filled with new data and shown at the other end.
 * For a potentially long medication list, this saves memory and is smoother.
 *
 * CRUD operations here:
 *   READ — onResume() loads all meds from DB
 *   DELETE — long-press shows dialog, confirmed delete removes from DB + list
 *   CREATE — FAB launches AddMedicationActivity (which handles INSERT)
 */
public class MedicationListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<Medication> medicationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_list);

        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        recyclerView   = findViewById(R.id.recyclerMedications);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAddMedication);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddMedicationActivity.class)));

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time this screen becomes visible
        // so new medications added in AddMedicationActivity appear instantly
        medicationList = dbHelper.getMedicationsByUser(sessionManager.getUserId());
        recyclerView.setAdapter(new MedicationAdapter());
    }

    // ── Inner Adapter class ────────────────────────────────────────────────

    /**
     * MedicationAdapter bridges List<Medication> data to item_medication.xml views.
     *
     * THREE METHODS EVERY ADAPTER MUST HAVE:
     * onCreateViewHolder — inflate the XML layout once per visible slot
     * onBindViewHolder — fill the inflated view with data at a given position
     * getItemCount — tell RecyclerView how many items total
     *
     * ViewHolder caches the findViewById results so we don't call it repeatedly.
     */
    class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_medication, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Medication med = medicationList.get(position);
            holder.tvMedName.setText(med.getMedName());
            holder.tvMedDetails.setText(med.getDosage() + " • " + med.getFrequency());
            holder.tvMedTime.setText(med.getReminderTime());

            // Long-press → confirm delete dialog
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(MedicationListActivity.this)
                        .setTitle("Delete Medication")
                        .setMessage("Delete " + med.getMedName() + "?")
                        .setPositiveButton("Delete", (d, w) -> {
                            dbHelper.deleteMedication(med.getMedicationId());
                            int pos = holder.getAdapterPosition();
                            medicationList.remove(pos);
                            notifyItemRemoved(pos);
                            Toast.makeText(MedicationListActivity.this,
                                    med.getMedName() + " deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() { return medicationList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMedName, tvMedDetails, tvMedTime;
            ViewHolder(@NonNull View v) {
                super(v);
                tvMedName    = v.findViewById(R.id.tvMedName);
                tvMedDetails = v.findViewById(R.id.tvMedDetails);
                tvMedTime    = v.findViewById(R.id.tvMedTime);
            }
        }
    }
}

