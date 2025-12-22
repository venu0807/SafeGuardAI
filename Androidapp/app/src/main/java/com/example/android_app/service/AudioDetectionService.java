package com.example.android_app.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.android_app.MainActivity;
import com.example.android_app.R;
import com.example.android_app.ml.AudioClassifier;
import com.example.android_app.ml.InferenceStats;

public class AudioDetectionService extends Service {

    // ===== AUDIO CONFIG =====
    private static final int SAMPLE_RATE = 16000;
    private static final int WINDOW_SIZE = 16000; // 1 second

    private static final String CHANNEL_ID = "safeguard_active";
    private static final int NOTIF_ID = 101;

    private AudioRecord recorder;
    private boolean running = false;

    // ===== CORE COMPONENTS =====
    private AudioClassifier classifier;
    private InferenceStats stats;
    private InferenceScheduler scheduler;
    private TemporalEventDetector eventDetector;
    private EventLogger eventLogger;

    @Override
    public void onCreate() {
        super.onCreate();

        classifier = new AudioClassifier(this);
        stats = new InferenceStats();
        scheduler = new InferenceScheduler();
        eventDetector = new TemporalEventDetector();
        eventLogger = new EventLogger(this);

        try {
            createNotificationChannel();
            startForeground(NOTIF_ID, buildForegroundNotification());
        } catch (SecurityException e) {
            stopSelf();
            return;
        }

        startAudioLoop();
    }

    private void startAudioLoop() {

        // ---- PERMISSION CHECK (MANDATORY) ----
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }

        int minBuffer = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuffer
        );

        try {
            recorder.startRecording();
            running = true;
        } catch (SecurityException e) {
            stopSelf();
            return;
        }

        new Thread(() -> {

            short[] buffer = new short[WINDOW_SIZE];

            while (running) {

                recorder.read(buffer, 0, buffer.length);

                // ---- ENERGY DIAGNOSTIC ----
                long energy = 0;
                for (short s : buffer) energy += Math.abs(s);
                Log.d("SAFEGUARD_ENERGY", "energy=" + energy);

                // ---- MODEL INFERENCE ----
                float score = classifier.predict(buffer);
                stats.update(score);
                scheduler.adapt(score);

                // ---- TEMPORAL EVENT LOGIC ----
                TemporalEventDetector.Event event =
                        eventDetector.update(score);

                if (event != null) {
                    if (event.type ==
                            TemporalEventDetector.EventType.DISTRESS_EVENT_START) {

                        Intent alert = new Intent(
                                this,
                                com.example.android_app.ui.AlertActivity.class
                        );
                        alert.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(alert);

                        Log.w("SAFEGUARD_EVENT",
                                "DISTRESS_EVENT_START confidence=" +
                                        event.confidence);

                    } else if (event.type ==
                            TemporalEventDetector.EventType.DISTRESS_EVENT_END) {

                        Log.w("SAFEGUARD_EVENT",
                                "DISTRESS_EVENT_END confidence=" +
                                        event.confidence);
                    }

                    eventLogger.log(
                            event.type,
                            event.timestamp,
                            event.confidence
                    );
                }

                // ---- DEBUG STATS ----
                Log.d(
                        "SAFEGUARD",
                        "score=" + score +
                                " mean=" + stats.mean() +
                                " var=" + stats.variance()
                );

                // ---- BATTERY-AWARE DELAY ----
                try {
                    Thread.sleep(scheduler.delay());
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SafeGuard Protection",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Audio distress detection active");

            NotificationManager manager =
                    getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildForegroundNotification() {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SafeGuard Active")
                .setContentText("Listening for distress sounds")
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        running = false;
        if (recorder != null) {
            recorder.stop();
            recorder.release();
        }
        super.onDestroy();
    }
}
