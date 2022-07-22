package com.example.wrk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseUser;
import com.parse.ParseException;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegisterClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvRegisterClick = findViewById(R.id.tvRegisterClick);
        tvRegisterClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username, password);
            }
        });
        etUsername.setTranslationX(300);
        etPassword.setTranslationX(300);
        btnLogin.setTranslationX(300);

        etUsername.setAlpha(0);
        etPassword.setAlpha(0);
        btnLogin.setAlpha(0);

        etUsername.animate().translationX(0).alpha(1).setDuration(1000).setStartDelay(300).start();
        etPassword.animate().translationX(0).alpha(1).setDuration(1000).setStartDelay(500).start();
        btnLogin.animate().translationX(0).alpha(1).setDuration(1000).setStartDelay(700).start();
    }

    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, com.parse.ParseException e) {
                if (e != null) {
                    Toast.makeText(LoginActivity.this, "Invalid login. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }
                goMainActivity();
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}