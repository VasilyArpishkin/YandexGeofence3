package com.example.yandexgeofence2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements InputListener {
    private final String API_KEY = "8fe19095-8322-4d19-b9cc-ef614df4a306";
    private static final String CHANNEL_ID = "my_id";
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private PlacemarkMapObject userMarker;
    private MapObjectCollection mapObjectCollection;
    private Point circleCenter;
    private RelativeLayout relativeLayout;
    private final float DEFAULT_RADIUS=300;
    private Button  clickZone;
    private boolean isButtonClicked=false, isRaletiveLayoutVisible=false;
    private int k1=0,k2=0;
    private List<String> Names_of_zones= new ArrayList<>();
    private ListView listView;
    private ImageButton imageButton;
    private EditText editText;
    private List<MyZones> zones = new ArrayList<>();
    private Intent intent;
    private static final int PERMISSION_REQUEST_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitInitializer.init(this, API_KEY);
        setContentView(R.layout.activity_main);
        intent = new Intent(this, BackgroundService.class);
        clickZone = findViewById(R.id.click);
        mapView = findViewById(R.id.mapview);
        listView=findViewById(R.id.lv);
        editText = findViewById(R.id.et);
        relativeLayout = findViewById(R.id.rl);
        imageButton = findViewById(R.id.menu);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Names_of_zones);
        listView.setAdapter(adapter);
        checkAndRequestPermissions();
    }
    private void checkAndRequestPermissions() {
        boolean needsLocationPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;

        boolean needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;

        List<String> permissionsToRequest = new ArrayList<>();
        if (needsLocationPermission) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (needsNotificationPermission) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            initializeApp();
        }
    }


    private void initializeApp() {
        mapObjectCollection = mapView.getMap().getMapObjects().addCollection();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Names_of_zones);
        listView.setAdapter(adapter);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    updateUserLocation(location.getLatitude(), location.getLongitude());
                }
            }
        };

        mapView.getMap().addInputListener(this);
        startLocationUpdates();
        createNotificationChannel();
        setupClickListeners();
    }
    private void setupClickListeners(){
        clickZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isButtonClicked && k2%2==0) {
                    Toast.makeText(getApplicationContext(), "укажите свою зону", Toast.LENGTH_SHORT).show();
                    isButtonClicked=true;
                }
                else if(!isButtonClicked && k2%2==1){
                    Names_of_zones.add(editText.getText().toString());
                    editText.setVisibility(View.GONE);
                    k2++;
                    zones.get(zones.size()-1).setName(editText.getText().toString());
                    clickZone.setText("добавить геозону");
                    editText.setText("");
                }
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRaletiveLayoutVisible) {
                    relativeLayout.setVisibility(View.VISIBLE);
                    isRaletiveLayoutVisible=true;
                }
                else {
                    relativeLayout.setVisibility(View.GONE);
                    isRaletiveLayoutVisible=false;
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i=0;i<zones.size();i++){
                    if(position==i){
                        mapView.getMap().move(
                                new CameraPosition(zones.get(i).getCenter(), 15.0f, 0.0f, 0.0f),
                                new com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 1),
                                null
                        );
                    }
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteDialog(position);
                return false;
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
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
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
        if (userMarker == null) {
            userMarker = mapView.getMap().getMapObjects().addPlacemark(userLocation);
            userMarker.setOpacity(1.0f);
            userMarker.setIcon(ImageProvider.fromResource(this, R.drawable.cursor)); // Укажите свою иконку
            Log.d("LocationUpdate", "MarkerCreated");
        } else {
            userMarker.setGeometry(userLocation);
            Log.d("LocationUpdate","MarkerChanged");
        }
        if(zones!=null){
            for(MyZones zone: zones){
                if(calculateDistance(zone.getCenter().getLatitude(), latitude, zone.getCenter().getLongitude(), longitude)<=(double) zone.getRadius()){
                    Log.d("Notification", "Trying to send notification...");
                    if(!zone.getIsInside()){
                        sendNotification(zone);
                        zone.setIsInside(true);
                    }
                }
                else{
                    zone.setIsInside(false);
                }
            }
        }
        if(k1==0){
            mapView.getMap().move(
                    new CameraPosition(userLocation, 15.0f, 0.0f, 0.0f),
                    new com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 1),
                    null
            );
            k1++;
        }
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


    public void sendNotification(MyZones zone){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Вы вошли в "+zone.getName())
                .setContentText("!!!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.cursor)
                .setVibrate(new long[]{0, 500, 200, 500});
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }


    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "my channel";
            String description = "in/out zone notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void deleteDialog(final int position){
        new AlertDialog.Builder(this)
                .setTitle("Удаление зоны")
                .setMessage("Вы уверены,что хотите удалить эту зону?")
                .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeZone(position);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void removeZone(int position){
        if (position>=0 && position<zones.size() && position<Names_of_zones.size()) {
            MapObject circle = zones.get(position).getMapObject();
            mapObjectCollection.remove(circle);
            zones.remove(position);
            ZoneStorage.getZones().remove(position);
            Names_of_zones.remove(position);
            ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
            Toast.makeText(this, "зона удалена", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Ошибка, некоректный индекс зоны", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean locationGranted = true;
            boolean notificationsGranted = true;

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    locationGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                } else if (permissions[i].equals(Manifest.permission.POST_NOTIFICATIONS)) {
                    notificationsGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }

            if (locationGranted) {
                initializeApp();

                if (!notificationsGranted) {
                    Toast.makeText(this,
                            "Уведомления отключены - вы не будете получать оповещения о входе в зону",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this,
                        "Приложению требуется доступ к геолокации для работы",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void onMapTap(@NonNull Map map, @NonNull Point point) {
        if(isButtonClicked && k2%2==0){
            circleCenter = point;
            MapObject circle = mapObjectCollection.addCircle(new Circle(circleCenter, DEFAULT_RADIUS));
            zones.add(new MyZones(point, DEFAULT_RADIUS, circle, false));
            ZoneStorage.setZones(zones);
            editText.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "введите название зоны", Toast.LENGTH_SHORT).show();
            isButtonClicked=false;
            k2++;
            clickZone.setText("добавить название");
            editText.setVisibility(View.VISIBLE);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(intent);
        startLocationUpdates();
    }
}