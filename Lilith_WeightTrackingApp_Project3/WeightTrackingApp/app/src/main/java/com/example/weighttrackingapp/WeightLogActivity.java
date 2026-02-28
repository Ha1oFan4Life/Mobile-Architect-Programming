package com.example.weighttrackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Displays weights in a grid/list (RecyclerView) and supports CRUD.
 */
public class WeightLogActivity extends AppCompatActivity implements WeightAdapter.Listener {

    private DBHelper db;
    private long userId;

    private TextView tvGoalValue;
    private WeightAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_log);

        db = new DBHelper(this);
        userId = getIntent().getLongExtra("userId", Session.getUserId(this));
        if (userId == -1) userId = Session.getUserId(this);

        tvGoalValue = findViewById(R.id.tvGoalValue);

        RecyclerView rv = findViewById(R.id.rvWeights);
        rv.setLayoutManager(new GridLayoutManager(this, 1));
        adapter = new WeightAdapter(this);
        rv.setAdapter(adapter);

        MaterialButton btnAdd = findViewById(R.id.btnAddWeight);
        MaterialButton btnGoal = findViewById(R.id.btnSetGoal);

        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(this, AddWeightActivity.class);
            i.putExtra("userId", userId);
            startActivity(i);
        });

        btnGoal.setOnClickListener(v -> {
            Intent i = new Intent(this, GoalWeightActivity.class);
            i.putExtra("userId", userId);
            startActivity(i);
        });

        // Long-press header to open SMS settings (keeps UI minimal but still accessible).
        findViewById(R.id.tvHeader).setOnLongClickListener(v -> {
            Intent i = new Intent(this, SmsPermissionActivity.class);
            i.putExtra("userId", userId);
            startActivity(i);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        Double goal = db.getGoalForUser(userId);
        tvGoalValue.setText(goal == null ? "—" : String.valueOf(goal));

        List<WeightEntry> weights = db.getAllWeightsForUser(userId);
        adapter.setItems(weights);
    }

    @Override
    public void onEdit(WeightEntry entry) {
        TextInputEditText input = new TextInputEditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(entry.weight));

        new AlertDialog.Builder(this)
                .setTitle("Update Weight")
                .setMessage("Edit the weight value for " + entry.date)
                .setView(input)
                .setPositiveButton("Save", (d, which) -> {
                    String s = input.getText() == null ? "" : input.getText().toString().trim();
                    try {
                        double val = Double.parseDouble(s);
                        boolean ok = db.updateWeight(entry.id, val);
                        toast(ok ? "Updated." : "Update failed.");
                        refresh();
                    } catch (Exception e) {
                        toast("Please enter a valid number.");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(WeightEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Delete " + entry.date + " (" + entry.weight + ")?")
                .setPositiveButton("Delete", (d, which) -> {
                    boolean ok = db.deleteWeight(entry.id);
                    toast(ok ? "Deleted." : "Delete failed.");
                    refresh();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
