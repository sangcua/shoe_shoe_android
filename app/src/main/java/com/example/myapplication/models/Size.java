package com.example.myapplication.models;

public class Size {
    private int size;
    private int quantity;

    public Size(int size, int quantity) {
        this.size = size;
        this.quantity = quantity;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Size{" +
                "size=" + size +
                ", quantity=" + quantity +
                '}';
    }
} 