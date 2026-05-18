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
import com.meditrack.app.models.Appointment;
import com.meditrack.app.utils.SessionManager;

import java.util.List;

/**
 * AppointmentActivity — Group Activity showing all appointments.
 * Identical pattern to MedicationListActivity.
 * RecyclerView + Adapter + FAB + long-press delete.
 */
public class AppointmentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<Appointment> appointmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        dbHelper       = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        recyclerView   = findViewById(R.id.recyclerAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ((FloatingActionButton) findViewById(R.id.fabAddAppointment))
                .setOnClickListener(v -> startActivity(new Intent(this, AddAppointmentActivity.class)));

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onResume() {
        super.onResume();
        appointmentList = dbHelper.getAppointmentsByUser(sessionManager.getUserId());
        recyclerView.setAdapter(new AppointmentAdapter());
    }

    class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_appointment, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Appointment apt = appointmentList.get(position);
            holder.tvDoctorName.setText("Dr. " + apt.getDoctorName());
            holder.tvSpecialty.setText(apt.getSpecialty());
            holder.tvDateTime.setText(apt.getAppointmentDate() + " • " + apt.getAppointmentTime());

            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(AppointmentActivity.this)
                        .setTitle("Delete Appointment")
                        .setMessage("Delete appointment with Dr. " + apt.getDoctorName() + "?")
                        .setPositiveButton("Delete", (d, w) -> {
                            dbHelper.deleteAppointment(apt.getAppointmentId());
                            int pos = holder.getAdapterPosition();
                            appointmentList.remove(pos);
                            notifyItemRemoved(pos);
                            Toast.makeText(AppointmentActivity.this,
                                    "Appointment deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null).show();
                return true;
            });
        }

        @Override
        public int getItemCount() { return appointmentList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDoctorName, tvSpecialty, tvDateTime;
            ViewHolder(@NonNull View v) {
                super(v);
                tvDoctorName = v.findViewById(R.id.tvDoctorName);
                tvSpecialty  = v.findViewById(R.id.tvSpecialty);
                tvDateTime   = v.findViewById(R.id.tvAptDateTime);
            }
        }
    }
}

