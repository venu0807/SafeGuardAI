package com.example.android_app.ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

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
        AssetFileDescriptor afd =
                context.getAssets().openFd("mfcc_audio_model.tflite");

        FileInputStream fis = new FileInputStream(afd.getFileDescriptor());
        FileChannel channel = fis.getChannel();

        return channel.map(
                FileChannel.MapMode.READ_ONLY,
                afd.getStartOffset(),
                afd.getDeclaredLength()
        );
    }


    public float predict(short[] audio) {

        float[] mfccFlat = MFCCExtractor.extract(audio);

        final int TIME = 120;
        final int MFCC = 101;

        // 4D input tensor: [1, 120, 101, 1]
        float[][][][] input = new float[1][TIME][MFCC][1];

        int idx = 0;
        for (int t = 0; t < TIME; t++) {
            for (int m = 0; m < MFCC; m++) {
                input[0][t][m][0] = mfccFlat[idx++];
            }
        }

        float[][] output = new float[1][1];
        tflite.run(input, output);

        return output[0][0];
    }


}
