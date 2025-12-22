package com.example.android_app.sos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {

    public interface Success {
        void onLocation(Location location);
    }

    public interface Failure {
        void onFailure();
    }

    @SuppressLint("MissingPermission")
    public static void get(
            Context context,
            Success success,
            Failure failure
    ) {
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(context);

        client.getLastLocation()
                .addOnSuccessListener(loc -> {
                    if (loc != null) {
                        success.onLocation(loc);
                    } else {
                        failure.onFailure();
                    }
                })
                .addOnFailureListener(e -> failure.onFailure());
    }
}
