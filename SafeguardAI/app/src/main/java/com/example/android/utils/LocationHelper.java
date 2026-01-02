package com.example.android.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

/**
 * Helper for location services
 */
public class LocationHelper {

    /**
     * Get last known location
     */
    public static void getLastLocation(Context context, LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationReceived(null);
            return;
        }

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> callback.onLocationReceived(location))
                .addOnFailureListener(e -> callback.onLocationReceived(null));
    }

    /**
     * Get address from coordinates
     */
    public static String getAddressFromLocation(Context context, double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }

                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return latitude + ", " + longitude;
    }

    /**
     * Get Google Maps URL
     */
    public static String getGoogleMapsUrl(double latitude, double longitude) {
        return "https://maps.google.com/?q=" + latitude + "," + longitude;
    }

    /**
     * Calculate distance between two locations (in meters)
     */
    public static float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    public interface LocationCallback {
        void onLocationReceived(Location location);
    }
}
