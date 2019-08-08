package com.example.bouncedemo;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bounce.location.LocationUpdateService;
import com.bounce.location.LocationUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private static final String TAG =  MyFirebaseMessagingService.class.getSimpleName();
    private static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationupdatesforegroundservice";
    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());



            //stop service
 /*           stopService(new Intent(MyFirebaseMessagingService.this, LocationUpdateService.class));

            Utils.setRequestingLocationUpdates(this,false);*/

            // Notify anyone listening for broadcasts about the new location.

            //for foreground and resume
            LocationUtils.setStopingLocationUpdate(this,true);
            stopService(new Intent(MyFirebaseMessagingService.this, LocationUpdateService.class));


            Intent intent = new Intent(ACTION_BROADCAST);
            intent.putExtra("from", 1234);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            Log.e(TAG,"Service has been stopped from fcm");


            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
               // scheduleJob();
            } else {
                // Handle message within 10 seconds
                //handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.e(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
       // sendRegistrationToServer(token);
    }
}
