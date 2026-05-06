---
AIGC:
    ContentProducer: Minimax Agent AI
    ContentPropagator: Minimax Agent AI
    Label: AIGC
    ProduceID: "00000000000000000000000000000000"
    PropagateID: "00000000000000000000000000000000"
    ReservedCode1: 3045022010a7e9f53f17e3018f906e69353543fe10e0cc204d7bb55dbd8c6230f6256cae022100a26b3a2c823395f63b14fa5356dbc3ebeb6776c816e6d56cf7d6e5317e6f91e2
    ReservedCode2: 3046022100f347940b895c639722b121ea3295b97c80b51395dbbfba38dfe2f96dbb5b436c022100a164fa1d280b784a8b21ef7f39b66d1da6d4d52224aa4956d54eed54430d6e07
---

# RadioDBTool Android App

This folder contains a complete Android Studio project for the RadioDBTool mobile app.

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34
- JDK 17 or later

## Building the APK

1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to this `android` folder
4. Click "Sync Project with Gradle Files"
5. Wait for Gradle sync to complete
6. Go to Build > Build Bundle(s) / APK(s) > Build APK(s)
7. The APK will be available at `app/build/outputs/apk/debug/app-debug.apk`

## Alternative: Using Gradle from Command Line

```bash
cd android
./gradlew assembleDebug
```

The APK will be at `android/app/build/outputs/apk/debug/app-debug.apk`

## Project Structure

- `app/src/main/java/com/radiotool/app/MainActivity.kt` - Main WebView Activity
- `app/src/main/assets/` - Contains the web app files
- `app/src/main/AndroidManifest.xml` - App manifest
- `build.gradle` - Root build configuration
- `app/build.gradle` - App module configuration

## Features

- Downloads radio stations from Radio Browser API
- Supports full and filtered sync modes
- Export stations to M3U, CSV, JSON formats
- Local IndexedDB storage
- Mobile-optimized UI
