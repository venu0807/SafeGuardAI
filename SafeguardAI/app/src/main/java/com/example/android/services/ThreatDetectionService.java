package com.example.android.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.android.activities.MainActivity;
import com.example.android.ml.AudioClassifier;
import com.example.android.models.ClassificationResult;
import com.example.android.models.ThreatEvent;
import com.example.android.utils.LocationHelper;
import com.example.android.utils.NotificationHelper;
import com.example.android.utils.SharedPrefsHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

/**
 * 24x7 Silent Foreground Service for continuous protection
 */
public class ThreatDetectionService extends Service {

    private static final String TAG = "ThreatDetectionService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "silent_protection_channel";

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE_SECONDS = 1;
    private static final int BUFFER_SIZE = SAMPLE_RATE * BUFFER_SIZE_SECONDS;

    private float threatThreshold = 0.7f;
    private static final int CONSECUTIVE_DETECTIONS = 2;

    private boolean isRunning = false;
    private AudioRecord audioRecord;
    private Thread recordingThread;
    private AudioClassifier classifier;
    private int consecutiveThreats = 0;
    private long lastDetectionTime = 0;
    private long lastLocationCheckTime = 0;
    private PowerManager.WakeLock wakeLock;
    private SharedPrefsHelper prefsHelper;
    private AudioMonitoringService incidentRecorder;
    private FusedLocationProviderClient fusedLocationClient;
    private Handler permissionCheckHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        prefsHelper = new SharedPrefsHelper(this);
        threatThreshold = prefsHelper.getSensitivity();
        
        classifier = new AudioClassifier();
        classifier.initialize(this);
        incidentRecorder = new AudioMonitoringService(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SafeGuardAI::ThreatDetectionWakeLock");
            wakeLock.acquire();
        }
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createSilentNotification(), 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE | ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, createSilentNotification());
        }
        
        if (!isRunning) {
            startAudioMonitoring();
        }
        return START_STICKY;
    }

    private void startAudioMonitoring() {
        if (isRunning) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Recording permission missing. Checking again in 5 seconds...");
            permissionCheckHandler.postDelayed(this::startAudioMonitoring, 5000);
            return;
        }

        try {
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, Math.max(minBufferSize, BUFFER_SIZE * 2));
            audioRecord.startRecording();
            isRunning = true;
            recordingThread = new Thread(new AudioRecordingRunnable());
            recordingThread.start();
            Log.d(TAG, "Background monitoring started successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error starting audio: " + e.getMessage());
            permissionCheckHandler.postDelayed(this::startAudioMonitoring, 10000);
        }
    }

    private class AudioRecordingRunnable implements Runnable {
        @Override
        public void run() {
            short[] audioBuffer = new short[BUFFER_SIZE];
            while (isRunning) {
                try {
                    int samplesRead = audioRecord.read(audioBuffer, 0, BUFFER_SIZE);
                    if (samplesRead > 0) {
                        processAudioChunk(audioBuffer);
                        checkHazardZoneProximity();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in recording thread: " + e.getMessage());
                }
            }
        }
    }

    private void processAudioChunk(short[] audioData) {
        if (!hasVoiceActivity(audioData)) {
            consecutiveThreats = 0;
            return;
        }

        ClassificationResult result = classifier.classify(audioData);
        if (result != null && result.isDistress() && result.confidence >= threatThreshold) {
            consecutiveThreats++;
            if (consecutiveThreats >= CONSECUTIVE_DETECTIONS) {
                onThreatDetected(result);
                consecutiveThreats = 0;
            }
        } else if (consecutiveThreats > 0) {
            consecutiveThreats--;
        }
    }

    private void checkHazardZoneProximity() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLocationCheckTime < 300000) return;
        lastLocationCheckTime = currentTime;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) return;
            List<ThreatEvent> hazardZones = prefsHelper.getHazardLocations();
            for (ThreatEvent zone : hazardZones) {
                float distance = LocationHelper.calculateDistance(location.getLatitude(), location.getLongitude(), zone.latitude, zone.longitude);
                if (distance < 200) {
                    NotificationHelper.showInfoNotification(this, "Safety Reminder", "You are near a previous threat location.");
                    break; 
                }
            }
        });
    }

    private boolean hasVoiceActivity(short[] audioData) {
        double sum = 0;
        for (short sample : audioData) sum += sample * sample;
        return Math.sqrt(sum / audioData.length) > 500.0;
    }

    private void onThreatDetected(ClassificationResult result) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDetectionTime < 30000) return;
        lastDetectionTime = currentTime;

        incidentRecorder.startIncidentRecording();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    ThreatEvent event = new ThreatEvent(System.currentTimeMillis(), result.confidence, result.distressProbability, location.getLatitude(), location.getLongitude());
                    prefsHelper.addHazardLocation(event);
                }
            });
        }

        // Notify UI in real-time
        Intent broadcastIntent = new Intent("THREAT_DETECTED");
        broadcastIntent.putExtra("confidence", result.confidence);
        broadcastIntent.setPackage(getPackageName());
        sendBroadcast(broadcastIntent);

        Intent emergencyIntent = new Intent(this, EmergencyResponseService.class);
        emergencyIntent.putExtra("confidence", result.confidence);
        emergencyIntent.putExtra("distress_prob", result.distressProbability);
        startService(emergencyIntent);
        
        prefsHelper.incrementThreatCount();
        prefsHelper.incrementDetectionCount();
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        permissionCheckHandler.removeCallbacksAndMessages(null);
        if (audioRecord != null) { audioRecord.stop(); audioRecord.release(); }
        if (classifier != null) classifier.close();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        super.onDestroy();
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Background Protection", NotificationManager.IMPORTANCE_MIN);
            channel.setShowBadge(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification createSilentNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SafeGuard AI Active")
                .setContentText("Continuous background protection is running.")
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .build();
    }
}
