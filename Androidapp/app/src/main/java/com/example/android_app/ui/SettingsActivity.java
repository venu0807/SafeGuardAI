package com.example.android_app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_app.R;
import com.example.android_app.service.AudioDetectionService;

public class SettingsActivity extends AppCompatActivity {

    private EditText edtPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        edtPhone = findViewById(R.id.edtPhone);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDisable = findViewById(R.id.btnDisable);

        SharedPreferences prefs =
                getSharedPreferences("SAFEGUARD", MODE_PRIVATE);

        edtPhone.setText(
                prefs.getString("EMERGENCY_PHONE", "")
        );

        btnSave.setOnClickListener(v -> {
            prefs.edit()
                    .putString("EMERGENCY_PHONE",
                            edtPhone.getText().toString())
                    .apply();
            Toast.makeText(this,
                    "Contact updated",
                    Toast.LENGTH_SHORT).show();
        });

        btnDisable.setOnClickListener(v -> {
            stopService(
                    new Intent(this,
                            AudioDetectionService.class));

            prefs.edit()
                    .putBoolean("SETUP_DONE", false)
                    .apply();

            Toast.makeText(this,
                    "Protection disabled",
                    Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
