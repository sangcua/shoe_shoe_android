package com.example.myapplication.views.adapters;

import android.content.Context;
import android.graphics.Color;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {
    private static final String TAG = "AdminOrderAdapter";
    private List<Order> orders;
    private OnOrderActionListener listener;
    private SimpleDateFormat inputDateFormat;
    private SimpleDateFormat outputDateFormat;
    private OrderController orderController;
    private NumberFormat numberFormat;

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
            // Initialize date formatters with UTC timezone
            this.inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            this.inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            this.outputDateFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
            this.outputDateFormat.setTimeZone(TimeZone.getDefault()); // Use local timezone for output
            // Initialize number format for Vietnamese currency
            this.numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            Log.d(TAG, "OrderController and formatters initialized successfully");
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

                // Set text color to black
                tvOrderId.setTextColor(Color.BLACK);
                tvCustomerName.setTextColor(Color.BLACK);
                tvAddress.setTextColor(Color.BLACK);
                tvPriceAndTime.setTextColor(Color.BLACK);

                Log.d(TAG, "All views found successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error finding views: " + e.getMessage());
            }
        }

        public void bind(Order order) {
            Log.d(TAG, "Binding order: " + order.getId());
            try {
                // Set order ID
                tvOrderId.setText("Đơn hàng #" + order.getId());

                // Set customer info
                tvCustomerName.setText(order.getName());
                tvAddress.setText(order.getAddress() + "\n" + order.getPhone());

                // Format price and time
                String formattedPrice = "";
                try {
                    // Remove any non-numeric characters except decimal point
                    String priceStr = order.getPrice().replaceAll("[^\\d.]", "");
                    double price = Double.parseDouble(priceStr);
                    formattedPrice = numberFormat.format(price) + "đ";
                } catch (Exception e) {
                    Log.e(TAG, "Error formatting price: " + e.getMessage());
                    formattedPrice = order.getPrice() + "đ";
                }

                String formattedDate = "";
                try {
                    String createdAt = order.getCreatedAt();
                    Log.d(TAG, "Original date string: " + createdAt);
                    
                    Date date = inputDateFormat.parse(createdAt);
                    if (date != null) {
                        formattedDate = outputDateFormat.format(date);
                        Log.d(TAG, "Parsed date: " + date.toString());
                        Log.d(TAG, "Formatted date: " + formattedDate);
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date: " + e.getMessage());
                    formattedDate = order.getCreatedAt(); // Fallback to original string
                }
                String priceAndTime = formattedPrice + " - " + formattedDate;
                tvPriceAndTime.setText(priceAndTime);

                // Set status
                updateStatusChip(order.getStatus());

                // Set up buttons
                setupButtons(order);

                // Set click listeners
                btnView.setOnClickListener(v -> {
                    Log.d(TAG, "View button clicked for order: " + order.getId());
                    listener.onViewOrder(order);
                });
                btnConfirm.setOnClickListener(v -> {
                    Log.d(TAG, "Confirm button clicked for order: " + order.getId());
                    Log.d(TAG, "Current order status: " + order.getStatus());
                    listener.onConfirmOrder(order);
                });
                btnCancel.setOnClickListener(v -> {
                    Log.d(TAG, "Cancel button clicked for order: " + order.getId());
                    Log.d(TAG, "Current order status: " + order.getStatus());
                    listener.onCancelOrder(order);
                });
                btnComplete.setOnClickListener(v -> {
                    Log.d(TAG, "Complete button clicked for order: " + order.getId());
                    Log.d(TAG, "Current order status: " + order.getStatus());
                    listener.onCompleteOrder(order);
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
                    case "pending":
                        chipStatus.setText("Chờ xác nhận");
                        chipStatus.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                        break;
                    case "confirmed":
                        chipStatus.setText("Đã xác nhận");
                        chipStatus.setChipBackgroundColorResource(android.R.color.holo_blue_light);
                        break;
                    case "completed":
                        chipStatus.setText("Hoàn thành");
                        chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_light);
                        break;
                    case "cancelled":
                        chipStatus.setText("Đã hủy");
                        chipStatus.setChipBackgroundColorResource(android.R.color.holo_red_light);
                        break;
                    default:
                        Log.e(TAG, "Unknown status: " + status);
                        chipStatus.setText(status);
                        chipStatus.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating status chip: " + e.getMessage());
            }
        }

        private void setupButtons(Order order) {
            Log.d(TAG, "Setting up buttons for order: " + order.getId() + " with status: " + order.getStatus());
            try {


                // Set visibility based on status
                switch (order.getStatus()) {
                    case "chờ xác nhận":
                        Log.d(TAG, "Order is pending - showing confirm and cancel buttons");
                        btnConfirm.setVisibility(View.VISIBLE);
                        btnCancel.setVisibility(View.VISIBLE);
                        btnComplete.setVisibility(View.GONE);
                        break;
                    case "đang giao hàng":
                        Log.d(TAG, "Order is confirmed - showing complete button");
                        btnConfirm.setVisibility(View.GONE);
                        btnCancel.setVisibility(View.GONE);
                        btnComplete.setVisibility(View.VISIBLE);
                        break;
                    case "đã hủy":
                        btnConfirm.setVisibility(View.GONE);
                        btnCancel.setVisibility(View.GONE);
                        btnComplete.setVisibility(View.GONE);

                    case "đã giao hàng":
                        Log.d(TAG, "Order is " + order.getStatus() + " - showing only view button");
                        btnConfirm.setVisibility(View.GONE);
                        btnCancel.setVisibility(View.GONE);
                        btnComplete.setVisibility(View.GONE);
                        break;
                    default:
                        // Reset all buttons visibility first

                        Log.e(TAG, "Unknown status for button setup: " + order.getStatus());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting up buttons: " + e.getMessage());
            }
        }
    }
} 