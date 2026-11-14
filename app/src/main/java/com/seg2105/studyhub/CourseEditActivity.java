package com.seg2105.studyhub;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

// Used for both adding a new course and editing an existing one
public class CourseEditActivity extends Activity {

    private static final String[] SEASONS = {"Fall", "Winter", "Summer"};

    private DatabaseHelper db;
    private String username;
    private int courseId = -1; // -1 means we are adding a new course

    private EditText codeInput, titleInput, yearInput, locationInput;
    private Spinner categorySpinner, seasonSpinner;
    private TextView noCategoriesText;
    private ArrayList<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_edit);

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("username");
        if (username == null) {
            finish();
            return;
        }

        codeInput = findViewById(R.id.courseCodeInput);
        titleInput = findViewById(R.id.courseTitleInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        noCategoriesText = findViewById(R.id.noCategoriesText);
        seasonSpinner = findViewById(R.id.seasonSpinner);
        yearInput = findViewById(R.id.yearInput);
        locationInput = findViewById(R.id.locationInput);
        Button saveButton = findViewById(R.id.saveCourseButton);

        // The student can only pick one of the categories the admin created
        categories = db.getAllCategories();
        ArrayList<String> categoryNames = new ArrayList<>();
        for (Category c : categories) {
            categoryNames.add(c.getName());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        noCategoriesText.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);

        ArrayAdapter<String> seasonAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, SEASONS);
        seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seasonSpinner.setAdapter(seasonAdapter);

        courseId = getIntent().getIntExtra("courseId", -1);
        if (courseId != -1) {
            setTitle("Edit Course");
            Course course = db.getCourse(courseId, username);
            if (course == null) {
                finish();
                return;
            }
            fillForm(course);
        } else {
            setTitle("Add Course");
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    // Puts the values of an existing course into the form
    private void fillForm(Course course) {
        codeInput.setText(course.getCode());
        titleInput.setText(course.getTitle());
        locationInput.setText(course.getLocation());

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == course.getCategoryId()) {
                categorySpinner.setSelection(i);
            }
        }

        // The term is saved as "Season Year", e.g. "Fall 2026"
        String[] termParts = course.getTerm().split(" ");
        if (termParts.length == 2) {
            for (int i = 0; i < SEASONS.length; i++) {
                if (SEASONS[i].equals(termParts[0])) {
                    seasonSpinner.setSelection(i);
                }
            }
            yearInput.setText(termParts[1]);
        }
    }

    private void save() {
        // Course codes are saved without spaces and in upper case, e.g. "seg 2105" -> "SEG2105"
        String code = codeInput.getText().toString().trim().replace(" ", "").toUpperCase();
        String title = titleInput.getText().toString().trim();
        String year = yearInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();

        boolean valid = true;

        if (code.isEmpty()) {
            codeInput.setError("Course code is required");
            valid = false;
        } else if (!code.matches("[A-Z]{3}[0-9]{4}")) {
            codeInput.setError("Course code must be 3 letters and 4 digits, e.g. SEG2105");
            valid = false;
        }

        if (title.isEmpty()) {
            titleInput.setError("Course title is required");
            valid = false;
        } else if (title.length() > 80) {
            titleInput.setError("Title must be 80 characters or less");
            valid = false;
        } else if (!title.matches("[a-zA-Z0-9&',:()/ -]+")) {
            titleInput.setError("Title can only contain letters, numbers, spaces and & ' , : ( ) / -");
            valid = false;
        }

        if (categories.isEmpty()) {
            Toast.makeText(this, "There are no categories yet. Ask the admin to create one.",
                    Toast.LENGTH_LONG).show();
            valid = false;
        }

        if (year.isEmpty()) {
            yearInput.setError("Year is required");
            valid = false;
        } else if (!year.matches("[0-9]{4}")) {
            yearInput.setError("Year must be 4 digits, e.g. 2026");
            valid = false;
        } else {
            int yearNumber = Integer.parseInt(year);
            if (yearNumber < 2000 || yearNumber > 2100) {
                yearInput.setError("Year must be between 2000 and 2100");
                valid = false;
            }
        }

        // Location is optional, but if it's filled in it must be reasonable
        if (location.length() > 50) {
            locationInput.setError("Location must be 50 characters or less");
            valid = false;
        } else if (!location.isEmpty() && !location.matches("[a-zA-Z0-9.,#' -]+")) {
            locationInput.setError("Location can only contain letters, numbers, spaces and . , # ' -");
            valid = false;
        }

        if (!valid) {
            return;
        }

        String term = SEASONS[seasonSpinner.getSelectedItemPosition()] + " " + year;
        int categoryId = categories.get(categorySpinner.getSelectedItemPosition()).getId();

        if (db.courseExists(username, code, term, courseId)) {
            codeInput.setError("You already have " + code + " in " + term);
            return;
        }

        Course course = new Course(courseId, username, code, title, categoryId, term, location);
        boolean saved;
        if (courseId == -1) {
            saved = db.addCourse(course);
        } else {
            saved = db.updateCourse(course);
        }

        if (saved) {
            Toast.makeText(this, "Course saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not save course", Toast.LENGTH_SHORT).show();
        }
    }
}
