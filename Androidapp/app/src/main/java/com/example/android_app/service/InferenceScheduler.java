package com.example.android_app.service;

public class InferenceScheduler {

    private static final long FAST = 200;    // ms
    private static final long SLOW = 1500;   // ms

    private long delay = FAST;

    public void adapt(float score) {
        if (score < 0.2f) {
            delay = SLOW;
        } else {
            delay = FAST;
        }
    }

    public long delay() {
        return delay;
    }
}
