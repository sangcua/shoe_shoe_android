package com.example.myapplication.views.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.controllers.ProfileController;
import com.example.myapplication.models.UserProfile;
import com.example.myapplication.controllers.ProfileController.UpdateProfileCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class AddAddressActivity extends AppCompatActivity {
    private TextInputEditText edtName, edtPhone, edtAddress;
    private SwitchMaterial switchDefault;
    private MaterialButton btnSave;
    private ProfileController profileController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        // Initialize views
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        switchDefault = findViewById(R.id.switchDefault);
        btnSave = findViewById(R.id.btnSave);

        // Setup back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Initialize controller
        profileController = new ProfileController(this);

        // Setup save button click listener
        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        boolean isDefault = switchDefault.isChecked();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        profileController.addAddress(
            name,
            phone,
            address,
            isDefault,
            new UpdateProfileCallback() {
                @Override
                public void onSuccess(String message, UserProfile userProfile) {
                    Toast.makeText(AddAddressActivity.this, "Thêm địa chỉ thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(AddAddressActivity.this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
} 