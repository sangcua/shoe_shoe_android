package com.example.myapplication.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.controllers.AuthController;
import com.example.myapplication.utils.LoadingManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.myapplication.utils.RoleManager;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPasswordLayout, phoneLayout;
    private TextInputEditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText, phoneEditText;
    private Button registerButton;
    private TextView loginTextView;
    private AuthController authController;
    private LoadingManager loadingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authController = new AuthController(this);
        loadingManager = new LoadingManager(this);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            String phone = phoneEditText.getText().toString().trim();

            if (validateInput(name, email, password, confirmPassword)) {
                showLoading(true);
                authController.register(email, password, name, phone, new AuthController.AuthCallback() {
                    @Override
                    public void onSuccess(String message) {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        RoleManager.checkRoleAndNavigate(RegisterActivity.this);
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        loginTextView.setOnClickListener(v -> finish());
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (name.isEmpty()) {
            nameLayout.setError("Vui lòng nhập họ tên");
            isValid = false;
        } else {
            nameLayout.setError(null);
        }

        if (email.isEmpty()) {
            emailLayout.setError("Vui lòng nhập email");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Vui lòng nhập mật khẩu");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Vui lòng xác nhận mật khẩu");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Mật khẩu không khớp");
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        return isValid;
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingManager.show("Đang đăng ký...");
            setInputsEnabled(false);
        } else {
            loadingManager.dismiss();
            setInputsEnabled(true);
        }
    }

    private void setInputsEnabled(boolean enabled) {
        registerButton.setEnabled(enabled);
        nameEditText.setEnabled(enabled);
        emailEditText.setEnabled(enabled);
        passwordEditText.setEnabled(enabled);
        confirmPasswordEditText.setEnabled(enabled);
        phoneEditText.setEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingManager != null) {
            loadingManager.dismiss();
        }
    }
} 