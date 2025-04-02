package com.example.yandexgeofence2;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.security.Provider;
import java.util.List;

public class CollapseService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private List<MyZones> zones;
    private static final String CHANNEL_ID = "my_id";
    @Override
    public void onCreate(){
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult){
                for (android.location.Location location : locationResult.getLocations()){

                }
            }
        };

        
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        // Создаем уведомление для ForegroundService
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Отслеживание местоположения")
                .setContentText("тслеживание местоположеняи в фоновом режиме")
                .build();
        startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // 5 секунд
        locationRequest.setFastestInterval(5000); // 5 секунд

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
    private void checkZones(double latitude, double longitude){

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
