package com.example.android_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_app.service.AudioDetectionService;
import com.example.android_app.ui.SetupActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs =
                getSharedPreferences("SAFEGUARD", MODE_PRIVATE);

        if (!prefs.getBoolean("SETUP_DONE", false)) {
            startActivity(
                    new Intent(this, SetupActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        startDetectionService();
    }

    private void startDetectionService() {

        Intent intent =
                new Intent(this, AudioDetectionService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
}
