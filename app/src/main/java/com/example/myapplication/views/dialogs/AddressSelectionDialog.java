package com.example.myapplication.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Address;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.List;

public class AddressSelectionDialog extends BottomSheetDialogFragment {
    private List<Address> addresses;
    private AddressSelectionListener listener;
    private Address selectedAddress;

    public interface AddressSelectionListener {
        void onAddressSelected(Address address);
    }

    public AddressSelectionDialog(List<Address> addresses, AddressSelectionListener listener) {
        this.addresses = addresses;
        this.listener = listener;
        // Tìm địa chỉ mặc định
        for (Address address : addresses) {
            if (address.isDefault()) {
                selectedAddress = address;
                break;
            }
        }
        // Nếu không có địa chỉ mặc định, chọn địa chỉ đầu tiên
        if (selectedAddress == null && !addresses.isEmpty()) {
            selectedAddress = addresses.get(0);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_address_selection, null);
        dialog.setContentView(view);

        TextView titleTextView = view.findViewById(R.id.titleTextView);
        titleTextView.setText("Chọn địa chỉ giao hàng");

        RecyclerView recyclerView = view.findViewById(R.id.addressRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        AddressAdapter adapter = new AddressAdapter(addresses, address -> {
            selectedAddress = address;
            listener.onAddressSelected(address);
            dismiss();
        });
        recyclerView.setAdapter(adapter);

        return dialog;
    }

    private interface OnAddressClickListener {
        void onAddressClick(Address address);
    }

    private class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
        private List<Address> addresses;
        private OnAddressClickListener listener;

        AddressAdapter(List<Address> addresses, OnAddressClickListener listener) {
            this.addresses = addresses;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_address_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Address address = addresses.get(position);
            holder.nameTextView.setText(address.getName());
            holder.addressTextView.setText(address.getAddress());
            holder.phoneTextView.setText(address.getPhone());
            holder.radioButton.setChecked(address.equals(selectedAddress));

            holder.itemView.setOnClickListener(v -> {
                selectedAddress = address;
                notifyDataSetChanged();
                listener.onAddressClick(address);
            });
        }

        @Override
        public int getItemCount() {
            return addresses.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            RadioButton radioButton;
            TextView nameTextView;
            TextView addressTextView;
            TextView phoneTextView;

            ViewHolder(View itemView) {
                super(itemView);
                radioButton = itemView.findViewById(R.id.radioButton);
                nameTextView = itemView.findViewById(R.id.nameTextView);
                addressTextView = itemView.findViewById(R.id.addressTextView);
                phoneTextView = itemView.findViewById(R.id.phoneTextView);
            }
        }
    }
} 