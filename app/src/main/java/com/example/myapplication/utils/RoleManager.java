package com.example.myapplication.utils;

import android.content.Context;
import android.content.Intent;
import com.example.myapplication.views.activities.AdminActivity;
import com.example.myapplication.views.activities.MainActivity;

public class RoleManager {
    public static void checkRoleAndNavigate(Context context) {
        TokenManager tokenManager = TokenManager.getInstance(context);
        String role = tokenManager.getRole();
        
        Intent intent;
        if ("admin".equalsIgnoreCase(role)) {
            intent = new Intent(context, AdminActivity.class);
        } else {
            intent = new Intent(context, MainActivity.class);
        }
        
        // Clear all previous activities
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
} 