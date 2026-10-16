package com.mowtiie.hubstock.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tvName.setText(Data.USER_NAME);
        binding.tvEmail.setText(Data.USER_EMAIL);
        binding.tvRole.setText(Data.USER_ROLE);
        binding.tvWarehouse.setText(Data.USER_WAREHOUSE);

        binding.btnLogout.setOnClickListener(v -> {
            Data.USER_ID = 0;
            Data.USER_EMAIL = "";
            Data.USER_NAME = "";
            Data.USER_ROLE = "";
            Data.USER_WAREHOUSE = "";
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });
    }
}
