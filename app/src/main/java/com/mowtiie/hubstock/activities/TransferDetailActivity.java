package com.mowtiie.hubstock.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.databinding.ActivityTransferDetailBinding;

public class TransferDetailActivity extends AppCompatActivity {

    private ActivityTransferDetailBinding binding;
    private int o;
    private String unit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTransferDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        o = getIntent().getIntExtra(Data.EXTRA_ORDER, 0);
        int p = Data.productIndex(Data.ORDER_PRODUCT.get(o));
        unit = Data.PRODUCT_UNIT.get(p);
        String status = Data.ORDER_STATUS.get(o);
        int requested = Data.ORDER_QTY.get(o);
        int approved = Data.ORDER_APPROVED_QTY.get(o);

        binding.toolbar.setTitle(Data.ORDER_ID.get(o));
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(status);
        sb.append("\nProduct: ").append(Data.PRODUCT_NAME.get(p)).append(" (").append(Data.PRODUCT_SKU.get(p)).append(")");
        sb.append("\nFrom: ").append(Data.ORDER_SOURCE.get(o));
        sb.append("\nTo: ").append(Data.ORDER_DEST.get(o));
        sb.append("\nRequested: ").append(Data.qty(requested, unit));
        if (approved > 0) sb.append("\nApproved: ").append(Data.qty(approved, unit));
        if (!Data.ORDER_DISPATCH_DATE.get(o).isEmpty()) sb.append("\nDispatch date: ").append(Data.ORDER_DISPATCH_DATE.get(o));
        if (!Data.ORDER_ARRIVAL_DATE.get(o).isEmpty()) sb.append("\nExpected arrival: ").append(Data.ORDER_ARRIVAL_DATE.get(o));
        if (status.equals(Data.STATUS_COMPLETED) || status.equals(Data.STATUS_PARTIAL)) {
            int received = Data.ORDER_RECEIVED_QTY.get(o);
            sb.append("\nReceived: ").append(Data.qty(received, unit));
            if (received < approved) sb.append("\nShortfall: ").append(Data.qty(approved - received, unit));
        }
        if (status.equals(Data.STATUS_REJECTED)) sb.append("\nReason: ").append(Data.ORDER_REASON.get(o));
        binding.tvBody.setText(sb.toString());

        if (Data.USER_ROLE.equals(Data.ROLE_HUB) && status.equals(Data.STATUS_PENDING)) {
            int available = Data.stockQty(Data.ORDER_SOURCE.get(o), Data.ORDER_PRODUCT.get(o));
            binding.llApproval.setVisibility(View.VISIBLE);
            binding.llBar.setVisibility(View.VISIBLE);
            binding.etApprovedQty.setText(String.valueOf(requested));
            binding.tilApprovedQty.setHelperText("Up to " + Data.qty(Math.min(requested, available), unit)
                    + " (" + Data.qty(available, unit) + " in stock at " + Data.ORDER_SOURCE.get(o) + ")");
            binding.btnApprove.setOnClickListener(v -> approve(requested, available));
            binding.btnReject.setOnClickListener(v -> reject());
        }
    }

    private void approve(int requested, int available) {
        binding.tilApprovedQty.setError(null);
        binding.tilReason.setError(null);
        String text = binding.etApprovedQty.getText().toString().trim();
        int qty = text.isEmpty() ? 0 : Integer.parseInt(text);
        if (qty <= 0) {
            binding.tilApprovedQty.setError("Enter a quantity greater than 0");
            return;
        }
        if (qty > requested) {
            binding.tilApprovedQty.setError("Can't approve more than the " + Data.qty(requested, unit) + " requested");
            return;
        }
        if (qty > available) {
            binding.tilApprovedQty.setError("Only " + Data.qty(available, unit) + " in stock at the source");
            return;
        }
        Data.ORDER_APPROVED_QTY.set(o, qty);
        Data.ORDER_STATUS.set(o, Data.STATUS_APPROVED);
        Data.save(this);
        Toast.makeText(this, Data.ORDER_ID.get(o) + " approved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void reject() {
        binding.tilApprovedQty.setError(null);
        binding.tilReason.setError(null);
        String reason = binding.etReason.getText().toString().trim();
        if (reason.isEmpty()) {
            binding.tilReason.setError("Enter a reason for rejecting");
            return;
        }
        Data.ORDER_REASON.set(o, reason);
        Data.ORDER_STATUS.set(o, Data.STATUS_REJECTED);
        Data.save(this);
        Toast.makeText(this, Data.ORDER_ID.get(o) + " rejected", Toast.LENGTH_SHORT).show();
        finish();
    }
}
