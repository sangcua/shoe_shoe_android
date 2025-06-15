package com.example.myapplication.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.controllers.CategoryController;
import com.example.myapplication.models.Category;
import com.example.myapplication.utils.LoadingManager;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.views.activities.CategoryFormActivity;
import com.example.myapplication.views.adapters.AdminCategoryAdapter;
import com.example.myapplication.views.adapters.CategoryAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment implements CategoryAdapter.OnCategoryActionListener, AdminCategoryAdapter.OnCategoryActionListener {
    private RecyclerView recyclerView;
    private CategoryAdapter userAdapter;
    private AdminCategoryAdapter adminAdapter;
    private CategoryController categoryController;
    private List<Category> categories = new ArrayList<>();
    private LoadingManager loadingManager;
    private boolean isAdmin;

    public static CategoryFragment newInstance() {
        return new CategoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        FloatingActionButton addButton = view.findViewById(R.id.addButton);
        
        // Check if user is admin
        isAdmin = TokenManager.getInstance(requireContext()).getRole().equals("admin");
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (isAdmin) {
            adminAdapter = new AdminCategoryAdapter(requireContext(), categories, this);
            recyclerView.setAdapter(adminAdapter);
            addButton.setVisibility(View.VISIBLE);
        } else {
            userAdapter = new CategoryAdapter(requireContext(), categories, this);
            recyclerView.setAdapter(userAdapter);
            addButton.setVisibility(View.GONE);
        }
        
        // Initialize controllers
        categoryController = new CategoryController(requireContext());
        loadingManager = new LoadingManager(requireContext());
        
        // Setup add button
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CategoryFormActivity.class);
            startActivityForResult(intent, 1);
        });
        
        // Load categories
        loadCategories();
        
        return view;
    }

    private void loadCategories() {
        showLoading(true);
        categoryController.getCategories(new CategoryController.CategoryListCallback() {
            @Override
            public void onSuccess(List<Category> categoryList) {
                showLoading(false);
                categories.clear();
                categories.addAll(categoryList);
                if (isAdmin) {
                    adminAdapter.updateCategories(categories);
                } else {
                    userAdapter.updateCategories(categories);
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(Category category) {
        Intent intent = new Intent(requireContext(), CategoryFormActivity.class);
        intent.putExtra("category", category);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onDeleteClick(Category category) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa danh mục này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                showLoading(true);
                categoryController.deleteCategory(category.getId(), new CategoryController.CategoryCallback() {
                    @Override
                    public void onSuccess(String message) {
                        showLoading(false);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        loadCategories();
                    }

                    @Override
                    public void onError(String message) {
                        showLoading(false);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingManager.show("Đang tải...");
        } else {
        loadingManager.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == -1) {
            loadCategories();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loadingManager != null) {
        loadingManager.dismiss();
        }
    }
} 