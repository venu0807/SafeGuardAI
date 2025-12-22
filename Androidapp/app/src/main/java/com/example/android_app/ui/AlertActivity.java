package com.example.android_app.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.VibrationEffect;
import android.os.Vibrator;



import com.example.android_app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class AlertActivity extends AppCompatActivity {

    private static final int LOCATION_REQ = 101;
    SharedPreferences prefs =
            getSharedPreferences("SAFEGUARD", MODE_PRIVATE);

    private String getEmergencyPhone() {
        return getSharedPreferences("SAFEGUARD", MODE_PRIVATE)
                .getString("EMERGENCY_PHONE", "9999999999");
    }



    private FusedLocationProviderClient locationClient;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        alertUser();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        TextView txtCountdown = findViewById(R.id.txtCountdown);
        Button btnCancel = findViewById(R.id.btnCancel);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                txtCountdown.setText("SOS in " + (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                checkLocationPermissionAndTrigger();
            }
        }.start();

        btnCancel.setOnClickListener(v -> {
            timer.cancel();
            finish();
        });
    }

    // ---------------- PERMISSION FLOW ----------------

    private void checkLocationPermissionAndTrigger() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQ
            );
        } else {
            triggerSOS();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQ) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                triggerSOS();
            } else {
                sendSOS(null); // fallback without location
            }
        }
    }

    // ---------------- SOS LOGIC ----------------

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void triggerSOS() {
        locationClient.getLastLocation()
                .addOnSuccessListener(this::sendSOS)
                .addOnFailureListener(e -> sendSOS(null));
    }

    private void sendSOS(Location location) {

        String phone = getEmergencyPhone();

        StringBuilder msg = new StringBuilder();
        msg.append("EMERGENCY ALERT!\nI may be in danger.\n");

        if (location != null) {
            msg.append("Location:\n")
                    .append("https://maps.google.com/?q=")
                    .append(location.getLatitude())
                    .append(",")
                    .append(location.getLongitude());
        } else {
            msg.append("Location unavailable.");
        }

        // SMS
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + phone));
        smsIntent.putExtra("sms_body", msg.toString());
        startActivity(smsIntent);

        // Dial
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + phone));
        startActivity(dialIntent);

        finish();
    }

//    Vibration
private void alertUser() {

    // Vibration
    Vibrator vib =
            (Vibrator) getSystemService(VIBRATOR_SERVICE);
    if (vib != null && vib.hasVibrator()) {
        vib.vibrate(
                VibrationEffect.createWaveform(
                        new long[]{0, 800, 400, 800},
                        -1
                )
        );
    }

    // Sound
    try {
        Ringtone tone =
                RingtoneManager.getRingtone(
                        this,
                        RingtoneManager.getDefaultUri(
                                RingtoneManager.TYPE_ALARM));
        tone.play();
    } catch (Exception ignored) {}
}



}
