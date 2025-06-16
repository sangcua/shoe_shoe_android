package com.example.myapplication.utils;

import android.content.Context;
import android.content.Intent;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.views.activities.LoginActivity;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ApiConfig {
    private static final String TAG = "ApiConfigNANCJD";
    
    // Base URL - Using 10.0.2.2 for emulator or your computer's IP for physical device
    public static final String BASE_URL = "http://192.168.1.12:3000/api/";

    // API Endpoints
    public static final String ENDPOINT_PRODUCTS = "products";
    public static final String ENDPOINT_PRODUCTS_BY_CATEGORY = "products/category/";
    public static final String ENDPOINT_CATEGORIES = "categories";
    public static final String ENDPOINT_AUTH = "auth";
    public static final String ENDPOINT_USER_PROFILE = "users/profile";
    public static final String ENDPOINT_CART = "cart";
    public static final String ENDPOINT_CART_ADD = "cart/add";
    public static final String ENDPOINT_ORDERS = "orders";
    public static final String ENDPOINT_ALL_ORDERS = "orders/allorders";
    public static final String ENDPOINT_USER_ADDRESSES = "users/addresses";
    public static final String ENDPOINT_TOTAL_CATEGORIES = "totalcategories";
    public static final String ENDPOINT_TOTAL_PRODUCTS = "totalproducts";
    public static final String ENDPOINT_TOTAL_ORDERS = "totalorders";
    public static final String ENDPOINT_TOTAL_USERS = "totalusers";

    
    public static final int TIMEOUT_MS = 10000; 
    public static final int MAX_RETRIES = 2;

    
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_BAD_REQUEST = 400;
    public static final int CODE_UNAUTHORIZED = 401;
    public static final int CODE_FORBIDDEN = 403;
    public static final int CODE_NOT_FOUND = 404;
    public static final int CODE_SERVER_ERROR = 500;

    public static JsonObjectRequest createAuthenticatedRequest(
            Context context,
            int method,
            String url,
            JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        return new JsonObjectRequest(method, url, jsonRequest, 
            response -> {
                listener.onResponse(response);
            }, 
            error -> {
                if (error.networkResponse != null) {
                    int statusCode = error.networkResponse.statusCode;
                    
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        
                        if (statusCode == CODE_UNAUTHORIZED || statusCode == CODE_FORBIDDEN) {
                            String currentToken = TokenManager.getInstance(context).getToken();
                            
                            TokenManager.getInstance(context).clearAll();
                            Intent intent = new Intent(context, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(intent);
                        }
                    } catch (Exception e) {
                    }
                }
                errorListener.onErrorResponse(error);
            }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = TokenManager.getInstance(context).getToken();
                
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
    }
} 