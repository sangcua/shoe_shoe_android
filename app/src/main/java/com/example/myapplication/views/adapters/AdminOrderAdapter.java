package com.example.myapplication.views.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controllers.OrderController;
import com.example.myapplication.models.Order;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {
    private static final String TAG = "AdminOrderAdapter";
    private List<Order> orders;
    private OnOrderActionListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private OrderController orderController;

    public interface OnOrderActionListener {
        void onViewOrder(Order order);
        void onConfirmOrder(Order order);
        void onCancelOrder(Order order);
        void onCompleteOrder(Order order);
        Context getContext();
    }

    public AdminOrderAdapter(List<Order> orders, OnOrderActionListener listener) {
        Log.d(TAG, "Constructor called");
        this.orders = orders;
        this.listener = listener;
        try {
            this.orderController = new OrderController(listener.getContext());
            Log.d(TAG, "OrderController initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing OrderController: " + e.getMessage());
        }
    }

    public void loadOrders() {
        Log.d(TAG, "loadOrders called");
        if (orderController == null) {
            Log.e(TAG, "OrderController is null");
            return;
        }
        orderController.getAllOrders(new OrderController.GetOrdersCallback() {
            @Override
            public void onSuccess(List<Order> orderList) {
                Log.d(TAG, "Orders loaded successfully, count: " + orderList.size());
                orders.clear();
                orders.addAll(orderList);
                notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading orders: " + error);
                Toast.makeText(listener.getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called for position: " + position);
        try {
            Order order = orders.get(position);
            holder.bind(order);
        } catch (Exception e) {
            Log.e(TAG, "Error in onBindViewHolder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    public void updateOrders(List<Order> newOrders) {
        Log.d(TAG, "updateOrders called with size: " + (newOrders != null ? newOrders.size() : 0));
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId;
        private TextView tvCustomerName;
        private TextView tvAddress;
        private TextView tvPriceAndTime;
        private Chip chipStatus;
        private ImageButton btnView;
        private Button btnConfirm;
        private Button btnCancel;
        private Button btnComplete;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "OrderViewHolder constructor called");
            try {
                tvOrderId = itemView.findViewById(R.id.tvOrderId);
                tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvPriceAndTime = itemView.findViewById(R.id.tvPriceAndTime);
                chipStatus = itemView.findViewById(R.id.chipStatus);
                btnView = itemView.findViewById(R.id.btnView);
                btnConfirm = itemView.findViewById(R.id.btnConfirm);
                btnCancel = itemView.findViewById(R.id.btnCancel);
                btnComplete = itemView.findViewById(R.id.btnComplete);
                Log.d(TAG, "All views found successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error finding views: " + e.getMessage());
            }
        }

        public void bind(Order order) {
            Log.d(TAG, "Binding order: " + order.getId());
            try {
                // Set order ID
                tvOrderId.setText("Order #" + order.getId());

                // Set customer info
                tvCustomerName.setText(order.getName());
                tvAddress.setText(order.getAddress() + "\n" + order.getPhone());

                // Set price and time
                String priceAndTime = order.getPrice() + "đ - " + order.getCreatedAt();
                tvPriceAndTime.setText(priceAndTime);

                // Set status
                updateStatusChip(order.getStatus());

                // Set up buttons
                setupButtons(order);

                // Set click listeners
                btnView.setOnClickListener(v -> listener.onViewOrder(order));
                btnConfirm.setOnClickListener(v -> {
                    listener.onConfirmOrder(order);
                    updateOrderStatus(order.getId(), "confirm");
                });
                btnCancel.setOnClickListener(v -> {
                    listener.onCancelOrder(order);
                    updateOrderStatus(order.getId(), "cancel");
                });
                btnComplete.setOnClickListener(v -> {
                    listener.onCompleteOrder(order);
                    updateOrderStatus(order.getId(), "complete");
                });
                Log.d(TAG, "Order bound successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error binding order: " + e.getMessage());
            }
        }

        private void updateStatusChip(String status) {
            Log.d(TAG, "Updating status chip: " + status);
            try {
                switch (status) {
                    case "chờ xác nhận":
                        chipStatus.setText("Chờ xác nhận");
                        chipStatus.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                        break;
                    case "đang giao hàng":
                        chipStatus.setText("Đang giao hàng");
                        chipStatus.setChipBackgroundColorResource(android.R.color.holo_blue_light);
                        break;
                    case "hoàn thành":
                        chipStatus.setText("Hoàn thành");
                        chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_light);
                        break;
                    case "đã hủy":
                        chipStatus.setText("Đã hủy");
                        chipStatus.setChipBackgroundColorResource(android.R.color.holo_red_light);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating status chip: " + e.getMessage());
            }
        }

        private void setupButtons(Order order) {
            Log.d(TAG, "Setting up buttons for order: " + order.getId() + " with status: " + order.getStatus());
            try {
                switch (order.getStatus()) {
                    case "chờ xác nhận":
                        btnConfirm.setVisibility(View.VISIBLE);
                        btnCancel.setVisibility(View.VISIBLE);
                        btnComplete.setVisibility(View.GONE);
                        break;
                    case "đang giao hàng":
                        btnConfirm.setVisibility(View.GONE);
                        btnCancel.setVisibility(View.VISIBLE);
                        btnComplete.setVisibility(View.VISIBLE);
                        break;
                    case "hoàn thành":
                    case "đã hủy":
                        btnConfirm.setVisibility(View.GONE);
                        btnCancel.setVisibility(View.GONE);
                        btnComplete.setVisibility(View.GONE);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting up buttons: " + e.getMessage());
            }
        }

        private void updateOrderStatus(String orderId, String status) {
            Log.d(TAG, "Updating order status: " + orderId + " to " + status);
            if (orderController == null) {
                Log.e(TAG, "OrderController is null");
                return;
            }
            orderController.updateOrderStatus(orderId, status, new OrderController.OrderCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "Order status updated successfully: " + message);
                    Toast.makeText(listener.getContext(), message, Toast.LENGTH_SHORT).show();
                    loadOrders(); // Reload orders after status update
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error updating order status: " + error);
                    Toast.makeText(listener.getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
} 