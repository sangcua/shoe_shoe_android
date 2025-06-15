package com.example.myapplication.views.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.views.adapters.AdminOrderAdapter;
import com.example.myapplication.models.Order;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersActivity extends AppCompatActivity implements AdminOrderAdapter.OnOrderActionListener {
    private RecyclerView recyclerView;
    private AdminOrderAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ChipGroup filterChipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        filterChipGroup = findViewById(R.id.filterChipGroup);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Setup filter chips
        setupFilterChips();

        // Load orders
        loadOrders();
    }

    private void setupFilterChips() {
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                loadOrders();
            } else if (checkedId == R.id.chipPending) {
                filterOrders("PENDING");
            } else if (checkedId == R.id.chipConfirmed) {
                filterOrders("CONFIRMED");
            } else if (checkedId == R.id.chipCompleted) {
                filterOrders("COMPLETED");
            } else if (checkedId == R.id.chipCancelled) {
                filterOrders("CANCELLED");
            }
        });
    }

    private void loadOrders() {
        showLoading(true);
        // TODO: Load orders from API
        // For now, using dummy data
        List<Order> dummyOrders = getDummyOrders();
        adapter.updateOrders(dummyOrders);
        showLoading(false);
        updateEmptyState();
    }

    private void filterOrders(String status) {
        showLoading(true);
        // TODO: Filter orders from API
        // For now, filtering dummy data
        List<Order> filteredOrders = new ArrayList<>();
        for (Order order : getDummyOrders()) {
            if (order.getStatus().equals(status)) {
                filteredOrders.add(order);
            }
        }
        adapter.updateOrders(filteredOrders);
        showLoading(false);
        updateEmptyState();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        tvEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    // Dummy data for testing
    private List<Order> getDummyOrders() {
        List<Order> orders = new ArrayList<>();
        
        // Create dummy products
        List<Order.OrderProduct> products1 = new ArrayList<>();
        products1.add(new Order.OrderProduct("1", "Áo thun nam", "image_url", "500000", 1, 2));
        products1.add(new Order.OrderProduct("2", "Quần jean", "image_url", "1000000", 2, 1));

        List<Order.OrderProduct> products2 = new ArrayList<>();
        products2.add(new Order.OrderProduct("3", "Váy liền thân", "image_url", "800000", 1, 1));

        // Create dummy orders
        orders.add(new Order(
            "1", // id
            "user1", // userId
            "Nguyễn Văn A", // name
            "0123456789", // phone
            products1, // products
            "2000000", // price
            "123 Đường ABC, Quận XYZ, TP.HCM", // address
            "PENDING", // status
            false, // deleted
            "2024-03-20 14:30:00", // createdAt
            "2024-03-20 14:30:00" // updatedAt
        ));

        orders.add(new Order(
            "2",
            "user2",
            "Trần Thị B",
            "0987654321",
            products2,
            "800000",
            "456 Đường DEF, Quận UVW, TP.HCM",
            "CONFIRMED",
            false,
            "2024-03-20 15:30:00",
            "2024-03-20 15:30:00"
        ));

        orders.add(new Order(
            "3",
            "user3",
            "Lê Văn C",
            "0123987456",
            new ArrayList<>(),
            "3500000",
            "789 Đường GHI, Quận KLM, TP.HCM",
            "COMPLETED",
            false,
            "2024-03-20 16:30:00",
            "2024-03-20 16:30:00"
        ));

        orders.add(new Order(
            "4",
            "user4",
            "Phạm Thị D",
            "0654789321",
            new ArrayList<>(),
            "4500000",
            "321 Đường JKL, Quận NOP, TP.HCM",
            "CANCELLED",
            false,
            "2024-03-20 17:30:00",
            "2024-03-20 17:30:00"
        ));

        return orders;
    }

    @Override
    public void onViewOrder(Order order) {
        // TODO: Navigate to order detail screen
        Toast.makeText(this, "Xem chi tiết đơn hàng #" + order.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfirmOrder(Order order) {
        // TODO: Call API to confirm order
        Toast.makeText(this, "Xác nhận đơn hàng #" + order.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelOrder(Order order) {
        // TODO: Call API to cancel order
        Toast.makeText(this, "Hủy đơn hàng #" + order.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCompleteOrder(Order order) {
        // TODO: Call API to complete order
        Toast.makeText(this, "Hoàn thành đơn hàng #" + order.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 