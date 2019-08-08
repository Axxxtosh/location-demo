package com.bounce.location.room;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface DaoLocation {

    @Insert
    Long insertLocation(LocationInfo location);
}
