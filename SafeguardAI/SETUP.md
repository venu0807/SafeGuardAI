# SafeGuard AI - Quick Setup Guide

## 🚀 5-Minute Setup

### Prerequisites Checklist
- [ ] Android Studio installed
- [ ] Android SDK 26+ installed
- [ ] Google account (for Maps API)
- [ ] Firebase account (for cloud logging)

### Step 1: Clone & Open Project
```bash
git clone https://github.com/yourusername/SafeguardAI.git
cd SafeguardAI
```
Open in Android Studio: `File > Open > Select SafeguardAI folder`

### Step 2: Get Google Maps API Key
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create new project or select existing
3. Enable "Maps SDK for Android"
4. Create API key
5. Copy API key

### Step 3: Configure API Key
Edit `app/build.gradle.kts`:
```kotlin
manifestPlaceholders["MAPS_API_KEY"] = "YOUR_ACTUAL_API_KEY_HERE"
```

### Step 4: Setup Firebase
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create new project: "SafeGuardAI"
3. Add Android app:
   - Package name: `com.example.android`
   - Download `google-services.json`
4. Place `google-services.json` in `app/` directory
5. Enable Realtime Database in Firebase Console

### Step 5: Build & Run
1. Click "Sync Project with Gradle Files" (elephant icon)
2. Wait for dependencies to download
3. Connect Android device or start emulator
4. Click "Run" (green play button)

### Step 6: First Launch
1. Grant all permissions when prompted
2. Add at least 1 emergency contact
3. Toggle protection ON
4. Test panic button (long-press)

## ✅ Verification

### Check Service is Running
- Look for persistent notification: "Monitoring active"
- Check notification bar icon

### Test Emergency Response
1. Long-press panic button
2. Confirm emergency alert
3. Verify SMS received by emergency contact
4. Check Firebase console for logged event

## 🔧 Troubleshooting

### Build Errors
- **"google-services.json not found"**: Download from Firebase Console
- **"API key invalid"**: Verify Maps API is enabled in Google Cloud
- **"Gradle sync failed"**: Check internet connection, invalidate caches

### Runtime Errors
- **"Model not initialized"**: Ensure `audio_mfcc_cnn.tflite` exists in `app/src/main/assets/`
- **"Permission denied"**: Grant all permissions in Android Settings
- **"SMS not sending"**: Check SMS permission and phone number format

## 📱 Testing Checklist

- [ ] App launches without crashes
- [ ] Permissions granted successfully
- [ ] Emergency contacts can be added
- [ ] Protection toggle works
- [ ] Service notification appears
- [ ] Panic button triggers emergency
- [ ] SMS sent to contacts
- [ ] Location shared correctly
- [ ] Firebase logs events

## 🎓 Next Steps

- Read [README.md](README.md) for detailed documentation
- Check [ml/README.md](ml/README.md) for ML training guide
- Customize detection sensitivity in Settings
- Add more emergency contacts

---

**Need Help?** Open an issue on GitHub or check the full README.

