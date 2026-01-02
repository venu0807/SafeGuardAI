package com.example.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.android.services.ThreatDetectionService;
import com.example.android.utils.SharedPrefsHelper;

/**
 * Receiver to auto-start service after device reboot
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed - checking if protection should start");

            SharedPrefsHelper prefsHelper = new SharedPrefsHelper(context);

            // Auto-start if protection was enabled before reboot
            if (prefsHelper.isProtectionEnabled()) {
                Intent serviceIntent = new Intent(context, ThreatDetectionService.class);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }

                Log.d(TAG, "ThreatDetectionService started after boot");
            }
        }
    }
}

