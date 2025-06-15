package com.example.myapplication.views.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Category;
import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.CategoryViewHolder> {
    private static final String TAG = "AdminCategoryAdapter";
    private Context context;
    private List<Category> categories;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEditClick(Category category);
        void onDeleteClick(Category category);
    }

    public AdminCategoryAdapter(Context context, List<Category> categories, OnCategoryActionListener listener) {
        Log.d(TAG, "Creating AdminCategoryAdapter with " + categories.size() + " categories");
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "Creating new ViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        try {
            Category category = categories.get(position);
            Log.d(TAG, "Binding category at position " + position + ": " + category.getName());
            
            holder.tvStt.setText(String.valueOf(position + 1));
            holder.tvCategoryName.setText(category.getName());

            holder.btnEdit.setOnClickListener(v -> {
                Log.d(TAG, "Edit button clicked for category: " + category.getName());
                if (listener != null) {
                    listener.onEditClick(category);
                }
            });

            holder.btnDelete.setOnClickListener(v -> {
                Log.d(TAG, "Delete button clicked for category: " + category.getName());
                if (listener != null) {
                    listener.onDeleteClick(category);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onBindViewHolder", e);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<Category> newCategories) {
        Log.d(TAG, "Updating categories: " + newCategories.size() + " items");
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvStt;
        TextView tvCategoryName;
        ImageButton btnEdit;
        ImageButton btnDelete;

        CategoryViewHolder(View itemView) {
            super(itemView);
            try {
                tvStt = itemView.findViewById(R.id.tvStt);
                tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            } catch (Exception e) {
                Log.e(TAG, "Error in CategoryViewHolder constructor", e);
            }
        }
    }
} 