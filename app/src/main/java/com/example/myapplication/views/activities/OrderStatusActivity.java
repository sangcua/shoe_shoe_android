package com.example.myapplication.views.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.views.adapters.OrderProductAdapter;
import com.example.myapplication.controllers.OrderController;
import com.example.myapplication.models.Order;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.example.myapplication.views.adapters.OrderAdapter;
import com.example.myapplication.utils.LoadingManager;

public class OrderStatusActivity extends AppCompatActivity {
    private static final String TAG = "OrderStatusActivity";
    private TextView tvPendingConfirmationCount;
    private TextView tvPendingDeliveryCount;
    private TextView tvDeliveredCount;
    private TextView tvCancelledCount;
    private RecyclerView recyclerView;
    private TextView tvNoOrders;
    private OrderController orderController;
    private List<Order> allOrders;
    private static final String STATUS_PENDING = "chờ xác nhận";
    private static final String STATUS_SHIPPING = "đang giao hàng";
    private static final String STATUS_DELIVERED = "đã giao hàng";
    private static final String STATUS_CANCELLED = "đã hủy";
    private LoadingManager loadingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
        setTitle("Lịch sử đơn hàng");

        // Initialize LoadingManager
        loadingManager = new LoadingManager(this);

        // Initialize views
        initViews();
        
        // Initialize OrderController
        orderController = new OrderController(this);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Setup click listeners
        setupClickListeners();
        
        // Load order counts
        loadOrders();

        // Setup back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvPendingConfirmationCount = findViewById(R.id.tvPendingConfirmationCount);
        tvPendingDeliveryCount = findViewById(R.id.tvPendingDeliveryCount);
        tvDeliveredCount = findViewById(R.id.tvDeliveredCount);
        tvCancelledCount = findViewById(R.id.tvCancelledCount);
        recyclerView = findViewById(R.id.recyclerView);
        tvNoOrders = findViewById(R.id.tvNoOrders);
    }

    private void setupClickListeners() {
        View btnPendingConfirmation = findViewById(R.id.btnPendingConfirmation);
        View btnPendingDelivery = findViewById(R.id.btnPendingDelivery);
        View btnDelivered = findViewById(R.id.btnDelivered);
        View btnCancelled = findViewById(R.id.btnCancelled);

        btnPendingConfirmation.setOnClickListener(v -> {
            updateSelectedTab(v);
            loadOrders(STATUS_PENDING);
        });
            
        btnPendingDelivery.setOnClickListener(v -> {
            updateSelectedTab(v);
            loadOrders(STATUS_SHIPPING);
        });
            
        btnDelivered.setOnClickListener(v -> {
            updateSelectedTab(v);
            loadOrders(STATUS_DELIVERED);
        });
            
        btnCancelled.setOnClickListener(v -> {
            updateSelectedTab(v);
            loadOrders(STATUS_CANCELLED);
        });

        // Set initial selection
        btnPendingConfirmation.setSelected(true);
    }

    private void updateSelectedTab(View selectedTab) {
        // Reset all tabs
        findViewById(R.id.btnPendingConfirmation).setSelected(false);
        findViewById(R.id.btnPendingDelivery).setSelected(false);
        findViewById(R.id.btnDelivered).setSelected(false);
        findViewById(R.id.btnCancelled).setSelected(false);

        // Set selected tab
        selectedTab.setSelected(true);
    }

    private void loadOrders() {
        loadingManager.show("Đang tải đơn hàng...");
        orderController.getUserOrders(new OrderController.GetOrdersCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                loadingManager.dismiss();
                allOrders = orders;
                updateStatusCounts(orders);
                // Show first category by default
                loadOrders(STATUS_PENDING);
            }

            @Override
            public void onError(String message) {
                loadingManager.dismiss();
                Log.e(TAG, "Error loading orders: " + message);
                tvNoOrders.setText(R.string.error_loading_orders);
                tvNoOrders.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void loadOrders(String status) {
        if (allOrders == null) {
            loadingManager.show("Đang tải đơn hàng...");
            return;
        }
        
        loadingManager.show("Đang lọc đơn hàng...");
        
        // Sử dụng Handler để thực hiện việc lọc bất đồng bộ
        new Handler(Looper.getMainLooper()).post(() -> {
            List<Order> filteredOrders = allOrders.stream()
                    .filter(order -> status.equals(order.getStatus()))
                    .collect(Collectors.toList());

            // Cập nhật UI trên main thread
            runOnUiThread(() -> {
                loadingManager.dismiss();

                if (filteredOrders.isEmpty()) {
                    tvNoOrders.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvNoOrders.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    
                    OrderAdapter adapter = new OrderAdapter(this, filteredOrders);
                    recyclerView.setAdapter(adapter);
                }
            });
        });
    }

    private void updateStatusCounts(List<Order> allOrders) {
        int pendingCount = 0;
        int shippingCount = 0;
        int deliveredCount = 0;
        int cancelledCount = 0;

        for (Order order : allOrders) {
            String status = order.getStatus();
            Log.d(TAG, "Counting order with status: " + status);
            switch (status) {
                case STATUS_PENDING:
                    pendingCount++;
                    break;
                case STATUS_SHIPPING:
                    shippingCount++;
                    break;
                case STATUS_DELIVERED:
                    deliveredCount++;
                    break;
                case STATUS_CANCELLED:
                    cancelledCount++;
                    break;
            }
        }

        Log.d(TAG, String.format("Order counts - Pending: %d, Shipping: %d, Delivered: %d, Cancelled: %d",
            pendingCount, shippingCount, deliveredCount, cancelledCount));

        // Update the status count views
        tvPendingConfirmationCount.setText(String.valueOf(pendingCount));
        tvPendingDeliveryCount.setText(String.valueOf(shippingCount));
        tvDeliveredCount.setText(String.valueOf(deliveredCount));
        tvCancelledCount.setText(String.valueOf(cancelledCount));
    }

    // Chuyển đổi từ trạng thái hiển thị sang trạng thái API
    private String convertDisplayToApiStatus(String displayStatus) {
        if ("chờ giao hàng".equals(displayStatus)) {
            return STATUS_SHIPPING;
        }
        return displayStatus;
    }

    // Chuyển đổi từ trạng thái API sang trạng thái hiển thị
    private String convertApiToDisplayStatus(String apiStatus) {
        if (STATUS_SHIPPING.equals(apiStatus)) {
            return "chờ giao hàng";
        }
        return apiStatus;
    }
} 