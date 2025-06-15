package com.example.myapplication.views.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.myapplication.R;
import com.example.myapplication.controllers.CartController;
import com.example.myapplication.models.Product;
import com.example.myapplication.utils.LoadingManager;
import com.example.myapplication.utils.VolleySingleton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private NetworkImageView productImage;
    private TextView productName;
    private TextView productPrice;
    private ChipGroup sizeChipGroupRow1;
    private ChipGroup sizeChipGroupRow2;
    private TextView quantityText;
    private TextView totalPrice;
    private TextView tvStockInfo;
    private MaterialButton addToCartButton;
    private ImageButton decreaseButton;
    private ImageButton increaseButton;
    private Product product;
    private Product.Size selectedSizeObj;
    private int quantity = 1;
    private NumberFormat currencyFormat;
    private ImageLoader imageLoader;
    private LoadingManager loadingManager;
    private CartController cartController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize LoadingManager and CartController
        loadingManager = new LoadingManager(this);
        cartController = new CartController(this);
        loadingManager.show();

        // Initialize views
        initViews();
        
        // Get product data from intent
        product = getIntent().getParcelableExtra("product");
        if (product == null) {
            finish();
            return;
        }

        // Setup toolbar
        setupToolbar();

        // Setup currency formatter
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        // Initialize image loader
        imageLoader = VolleySingleton.getInstance(this).getImageLoader();

        // Display product info
        displayProductInfo();

        // Setup size chips
        setupSizeChips();

        // Setup quantity buttons
        setupQuantityButtons();

        // Setup add to cart button
        setupAddToCartButton();

        // Update total price
        updateTotalPrice();

        // Hide loading when everything is ready
        loadingManager.dismiss();
    }

    private void initViews() {
        productImage = findViewById(R.id.productImage);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        sizeChipGroupRow1 = findViewById(R.id.sizeChipGroupRow1);
        sizeChipGroupRow2 = findViewById(R.id.sizeChipGroupRow2);
        quantityText = findViewById(R.id.quantityText);
        totalPrice = findViewById(R.id.totalPrice);
        tvStockInfo = findViewById(R.id.tvStockInfo);
        addToCartButton = findViewById(R.id.addToCartButton);
        decreaseButton = findViewById(R.id.decreaseButton);
        increaseButton = findViewById(R.id.increaseButton);

        // Disable quantity buttons and add to cart button initially
        decreaseButton.setEnabled(false);
        increaseButton.setEnabled(false);
        addToCartButton.setEnabled(false);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết sản phẩm");
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black));
        }
    }

    private void displayProductInfo() {
        productName.setText(product.getName());
        productPrice.setText(currencyFormat.format(product.getPrice()));
        productImage.setImageUrl(product.getImage(), imageLoader);
        productImage.setDefaultImageResId(R.drawable.sample_product);
        productImage.setErrorImageResId(R.drawable.sample_product);
        tvStockInfo.setText("Vui lòng chọn size");
    }

    private void setupSizeChips() {
        if (product.getSizes() != null) {
            List<Product.Size> availableSizes = product.getSizes().stream()
                    .filter(size -> size.getQuantity() > 0)
                    .collect(java.util.stream.Collectors.toList());

            int midPoint = (availableSizes.size() + 1) / 2;

            ChipGroup.OnCheckedChangeListener chipListener = (group, checkedId) -> {
                // Uncheck chips in the other group
                if (group == sizeChipGroupRow1) {
                    sizeChipGroupRow2.clearCheck();
                } else {
                    sizeChipGroupRow1.clearCheck();
                }

                if (checkedId != -1) {
                    Chip chip = findViewById(checkedId);
                    selectedSizeObj = (Product.Size) chip.getTag();
                    
                    // Enable quantity buttons
                    decreaseButton.setEnabled(true);
                    increaseButton.setEnabled(true);
                    addToCartButton.setEnabled(true);
                    
                    // Reset quantity when size changes
                    quantity = 1;
                    quantityText.setText(String.valueOf(quantity));
                    
                    // Update stock info
                    tvStockInfo.setText(String.format("Còn %d sản phẩm", selectedSizeObj.getQuantity()));
                    
                    // Update total price
                    updateTotalPrice();
                } else {
                    selectedSizeObj = null;
                    decreaseButton.setEnabled(false);
                    increaseButton.setEnabled(false);
                    addToCartButton.setEnabled(false);
                    tvStockInfo.setText("Vui lòng chọn size");
                }
            };

            // Add chips to first row
            for (int i = 0; i < midPoint; i++) {
                Product.Size size = availableSizes.get(i);
                Chip chip = createSizeChip(size);
                sizeChipGroupRow1.addView(chip);
            }

            // Add chips to second row
            for (int i = midPoint; i < availableSizes.size(); i++) {
                Product.Size size = availableSizes.get(i);
                Chip chip = createSizeChip(size);
                sizeChipGroupRow2.addView(chip);
            }

            sizeChipGroupRow1.setOnCheckedChangeListener(chipListener);
            sizeChipGroupRow2.setOnCheckedChangeListener(chipListener);
        }
    }

    private Chip createSizeChip(Product.Size size) {
        Chip chip = new Chip(this);
        chip.setText(String.valueOf(size.getSize()));
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setTag(size);
        return chip;
    }

    private void setupQuantityButtons() {
        decreaseButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityText.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });

        increaseButton.setOnClickListener(v -> {
            if (selectedSizeObj != null && quantity < selectedSizeObj.getQuantity()) {
                quantity++;
                quantityText.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });
    }

    private void setupAddToCartButton() {
        addToCartButton.setOnClickListener(v -> {
            if (selectedSizeObj == null) {
                Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingManager.show("Đang thêm vào giỏ hàng...");
            cartController.addToCart(
                product.getId(),
                selectedSizeObj.getSize(),
                quantity,
                new CartController.CartCallback() {
                    @Override
                    public void onSuccess(String message) {
                        loadingManager.dismiss();
                        Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        loadingManager.dismiss();
                        Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            );
        });
    }

    private void updateTotalPrice() {
        if (product != null) {
            double total = product.getPrice() * quantity;
            totalPrice.setText(currencyFormat.format(total));
        }
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