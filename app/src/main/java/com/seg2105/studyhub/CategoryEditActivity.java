package com.seg2105.studyhub;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// Used for both adding a new category and editing an existing one
public class CategoryEditActivity extends Activity {

    private EditText nameInput;
    private EditText descriptionInput;
    private DatabaseHelper db;
    private int categoryId = -1; // -1 means we are adding a new category

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_edit);

        db = new DatabaseHelper(this);
        nameInput = findViewById(R.id.categoryNameInput);
        descriptionInput = findViewById(R.id.categoryDescriptionInput);
        Button saveButton = findViewById(R.id.saveCategoryButton);

        categoryId = getIntent().getIntExtra("categoryId", -1);
        if (categoryId != -1) {
            setTitle("Edit Category");
            Category category = db.getCategory(categoryId);
            if (category == null) {
                finish();
                return;
            }
            nameInput.setText(category.getName());
            descriptionInput.setText(category.getDescription());
        } else {
            setTitle("Add Category");
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    private void save() {
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        boolean valid = true;
        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            valid = false;
        } else if (name.length() > 50) {
            nameInput.setError("Name must be 50 characters or less");
            valid = false;
        } else if (!name.matches("[a-zA-Z0-9&' -]+")) {
            nameInput.setError("Name can only contain letters, numbers, spaces, & and -");
            valid = false;
        } else if (db.categoryNameExists(name, categoryId)) {
            nameInput.setError("A category with this name already exists");
            valid = false;
        }

        if (description.isEmpty()) {
            descriptionInput.setError("Description is required");
            valid = false;
        } else if (description.length() > 200) {
            descriptionInput.setError("Description must be 200 characters or less");
            valid = false;
        }

        if (!valid) {
            return;
        }

        boolean saved;
        if (categoryId == -1) {
            saved = db.addCategory(name, description);
        } else {
            saved = db.updateCategory(categoryId, name, description);
        }

        if (saved) {
            Toast.makeText(this, "Category saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not save category", Toast.LENGTH_SHORT).show();
        }
    }
}
