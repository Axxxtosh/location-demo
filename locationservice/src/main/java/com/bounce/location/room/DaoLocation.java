package com.bounce.location.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface DaoLocation {

    @Insert
    Long insertLocation(LocationInfo location);

    @Query("SELECT * FROM locationinfo")
    public LocationInfo[] loadCache();
}
