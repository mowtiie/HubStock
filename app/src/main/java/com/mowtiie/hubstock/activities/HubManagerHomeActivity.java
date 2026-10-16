package com.mowtiie.hubstock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.databinding.ActivityHubManagerHomeBinding;

public class HubManagerHomeActivity extends AppCompatActivity {

    private ActivityHubManagerHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHubManagerHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvGreeting.setText("Hello, " + Data.USER_NAME + " · " + Data.USER_WAREHOUSE);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        });
        binding.cardApprovals.setOnClickListener(v -> startActivity(new Intent(this, TransferOrderListActivity.class).putExtra(Data.EXTRA_FILTER, Data.FILTER_PENDING)));
        binding.cardWarehouses.setOnClickListener(v -> startActivity(new Intent(this, WarehouseListActivity.class)));
        binding.cardInventory.setOnClickListener(v -> startActivity(new Intent(this, InventoryActivity.class)));
        binding.cardDeliveries.setOnClickListener(v -> soon());
        binding.cardReports.setOnClickListener(v -> soon());
    }

    private void soon() {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    }
}
