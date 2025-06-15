package com.example.myapplication.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.myapplication.R;
import com.example.myapplication.views.adapters.CartAdapter;
import com.example.myapplication.controllers.CartController;
import com.example.myapplication.controllers.OrderController;
import com.example.myapplication.controllers.ProfileController;
import com.example.myapplication.models.Address;
import com.example.myapplication.models.CartItem;
import com.example.myapplication.models.UserProfile;
import com.example.myapplication.utils.LoadingManager;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.views.dialogs.AddressSelectionDialog;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class CartFragment extends Fragment implements CartAdapter.OnCartUpdateListener {
    private RecyclerView rvCart;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout emptyStateLayout;
    private CartAdapter cartAdapter;
    private CartController cartController;
    private OrderController orderController;
    private ProfileController profileController;
    private TextView tvCartTitle;
    private Button btnDeleteSelected;
    private Button btnSaveSelected;
    private TextView tvTotalAmount;
    private Button btnCheckout;
    private TextView tvSelectedAddress;
    private Button btnSelectAddress;
    private LoadingManager loadingManager;
    private NumberFormat currencyFormatter;
    private Address selectedAddress;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            initViews(view);
            setupRecyclerView();
            setupSwipeRefresh();
            setupDeleteButton();
            setupSaveButton();
            setupCheckoutButton();
            setupAddressSelection();
            showLoading("Đang tải giỏ hàng...");
            loadCartData();
        } catch (Exception e) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Có lỗi xảy ra khi khởi tạo giao diện", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews(View view) {
        try {
            rvCart = view.findViewById(R.id.rvCart);
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
            tvCartTitle = view.findViewById(R.id.tvCartTitle);
            btnDeleteSelected = view.findViewById(R.id.btnDeleteSelected);
            btnSaveSelected = view.findViewById(R.id.btnSaveSelected);
            tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
            btnCheckout = view.findViewById(R.id.btnCheckout);
            tvSelectedAddress = view.findViewById(R.id.tvSelectedAddress);
            btnSelectAddress = view.findViewById(R.id.btnSelectAddress);
            
            currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            
            if (getContext() != null) {
                cartController = new CartController(getContext());
                orderController = new OrderController(getContext());
                profileController = new ProfileController(getContext());
                loadingManager = new LoadingManager(getContext());
            } else {
                throw new IllegalStateException("Fragment context is null");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void setupRecyclerView() {
        try {
            cartAdapter = new CartAdapter(requireContext());
            cartAdapter.setCartUpdateListener(this);
            rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvCart.setAdapter(cartAdapter);
        } catch (Exception e) {
            throw e;
        }
    }

    private void setupSwipeRefresh() {
        try {
            swipeRefreshLayout.setOnRefreshListener(this::loadCartData);
        } catch (Exception e) {
            throw e;
        }
    }

    private void setupDeleteButton() {
        try {
            btnDeleteSelected.setOnClickListener(v -> {
                cartAdapter.deleteSelectedItems();
                btnDeleteSelected.setVisibility(View.GONE);
            });
        } catch (Exception e) {
            throw e;
        }
    }

    private void setupSaveButton() {
        try {
            btnSaveSelected.setOnClickListener(v -> {
                if (cartAdapter != null) {
                    cartAdapter.saveAllModifiedItems();
                }
            });
        } catch (Exception e) {
            throw e;
        }
    }

    private void setupCheckoutButton() {
        btnCheckout.setOnClickListener(v -> {
            if (selectedAddress == null) {
                showMessage("Vui lòng chọn địa chỉ giao hàng");
                return;
            }

            showLoading("Đang xử lý đơn hàng...");
            orderController.createOrder(
                selectedAddress.getName(),
                selectedAddress.getAddress(),
                new OrderController.OrderCallback() {
                    @Override
                    public void onSuccess(String message) {
                        hideLoading();
                        showMessage(message);
                        loadCartData(); // Refresh cart after successful order
                    }

                    @Override
                    public void onError(String message) {
                        hideLoading();
                        showMessage(message);
                    }
                }
            );
        });
    }

    private void setupAddressSelection() {
        btnSelectAddress.setOnClickListener(v -> {
            showLoading("Đang tải địa chỉ...");
            profileController.getAddresses(new ProfileController.GetAddressesCallback() {
                @Override
                public void onSuccess(List<Address> addresses) {
                    hideLoading();
                    if (addresses.isEmpty()) {
                        showMessage("Bạn chưa có địa chỉ giao hàng nào");
                        return;
                    }
                    showAddressSelectionDialog(addresses);
                }

                @Override
                public void onError(String message) {
                    hideLoading();
                    showMessage("Không thể tải địa chỉ: " + message);
                }
            });
        });
    }

    private void showAddressSelectionDialog(List<Address> addresses) {
        AddressSelectionDialog dialog = new AddressSelectionDialog(addresses, address -> {
            selectedAddress = address;
            updateSelectedAddressView();
        });
        dialog.show(getChildFragmentManager(), "address_selection");
    }

    private void updateSelectedAddressView() {
        if (selectedAddress != null) {
            tvSelectedAddress.setText(String.format("%s\n%s\n%s", 
                selectedAddress.getName(),
                selectedAddress.getPhone(),
                selectedAddress.getAddress()));
            tvSelectedAddress.setVisibility(View.VISIBLE);
        } else {
            tvSelectedAddress.setVisibility(View.GONE);
        }
    }

    private void loadCartData() {
        if (getContext() == null) {
            return;
        }

        String token = TokenManager.getInstance(getContext()).getToken();
        if (token == null || token.isEmpty()) {
            if (isAdded()) {
                showMessage("Vui lòng đăng nhập để xem giỏ hàng");
            }
            return;
        }

        cartController.getCart(new CartController.CartDataCallback() {
            @Override
            public void onSuccess(List<CartItem> cartItems) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        hideLoading();
                        swipeRefreshLayout.setRefreshing(false);
                        if (cartItems.isEmpty()) {
                            showEmptyState();
                        } else {
                            showCartItems(cartItems);
                        }
                        updateItemCount(cartItems.size());
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        hideLoading();
                        swipeRefreshLayout.setRefreshing(false);
                        showMessage(message);
                    });
                }
            }
        });
    }

    private void showEmptyState() {
        if (rvCart != null && emptyStateLayout != null) {
            rvCart.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            // Clear total amount when cart is empty
            tvTotalAmount.setText("");
            btnCheckout.setEnabled(false);
        }
    }

    private void showCartItems(List<CartItem> cartItems) {
        if (rvCart != null && emptyStateLayout != null && cartAdapter != null) {
            rvCart.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            cartAdapter.setCartItems(cartItems);
            updateTotalAmount(cartItems);
            btnCheckout.setEnabled(true);
        }
    }

    private void updateItemCount(int count) {
        try {
            if (tvCartTitle != null) {
                tvCartTitle.setText("Giỏ hàng của tôi (" + count + ")");
            }
        } catch (Exception e) {
        }
    }

    private void updateTotalAmount(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            tvTotalAmount.setText("");
            btnCheckout.setEnabled(false);
            return;
        }

        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        String formattedAmount = currencyFormatter.format(total);
        tvTotalAmount.setText(formattedAmount);
        btnCheckout.setEnabled(true);
    }

    @Override
    public void showMessage(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showLoading(String message) {
        if (loadingManager != null && isAdded()) {
            loadingManager.show(message);
        }
    }

    @Override
    public void hideLoading() {
        if (loadingManager != null && isAdded()) {
            loadingManager.dismiss();
        }
    }

    @Override
    public void onCartUpdated() {
        loadCartData();
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        btnDeleteSelected.setVisibility(selectedCount > 0 ? View.VISIBLE : View.GONE);
        if (selectedCount > 0) {
            btnDeleteSelected.setText("Xóa (" + selectedCount + ")");
        }
    }

    @Override
    public void onQuantityChanged(boolean hasChanges) {
        if (btnSaveSelected != null) {
            btnSaveSelected.setVisibility(hasChanges ? View.VISIBLE : View.GONE);
        }
    }
}
