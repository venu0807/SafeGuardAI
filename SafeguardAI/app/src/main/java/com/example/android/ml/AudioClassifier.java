package com.example.android.ml;

import android.content.Context;
import android.util.Log;

import com.example.android.models.ClassificationResult;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.nio.MappedByteBuffer;
import java.util.Locale;

/**
 * TensorFlow Lite Audio Classifier
 * Runs MFCC+CNN model for distress detection
 */
public class AudioClassifier {

    private static final String TAG = "AudioClassifier";
    private static final String MODEL_PATH = "audio_mfcc_cnn.tflite";

    private static final int MAX_TIME_STEPS = 100;
    private static final int N_MFCC = 40;

    private Interpreter tflite;
    private GpuDelegate gpuDelegate = null;
    private boolean isInitialized = false;

    /**
     * Initialize TensorFlow Lite interpreter
     */
    public boolean initialize(Context context) {
        try {
            Log.d(TAG, "Initializing TensorFlow Lite model...");

            // Load model using TFLiteHelper
            MappedByteBuffer modelBuffer = TFLiteHelper.loadModelFile(context, MODEL_PATH);

            // Configure interpreter options
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);

            // Try to use GPU delegate if available
            CompatibilityList compatList = new CompatibilityList();
            if (compatList.isDelegateSupportedOnThisDevice()) {
                gpuDelegate = new GpuDelegate();
                options.addDelegate(gpuDelegate);
                Log.d(TAG, "GPU delegate enabled");
            } else {
                Log.d(TAG, "GPU not available, using CPU");
            }

            // Create interpreter
            tflite = new Interpreter(modelBuffer, options);
            isInitialized = true;
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error initializing model: " + e.getMessage());
            return false;
        }
    }

    /**
     * Classify audio from raw PCM samples
     */
    public ClassificationResult classify(short[] audioSamples) {
        if (!isInitialized) {
            Log.e(TAG, "Model not initialized");
            return new ClassificationResult(0.5f, 0.5f, 0, 0.5f, -1);
        }

        try {
            long startTime = System.currentTimeMillis();

            // Step 1: Extract MFCC features
            float[][] mfcc = MFCCExtractor.extractMFCC(audioSamples);

            // Step 2: Prepare tensors
            float[][][] input = new float[1][MAX_TIME_STEPS][N_MFCC];
            input[0] = mfcc;
            float[][] output = new float[1][2];

            // Step 3: Run inference
            long inferenceStart = System.currentTimeMillis();
            tflite.run(input, output);
            long inferenceTime = System.currentTimeMillis() - inferenceStart;

            // Step 4: Parse results
            float normalProb = output[0][0];
            float distressProb = output[0][1];
            int predictedClass = (distressProb > 0.5f) ? 1 : 0;
            float confidence = Math.max(normalProb, distressProb);
            long totalTime = System.currentTimeMillis() - startTime;

            Log.d(TAG, String.format(Locale.getDefault(), 
                "Classification: %s (%.1f%% confidence)", 
                predictedClass == 1 ? "DISTRESS" : "NORMAL", confidence * 100));

            return new ClassificationResult(normalProb, distressProb, predictedClass, confidence, totalTime);

        } catch (Exception e) {
            Log.e(TAG, "Error during classification: " + e.getMessage());
            return new ClassificationResult(0.5f, 0.5f, 0, 0.5f, -1);
        }
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
        if (gpuDelegate != null) {
            gpuDelegate.close();
            gpuDelegate = null;
        }
        isInitialized = false;
    }
}
