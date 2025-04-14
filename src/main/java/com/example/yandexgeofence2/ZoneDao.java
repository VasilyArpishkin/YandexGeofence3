package com.example.yandexgeofence2;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ZoneDao {
    @Insert
    void insert(ZoneEntity zone);

    @Update
    void update(ZoneEntity zone);
    @Delete
    void delete(ZoneEntity zone);
    @Query("SELECT * FROM zones")
    List<ZoneEntity> getAllZones();
    @Query("SELECT * FROM zones WHERE id = :id")
    ZoneEntity getZoneByID(int id);
}
