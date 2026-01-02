# SafeGuard AI: ML-Powered Women's Safety Application

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://www.android.com/)
[![TensorFlow](https://img.shields.io/badge/TensorFlow-2.13-orange.svg)](https://www.tensorflow.org/)

**SafeGuard AI** is an intelligent Android application that provides 24x7 real-time audio threat detection for women's safety using deep learning. The system continuously monitors ambient audio, identifies distress signals (screams, help calls, panic sounds), and automatically triggers emergency response protocols including SMS alerts, phone calls, and GPS location sharing to pre-configured emergency contacts.

## 🎯 Key Features

### 📱 User Interface
- **Dashboard**: Real-time protection status, statistics, panic button
- **Emergency Contacts Manager**: Add/edit/delete contacts from phonebook or manual entry
- **Settings**: Auto-call toggle, detection sensitivity, battery optimization
- **Splash Screen**: Brand identity and initialization

### 🎙️ Audio Monitoring System
- **24x7 Background Service**: Foreground service with persistent notification
- **Voice Activity Detection (VAD)**: Filters silence to save battery (70% battery savings)
- **Real-time MFCC Extraction**: Java-based feature extraction matching Python training
- **TFLite Model Inference**: On-device AI processing (no cloud dependency)
- **Adaptive Threshold**: Consecutive detection logic to reduce false positives

### 🚨 Emergency Response System
- **Automatic SMS Alerts**: Sends detailed emergency messages with:
  - User name and timestamp
  - GPS coordinates and address
  - Google Maps link for navigation
  - Threat confidence percentage
- **Auto-Call Emergency Services**: Optional 112/911 dialing
- **Multi-Contact Alerts**: Simultaneous alerts to all emergency contacts
- **Firebase Cloud Logging**: Real-time event backup to Firebase Realtime Database
- **Local Notifications**: Critical alerts with sound and vibration

### 📍 Location Services
- **GPS Tracking**: Fused Location Provider for accurate positioning
- **Reverse Geocoding**: Converts coordinates to human-readable addresses
- **Background Location**: Continues tracking even when app is minimized
- **Google Maps Integration**: Direct link sharing for emergency responders

### 🔒 Privacy & Security
- **On-Device Processing**: Audio never uploaded to cloud
- **Local ML Inference**: TFLite model runs entirely on device
- **Encrypted Storage**: SharedPreferences encryption for sensitive data
- **HTTPS Only**: Network security config enforces secure connections
- **Minimal Permissions**: Only requests essential Android permissions

### ⚡ Performance Optimization
- **Battery Efficient**: <5% battery drain per hour
- **Wake Lock Management**: Partial wake lock for background operation
- **CPU Throttling**: GPU delegate for ML inference when available
- **Memory Management**: Efficient audio buffer handling
- **Auto-Start**: Boot receiver restarts protection after device reboot

## 📊 Technical Specifications

### Machine Learning Model
- **Architecture**: Convolutional Neural Network (CNN)
- **Input**: 1-second audio chunks at 16kHz sampling rate
- **Feature Extraction**: 40 MFCC (Mel-Frequency Cepstral Coefficients) × 100 timesteps
- **Model Size**: 2.3 MB (TensorFlow Lite format)
- **Performance**: 87.3% accuracy, 89.2% precision, 84.5% recall
- **Inference Time**: <100ms on mobile devices

### Android Application
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Language**: Java 8
- **Architecture**: Service-based background processing with MVVM pattern
- **ML Runtime**: TensorFlow Lite 2.13 with GPU delegate

## 🏗️ Architecture

### Data Flow

```
1. Audio Capture (ThreatDetectionService)
   16kHz Microphone → AudioRecord → 1-second buffer (16,000 samples)

2. Voice Activity Detection
   Calculate RMS energy → Filter silence → Pass to ML if energy > threshold

3. Feature Extraction (MFCCExtractor)
   Raw audio → Pre-emphasis filter → Framing → Hamming window → FFT → 
   Mel filterbank → Log compression → DCT → 40 MFCCs → Normalize → [100×40] matrix

4. ML Inference (AudioClassifier)
   MFCC [100×40] → TFLite model → [normal_prob, distress_prob]

5. Threat Detection
   IF distress_prob > 0.7 AND consecutive_detections >= 2 THEN trigger_emergency()

6. Emergency Response (EmergencyResponseService)
   Get GPS → Load contacts → Send SMS (parallel) → Auto-call 112 (optional) → 
   Log to Firebase → Show notification
```

### Project Structure

```
SafeguardAI/
├── app/                          # Android Application
│   ├── src/main/
│   │   ├── java/com/example/android/
│   │   │   ├── activities/      # UI Activities
│   │   │   ├── services/        # Background Services
│   │   │   ├── ml/              # ML Inference (TFLite)
│   │   │   ├── models/          # Data Models
│   │   │   ├── utils/           # Helper Classes
│   │   │   └── receivers/       # Broadcast Receivers
│   │   ├── res/                 # Resources (layouts, strings, etc.)
│   │   ├── assets/              # TFLite model files
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── ml/                           # Machine Learning Training
│   ├── scripts/                  # Training pipeline
│   │   ├── 0_setup_environment.py
│   │   ├── 1_prepare_datasets.py
│   │   ├── 2_preprocess_audio.py
│   │   ├── 2_train_mfcc_cnn.py
│   │   ├── 3_train_wav2vec2.py
│   │   ├── 4_test_models.py
│   │   ├── 5_export_models.py
│   │   └── 6_test_tflite_inference.py
│   ├── datasets/                 # Training datasets
│   ├── models/                   # Trained models (.h5, .tflite)
│   └── requirements.txt
│
└── README.md
```

## 🚀 Getting Started

### Prerequisites

#### For Android Development
- Android Studio Arctic Fox or later
- Android SDK 26+ (Android 8.0+)
- JDK 8 or later
- Google Maps API key (for location features)
- Firebase project (for cloud logging)

#### For ML Training
- Python 3.8+
- TensorFlow 2.13+
- Librosa, NumPy, Pandas, Scikit-learn
- 2000+ audio samples (distress + normal)

### Installation

#### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/SafeguardAI.git
cd SafeguardAI
```

#### 2. Android Setup

1. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `SafeguardAI` directory

2. **Configure Google Maps API**
   - Get API key from [Google Cloud Console](https://console.cloud.google.com/)
   - Update `app/build.gradle.kts`:
     ```kotlin
     manifestPlaceholders["MAPS_API_KEY"] = "YOUR_GOOGLE_MAPS_API_KEY"
     ```

3. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Download `google-services.json` and place it in `app/` directory
   - Enable Realtime Database in Firebase Console

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   # Or use Android Studio: Run > Run 'app'
   ```

#### 3. ML Model Training (Optional)

If you want to train your own model:

1. **Setup Python Environment**
   ```bash
   cd ml
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   pip install -r requirements.txt
   ```

2. **Prepare Dataset**
   - Place audio files in `ml/datasets/`:
     - `distress/`: Screams, help calls, panic sounds
     - `normal/`: Conversation, ambient noise, music
   - Minimum 500 samples per class

3. **Run Training Pipeline**
   ```bash
   cd scripts
   python 1_prepare_datasets.py    # Prepare and validate data
   python 2_preprocess_audio.py    # Preprocess audio files
   python 2_train_mfcc_cnn.py      # Train CNN model
   python 4_test_models.py          # Evaluate model
   python 5_export_models.py        # Export to TFLite
   ```

4. **Deploy Model to Android**
   - Copy `.tflite` file from `ml/models/` to `app/src/main/assets/`
   - Ensure filename matches `AudioClassifier.MODEL_PATH`

## 📱 Usage

### First Time Setup

1. **Launch App**: Open SafeGuard AI from app drawer
2. **Grant Permissions**: Allow microphone, location, SMS, and phone permissions
3. **Add Emergency Contacts**: 
   - Tap "Emergency Contacts"
   - Add contacts from phonebook or enter manually
   - Minimum 1 contact required
4. **Configure Settings**:
   - Enable/disable auto-call to emergency services
   - Adjust detection sensitivity (50%-80%)
   - Set user name for emergency alerts

### Activating Protection

1. **Toggle Protection**: Switch ON the protection toggle on main screen
2. **Verify Service**: Check notification bar for "Monitoring active" notification
3. **Test Panic Button**: Long-press the red panic button to trigger manual emergency

### During Emergency

When a threat is detected:
1. **Automatic Actions**:
   - SMS sent to all emergency contacts with location
   - Optional auto-call to 112/911 (if enabled)
   - Event logged to Firebase
   - Local notification displayed

2. **Manual Trigger**:
   - Long-press panic button (3 seconds)
   - Confirm emergency alert
   - Same actions as automatic detection

## 🔧 Configuration

### Detection Sensitivity

Adjust in Settings:
- **High (80%)**: More sensitive, may have false positives
- **Medium (70%)**: Balanced (default)
- **Low (50%)**: Less sensitive, fewer false positives

### Battery Optimization

To ensure 24x7 operation:
1. Go to Android Settings > Apps > SafeGuard AI
2. Battery > Unrestricted
3. Allow background activity

### Emergency Number

Default: `112` (India ERSS)
- Change in `EmergencyResponseService.EMERGENCY_NUMBER`
- Use `911` for USA, `999` for UK, etc.

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### ML Model Testing
```bash
cd ml/scripts
python 4_test_models.py      # Evaluate on test set
python 6_test_tflite_inference.py  # Test TFLite conversion
```

### Manual Testing Checklist
- [ ] Audio recording works in background
- [ ] ML inference runs without errors
- [ ] SMS alerts sent successfully
- [ ] Location sharing accurate
- [ ] Firebase logging functional
- [ ] Panic button triggers emergency
- [ ] Service restarts after reboot

## 📊 Performance Metrics

### Model Performance
- **Accuracy**: 87.3%
- **Precision**: 89.2%
- **Recall**: 84.5%
- **F1-Score**: 86.8%

### App Performance
- **Inference Time**: <100ms per 1-second audio chunk
- **Battery Drain**: <5% per hour
- **Memory Usage**: ~50MB (app + model)
- **CPU Usage**: <10% average

## 🛠️ Development

### Adding New Features

1. **New ML Model**: 
   - Train in `ml/scripts/`
   - Export to TFLite
   - Update `AudioClassifier.java` if input/output changes

2. **New Emergency Action**:
   - Extend `EmergencyResponseService.java`
   - Add UI toggle in `SettingsActivity.java`

3. **New UI Screen**:
   - Create Activity in `activities/`
   - Add to `AndroidManifest.xml`
   - Update navigation

### Code Style
- Follow Android Java style guide
- Use meaningful variable names
- Add Javadoc comments for public methods
- Keep methods under 50 lines

## 🐛 Troubleshooting

### Common Issues

**Issue**: "Model not initialized"
- **Solution**: Ensure `audio_mfcc_cnn.tflite` exists in `app/src/main/assets/`

**Issue**: "Permission denied"
- **Solution**: Grant all permissions in Android Settings > Apps > SafeGuard AI

**Issue**: "SMS not sending"
- **Solution**: Check SMS permission and verify emergency contacts have valid phone numbers

**Issue**: "Location not found"
- **Solution**: Enable GPS and grant location permissions

**Issue**: "Firebase connection failed"
- **Solution**: Verify `google-services.json` is in `app/` directory and Firebase project is active

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- TensorFlow Lite team for on-device ML framework
- Librosa for audio feature extraction
- Android community for best practices
- Women's safety advocates for inspiration

## 📞 Support

For issues, questions, or contributions:
- **GitHub Issues**: [Create an issue](https://github.com/yourusername/SafeguardAI/issues)
- **Email**: support@safeguardai.com
- **Documentation**: [Full Documentation](https://safeguardai.readthedocs.io/)

## 🔮 Future Enhancements

- [ ] Multi-language distress detection (Hindi, regional languages)
- [ ] Smartwatch integration (Samsung Galaxy Watch, Wear OS)
- [ ] Community safety network (nearby SafeGuard users can help)
- [ ] Cloud backup of threat events with end-to-end encryption
- [ ] Integration with local police APIs
- [ ] Video recording trigger during emergencies
- [ ] Biometric authentication for privacy
- [ ] Offline mode with local SMS gateway

---

**Made with ❤️ for Women's Safety**

*SafeGuard AI - Your AI-Powered Safety Companion*

