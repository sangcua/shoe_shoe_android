package com.example.myapplication.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.controllers.ProfileController;
import com.example.myapplication.models.Address;
import com.example.myapplication.views.adapters.AddressAdapter;
import com.example.myapplication.controllers.ProfileController.GetAddressesCallback;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class ShippingAddressActivity extends AppCompatActivity implements AddressAdapter.OnAddressClickListener {
    private RecyclerView rvAddresses;
    private MaterialButton btnAddAddress;
    private ProfileController profileController;
    private AddressAdapter addressAdapter;

    private final ActivityResultLauncher<Intent> addAddressLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK) {
                loadAddresses();
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_address);

        // Initialize views
        rvAddresses = findViewById(R.id.rvAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);

        // Setup back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Initialize controller
        profileController = new ProfileController(this);

        // Setup RecyclerView
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        addressAdapter = new AddressAdapter(new ArrayList<>(), this);
        rvAddresses.setAdapter(addressAdapter);

        // Setup add address button
        btnAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAddressActivity.class);
            addAddressLauncher.launch(intent);
        });

        // Load addresses
        loadAddresses();
    }

    private void loadAddresses() {
        profileController.getAddresses(new GetAddressesCallback() {
            @Override
            public void onSuccess(List<Address> addresses) {
                addressAdapter.updateAddresses(addresses);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ShippingAddressActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAddressClick(Address address) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_address", address);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
} 