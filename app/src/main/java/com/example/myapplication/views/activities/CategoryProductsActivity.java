package com.example.myapplication.views.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.controllers.ProductController;
import com.example.myapplication.models.Product;
import com.example.myapplication.utils.LoadingManager;
import com.example.myapplication.views.adapters.ProductAdapter;
import java.util.ArrayList;
import java.util.List;

public class CategoryProductsActivity extends AppCompatActivity implements ProductController.ProductCallback {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private ProductController productController;
    private LoadingManager loadingManager;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        categoryName = getIntent().getStringExtra("category");
        loadingManager = new LoadingManager(this);

        setupToolbar();
        setupRecyclerView();
        loadProducts();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName);
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black));
        }
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void loadProducts() {
        productController = new ProductController(this, this);
        loadingManager.show();
        productController.getProductsByCategory(categoryName);
    }

    @Override
    public void onSuccess(List<Product> products, boolean hasMore) {
        loadingManager.dismiss();
        adapter.updateProducts(products);
    }

    @Override
    public void onError(String message) {
        loadingManager.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 