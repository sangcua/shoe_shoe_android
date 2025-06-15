package com.example.myapplication.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.controllers.ProfileController;
import com.example.myapplication.models.UserProfile;
import com.example.myapplication.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditProfileFieldActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileField";
    
    public static final String EXTRA_FIELD = "field";
    public static final String EXTRA_VALUE = "value";
    public static final String EXTRA_TITLE = "title";

    private ImageView btnBack;
    private TextView tvTitle;
    private TextInputLayout textInputLayout;
    private TextInputEditText etValue;
    private Button btnSave;
    private ProgressBar progressBar;
    private ProfileController profileController;
    private String field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_MyApplication);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_field);

        profileController = new ProfileController(this);
        
        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        textInputLayout = findViewById(R.id.textInputLayout);
        etValue = findViewById(R.id.etValue);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String newValue = etValue.getText().toString().trim();
            
            // Validate input
            String error = validateInput(field, newValue);
            if (error != null) {
                textInputLayout.setError(error);
                return;
            }
            textInputLayout.setError(null);
            
            updateProfile(field, newValue);
        });
    }

    private void loadData() {
        Intent intent = getIntent();
        field = intent.getStringExtra(EXTRA_FIELD);
        String value = intent.getStringExtra(EXTRA_VALUE);
        String title = intent.getStringExtra(EXTRA_TITLE);

        tvTitle.setText(title);
        etValue.setText(value);
        
        // Set input type based on field
        if (field.equals("email")) {
            etValue.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else if (field.equals("phone")) {
            etValue.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        }
    }

    private String validateInput(String field, String value) {
        if (value.isEmpty()) {
            return "Vui lòng nhập thông tin";
        }

        switch (field) {
            case "username":
                if (value.length() < 3) {
                    return "Tên người dùng phải có ít nhất 3 ký tự";
                }
                break;
            case "email":
                if (!ValidationUtils.isValidEmail(value)) {
                    return "Email không hợp lệ";
                }
                break;
            case "phone":
                if (!ValidationUtils.isValidPhone(value)) {
                    return "Số điện thoại không hợp lệ";
                }
                break;
        }

        return null;
    }

    private void updateProfile(String field, String newValue) {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        profileController.updateProfile(field, newValue, new ProfileController.UpdateProfileCallback() {
            @Override
            public void onSuccess(String message, UserProfile userProfile) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    
                    Toast.makeText(EditProfileFieldActivity.this, 
                        "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                    // Return the updated value
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(EXTRA_FIELD, field);
                    resultIntent.putExtra(EXTRA_VALUE, newValue);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(EditProfileFieldActivity.this, 
                        "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileController.cancelRequests();
    }
} 