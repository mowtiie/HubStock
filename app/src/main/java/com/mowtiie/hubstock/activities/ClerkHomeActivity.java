package com.mowtiie.hubstock.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.databinding.ActivityClerkHomeBinding;

public class ClerkHomeActivity extends AppCompatActivity {

    private ActivityClerkHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityClerkHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvGreeting.setText("Hello, " + Data.USER_NAME + " · " + Data.USER_WAREHOUSE);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        });
        binding.cardInventory.setOnClickListener(v -> startActivity(new Intent(this, InventoryActivity.class)));
        binding.cardDeliveries.setOnClickListener(v -> startActivity(new Intent(this, DeliveryListActivity.class)));
        binding.cardReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
    }
}
