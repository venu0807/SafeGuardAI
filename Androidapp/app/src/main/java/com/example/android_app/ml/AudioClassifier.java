package com.example.android_app.ml;

import android.content.Context;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
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

    private MappedByteBuffer loadModel(Context context) throws Exception {

        FileInputStream fis = new FileInputStream(
                context.getAssets()
                        .openFd("mfcc_audio_model.tflite")
                        .getFileDescriptor()
        );

        FileChannel channel = fis.getChannel();
        long start = context.getAssets()
                .openFd("mfcc_audio_model.tflite")
                .getStartOffset();
        long length = context.getAssets()
                .openFd("mfcc_audio_model.tflite")
                .getDeclaredLength();

        return channel.map(
                FileChannel.MapMode.READ_ONLY,
                start,
                length
        );
    }

    public float predict(short[] audio) {

        float[][] mfcc2D = MFCCExtractor.extract2D(audio);

        int nMfcc = mfcc2D.length;
        int nFrames = mfcc2D[0].length;

        float[][][][] input =
                new float[1][nMfcc][nFrames][1];

        for (int i = 0; i < nMfcc; i++) {
            for (int j = 0; j < nFrames; j++) {
                float v = mfcc2D[i][j];
                if (Float.isNaN(v) || Float.isInfinite(v)) {
                    throw new IllegalStateException("Invalid MFCC");
                }
                input[0][i][j][0] = v;
            }
        }

        float[][] output = new float[1][1];
        tflite.run(input, output);

        return output[0][0];
    }
}
