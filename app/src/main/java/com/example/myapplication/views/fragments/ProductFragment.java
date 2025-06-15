package com.example.myapplication.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.myapplication.controllers.ProductController;
import com.example.myapplication.models.Product;
import com.example.myapplication.utils.LoadingManager;
import com.example.myapplication.views.activities.AddProductActivity;
import com.example.myapplication.views.adapters.AdminProductAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment implements AdminProductAdapter.OnProductActionListener, ProductController.ProductCallback {
    private static final String TAG = "ProductFragment";
    private RecyclerView recyclerView;
    private AdminProductAdapter adapter;
    private ProductController productController;
    private List<Product> products = new ArrayList<>();
    private LoadingManager loadingManager;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private FloatingActionButton fabAddProduct;

    public static ProductFragment newInstance() {
        return new ProductFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Starting to create view");
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        fabAddProduct = view.findViewById(R.id.fabAddProduct);
        
        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AdminProductAdapter(requireContext(), products, this);
        recyclerView.setAdapter(adapter);

        // Add scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && hasMoreData) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        Log.d(TAG, "onScrolled: Loading more products");
                        loadProducts();
                    }
                }
            }
        });
        
        // Initialize controllers
        productController = new ProductController(requireContext(), this);
        loadingManager = new LoadingManager(requireContext());
        
        // Setup add button
        setupFab();
        
        // Load products
        loadProducts();
        
        return view;
    }

    private void setupFab() {
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddProductActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        Log.d(TAG, "loadProducts: Starting to load products. isLoading=" + isLoading + ", hasMoreData=" + hasMoreData);
        if (isLoading || !hasMoreData) {
            Log.d(TAG, "loadProducts: Skipping load - isLoading=" + isLoading + ", hasMoreData=" + hasMoreData);
            return;
        }
        showLoading(true);
        isLoading = true;
        productController.fetchProducts();
    }

    @Override
    public void onSuccess(List<Product> products, boolean hasMore) {
        Log.d(TAG, "onSuccess: Received " + products.size() + " products, hasMore=" + hasMore);
        showLoading(false);
        isLoading = false;
        hasMoreData = hasMore;
        
        // Add new products to the list
        this.products.addAll(products);
        Log.d(TAG, "onSuccess: Total products after adding: " + this.products.size());
        adapter.updateProducts(this.products);
        
        if (hasMore) {
            currentPage++;
        }
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "onError: " + error);
        showLoading(false);
        isLoading = false;
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        Log.d(TAG, "showLoading: " + show);
        if (show) {
            loadingManager.show("Đang tải...");
        } else {
            loadingManager.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loadingManager != null) {
            loadingManager.dismiss();
        }
    }

    @Override
    public void onEditClick(Product product) {

    }

    @Override
    public void onDeleteClick(Product product) {

    }
}