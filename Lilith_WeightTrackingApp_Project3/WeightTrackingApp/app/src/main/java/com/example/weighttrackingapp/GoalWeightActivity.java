package com.example.weighttrackingapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Sets or edits goal weight for the logged-in user.
 */
public class GoalWeightActivity extends AppCompatActivity {

    private DBHelper db;
    private long userId;

    private TextInputEditText etGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_weight);

        db = new DBHelper(this);
        userId = getIntent().getLongExtra("userId", Session.getUserId(this));

        etGoal = findViewById(R.id.etGoal);
        MaterialButton btnSave = findViewById(R.id.btnSaveGoal);

        Double existing = db.getGoalForUser(userId);
        if (existing != null) {
            etGoal.setText(String.valueOf(existing));
        }

        btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String s = etGoal.getText() == null ? "" : etGoal.getText().toString().trim();
        if (s.isEmpty()) {
            toast("Please enter a goal weight.");
            return;
        }

        double goal;
        try {
            goal = Double.parseDouble(s);
        } catch (Exception e) {
            toast("Please enter a valid number.");
            return;
        }

        boolean ok = db.upsertGoal(userId, goal);
        toast(ok ? "Goal saved." : "Save failed.");
        if (ok) finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
