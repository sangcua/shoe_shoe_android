package com.example.myapplication.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.controllers.ProfileController;
import com.example.myapplication.models.UserProfile;
import com.example.myapplication.utils.LoadingManager;

public class ProfileDetailActivity extends AppCompatActivity {
    private static final int REQUEST_EDIT_FIELD = 100;
    
    private ImageView btnBack;
    private LinearLayout layoutUsername, layoutEmail, layoutPhone;
    private TextView tvUsername, tvEmail, tvPhone;
    private LoadingManager loadingManager;
    private ProfileController profileController;

    private final ActivityResultLauncher<Intent> editFieldLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String field = result.getData().getStringExtra(EditProfileFieldActivity.EXTRA_FIELD);
                String value = result.getData().getStringExtra(EditProfileFieldActivity.EXTRA_VALUE);
                
                // Update UI with new value
                updateUIField(field, value);
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);
        
        profileController = new ProfileController(this);
        loadingManager = new LoadingManager(this);
        
        initViews();
        setupListeners();
        loadUserProfile();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutUsername = findViewById(R.id.layoutUsername);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPhone = findViewById(R.id.layoutPhone);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        layoutUsername.setOnClickListener(v -> {
            openEditField("username", "Tên người dùng", tvUsername.getText().toString());
        });

        layoutEmail.setOnClickListener(v -> {
            openEditField("email", "Email", tvEmail.getText().toString());
        });

        layoutPhone.setOnClickListener(v -> {
            openEditField("phone", "Số điện thoại", tvPhone.getText().toString());
        });
    }

    private void openEditField(String field, String title, String currentValue) {
        Intent intent = new Intent(this, EditProfileFieldActivity.class);
        intent.putExtra(EditProfileFieldActivity.EXTRA_FIELD, field);
        intent.putExtra(EditProfileFieldActivity.EXTRA_TITLE, title);
        intent.putExtra(EditProfileFieldActivity.EXTRA_VALUE, currentValue);
        editFieldLauncher.launch(intent);
    }

    private void loadUserProfile() {
        loadingManager.show();

        profileController.fetchUserProfile(new ProfileController.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                runOnUiThread(() -> {
                    loadingManager.dismiss();
                    updateUIWithProfile(userProfile);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    loadingManager.dismiss();
                    Toast.makeText(ProfileDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateUIWithProfile(UserProfile userProfile) {
        tvUsername.setText(userProfile.getUsername());
        tvEmail.setText(userProfile.getEmail());
        tvPhone.setText(userProfile.getPhone());
    }

    private void updateUIField(String field, String value) {
        switch (field) {
            case "username":
                tvUsername.setText(value);
                break;
            case "email":
                tvEmail.setText(value);
                break;
            case "phone":
                tvPhone.setText(value);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileController.cancelRequests();
    }
}
