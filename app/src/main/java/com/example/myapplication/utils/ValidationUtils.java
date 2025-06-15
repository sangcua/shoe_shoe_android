package com.example.myapplication.utils;

import android.util.Patterns;

public class ValidationUtils {
    
    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        // Kiểm tra số điện thoại Việt Nam:
        // - Bắt đầu bằng 0
        // - Tiếp theo là 3,5,7,8,9
        // - Tổng cộng 10 số
        String phoneRegex = "^0[35789][0-9]{8}$";
        return phone != null && phone.matches(phoneRegex);
    }

    public static boolean isValidUsername(String username) {
        // Username phải:
        // - Có độ dài từ 3-30 ký tự
        // - Chỉ chứa chữ cái, số, dấu gạch dưới và gạch ngang
        // - Không bắt đầu hoặc kết thúc bằng gạch dưới hoặc gạch ngang
        String usernameRegex = "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){1,28}[a-zA-Z0-9]$";
        return username != null && username.matches(usernameRegex);
    }
} 