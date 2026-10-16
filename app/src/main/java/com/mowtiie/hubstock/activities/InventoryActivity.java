package com.mowtiie.hubstock.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.mowtiie.hubstock.R;
import com.mowtiie.hubstock.adapters.GenericAdapter;
import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.databinding.ActivityInventoryBinding;
import com.mowtiie.hubstock.databinding.SheetProductBinding;

import java.util.ArrayList;

public class InventoryActivity extends AppCompatActivity {

    private ActivityInventoryBinding binding;
    private GenericAdapter<Integer> adapter;
    private final ArrayList<Integer> rows = new ArrayList<>(); // product indexes
    private String warehouse = Data.USER_WAREHOUSE;
    private boolean isManager; // Hub Manager or Clerk: may view every warehouse

    // Each warehouse owns its stock: only the hub's own stock is edited here. Branch stock
    // changes through accepted deliveries.
    private boolean canEdit() {
        return isManager && warehouse.equals(Data.HUB);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityInventoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isManager = !Data.USER_ROLE.equals(Data.ROLE_BRANCH);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (isManager) {
            binding.ddWarehouse.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Data.WAREHOUSE_NAME));
            binding.ddWarehouse.setText(warehouse, false);
            binding.ddWarehouse.setOnItemClickListener((parent, view, position, id) -> {
                warehouse = Data.WAREHOUSE_NAME.get(position);
                refresh();
            });
            binding.toolbar.setOnMenuItemClickListener(item -> {
                showSheet(-1);
                return true;
            });
        } else {
            binding.toolbar.setTitle("My stock");
            binding.toolbar.getMenu().findItem(R.id.action_add).setVisible(false);
            binding.tilWarehouse.setVisibility(android.view.View.GONE);
        }

        adapter = new GenericAdapter<>(rows, (b, p, position) -> {
            String sku = Data.PRODUCT_SKU.get(p);
            int qty = Data.stockQty(warehouse, sku);
            String sub = sku + " · " + Data.PRODUCT_CATEGORY.get(p);
            if (qty < Data.LOW_STOCK_LEVEL) sub += "\nLow stock: below " + Data.LOW_STOCK_LEVEL;
            b.tvTitle.setText(Data.PRODUCT_NAME.get(p));
            b.tvSubtitle.setText(sub);
            b.tvTrailing.setText(Data.qty(qty, Data.PRODUCT_UNIT.get(p)));
            b.getRoot().setClickable(canEdit());
            b.getRoot().setOnClickListener(canEdit() ? v -> showSheet(p) : null);
        });
        binding.rvItems.setAdapter(adapter);
        refresh();
    }

    private void refresh() {
        if (isManager) {
            binding.toolbar.getMenu().findItem(R.id.action_add).setVisible(canEdit());
            binding.tilWarehouse.setHelperText(canEdit() ? null : "View only. Branch stock changes through deliveries");
        }
        rows.clear();
        for (int i = 0; i < Data.PRODUCT_SKU.size(); i++) rows.add(i);
        adapter.setItems(rows);
        binding.viewEmpty.getRoot().setVisibility(rows.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    // p = product index to edit, or -1 to add
    private void showSheet(int p) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        SheetProductBinding s = SheetProductBinding.inflate(getLayoutInflater());
        dialog.setContentView(s.getRoot());
        boolean edit = p >= 0;

        s.tvTitle.setText(edit ? "Edit product" : "Add product");
        s.tilQty.setHint("Stock in " + warehouse);
        if (edit) {
            String sku = Data.PRODUCT_SKU.get(p);
            s.etSku.setText(sku);
            s.etSku.setEnabled(false);
            s.tilSku.setHelperText("SKU can't be changed");
            s.etName.setText(Data.PRODUCT_NAME.get(p));
            s.etUnit.setText(Data.PRODUCT_UNIT.get(p));
            s.etCategory.setText(Data.PRODUCT_CATEGORY.get(p));
            s.etQty.setText(String.valueOf(Data.stockQty(warehouse, sku)));
        } else {
            s.etQty.setText("0");
        }

        s.btnSave.setOnClickListener(v -> {
            s.tilSku.setError(null);
            s.tilName.setError(null);
            s.tilUnit.setError(null);
            s.tilCategory.setError(null);
            s.tilQty.setError(null);
            String sku = s.etSku.getText().toString().trim().toUpperCase();
            String name = s.etName.getText().toString().trim();
            String unit = s.etUnit.getText().toString().trim();
            String category = s.etCategory.getText().toString().trim();
            String qtyText = s.etQty.getText().toString().trim();

            if (!edit && sku.isEmpty()) {
                s.tilSku.setError("Enter a SKU");
                return;
            }
            if (!edit && Data.PRODUCT_SKU.contains(sku)) {
                s.tilSku.setError("This SKU already exists");
                return;
            }
            if (name.isEmpty()) {
                s.tilName.setError("Enter a product name");
                return;
            }
            if (unit.isEmpty()) {
                s.tilUnit.setError("Enter a unit, for example pcs");
                return;
            }
            if (category.isEmpty()) {
                s.tilCategory.setError("Enter a category");
                return;
            }
            if (qtyText.isEmpty()) {
                s.tilQty.setError("Enter 0 or more");
                return;
            }
            int qty = Integer.parseInt(qtyText);

            if (edit) {
                Data.PRODUCT_NAME.set(p, name);
                Data.PRODUCT_UNIT.set(p, unit);
                Data.PRODUCT_CATEGORY.set(p, category);
                sku = Data.PRODUCT_SKU.get(p);
            } else {
                Data.PRODUCT_SKU.add(sku);
                Data.PRODUCT_NAME.add(name);
                Data.PRODUCT_UNIT.add(unit);
                Data.PRODUCT_CATEGORY.add(category);
            }
            int idx = Data.stockIndex(warehouse, sku);
            if (idx >= 0) {
                Data.STOCK_QTY.set(idx, qty);
            } else {
                Data.STOCK_WAREHOUSE.add(warehouse);
                Data.STOCK_PRODUCT.add(sku);
                Data.STOCK_QTY.add(qty);
            }
            Data.save(this);
            dialog.dismiss();
            refresh();
            Snackbar.make(binding.getRoot(), "Saved", Snackbar.LENGTH_SHORT).show();
        });
        dialog.show();
    }
}
