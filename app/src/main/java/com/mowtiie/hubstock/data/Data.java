package com.mowtiie.hubstock.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;

public class Data {

    public static final String ROLE_HUB = "Hub Manager";
    public static final String ROLE_CLERK = "Inventory Clerk";
    public static final String ROLE_BRANCH = "Branch Manager";

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_APPROVED = "Approved";
    public static final String STATUS_REJECTED = "Rejected";
    public static final String STATUS_SCHEDULED = "Scheduled";
    public static final String STATUS_IN_TRANSIT = "In transit";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_PARTIAL = "Partial";

    public static final String HUB = "Central Hub";
    public static final int LOW_STOCK_LEVEL = 10;

    public static int USER_ID;
    public static String USER_EMAIL = "";
    public static String USER_NAME = "";
    public static String USER_ROLE = "";
    public static String USER_WAREHOUSE = "";

    // Warehouses (parallel)
    public static final ArrayList<String> WAREHOUSE_NAME = new ArrayList<>();
    public static final ArrayList<String> WAREHOUSE_LOCATION = new ArrayList<>();
    public static final ArrayList<String> WAREHOUSE_MANAGER = new ArrayList<>();

    // Products (parallel)
    public static final ArrayList<String> PRODUCT_SKU = new ArrayList<>();
    public static final ArrayList<String> PRODUCT_NAME = new ArrayList<>();
    public static final ArrayList<String> PRODUCT_UNIT = new ArrayList<>();
    public static final ArrayList<String> PRODUCT_CATEGORY = new ArrayList<>();

    // Stock per warehouse (parallel): warehouse name, product SKU, quantity
    public static final ArrayList<String> STOCK_WAREHOUSE = new ArrayList<>();
    public static final ArrayList<String> STOCK_PRODUCT = new ArrayList<>();
    public static final ArrayList<Integer> STOCK_QTY = new ArrayList<>();

    // Transfer orders (parallel). Source and destination are warehouse names, product is a SKU.
    public static final ArrayList<String> ORDER_ID = new ArrayList<>();
    public static final ArrayList<String> ORDER_SOURCE = new ArrayList<>();
    public static final ArrayList<String> ORDER_DEST = new ArrayList<>();
    public static final ArrayList<String> ORDER_PRODUCT = new ArrayList<>();
    public static final ArrayList<Integer> ORDER_QTY = new ArrayList<>();
    public static final ArrayList<Integer> ORDER_APPROVED_QTY = new ArrayList<>();
    public static final ArrayList<Integer> ORDER_RECEIVED_QTY = new ArrayList<>();
    public static final ArrayList<String> ORDER_STATUS = new ArrayList<>();
    public static final ArrayList<String> ORDER_REASON = new ArrayList<>();
    public static final ArrayList<String> ORDER_DISPATCH_DATE = new ArrayList<>();
    public static final ArrayList<String> ORDER_ARRIVAL_DATE = new ArrayList<>();

    private static boolean LOADED = false;

    public static String qty(int amount, String unit) {
        return amount + " " + unit;
    }

    // Reads the four data tables into the lists above, replacing their contents.
    public static void load(Context context) {
        SQLiteDatabase db = new DbHelper(context).getReadableDatabase();
        WAREHOUSE_NAME.clear();
        WAREHOUSE_LOCATION.clear();
        WAREHOUSE_MANAGER.clear();
        PRODUCT_SKU.clear();
        PRODUCT_NAME.clear();
        PRODUCT_UNIT.clear();
        PRODUCT_CATEGORY.clear();
        STOCK_WAREHOUSE.clear();
        STOCK_PRODUCT.clear();
        STOCK_QTY.clear();
        ORDER_ID.clear();
        ORDER_SOURCE.clear();
        ORDER_DEST.clear();
        ORDER_PRODUCT.clear();
        ORDER_QTY.clear();
        ORDER_APPROVED_QTY.clear();
        ORDER_RECEIVED_QTY.clear();
        ORDER_STATUS.clear();
        ORDER_REASON.clear();
        ORDER_DISPATCH_DATE.clear();
        ORDER_ARRIVAL_DATE.clear();

        Cursor c = db.rawQuery("SELECT name, location, manager FROM warehouses ORDER BY rowid", null);
        while (c.moveToNext()) {
            WAREHOUSE_NAME.add(c.getString(0));
            WAREHOUSE_LOCATION.add(c.getString(1));
            WAREHOUSE_MANAGER.add(c.getString(2));
        }
        c.close();
        c = db.rawQuery("SELECT sku, name, unit, category FROM products ORDER BY rowid", null);
        while (c.moveToNext()) {
            PRODUCT_SKU.add(c.getString(0));
            PRODUCT_NAME.add(c.getString(1));
            PRODUCT_UNIT.add(c.getString(2));
            PRODUCT_CATEGORY.add(c.getString(3));
        }
        c.close();
        c = db.rawQuery("SELECT warehouse, product, qty FROM stock ORDER BY rowid", null);
        while (c.moveToNext()) {
            STOCK_WAREHOUSE.add(c.getString(0));
            STOCK_PRODUCT.add(c.getString(1));
            STOCK_QTY.add(c.getInt(2));
        }
        c.close();
        c = db.rawQuery("SELECT id, source, dest, product, qty, approved_qty, received_qty, status, reason, "
                + "dispatch_date, arrival_date FROM orders ORDER BY rowid", null);
        while (c.moveToNext()) {
            ORDER_ID.add(c.getString(0));
            ORDER_SOURCE.add(c.getString(1));
            ORDER_DEST.add(c.getString(2));
            ORDER_PRODUCT.add(c.getString(3));
            ORDER_QTY.add(c.getInt(4));
            ORDER_APPROVED_QTY.add(c.getInt(5));
            ORDER_RECEIVED_QTY.add(c.getInt(6));
            ORDER_STATUS.add(c.getString(7));
            ORDER_REASON.add(c.getString(8));
            ORDER_DISPATCH_DATE.add(c.getString(9));
            ORDER_ARRIVAL_DATE.add(c.getString(10));
        }
        c.close();
    }

    // Rewrites the four data tables from the lists above. Call after any change to them.
    public static void save(Context context) {
        SQLiteDatabase db = new DbHelper(context).getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("warehouses", null, null);
            db.delete("products", null, null);
            db.delete("stock", null, null);
            db.delete("orders", null, null);
            for (int i = 0; i < WAREHOUSE_NAME.size(); i++) {
                ContentValues v = new ContentValues();
                v.put("name", WAREHOUSE_NAME.get(i));
                v.put("location", WAREHOUSE_LOCATION.get(i));
                v.put("manager", WAREHOUSE_MANAGER.get(i));
                db.insert("warehouses", null, v);
            }
            for (int i = 0; i < PRODUCT_SKU.size(); i++) {
                ContentValues v = new ContentValues();
                v.put("sku", PRODUCT_SKU.get(i));
                v.put("name", PRODUCT_NAME.get(i));
                v.put("unit", PRODUCT_UNIT.get(i));
                v.put("category", PRODUCT_CATEGORY.get(i));
                db.insert("products", null, v);
            }
            for (int i = 0; i < STOCK_WAREHOUSE.size(); i++) {
                ContentValues v = new ContentValues();
                v.put("warehouse", STOCK_WAREHOUSE.get(i));
                v.put("product", STOCK_PRODUCT.get(i));
                v.put("qty", STOCK_QTY.get(i));
                db.insert("stock", null, v);
            }
            for (int i = 0; i < ORDER_ID.size(); i++) {
                ContentValues v = new ContentValues();
                v.put("id", ORDER_ID.get(i));
                v.put("source", ORDER_SOURCE.get(i));
                v.put("dest", ORDER_DEST.get(i));
                v.put("product", ORDER_PRODUCT.get(i));
                v.put("qty", ORDER_QTY.get(i));
                v.put("approved_qty", ORDER_APPROVED_QTY.get(i));
                v.put("received_qty", ORDER_RECEIVED_QTY.get(i));
                v.put("status", ORDER_STATUS.get(i));
                v.put("reason", ORDER_REASON.get(i));
                v.put("dispatch_date", ORDER_DISPATCH_DATE.get(i));
                v.put("arrival_date", ORDER_ARRIVAL_DATE.get(i));
                db.insert("orders", null, v);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Loads saved data once per process; seeds and saves the demo data only if the tables are empty.
    public static void seedDemo(Context context) {
        if (LOADED) return;
        LOADED = true;
        load(context);
        if (!WAREHOUSE_NAME.isEmpty()) return;

        WAREHOUSE_NAME.addAll(Arrays.asList(HUB, "Branch A", "Branch B"));
        WAREHOUSE_LOCATION.addAll(Arrays.asList("Port Area, Manila", "Quezon City", "Cebu City"));
        WAREHOUSE_MANAGER.addAll(Arrays.asList("Hana Santos", "Ana Reyes", "Ben Cruz"));

        addProduct("SKU-001", "Rice 25kg", "sacks", "Grocery");
        addProduct("SKU-002", "Cooking Oil 1L", "bottles", "Grocery");
        addProduct("SKU-003", "Canned Sardines", "cans", "Grocery");
        addProduct("SKU-004", "Bottled Water 500ml", "bottles", "Beverage");
        addProduct("SKU-005", "Laundry Soap", "bars", "Household");
        addProduct("SKU-006", "Packing Tape", "rolls", "Supplies");

        // Hub holds plenty, branches are lower, with a few below LOW_STOCK_LEVEL
        int[][] qty = {
                {200, 150, 300, 500, 120, 80},
                {40, 8, 25, 60, 5, 30},
                {15, 35, 6, 45, 20, 9}
        };
        for (int w = 0; w < 3; w++) {
            for (int p = 0; p < 6; p++) {
                STOCK_WAREHOUSE.add(WAREHOUSE_NAME.get(w));
                STOCK_PRODUCT.add(PRODUCT_SKU.get(p));
                STOCK_QTY.add(qty[w][p]);
            }
        }

        addOrder("TO-001", HUB, "Branch A", "SKU-002", 40, 0, 0, STATUS_PENDING, "", "", "");
        addOrder("TO-002", HUB, "Branch B", "SKU-003", 30, 30, 0, STATUS_APPROVED, "", "", "");
        addOrder("TO-003", HUB, "Branch A", "SKU-005", 20, 20, 0, STATUS_SCHEDULED, "", "20 Oct 2026", "22 Oct 2026");
        addOrder("TO-004", HUB, "Branch B", "SKU-006", 25, 25, 0, STATUS_IN_TRANSIT, "", "15 Oct 2026", "17 Oct 2026");
        addOrder("TO-005", HUB, "Branch A", "SKU-004", 100, 100, 100, STATUS_COMPLETED, "", "05 Oct 2026", "07 Oct 2026");
        addOrder("TO-006", HUB, "Branch B", "SKU-001", 50, 50, 42, STATUS_PARTIAL, "", "01 Oct 2026", "04 Oct 2026");
        addOrder("TO-007", HUB, "Branch A", "SKU-001", 500, 0, 0, STATUS_REJECTED, "Quantity exceeds available stock", "", "");
        save(context);
    }

    private static void addProduct(String sku, String name, String unit, String category) {
        PRODUCT_SKU.add(sku);
        PRODUCT_NAME.add(name);
        PRODUCT_UNIT.add(unit);
        PRODUCT_CATEGORY.add(category);
    }

    private static void addOrder(String id, String source, String dest, String sku, int qty, int approved,
                                 int received, String status, String reason, String dispatch, String arrival) {
        ORDER_ID.add(id);
        ORDER_SOURCE.add(source);
        ORDER_DEST.add(dest);
        ORDER_PRODUCT.add(sku);
        ORDER_QTY.add(qty);
        ORDER_APPROVED_QTY.add(approved);
        ORDER_RECEIVED_QTY.add(received);
        ORDER_STATUS.add(status);
        ORDER_REASON.add(reason);
        ORDER_DISPATCH_DATE.add(dispatch);
        ORDER_ARRIVAL_DATE.add(arrival);
    }
}
