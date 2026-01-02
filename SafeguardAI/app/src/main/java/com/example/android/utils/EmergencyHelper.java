package com.example.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.android.models.EmergencyContact;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper for managing emergency contacts with Encrypted Storage
 */
public class EmergencyHelper {

    private static final String TAG = "EmergencyHelper";
    private static final String PREFS_NAME = "secure_emergency_contacts";
    private static final String KEY_CONTACTS = "contacts_list";

    private static SharedPreferences getPrefs(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "Encryption initialization failed, falling back: " + e.getMessage());
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    /**
     * Save emergency contacts
     */
    public static void saveEmergencyContacts(Context context, List<EmergencyContact> contacts) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(contacts);
        editor.putString(KEY_CONTACTS, json);
        editor.apply();
    }

    /**
     * Get emergency contacts
     */
    public static List<EmergencyContact> getEmergencyContacts(Context context) {
        String json = getPrefs(context).getString(KEY_CONTACTS, null);
        if (json == null) return new ArrayList<>();
        
        Gson gson = new Gson();
        Type type = new TypeToken<List<EmergencyContact>>(){}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Add emergency contact
     */
    public static void addEmergencyContact(Context context, EmergencyContact contact) {
        List<EmergencyContact> contacts = getEmergencyContacts(context);
        contacts.add(contact);
        saveEmergencyContacts(context, contacts);
    }

    /**
     * Check if emergency contacts exist
     */
    public static boolean hasEmergencyContacts(Context context) {
        return !getEmergencyContacts(context).isEmpty();
    }
}
