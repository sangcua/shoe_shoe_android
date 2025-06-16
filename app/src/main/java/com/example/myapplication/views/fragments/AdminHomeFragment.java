package com.example.myapplication.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;
import com.example.myapplication.utils.ApiConfig;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.views.activities.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class AdminHomeFragment extends Fragment {

    private TextView tvTotalCategories;
    private TextView tvTotalProducts;
    private TextView tvTotalOrders;
    private TextView tvTotalUsers;
    private TokenManager tokenManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalCategories = view.findViewById(R.id.tv_total_categories);
        tvTotalProducts = view.findViewById(R.id.tv_total_products);
        tvTotalOrders = view.findViewById(R.id.tv_total_orders);
        tvTotalUsers = view.findViewById(R.id.tv_total_users);

        tokenManager = TokenManager.getInstance(requireContext());

        fetchData();
    }

    private void fetchData() {
        fetchCount(ApiConfig.ENDPOINT_TOTAL_CATEGORIES, tvTotalCategories, "totalCategories");
        fetchCount(ApiConfig.ENDPOINT_TOTAL_PRODUCTS, tvTotalProducts, "totalProducts");
        fetchCount(ApiConfig.ENDPOINT_TOTAL_ORDERS, tvTotalOrders, "totalOrders");
        fetchCount(ApiConfig.ENDPOINT_TOTAL_USERS, tvTotalUsers, "totalUsers");
    }

    private void fetchCount(String endpoint, TextView textView, String jsonKey) {
        String url = ApiConfig.BASE_URL + endpoint;
        String token = tokenManager.getToken();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject data = response.getJSONObject("data");
                            textView.setText(String.valueOf(data.getInt(jsonKey)));
                        } else {
                            textView.setText("Error");
                            Toast.makeText(requireContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("AdminHomeFragment", "JSON parsing error: " + e.getMessage());
                        textView.setText("Error");
                        Toast.makeText(requireContext(), "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("AdminHomeFragment", "Volley error: " + error.getMessage());
                    textView.setText("Error");
                    String errorMessage = "Có lỗi xảy ra";
                    if (error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            JSONObject data = new JSONObject(responseBody);
                            errorMessage = data.getString("message");
                            if (error.networkResponse.statusCode == ApiConfig.CODE_UNAUTHORIZED || error.networkResponse.statusCode == ApiConfig.CODE_FORBIDDEN) {
                                tokenManager.clearAll();
                                Intent intent = new Intent(requireContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                requireActivity().finish();
                            }
                        } catch (Exception e) {
                            Log.e("AdminHomeFragment", "Error parsing error response: " + e.getMessage());
                        }
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }
} 