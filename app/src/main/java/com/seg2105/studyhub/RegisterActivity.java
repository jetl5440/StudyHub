package com.seg2105.studyhub;

import android.app.Activity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class RegisterActivity extends Activity {

    private EditText firstNameInput, lastNameInput, emailInput, usernameInput;
    private EditText studentNumberInput, passwordInput, confirmPasswordInput;
    private RadioButton studentRadio, leaderRadio;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        studentRadio = findViewById(R.id.studentRadio);
        leaderRadio = findViewById(R.id.leaderRadio);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        usernameInput = findViewById(R.id.usernameInput);
        studentNumberInput = findViewById(R.id.studentNumberInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        Button createButton = findViewById(R.id.createAccountButton);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String studentNumber = studentNumberInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (!validate(firstName, lastName, email, username, studentNumber, password, confirmPassword)) {
            return;
        }

        if (db.usernameExists(username)) {
            usernameInput.setError("This username is already taken");
            return;
        }

        Student newUser;
        if (leaderRadio.isChecked()) {
            newUser = new StudyGroupLeader(firstName, lastName, email, username, password, studentNumber);
        } else {
            newUser = new Student(firstName, lastName, email, username, password, studentNumber);
        }

        if (db.addUser(newUser)) {
            Toast.makeText(this, newUser.getRole() + " account created. You can now log in.",
                    Toast.LENGTH_LONG).show();
            finish(); // go back to the login screen
        } else {
            Toast.makeText(this, "Could not create account", Toast.LENGTH_SHORT).show();
        }
    }

    // Checks every field and shows an error on the ones that are wrong
    private boolean validate(String firstName, String lastName, String email, String username,
                             String studentNumber, String password, String confirmPassword) {
        boolean valid = true;

        if (!studentRadio.isChecked() && !leaderRadio.isChecked()) {
            Toast.makeText(this, "Please choose an account type", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (firstName.isEmpty()) {
            firstNameInput.setError("First name is required");
            valid = false;
        } else if (!firstName.matches("[a-zA-Z' -]+")) {
            firstNameInput.setError("First name can only contain letters");
            valid = false;
        }

        if (lastName.isEmpty()) {
            lastNameInput.setError("Last name is required");
            valid = false;
        } else if (!lastName.matches("[a-zA-Z' -]+")) {
            lastNameInput.setError("Last name can only contain letters");
            valid = false;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            valid = false;
        }

        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            valid = false;
        } else if (!username.matches("[a-zA-Z0-9_]{3,20}")) {
            usernameInput.setError("Username must be 3-20 letters, numbers or _");
            valid = false;
        }

        if (studentNumber.isEmpty()) {
            studentNumberInput.setError("Student number is required");
            valid = false;
        } else if (!studentNumber.matches("[0-9]{7,10}")) {
            studentNumberInput.setError("Student number must be 7 to 10 digits");
            valid = false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            valid = false;
        }

        if (!confirmPassword.equals(password)) {
            confirmPasswordInput.setError("Passwords do not match");
            valid = false;
        }

        return valid;
    }
}
