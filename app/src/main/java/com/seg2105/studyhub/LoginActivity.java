package com.seg2105.studyhub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

    private EditText usernameInput;
    private EditText passwordInput;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void login() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // Validate fields
        boolean valid = true;
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            valid = false;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            valid = false;
        }
        if (!valid) {
            return;
        }

        User user = db.login(username, password);
        if (user == null) {
            Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
            return;
        }

        passwordInput.setText("");
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.putExtra("username", user.getUsername());
        startActivity(intent);
    }
}
