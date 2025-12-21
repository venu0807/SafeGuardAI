package com.example.safeguard.ml;

import android.content.Context;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.MappedByteBuffer;

import org.tensorflow.lite.support.common.FileUtil;

public class MFCCGate {

    private Interpreter interpreter;

    public MFCCGate(Context context) throws IOException {
        MappedByteBuffer model =
                FileUtil.loadMappedFile(context, "mfcc_audio_model.tflite");
        interpreter = new Interpreter(model);
    }

    public float predict(float[] input) {
        float[][] output = new float[1][1];
        interpreter.run(input, output);
        return output[0][0];
    }
}
