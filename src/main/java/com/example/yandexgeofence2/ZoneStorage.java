package com.example.yandexgeofence2;

import java.util.ArrayList;
import java.util.List;

public class ZoneStorage {
    private static List<MyZones> zones = new ArrayList<>();
    public static List<MyZones> getZones(){
        return zones;
    }
    public static void setZones(List<MyZones> zone){
        zones = new ArrayList<>(zone);
    }
}
