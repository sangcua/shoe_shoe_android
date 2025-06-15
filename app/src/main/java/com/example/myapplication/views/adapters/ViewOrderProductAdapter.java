package com.example.myapplication.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Order;
import java.util.List;

public class ViewOrderProductAdapter extends RecyclerView.Adapter<ViewOrderProductAdapter.ProductViewHolder> {
    private List<Order.OrderProduct> products;

    public ViewOrderProductAdapter(List<Order.OrderProduct> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view_order_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Order.OrderProduct product = products.get(position);
        holder.bind(product, position + 1);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductIndex;
        private TextView tvProductName;
        private TextView tvProductSize;
        private TextView tvProductQuantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductIndex = itemView.findViewById(R.id.tvProductIndex);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductSize = itemView.findViewById(R.id.tvProductSize);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
        }

        public void bind(Order.OrderProduct product, int index) {
            tvProductIndex.setText(String.valueOf(index));
            tvProductName.setText(product.getName());
            tvProductSize.setText(String.valueOf(product.getSize()));
            tvProductQuantity.setText(String.valueOf(product.getQuantity()));
        }
    }
} 