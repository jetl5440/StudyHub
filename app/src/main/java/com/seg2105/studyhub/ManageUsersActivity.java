package com.seg2105.studyhub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

// Admin screen: see every account, and disable/enable or delete students and group leaders
public class ManageUsersActivity extends BackButtonActivity {

    private DatabaseHelper db;
    private ListView usersList;
    private ArrayList<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        db = new DatabaseHelper(this);
        usersList = findViewById(R.id.usersList);

        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showUserOptions(users.get(position));
            }
        });

        loadUsers();
    }

    private void loadUsers() {
        users = db.getAllUsers();
        ArrayList<String> rows = new ArrayList<>();
        for (User u : users) {
            String row = u.getFirstName() + " " + u.getLastName() + " (" + u.getUsername() + ")\n"
                    + u.getRole() + " - " + u.getEmail();
            if (u.isDisabled()) {
                row += "\nDISABLED";
            }
            rows.add(row);
        }
        usersList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
    }

    private void showUserOptions(final User user) {
        if (user.getRole().equals("Admin")) {
            Toast.makeText(this, "The admin account can't be changed", Toast.LENGTH_SHORT).show();
            return;
        }

        final String toggleText = user.isDisabled() ? "Enable account" : "Disable account";
        String[] options = {toggleText, "Delete account"};

        new AlertDialog.Builder(this)
                .setTitle(user.getUsername())
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            db.setUserDisabled(user.getUsername(), !user.isDisabled());
                            Toast.makeText(ManageUsersActivity.this,
                                    user.isDisabled() ? "Account enabled" : "Account disabled",
                                    Toast.LENGTH_SHORT).show();
                            loadUsers();
                        } else {
                            confirmDelete(user);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(final User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete account")
                .setMessage("Are you sure you want to delete " + user.getUsername() + "?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.deleteUser(user.getUsername());
                        Toast.makeText(ManageUsersActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
