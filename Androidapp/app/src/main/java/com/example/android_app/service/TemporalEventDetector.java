package com.example.android_app.service;

import android.os.SystemClock;

public class TemporalEventDetector {

    // ===== TUNABLE PARAMETERS =====
    private static final float START_THRESHOLD = 0.8f;
    private static final float END_THRESHOLD   = 0.4f;

    private static final long MIN_START_MS = 2000;   // sustain to start
    private static final long MIN_END_MS   = 2000;   // sustain to end
    private static final long COOLDOWN_MS  = 15000;  // after end

    // ===== INTERNAL STATE =====
    private boolean inEvent = false;
    private long startCandidateTs = 0;
    private long endCandidateTs = 0;
    private long lastEventEndTs = 0;

    private float peakScore = 0f;

    // ===== EVENTS =====
    public enum EventType {
        NONE,
        DISTRESS_EVENT_START,
        DISTRESS_EVENT_END
    }

    public static class Event {
        public final EventType type;
        public final long timestamp;
        public final float confidence;

        public Event(EventType type, long timestamp, float confidence) {
            this.type = type;
            this.timestamp = timestamp;
            this.confidence = confidence;
        }
    }

    public Event update(float score) {

        long now = SystemClock.elapsedRealtime();

        // ---------- COOLDOWN ----------
        if (!inEvent && now - lastEventEndTs < COOLDOWN_MS) {
            return null;
        }

        // ---------- NOT IN EVENT ----------
        if (!inEvent) {

            if (score >= START_THRESHOLD) {
                if (startCandidateTs == 0) {
                    startCandidateTs = now;
                    peakScore = score;
                } else {
                    peakScore = Math.max(peakScore, score);
                    if (now - startCandidateTs >= MIN_START_MS) {
                        inEvent = true;
                        startCandidateTs = 0;
                        return new Event(
                                EventType.DISTRESS_EVENT_START,
                                now,
                                peakScore
                        );
                    }
                }
            } else {
                startCandidateTs = 0;
            }
            return null;
        }

        // ---------- IN EVENT ----------
        if (score <= END_THRESHOLD) {
            if (endCandidateTs == 0) {
                endCandidateTs = now;
            } else {
                if (now - endCandidateTs >= MIN_END_MS) {
                    inEvent = false;
                    endCandidateTs = 0;
                    lastEventEndTs = now;

                    return new Event(
                            EventType.DISTRESS_EVENT_END,
                            now,
                            peakScore
                    );
                }
            }
        } else {
            endCandidateTs = 0;
            peakScore = Math.max(peakScore, score);
        }

        return null;
    }
}
