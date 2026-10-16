package com.mowtiie.hubstock.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.databinding.ActivityScheduleDeliveryBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ScheduleDeliveryActivity extends AppCompatActivity {

    private ActivityScheduleDeliveryBinding binding;
    private int o;
    private long dispatchMillis = -1;
    private long arrivalMillis = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityScheduleDeliveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        o = getIntent().getIntExtra(Data.EXTRA_ORDER, 0);
        int p = Data.productIndex(Data.ORDER_PRODUCT.get(o));
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tvSummary.setText(Data.ORDER_ID.get(o) + " · " + Data.PRODUCT_NAME.get(p)
                + "\n" + Data.ORDER_SOURCE.get(o) + " → " + Data.ORDER_DEST.get(o)
                + "\nApproved: " + Data.qty(Data.ORDER_APPROVED_QTY.get(o), Data.PRODUCT_UNIT.get(p)));

        binding.etDispatch.setOnClickListener(v -> pickDate("Dispatch date", dispatchMillis, millis -> {
            dispatchMillis = millis;
            binding.etDispatch.setText(format(millis));
        }));
        binding.etArrival.setOnClickListener(v -> pickDate("Expected arrival date", arrivalMillis, millis -> {
            arrivalMillis = millis;
            binding.etArrival.setText(format(millis));
        }));
        binding.btnSchedule.setOnClickListener(v -> schedule());
    }

    private interface OnPicked {
        void picked(long millis);
    }

    private void pickDate(String title, long current, OnPicked callback) {
        if (getSupportFragmentManager().findFragmentByTag(title) != null) return; // already open
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker().setTitleText(title);
        if (current >= 0) builder.setSelection(current);
        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(callback::picked);
        picker.show(getSupportFragmentManager(), title);
    }

    // The picker returns UTC midnight, so format in UTC to keep the chosen day.
    private String format(long millis) {
        SimpleDateFormat f = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new Date(millis));
    }

    private void schedule() {
        binding.tilDispatch.setError(null);
        binding.tilArrival.setError(null);
        if (dispatchMillis < 0) {
            binding.tilDispatch.setError("Pick a dispatch date");
            return;
        }
        if (arrivalMillis < 0) {
            binding.tilArrival.setError("Pick an expected arrival date");
            return;
        }
        if (arrivalMillis < dispatchMillis) {
            binding.tilArrival.setError("Arrival can't be before the dispatch date");
            return;
        }
        Data.ORDER_DISPATCH_DATE.set(o, format(dispatchMillis));
        Data.ORDER_ARRIVAL_DATE.set(o, format(arrivalMillis));
        Data.ORDER_STATUS.set(o, Data.STATUS_SCHEDULED);
        Data.save(this);
        Toast.makeText(this, Data.ORDER_ID.get(o) + " scheduled", Toast.LENGTH_SHORT).show();
        finish();
    }
}
