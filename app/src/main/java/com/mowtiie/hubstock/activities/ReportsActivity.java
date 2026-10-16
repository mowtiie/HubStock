package com.mowtiie.hubstock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.mowtiie.hubstock.adapters.GenericAdapter;
import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.databinding.ActivityReportsBinding;

import java.util.ArrayList;

public class ReportsActivity extends AppCompatActivity {

    private ActivityReportsBinding binding;
    private GenericAdapter<Integer> adapter;
    private final ArrayList<Integer> rows = new ArrayList<>(); // order indexes
    private int tab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tabs.addTab(binding.tabs.newTab().setText("Pending"));
        binding.tabs.addTab(binding.tabs.newTab().setText("Completed"));
        binding.tabs.addTab(binding.tabs.newTab().setText("Partial"));
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab t) {
                tab = t.getPosition();
                refresh();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab t) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab t) {
            }
        });

        adapter = new GenericAdapter<>(rows, (b, o, position) -> {
            int p = Data.productIndex(Data.ORDER_PRODUCT.get(o));
            String unit = Data.PRODUCT_UNIT.get(p);
            String status = Data.ORDER_STATUS.get(o);
            String sub = Data.ORDER_SOURCE.get(o) + " → " + Data.ORDER_DEST.get(o) + "\n";
            if (status.equals(Data.STATUS_COMPLETED) || status.equals(Data.STATUS_PARTIAL)) {
                int approved = Data.ORDER_APPROVED_QTY.get(o);
                int received = Data.ORDER_RECEIVED_QTY.get(o);
                sub += Data.qty(approved, unit) + " approved · " + Data.qty(received, unit) + " received";
                if (received < approved) sub += "\nShortfall: " + Data.qty(approved - received, unit);
            } else {
                sub += Data.qty(Data.ORDER_QTY.get(o), unit) + " requested";
            }
            b.tvTitle.setText(Data.ORDER_ID.get(o) + " · " + Data.PRODUCT_NAME.get(p));
            b.tvSubtitle.setText(sub);
            b.tvTrailing.setText(status);
            b.getRoot().setOnClickListener(v -> startActivity(
                    new Intent(this, TransferDetailActivity.class).putExtra(Data.EXTRA_ORDER, o)));
        });
        binding.rvItems.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    // Pending covers every order that is not finished or rejected yet.
    private boolean matches(String status) {
        if (tab == 1) return status.equals(Data.STATUS_COMPLETED);
        if (tab == 2) return status.equals(Data.STATUS_PARTIAL);
        return status.equals(Data.STATUS_PENDING) || status.equals(Data.STATUS_APPROVED)
                || status.equals(Data.STATUS_SCHEDULED) || status.equals(Data.STATUS_IN_TRANSIT);
    }

    private void refresh() {
        boolean branch = Data.USER_ROLE.equals(Data.ROLE_BRANCH);
        rows.clear();
        for (int i = Data.ORDER_ID.size() - 1; i >= 0; i--) {
            boolean mine = Data.ORDER_SOURCE.get(i).equals(Data.USER_WAREHOUSE)
                    || Data.ORDER_DEST.get(i).equals(Data.USER_WAREHOUSE);
            if ((!branch || mine) && matches(Data.ORDER_STATUS.get(i))) rows.add(i);
        }
        String[] names = {"Pending", "Completed", "Partial"};
        binding.tvCount.setText(rows.size() + " " + names[tab].toLowerCase() + (rows.size() == 1 ? " order" : " orders"));
        adapter.setItems(rows);
        binding.viewEmpty.getRoot().setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
