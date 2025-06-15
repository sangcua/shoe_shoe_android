package com.example.myapplication.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.controllers.CartController;
import com.example.myapplication.models.CartItem;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private final Context context;
    private List<CartItem> cartItems;
    private final NumberFormat currencyFormatter;
    private final Set<String> selectedItems;
    private final CartController cartController;
    private OnCartUpdateListener cartUpdateListener;
    private final Map<String, Integer> originalQuantities = new HashMap<>();
    private final Set<String> modifiedItems = new HashSet<>();

    public interface OnCartUpdateListener {
        void onCartUpdated();
        void onSelectionChanged(int selectedCount);
        void showLoading(String message);
        void hideLoading();
        void showMessage(String message);
        void onQuantityChanged(boolean hasChanges);
    }

    public CartAdapter(Context context) {
        this.context = context;
        this.cartController = new CartController(context);
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.selectedItems = new HashSet<>();
        this.cartItems = new ArrayList<>();
    }

    public void setCartUpdateListener(OnCartUpdateListener listener) {
        this.cartUpdateListener = listener;
    }

    public void setCartItems(List<CartItem> items) {
        if (items == null) {
            this.cartItems = new ArrayList<>();
        } else {
            this.cartItems = new ArrayList<>(items);
            // Đảo ngược danh sách để hiển thị sản phẩm mới nhất lên đầu
            Collections.reverse(this.cartItems);
        }
        notifyDataSetChanged();
    }

    public Set<String> getSelectedItems() {
        return new HashSet<>(selectedItems);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
        if (cartUpdateListener != null) {
            cartUpdateListener.onSelectionChanged(0);
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        try {
            CartItem item = cartItems.get(position);
            if (item == null) {
                return;
            }

            // Store original quantity if not already stored
            if (!originalQuantities.containsKey(item.getStringId())) {
                originalQuantities.put(item.getStringId(), item.getQuantity());
            }

            // Load product image using Glide
            try {
                if (item.getImage() != null && !item.getImage().isEmpty()) {
                    Glide.with(context)
                        .load(item.getImage())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(holder.ivProductImage);
                } else {
                    holder.ivProductImage.setImageResource(R.drawable.error_image);
                }
            } catch (Exception e) {
                holder.ivProductImage.setImageResource(R.drawable.error_image);
            }

            // Set product details
            holder.tvProductName.setText(item.getProductName() != null ? item.getProductName() : "");
            holder.tvSize.setText("Size: " + item.getSize());
            holder.tvPrice.setText(currencyFormatter.format(item.getPrice()));
            holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Handle checkbox state
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setChecked(selectedItems.contains(item.getStringId()));
            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedItems.add(item.getStringId());
                } else {
                    selectedItems.remove(item.getStringId());
                }
                if (cartUpdateListener != null) {
                    cartUpdateListener.onSelectionChanged(selectedItems.size());
                }
            });

            // Handle quantity changes
            holder.btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    // Automatically check the item
                    if (!selectedItems.contains(item.getStringId())) {
                        selectedItems.add(item.getStringId());
                        holder.cbSelect.setChecked(true);
                        if (cartUpdateListener != null) {
                            cartUpdateListener.onSelectionChanged(selectedItems.size());
                        }
                    }

                    item.setQuantity(item.getQuantity() - 1);
                    holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
                    checkQuantityChanged(item);
                }
            });

            holder.btnIncrease.setOnClickListener(v -> {
                // Automatically check the item
                if (!selectedItems.contains(item.getStringId())) {
                    selectedItems.add(item.getStringId());
                    holder.cbSelect.setChecked(true);
                    if (cartUpdateListener != null) {
                        cartUpdateListener.onSelectionChanged(selectedItems.size());
                    }
                }

                item.setQuantity(item.getQuantity() + 1);
                holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
                checkQuantityChanged(item);
            });

        } catch (Exception e) {
            // Silent catch
        }
    }

    private void checkQuantityChanged(CartItem item) {
        int originalQuantity = originalQuantities.getOrDefault(item.getStringId(), item.getQuantity());
        if (originalQuantity != item.getQuantity()) {
            modifiedItems.add(item.getStringId());
        } else {
            modifiedItems.remove(item.getStringId());
        }

        if (cartUpdateListener != null) {
            cartUpdateListener.onQuantityChanged(!modifiedItems.isEmpty());
        }
    }

    public void saveAllModifiedItems() {
        if (modifiedItems.isEmpty()) {
            return;
        }

        if (cartUpdateListener != null) {
            cartUpdateListener.showLoading("Đang cập nhật số lượng...");
        }

        // Count for tracking completion
        final int[] completedCount = {0};
        final int totalCount = modifiedItems.size();
        final boolean[] hasError = {false};

        for (String itemId : modifiedItems) {
            CartItem item = findCartItemById(itemId);
            if (item == null) continue;

            cartController.updateCartItem(itemId, item.getQuantity(), new CartController.CartCallback() {
                @Override
                public void onSuccess(String message) {
                    completedCount[0]++;
                    originalQuantities.put(itemId, item.getQuantity());
                    
                    if (completedCount[0] == totalCount) {
                        modifiedItems.clear();
                        if (cartUpdateListener != null) {
                            cartUpdateListener.hideLoading();
                            if (!hasError[0]) {
                                // Bỏ chọn tất cả các items đã được lưu
                                selectedItems.clear();
                                cartUpdateListener.onSelectionChanged(0);
                                cartUpdateListener.showMessage("Cập nhật số lượng thành công");
                                cartUpdateListener.onQuantityChanged(false);
                                cartUpdateListener.onCartUpdated();
                                notifyDataSetChanged(); // Cập nhật lại UI để hiển thị trạng thái checkbox
                            }
                        }
                    }
                }

                @Override
                public void onError(String message) {
                    completedCount[0]++;
                    hasError[0] = true;
                    
                    if (cartUpdateListener != null) {
                        cartUpdateListener.showMessage(message);
                        // Revert quantity
                        item.setQuantity(originalQuantities.getOrDefault(itemId, item.getQuantity()));
                    }

                    if (completedCount[0] == totalCount) {
                        if (cartUpdateListener != null) {
                            cartUpdateListener.hideLoading();
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

    private CartItem findCartItemById(String itemId) {
        if (cartItems == null) return null;
        for (CartItem item : cartItems) {
            if (item.getStringId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    public void deleteSelectedItems() {
        if (cartUpdateListener != null) {
            cartUpdateListener.showLoading("Đang xóa sản phẩm...");
        }

        List<String> itemsToDelete = new ArrayList<>(selectedItems);
        AtomicInteger deleteCount = new AtomicInteger(itemsToDelete.size());

        for (String itemId : itemsToDelete) {
            cartController.removeFromCart(itemId, new CartController.CartCallback() {
                @Override
                public void onSuccess(String message) {
                    selectedItems.remove(itemId);
                    originalQuantities.remove(itemId);
                    if (deleteCount.decrementAndGet() == 0) {
                        if (cartUpdateListener != null) {
                            cartUpdateListener.hideLoading();
                            cartUpdateListener.showMessage("Xóa sản phẩm thành công");
                            cartUpdateListener.onCartUpdated();
                            cartUpdateListener.onSelectionChanged(selectedItems.size());
                        }
                    }
                }

                @Override
                public void onError(String message) {
                    if (deleteCount.decrementAndGet() == 0 && cartUpdateListener != null) {
                        cartUpdateListener.hideLoading();
                        cartUpdateListener.showMessage(message);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvSize;
        TextView tvPrice;
        TextView tvQuantity;
        ImageButton btnDecrease;
        ImageButton btnIncrease;
        Button btnSave;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }
} 