package com.mowtiie.hubstock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
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
        binding.toolbar.setTitle(Data.ORDER_ID.get(o));
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        int p = Data.productIndex(Data.ORDER_PRODUCT.get(o));
        unit = Data.PRODUCT_UNIT.get(p);
        String status = Data.ORDER_STATUS.get(o);
        int requested = Data.ORDER_QTY.get(o);
        int approved = Data.ORDER_APPROVED_QTY.get(o);

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

        binding.llApproval.setVisibility(View.GONE);
        binding.llBar.setVisibility(View.GONE);
        binding.llActionBar.setVisibility(View.GONE);

        boolean hubOrClerk = Data.USER_ROLE.equals(Data.ROLE_HUB) || Data.USER_ROLE.equals(Data.ROLE_CLERK);

        if (Data.USER_ROLE.equals(Data.ROLE_HUB) && status.equals(Data.STATUS_PENDING)) {
            int available = Data.stockQty(Data.ORDER_SOURCE.get(o), Data.ORDER_PRODUCT.get(o));
            binding.llApproval.setVisibility(View.VISIBLE);
            binding.llBar.setVisibility(View.VISIBLE);
            binding.etApprovedQty.setText(String.valueOf(requested));
            binding.tilApprovedQty.setHelperText("Up to " + Data.qty(Math.min(requested, available), unit)
                    + " (" + Data.qty(available, unit) + " in stock at " + Data.ORDER_SOURCE.get(o) + ")");
            binding.btnApprove.setOnClickListener(v -> approve(requested, available));
            binding.btnReject.setOnClickListener(v -> reject());
        } else if (hubOrClerk && status.equals(Data.STATUS_APPROVED)) {
            showAction("Schedule delivery", v -> startActivity(
                    new Intent(this, ScheduleDeliveryActivity.class).putExtra(Data.EXTRA_ORDER, o)));
        } else if (hubOrClerk && status.equals(Data.STATUS_SCHEDULED)) {
            showAction("Dispatch", v -> dispatch(approved));
        } else if (Data.USER_ROLE.equals(Data.ROLE_BRANCH) && status.equals(Data.STATUS_IN_TRANSIT)
                && Data.ORDER_DEST.get(o).equals(Data.USER_WAREHOUSE)) {
            showAction("Accept delivery", v -> startActivity(
                    new Intent(this, ReceiveDeliveryActivity.class).putExtra(Data.EXTRA_ORDER, o)));
        }
    }

    private void showAction(String label, View.OnClickListener listener) {
        binding.llActionBar.setVisibility(View.VISIBLE);
        binding.btnAction.setText(label);
        binding.btnAction.setOnClickListener(listener);
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

    // Stock leaves the source here, not at approval.
    private void dispatch(int approved) {
        String source = Data.ORDER_SOURCE.get(o);
        int idx = Data.stockIndex(source, Data.ORDER_PRODUCT.get(o));
        int available = idx < 0 ? 0 : Data.STOCK_QTY.get(idx);
        if (approved > available) {
            Snackbar.make(binding.getRoot(), "Not enough stock at " + source + ": only "
                    + Data.qty(available, unit) + " left", Snackbar.LENGTH_LONG).show();
            return;
        }
        Data.STOCK_QTY.set(idx, available - approved);
        Data.ORDER_STATUS.set(o, Data.STATUS_IN_TRANSIT);
        Data.save(this);
        render();
        Snackbar.make(binding.getRoot(), "Dispatched " + Data.qty(approved, unit) + " from " + source,
                Snackbar.LENGTH_SHORT).show();
    }
}
