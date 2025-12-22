package com.example.android_app.sos;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class EmergencyActions {

    // Safe call handler
    public static void call(Context ctx, String phone) {
        if (ActivityCompat.checkSelfPermission(
                ctx, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(ctx,
                    "Call permission not granted",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(Uri.parse("tel:" + phone));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }


    // SMS is allowed with SEND_SMS permission
    public static void sms(Context ctx, String phone, String message) {
        try {
            SmsManager.getDefault().sendTextMessage(
                    phone, null, message, null, null
            );
        } catch (SecurityException ignored) {}
    }
}
