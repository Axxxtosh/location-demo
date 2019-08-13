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

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.bounce.location.remote.Example;
import com.bounce.location.remote.GetDataService;
import com.bounce.location.remote.RetrofitClientInstance;
import com.bounce.location.room.LocationDatabase;
import com.bounce.location.room.LocationInfo;
import com.facebook.battery.metrics.cpu.CpuMetrics;
import com.facebook.battery.metrics.cpu.CpuMetricsCollector;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.judemanutd.autostarter.AutoStartPermissionHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 *
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 *
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification assocaited with that service is removed.
 */
public class LocationUpdateService extends Service {

    private static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationupdatesforegroundservice";

    private static final String TAG = LocationUpdateService.class.getSimpleName();

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "Bounce";

    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";


    private static final CpuMetricsCollector sCollector = new CpuMetricsCollector();
    private static final float SMALLEST_DISTANCE = 1;
    private final CpuMetrics mInitialMetrics = sCollector.createMetrics();
    private final CpuMetrics mFinalMetrics = sCollector.createMetrics();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS =  10000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            2*30 * 1000;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused LocationInfo Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;
    /**
     * The current location.
     */
    private Location mLocation;

    private LocationDatabase locationDatabase;

    private String diff;
    private String cachedJson;


    public LocationUpdateService() {
    }

    @Override
    public void onCreate() {


        //auto starter for chinese OEMs
        AutoStartPermissionHelper.getInstance().getAutoStartPermission(getApplicationContext());
        Log.e(TAG,"auto starter for OENs initialised");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //may be a batch
                onNewLocation(locationResult.getLastLocation());
                Log.e(TAG,"Batch size: "+locationResult.getLocations().size());

                /* for(int i = 0; i < locationResult.getLocations().size(); i++) {

                    Log.e(TAG,"Batch data: "+locationResult.getLocations().get(i).toString());


                }*/
                //checkNetworkforApi();
                // create a new Gson instance
                Gson gson = new Gson();
                // convert your list to json
                String jsonLocationList = gson.toJson(locationResult);
                // print your generated json
                Log.e(TAG,"jsonLocationList: " + jsonLocationList);

                checkNetworkforApi(jsonLocationList);

            }



        };

        LocationUtils.setTimestamp(this,000);
        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        locationDatabase= Room.databaseBuilder(getApplicationContext(), LocationDatabase.class, "LOCATION").build();



    }

    private void checkNetworkforApi(String jsonData) {

        if(isNetworkAvailable(getApplicationContext())){

                    Log.e(TAG,"checking connection : available ");
                    //upload data from cache if available and clear the cache
                    LocationInfo locationInfo=new LocationInfo();
                    locationInfo.setJson(jsonData);



                    callApi(jsonData+","+getCache());

                }
                else{

                    Log.e(TAG,"checking connection : not available ");
                    cacheData(jsonData);
                }
    }


    private void cacheData(String locationResult) {

        final LocationInfo location=new LocationInfo();

        location.setJson(locationResult);

        Log.e(TAG,"caching data ");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                locationDatabase.daoLocation().insertLocation(location);
                return null;
            }
        }.execute();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "Service started");

        requestLocationUpdates();
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }else{


            startForeground(NOTIFICATION_ID, getNotification());
        }

        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        //to remove all callbacks
        //removeCache();

        sCollector.getSnapshot(mFinalMetrics);
        Log.e("BatteryMetrics", mFinalMetrics.diff(mInitialMetrics).toString());
        removeLocationUpdates();
        Log.e(TAG, "In onDestroyed");
    }


    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    @SuppressLint("MissingPermission")
    public void requestLocationUpdates() {

        LocationUtils.setRequestingLocationUpdates(this, true);
        Log.e(TAG, "Requesting location updates");

        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            LocationUtils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.e(TAG, "Removing location updates");
        Log.e(TAG, "Removing cache data");

        removeCache();

        LocationUtils.setRequestingLocationUpdates(this, false);
        try {
            mServiceHandler.removeCallbacksAndMessages(null);
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            stopSelf();
        } catch (SecurityException unlikely) {
            LocationUtils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, LocationUpdateService.class);

        CharSequence text = LocationUtils.getLocationText(mLocation);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)

                .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(" Location refreshed after"+ diff+" seconds.")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launch)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                                //first location before repeated callbacks
                                mNotificationManager.notify(NOTIFICATION_ID, getNotification());
                                Log.e(TAG,"Last location: "+task.getResult());
                            } else {
                                Log.e(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {


        Log.e(TAG, "New location: " + location);
        mLocation = location;
        //calculate difference
        diff= calculateDiff(location.getTime());
        //set location time to pref
        LocationUtils.setTimestamp(this,mLocation.getTime());
        // Notify anyone listening for broadcasts about the new location.
       /*  Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);*/
        //API call here
        // Update notification content if running as a foreground service.
        mNotificationManager.notify(NOTIFICATION_ID, getNotification());
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setMaxWaitTime(10000);
        mLocationRequest.setFastestInterval(2000);//affects batching
        //mLocationRequest.setSmallestDisplacement(1);
        //mLocationRequest.setSmallestDisplacement(SMALLEST_DISTANCE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "In onTaskRemoved");
    }

    private String calculateDiff(long time2) {

        long time1=LocationUtils.getTimeStamp(this);
        long diffMs = time2 - time1;
        long diffSec = diffMs / 1000;
        long min = diffSec / 60;
        long sec = diffSec % 60;
        Log.e(TAG,"The difference is "+min+" minutes and "+sec+" seconds.");


        return String.valueOf(sec);
    }

    private void callApi(String locList) {
        GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        Call<List<Example>> call=service.getAll(locList);

        call.enqueue(new Callback<List<Example>>() {
            @Override
            public void onResponse(Call<List<Example>> call, Response<List<Example>> response) {

                Log.e("API Call","success");

            }

            @Override
            public void onFailure(Call<List<Example>> call, Throwable t) {

                Log.e("API error",t.getMessage());

            }
        });
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static boolean isConnectedToNetwork(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isConnected = false;
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            isConnected = (activeNetwork != null) && (activeNetwork.isConnectedOrConnecting());
        }

        return isConnected;
    }

    @SuppressLint("StaticFieldLeak")
    private void removeCache() {

        Log.e(TAG, "remove cache");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                locationDatabase.clearAllTables();
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private String getCache() {

        Log.e(TAG, "in get cache block");



        new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                locationDatabase.daoLocation().loadCache();

                // create a new Gson instance
                Gson gson = new Gson();
                // convert your list to json
                String jsonLocationList = gson.toJson(locationDatabase.daoLocation().loadCache());
                // print your generated json
                Log.e(TAG,"jsonLocationList: " + jsonLocationList);

                cachedJson=jsonLocationList;

                return null;
            }
        }.execute();


        Log.e(TAG,"cached location data "+cachedJson);
        return cachedJson;
    }




}
