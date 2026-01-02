package com.example.android.ml;

import android.util.Log;

/**
 * Optimized MFCC Extractor for Production
 * Uses Radix-2 FFT for efficient processing
 */
public class MFCCExtractor {

    private static final String TAG = "MFCCExtractor";

    private static final int SAMPLE_RATE = 16000;
    private static final int N_MFCC = 40;
    private static final int N_FFT = 2048;
    private static final int HOP_LENGTH = 512;
    private static final int MAX_TIME_STEPS = 100;
    private static final int N_MELS = 128;

    private static final float PRE_EMPHASIS_COEFF = 0.97f;
    private static final double LOG_EPSILON = 1e-10;

    private static float[][] melFilterBank = null;
    private static float[] hammingWindow = null;

    static {
        melFilterBank = createMelFilterBank();
        hammingWindow = createHammingWindow(N_FFT);
    }

    public static float[][] extractMFCC(short[] audioData) {
        try {
            float[] audioFloat = convertAndNormalize(audioData);
            audioFloat = applyPreEmphasis(audioFloat);
            float[][] frames = frameSignal(audioFloat);
            float[][] powerSpectrum = computePowerSpectrum(frames);
            float[][] melSpectrum = applyMelFilters(powerSpectrum);
            melSpectrum = logCompress(melSpectrum);
            float[][] mfcc = applyDCT(melSpectrum);
            mfcc = normalizeMFCC(mfcc);
            return padOrTruncate(mfcc, MAX_TIME_STEPS);
        } catch (Exception e) {
            Log.e(TAG, "MFCC extraction failed: " + e.getMessage());
            return new float[MAX_TIME_STEPS][N_MFCC];
        }
    }

    private static float[] convertAndNormalize(short[] audioData) {
        float[] normalized = new float[audioData.length];
        for (int i = 0; i < audioData.length; i++) {
            normalized[i] = audioData[i] / 32768.0f;
        }
        return normalized;
    }

    private static float[] applyPreEmphasis(float[] signal) {
        float[] emphasized = new float[signal.length];
        emphasized[0] = signal[0];
        for (int i = 1; i < signal.length; i++) {
            emphasized[i] = signal[i] - PRE_EMPHASIS_COEFF * signal[i - 1];
        }
        return emphasized;
    }

    private static float[][] frameSignal(float[] signal) {
        int numFrames = 1 + (signal.length - N_FFT) / HOP_LENGTH;
        numFrames = Math.min(numFrames, MAX_TIME_STEPS);
        float[][] frames = new float[numFrames][N_FFT];

        for (int i = 0; i < numFrames; i++) {
            int start = i * HOP_LENGTH;
            for (int j = 0; j < N_FFT && (start + j) < signal.length; j++) {
                frames[i][j] = signal[start + j] * hammingWindow[j];
            }
        }
        return frames;
    }

    private static float[][] computePowerSpectrum(float[][] frames) {
        int numFrames = frames.length;
        int specSize = N_FFT / 2 + 1;
        float[][] powerSpec = new float[numFrames][specSize];

        for (int i = 0; i < numFrames; i++) {
            float[] fftData = new float[N_FFT * 2];
            for (int j = 0; j < N_FFT; j++) {
                fftData[j * 2] = frames[i][j];
            }
            
            applyFFT(fftData);

            for (int j = 0; j < specSize; j++) {
                float real = fftData[j * 2];
                float imag = fftData[j * 2 + 1];
                powerSpec[i][j] = (real * real + imag * imag) / N_FFT;
            }
        }
        return powerSpec;
    }

    private static void applyFFT(float[] data) {
        int n = data.length / 2;
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1) j ^= bit;
            j ^= bit;
            if (i < j) {
                float tempReal = data[i * 2];
                float tempImag = data[i * 2 + 1];
                data[i * 2] = data[j * 2];
                data[i * 2 + 1] = data[j * 2 + 1];
                data[j * 2] = tempReal;
                data[j * 2 + 1] = tempImag;
            }
        }

        for (int len = 2; len <= n; len <<= 1) {
            double ang = 2 * Math.PI / len;
            float wlenReal = (float) Math.cos(ang);
            float wlenImag = (float) Math.sin(ang);
            for (int i = 0; i < n; i += len) {
                float wReal = 1, wImag = 0;
                for (int j = 0; j < len / 2; j++) {
                    float uReal = data[(i + j) * 2];
                    float uImag = data[(i + j) * 2 + 1];
                    float vReal = data[(i + j + len / 2) * 2] * wReal - data[(i + j + len / 2) * 2 + 1] * wImag;
                    float vImag = data[(i + j + len / 2) * 2] * wImag + data[(i + j + len / 2) * 2 + 1] * wReal;
                    data[(i + j) * 2] = uReal + vReal;
                    data[(i + j) * 2 + 1] = uImag + vImag;
                    data[(i + j + len / 2) * 2] = uReal - vReal;
                    data[(i + j + len / 2) * 2 + 1] = uImag - vImag;
                    float tmpReal = wReal * wlenReal - wImag * wlenImag;
                    wImag = wReal * wlenImag + wImag * wlenReal;
                    wReal = tmpReal;
                }
            }
        }
    }

    private static float[][] applyMelFilters(float[][] powerSpectrum) {
        int numFrames = powerSpectrum.length;
        float[][] melSpectrum = new float[numFrames][N_MELS];
        for (int i = 0; i < numFrames; i++) {
            for (int j = 0; j < N_MELS; j++) {
                float sum = 0;
                for (int k = 0; k < powerSpectrum[i].length; k++) {
                    sum += powerSpectrum[i][k] * melFilterBank[j][k];
                }
                melSpectrum[i][j] = sum;
            }
        }
        return melSpectrum;
    }

    private static float[][] logCompress(float[][] melSpectrum) {
        int numFrames = melSpectrum.length;
        float[][] logMel = new float[numFrames][N_MELS];
        for (int i = 0; i < numFrames; i++) {
            for (int j = 0; j < N_MELS; j++) {
                logMel[i][j] = (float) Math.log(melSpectrum[i][j] + LOG_EPSILON);
            }
        }
        return logMel;
    }

    private static float[][] applyDCT(float[][] melSpectrum) {
        int numFrames = melSpectrum.length;
        float[][] mfcc = new float[numFrames][N_MFCC];
        for (int i = 0; i < numFrames; i++) {
            for (int j = 0; j < N_MFCC; j++) {
                double sum = 0;
                for (int k = 0; k < N_MELS; k++) {
                    sum += melSpectrum[i][k] * Math.cos(Math.PI * j * (k + 0.5) / N_MELS);
                }
                mfcc[i][j] = (float) (sum * Math.sqrt(2.0 / N_MELS));
            }
        }
        return mfcc;
    }

    private static float[][] normalizeMFCC(float[][] mfcc) {
        if (mfcc.length == 0) return mfcc;
        for (int j = 0; j < N_MFCC; j++) {
            float mean = 0;
            for (float[] floats : mfcc) mean += floats[j];
            mean /= mfcc.length;
            float std = 0;
            for (float[] floats : mfcc) std += Math.pow(floats[j] - mean, 2);
            std = (float) Math.sqrt(std / mfcc.length);
            if (std > 1e-6) {
                for (int i = 0; i < mfcc.length; i++) mfcc[i][j] = (mfcc[i][j] - mean) / std;
            }
        }
        return mfcc;
    }

    private static float[] createHammingWindow(int size) {
        float[] window = new float[size];
        for (int i = 0; i < size; i++) {
            window[i] = (float) (0.54 - 0.46 * Math.cos(2 * Math.PI * i / (size - 1)));
        }
        return window;
    }

    private static float[][] createMelFilterBank() {
        float[][] filters = new float[N_MELS][N_FFT / 2 + 1];
        float fMin = 0;
        float fMax = SAMPLE_RATE / 2.0f;
        float melMin = hzToMel(fMin);
        float melMax = hzToMel(fMax);
        float[] melPoints = new float[N_MELS + 2];
        for (int i = 0; i < melPoints.length; i++) {
            melPoints[i] = melMin + (melMax - melMin) * i / (N_MELS + 1);
        }
        int[] bins = new int[melPoints.length];
        for (int i = 0; i < melPoints.length; i++) {
            bins[i] = (int) Math.floor((N_FFT + 1) * melToHz(melPoints[i]) / SAMPLE_RATE);
        }
        for (int i = 0; i < N_MELS; i++) {
            for (int j = bins[i]; j < bins[i + 1]; j++) filters[i][j] = (float) (j - bins[i]) / (bins[i + 1] - bins[i]);
            for (int j = bins[i + 1]; j < bins[i + 2]; j++) filters[i][j] = (float) (bins[i + 2] - j) / (bins[i + 2] - bins[i + 1]);
        }
        return filters;
    }

    private static float hzToMel(float hz) { return (float) (2595 * Math.log10(1 + hz / 700.0)); }
    private static float melToHz(float mel) { return (float) (700 * (Math.pow(10, mel / 2595.0) - 1)); }

    private static float[][] padOrTruncate(float[][] mfcc, int targetLength) {
        float[][] result = new float[targetLength][N_MFCC];
        int copyLength = Math.min(mfcc.length, targetLength);
        for (int i = 0; i < copyLength; i++) System.arraycopy(mfcc[i], 0, result[i], 0, N_MFCC);
        return result;
    }
}
