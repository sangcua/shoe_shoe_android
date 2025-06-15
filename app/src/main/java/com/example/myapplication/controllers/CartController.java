package com.example.myapplication.controllers;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.example.myapplication.models.CartItem;
import com.example.myapplication.utils.ApiConfig;
import com.example.myapplication.utils.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CartController {
    private static final String TAG = "CartController";
    private final Context context;
    private final RequestQueue requestQueue;

    public interface CartCallback {
        void onSuccess(String message);
        void onError(String message);
    }

    public interface CartDataCallback {
        void onSuccess(List<CartItem> cartItems);
        void onError(String message);
    }

    public CartController(Context context) {
        this.context = context;
        this.requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    public void getCart(CartDataCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_CART;
        Log.d(TAG, "Fetching cart data from: " + url);

        requestQueue.add(ApiConfig.createAuthenticatedRequest(
            context,
            Request.Method.GET,
            url,
            null,
            response -> {
                Log.d(TAG, "Cart response received: " + response.toString());
                try {
                    if (response.getBoolean("success")) {
                        JSONObject data = response.getJSONObject("data");
                        JSONArray cartArray = data.getJSONArray("cart");
                        List<CartItem> cartItems = new ArrayList<>();

                        for (int i = 0; i < cartArray.length(); i++) {
                            JSONObject item = cartArray.getJSONObject(i);
                            CartItem cartItem = new CartItem(
                                item.getString("_id"),
                                item.getString("productId"),
                                item.getString("productName"),
                                item.getDouble("price"),
                                item.getString("image"),
                                item.getString("category"),
                                item.getInt("size"),
                                item.getInt("quantity")
                            );
                            cartItems.add(cartItem);
                        }
                        callback.onSuccess(cartItems);
                    } else {
                        callback.onError("Failed to load cart data");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing cart data: " + e.getMessage());
                    callback.onError("Error parsing cart data");
                }
            },
            error -> {
                Log.e(TAG, "Error fetching cart: " + error.toString());
                callback.onError("Network error: " + error.getMessage());
            }
        ));
    }

    public void addToCart(String productId, int size, int quantity, CartCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_CART_ADD;
        Log.d(TAG, "Adding to cart: productId=" + productId + ", size=" + size + ", quantity=" + quantity);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("productId", productId);
            requestBody.put("size", size);
            requestBody.put("quantity", quantity);

            Log.d(TAG, "Request URL: " + url);
            Log.d(TAG, "Request body: " + requestBody.toString());

            requestQueue.add(ApiConfig.createAuthenticatedRequest(
                context,
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    Log.d(TAG, "Response received: " + response.toString());
                    try {
                        if (response.getBoolean("success")) {
                            callback.onSuccess("Đã thêm vào giỏ hàng");
                        } else {
                            callback.onError(response.getString("message"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                        callback.onError("Lỗi xử lý dữ liệu");
                    }
                },
                error -> {
                    Log.e(TAG, "Network error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Error status code: " + error.networkResponse.statusCode);
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            Log.e(TAG, "Error response body: " + responseBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error response body: " + e.getMessage());
                        }
                    }
                    callback.onError("Lỗi kết nối server");
                }
            ));
        } catch (Exception e) {
            Log.e(TAG, "Error creating request: " + e.getMessage());
            callback.onError("Lỗi xử lý dữ liệu");
        }
    }

    public void updateCartItem(String itemId, int quantity, CartCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_CART + "/update/" + itemId;
        Log.d(TAG, "Updating cart item: itemId=" + itemId + ", quantity=" + quantity);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("quantity", quantity);

            requestQueue.add(ApiConfig.createAuthenticatedRequest(
                context,
                Request.Method.PUT,
                url,
                requestBody,
                response -> {
                    Log.d(TAG, "Update response: " + response.toString());
                    try {
                        if (response.getBoolean("success")) {
                            callback.onSuccess(response.getString("message"));
                        } else {
                            callback.onError(response.getString("message"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing update response: " + e.getMessage());
                        callback.onError("Lỗi cập nhật giỏ hàng");
                    }
                },
                error -> {
                    Log.e(TAG, "Update error: " + error.toString());
                    callback.onError("Lỗi kết nối: " + error.getMessage());
                }
            ));
        } catch (Exception e) {
            Log.e(TAG, "Error creating update request: " + e.getMessage());
            callback.onError("Lỗi tạo yêu cầu");
        }
    }

    public void removeFromCart(String itemId, CartCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_CART + "/remove/" + itemId;
        Log.d(TAG, "Removing cart item: itemId=" + itemId);

        requestQueue.add(ApiConfig.createAuthenticatedRequest(
            context,
            Request.Method.DELETE,
            url,
            null,
            response -> {
                Log.d(TAG, "Remove response: " + response.toString());
                try {
                    if (response.getBoolean("success")) {
                        callback.onSuccess(response.getString("message"));
                    } else {
                        callback.onError(response.getString("message"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing remove response: " + e.getMessage());
                    callback.onError("Lỗi xóa sản phẩm");
                }
            },
            error -> {
                Log.e(TAG, "Remove error: " + error.toString());
                callback.onError("Lỗi kết nối: " + error.getMessage());
            }
        ));
    }
} 