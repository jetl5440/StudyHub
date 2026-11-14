package com.seg2105.studyhub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

// Student screen: tasks of one course. Tap a task to edit it, mark it completed or delete it.
public class TasksActivity extends BackButtonActivity {

    private DatabaseHelper db;
    private String username;
    private int courseId;
    private ListView tasksList;
    private TextView emptyText;
    private ArrayList<Task> tasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("username");
        courseId = getIntent().getIntExtra("courseId", -1);

        Course course = null;
        if (username != null) {
            course = db.getCourse(courseId, username);
        }
        if (course == null) {
            finish();
            return;
        }
        setTitle(course.getCode() + " Tasks");

        tasksList = findViewById(R.id.tasksList);
        emptyText = findViewById(R.id.emptyText);
        Button addButton = findViewById(R.id.addTaskButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TasksActivity.this, TaskEditActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("courseId", courseId);
                startActivity(intent);
            }
        });

        tasksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showTaskOptions(tasks.get(position));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        tasks = db.getTasksForCourse(courseId, username);
        ArrayList<String> rows = new ArrayList<>();
        for (Task t : tasks) {
            rows.add(t.getTitle() + " [" + t.getStatus() + "]\n"
                    + "Due " + t.getDueDate() + "\n"
                    + t.getDescription());
        }
        tasksList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
        emptyText.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showTaskOptions(final Task task) {
        final String toggleText = task.isCompleted() ? "Mark as not completed" : "Mark as completed";
        String[] options = {"Edit", toggleText, "Delete"};

        new AlertDialog.Builder(this)
                .setTitle(task.getTitle())
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(TasksActivity.this, TaskEditActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("courseId", courseId);
                            intent.putExtra("taskId", task.getId());
                            startActivity(intent);
                        } else if (which == 1) {
                            String newStatus = task.isCompleted() ? Task.STATUS_TODO : Task.STATUS_COMPLETED;
                            db.setTaskStatus(task.getId(), username, newStatus);
                            Toast.makeText(TasksActivity.this, "Task marked as " + newStatus,
                                    Toast.LENGTH_SHORT).show();
                            loadTasks();
                        } else {
                            confirmDelete(task);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(final Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete task")
                .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.deleteTask(task.getId(), username);
                        Toast.makeText(TasksActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                        loadTasks();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
