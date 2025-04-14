package com.example.yandexgeofence2;


import com.yandex.mapkit.geometry.Circle;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;

public class ZoneConverter {
    private final int DEFAULT_RADIUS=300;
    MapObjectCollection mapObjectCollection;
    public static ZoneEntity toEntity(MyZones zone){
        Point center = zone.getCenter();
        return new ZoneEntity(
                center.getLatitude(),
                center.getLongitude(),
                zone.getRadius(),
                zone.getIsInside(),
                zone.getName()
        );
    }
    public MyZones toMyZones(ZoneEntity entity){
        Point center = new Point(entity.getCenterLatitude(), entity.getCenterLongitude());
        MapObject circle = mapObjectCollection.addCircle(new Circle(center, DEFAULT_RADIUS));
        return new MyZones(
                center,
                entity.getRadius(),
                circle,
                entity.getIsInside());
    }
}
