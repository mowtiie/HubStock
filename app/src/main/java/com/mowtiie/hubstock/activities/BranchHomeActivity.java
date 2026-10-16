package com.mowtiie.hubstock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.databinding.ActivityBranchHomeBinding;

public class BranchHomeActivity extends AppCompatActivity {

    private ActivityBranchHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBranchHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvGreeting.setText("Hello, " + Data.USER_NAME + " · " + Data.USER_WAREHOUSE);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        });
        binding.cardNewRequest.setOnClickListener(v -> soon());
        binding.cardMyOrders.setOnClickListener(v -> soon());
        binding.cardIncoming.setOnClickListener(v -> soon());
        binding.cardMyStock.setOnClickListener(v -> startActivity(new Intent(this, InventoryActivity.class)));
        binding.cardReports.setOnClickListener(v -> soon());
    }

    private void soon() {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    }
}
