package com.example.android_app.ml;

public class InferenceStats {

    private int count = 0;
    private double mean = 0;
    private double m2 = 0;

    public void update(double x) {
        count++;
        double delta = x - mean;
        mean += delta / count;
        m2 += delta * (x - mean);
    }

    public double mean() {
        return mean;
    }

    public double variance() {
        return count > 1 ? m2 / (count - 1) : 0;
    }
}
