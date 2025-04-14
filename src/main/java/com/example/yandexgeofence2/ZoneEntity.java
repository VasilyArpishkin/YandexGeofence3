package com.example.yandexgeofence2;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "zones")
public class ZoneEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private double centerLatitude;
    private double centerLongitude;
    private float radius;
    private boolean isInside;
    private String name;
    public ZoneEntity(double centerLatitude, double centerLongitude, float radius, boolean isInside, String name){
        this.centerLatitude=centerLatitude;
        this.centerLongitude=centerLongitude;
        this.radius=radius;
        this.isInside=isInside;
        this.name=name;
    }
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id=id;
    }

    public float getRadius() {
        return radius;
    }
    public void setRadius(float radius){
    this.radius=radius;
    }
    public double getCenterLatitude(){
        return centerLatitude;
    }
    public void setCenterLatitude(double centerLatitude){
        this.centerLatitude=centerLatitude;
    }
    public double getCenterLongitude(){
        return centerLongitude;
    }
    public void setCenterLongitude(double centerLatitude){
        this.centerLatitude=centerLatitude;
    }
    public boolean getIsInside(){
        return isInside;
    }
    public void setIsInside(boolean isInside){
        this.isInside=isInside;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
}
