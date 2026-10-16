package com.mowtiie.hubstock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.databinding.ActivityTransferRequestBinding;

import java.util.ArrayList;

public class TransferRequestActivity extends AppCompatActivity {

    private ActivityTransferRequestBinding binding;
    private String source;
    private int product = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTransferRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        ArrayList<String> sources = new ArrayList<>(Data.WAREHOUSE_NAME);
        sources.remove(Data.USER_WAREHOUSE);
        binding.ddSource.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sources));
        if (sources.contains(Data.HUB)) {
            source = Data.HUB;
            binding.ddSource.setText(source, false);
        }
        binding.ddSource.setOnItemClickListener((parent, view, position, id) -> {
            source = sources.get(position);
            updateHelper();
        });

        binding.ddProduct.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Data.PRODUCT_NAME));
        binding.ddProduct.setOnItemClickListener((parent, view, position, id) -> {
            product = position;
            updateHelper();
        });

        binding.btnSubmit.setOnClickListener(v -> submit());
    }

    private void updateHelper() {
        if (source != null && product >= 0) {
            String sku = Data.PRODUCT_SKU.get(product);
            binding.tilQty.setHelperText("Available at " + source + ": "
                    + Data.qty(Data.stockQty(source, sku), Data.PRODUCT_UNIT.get(product)));
        }
    }

    private void submit() {
        binding.tilSource.setError(null);
        binding.tilProduct.setError(null);
        binding.tilQty.setError(null);

        if (source == null) {
            binding.tilSource.setError("Pick a source warehouse");
            return;
        }
        if (product < 0) {
            binding.tilProduct.setError("Pick a product");
            return;
        }
        String qtyText = binding.etQty.getText().toString().trim();
        int qty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);
        if (qty <= 0) {
            binding.tilQty.setError("Enter a quantity greater than 0");
            return;
        }
        String sku = Data.PRODUCT_SKU.get(product);
        int available = Data.stockQty(source, sku);
        if (qty > available) {
            binding.tilQty.setError("Only " + Data.qty(available, Data.PRODUCT_UNIT.get(product)) + " available at " + source);
            return;
        }

        Data.addOrder(Data.nextOrderId(), source, Data.USER_WAREHOUSE, sku, qty, 0, 0, Data.STATUS_PENDING, "", "", "");
        Data.save(this);
        Toast.makeText(this, "Request sent to " + source, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, BranchHomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
