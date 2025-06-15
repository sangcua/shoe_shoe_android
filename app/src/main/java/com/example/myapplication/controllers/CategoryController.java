package com.example.myapplication.controllers;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.models.Category;
import com.example.myapplication.utils.ApiConfig;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.utils.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class CategoryController {
    private final Context context;
    private final RequestQueue requestQueue;
    private final TokenManager tokenManager;

    public interface CategoryCallback {
        void onSuccess(String message);
        void onError(String message);
    }

    public interface CategoryListCallback {
        void onSuccess(List<Category> categories);
        void onError(String message);
    }

    public CategoryController(Context context) {
        this.context = context;
        this.requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        this.tokenManager = TokenManager.getInstance(context);
    }

     // Make sure to import this

    public void getCategories(CategoryListCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_CATEGORIES;
        Log.d("CategoryController", "Fetching categories from URL: " + url); // Log the URL being called

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("CategoryController", "API Response: " + response.toString());

                        if (response.getBoolean("success")) {
                            JSONObject data = response.getJSONObject("data");
                            if (data.has("categories")) {
                                JSONArray categoriesArray = data.getJSONArray("categories");
                                List<Category> categories = new ArrayList<>();
                                for (int i = 0; i < categoriesArray.length(); i++) {
                                    JSONObject categoryJson = categoriesArray.getJSONObject(i);
                                    String id = categoryJson.optString("_id", "");
                                    String name = categoryJson.optString("name", "");
                                    String description = categoryJson.optString("description", "");

                                    Log.d("CategoryController", "Parsed Category: ID=" + id + ", Name=" + name + ", Description=" + description);

                                    Category category = new Category(id, name, description);
                                    categories.add(category);
                                }
                                callback.onSuccess(categories);
                                Log.d("CategoryController", "Successfully retrieved " + categories.size() + " categories.");
                            } else {
                                String errorMessage = "Invalid response format: missing 'categories' field in data";
                                Log.e("CategoryController", errorMessage);
                                callback.onError(errorMessage);
                            }
                        } else {
                            String errorMessage = response.optString("message", "Unknown error");
                            Log.e("CategoryController", "API returned success: false. Message: " + errorMessage);
                            callback.onError(errorMessage);
                        }
                    } catch (JSONException e) {
                        Log.e("CategoryController", "JSON parsing error: " + e.getMessage(), e);
                        callback.onError("Lỗi xử lý dữ liệu: " + e.getMessage());
                    }
                },
                error -> {
                    String volleyError = "Unknown Volley error";
                    if (error != null && error.getMessage() != null) {
                        volleyError = error.getMessage();
                    } else if (error != null && error.networkResponse != null) {
                        volleyError = "Status Code: " + error.networkResponse.statusCode;
                        try {
                            if (error.networkResponse.data != null) {
                                volleyError += ", Data: " + new String(error.networkResponse.data);
                            }
                        } catch (Exception e) {
                            // Ignore, just trying to get more info
                        }
                    }
                    Log.e("CategoryController", "Volley error: " + volleyError, error); // Log Volley network errors
                    callback.onError("Lỗi kết nối: " + volleyError);
                }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void createCategory(String name, String description, CategoryCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("name", name);
            requestBody.put("description", description);

            String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_CATEGORIES;

            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            callback.onSuccess(response.getString("message"));
                        } else {
                            callback.onError(response.getString("message"));
                        }
                    } catch (JSONException e) {
                        callback.onError("Lỗi xử lý dữ liệu");
                    }
                },
                error -> {
                    String message = "Lỗi kết nối";
                    if (error.networkResponse != null) {
                        try {
                            String errorResponse = new String(error.networkResponse.data);
                            JSONObject errorJson = new JSONObject(errorResponse);
                            message = errorJson.getString("message");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    callback.onError(message);
                }
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Authorization", "Bearer " + tokenManager.getToken());
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            callback.onError("Lỗi xử lý dữ liệu");
        }
    }

    public void updateCategory(String id, String name, String description, CategoryCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("name", name);
            requestBody.put("description", description);

            String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_CATEGORIES + "/" + id;

            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                requestBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            callback.onSuccess(response.getString("message"));
                        } else {
                            callback.onError(response.getString("message"));
                        }
                    } catch (JSONException e) {
                        callback.onError("Lỗi xử lý dữ liệu");
                    }
                },
                error -> {
                    String message = "Lỗi kết nối";
                    if (error.networkResponse != null) {
                        try {
                            String errorResponse = new String(error.networkResponse.data);
                            JSONObject errorJson = new JSONObject(errorResponse);
                            message = errorJson.getString("message");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    callback.onError(message);
                }
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Authorization", "Bearer " + tokenManager.getToken());
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            callback.onError("Lỗi xử lý dữ liệu");
        }
    }

    public void deleteCategory(String id, CategoryCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_CATEGORIES + "/" + id;

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.DELETE,
            url,
            null,
            response -> {
                try {
                    if (response.getBoolean("success")) {
                        callback.onSuccess(response.getString("message"));
                    } else {
                        callback.onError(response.getString("message"));
                    }
                } catch (JSONException e) {
                    callback.onError("Lỗi xử lý dữ liệu");
                }
            },
            error -> {
                String message = "Lỗi kết nối";
                if (error.networkResponse != null) {
                    try {
                        String errorResponse = new String(error.networkResponse.data);
                        JSONObject errorJson = new JSONObject(errorResponse);
                        message = errorJson.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                callback.onError(message);
            }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + tokenManager.getToken());
                return headers;
            }
        };

        requestQueue.add(request);
    }
} 