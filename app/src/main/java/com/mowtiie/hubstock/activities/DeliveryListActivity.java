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

// Approved orders are listed too, so there is a way to reach them and schedule them.
public class DeliveryListActivity extends AppCompatActivity {

    private ActivityTransferOrderListBinding binding;
    private GenericAdapter<Integer> adapter;
    private final ArrayList<Integer> rows = new ArrayList<>(); // order indexes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTransferOrderListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle("Deliveries");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new GenericAdapter<>(rows, (b, o, position) -> {
            int p = Data.productIndex(Data.ORDER_PRODUCT.get(o));
            String status = Data.ORDER_STATUS.get(o);
            String sub = Data.ORDER_SOURCE.get(o) + " → " + Data.ORDER_DEST.get(o) + "\n"
                    + Data.qty(Data.ORDER_APPROVED_QTY.get(o), Data.PRODUCT_UNIT.get(p));
            if (status.equals(Data.STATUS_APPROVED)) sub += "\nReady to schedule";
            else sub += "\nDispatch " + Data.ORDER_DISPATCH_DATE.get(o) + " · Arrival " + Data.ORDER_ARRIVAL_DATE.get(o);
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
        rows.clear();
        for (int i = Data.ORDER_ID.size() - 1; i >= 0; i--) {
            String s = Data.ORDER_STATUS.get(i);
            if (s.equals(Data.STATUS_APPROVED) || s.equals(Data.STATUS_SCHEDULED) || s.equals(Data.STATUS_IN_TRANSIT)) {
                rows.add(i);
            }
        }
        adapter.setItems(rows);
        binding.viewEmpty.getRoot().setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
