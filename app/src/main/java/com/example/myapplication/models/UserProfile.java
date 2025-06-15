package com.example.myapplication.models;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private List<Address> addresses;

    public UserProfile(String id, String username, String email, String phone, String avatar) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.addresses = new ArrayList<>();
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAvatar() { return avatar; }
    public List<Address> getAddresses() { return addresses; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }

    public void addAddress(Address address) {
        if (this.addresses == null) {
            this.addresses = new ArrayList<>();
        }
        this.addresses.add(address);
    }
} 