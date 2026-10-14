package com.seg2105.studyhub;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "studyhub.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_USERS = "users";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "firstName TEXT, "
                + "lastName TEXT, "
                + "email TEXT COLLATE NOCASE, "
                + "username TEXT UNIQUE COLLATE NOCASE, "
                + "password TEXT, "
                + "role TEXT, "
                + "studentNumber TEXT)");

        // The single admin account is created when the database is first made
        ContentValues admin = new ContentValues();
        admin.put("firstName", "Admin");
        admin.put("lastName", "StudyHub");
        admin.put("email", "admin@studyhub.com");
        admin.put("username", "admin");
        admin.put("password", "XPI76SZUqyCjVxgnUjm0");
        admin.put("role", "Admin");
        db.insert(TABLE_USERS, null, admin);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Adds a student or study group leader. Returns false if it could not be saved.
    public boolean addUser(Student user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("firstName", user.getFirstName());
        values.put("lastName", user.getLastName());
        values.put("email", user.getEmail());
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("role", user.getRole());
        values.put("studentNumber", user.getStudentNumber());
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Usernames and emails are compared without caring about upper/lower case
    public boolean usernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_USERS
                + " WHERE username = ? COLLATE NOCASE", new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_USERS
                + " WHERE email = ? COLLATE NOCASE", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Returns the user if the username and password match, otherwise null
    public User login(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS
                + " WHERE username = ? COLLATE NOCASE AND password = ?", new String[]{username, password});
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    public User getUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS
                + " WHERE username = ? COLLATE NOCASE", new String[]{username});
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    public ArrayList<User> getAllUsers() {
        ArrayList<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
        while (cursor.moveToNext()) {
            users.add(cursorToUser(cursor));
        }
        cursor.close();
        return users;
    }

    // Turns a database row back into the right kind of User object
    private User cursorToUser(Cursor cursor) {
        String firstName = cursor.getString(cursor.getColumnIndexOrThrow("firstName"));
        String lastName = cursor.getString(cursor.getColumnIndexOrThrow("lastName"));
        String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
        String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
        String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
        String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
        String studentNumber = cursor.getString(cursor.getColumnIndexOrThrow("studentNumber"));

        if (role.equals("Admin")) {
            return new Admin(firstName, lastName, email, username, password);
        } else if (role.equals("Study Group Leader")) {
            return new StudyGroupLeader(firstName, lastName, email, username, password, studentNumber);
        } else {
            return new Student(firstName, lastName, email, username, password, studentNumber);
        }
    }
}
