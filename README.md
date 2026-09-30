# ARSAM Android

Analysis-only market dashboard APK project for **Android 15** (Xiaomi Redmi Note 12S friendly).

- **App name:** ARSAM  
- **Application id:** `com.arsam.app`  
- **Data root (hard-locked):** `/storage/emulated/0/AAA`  
- **Stack:** Android WebView + Chaquopy (Python Flask engine)

## Build APK via GitHub Actions

1. Push this folder to a GitHub repository.
2. Open **Actions** → **Build ARSAM APK** → **Run workflow**.
3. Download the **ARSAM-debug-apk** artifact.

## Local build

Open in Android Studio (JDK 17), sync Gradle, then `assembleDebug`.

## Storage layout

```text
/storage/emulated/0/AAA/{data,cache,memory,secrets}
```

Analysis only. No order execution. Profit is not guaranteed.
