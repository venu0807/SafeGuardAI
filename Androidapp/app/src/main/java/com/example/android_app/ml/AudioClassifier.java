package com.example.android_app.ml;

import android.content.Context;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class AudioClassifier {

    private Interpreter tflite;

    public AudioClassifier(Context context) {
        try {
            tflite = new Interpreter(loadModel(context));
        } catch (Exception e) {
            throw new RuntimeException("TFLite init failed", e);
        }
    }

    private MappedByteBuffer loadModel(Context context) throws IOException {
        FileInputStream fis =
                new FileInputStream(context.getAssets()
                        .openFd("mfcc_audio_model.tflite")
                        .getFileDescriptor());

        FileChannel channel = fis.getChannel();
        long startOffset = context.getAssets()
                .openFd("mfcc_audio_model.tflite")
                .getStartOffset();
        long declaredLength = context.getAssets()
                .openFd("mfcc_audio_model.tflite")
                .getDeclaredLength();

        return channel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
        );
    }

    public float predict(short[] audio) {
        float[] mfcc = MFCCExtractor.extract(audio);

        float[][] input = new float[1][mfcc.length];
        System.arraycopy(mfcc, 0, input[0], 0, mfcc.length);

        float[][] output = new float[1][1];
        tflite.run(input, output);

        return output[0][0];
    }
}
