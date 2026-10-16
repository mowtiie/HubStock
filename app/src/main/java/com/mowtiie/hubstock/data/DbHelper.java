package com.mowtiie.hubstock.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context) {
        super(context, "hubstock.db", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE, "
                + "password TEXT, name TEXT, role TEXT, warehouse TEXT)");
        insertUser(db, "hub@demo.com", "Hana Santos", Data.ROLE_HUB, "Central Hub");
        insertUser(db, "clerk@demo.com", "Carlo Mendoza", Data.ROLE_CLERK, "Central Hub");
        insertUser(db, "branch.a@demo.com", "Ana Reyes", Data.ROLE_BRANCH, "Branch A");
        insertUser(db, "branch.b@demo.com", "Ben Cruz", Data.ROLE_BRANCH, "Branch B");
        createDataTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) createDataTables(db);
    }

    private void createDataTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE warehouses (name TEXT, location TEXT, manager TEXT)");
        db.execSQL("CREATE TABLE products (sku TEXT, name TEXT, unit TEXT, category TEXT)");
        db.execSQL("CREATE TABLE stock (warehouse TEXT, product TEXT, qty INTEGER)");
        db.execSQL("CREATE TABLE orders (id TEXT, source TEXT, dest TEXT, product TEXT, qty INTEGER, "
                + "approved_qty INTEGER, received_qty INTEGER, status TEXT, reason TEXT, "
                + "dispatch_date TEXT, arrival_date TEXT)");
    }

    private void insertUser(SQLiteDatabase db, String email, String name, String role, String warehouse) {
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", "demo1234");
        values.put("name", name);
        values.put("role", role);
        values.put("warehouse", warehouse);
        db.insert("users", null, values);
    }
}
