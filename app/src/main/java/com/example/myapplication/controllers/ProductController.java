package com.example.myapplication.controllers;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.models.Product;
import com.example.myapplication.models.Size;
import com.example.myapplication.utils.ApiConfig;
import com.example.myapplication.utils.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;


public class ProductController {
    private static final String TAG = "ProductController";
    private Context context;
    private final RequestQueue requestQueue;
    private ProductCallback callback;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private static final int PAGE_SIZE = 20;

    public interface ProductCallback {
        void onSuccess(List<Product> products, boolean hasMore);
        void onError(String error);
    }

    public interface ProductGetCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public ProductController(Context context, ProductCallback callback) {
        this.context = context;
        this.requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        this.callback = callback;
    }

    public void resetPagination() {
        currentPage = 1;
        hasMoreData = true;
        isLoading = false;
    }

    private Product parseProduct(JSONObject productJson) throws JSONException {
        Log.d(TAG, "parseProduct: Parsing product JSON: " + productJson.toString());
        String id = productJson.getString("_id");
        String name = productJson.getString("name");
        String category = productJson.getString("category");
        int price = productJson.getInt("price");
        String description = productJson.optString("description", "");
        String image = productJson.getString("image");
        String slug = productJson.optString("slug", "");

        // Parse sizes array
        List<Product.Size> sizes = new ArrayList<>();
        if (productJson.has("sizes")) {
            JSONArray sizesArray = productJson.getJSONArray("sizes");
            for (int i = 0; i < sizesArray.length(); i++) {
                JSONObject sizeObj = sizesArray.getJSONObject(i);
                int size = sizeObj.getInt("size");
                int quantity = sizeObj.getInt("quantity");
                String sizeId = sizeObj.optString("_id", "");
                sizes.add(new Product.Size(size, quantity, sizeId));
            }
        }

        return new Product(id, name, category, price, description, image, slug, sizes);
    }

    public void fetchProducts() {
        if (isLoading || !hasMoreData) {
            Log.d(TAG, "fetchProducts: Skipping fetch - isLoading=" + isLoading + ", hasMoreData=" + hasMoreData);
            return;
        }
        
        isLoading = true;
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_PRODUCTS + 
                    "?page=" + currentPage + "&limit=" + PAGE_SIZE;
        
        Log.d(TAG, "fetchProducts: Fetching products from URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            response -> {
                try {
                    Log.d(TAG, "fetchProducts: Received response: " + response.toString());
                    if (!response.getBoolean("success")) {
                        String error = "API request failed";
                        Log.e(TAG, "fetchProducts: " + error);
                        callback.onError(error);
                        isLoading = false;
                        return;
                    }

                    JSONObject data = response.getJSONObject("data");
                    JSONArray productsArray = data.getJSONArray("products");
                    List<Product> products = new ArrayList<>();
                    
                    Log.d(TAG, "fetchProducts: Found " + productsArray.length() + " products");
                    
                    for (int i = 0; i < productsArray.length(); i++) {
                        try {
                            JSONObject productJson = productsArray.getJSONObject(i);
                            
                            // Validate required fields
                            if (!productJson.has("_id") || !productJson.has("name") || 
                                !productJson.has("category") || !productJson.has("price") || 
                                !productJson.has("image")) {
                                Log.w(TAG, "fetchProducts: Skipping invalid product at index " + i);
                                continue;
                            }

                            Product product = parseProduct(productJson);
                            products.add(product);
                        } catch (JSONException e) {
                            Log.e(TAG, "fetchProducts: Error parsing product at index " + i, e);
                            continue;
                        }
                    }
                    
                    // Check if there are more pages
                    boolean hasMore = false;
                    if (data.has("pagination")) {
                        JSONObject pagination = data.getJSONObject("pagination");
                        if (pagination.has("hasNext")) {
                            hasMore = pagination.getBoolean("hasNext");
                        } else if (pagination.has("totalPages")) {
                            int totalPages = pagination.getInt("totalPages");
                            hasMore = currentPage < totalPages;
                        }
                    }
                    Log.d(TAG, "fetchProducts: Has more pages: " + hasMore);
                    
                    if (products.isEmpty() && currentPage > 1) {
                        hasMore = false;
                    }
                    
                    callback.onSuccess(products, hasMore);
                } catch (JSONException e) {
                    Log.e(TAG, "fetchProducts: Error parsing response", e);
                    callback.onError("Error parsing response: " + e.getMessage());
                } finally {
                    isLoading = false;
                }
            },
            error -> {
                Log.e(TAG, "fetchProducts: Network error", error);
                callback.onError("Network error: " + error.getMessage());
                isLoading = false;
            }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void getProductsByCategory(String category) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_PRODUCTS_BY_CATEGORY + category;

        JsonObjectRequest request = ApiConfig.createAuthenticatedRequest(
            context,
            Request.Method.GET,
            url,
            null,
            response -> {
                try {
                    if (response.getBoolean("success")) {
                        JSONObject data = response.getJSONObject("data");
                        JSONArray productsArray = data.getJSONArray("products");
                        List<Product> products = new ArrayList<>();

                        for (int i = 0; i < productsArray.length(); i++) {
                            try {
                                JSONObject obj = productsArray.getJSONObject(i);
                                Product product = parseProduct(obj);
                                products.add(product);
                            } catch (JSONException e) {
                                continue;
                            }
                        }

                        callback.onSuccess(products, false);
                    } else {
                        callback.onError("Không thể tải sản phẩm");
                    }
                } catch (JSONException e) {
                    callback.onError("Lỗi xử lý dữ liệu: " + e.getMessage());
                }
            },
            error -> callback.onError("Lỗi kết nối: " + error.getMessage())
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void getProductById(String productId, ProductCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_PRODUCTS + productId;
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            response -> {
                try {
                    if (response.getBoolean("success")) {
                        JSONObject data = response.getJSONObject("data");
                        JSONObject productJson = data.getJSONObject("product");
                        List<Product> products = new ArrayList<>();
                        products.add(parseProduct(productJson));
                        callback.onSuccess(products, false);
                    } else {
                        callback.onError(response.getString("message"));
                    }
                } catch (JSONException e) {
                    callback.onError("Lỗi xử lý dữ liệu");
                }
            },
            error -> callback.onError("Lỗi kết nối")
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public void deleteProduct(String productId, ProductCallback callback) {
        String url = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_PRODUCTS + productId;
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.DELETE,
            url,
            null,
            response -> {
                try {
                    if (response.getBoolean("success")) {
                        callback.onSuccess(new ArrayList<>(), false);
                    } else {
                        callback.onError(response.getString("message"));
                    }
                } catch (JSONException e) {
                    callback.onError("Lỗi xử lý dữ liệu");
                }
            },
            error -> callback.onError("Lỗi kết nối")
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

} 
