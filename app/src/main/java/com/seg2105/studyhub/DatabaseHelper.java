package com.seg2105.studyhub;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "studyhub.db";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_USERS = "users";
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_COURSES = "courses";
    private static final String TABLE_TASKS = "tasks";

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
        createCoursesAndTasksTables(db);

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

    private void createCoursesAndTasksTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_COURSES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "owner TEXT, "
                + "code TEXT, "
                + "title TEXT, "
                + "categoryId INTEGER, "
                + "term TEXT, "
                + "location TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_TASKS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "courseId INTEGER, "
                + "owner TEXT, "
                + "title TEXT, "
                + "description TEXT, "
                + "dueDate TEXT, "
                + "status TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 2 (Deliverable 2) added disabled accounts and course categories.
        // Upgrade without deleting the accounts that already exist.
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN disabled INTEGER DEFAULT 0");
            createCategoriesTable(db);
        }
        // Version 3 (Deliverable 3) added students' courses and tasks
        if (oldVersion < 3) {
            createCoursesAndTasksTables(db);
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
        int deleted = db.delete(TABLE_USERS, "username = ? AND role != 'Admin'", new String[]{username});
        if (deleted > 0) {
            // Also remove the student's courses and tasks so nothing is left behind
            db.delete(TABLE_TASKS, "owner = ? COLLATE NOCASE", new String[]{username});
            db.delete(TABLE_COURSES, "owner = ? COLLATE NOCASE", new String[]{username});
        }
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

    // ---------- Student: managing their own courses ----------

    private ContentValues courseValues(Course course) {
        ContentValues values = new ContentValues();
        values.put("owner", course.getOwnerUsername());
        values.put("code", course.getCode());
        values.put("title", course.getTitle());
        values.put("categoryId", course.getCategoryId());
        values.put("term", course.getTerm());
        values.put("location", course.getLocation());
        return values;
    }

    public boolean addCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_COURSES, null, courseValues(course)) != -1;
    }

    // Only updates the course if it belongs to the same student
    public boolean updateCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TABLE_COURSES, courseValues(course), "id = ? AND owner = ? COLLATE NOCASE",
                new String[]{String.valueOf(course.getId()), course.getOwnerUsername()}) > 0;
    }

    // Rule: deleting a course always deletes all of the tasks that belong to it
    public void deleteCourse(int courseId, String owner) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deleted = db.delete(TABLE_COURSES, "id = ? AND owner = ? COLLATE NOCASE",
                new String[]{String.valueOf(courseId), owner});
        if (deleted > 0) {
            db.delete(TABLE_TASKS, "courseId = ?", new String[]{String.valueOf(courseId)});
        }
    }

    // A student can't have the same course code twice in the same term
    public boolean courseExists(String owner, String code, String term, int ignoreId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_COURSES
                        + " WHERE owner = ? COLLATE NOCASE AND code = ? COLLATE NOCASE"
                        + " AND term = ? COLLATE NOCASE AND id != ?",
                new String[]{owner, code, term, String.valueOf(ignoreId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private Course cursorToCourse(Cursor cursor) {
        return new Course(cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("owner")),
                cursor.getString(cursor.getColumnIndexOrThrow("code")),
                cursor.getString(cursor.getColumnIndexOrThrow("title")),
                cursor.getInt(cursor.getColumnIndexOrThrow("categoryId")),
                cursor.getString(cursor.getColumnIndexOrThrow("term")),
                cursor.getString(cursor.getColumnIndexOrThrow("location")));
    }

    // Returns the course only if it belongs to this student, otherwise null
    public Course getCourse(int courseId, String owner) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COURSES
                        + " WHERE id = ? AND owner = ? COLLATE NOCASE",
                new String[]{String.valueOf(courseId), owner});
        Course course = null;
        if (cursor.moveToFirst()) {
            course = cursorToCourse(cursor);
        }
        cursor.close();
        return course;
    }

    public ArrayList<Course> getCoursesForStudent(String owner) {
        ArrayList<Course> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COURSES
                + " WHERE owner = ? COLLATE NOCASE ORDER BY code", new String[]{owner});
        while (cursor.moveToNext()) {
            courses.add(cursorToCourse(cursor));
        }
        cursor.close();
        return courses;
    }

    // ---------- Student: managing the tasks of a course ----------

    private ContentValues taskValues(Task task) {
        ContentValues values = new ContentValues();
        values.put("courseId", task.getCourseId());
        values.put("owner", task.getOwnerUsername());
        values.put("title", task.getTitle());
        values.put("description", task.getDescription());
        values.put("dueDate", task.getDueDate());
        values.put("status", task.getStatus());
        return values;
    }

    public boolean addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_TASKS, null, taskValues(task)) != -1;
    }

    public boolean updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TABLE_TASKS, taskValues(task), "id = ? AND owner = ? COLLATE NOCASE",
                new String[]{String.valueOf(task.getId()), task.getOwnerUsername()}) > 0;
    }

    public void setTaskStatus(int taskId, String owner, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        db.update(TABLE_TASKS, values, "id = ? AND owner = ? COLLATE NOCASE",
                new String[]{String.valueOf(taskId), owner});
    }

    public void deleteTask(int taskId, String owner) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, "id = ? AND owner = ? COLLATE NOCASE",
                new String[]{String.valueOf(taskId), owner});
    }

    private Task cursorToTask(Cursor cursor) {
        return new Task(cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("courseId")),
                cursor.getString(cursor.getColumnIndexOrThrow("owner")),
                cursor.getString(cursor.getColumnIndexOrThrow("title")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getString(cursor.getColumnIndexOrThrow("dueDate")),
                cursor.getString(cursor.getColumnIndexOrThrow("status")));
    }

    // Returns the task only if it belongs to this student, otherwise null
    public Task getTask(int taskId, String owner) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS
                        + " WHERE id = ? AND owner = ? COLLATE NOCASE",
                new String[]{String.valueOf(taskId), owner});
        Task task = null;
        if (cursor.moveToFirst()) {
            task = cursorToTask(cursor);
        }
        cursor.close();
        return task;
    }

    // Tasks of one course, the ones due first at the top
    public ArrayList<Task> getTasksForCourse(int courseId, String owner) {
        ArrayList<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS
                        + " WHERE courseId = ? AND owner = ? COLLATE NOCASE ORDER BY dueDate",
                new String[]{String.valueOf(courseId), owner});
        while (cursor.moveToNext()) {
            tasks.add(cursorToTask(cursor));
        }
        cursor.close();
        return tasks;
    }

    public int countTasksForCourse(int courseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_TASKS + " WHERE courseId = ?",
                new String[]{String.valueOf(courseId)});
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}
