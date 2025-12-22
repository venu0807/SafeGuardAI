package com.example.android_app.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.android_app.MainActivity;
import com.example.android_app.R;

public class SetupActivity extends AppCompatActivity {

    private static final int REQ_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        EditText edtPhone = findViewById(R.id.edtPhone);
        Button btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQ_AUDIO
                );
                return;
            }

            SharedPreferences prefs =
                    getSharedPreferences("SAFEGUARD", MODE_PRIVATE);

            prefs.edit()
                    .putString("EMERGENCY_PHONE",
                            edtPhone.getText().toString())
                    .putBoolean("SETUP_DONE", true)
                    .apply();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
