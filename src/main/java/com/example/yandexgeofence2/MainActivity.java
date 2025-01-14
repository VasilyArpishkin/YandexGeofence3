package com.example.yandexgeofence2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Circle;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;
import android.widget.ArrayAdapter;

public class MainActivity extends AppCompatActivity implements InputListener {
    private final String API_KEY = "8fe19095-8322-4d19-b9cc-ef614df4a306";
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private PlacemarkMapObject userMarker;
    // Маркер для отображения местоположения пользователя
    private MapObjectCollection mapObjectCollection;
    private Point circleCenter;
    private final float DEFAULT_RADIUS=100;
    private Button  clickZone;
    private boolean isButtonClicked=false;
    private int k=0;
    private int pos=0;
    private String[] Names_of_zones={};
    private ListView listView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey(API_KEY);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_main);
        clickZone = findViewById(R.id.click);
        mapView = findViewById(R.id.mapview);
        listView=findViewById(R.id.lv);
        textView=findViewById(R.id.tv);
        mapObjectCollection = mapView.getMap().getMapObjects().addCollection();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Names_of_zones);
        listView.setAdapter(adapter);
        // Инициализация LocationCallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    //float accuracy = location.getAccuracy();
                    /*if(accuracy<50){
                        updateUserLocation(latitude, longitude); // Обновляем позицию маркера
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "неточное местоположение", Toast.LENGTH_SHORT).show();
                    }*/
                    updateUserLocation(latitude, longitude); // Обновляем позицию маркера
                }
            }
        };
        mapView.getMap().addInputListener(this);
        // Запуск периодического получения местоположения
        startLocationUpdates();
        clickZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isButtonClicked=true;
                Toast.makeText(getApplicationContext(), "укажите свою зону", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        getCurrentLocationOnce();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // 5 секунд
        locationRequest.setFastestInterval(5000); // 5 секунд

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }


    private void getCurrentLocationOnce(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
               && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location!=null){
                    double latitude=location.getLatitude();
                    double longitude=location.getLongitude();
                    updateUserLocation(latitude, longitude);
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "ошибка при получении местоположения", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateUserLocation(double latitude, double longitude) {
        Point userLocation = new Point(latitude, longitude);
        Log.d("LocationUpdate","Update user location to"+latitude+","+longitude);
        // Если маркер еще не создан, создаем его
        if (userMarker == null) {
            userMarker = mapView.getMap().getMapObjects().addPlacemark(userLocation);
            userMarker.setOpacity(1.0f); // Прозрачность маркера
            userMarker.setIcon(ImageProvider.fromResource(this, R.drawable.cursor)); // Укажите свою иконку
            Log.d("LocationUpdate", "MarkerCreated");
        } else {
            // Если маркер уже существует, просто обновляем его позицию
            userMarker.setGeometry(userLocation);
            Log.d("LocationUpdate","MarkerChanged");
        }
        // Перемещаем камеру на новую позицию
        if(k==0){
         mapView.getMap().move(
                new CameraPosition(userLocation, 15.0f, 0.0f, 0.0f),
                new com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 1),
                null
        );
        k++;
        }
    }


    private void drawCircle(Point center, float radius) {
        mapObjectCollection.addCircle(new Circle(center, radius));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) { // requestCode должен совпадать с тем, что ты использовал в запросе разрешения
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение дано, запрашиваем текущее местоположение
                getCurrentLocationOnce();
                // Запускаем периодические обновления
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Разрешение на доступ к местоположению отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onMapTap(@NonNull Map map, @NonNull Point point) {
        if(isButtonClicked){
            circleCenter = point;
            drawCircle(circleCenter, DEFAULT_RADIUS);
            isButtonClicked=false;
        }
    }

    @Override
    public void onMapLongTap(@NonNull Map map, @NonNull Point point) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Остановка обновлений местоположения при паузе активности
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Возобновление обновлений местоположения при возобновлении активности
        startLocationUpdates();
    }
}