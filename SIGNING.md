# ARSAM — امضای Release و GitHub Secrets

## ساخت keystore (یک‌بار، روی کامپیوتر خودتان)

```bash
cd ARSAM-Android
bash scripts/create-keystore.sh
```

فایل‌های ساخته‌شده (هرگز commit نکنید):

- `arsam-release.jks`
- `keystore.properties`

## افزودن Secrets در GitHub

Repository → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

| Secret | مقدار |
|--------|--------|
| `ARSAM_KEYSTORE_BASE64` | خروجی `base64 -w0 arsam-release.jks` |
| `ARSAM_KEYSTORE_PASSWORD` | رمز keystore |
| `ARSAM_KEY_ALIAS` | معمولاً `arsam` |
| `ARSAM_KEY_PASSWORD` | رمز کلید |

## اجرای بیلد امضاشده

1. **Actions** → **Build ARSAM Release (Signed)** → **Run workflow**
2. یا تگ بزنید: `git tag v5.2.0 && git push --tags`
3. Artifact: **ARSAM-release-signed**

## بیلد محلی امضاشده

اگر `keystore.properties` کنار پروژه باشد:

```bash
./gradlew assembleRelease
```

APK: `app/build/outputs/apk/release/app-release.apk`

## امنیت

- `.jks` و `keystore.properties` را در git نگذارید
- رمزها را در چت/اسکرین‌شات نفرستید
- گم شدن keystore یعنی نمی‌توانید همان امضا را برای آپدیت بعدی تکرار کنید — بکاپ امن بگیرید
