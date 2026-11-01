package com.seg2105.studyhub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

        // Only the admin can manage users and categories
        if (user.getRole().equals("Admin")) {
            Button manageUsersButton = findViewById(R.id.manageUsersButton);
            Button manageCategoriesButton = findViewById(R.id.manageCategoriesButton);
            manageUsersButton.setVisibility(View.VISIBLE);
            manageCategoriesButton.setVisibility(View.VISIBLE);

            manageUsersButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(WelcomeActivity.this, ManageUsersActivity.class));
                }
            });

            manageCategoriesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(WelcomeActivity.this, CategoriesActivity.class));
                }
            });
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
