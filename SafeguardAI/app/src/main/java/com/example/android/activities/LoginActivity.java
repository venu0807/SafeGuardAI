package com.example.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.R;
import com.example.android.utils.SharedPrefsHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPrefsHelper prefsHelper;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null && account.getIdToken() != null && !FirebaseApp.getApps(this).isEmpty()) {
                        firebaseAuthWithGoogle(account.getIdToken());
                    } else {
                        proceedToNextStep();
                    }
                } catch (ApiException e) {
                    Log.w("Login", "Google sign in failed code: " + e.getStatusCode());
                    proceedToNextStep();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefsHelper = new SharedPrefsHelper(this);
        
        try {
            if (!FirebaseApp.getApps(this).isEmpty()) {
                mAuth = FirebaseAuth.getInstance();
            }
        } catch (Exception e) {
            Log.w("Login", "Firebase Auth initialization skipped.");
        }

        GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail();

        try {
            int stringId = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
            if (stringId != 0) {
                gsoBuilder.requestIdToken(getString(stringId));
            }
        } catch (Exception ignored) {}

        mGoogleSignInClient = GoogleSignIn.getClient(this, gsoBuilder.build());

        findViewById(R.id.btn_google_login).setOnClickListener(v -> signIn());
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (mAuth == null) {
            proceedToNextStep();
            return;
        }
        
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> proceedToNextStep());
    }

    private void proceedToNextStep() {
        if (mAuth != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                prefsHelper.setUserId(user.getUid());
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    prefsHelper.setUserName(user.getDisplayName());
                }
            }
        }

        if (prefsHelper.getUserId().isEmpty()) {
            prefsHelper.setUserId("dev_user_" + System.currentTimeMillis());
        }

        if (prefsHelper.getUserName().isEmpty()) {
            startActivity(new Intent(this, OnboardingActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}
