package com.example.myapplication.models;

import java.util.List;

public class Order {
    private String id;
    private String userId;
    private String name;
    private String phone;
    private List<OrderProduct> products;
    private String price;
    private String address;
    private String status;
    private boolean deleted;
    private String createdAt;
    private String updatedAt;

    public static class OrderProduct {
        private String id;
        private String name;
        private String image;
        private String price;
        private int size;
        private int quantity;

        public OrderProduct(String id, String name, String image, String price, int size, int quantity) {
            this.id = id;
            this.name = name;
            this.image = image;
            this.price = price;
            this.size = size;
            this.quantity = quantity;
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getImage() { return image; }
        public String getPrice() { return price; }
        public int getSize() { return size; }
        public int getQuantity() { return quantity; }
    }

    public Order(String id, String userId, String name, String phone, List<OrderProduct> products,
                String price, String address, String status, boolean deleted,
                String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.products = products;
        this.price = price;
        this.address = address;
        this.status = status;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public List<OrderProduct> getProducts() { return products; }
    public String getPrice() { return price; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
    public boolean isDeleted() { return deleted; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
} 