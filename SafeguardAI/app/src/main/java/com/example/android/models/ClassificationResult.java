package com.example.android.models;

import java.util.Locale;

/**
 * ML output wrapper for classification results
 */
public class ClassificationResult {
    public final float normalProbability;
    public final float distressProbability;
    public final int predictedClass;
    public final float confidence;
    public final long inferenceTimeMs;

    public ClassificationResult(float normalProb, float distressProb, int predictedClass, float confidence, long inferenceTime) {
        this.normalProbability = normalProb;
        this.distressProbability = distressProb;
        this.predictedClass = predictedClass;
        this.confidence = confidence;
        this.inferenceTimeMs = inferenceTime;
    }

    public boolean isDistress() {
        return predictedClass == 1;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "Result: %s (%.2f)",
                predictedClass == 1 ? "DISTRESS" : "NORMAL", confidence);
    }
}
