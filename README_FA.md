# ARSAM — ساخت APK اندروید ۱۵ (شیائومی Note 12S)

اپلیکیشن **ARSAM** داشبورد تحلیل بازار است (بدون اجرای سفارش).

| مورد | مقدار |
|------|--------|
| نام اپ | **ARSAM** |
| شناسه بسته | `com.arsam.app` |
| پوشه داده | **فقط** `/storage/emulated/0/AAA` |
| حداقل اندروید | 8.0 (API 26) |
| هدف | Android 15 (API 35) |
| معماری | arm64-v8a + armeabi-v7a |

## ساخت APK با GitHub (پیشنهادی)

### ۱) ساخت ریپو

1. در GitHub یک مخزن خالی بسازید (مثلاً `ARSAM-Android`).
2. این پوشه را push کنید:

```bash
cd ARSAM-Android
git init
git add .
git commit -m "ARSAM Android V51 AAA storage"
git branch -M main
git remote add origin https://github.com/YOUR_USER/ARSAM-Android.git
git push -u origin main
```

### ۲) اجرای Action

1. در GitHub بروید به **Actions**
2. workflow **Build ARSAM APK** را انتخاب کنید
3. **Run workflow** بزنید
4. بعد از اتمام، از **Artifacts** فایل `ARSAM-debug-apk` را دانلود کنید

### ۳) نصب روی گوشی شیائومی

1. در تنظیمات → امنیت: نصب از منابع ناشناس را برای مرورگر/فایل‌منیجر فعال کنید
2. APK را نصب کنید
3. هنگام اولین اجرا:
   - مجوز **اعلان** را بدهید
   - مجوز **مدیریت همه فایل‌ها (All files access)** را بدهید تا پوشه `AAA` ساخته شود
4. در MIUI/HyperOS: تنظیمات باتری → بدون محدودیت برای ARSAM

## ساختار پوشه AAA

```text
Internal storage/AAA/
  data/      دیتابیس و تنظیمات
  cache/     کش بازار
  memory/    حافظه یادگیری
  secrets/   کلید API اختیاری
```

کلید نمونه:

```text
/storage/emulated/0/AAA/secrets/gemini_api_key.txt
```

## ساخت محلی (Android Studio)

1. Android Studio Hedgehog+ و JDK 17
2. Open این پوشه به‌عنوان پروژه
3. Sync Gradle (افزونه Chaquopy وابستگی‌های Python را می‌گیرد)
4. Build → Build APK(s) یا Run روی گوشی

## نکات مهم

- **سود تضمین نیست** — فقط تحلیل احتمالی
- اولین بیلد GitHub ممکن است ۳۰–۶۰ دقیقه طول بکشد (دانلود NDK/چاقوپی)
- اگر Artifact خالی بود، لاگ Action را برای خطای Chaquopy/SDK بررسی کنید
- برای انتشار فروشگاهی باید keystore امضای release بسازید (این بیلد debug است)

## عیب‌یابی Note 12S

| مشکل | کار |
|------|-----|
| اپ باز می‌شود ولی سفید می‌ماند | مجوز All files + اینترنت |
| کرش فوری | از `adb logcat` خطای Python/Chaquopy را ببینید |
| بسته شدن در پس‌زمینه | باتری بدون محدودیت |
| عدم ساخت AAA | Settings → Apps → ARSAM → Permissions → Files |

## امضای Release و آیکون

- آیکون اختصاصی ARSAM در چند چگالی + Adaptive Icon
- راهنمای کامل امضا: فایل **SIGNING.md**
- workflow جدا: **Build ARSAM Release (Signed)**
- ساخت keystore: `bash scripts/create-keystore.sh`
