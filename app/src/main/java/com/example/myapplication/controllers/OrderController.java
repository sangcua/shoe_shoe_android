package com.example.myapplication.controllers;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.models.Order;
import com.example.myapplication.utils.ApiConfig;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.utils.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class OrderController {
    private static final String TAG = "OrderController";
    private final Context context;
    private final RequestQueue requestQueue;
    private final TokenManager tokenManager;

    public interface OrderCallback {
        void onSuccess(String message);
        void onError(String message);
    }

    public interface GetOrdersCallback {
        void onSuccess(List<Order> orders);
        void onError(String message);
    }

    public OrderController(Context context) {
        this.context = context;
        this.requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        this.tokenManager = TokenManager.getInstance(context);
    }

    public void createOrder(String name, String address, OrderCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_ORDERS;
        Log.d(TAG, "Creating order with address: " + address);

        // Lấy số điện thoại từ TokenManager
        String phone = tokenManager.getPhone();
        if (phone == null || phone.isEmpty()) {
            callback.onError("Không tìm thấy số điện thoại người dùng");
            return;
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("name", name);
            requestBody.put("phone", phone);
            requestBody.put("address", address);

            Log.d(TAG, "Request URL: " + url);
            Log.d(TAG, "Request body: " + requestBody.toString());

            JsonObjectRequest request = ApiConfig.createAuthenticatedRequest(
                context,
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    Log.d(TAG, "Order creation response: " + response.toString());
                    try {
                        if (response.getBoolean("success")) {
                            callback.onSuccess(response.getString("message"));
                        } else {
                            callback.onError(response.getString("message"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing order response: " + e.getMessage());
                        callback.onError("Lỗi xử lý phản hồi từ server");
                    }
                },
                error -> {
                    Log.e(TAG, "Order creation error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Error status code: " + error.networkResponse.statusCode);
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            Log.e(TAG, "Error response body: " + responseBody);
                            JSONObject errorObj = new JSONObject(responseBody);
                            callback.onError(errorObj.getString("message"));
                        } catch (Exception e) {
                            callback.onError("Lỗi kết nối: " + error.getMessage());
                        }
                    } else {
                        callback.onError("Lỗi kết nối: " + error.getMessage());
                    }
                }
            );

            request.setTag(TAG);
            requestQueue.add(request);
        } catch (Exception e) {
            Log.e(TAG, "Error creating order request: " + e.getMessage());
            callback.onError("Lỗi tạo yêu cầu đặt hàng");
        }
    }

    public void getAllOrders(GetOrdersCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_ORDERS;
        Log.d(TAG, "Getting all orders from: " + url);

        JsonObjectRequest request = ApiConfig.createAuthenticatedRequest(
            context,
            Request.Method.GET,
            url,
            null,
            response -> {
                Log.d(TAG, "Orders response: " + response.toString());
                try {
                    if (response.getBoolean("success")) {
                        JSONArray ordersArray = response.getJSONArray("data");
                        List<Order> orders = new ArrayList<>();

                        for (int i = 0; i < ordersArray.length(); i++) {
                            JSONObject orderObj = ordersArray.getJSONObject(i);
                            
                            // Parse products array
                            JSONArray productsArray = orderObj.getJSONArray("products");
                            List<Order.OrderProduct> products = new ArrayList<>();
                            
                            for (int j = 0; j < productsArray.length(); j++) {
                                JSONObject productObj = productsArray.getJSONObject(j);
                                Order.OrderProduct product = new Order.OrderProduct(
                                    productObj.getString("_id"),
                                    productObj.getString("name"),
                                    productObj.getString("image"),
                                    productObj.getString("price"),
                                    productObj.getInt("size"),
                                    productObj.getInt("quantity")
                                );
                                products.add(product);
                            }

                            Order order = new Order(
                                orderObj.getString("_id"),
                                orderObj.getString("userId"),
                                orderObj.getString("name"),
                                orderObj.getString("phone"),
                                products,
                                orderObj.getString("price"),
                                orderObj.getString("address"),
                                orderObj.getString("status"),
                                orderObj.getBoolean("deleted"),
                                orderObj.getString("createdAt"),
                                orderObj.getString("updatedAt")
                            );
                            Log.d(TAG, "Order status from API: " + orderObj.getString("status"));
                            orders.add(order);
                        }
                        callback.onSuccess(orders);
                    } else {
                        callback.onError("Failed to load orders");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing orders: " + e.getMessage());
                    callback.onError("Error parsing orders data");
                }
            },
            error -> {
                Log.e(TAG, "Error getting orders: " + error.toString());
                callback.onError("Network error: " + error.getMessage());
            }
        );

        requestQueue.add(request);
    }
} 