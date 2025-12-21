package com.example.safeguard.ml;

import java.util.ArrayDeque;

public class TemporalGate {

    private ArrayDeque<Float> buffer = new ArrayDeque<>();
    private int window = 5;
    private float threshold = 0.6f;

    public boolean update(float score) {
        if (buffer.size() == window) buffer.poll();
        buffer.add(score);

        if (buffer.size() < window) return false;

        float sum = 0f;
        for (float s : buffer) sum += s;
        return (sum / window) >= threshold;
    }
}
