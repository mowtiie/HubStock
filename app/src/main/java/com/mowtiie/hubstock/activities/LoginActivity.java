package com.mowtiie.hubstock.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.hubstock.data.Data;
import com.mowtiie.hubstock.data.DbHelper;
import com.mowtiie.hubstock.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        if (email.isEmpty()) {
            binding.tilEmail.setError("Enter your email");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email, for example hub@demo.com");
            return;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            return;
        }

        SQLiteDatabase db = new DbHelper(this).getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, password, name, role, warehouse FROM users WHERE email = ? COLLATE NOCASE",
                new String[]{email});
        if (!c.moveToFirst()) {
            c.close();
            binding.tilEmail.setError("No account with this email");
            return;
        }
        if (!c.getString(1).equals(password)) {
            c.close();
            binding.tilPassword.setError("Wrong password");
            return;
        }
        Data.USER_ID = c.getInt(0);
        Data.USER_EMAIL = email.toLowerCase();
        Data.USER_NAME = c.getString(2);
        Data.USER_ROLE = c.getString(3);
        Data.USER_WAREHOUSE = c.getString(4);
        c.close();

        Data.seedDemo(this);

        Class<?> home = BranchHomeActivity.class;
        if (Data.USER_ROLE.equals(Data.ROLE_HUB)) home = HubManagerHomeActivity.class;
        else if (Data.USER_ROLE.equals(Data.ROLE_CLERK)) home = ClerkHomeActivity.class;
        startActivity(new Intent(this, home).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
