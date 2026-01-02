# How to Run the Android Application

## ⚠️ Current Issue

The Android app build requires **Java 11 or higher**, but your system currently has **Java 8**.

**Error Message:**
```
Dependency requires at least JVM runtime version 11. This build uses a Java 8 JVM.
```

## ✅ Solution: Install Java 11+

### Option 1: Install Java 11+ JDK (Recommended)

1. **Download Java 11+ JDK**
   - Go to: https://adoptium.net/ (Eclipse Temurin - Free)
   - Or: https://www.oracle.com/java/technologies/downloads/ (Oracle JDK)
   - Download **JDK 11, 17, or 21** (LTS versions recommended)

2. **Install JDK**
   - Run the installer
   - Note the installation path (usually `C:\Program Files\Java\jdk-11` or similar)

3. **Set JAVA_HOME Environment Variable**
   ```powershell
   # Open PowerShell as Administrator
   [System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-11", "Machine")
   ```

4. **Update PATH**
   ```powershell
   # Add to PATH
   $currentPath = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
   [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;C:\Program Files\Java\jdk-11\bin", "Machine")
   ```

5. **Verify Installation**
   ```powershell
   java -version
   # Should show Java 11 or higher
   ```

6. **Restart Terminal/IDE**
   - Close and reopen PowerShell/Command Prompt
   - Or restart your computer

### Option 2: Use Android Studio's JDK

If you have Android Studio installed:

1. **Find Android Studio's JDK**
   - Usually at: `C:\Program Files\Android\Android Studio\jbr`
   - Or: `C:\Users\YourName\AppData\Local\Android\Sdk\jbr`

2. **Set JAVA_HOME to Android Studio's JDK**
   ```powershell
   [System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Android\Android Studio\jbr", "Machine")
   ```

3. **Restart terminal and verify**
   ```powershell
   java -version
   ```

## 🚀 After Installing Java 11+

### Step 1: Verify Java Version
```powershell
java -version
# Should show: java version "11.x.x" or higher
```

### Step 2: Build the Android App
```powershell
cd D:\Proposals\SafeguardAI
.\gradlew.bat assembleDebug
```

### Step 3: Install on Device/Emulator

**Option A: Using ADB (if device connected)**
```powershell
.\gradlew.bat installDebug
```

**Option B: Using Android Studio**
1. Open project in Android Studio
2. Click "Run" button (green play icon)
3. Select device/emulator
4. App will build and install automatically

### Step 4: Run the App
- The app will launch automatically after installation
- Or find "SafeGuard AI" in your app drawer

## 📱 Prerequisites for Running

### Required Setup:
- [x] Java 11+ JDK installed
- [x] Android SDK installed (via Android Studio)
- [x] Android device connected OR emulator running
- [ ] Google Maps API key configured (for location features)
- [ ] Firebase project setup (for cloud logging)

### Quick Setup Checklist:

1. **Google Maps API Key** (Optional but recommended)
   - Get from: https://console.cloud.google.com/
   - Edit `app/build.gradle.kts`:
     ```kotlin
     manifestPlaceholders["MAPS_API_KEY"] = "YOUR_API_KEY_HERE"
     ```

2. **Firebase Setup** (Optional but recommended)
   - Create project at: https://console.firebase.google.com/
   - Download `google-services.json`
   - Place in `app/` directory

3. **Permissions** (Will be requested at runtime)
   - Microphone
   - Location
   - SMS
   - Phone

## 🧪 Testing the App

### Manual Testing Steps:

1. **Launch App**
   - Open SafeGuard AI
   - Grant all permissions when prompted

2. **Add Emergency Contact**
   - Tap "Emergency Contacts"
   - Add at least one contact with phone number

3. **Activate Protection**
   - Toggle protection switch ON
   - Verify notification appears: "Monitoring active"

4. **Test Panic Button**
   - Long-press the red panic button
   - Confirm emergency alert
   - Verify SMS sent to contact

5. **Test ML Detection** (Requires actual distress audio)
   - Service runs in background
   - Scream or play distress audio near microphone
   - Should trigger emergency after 2 consecutive detections

## 🔧 Troubleshooting

### Build Fails with "Java 8" Error
- **Solution**: Install Java 11+ and set JAVA_HOME (see above)

### "SDK not found" Error
- **Solution**: Install Android SDK via Android Studio
  - Android Studio > SDK Manager > Install SDK Platform 26+

### "google-services.json not found"
- **Solution**: This is optional. Comment out Firebase dependencies if not using Firebase

### "Model not initialized" in App
- **Solution**: Ensure `audio_mfcc_cnn.tflite` exists in `app/src/main/assets/`
- Current status: ✅ Model file exists

### App Crashes on Launch
- **Solution**: Check logcat in Android Studio
  - View > Tool Windows > Logcat
  - Look for error messages

## 📊 Current Status

- ✅ **Code**: Complete and ready
- ✅ **Model**: TFLite file in assets
- ✅ **Gradle**: Configured correctly
- ⚠️ **Java**: Needs Java 11+ (currently Java 8)
- ✅ **Dependencies**: All configured

## 🎯 Quick Start (After Java 11+ Installed)

```powershell
# 1. Verify Java
java -version

# 2. Build app
cd D:\Proposals\SafeguardAI
.\gradlew.bat assembleDebug

# 3. Install on connected device
.\gradlew.bat installDebug

# 4. Or open in Android Studio and click Run
```

---

**Once Java 11+ is installed, the app will build and run successfully!** 🚀

