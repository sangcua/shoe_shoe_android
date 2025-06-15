package com.example.myapplication.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.views.activities.LoginActivity;
import com.example.myapplication.views.activities.ProfileDetailActivity;
import com.example.myapplication.views.activities.ShippingAddressActivity;
import com.example.myapplication.views.activities.OrderStatusActivity;
import com.example.myapplication.controllers.OrderController;
import com.example.myapplication.models.Order;
import java.util.List;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private LinearLayout btnViewProfile;
    private LinearLayout btnShippingAddress;
    private LinearLayout btnOrderHistory;
    private Button logoutButton;
    private TextView tvPendingConfirmationCount;
    private TextView tvPendingDeliveryCount;
    private TextView tvDeliveredCount;
    private TextView tvCancelledCount;
    private OrderController orderController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        setupListeners();
        setupOrderController();
        loadOrderCounts();
        return view;
    }

    private void initViews(View view) {
        btnViewProfile = view.findViewById(R.id.btnViewProfile);
        btnShippingAddress = view.findViewById(R.id.btnShippingAddress);
        btnOrderHistory = view.findViewById(R.id.btnOrderHistory);
        logoutButton = view.findViewById(R.id.logoutButton);
        tvPendingConfirmationCount = view.findViewById(R.id.tvPendingConfirmationCount);
        tvPendingDeliveryCount = view.findViewById(R.id.tvPendingDeliveryCount);
        tvDeliveredCount = view.findViewById(R.id.tvDeliveredCount);
        tvCancelledCount = view.findViewById(R.id.tvCancelledCount);
    }

    private void setupListeners() {
        btnViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfileDetailActivity.class);
            startActivity(intent);
        });

        btnShippingAddress.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ShippingAddressActivity.class);
            startActivity(intent);
        });

        btnOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), OrderStatusActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            // Clear all stored data
            TokenManager.getInstance(requireContext()).clearAll();

            // Navigate to login screen
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private void setupOrderController() {
        orderController = new OrderController(requireContext());
    }

    private void loadOrderCounts() {
        orderController.getAllOrders(new OrderController.GetOrdersCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                updateOrderCounts(orders);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading orders: " + message);
            }
        });
    }

    private void updateOrderCounts(List<Order> orders) {
        int pendingConfirmation = 0;
        int pendingDelivery = 0;
        int delivered = 0;
        int cancelled = 0;

        for (Order order : orders) {
            switch (order.getStatus()) {
                case "chờ xác nhận":
                    pendingConfirmation++;
                    break;
                case "đang giao hàng":
                    pendingDelivery++;
                    break;
                case "đã giao":
                    delivered++;
                    break;
                case "đã hủy":
                    cancelled++;
                    break;
            }
        }

        tvPendingConfirmationCount.setText(String.valueOf(pendingConfirmation));
        tvPendingDeliveryCount.setText(String.valueOf(pendingDelivery));
        tvDeliveredCount.setText(String.valueOf(delivered));
        tvCancelledCount.setText(String.valueOf(cancelled));
    }
} 