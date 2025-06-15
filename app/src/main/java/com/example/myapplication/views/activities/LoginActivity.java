package com.example.myapplication.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.controllers.AuthController;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.utils.LoadingManager;
import com.example.myapplication.utils.RoleManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private TextView registerTextView;
    private TokenManager tokenManager;
    private AuthController authController;
    private LoadingManager loadingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = TokenManager.getInstance(this);
        authController = new AuthController(this);
        loadingManager = new LoadingManager(this);
        
        // If already logged in, check role and navigate accordingly
        if (tokenManager.getToken() != null) {
            String storedRole = tokenManager.getRole();
            Log.d("LoginActivity", "Stored Role: " + storedRole); // Thêm dòng này
            RoleManager.checkRoleAndNavigate(this);
            finish();
            return;
        }

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        loginButton.setOnClickListener(v -> attemptLogin());
        registerTextView.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        authController.login(email, password, new AuthController.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                showLoading(false);
                // Use RoleManager to check role and navigate accordingly
                RoleManager.checkRoleAndNavigate(LoginActivity.this);
                finish();
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingManager.show("Đang đăng nhập...");
            loginButton.setEnabled(false);
            emailInput.setEnabled(false);
            passwordInput.setEnabled(false);
        } else {
            loadingManager.dismiss();
            loginButton.setEnabled(true);
            emailInput.setEnabled(true);
            passwordInput.setEnabled(true);
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