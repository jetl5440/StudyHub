package com.seg2105.studyhub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        DatabaseHelper db = new DatabaseHelper(this);
        String username = getIntent().getStringExtra("username");
        User user = null;
        if (username != null) {
            user = db.getUser(username);
        }

        // Should not happen, but if the account can't be found go back to the login screen
        if (user == null) {
            logout();
            return;
        }

        TextView welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText("Welcome " + user.getFirstName() + "! You are logged in as \""
                + user.getRole() + "\".");

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // Only the admin can see the list of all users
        if (user.getRole().equals("Admin")) {
            TextView usersTitle = findViewById(R.id.usersTitle);
            ListView usersList = findViewById(R.id.usersList);
            usersTitle.setVisibility(View.VISIBLE);
            usersList.setVisibility(View.VISIBLE);

            ArrayList<String> rows = new ArrayList<>();
            for (User u : db.getAllUsers()) {
                rows.add(u.getFirstName() + " " + u.getLastName() + " (" + u.getUsername() + ")\n"
                        + u.getRole() + " - " + u.getEmail());
            }
            usersList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
        }
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        // Clear the back stack so the user can't press back to return here
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
