package com.example.myapplication.controllers;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.utils.ApiConfig;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.utils.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthController {
    private final Context context;
    private final RequestQueue requestQueue;
    private final TokenManager tokenManager;

    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String message);
    }

    public AuthController(Context context) {
        this.context = context;
        this.requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        this.tokenManager = TokenManager.getInstance(context);
    }

    public void register(String email, String password, String username, String phone, AuthCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("username", username);
            requestBody.put("phone", phone);

            String registerUrl = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_AUTH + "/register";

            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                registerUrl,
                requestBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject data = response.getJSONObject("data");
                            String token = data.getString("token");
                            JSONObject user = data.getJSONObject("user");

                            // Save token and user info
                            tokenManager.saveToken(token);

                            String userId = user.getString("id");
                            tokenManager.saveUserInfo(
                                userId,
                                user.getString("username"),
                                user.getString("email"),
                                user.getString("phone"),
                                user.getString("role")
                            );

                            callback.onSuccess("Đăng ký thành công");
                        } else {
                            String message = response.getString("message");
                            callback.onError(message);
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
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            callback.onError("Lỗi xử lý dữ liệu");
        }
    }

    public void login(String email, String password, AuthCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            requestBody.put("password", password);

            String loginUrl = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_AUTH + "/login";

            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                loginUrl,
                requestBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject data = response.getJSONObject("data");
                            String token = data.getString("token");
                            JSONObject user = data.getJSONObject("user");

                            // Save token and user info
                            tokenManager.saveToken(token);

                            // Try both "id" and "_id" fields
                            String userId = user.has("_id") ? user.getString("_id") : user.getString("id");
                            
                            tokenManager.saveUserInfo(
                                userId,
                                user.getString("username"),
                                user.getString("email"),
                                user.getString("phone"),
                                user.getString("role")
                            );
                            Log.d("AuthController", "Role saved: " + user.getString("role")); // Thêm dòng này
                            callback.onSuccess("Đăng nhập thành công");
                        } else {
                            String message = response.getString("message");
                            callback.onError(message);
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
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            callback.onError("Lỗi xử lý dữ liệu");
        }
    }
} 