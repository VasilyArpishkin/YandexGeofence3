package com.example.yandexgeofence2;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ZoneEntity.class}, version = 1)
public abstract class AppDataBase extends RoomDatabase {
    public abstract ZoneDao zoneDao();
}
