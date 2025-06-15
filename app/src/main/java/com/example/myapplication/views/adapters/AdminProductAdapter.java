package com.example.myapplication.views.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {
    private static final String TAG = "AdminProductAdapter";
    private Context context;
    private List<Product> products;
    private OnProductActionListener listener;
    private NumberFormat currencyFormat;

    public interface OnProductActionListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public AdminProductAdapter(Context context, List<Product> products, OnProductActionListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        try {
            Product product = products.get(position);
            
            // Set STT
            holder.tvStt.setText(String.format("#%d", position + 1));
            
            // Set product name
            holder.tvProductName.setText(product.getName());
            
            // Set category
            holder.tvCategory.setText(product.getCategory());
            
            // Set price
            holder.tvPrice.setText(currencyFormat.format(product.getPrice()));
            
            // Set total quantity
            int totalQuantity = product.getTotalQuantity();
            holder.tvQuantity.setText(String.format("SL: %d", totalQuantity));
            
            // Load image
            Glide.with(context)
                .load(product.getImage())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.productImage);

            // Set click listeners
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(product);
                }
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(product);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onBindViewHolder", e);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvStt;
        TextView tvProductName;
        TextView tvCategory;
        TextView tvPrice;
        TextView tvQuantity;
        ImageView productImage;
        ImageButton btnEdit;
        ImageButton btnDelete;

        ProductViewHolder(View itemView) {
            super(itemView);
            tvStt = itemView.findViewById(R.id.tvStt);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            productImage = itemView.findViewById(R.id.productImage);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
} 