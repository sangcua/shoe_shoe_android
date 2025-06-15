package com.example.myapplication.views.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.myapplication.R;
import com.example.myapplication.controllers.CategoryController;
import com.example.myapplication.models.Category;
import com.example.myapplication.models.Size;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddProductActivity extends AppCompatActivity {
    private static final String TAG = "AddProductActivity";
    private EditText nameInput, descriptionInput, priceInput, imageUrlInput;
    private AutoCompleteTextView categorySpinner;
    private LinearLayout sizesContainer;
    private Button addSizeButton, saveButton;
    private CategoryController categoryController;
    private List<Category> categoryList = new ArrayList<>();
    private ArrayAdapter<Category> categoryAdapter;
    private List<Size> sizes = new ArrayList<>();
    private NumberFormat numberFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thêm sản phẩm");

        // Khởi tạo NumberFormat cho tiền tệ Việt Nam
        numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        numberFormat.setMaximumFractionDigits(0);

        initializeViews();
        setupCategorySpinner();
        setupClickListeners();
        setupPriceInput();
        loadCategories();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.nameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        priceInput = findViewById(R.id.priceInput);
        imageUrlInput = findViewById(R.id.imageUrlInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        sizesContainer = findViewById(R.id.sizesContainer);
        addSizeButton = findViewById(R.id.addSizeButton);
        saveButton = findViewById(R.id.saveButton);
        categoryController = new CategoryController(this);
    }

    private void setupCategorySpinner() {
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryList);
        categorySpinner.setAdapter(categoryAdapter);
        
        categorySpinner.setOnItemClickListener((parent, view, position, id) -> {
            Category selectedCategory = categoryList.get(position);
            Log.d(TAG, "Selected category: " + selectedCategory.getName() + " (ID: " + selectedCategory.getId() + ")");
        });
    }

    private void loadCategories() {
        Log.d(TAG, "Loading categories...");
        categoryController.getCategories(new CategoryController.CategoryListCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                Log.d(TAG, "Loaded " + categories.size() + " categories");
                categoryList.clear();
                categoryList.addAll(categories);
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading categories: " + message);
                Toast.makeText(AddProductActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPriceInput() {
        priceInput.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    priceInput.removeTextChangedListener(this);

                    // Xóa tất cả ký tự không phải số
                    String cleanString = s.toString().replaceAll("[^\\d]", "");
                    
                    if (!cleanString.isEmpty()) {
                        // Chuyển đổi thành số và format lại
                        double parsed = Double.parseDouble(cleanString);
                        String formatted = numberFormat.format(parsed);
                        current = formatted;
                        priceInput.setText(formatted);
                        priceInput.setSelection(formatted.length());
                    }

                    priceInput.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupClickListeners() {
        addSizeButton.setOnClickListener(v -> showAddSizeDialog());

        saveButton.setOnClickListener(v -> {
            // TODO: Thêm logic lưu sản phẩm
        });
    }

    private void showAddSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_size, null);
        EditText sizeInput = dialogView.findViewById(R.id.sizeInput);
        EditText quantityInput = dialogView.findViewById(R.id.quantityInput);

        builder.setView(dialogView)
                .setTitle("Thêm kích thước")
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String sizeStr = sizeInput.getText().toString();
                    String quantityStr = quantityInput.getText().toString();

                    if (sizeStr.isEmpty() || quantityStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int size = Integer.parseInt(sizeStr);
                    int quantity = Integer.parseInt(quantityStr);

                    // Kiểm tra xem size đã tồn tại chưa
                    for (Size existingSize : sizes) {
                        if (existingSize.getSize() == size) {
                            Toast.makeText(this, "Kích thước này đã tồn tại", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    Size newSize = new Size(size, quantity);
                    sizes.add(newSize);
                    addSizeView(newSize);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addSizeView(Size size) {
        View sizeView = getLayoutInflater().inflate(R.layout.item_size, sizesContainer, false);
        TextView sizeText = sizeView.findViewById(R.id.sizeText);
        TextView quantityText = sizeView.findViewById(R.id.quantityText);
        ImageButton deleteButton = sizeView.findViewById(R.id.deleteButton);

        sizeText.setText("Size: " + size.getSize());
        quantityText.setText("Số lượng: " + size.getQuantity());

        deleteButton.setOnClickListener(v -> {
            sizes.remove(size);
            sizesContainer.removeView(sizeView);
        });

        sizesContainer.addView(sizeView);
    }
} 