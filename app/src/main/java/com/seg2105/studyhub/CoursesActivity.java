package com.seg2105.studyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

// Student screen: list of the signed-in student's courses
public class CoursesActivity extends BackButtonActivity {

    private DatabaseHelper db;
    private String username;
    private ListView coursesList;
    private TextView emptyText;
    private ArrayList<Course> courses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("username");
        if (username == null) {
            finish();
            return;
        }

        coursesList = findViewById(R.id.coursesList);
        emptyText = findViewById(R.id.emptyText);
        Button addButton = findViewById(R.id.addCourseButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CoursesActivity.this, CourseEditActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        coursesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CoursesActivity.this, CourseDetailActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("courseId", courses.get(position).getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses(); // refresh after adding, editing or deleting a course
    }

    private void loadCourses() {
        courses = db.getCoursesForStudent(username);
        ArrayList<String> rows = new ArrayList<>();
        for (Course c : courses) {
            rows.add(c.getCode() + " - " + c.getTitle() + "\n" + c.getTerm());
        }
        coursesList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
        emptyText.setVisibility(courses.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
