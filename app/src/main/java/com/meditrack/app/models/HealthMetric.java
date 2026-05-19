package com.meditrack.app.models;

/**
 * Model class representing one health metric reading.
 * Maps to the 'health_metrics' table — this is the 4th table
 * that pushes the database criterion into the.
 * One user can have many metric readings over time. (1-Many relationship)
 */
public class HealthMetric {

    private int metricId;
    private int userId;           // FK → users.user_id
    private String bloodPressure; // Stored as "120/80" string
    private double bloodGlucose;  // in mg/dL
    private double weight;        // in kg
    private String recordedDate;  // "dd/MM/yyyy HH:mm"

    public HealthMetric() {}

    public HealthMetric(int userId, String bloodPressure,
                        double bloodGlucose, double weight, String recordedDate) {
        this.userId = userId;
        this.bloodPressure = bloodPressure;
        this.bloodGlucose = bloodGlucose;
        this.weight = weight;
        this.recordedDate = recordedDate;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public int getMetricId() { return metricId; }
    public void setMetricId(int metricId) { this.metricId = metricId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }

    public double getBloodGlucose() { return bloodGlucose; }
    public void setBloodGlucose(double bloodGlucose) { this.bloodGlucose = bloodGlucose; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getRecordedDate() { return recordedDate; }
    public void setRecordedDate(String recordedDate) { this.recordedDate = recordedDate; }
}