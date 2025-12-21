package com.example.safeguard.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioRecorder {

    private static final int SAMPLE_RATE = 16000;
    private AudioRecord recorder;

    public AudioRecorder() {
        int bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );
    }

    public short[] recordOneSecond() {
        short[] buffer = new short[SAMPLE_RATE];
        recorder.startRecording();
        recorder.read(buffer, 0, buffer.length);
        return buffer;
    }
}
