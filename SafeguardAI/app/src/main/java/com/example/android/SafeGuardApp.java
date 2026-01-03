package com.example.android;

import android.app.Application;
import com.google.firebase.FirebaseApp;

/**
 * Main Application class for SafeGuard AI
 * Handles global initialization
 */
public class SafeGuardApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            // Log if initialization fails, but don't crash the app
        }
    }
}
