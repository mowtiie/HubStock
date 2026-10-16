package com.mowtiie.hubstock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.databinding.ActivityReceiveDeliveryBinding;

public class ReceiveDeliveryActivity extends AppCompatActivity {

    private ActivityReceiveDeliveryBinding binding;
    private int o;
    private String unit;
    private int approved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityReceiveDeliveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        o = getIntent().getIntExtra(Data.EXTRA_ORDER, 0);
        int p = Data.productIndex(Data.ORDER_PRODUCT.get(o));
        unit = Data.PRODUCT_UNIT.get(p);
        approved = Data.ORDER_APPROVED_QTY.get(o);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tvSummary.setText(Data.ORDER_ID.get(o) + " · " + Data.PRODUCT_NAME.get(p)
                + "\nFrom: " + Data.ORDER_SOURCE.get(o)
                + "\nExpected arrival: " + Data.ORDER_ARRIVAL_DATE.get(o)
                + "\nApproved: " + Data.qty(approved, unit));
        binding.etReceived.setText(String.valueOf(approved));
        binding.tilReceived.setHelperText("0 to " + Data.qty(approved, unit) + ". Less than approved is a partial delivery");
        binding.btnAccept.setOnClickListener(v -> accept());
    }

    private void accept() {
        binding.tilReceived.setError(null);
        String text = binding.etReceived.getText().toString().trim();
        if (text.isEmpty()) {
            binding.tilReceived.setError("Enter the quantity received");
            return;
        }
        int received = Integer.parseInt(text);
        if (received > approved) {
            binding.tilReceived.setError("Can't receive more than the " + Data.qty(approved, unit) + " approved");
            return;
        }

        String dest = Data.ORDER_DEST.get(o);
        String sku = Data.ORDER_PRODUCT.get(o);
        int idx = Data.stockIndex(dest, sku);
        if (idx >= 0) {
            Data.STOCK_QTY.set(idx, Data.STOCK_QTY.get(idx) + received);
        } else {
            Data.STOCK_WAREHOUSE.add(dest);
            Data.STOCK_PRODUCT.add(sku);
            Data.STOCK_QTY.add(received);
        }
        Data.ORDER_RECEIVED_QTY.set(o, received);
        boolean full = received == approved;
        Data.ORDER_STATUS.set(o, full ? Data.STATUS_COMPLETED : Data.STATUS_PARTIAL);
        Data.save(this);

        Toast.makeText(this, full ? "Delivery completed" : "Partial delivery: "
                + Data.qty(approved - received, unit) + " short", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, BranchHomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
