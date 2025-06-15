package com.example.myapplication.views.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Order;
import com.example.myapplication.views.adapters.AdminOrderAdapter;
import java.util.ArrayList;

public class AdminOrderActivity extends AppCompatActivity implements AdminOrderAdapter.OnOrderActionListener {
    private static final String TAG = "AdminOrderActivity";
    private RecyclerView recyclerView;
    private AdminOrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");
        try {
            setContentView(R.layout.activity_admin_orders);
            Log.d(TAG, "setContentView successful");

            // Setup toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                Log.d(TAG, "Toolbar set as support action bar");
            } else {
                Log.e(TAG, "Toolbar not found in layout");
                return;
            }

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Quản lý đơn hàng");
                Log.d(TAG, "Toolbar setup completed");
            } else {
                Log.e(TAG, "getSupportActionBar() returned null");
                return;
            }

            // Initialize RecyclerView
            recyclerView = findViewById(R.id.recyclerView);
            if (recyclerView == null) {
                Log.e(TAG, "recyclerView is null");
                return;
            }
            Log.d(TAG, "RecyclerView found");

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            Log.d(TAG, "LayoutManager set");

            // Initialize adapter with empty list
            adapter = new AdminOrderAdapter(new ArrayList<>(), this);
            recyclerView.setAdapter(adapter);
            Log.d(TAG, "Adapter initialized and set");

            // Load orders from API
            adapter.loadOrders();
            Log.d(TAG, "loadOrders called");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onViewOrder(Order order) {
        Log.d(TAG, "onViewOrder called for order: " + order.getId());
        // TODO: Implement view order details
    }

    @Override
    public void onConfirmOrder(Order order) {
        Log.d(TAG, "onConfirmOrder called for order: " + order.getId());
        // TODO: Implement confirm order
    }

    @Override
    public void onCancelOrder(Order order) {
        Log.d(TAG, "onCancelOrder called for order: " + order.getId());
        // TODO: Implement cancel order
    }

    @Override
    public void onCompleteOrder(Order order) {
        Log.d(TAG, "onCompleteOrder called for order: " + order.getId());
        // TODO: Implement complete order
    }
} 