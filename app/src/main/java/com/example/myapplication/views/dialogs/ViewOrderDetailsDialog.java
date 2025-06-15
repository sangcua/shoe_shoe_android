package com.example.myapplication.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Order;
import com.example.myapplication.views.adapters.ViewOrderProductAdapter;
import java.text.NumberFormat;
import java.util.Locale;

public class ViewOrderDetailsDialog extends Dialog {
    private Order order;
    private NumberFormat numberFormat;

    public ViewOrderDetailsDialog(Context context, Order order) {
        super(context);
        this.order = order;
        this.numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_view_order_details);

        // Set dialog width to 90% of screen width
        Window window = getWindow();
        if (window != null) {
            window.setLayout(
                (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        // Initialize views
        TextView tvCustomerName = findViewById(R.id.tvCustomerName);
        TextView tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        TextView tvCustomerAddress = findViewById(R.id.tvCustomerAddress);
        TextView tvTotalPrice = findViewById(R.id.tvTotalPrice);
        RecyclerView rvOrderProducts = findViewById(R.id.rvOrderProducts);

        // Set customer information
        tvCustomerName.setText("Tên: " + order.getName());
        tvCustomerPhone.setText("SĐT: " + order.getPhone());
        tvCustomerAddress.setText("Địa chỉ: " + order.getAddress());
        tvTotalPrice.setText("Tổng tiền: " + numberFormat.format(Long.parseLong(order.getPrice())) + "đ");

        // Setup RecyclerView for products
        rvOrderProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        ViewOrderProductAdapter adapter = new ViewOrderProductAdapter(order.getProducts());
        rvOrderProducts.setAdapter(adapter);
    }
} 