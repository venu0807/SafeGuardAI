package com.example.android_app.service;

import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.*;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.android_app.ml.AudioClassifier;

public class AudioDetectionService extends Service {

    private static final int SAMPLE_RATE = 16000;
    private static final int WINDOW_SIZE = 16000;

    private AudioRecord recorder;
    private boolean running = false;
    private AudioClassifier classifier;

    @Override
    public void onCreate() {
        super.onCreate();

        classifier = new AudioClassifier(this);
        startForeground(1, createNotification());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startAudioLoop();
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private void startAudioLoop() {

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

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            stopSelf();
            return;
        }

        recorder.startRecording();
        running = true;

        new Thread(() -> {
            short[] buffer = new short[WINDOW_SIZE];

            while (running) {
                int read = recorder.read(buffer, 0, buffer.length);
                if (read > 0) {
                    float score = classifier.predict(buffer);
                    Log.d("SAFEGUARD", "Score=" + score);
                }
            }
        }).start();
    }


    private Notification createNotification() {
        String channelId = "audio_detection";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Audio Detection",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("SafeGuard AI Active")
                .setContentText("Listening for distress sounds")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
