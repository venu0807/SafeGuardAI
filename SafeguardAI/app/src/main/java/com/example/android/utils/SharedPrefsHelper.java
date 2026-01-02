package com.example.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.android.models.ThreatEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper for Secure SharedPreferences using EncryptedSharedPreferences
 */
public class SharedPrefsHelper {

    private static final String TAG = "SharedPrefsHelper";
    private static final String PREFS_NAME = "safeguard_secure_prefs";

    // Keys
    private static final String KEY_PROTECTION_ENABLED = "protection_enabled";
    private static final String KEY_AUTO_CALL_ENABLED = "auto_call_enabled";
    private static final String KEY_DETECTION_COUNT = "detection_count";
    private static final String KEY_THREAT_COUNT = "threat_count";
    private static final String KEY_LAST_DETECTION_TIME = "last_detection_time";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_SENSITIVITY = "detection_sensitivity";
    private static final String KEY_HAZARD_LOCATIONS = "hazard_locations";

    private SharedPreferences prefs;
    private final Gson gson;

    public SharedPrefsHelper(Context context) {
        this.gson = new Gson();
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            prefs = EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences: " + e.getMessage());
            // Fallback to standard SharedPreferences if encryption fails (not recommended for prod)
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    // Protection state
    public void setProtectionEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PROTECTION_ENABLED, enabled).apply();
    }

    public boolean isProtectionEnabled() {
        return prefs.getBoolean(KEY_PROTECTION_ENABLED, false);
    }

    // Auto call setting
    public void setAutoCallEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_CALL_ENABLED, enabled).apply();
    }

    public boolean isAutoCallEnabled() {
        return prefs.getBoolean(KEY_AUTO_CALL_ENABLED, false);
    }

    // Detection sensitivity
    public void setSensitivity(float sensitivity) {
        prefs.edit().putFloat(KEY_SENSITIVITY, sensitivity).apply();
    }

    public float getSensitivity() {
        return prefs.getFloat(KEY_SENSITIVITY, 0.7f);
    }

    // Hazard Locations Tracking
    public void addHazardLocation(ThreatEvent event) {
        List<ThreatEvent> locations = getHazardLocations();
        locations.add(event);
        String json = gson.toJson(locations);
        prefs.edit().putString(KEY_HAZARD_LOCATIONS, json).apply();
    }

    public List<ThreatEvent> getHazardLocations() {
        String json = prefs.getString(KEY_HAZARD_LOCATIONS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<ThreatEvent>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Statistics
    public void incrementDetectionCount() {
        int count = getDetectionCount();
        prefs.edit().putInt(KEY_DETECTION_COUNT, count + 1).apply();
    }

    public int getDetectionCount() {
        return prefs.getInt(KEY_DETECTION_COUNT, 0);
    }

    public void incrementThreatCount() {
        int count = getThreatCount();
        prefs.edit().putInt(KEY_THREAT_COUNT, count + 1).apply();
    }

    public int getThreatCount() {
        return prefs.getInt(KEY_THREAT_COUNT, 0);
    }

    public void setLastDetectionTime(long time) {
        prefs.edit().putLong(KEY_LAST_DETECTION_TIME, time).apply();
    }

    public long getLastDetectionTime() {
        return prefs.getLong(KEY_LAST_DETECTION_TIME, 0);
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public void setUserId(String id) {
        prefs.edit().putString(KEY_USER_ID, id).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
