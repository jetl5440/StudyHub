package com.seg2105.studyhub;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "studyhub.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_USERS = "users";
    private static final String TABLE_CATEGORIES = "categories";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "firstName TEXT, "
                + "lastName TEXT, "
                + "email TEXT, "
                + "username TEXT UNIQUE COLLATE NOCASE, "
                + "password TEXT, "
                + "role TEXT, "
                + "studentNumber TEXT, "
                + "disabled INTEGER DEFAULT 0)");

        createCategoriesTable(db);

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

    private void createCategoriesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT UNIQUE COLLATE NOCASE, "
                + "description TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 2 (Deliverable 2) added disabled accounts and course categories.
        // Upgrade without deleting the accounts that already exist.
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN disabled INTEGER DEFAULT 0");
            createCategoriesTable(db);
        }
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

    // Usernames are compared without caring about upper/lower case
    public boolean usernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_USERS
                + " WHERE username = ? COLLATE NOCASE", new String[]{username});
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
        boolean disabled = cursor.getInt(cursor.getColumnIndexOrThrow("disabled")) == 1;

        User user;
        if (role.equals("Admin")) {
            user = new Admin(firstName, lastName, email, username, password);
        } else if (role.equals("Study Group Leader")) {
            user = new StudyGroupLeader(firstName, lastName, email, username, password, studentNumber);
        } else {
            user = new Student(firstName, lastName, email, username, password, studentNumber);
        }
        user.setDisabled(disabled);
        return user;
    }

    // ---------- Admin: managing user accounts ----------

    public void setUserDisabled(String username, boolean disabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("disabled", disabled ? 1 : 0);
        // The admin account can never be disabled
        db.update(TABLE_USERS, values, "username = ? AND role != 'Admin'", new String[]{username});
    }

    public void deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        // The admin account can never be deleted
        db.delete(TABLE_USERS, "username = ? AND role != 'Admin'", new String[]{username});
    }

    // ---------- Admin: managing course categories ----------

    public boolean addCategory(String name, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        return db.insert(TABLE_CATEGORIES, null, values) != -1;
    }

    public boolean updateCategory(int id, String name, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        return db.update(TABLE_CATEGORIES, values, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public void deleteCategory(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORIES, "id = ?", new String[]{String.valueOf(id)});
    }

    // Checks if another category already uses this name (ignoring the one being edited)
    public boolean categoryNameExists(String name, int ignoreId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_CATEGORIES
                + " WHERE name = ? COLLATE NOCASE AND id != ?",
                new String[]{name, String.valueOf(ignoreId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public Category getCategory(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES + " WHERE id = ?",
                new String[]{String.valueOf(id)});
        Category category = null;
        if (cursor.moveToFirst()) {
            category = new Category(cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")));
        }
        cursor.close();
        return category;
    }

    public ArrayList<Category> getAllCategories() {
        ArrayList<Category> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES + " ORDER BY name", null);
        while (cursor.moveToNext()) {
            categories.add(new Category(cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description"))));
        }
        cursor.close();
        return categories;
    }
}
