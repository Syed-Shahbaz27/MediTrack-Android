package com.meditrack.app;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * TeamMembersActivity — displays developer information.
 * Required by assignment: "A Team Members button that, on click,
 * should display your name, student id and email in another activity."
 * No database needed — purely static display screen.
 */
public class TeamMembersActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_members);
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }
}
