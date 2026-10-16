package com.mowtiie.hubstock.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.mowtiie.hubstock.adapters.GenericAdapter;
import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.data.DbHelper;
import com.mowtiie.hubstock.databinding.ActivityWarehouseListBinding;
import com.mowtiie.hubstock.databinding.SheetWarehouseBinding;

import java.util.ArrayList;

public class WarehouseListActivity extends AppCompatActivity {

    private ActivityWarehouseListBinding binding;
    private GenericAdapter<Integer> adapter;
    private final ArrayList<Integer> rows = new ArrayList<>(); // warehouse indexes
    private final ArrayList<String> managers = new ArrayList<>(); // branch manager account names

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWarehouseListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            showSheet(-1);
            return true;
        });

        Cursor c = new DbHelper(this).getReadableDatabase()
                .rawQuery("SELECT name FROM users WHERE role = ? ORDER BY name", new String[]{Data.ROLE_BRANCH});
        while (c.moveToNext()) managers.add(c.getString(0));
        c.close();

        adapter = new GenericAdapter<>(rows, (b, w, position) -> {
            b.tvTitle.setText(Data.WAREHOUSE_NAME.get(w));
            b.tvSubtitle.setText(Data.WAREHOUSE_LOCATION.get(w) + "\nManager: " + Data.WAREHOUSE_MANAGER.get(w));
            b.tvTrailing.setText("");
            b.getRoot().setOnClickListener(v -> showSheet(w));
        });
        binding.rvItems.setAdapter(adapter);
        refresh();
    }

    private void refresh() {
        rows.clear();
        for (int i = 0; i < Data.WAREHOUSE_NAME.size(); i++) rows.add(i);
        adapter.setItems(rows);
        binding.viewEmpty.getRoot().setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // w = warehouse index to edit, or -1 to add
    private void showSheet(int w) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        SheetWarehouseBinding s = SheetWarehouseBinding.inflate(getLayoutInflater());
        dialog.setContentView(s.getRoot());
        boolean edit = w >= 0;
        boolean isHub = edit && Data.WAREHOUSE_NAME.get(w).equals(Data.HUB);

        s.tvTitle.setText(edit ? "Edit warehouse" : "Add warehouse");
        s.ddManager.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, managers));
        if (edit) {
            s.etName.setText(Data.WAREHOUSE_NAME.get(w));
            s.etName.setEnabled(false);
            s.tilName.setHelperText("Name can't be changed");
            s.etLocation.setText(Data.WAREHOUSE_LOCATION.get(w));
            s.ddManager.setText(Data.WAREHOUSE_MANAGER.get(w), false);
        }
        if (isHub) {
            s.tilManager.setEnabled(false);
            s.tilManager.setHelperText("The hub is run by the Hub Manager");
        }

        s.btnSave.setOnClickListener(v -> {
            s.tilName.setError(null);
            s.tilLocation.setError(null);
            s.tilManager.setError(null);
            String name = s.etName.getText().toString().trim();
            String location = s.etLocation.getText().toString().trim();
            String manager = s.ddManager.getText().toString().trim();

            if (!edit && name.isEmpty()) {
                s.tilName.setError("Enter a warehouse name");
                return;
            }
            if (!edit && Data.WAREHOUSE_NAME.contains(name)) {
                s.tilName.setError("A warehouse with this name already exists");
                return;
            }
            if (location.isEmpty()) {
                s.tilLocation.setError("Enter a location");
                return;
            }
            if (!isHub && !managers.contains(manager)) {
                s.tilManager.setError("Pick a branch manager");
                return;
            }

            if (edit) {
                Data.WAREHOUSE_LOCATION.set(w, location);
                if (!isHub) Data.WAREHOUSE_MANAGER.set(w, manager);
            } else {
                Data.WAREHOUSE_NAME.add(name);
                Data.WAREHOUSE_LOCATION.add(location);
                Data.WAREHOUSE_MANAGER.add(manager);
            }
            Data.save(this);
            dialog.dismiss();
            refresh();
            Snackbar.make(binding.getRoot(), "Saved", Snackbar.LENGTH_SHORT).show();
        });
        dialog.show();
    }
}
