package com.example.myapplication.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Order;
import java.util.List;
import java.text.DecimalFormat;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private final Context context;
    private final List<Order> orders;
    private final DecimalFormat formatter;
    private static final String STATUS_SHIPPING = "đang giao hàng";

    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
        this.formatter = new DecimalFormat("#,###");
    }

    private String formatPrice(String price) {
        try {
            // Remove VND and any non-digit characters except decimal point
            String cleanPrice = price.replace("VND", "").replaceAll("[^\\d.]", "").trim();
            // Parse the price as a number
            double amount = Double.parseDouble(cleanPrice);
            // Format with thousands separator
            return formatter.format(amount) + "đ";
        } catch (NumberFormatException e) {
            return price + "đ";
        }
    }

    private String convertStatusForDisplay(String status) {
        if (STATUS_SHIPPING.equals(status)) {
            return "chờ giao hàng";
        }
        return status;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        List<Order.OrderProduct> products = order.getProducts();

        // Set order status
        holder.tvOrderStatus.setText(convertStatusForDisplay(order.getStatus()));

        // Clear previous product views
        holder.productsContainer.removeAllViews();

        // Calculate total quantity of all products
        int totalQuantity = 0;
        for (Order.OrderProduct product : products) {
            totalQuantity += product.getQuantity();
        }

        // Add each product
        for (Order.OrderProduct product : products) {
            View productView = LayoutInflater.from(context).inflate(R.layout.item_order_product, holder.productsContainer, false);

            ImageView ivProductImage = productView.findViewById(R.id.ivProductImage);
            TextView tvProductName = productView.findViewById(R.id.tvProductName);
            TextView tvQuantity = productView.findViewById(R.id.tvQuantity);
            TextView tvTotalPrice = productView.findViewById(R.id.tvTotalPrice);

            // Load product image
            Glide.with(context)
                .load(product.getImage())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(ivProductImage);

            // Set product details
            tvProductName.setText(product.getName());
            tvQuantity.setText(String.format("x%d", product.getQuantity()));
            tvTotalPrice.setText(formatPrice(product.getPrice()));

            holder.productsContainer.addView(productView);
        }

        // Set order total with total quantity
        holder.tvOrderTotal.setText(String.format("Tổng số tiền (%d sản phẩm): %s", totalQuantity, formatPrice(order.getPrice())));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout productsContainer;
        TextView tvOrderTotal;
        TextView tvOrderStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productsContainer = itemView.findViewById(R.id.productsContainer);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
        }
    }
}