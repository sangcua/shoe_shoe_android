package com.example.myapplication.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.views.adapters.ProductAdapter;
import com.example.myapplication.controllers.ProductController;
import com.example.myapplication.models.Product;
import com.example.myapplication.utils.LoadingManager;
import java.util.ArrayList;
import java.util.List;

public class ProductListFragment extends Fragment implements ProductController.ProductCallback {
    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> products;
    private ProductController productController;
    private LoadingManager loadingManager;
    private boolean isLoadingMore = false;
    private boolean hasMoreProducts = true;

    public static ProductListFragment newInstance() {
        return new ProductListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        products = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize LoadingManager
        loadingManager = new LoadingManager(requireContext());
        
        // Initialize RecyclerView
        productRecyclerView = view.findViewById(R.id.productRecyclerView);
        
        // Set up GridLayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        productRecyclerView.setLayoutManager(layoutManager);
        
        // Initialize adapter
        productAdapter = new ProductAdapter(requireContext(), products);
        productRecyclerView.setAdapter(productAdapter);
        
        // Add scroll listener for pagination
        productRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                if (!hasMoreProducts || isLoadingMore) {
                    return;
                }

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Load more when user scrolls to the last 5 items
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= 20) { // Only load more if we have at least one page
                        loadMoreProducts();
                    }
                }
            }
        });
        
        // Initialize controller and fetch initial data
        productController = new ProductController(requireContext(), this);
        loadingManager.show("Đang tải sản phẩm...");
        productController.fetchProducts();
    }

    private void loadMoreProducts() {
        if (!isLoadingMore && hasMoreProducts) {
            isLoadingMore = true;
            loadingManager.show("Đang tải thêm sản phẩm...");
            productController.fetchProducts();
        }
    }

    @Override
    public void onSuccess(List<Product> newProducts, boolean hasMore) {
        if (isAdded()) {
            if (newProducts.isEmpty()) {
                hasMoreProducts = false;
            } else {
                products.addAll(newProducts);
                productAdapter.notifyDataSetChanged();
                hasMoreProducts = hasMore;
            }
            isLoadingMore = false;
            loadingManager.dismiss();
        }
    }

    @Override
    public void onError(String error) {
        if (isAdded()) {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            isLoadingMore = false;
            hasMoreProducts = false; // Stop trying to load more on error
            loadingManager.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (productController != null) {
            productController.resetPagination();
        }
        if (loadingManager != null) {
            loadingManager.dismiss();
        }
    }
} 