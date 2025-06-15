package com.example.myapplication.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.myapplication.R;
import com.example.myapplication.models.Product;
import com.example.myapplication.utils.VolleySingleton;
import com.example.myapplication.views.activities.ProductDetailActivity;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private Context context;
    private List<Product> products;
    private ImageLoader imageLoader;
    private NumberFormat currencyFormat;

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        // Set product image
        holder.productImage.setImageUrl(product.getImage(), imageLoader);
        holder.productImage.setDefaultImageResId(R.drawable.sample_product);
        holder.productImage.setErrorImageResId(R.drawable.sample_product);

        // Set product name
        holder.productName.setText(product.getName());

        // Format and set price
        String formattedPrice = currencyFormat.format(product.getPrice());
        holder.productPrice.setText(formattedPrice);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products.clear();
        this.products.addAll(newProducts);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView productImage;
        TextView productName;
        TextView productPrice;

        ViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
        }
    }
} 