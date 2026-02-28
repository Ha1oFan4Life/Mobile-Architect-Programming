package com.example.weighttrackingapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adds a daily weight entry to the database and triggers SMS alert if goal reached.
 */
public class AddWeightActivity extends AppCompatActivity {

    private DBHelper db;
    private long userId;

    private TextView tvDate;
    private TextInputEditText etWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_weight);

        db = new DBHelper(this);
        userId = getIntent().getLongExtra("userId", Session.getUserId(this));

        tvDate = findViewById(R.id.tvDate);
        etWeight = findViewById(R.id.etWeight);
        MaterialButton btnSave = findViewById(R.id.btnSaveWeight);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        tvDate.setText("Date: " + today);

        btnSave.setOnClickListener(v -> save(today));
    }

    private void save(String date) {
        String s = etWeight.getText() == null ? "" : etWeight.getText().toString().trim();
        if (s.isEmpty()) {
            toast("Please enter a weight.");
            return;
        }

        double weight;
        try {
            weight = Double.parseDouble(s);
        } catch (Exception e) {
            toast("Please enter a valid number.");
            return;
        }

        long id = db.addWeight(userId, date, weight);
        if (id == -1) {
            toast("Save failed.");
            return;
        }

        toast("Saved!");
        maybeSendGoalSms(weight);

        finish();
    }

    private void maybeSendGoalSms(double weight) {
        Double goal = db.getGoalForUser(userId);
        if (goal == null) return;

        if (weight > goal) return; // Only notify when goal reached (<=)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission denied: app still works; no SMS.
            return;
        }

        String phone = Session.getSmsPhone(this);
        String msg = "Congratulations! You reached your goal weight (" + goal + ").";

        try {
            SmsManager.getDefault().sendTextMessage(phone, null, msg, null, null);
            toast("Goal reached! SMS sent to " + phone);
        } catch (Exception e) {
            toast("Goal reached, but SMS could not be sent.");
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
