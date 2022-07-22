package com.example.wrk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private EditText etRegName;
    private EditText etRegUsername;
    private EditText etRegPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegName = findViewById(R.id.etRegName);
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etRegName.getText().toString();
                String username = etRegUsername.getText().toString();
                String password = etRegPassword.getText().toString();

                ParseUser user = new ParseUser();
                user.put("name", name);
                user.setUsername(username);
                user.setPassword(password);
                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e("RegisterActivity", "Error signing up", e);
                            Toast.makeText(RegisterActivity.this, "Error Signing Up", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
                // assign following list with new user so they will see their posts at top of feed when created
                List<ParseUser> list = new ArrayList<>();
                list.add(user);
                user.put("following", list);
                user.saveInBackground();
            }
        });
        etRegName.setTranslationX(300);
        etRegUsername.setTranslationX(300);
        etRegPassword.setTranslationX(300);
        btnRegister.setTranslationX(300);

        etRegName.setAlpha(0);
        etRegUsername.setAlpha(0);
        etRegPassword.setAlpha(0);
        btnRegister.setAlpha(0);

        etRegName.animate().translationX(0).alpha(1).setDuration(1000).setStartDelay(300).start();
        etRegUsername.animate().translationX(0).alpha(1).setDuration(1000).setStartDelay(500).start();
        etRegPassword.animate().translationX(0).alpha(1).setDuration(1000).setStartDelay(700).start();
        btnRegister.animate().translationX(0).alpha(1).setDuration(1000).setStartDelay(900).start();
    }
}