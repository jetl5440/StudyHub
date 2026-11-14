package com.seg2105.studyhub;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

// Used for both adding a new task and editing an existing one
public class TaskEditActivity extends Activity {

    private DatabaseHelper db;
    private String username;
    private int courseId;
    private int taskId = -1; // -1 means we are adding a new task

    private EditText titleInput, descriptionInput;
    private TextView dueDateText;
    private Spinner statusSpinner;
    private String dueDate = ""; // yyyy-MM-dd, empty until the student picks a date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("username");
        courseId = getIntent().getIntExtra("courseId", -1);

        // Make sure the course exists and belongs to this student
        if (username == null || db.getCourse(courseId, username) == null) {
            finish();
            return;
        }

        titleInput = findViewById(R.id.taskTitleInput);
        descriptionInput = findViewById(R.id.taskDescriptionInput);
        dueDateText = findViewById(R.id.dueDateText);
        statusSpinner = findViewById(R.id.statusSpinner);
        Button pickDateButton = findViewById(R.id.pickDateButton);
        Button saveButton = findViewById(R.id.saveTaskButton);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Task.STATUSES);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        taskId = getIntent().getIntExtra("taskId", -1);
        if (taskId != -1) {
            setTitle("Edit Task");
            Task task = db.getTask(taskId, username);
            if (task == null) {
                finish();
                return;
            }
            titleInput.setText(task.getTitle());
            descriptionInput.setText(task.getDescription());
            setDueDate(task.getDueDate());
            for (int i = 0; i < Task.STATUSES.length; i++) {
                if (Task.STATUSES[i].equals(task.getStatus())) {
                    statusSpinner.setSelection(i);
                }
            }
        } else {
            setTitle("Add Task");
        }

        pickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    private void setDueDate(String date) {
        dueDate = date;
        dueDateText.setText("Due date: " + date);
        dueDateText.setError(null);
    }

    // Shows a calendar so the student can't type a badly formatted date
    private void pickDate() {
        Calendar calendar = Calendar.getInstance();
        if (!dueDate.isEmpty()) {
            // Start the calendar on the date that was already chosen
            String[] parts = dueDate.split("-");
            calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1,
                    Integer.parseInt(parts[2]));
        }

        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // month starts at 0 in Java, so add 1
                setDueDate(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        boolean valid = true;

        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            valid = false;
        } else if (title.length() > 60) {
            titleInput.setError("Title must be 60 characters or less");
            valid = false;
        }

        if (description.isEmpty()) {
            descriptionInput.setError("Description is required");
            valid = false;
        } else if (description.length() > 300) {
            descriptionInput.setError("Description must be 300 characters or less");
            valid = false;
        }

        if (dueDate.isEmpty()) {
            dueDateText.setError("Due date is required");
            Toast.makeText(this, "Please pick a due date", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (!valid) {
            return;
        }

        String status = Task.STATUSES[statusSpinner.getSelectedItemPosition()];
        Task task = new Task(taskId, courseId, username, title, description, dueDate, status);

        boolean saved;
        if (taskId == -1) {
            saved = db.addTask(task);
        } else {
            saved = db.updateTask(task);
        }

        if (saved) {
            Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not save task", Toast.LENGTH_SHORT).show();
        }
    }
}
