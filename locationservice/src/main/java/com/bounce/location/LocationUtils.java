/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bounce.location;


import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;

public class LocationUtils {

    public static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";
    public static final String KEY_STOPPING_LOCATION_UPDATES = "stopping_locaction_updates";

    public static final String KEY_CACHE_ENABLE = "stopping_locaction_updates";
    private static final String KEY_LOCATION_TIMESTAMP ="location_timestamp" ;

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
   public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    public static void setStopingLocationUpdate(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_STOPPING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }
   public static boolean getStoppingFlag(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_STOPPING_LOCATION_UPDATES, false);
    }

    public static void setCache(Context context, boolean setCache) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_CACHE_ENABLE, setCache)
                .apply();
    }
    public static boolean getCache(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_CACHE_ENABLE, false);
    }


    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
   public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    public static void setTimestamp(Context context, long requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(KEY_LOCATION_TIMESTAMP, requestingLocationUpdates)
                .apply();
    }
    public static long getTimeStamp(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(KEY_LOCATION_TIMESTAMP,0023L);
    }
}
