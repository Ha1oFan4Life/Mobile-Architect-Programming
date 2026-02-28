package com.example.weighttrackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Login screen:
 *  - Log in: validates against users table
 *  - Create: inserts new user row
 */
public class LoginActivity extends AppCompatActivity {

    private DBHelper db;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DBHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnCreate = findViewById(R.id.btnCreateAccount);

        btnLogin.setOnClickListener(v -> doLogin());
        btnCreate.setOnClickListener(v -> doCreate());
    }

    private void doLogin() {
        String u = safeText(etUsername);
        String p = safeText(etPassword);

        if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
            toast("Please enter a username and password.");
            return;
        }

        long userId = db.login(u, p);
        if (userId == -1) {
            toast("Login failed. Check your password or create an account.");
            return;
        }

        onAuthSuccess(userId);
    }

    private void doCreate() {
        String u = safeText(etUsername);
        String p = safeText(etPassword);

        if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
            toast("Please enter a username and password.");
            return;
        }

        long userId = db.createUser(u, p);
        if (userId == -1) {
            toast("That username already exists. Try logging in.");
            return;
        }

        toast("Account created!");
        onAuthSuccess(userId);
    }

    private void onAuthSuccess(long userId) {
        Session.setUserId(this, userId);

        // First-time SMS permission prompt
        if (!Session.hasAskedSms(this)) {
            Session.setAskedSms(this, true);
            Intent i = new Intent(this, SmsPermissionActivity.class);
            i.putExtra("userId", userId);
            startActivity(i);
            finish();
            return;
        }

        Intent i = new Intent(this, WeightLogActivity.class);
        i.putExtra("userId", userId);
        startActivity(i);
        finish();
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
