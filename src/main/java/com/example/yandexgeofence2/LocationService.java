package com.example.yandexgeofence2;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
public class LocationService extends Service implements Parcelable {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private static final String CHANNEL_ID="my_id";
    @Override
    public void onCreate(){
        super.onCreate();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    // Здесь можно обновить местоположение и проверить зоны
                    //checkZones(latitude, longitude);
                }
            }
        };
    }
    private void startLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
    private double calculateDistance(double lat1, double lat2, double lon1, double lon2){
        double lat1R=Math.toRadians(lat1);
        double lat2R=Math.toRadians(lat2);
        double lon1R=Math.toRadians(lon1);
        double lon2R=Math.toRadians(lon2);
        double x= (lon2R-lon1R)*Math.cos((lat1R+lat2R)/2);
        double y = lat2R-lat1R;
        double distance = Math.sqrt(x*x+y*y)*6371000;
        return distance;
    }
    private void findUserLocation(double latitude, double longitude){

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        // Создаем уведомление для ForegroundService
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Служба отслеживания местоположения")
                .setContentText("Идет отслеживание вашего местоположения")
                .build();
        startForeground(1, notification);
        return START_STICKY;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

    }
}
