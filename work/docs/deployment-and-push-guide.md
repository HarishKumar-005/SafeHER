# SafeHer AR — Deployment & Push Guide

## Local Development

### Prerequisites

- Android Studio Hedgehog (2023.1) or later
- JDK 17+
- Android SDK 34 (API level 34)
- A physical Android device with ARCore support (for AR features)

### Build Debug APK

```bash
cd d:\Hackathons\SafeHER\SafeHER
.\gradlew assembleDebug
```

The APK will be at:
```
app\build\outputs\apk\debug\app-debug.apk
```

### Install on Device

```bash
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Run Unit Tests

```bash
.\gradlew test
```

### Run Instrumented Tests

```bash
.\gradlew connectedAndroidTest
```

## Git Workflow

### Initial Push

```bash
git init
git add .
git commit -m "SafeHer AR v1.0 — hackathon submission"
git remote add origin <your-repo-url>
git branch -M main
git push -u origin main
```

### Before Pushing

Ensure `google-services.json` is in `.gitignore`:

```
# Firebase
app/google-services.json
```

If it's already tracked:
```bash
git rm --cached app/google-services.json
echo "app/google-services.json" >> .gitignore
git commit -m "Remove google-services.json from tracking"
```

## Release Build (Optional)

### Generate Signed APK

1. Android Studio → Build → Generate Signed Bundle/APK
2. Choose APK
3. Create or select a keystore
4. Build type: release
5. Output: `app/build/outputs/apk/release/app-release.apk`

### Or via command line:

```bash
.\gradlew assembleRelease
```

> Requires `signingConfigs` in `app/build.gradle.kts`

## Useful Commands

| Command                          | Description                    |
|----------------------------------|--------------------------------|
| `.\gradlew assembleDebug`        | Build debug APK                |
| `.\gradlew test`                 | Run unit tests                 |
| `.\gradlew connectedAndroidTest` | Run instrumented tests         |
| `.\gradlew lint`                 | Run Android lint               |
| `.\gradlew clean`                | Clean build artifacts          |
| `adb logcat -s SafeHerAR`       | Filter logcat for app logs     |

## Architecture Notes

- **Package:** `com.phantomcrowd` (kept for Firebase compatibility)
- **App Name:** SafeHer AR
- **Theme:** `Theme.SafeHerAR`
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **AR:** ARCore + CameraX (dual camera management)
- **Maps:** osmdroid (OpenStreetMap, no API key needed)
- **AI:** MediaPipe on-device text classification
