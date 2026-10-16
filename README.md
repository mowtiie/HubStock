# HubStock

An Android warehouse logistics app for a multi-branch business. Branch managers request stock, the hub manager approves it, deliveries are scheduled and dispatched, and the receiving branch accepts the delivery to update its inventory. It is a fully simulated demo: no backend, no payment gateway and no real notifications.

**Core flow:** Request Stock → Approve → Schedule Delivery → Replenish (Accept Delivery)

## About

HubStock is a practice project built in preparation for the Tagisan ng Talino 2026 - 2027 Code Fest hackathon.

| | |
|---|---|
| Team Name | ERRAnds |
| Team Members | [Vrixzandro Eliponga](https://github.com/mowtiie), [Michael Tagle](https://github.com/ImmunoCapacity), [Christine Jane Lapuz](https://github.com/chrstnjn) |

## Features

**Branch Manager**
- Create a transfer request for their own warehouse: pick the source (default Central Hub), one product and a quantity, validated against the source's available stock
- Track their orders and incoming deliveries
- Accept an in-transit delivery with a received quantity (full receipt completes the order, a shortfall marks it partial and shows the difference)
- View their own stock, with a low-stock warning
- Reports limited to their own branch

**Hub Manager**
- Approve a request with an approved quantity (can be lowered, never above source stock) or reject it with a reason
- Schedule deliveries with dispatch and expected arrival dates, then dispatch them
- Manage warehouses (name, location, branch manager)
- Manage products and Central Hub stock, and view any branch's stock read-only
- Reports for all branches

**Inventory Clerk**
- Manage products and Central Hub stock
- Schedule deliveries and advance their status
- Reports for all branches

**Reports:** Pending, Completed and Partial tabs, each with a count.

## Tech specs

| | |
|---|---|
| Language | Java 11 |
| UI | XML layouts, Material Design 3 (Material Components 1.14), view binding |
| Min / target SDK | 24 (Android 7.0) / 36 |
| Build | Android Gradle Plugin 9.2.1, Gradle 9.4.1 |
| Storage | Local SQLite for user accounts, warehouses, products, stock and orders |
| Theme | Material Theme Builder palette with light and dark mode |
| Navigation | One Activity per screen, no fragments |
| Libraries | AndroidX AppCompat, Material Components and ConstraintLayout only |

## Requirements

- Android Studio (a recent version that supports AGP 9.x) and JDK 11 or newer
- An Android emulator or device running Android 7.0 (API 24) or higher

## Getting started

```bash
git clone https://github.com/mowtiie/HubStock.git
cd HubStock
./gradlew assembleDebug
```

Then open the project in Android Studio and press **Run** on an emulator. The debug APK is created at `app/build/outputs/apk/debug/`.

## Demo accounts

All accounts use the password `demo1234`.

| Role | Email |
|---|---|
| Hub Manager | `hub@demo.com` |
| Inventory Clerk | `clerk@demo.com` |
| Branch Manager (Branch A) | `branch.a@demo.com` |
| Branch Manager (Branch B) | `branch.b@demo.com` |

There is no sign up. The stored account decides which home screen opens. Demo warehouses, products, stock and a few transfer orders in different statuses are seeded on first login, so no screen is empty.

## What is simulated

- Login and logout are fake but validated.
- Transit is simulated by a manual **Dispatch** button. There are no timers, maps or tracking.
- Messages are toasts and snackbars only. There are no real notifications.
- Approving an order does not reserve stock. Stock changes only on dispatch (source) and on accept (destination).
- Each order holds a single product.
- Data is saved in local SQLite and persists across restarts.

## Release build

Release builds are minified and shrunk, and signed with a key read from `keystore.properties` in the project root. Copy `keystore.properties.example` to `keystore.properties` and fill in your values:

```
storeFile=hubstock.jks
storePassword=your-store-password
keyAlias=hubstock
keyPassword=your-key-password
```

Then build with:

```bash
./gradlew assembleRelease
```

`keystore.properties`, `*.jks` and `*.keystore` are git-ignored and must never be committed.

## Verification

Release APKs can be verified with
[apksigner](https://developer.android.com/studio/command-line/apksigner.html#options-verify):

```
apksigner verify --print-certs --verbose hubstock.apk
```

Compare the certificate fingerprints in the output with the ones published for the release.

## License

See [LICENSE](LICENSE).
