package com.mowtiie.hubstock.activities;

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
            soon();
            return true;
        });
        binding.cardApprovals.setOnClickListener(v -> soon());
        binding.cardWarehouses.setOnClickListener(v -> soon());
        binding.cardInventory.setOnClickListener(v -> soon());
        binding.cardDeliveries.setOnClickListener(v -> soon());
        binding.cardReports.setOnClickListener(v -> soon());
    }

    private void soon() {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    }
}
