package com.mowtiie.hubstock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.hubstock.adapters.GenericAdapter;
import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.databinding.ActivityTransferOrderListBinding;

import java.util.ArrayList;

public class TransferOrderListActivity extends AppCompatActivity {

    private ActivityTransferOrderListBinding binding;
    private GenericAdapter<Integer> adapter;
    private final ArrayList<Integer> rows = new ArrayList<>(); // order indexes
    private String filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTransferOrderListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        filter = getIntent().getStringExtra(Data.EXTRA_FILTER);
        if (Data.FILTER_PENDING.equals(filter)) binding.toolbar.setTitle("Pending approvals");
        else if (Data.FILTER_MINE.equals(filter)) binding.toolbar.setTitle("My orders");
        else if (Data.FILTER_INCOMING.equals(filter)) binding.toolbar.setTitle("Incoming deliveries");
        else binding.toolbar.setTitle("All orders");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new GenericAdapter<>(rows, (b, o, position) -> {
            int p = Data.productIndex(Data.ORDER_PRODUCT.get(o));
            b.tvTitle.setText(Data.ORDER_ID.get(o) + " · " + Data.PRODUCT_NAME.get(p));
            b.tvSubtitle.setText(Data.ORDER_SOURCE.get(o) + " → " + Data.ORDER_DEST.get(o) + "\n"
                    + Data.qty(Data.ORDER_QTY.get(o), Data.PRODUCT_UNIT.get(p)) + " requested");
            b.tvTrailing.setText(Data.ORDER_STATUS.get(o));
            b.getRoot().setOnClickListener(v -> startActivity(
                    new Intent(this, TransferDetailActivity.class).putExtra(Data.EXTRA_ORDER, o)));
        });
        binding.rvItems.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        rows.clear();
        for (int i = Data.ORDER_ID.size() - 1; i >= 0; i--) { // newest first
            boolean show;
            if (Data.FILTER_PENDING.equals(filter)) show = Data.ORDER_STATUS.get(i).equals(Data.STATUS_PENDING);
            else if (Data.FILTER_MINE.equals(filter)) show = Data.ORDER_DEST.get(i).equals(Data.USER_WAREHOUSE);
            else if (Data.FILTER_INCOMING.equals(filter)) show = Data.ORDER_DEST.get(i).equals(Data.USER_WAREHOUSE)
                    && Data.ORDER_STATUS.get(i).equals(Data.STATUS_IN_TRANSIT);
            else show = true;
            if (show) rows.add(i);
        }
        adapter.setItems(rows);
        binding.viewEmpty.getRoot().setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
