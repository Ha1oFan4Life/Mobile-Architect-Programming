package com.example.weighttrackingapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

/**
 * Requests SEND_SMS permission at runtime and stores the "destination phone"
 * in SharedPreferences for emulator-friendly testing.
 *
 * If permission is denied, the app continues without SMS features.
 */
public class SmsPermissionActivity extends AppCompatActivity {

    private static final int REQ_SMS = 101;

    private TextView tvPermissionValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        tvPermissionValue = findViewById(R.id.tvPermissionValue);
        MaterialButton btn = findViewById(R.id.btnRequestSms);

        btn.setOnClickListener(v -> requestSms());
        updateStatus();

        // If you came here right after login, continue to main screen when done.
        // (User can still open this screen later via long-press on header.)
        findViewById(R.id.tvSmsTitle).setOnLongClickListener(v -> {
            finishToMain();
            return true;
        });
    }

    private void requestSms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            toast("SMS permission already granted.");
            promptForPhone();
            updateStatus();
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.SEND_SMS},
                REQ_SMS
        );
    }

    private void updateStatus() {
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        tvPermissionValue.setText(granted ? "Granted" : "Denied / Not granted");
    }

    private void promptForPhone() {
        final androidx.appcompat.widget.AppCompatEditText input =
                new androidx.appcompat.widget.AppCompatEditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setText(Session.getSmsPhone(this));

        new AlertDialog.Builder(this)
                .setTitle("SMS Destination Number")
                .setMessage("For emulator testing, enter another emulator number (e.g., 5554 or 5556).")
                .setView(input)
                .setPositiveButton("Save", (d, which) -> {
                    String phone = input.getText() == null ? "" : input.getText().toString().trim();
                    if (!phone.isEmpty()) {
                        Session.setSmsPhone(this, phone);
                    }
                    toast("Saved destination: " + Session.getSmsPhone(this));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void finishToMain() {
        // If opened from login flow, userId is in Session already.
        // Simply finishing returns to the prior screen; login finishes itself.
        if (Session.getUserId(this) != -1) {
            startActivity(new android.content.Intent(this, WeightLogActivity.class)
                    .putExtra("userId", Session.getUserId(this)));
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQ_SMS) return;

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            toast("SMS permission granted.");
            promptForPhone();
        } else {
            toast("SMS permission denied. App will still work without SMS.");
        }
        updateStatus();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
