package com.example.android_app.ml;

public class ModelGuard {

    public static void validate(float[] mfcc) {

        if (mfcc == null || mfcc.length == 0) {
            throw new IllegalStateException("MFCC empty");
        }

        for (float v : mfcc) {
            if (Float.isNaN(v) || Float.isInfinite(v)) {
                throw new IllegalStateException("Invalid MFCC value");
            }
        }
    }
}
