package com.bounce.location.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class LocationInfo {


    @ColumnInfo(name = "lat")
    private double latitude;

    @ColumnInfo(name = "long")
    private double longitude;

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "accuracy")
    private float accuracy;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


}
