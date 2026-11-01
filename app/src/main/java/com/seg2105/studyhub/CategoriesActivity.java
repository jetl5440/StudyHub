package com.seg2105.studyhub;

import android.app.Activity;
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

// Admin screen: list course categories, add new ones, and edit or delete existing ones
public class CategoriesActivity extends Activity {

    private DatabaseHelper db;
    private ListView categoriesList;
    private TextView emptyText;
    private ArrayList<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        db = new DatabaseHelper(this);
        categoriesList = findViewById(R.id.categoriesList);
        emptyText = findViewById(R.id.emptyText);
        Button addButton = findViewById(R.id.addCategoryButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CategoriesActivity.this, CategoryEditActivity.class));
            }
        });

        categoriesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showCategoryOptions(categories.get(position));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories(); // refresh after coming back from the add/edit screen
    }

    private void loadCategories() {
        categories = db.getAllCategories();
        ArrayList<String> rows = new ArrayList<>();
        for (Category c : categories) {
            rows.add(c.getName() + "\n" + c.getDescription());
        }
        categoriesList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
        emptyText.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showCategoryOptions(final Category category) {
        String[] options = {"Edit", "Delete"};
        new AlertDialog.Builder(this)
                .setTitle(category.getName())
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(CategoriesActivity.this, CategoryEditActivity.class);
                            intent.putExtra("categoryId", category.getId());
                            startActivity(intent);
                        } else {
                            confirmDelete(category);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(final Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete category")
                .setMessage("Are you sure you want to delete \"" + category.getName() + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.deleteCategory(category.getId());
                        Toast.makeText(CategoriesActivity.this, "Category deleted", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
