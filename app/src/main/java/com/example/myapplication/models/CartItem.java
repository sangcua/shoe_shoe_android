package com.example.myapplication.models;

public class CartItem extends BaseModel {
    private String _id;  // Changed to store original string ID from API
    private String productId;
    private String productName;
    private double price;
    private String image;
    private String category;
    private int size;
    private int quantity;

    public CartItem(String id, String productId, String productName, double price, 
                   String image, String category, int size, int quantity) {
        this._id = id;  // Store original string ID
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.image = image;
        this.category = category;
        this.size = size;
        this.quantity = quantity;
    }

    // Override getId to return integer ID for BaseModel compatibility
    @Override
    public int getId() {
        try {
            // Try to parse last part of MongoDB ID as int, or return 0 if not possible
            String lastPart = _id.substring(Math.max(0, _id.length() - 6));
            return Integer.parseInt(lastPart, 16);
        } catch (Exception e) {
            return 0;
        }
    }

    // Add getter for original string ID
    public String getStringId() {
        return _id;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public String getImage() { return image; }
    public String getCategory() { return category; }
    public int getSize() { return size; }
    public int getQuantity() { return quantity; }

    // Setters
    public void setProductId(String productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setPrice(double price) { this.price = price; }
    public void setImage(String image) { this.image = image; }
    public void setCategory(String category) { this.category = category; }
    public void setSize(int size) { this.size = size; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
} 