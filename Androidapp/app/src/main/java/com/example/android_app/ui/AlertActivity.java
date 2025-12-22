package com.example.android_app.ui;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_app.R;
import com.example.android_app.sos.EmergencyActions;
import com.example.android_app.sos.LocationHelper;

public class AlertActivity extends AppCompatActivity {

    private CountDownTimer timer;
    private boolean cancelled = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_alert);

        TextView txt = findViewById(R.id.txtCountdown);
        Button btnCancel = findViewById(R.id.btnCancel);

        SharedPreferences prefs =
                getSharedPreferences("SAFEGUARD", MODE_PRIVATE);

        String phone = prefs.getString("EMERGENCY_PHONE", "");

        timer = new CountDownTimer(10_000, 1_000) {

            @Override
            public void onTick(long ms) {
                txt.setText("Sending alert in " + (ms / 1000) + "s");
            }

            @Override
            public void onFinish() {
                if (cancelled) return;

                LocationHelper.get(
                        AlertActivity.this,
                        loc -> sendSOS(phone, loc),
                        () -> {
                            Toast.makeText(
                                    AlertActivity.this,
                                    "Unable to get location. Sending SOS without GPS.",
                                    Toast.LENGTH_LONG
                            ).show();
                            sendSOS(phone, null);
                        }
                );

                finish();
            }
        }.start();

        btnCancel.setOnClickListener(v -> {
            cancelled = true;
            timer.cancel();
            finish();
        });
    }

    private void sendSOS(String phone, Location loc) {

        String msg;

        if (loc != null) {
            String map =
                    "https://maps.google.com/?q=" +
                            loc.getLatitude() + "," +
                            loc.getLongitude();

            msg =
                    "EMERGENCY! I may be in danger.\nLocation:\n" + map;
        } else {
            msg =
                    "EMERGENCY! I may be in danger.\nLocation unavailable.";
        }

        EmergencyActions.sms(this, phone, msg);
        EmergencyActions.call(this, phone);
    }
}
