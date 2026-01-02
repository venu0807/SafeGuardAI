package com.example.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.android.R; // Corrected to match namespace
import com.example.android.services.ThreatDetectionService;
import com.example.android.services.EmergencyResponseService;
import com.example.android.utils.PermissionHelper;
import com.example.android.utils.SharedPrefsHelper;

import java.util.Locale;

/**
 * Main Activity - Dashboard
 */
public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    // UI Components
    private SwitchMaterial protectionSwitch;
    private TextView statusText;
    private TextView detectionCountText;
    private TextView threatCountText;
    private ImageView statusIcon;
    private Button btnEmergencyContacts;
    private Button btnSettings;
    private Button btnIncidentVault;
    private Button btnProfile;
    private FloatingActionButton fabPanicButton;

    // Service state
    private boolean isServiceRunning = false;

    // Broadcast receiver for threat detection
    private BroadcastReceiver threatReceiver;

    // Shared preferences
    private SharedPrefsHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefsHelper = new SharedPrefsHelper(this);

        initializeUI();
        setupListeners();
        checkPermissions();
        registerThreatReceiver();
        updateUI();
    }

    /**
     * Initialize UI components
     */
    private void initializeUI() {
        protectionSwitch = findViewById(R.id.protection_switch);
        statusText = findViewById(R.id.status_text);
        detectionCountText = findViewById(R.id.detection_count);
        threatCountText = findViewById(R.id.threat_count);
        statusIcon = findViewById(R.id.status_icon);
        btnEmergencyContacts = findViewById(R.id.btn_emergency_contacts);
        btnSettings = findViewById(R.id.btn_settings);
        btnIncidentVault = findViewById(R.id.btn_incident_vault);
        btnProfile = findViewById(R.id.btn_profile);
        fabPanicButton = findViewById(R.id.fab_panic_button);
    }

    /**
     * Setup click listeners
     */
    private void setupListeners() {
        // Protection toggle
        protectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startProtection();
            } else {
                stopProtection();
            }
        });

        // Profile button
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
            });
        }

        // Emergency contacts button
        btnEmergencyContacts.setOnClickListener(v -> {
            Intent intent = new Intent(this, EmergencyContactsActivity.class);
            startActivity(intent);
        });

        // Settings button
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        // Vault button
        btnIncidentVault.setOnClickListener(v -> {
            Intent intent = new Intent(this, IncidentHistoryActivity.class);
            startActivity(intent);
        });

        // Panic button (manual emergency trigger)
        fabPanicButton.setOnLongClickListener(v -> {
            triggerManualEmergency();
            return true;
        });

        fabPanicButton.setOnClickListener(v -> 
            Toast.makeText(this, "Hold for 3 seconds to trigger emergency", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Check and request permissions
     */
    private void checkPermissions() {
        if (!PermissionHelper.hasAllPermissions(this)) {
            PermissionHelper.requestPermissions(this, PERMISSION_REQUEST_CODE);
        } else {
            // Auto-start if previously enabled
            if (prefsHelper.isProtectionEnabled()) {
                startProtection();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();
                if (prefsHelper.isProtectionEnabled()) {
                    startProtection();
                }
            } else {
                Toast.makeText(this, "Permissions required for protection", Toast.LENGTH_LONG).show();
                protectionSwitch.setChecked(false);
            }
        }
    }

    /**
     * Start threat detection service
     */
    private void startProtection() {
        if (!PermissionHelper.hasAllPermissions(this)) {
            Toast.makeText(this, "Please grant all permissions", Toast.LENGTH_SHORT).show();
            protectionSwitch.setChecked(false);
            return;
        }

        Intent serviceIntent = new Intent(this, ThreatDetectionService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        isServiceRunning = true;
        prefsHelper.setProtectionEnabled(true);
        updateUI();

        Toast.makeText(this, "Protection activated", Toast.LENGTH_SHORT).show();
    }

    /**
     * Stop threat detection service
     */
    private void stopProtection() {
        Intent serviceIntent = new Intent(this, ThreatDetectionService.class);
        stopService(serviceIntent);

        isServiceRunning = false;
        prefsHelper.setProtectionEnabled(false);
        updateUI();

        Toast.makeText(this, "Protection deactivated", Toast.LENGTH_SHORT).show();
    }

    /**
     * Trigger manual emergency
     */
    private void triggerManualEmergency() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Emergency Alert")
                .setMessage("Are you sure you want to send an emergency alert to your contacts?")
                .setPositiveButton("YES, SEND ALERT", (dialog, which) -> {
                    Intent emergencyIntent = new Intent(this, EmergencyResponseService.class);
                    emergencyIntent.putExtra("confidence", 1.0f);
                    emergencyIntent.putExtra("distress_prob", 1.0f);
                    startService(emergencyIntent);

                    Toast.makeText(this, "Emergency alert sent!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Register broadcast receiver for threat detection
     */
    private void registerThreatReceiver() {
        threatReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("THREAT_DETECTED".equals(intent.getAction())) {
                    float confidence = intent.getFloatExtra("confidence", 0);
                    onThreatDetected(confidence);
                }
            }
        };

        IntentFilter filter = new IntentFilter("THREAT_DETECTED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(threatReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(threatReceiver, filter);
        }
    }

    /**
     * Handle threat detection event
     */
    private void onThreatDetected(float confidence) {
        runOnUiThread(() -> {
            statusIcon.setImageResource(R.drawable.ic_alert);
            statusText.setText("THREAT DETECTED!");
            statusText.setTextColor(getColor(android.R.color.holo_red_dark));

            updateUI();

            Toast.makeText(this,
                    String.format(Locale.getDefault(), "Threat detected! Confidence: %.0f%%", confidence * 100),
                    Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Update UI based on current state
     */
    private void updateUI() {
        protectionSwitch.setChecked(isServiceRunning);

        if (isServiceRunning) {
            statusText.setText("Protection Active");
            statusText.setTextColor(getColor(android.R.color.holo_green_dark));
            statusIcon.setImageResource(R.drawable.ic_shield_check);
        } else {
            statusText.setText("Protection Inactive");
            statusText.setTextColor(getColor(android.R.color.darker_gray));
            statusIcon.setImageResource(R.drawable.ic_shield_off);
        }

        detectionCountText.setText(String.valueOf(prefsHelper.getDetectionCount()));
        threatCountText.setText(String.valueOf(prefsHelper.getThreatCount()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        isServiceRunning = prefsHelper.isProtectionEnabled();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (threatReceiver != null) {
            unregisterReceiver(threatReceiver);
        }
    }
}
