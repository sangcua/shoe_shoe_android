package com.example.myapplication.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.TextView;
import com.example.myapplication.R;

public class LoadingManager {
    private Dialog dialog;
    private TextView tvMessage;

    public LoadingManager(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        tvMessage = dialog.findViewById(R.id.tvLoadingMessage);
    }

    public void show() {
        show("Đang tải...");
    }

    public void show(String message) {
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
} 