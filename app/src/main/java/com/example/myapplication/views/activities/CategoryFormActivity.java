package com.example.myapplication.views.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.controllers.CategoryController;
import com.example.myapplication.models.Category;
import com.example.myapplication.utils.LoadingManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class CategoryFormActivity extends AppCompatActivity {
    private TextInputEditText nameInput;
    private TextInputEditText descriptionInput;
    private MaterialButton saveButton;
    private MaterialToolbar toolbar;
    private CategoryController categoryController;
    private LoadingManager loadingManager;
    private Category category; // null if adding new, not null if editing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_form);

        categoryController = new CategoryController(this);
        loadingManager = new LoadingManager(this);

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        saveButton = findViewById(R.id.saveButton);
        toolbar = findViewById(R.id.toolbar);

        // Set up toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Check if we're editing an existing category
        category = (Category) getIntent().getSerializableExtra("category");
        if (category != null) {
            // We're editing
            toolbar.setTitle("Sửa danh mục");
            nameInput.setText(category.getName());
            descriptionInput.setText(category.getDescription());
        } else {
            // We're adding new
            toolbar.setTitle("Thêm danh mục");
        }

        saveButton.setOnClickListener(v -> saveCategory());
    }

    private void saveCategory() {
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Vui lòng nhập tên danh mục");
            nameInput.setError("Vui lòng nhập tên danh mục");
            return;
        }

        showLoading(true);

        if (category != null) {
            // Update existing category
            categoryController.updateCategory(category.getId(), name, description, new CategoryController.CategoryCallback() {
                @Override
                public void onSuccess(String message) {
                    showLoading(false);
                    Toast.makeText(CategoryFormActivity.this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String message) {
                    showLoading(false);
                    Toast.makeText(CategoryFormActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new category
            categoryController.createCategory(name, description, new CategoryController.CategoryCallback() {
                @Override
                public void onSuccess(String message) {
                    showLoading(false);
                    Toast.makeText(CategoryFormActivity.this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String message) {
                    showLoading(false);
                    Toast.makeText(CategoryFormActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingManager.show("Đang lưu...");
            saveButton.setEnabled(false);
            nameInput.setEnabled(false);
            descriptionInput.setEnabled(false);
        } else {
            loadingManager.dismiss();
            saveButton.setEnabled(true);
            nameInput.setEnabled(true);
            descriptionInput.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingManager != null) {
            loadingManager.dismiss();
        }
    }
} 