package com.example.android.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Threat detection event model
 */
public class ThreatEvent {

    public long timestamp;
    public float confidence;
    public float distressProbability;
    public double latitude;
    public double longitude;

    public ThreatEvent() {
    }

    public ThreatEvent(long timestamp, float confidence, float distressProbability,
                       double latitude, double longitude) {
        this.timestamp = timestamp;
        this.confidence = confidence;
        this.distressProbability = distressProbability;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}

