package com.bounce.location.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {LocationInfo.class}, version = 1, exportSchema = false)
public abstract class LocationDatabase extends RoomDatabase {

    public abstract DaoLocation daoLocation();
}