package ru.mashinis.mlkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationTracker {

    private Context mContext;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private LocationListener mListener;

    public interface LocationListener {
        void onLocationChanged(double latitude, double longitude, long time, double altitude);
    }

    public LocationTracker(Context context, LocationListener listener) {
        mContext = context;
        mListener = listener;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        createLocationRequest();
        createLocationCallback();
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
                .addOnSuccessListener(locationRequest -> {
                    // Location updates started successfully
                })
                .addOnFailureListener(e -> {
                    // Failed to start location updates
                });
    }

    public void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest.Builder(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .build();
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        mListener.onLocationChanged(
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getTime(),
                                location.getAltitude()
                        );
                    }
                }
            }
        };
    }
}
