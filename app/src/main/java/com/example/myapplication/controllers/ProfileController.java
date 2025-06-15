package com.example.myapplication.controllers;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.models.Address;
import com.example.myapplication.models.UserProfile;
import com.example.myapplication.utils.ApiConfig;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ProfileController {
    private static final String TAG = "ProfileController";
    private Context context;
    private RequestQueue requestQueue;
    private TokenManager tokenManager;

    public interface ProfileCallback {
        void onSuccess(UserProfile userProfile);
        void onError(String message);
    }

    public interface UpdateProfileCallback {
        void onSuccess(String message, UserProfile userProfile);
        void onError(String message);
    }

    public interface GetAddressesCallback {
        void onSuccess(List<Address> addresses);
        void onError(String message);
    }

    public ProfileController(Context context) {
        this.context = context;
        this.requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        this.tokenManager = TokenManager.getInstance(context);
    }

    public void fetchUserProfile(ProfileCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_USER_PROFILE;
        Log.d(TAG, "Fetching user profile from: " + url);

        JsonObjectRequest request = ApiConfig.createAuthenticatedRequest(
            context,
            Request.Method.GET,
            url,
            null,
            response -> {
                Log.d(TAG, "Profile response received: " + response.toString());
                try {
                    if (response.getBoolean("success")) {
                        JSONObject userData = response.getJSONObject("data")
                                                   .getJSONObject("user");
                        
                        UserProfile userProfile = parseUserProfile(userData);
                        callback.onSuccess(userProfile);
                    } else {
                        String message = response.getString("message");
                        Log.e(TAG, "API returned error: " + message);
                        callback.onError(message);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                    callback.onError("Lỗi xử lý dữ liệu: " + e.getMessage());
                }
            },
            error -> {
                Log.e(TAG, "Network error", error);
                callback.onError("Lỗi kết nối server");
            }
        );

        request.setTag(TAG);
        requestQueue.add(request);
    }

    public void updateProfile(String field, String value, UpdateProfileCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_USER_PROFILE;
        Log.d(TAG, "Updating profile - Field: " + field + ", Value: " + value);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put(field, value);

            JsonObjectRequest request = ApiConfig.createAuthenticatedRequest(
                context,
                Request.Method.PUT,
                url,
                requestBody,
                response -> {
                    Log.d(TAG, "Update response received: " + response.toString());
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        
                        if (success) {
                            JSONObject userData = response.getJSONObject("data")
                                                       .getJSONObject("user");
                            
                            UserProfile userProfile = parseUserProfile(userData);
                            callback.onSuccess(message, userProfile);
                        } else {
                            Log.e(TAG, "API returned error: " + message);
                            callback.onError(message);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        callback.onError("Lỗi xử lý dữ liệu");
                    }
                },
                error -> {
                    Log.e(TAG, "Network error", error);
                    callback.onError("Lỗi kết nối server");
                }
            );

            request.setTag(TAG);
            requestQueue.add(request);
        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
            callback.onError("Lỗi tạo yêu cầu");
        }
    }

    public void getAddresses(GetAddressesCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_USER_PROFILE;
        Log.d(TAG, "Getting addresses from URL: " + url);

        JsonObjectRequest request = ApiConfig.createAuthenticatedRequest(
            context,
            Request.Method.GET,
            url,
            null,
            response -> {
                Log.d(TAG, "Get addresses response: " + response.toString());
                try {
                    if (response.getBoolean("success")) {
                        JSONObject userData = response.getJSONObject("data").getJSONObject("user");
                        JSONArray addressesArray = userData.getJSONArray("addresses");
                        Log.d(TAG, "Found " + addressesArray.length() + " addresses");

                        List<Address> addresses = new ArrayList<>();
                        for (int i = 0; i < addressesArray.length(); i++) {
                            JSONObject addressObj = addressesArray.getJSONObject(i);
                            Log.d(TAG, "Processing address " + (i + 1) + ": " + addressObj.toString());
                            try {
                                Address address = new Address(
                                    addressObj.getString("_id"),
                                    addressObj.getString("name"),
                                    addressObj.optString("phone", ""),
                                    addressObj.getString("address"),
                                    addressObj.optBoolean("isDefault", false)
                                );
                                addresses.add(address);
                                Log.d(TAG, "Successfully added address: " + address.getName());
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing address at index " + i + ": " + e.getMessage());
                                Log.e(TAG, "Address data: " + addressObj.toString());
                            }
                        }
                        
                        Log.d(TAG, "Successfully parsed " + addresses.size() + " addresses");
                        callback.onSuccess(addresses);
                    } else {
                        String message = response.getString("message");
                        Log.e(TAG, "API returned error: " + message);
                        callback.onError(message);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing addresses: " + e.getMessage());
                    Log.e(TAG, "Full response: " + response.toString());
                    e.printStackTrace();
                    callback.onError("Lỗi xử lý dữ liệu: " + e.getMessage());
                }
            },
            error -> {
                Log.e(TAG, "Error getting addresses: " + error.toString());
                if (error.networkResponse != null) {
                    Log.e(TAG, "Error status code: " + error.networkResponse.statusCode);
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        Log.e(TAG, "Error response body: " + responseBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response: " + e.getMessage());
                    }
                }
                callback.onError("Không thể tải danh sách địa chỉ: " + error.getMessage());
            }
        );

        Log.d(TAG, "Before adding request to queue");
        VolleySingleton.getInstance(context).addToRequestQueue(request);
        Log.d(TAG, "After adding request to queue");
    }

    public void addAddress(String name, String phone, String address, boolean isDefault, UpdateProfileCallback callback) {
        Log.e(TAG, "Starting addAddress method"); // Using Log.e to ensure visibility
        try {
            String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_USER_ADDRESSES;
            Log.e(TAG, "URL for adding address: " + url);
            
            // Log input parameters
            Log.e(TAG, "Input parameters - name: " + name + ", phone: " + phone + 
                      ", address: " + address + ", isDefault: " + isDefault);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("name", name);
            requestBody.put("phone", phone);
            requestBody.put("address", address);
            requestBody.put("isDefault", isDefault);
            Log.e(TAG, "Request body created: " + requestBody.toString());

            JsonObjectRequest request = ApiConfig.createAuthenticatedRequest(
                context,
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    Log.e(TAG, "Response received from server: " + response.toString());
                    try {
                        if (response.getBoolean("success")) {
                            String message = response.getString("message");
                            Log.e(TAG, "Success message from server: " + message);

                            JSONArray addressesArray = response.getJSONObject("data").getJSONArray("addresses");
                            Log.e(TAG, "Number of addresses in response: " + addressesArray.length());

                            List<Address> addresses = new ArrayList<>();
                            for (int i = 0; i < addressesArray.length(); i++) {
                                JSONObject addressObj = addressesArray.getJSONObject(i);
                                Log.e(TAG, "Processing address " + (i + 1) + ": " + addressObj.toString());
                                
                                Address newAddress = new Address(
                                    addressObj.getString("_id"),
                                    addressObj.getString("name"),
                                    addressObj.getString("phone"),
                                    addressObj.getString("address"),
                                    addressObj.getBoolean("isDefault")
                                );
                                addresses.add(newAddress);
                            }

                            UserProfile tempProfile = new UserProfile(
                                "",  // id
                                "", // username
                                "", // email
                                "", // phone
                                ""  // avatar
                            );
                            tempProfile.setAddresses(addresses);
                            
                            Log.e(TAG, "Successfully created UserProfile with " + addresses.size() + " addresses");
                            callback.onSuccess(message, tempProfile);
                        } else {
                            String message = response.getString("message");
                            Log.e(TAG, "Server returned error: " + message);
                            callback.onError(message);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                        Log.e(TAG, "Full stack trace", e);
                        callback.onError("Lỗi xử lý dữ liệu: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Network error occurred: " + error.toString());
                    Log.e(TAG, "Full error details", error);
                    
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        Log.e(TAG, "Error status code: " + statusCode);
                        
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            Log.e(TAG, "Error response body: " + responseBody);
                            
                            JSONObject errorObj = new JSONObject(responseBody);
                            if (errorObj.has("message")) {
                                String errorMessage = errorObj.getString("message");
                                Log.e(TAG, "Error message from server: " + errorMessage);
                                callback.onError(errorMessage);
                                return;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error response", e);
                        }
                    }
                    callback.onError("Không thể thêm địa chỉ mới: " + error.getMessage());
                }
            );

            Log.e(TAG, "Adding request to queue");
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            Log.e(TAG, "Request successfully added to queue");
            
        } catch (Exception e) {
            Log.e(TAG, "Critical error in addAddress", e);
            Log.e(TAG, "Error message: " + e.getMessage());
            Log.e(TAG, "Error cause: " + (e.getCause() != null ? e.getCause().getMessage() : "Unknown"));
            callback.onError("Lỗi nghiêm trọng khi thêm địa chỉ: " + e.getMessage());
        }
    }

    private UserProfile parseUserProfile(JSONObject userData) throws Exception {
        String id = userData.getString("_id");
        String username = userData.getString("username");
        String email = userData.getString("email");
        String phone = userData.optString("phone", "");
        String avatar = userData.optString("avatar", "");

        UserProfile userProfile = new UserProfile(id, username, email, phone, avatar);

        // Parse addresses if available
        if (userData.has("addresses")) {
            JSONArray addressesArray = userData.getJSONArray("addresses");
            List<Address> addresses = new ArrayList<>();

            for (int i = 0; i < addressesArray.length(); i++) {
                JSONObject addressObj = addressesArray.getJSONObject(i);
                try {
                    String addressId = addressObj.getString("_id");
                    String addressName = addressObj.getString("name");
                    String addressValue = addressObj.getString("address");
                    String addressPhone = addressObj.optString("phone", "");
                    boolean isDefault = addressObj.optBoolean("isDefault", false);

                    Address address = new Address(
                        addressId,
                        addressName,
                        addressValue,
                        addressPhone,
                        isDefault
                    );
                    addresses.add(address);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing address at index " + i + ": " + e.getMessage());
                    continue;
                }
            }

            userProfile.setAddresses(addresses);
        }

        return userProfile;
    }

    public void cancelRequests() {
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
} 