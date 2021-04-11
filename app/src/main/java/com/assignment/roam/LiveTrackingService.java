package com.assignment.roam;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;

public class LiveTrackingService extends Service {

    private static final String TAG = "LiveTrackingService";
    private static final String CHANNEL_ID = "CID_LOCATION";
    private static final int NOTIFICATION_ID = 123;

    private NotificationManager notificationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location location;

    private static final int LIVE_TRACKING_INTERVAL = 5000;

    private IBinder binder = new LocalBinder();
    private DBHelper dbHelper;

    public LiveTrackingService() {
    }

    public class LocalBinder extends Binder {
        public LiveTrackingService getService() {
            return LiveTrackingService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setShowBadge(false);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        dbHelper = new DBHelper(LiveTrackingService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

//        tripId = intent.getStringExtra(Trip.TRIP_ID);

//        if (tripId != null) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.e(TAG, "onLocationResult: tracking " + locationResult.getLastLocation());
                onNewLocation(locationResult.getLastLocation());
            }
        };
        createLocationRequest();
//        getLastLocation();
//        } else
//            removeLocationUpdate();

        return START_NOT_STICKY;
    }

    public void removeLocationUpdate() {

        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            stopSelf();
            LiveTrackingService.this.stopForeground(true);
            SharedPref.locationUpdates(LiveTrackingService.this, false);

        } catch (SecurityException e) {
            e.printStackTrace();
            SharedPref.locationUpdates(LiveTrackingService.this, true);
        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LIVE_TRACKING_INTERVAL);
        locationRequest.setMaxWaitTime(LIVE_TRACKING_INTERVAL);
        locationRequest.setFastestInterval(LIVE_TRACKING_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        requestLocationUpdates();
    }

    private void getLastLocation() {

        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            LiveTrackingService.this.location = location;
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void onNewLocation(Location lastLocation) {
        location = lastLocation;
        updateTripLocation();

        notificationManager.notify(NOTIFICATION_ID, getNotification());
        LiveTrackingService.this.startForeground(NOTIFICATION_ID, getNotification());
    }

    public void requestLocationUpdates() {
        try {
            SharedPref.locationUpdates(LiveTrackingService.this, true);
            if (fusedLocationProviderClient != null) {
                SharedPref.setTripStart(LiveTrackingService.this, String.valueOf(Calendar.getInstance().getTime()), true);
                dbHelper.addStartTimeTrip(String.valueOf(Calendar.getInstance().getTime()));
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            SharedPref.locationUpdates(LiveTrackingService.this, false);
        }
    }

    private Notification getNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                /*.setContentIntent(activityPendingIntent)*/
                /*.addAction(R.drawable.ic_my_location_black_24dp, "Open", activityPendingIntent)*/
                /*.addAction(R.drawable.ic_cancel_black_24dp, "Stop", servicePendingIntent)*/
                .setSmallIcon(R.drawable.ic_my_location_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                .setContentTitle("Roam")
                .setContentText("Trip tracking is ON")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setTicker("Live tracking")
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }
        return builder.build();
    }

    private void updateTripLocation() {

        if (location != null) {
            Locations locations = new Locations(String.valueOf(location.getTime()), location.getLatitude(), location.getLongitude(), location.getAccuracy());
            try {
                dbHelper.addLocation(locations);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("JSON", "JSONException " + e);
            }
        }

    }

}
