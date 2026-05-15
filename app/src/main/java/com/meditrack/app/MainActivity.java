package com.meditrack.app;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.meditrack.app.utils.SessionManager;

/** Temporary stub — replaced on Day 3 with full dashboard */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        SessionManager session = new SessionManager(this);
        tv.setText("Login successful! Welcome, " + session.getUserName());
        tv.setTextSize(20f);
        tv.setPadding(60, 200, 60, 60);
        setContentView(tv);
    }
}