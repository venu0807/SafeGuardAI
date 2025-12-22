package com.example.android_app.ml;

import java.util.Arrays;

public class MFCCExtractor {

    // ===== MUST MATCH TRAINING =====
    private static final int SAMPLE_RATE = 16000;
    private static final int FRAME_SIZE = 400;   // 25 ms
    private static final int HOP_SIZE = 160;     // 10 ms
    private static final int FFT_SIZE = 512;
    private static final int NUM_MEL = 40;
    private static final int NUM_MFCC = 40;

    private static final double[] HAMMING = createHamming();

    public static float[][] extract2D(short[] audio) {

        double[] signal = normalize(audio);
        int numFrames = 1 + (signal.length - FRAME_SIZE) / HOP_SIZE;

        if (numFrames <= 0) {
            return new float[NUM_MFCC][1];
        }

        float[][] mfccMatrix = new float[NUM_MFCC][numFrames];

        for (int i = 0; i < numFrames; i++) {
            int start = i * HOP_SIZE;

            double[] frame = Arrays.copyOfRange(
                    signal, start, start + FRAME_SIZE
            );

            applyWindow(frame);
            double[] mag = magnitudeFFT(frame);
            double[] mel = melFilter(mag);
            double[] logMel = log(mel);
            double[] mfcc = dct(logMel);

            for (int j = 0; j < NUM_MFCC; j++) {
                mfccMatrix[j][i] = (float) mfcc[j];
            }
        }

        // ===== CMVN (CRITICAL FIX) =====
        for (int i = 0; i < NUM_MFCC; i++) {
            float mean = 0f;
            float var = 0f;

            for (int j = 0; j < numFrames; j++) {
                mean += mfccMatrix[i][j];
            }
            mean /= numFrames;

            for (int j = 0; j < numFrames; j++) {
                float diff = mfccMatrix[i][j] - mean;
                var += diff * diff;
            }
            var = (float) Math.sqrt(var / numFrames + 1e-6f);

            for (int j = 0; j < numFrames; j++) {
                mfccMatrix[i][j] =
                        (mfccMatrix[i][j] - mean) / var;
            }
        }

        return mfccMatrix;
    }

    // ---------- helpers ----------

    private static double[] normalize(short[] x) {
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            out[i] = x[i] / 32768.0;
        }
        return out;
    }

    private static void applyWindow(double[] frame) {
        for (int i = 0; i < frame.length; i++) {
            frame[i] *= HAMMING[i];
        }
    }

    private static double[] magnitudeFFT(double[] frame) {
        double[] mag = new double[FFT_SIZE / 2 + 1];

        for (int k = 0; k < mag.length; k++) {
            double re = 0, im = 0;
            for (int n = 0; n < frame.length; n++) {
                double angle = 2 * Math.PI * k * n / FFT_SIZE;
                re += frame[n] * Math.cos(angle);
                im -= frame[n] * Math.sin(angle);
            }
            mag[k] = Math.sqrt(re * re + im * im);
        }
        return mag;
    }

    private static double[] melFilter(double[] mag) {
        double[] mel = new double[NUM_MEL];

        for (int i = 0; i < NUM_MEL; i++) {
            int start = i * mag.length / NUM_MEL;
            int end = (i + 1) * mag.length / NUM_MEL;

            double sum = 0;
            for (int j = start; j < end; j++) {
                sum += mag[j];
            }
            mel[i] = sum + 1e-9;
        }
        return mel;
    }

    private static double[] log(double[] x) {
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            out[i] = Math.log(x[i]);
        }
        return out;
    }

    private static double[] dct(double[] x) {
        double[] out = new double[NUM_MFCC];

        for (int k = 0; k < NUM_MFCC; k++) {
            double sum = 0;
            for (int n = 0; n < x.length; n++) {
                sum += x[n] *
                        Math.cos(Math.PI * k * (2 * n + 1)
                                / (2 * x.length));
            }
            out[k] = sum;
        }
        return out;
    }

    private static double[] createHamming() {
        double[] w = new double[FRAME_SIZE];
        for (int i = 0; i < FRAME_SIZE; i++) {
            w[i] = 0.54 - 0.46 *
                    Math.cos(2 * Math.PI * i / (FRAME_SIZE - 1));
        }
        return w;
    }
}
