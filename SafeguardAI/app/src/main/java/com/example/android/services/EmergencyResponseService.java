package com.example.android.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.android.models.EmergencyContact;
import com.example.android.models.ThreatEvent;
import com.example.android.utils.EmergencyHelper;
import com.example.android.utils.LocationHelper;
import com.example.android.utils.NotificationHelper;
import com.example.android.utils.SharedPrefsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmergencyResponseService extends Service {

    private static final String TAG = "EmergencyResponseService";
    private static final String EMERGENCY_NUMBER = "112";
    private static final String CHANNEL_ID = "emergency_call_channel";

    private SharedPrefsHelper prefsHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference firebaseDb;

    @Override
    public void onCreate() {
        super.onCreate();
        prefsHelper = new SharedPrefsHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        try {
            if (!FirebaseApp.getApps(this).isEmpty()) {
                firebaseDb = FirebaseDatabase.getInstance().getReference();
            }
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed");
        }
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            float confidence = intent.getFloatExtra("confidence", 0.0f);
            float distressProb = intent.getFloatExtra("distress_prob", 0.0f);
            executeEmergencyProtocol(confidence, distressProb);
        }
        return START_NOT_STICKY;
    }

    private void executeEmergencyProtocol(float confidence, float distressProb) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> processIncidentWithLocation(location, confidence, distressProb))
                    .addOnFailureListener(e -> fusedLocationClient.getLastLocation().addOnSuccessListener(lastLoc -> processIncidentWithLocation(lastLoc, confidence, distressProb)));
        } else {
            processIncidentWithLocation(null, confidence, distressProb);
        }
    }

    private void processIncidentWithLocation(Location location, float confidence, float distressProb) {
        ThreatEvent event = new ThreatEvent(System.currentTimeMillis(), confidence, distressProb, 
                location != null ? location.getLatitude() : 0, location != null ? location.getLongitude() : 0);

        String address = location != null ? LocationHelper.getAddressFromLocation(this, location.getLatitude(), location.getLongitude()) : "Location unavailable";

        // Notify user locally
        NotificationHelper.showEmergencyNotification(this, "Emergency Protocol Active", "Sending alerts to your contacts...");

        sendEmergencySMS(event, address);
        logToFirebase(event);

        if (prefsHelper.isAutoCallEnabled()) {
            makeEmergencyCall();
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::stopSelf, 15000);
    }

    private void makeEmergencyCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) return;

        // To bypass Background Activity Start restrictions on Android 10+, 
        // we use a High-Priority Notification with a FullScreenIntent.
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + EMERGENCY_NUMBER));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, callIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .setContentTitle("Emergency Call")
                .setContentText("Calling emergency services...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(pendingIntent, true) // Key for background bypass
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(2002, notification);
        }

        // Trigger actual call attempt immediately
        try {
            startActivity(callIntent);
        } catch (Exception e) {
            Log.e(TAG, "Direct call failed, relying on notification");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Emergency Calls", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Used to initiate emergency calls from background");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void sendEmergencySMS(ThreatEvent event, String address) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return;
        List<EmergencyContact> contacts = EmergencyHelper.getEmergencyContacts(this);
        if (contacts.isEmpty()) return;

        String message = composeEmergencyMessage(event, address);
        SmsManager smsManager = SmsManager.getDefault();
        for (EmergencyContact contact : contacts) {
            try {
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(contact.getPhoneNumber(), null, parts, null, null);
                Log.d(TAG, "SMS sent to " + contact.getName());
            } catch (Exception e) { Log.e(TAG, "SMS failed for " + contact.getName()); }
        }
    }

    private String composeEmergencyMessage(ThreatEvent event, String address) {
        return "🚨 EMERGENCY ALERT 🚨\nSafeGuard AI detected a threat.\n" +
                "User: " + prefsHelper.getUserName() + "\n" +
                "Confidence: " + String.format(Locale.getDefault(), "%.0f%%", event.confidence * 100) + "\n" +
                "📍 Address: " + address + "\n" +
                "📍 Maps: https://maps.google.com/?q=" + event.latitude + "," + event.longitude + "\n" +
                "- SafeGuard AI";
    }

    private void logToFirebase(ThreatEvent event) {
        if (firebaseDb == null) return;
        try {
            String uid = prefsHelper.getUserId();
            if (uid.isEmpty()) uid = "anonymous";
            Map<String, Object> data = new HashMap<>();
            data.put("timestamp", event.timestamp);
            data.put("confidence", event.confidence);
            data.put("distress_prob", event.distressProbability);
            data.put("latitude", event.latitude);
            data.put("longitude", event.longitude);
            firebaseDb.child("threat_events").child(uid).push().setValue(data);
        } catch (Exception ignored) {}
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
}
