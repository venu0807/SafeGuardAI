package com.example.safeguard.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.safeguard.audio.*;
import com.example.safeguard.ml.*;

public class ThreatDetectionService extends Service {

    private AudioRecorder recorder;
    private MFCCExtractor extractor;
    private MFCCGate gate;
    private TemporalGate temporal;

    @Override
    public void onCreate() {
        super.onCreate();

        recorder = new AudioRecorder();
        extractor = new MFCCExtractor();
        temporal = new TemporalGate();

        try {
            gate = new MFCCGate(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Notification notification = new NotificationCompat.Builder(this, "SAFE")
                .setContentTitle("SafeGuard Active")
                .setContentText("Listening for threats...")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .build();

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(() -> {
            while (true) {
                short[] pcm = recorder.recordOneSecond();

                float[] audio = new float[pcm.length];
                for (int i = 0; i < pcm.length; i++) {
                    audio[i] = pcm[i] / 32768f;
                }

                if (AudioPrefilter.isSuspicious(audio)) {
                    float[] mfcc = extractor.extract(audio);
                    float score = gate.predict(mfcc);
                    boolean consistent = temporal.update(score);

                    Log.d("SAFEGUARD",
                            "MFCC=" + score + " | Consistent=" + consistent);
                }
            }
        }).start();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
