package com.example.yandexgeofence2;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.MapObject;

public class MyZones {
    private Point center;
    private boolean isInside;
    private float radius;
    private MapObject mapObject;
    private String name="зона";
    public MyZones (Point center, float radius, MapObject mapObject, boolean isInside){
        this.center = center;
        this.radius = radius;
        this.mapObject=mapObject;
        this.isInside=isInside;
    }

    public Point getCenter(){
        return center;
    }

    public float getRadius(){
        return radius;
    }
    public MapObject getMapObject(){
        return mapObject;
    }
    public boolean getIsInside(){
        return isInside;
    }
    public void setIsInside(boolean isInside){
        this.isInside=isInside;
    }
    public void setName(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
}
