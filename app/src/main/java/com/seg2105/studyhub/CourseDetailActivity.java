package com.seg2105.studyhub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// Student screen: all the information about one course, with edit / delete / tasks buttons
public class CourseDetailActivity extends BackButtonActivity {

    private DatabaseHelper db;
    private String username;
    private int courseId;
    private Course course;

    private TextView codeText, titleText, categoryText, termText, locationText, taskCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("username");
        courseId = getIntent().getIntExtra("courseId", -1);

        codeText = findViewById(R.id.courseCodeText);
        titleText = findViewById(R.id.courseTitleText);
        categoryText = findViewById(R.id.courseCategoryText);
        termText = findViewById(R.id.courseTermText);
        locationText = findViewById(R.id.courseLocationText);
        taskCountText = findViewById(R.id.courseTaskCountText);
        Button tasksButton = findViewById(R.id.viewTasksButton);
        Button editButton = findViewById(R.id.editCourseButton);
        Button deleteButton = findViewById(R.id.deleteCourseButton);

        tasksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseDetailActivity.this, TasksActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("courseId", courseId);
                startActivity(intent);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseDetailActivity.this, CourseEditActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("courseId", courseId);
                startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCourse(); // refresh after editing the course or its tasks
    }

    private void showCourse() {
        course = null;
        if (username != null) {
            course = db.getCourse(courseId, username);
        }
        if (course == null) {
            // The course doesn't exist or belongs to someone else
            finish();
            return;
        }

        Category category = db.getCategory(course.getCategoryId());
        String categoryName = category != null ? category.getName() : "(category removed by admin)";
        String location = course.getLocation().isEmpty() ? "Not specified" : course.getLocation();

        codeText.setText(course.getCode());
        titleText.setText(course.getTitle());
        categoryText.setText("Category: " + categoryName);
        termText.setText("Term: " + course.getTerm());
        locationText.setText("Location: " + location);
        taskCountText.setText("Tasks: " + db.countTasksForCourse(courseId));
    }

    // Rule: deleting a course also deletes every task in it, so we warn the student first
    private void confirmDelete() {
        int taskCount = db.countTasksForCourse(courseId);
        String message = "Are you sure you want to delete " + course.getCode() + "?";
        if (taskCount > 0) {
            message += "\n\nIts " + taskCount + (taskCount == 1 ? " task" : " tasks")
                    + " will also be deleted.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete course")
                .setMessage(message)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.deleteCourse(courseId, username);
                        Toast.makeText(CourseDetailActivity.this, "Course deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
