package com.example.android.services;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.android.utils.EncryptionHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Service to record and encrypt audio during a threat incident
 */
public class AudioMonitoringService {

    private static final String TAG = "AudioMonitoringService";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORD_DURATION_MS = 30000; // 30 seconds

    private boolean isRecording = false;
    private final Context context;
    private final EncryptionHelper encryptionHelper;

    public AudioMonitoringService(Context context) {
        this.context = context;
        this.encryptionHelper = new EncryptionHelper();
    }

    /**
     * Start recording raw audio, encrypt it, and save it privately
     */
    public void startIncidentRecording() {
        if (isRecording) return;

        new Thread(() -> {
            isRecording = true;
            Log.d(TAG, "Starting incident recording (encrypted)...");

            AudioRecord recorder = null;
            FileOutputStream fos = null;
            File outputFile = new File(context.getFilesDir(), "incident_" + System.currentTimeMillis() + ".enc");

            try {
                int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

                recorder.startRecording();
                fos = new FileOutputStream(outputFile);

                long startTime = System.currentTimeMillis();
                byte[] data = new byte[bufferSize];

                while (isRecording && (System.currentTimeMillis() - startTime) < RECORD_DURATION_MS) {
                    int read = recorder.read(data, 0, bufferSize);
                    if (read > 0) {
                        // Encrypt the chunk before writing to disk
                        byte[] encryptedChunk = encryptionHelper.encrypt(data);
                        
                        // Write size of chunk first so we can read it back
                        ByteBuffer sizeBuf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(encryptedChunk.length);
                        fos.write(sizeBuf.array());
                        fos.write(encryptedChunk);
                    }
                }

                Log.d(TAG, "Incident recording finished and encrypted: " + outputFile.getName());

            } catch (Exception e) {
                Log.e(TAG, "Error during encrypted recording: " + e.getMessage());
            } finally {
                isRecording = false;
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                }
                if (fos != null) {
                    try { fos.close(); } catch (IOException ignored) {}
                }
            }
        }).start();
    }

    public void stopRecording() {
        isRecording = false;
    }
}
