package com.example.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.android.R;
import com.example.android.utils.SharedPrefsHelper;

import java.util.Locale;

/**
 * Settings Activity
 */
public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial autoCallSwitch;
    private Slider sensitivitySlider;
    private TextView userNameText;
    private TextView versionText;

    private SharedPrefsHelper prefsHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefsHelper = new SharedPrefsHelper(this);

        setupToolbar();
        initializeViews();
        loadSettings();
        setupListeners();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        autoCallSwitch = findViewById(R.id.switch_auto_call);
        sensitivitySlider = findViewById(R.id.slider_sensitivity);
        userNameText = findViewById(R.id.et_user_name);
        versionText = findViewById(R.id.text_version);

        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            if (versionText != null) versionText.setText("Version " + version);
        } catch (Exception e) {
            if (versionText != null) versionText.setText("Version 1.0");
        }
    }

    private void loadSettings() {
        if (autoCallSwitch != null) autoCallSwitch.setChecked(prefsHelper.isAutoCallEnabled());

        if (sensitivitySlider != null) {
            float savedValue = prefsHelper.getSensitivity();
            float roundedValue = Math.round(savedValue * 10.0f) / 10.0f;
            if (roundedValue < 0.5f) roundedValue = 0.5f;
            if (roundedValue > 0.9f) roundedValue = 0.9f;
            sensitivitySlider.setValue(roundedValue);
        }

        String userName = prefsHelper.getUserName();
        if (userNameText instanceof EditText) {
            ((EditText)userNameText).setText(userName != null ? userName : "");
        } else if (userNameText != null) {
            userNameText.setText(userName != null && !userName.isEmpty() ? userName : "Not set");
        }
    }

    private void setupListeners() {
        if (autoCallSwitch != null) {
            autoCallSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefsHelper.setAutoCallEnabled(isChecked);
                if (isChecked) {
                    showAutoCallWarning();
                }
                Toast.makeText(this, isChecked ? "Auto-call enabled" : "Auto-call disabled", Toast.LENGTH_SHORT).show();
            });
        }

        if (sensitivitySlider != null) {
            sensitivitySlider.addOnChangeListener((slider, value, fromUser) -> {
                prefsHelper.setSensitivity(value);
            });
        }

        View btnSave = findViewById(R.id.btn_save_settings);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                if (userNameText instanceof EditText) {
                    prefsHelper.setUserName(((EditText)userNameText).getText().toString().trim());
                }
                Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        View btnAbout = findViewById(R.id.btn_about);
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> showAboutDialog());
        }
    }

    private void showAutoCallWarning() {
        new AlertDialog.Builder(this)
                .setTitle("Auto-Call Enabled")
                .setMessage("Emergency services (112) will be automatically called when a threat is detected. Use this feature responsibly.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAboutDialog() {
        String aboutText = "SafeGuard AI\n\nMachine learning-powered safety application using CNN and MFCC for real-time distress detection.";
        new AlertDialog.Builder(this)
                .setTitle("About SafeGuard AI")
                .setMessage(aboutText)
                .setPositiveButton("OK", null)
                .show();
    }
}
