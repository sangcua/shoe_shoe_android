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
            String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_ALL_ORDERS;
            Log.d(TAG, "Fetching all orders from: " + url);

            JsonObjectRequest request = ApiConfig.createAuthenticatedRequest(
                context,
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "Get orders response: " + response.toString());
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject data = response.getJSONObject("data");
                            JSONArray ordersArray = data.getJSONArray("orders");
                            List<Order> orders = new ArrayList<>();

                            for (int i = 0; i < ordersArray.length(); i++) {
                                JSONObject orderObj = ordersArray.getJSONObject(i);
                                Order order = parseOrderFromJson(orderObj);
                                orders.add(order);
                            }
                            callback.onSuccess(orders);
                        } else {
                            callback.onError("Failed to fetch orders");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing orders response: " + e.getMessage());
                        callback.onError("Error parsing response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Get orders error: " + error.toString());
                    if (error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            JSONObject errorObj = new JSONObject(responseBody);
                            callback.onError(errorObj.getString("message"));
                        } catch (Exception e) {
                            callback.onError("Network error: " + error.getMessage());
                        }
                    } else {
                        callback.onError("Network error: " + error.getMessage());
                    }
                }
            );

            request.setTag(TAG);
            requestQueue.add(request);
        }

    public void updateOrderStatus(String orderId, String status, OrderCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_ORDERS + "/" + orderId + "/" + status;
        Log.d(TAG, "Updating order status: " + url);

        JsonObjectRequest request = ApiConfig.createAuthenticatedRequest(
            context,
            Request.Method.PUT,
            url,
            null,
            response -> {
                Log.d(TAG, "Update order status response: " + response.toString());
                try {
                    if (response.getBoolean("success")) {
                        callback.onSuccess(response.getString("message"));
                    } else {
                        callback.onError(response.getString("message"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing update status response: " + e.getMessage());
                    callback.onError("Error parsing response: " + e.getMessage());
                }
            },
            error -> {
                Log.e(TAG, "Update order status error: " + error.toString());
                if (error.networkResponse != null) {
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject errorObj = new JSONObject(responseBody);
                        callback.onError(errorObj.getString("message"));
                    } catch (Exception e) {
                        callback.onError("Network error: " + error.getMessage());
                    }
                } else {
                    callback.onError("Network error: " + error.getMessage());
                }
            }
        );

        request.setTag(TAG);
        requestQueue.add(request);
    }

    private Order parseOrderFromJson(JSONObject orderObj) throws Exception {
        Log.d(TAG, "Parsing order JSON: " + orderObj.toString());
        
        Order order = new Order();
        try {
            order.setId(orderObj.getString("_id"));
            order.setUserId(orderObj.getString("userId"));
            order.setName(orderObj.getString("name"));
            order.setPhone(orderObj.getString("phone"));
            order.setAddress(orderObj.getString("address"));
            order.setPrice(orderObj.getString("price"));
            order.setStatus(orderObj.getString("status"));
            order.setDeleted(orderObj.getBoolean("deleted"));
            order.setCreatedAt(orderObj.getString("createdAt"));
            order.setUpdatedAt(orderObj.getString("updatedAt"));

            JSONArray productsArray = orderObj.getJSONArray("products");
            List<Order.OrderProduct> products = new ArrayList<>();
            
            for (int i = 0; i < productsArray.length(); i++) {
                JSONObject productObj = productsArray.getJSONObject(i);
                Log.d(TAG, "Parsing product: " + productObj.toString());
                
                Order.OrderProduct product = new Order.OrderProduct(
                    productObj.getString("_id"),
                    productObj.getString("name"),
                    productObj.getString("image"),
                    productObj.getString("price"),
                    productObj.getInt("size"),
                    productObj.getInt("quantity")
                );
                products.add(product);
                Log.d(TAG, "Product parsed successfully: " + product.getName());
            }
            order.setProducts(products);
            
            Log.d(TAG, "Order parsed successfully: ID=" + order.getId() + 
                      ", Name=" + order.getName() + 
                      ", Status=" + order.getStatus() + 
                      ", Products count=" + products.size());
            
            return order;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing order: " + e.getMessage());
            Log.e(TAG, "Problematic JSON: " + orderObj.toString());
            throw e;
        }
    }

    private Order parseUserOrderFromJson(JSONObject orderObj) throws Exception {
        Log.d(TAG, "Parsing user order JSON: " + orderObj.toString());
        
        Order order = new Order();
        try {
            order.setId(orderObj.getString("_id"));
            order.setUserId(orderObj.getString("userId"));
            order.setName(orderObj.getString("name"));
            order.setPhone(orderObj.getString("phone"));
            order.setAddress(orderObj.getString("address"));
            order.setPrice(orderObj.getString("price"));
            order.setStatus(orderObj.getString("status"));
            order.setDeleted(orderObj.getBoolean("deleted"));
            order.setCreatedAt(orderObj.getString("createdAt"));
            order.setUpdatedAt(orderObj.getString("updatedAt"));

            JSONArray productsArray = orderObj.getJSONArray("products");
            List<Order.OrderProduct> products = new ArrayList<>();
            
            for (int i = 0; i < productsArray.length(); i++) {
                JSONObject productObj = productsArray.getJSONObject(i);
                Log.d(TAG, "Parsing user order product: " + productObj.toString());
                
                Order.OrderProduct product = new Order.OrderProduct(
                    productObj.getString("_id"),
                    productObj.getString("name"),
                    productObj.getString("image"),
                    productObj.getString("price"),
                    productObj.getInt("size"),
                    productObj.getInt("quantity")
                );
                products.add(product);
                Log.d(TAG, "User order product parsed successfully: " + product.getName());
            }
            order.setProducts(products);
            
            Log.d(TAG, "User order parsed successfully: ID=" + order.getId() + 
                      ", Name=" + order.getName() + 
                      ", Status=" + order.getStatus() + 
                      ", Products count=" + products.size());
            
            return order;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing user order: " + e.getMessage());
            Log.e(TAG, "Problematic JSON: " + orderObj.toString());
            throw e;
        }
    }

    public void getUserOrders(GetOrdersCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_ORDERS;
        Log.d(TAG, "=== Starting getUserOrders ===");
        Log.d(TAG, "Request URL: " + url);
        Log.d(TAG, "User token: " + (tokenManager.getToken() != null ? "Token exists" : "No token"));

        JsonObjectRequest request = ApiConfig.createAuthenticatedRequest(
                context,
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "=== Response received ===");
                    Log.d(TAG, "Raw response: " + response.toString());
                    try {
                        if (response.getBoolean("success")) {
                            Log.d(TAG, "Response success is true");
                            JSONArray ordersArray = response.getJSONArray("data");
                            Log.d(TAG, "Number of orders found: " + ordersArray.length());
                            
                            List<Order> orders = new ArrayList<>();
                            for (int i = 0; i < ordersArray.length(); i++) {
                                JSONObject orderObj = ordersArray.getJSONObject(i);
                                Log.d(TAG, "Processing user order " + (i + 1) + ": " + orderObj.toString());
                                
                                try {
                                    Order order = parseUserOrderFromJson(orderObj);
                                    Log.d(TAG, "Successfully parsed user order: ID=" + order.getId() + 
                                          ", UserID=" + order.getUserId() + 
                                          ", Status=" + order.getStatus());
                                    orders.add(order);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing user order at index " + i + ": " + e.getMessage());
                                }
                            }
                            
                            Log.d(TAG, "Total user orders parsed successfully: " + orders.size());
                            callback.onSuccess(orders);
                        } else {
                            String message = response.has("message") ? response.getString("message") : "Failed to fetch user orders";
                            Log.e(TAG, "Response success is false. Message: " + message);
                            callback.onError(message);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing response: " + e.getMessage());
                        Log.e(TAG, "Stack trace: ", e);
                        callback.onError("Error parsing response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "=== Network Error ===");
                    Log.e(TAG, "Error type: " + error.getClass().getSimpleName());
                    Log.e(TAG, "Error message: " + error.getMessage());
                    
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Network response status code: " + error.networkResponse.statusCode);
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            Log.e(TAG, "Error response body: " + responseBody);
                            JSONObject errorObj = new JSONObject(responseBody);
                            String errorMessage = errorObj.has("message") ? errorObj.getString("message") : "Unknown network error";
                            Log.e(TAG, "Parsed error message: " + errorMessage);
                            callback.onError(errorMessage);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error response: " + e.getMessage());
                            callback.onError("Network error: " + error.getMessage());
                        }
                    } else {
                        Log.e(TAG, "No network response available");
                        callback.onError("Network error: " + error.getMessage());
                    }
                }
        );

        request.setTag(TAG);
        Log.d(TAG, "Adding request to queue");
        requestQueue.add(request);
        Log.d(TAG, "=== Request added to queue ===");
    }
} 
