package com.assignment.roam;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_LOCATION_PERMISSION = 1;
    private Button btnStartTrip;
    private Button btnEndTrip;
    private DBHelper dbHelper;

    private LiveTrackingService liveTrackingService;
    private boolean serviceBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStartTrip = findViewById(R.id.btnStartTrip);
        Button btnEndTrip = findViewById(R.id.btnEndTrip);

        dbHelper = new DBHelper(MainActivity.this);

        btnStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrackingTrip();
            }
        });

        btnEndTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTrackingTrip();
            }
        });

    }

    private void startTrackingTrip() {

        if (checkLocationPermission() && isGpsEnabled()) {
            bindService(new Intent(MainActivity.this, LiveTrackingService.class),
                    serviceConnection, Context.BIND_AUTO_CREATE);

            dbHelper.addStartTime(String.valueOf(Calendar.getInstance().getTime()), true);

        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected");

            LiveTrackingService.LocalBinder localBinder = (LiveTrackingService.LocalBinder) service;
            liveTrackingService = localBinder.getService();
            serviceBound = true;

            Intent intent = new Intent(MainActivity.this, LiveTrackingService.class);
//            intent.putExtra(Trip.TRIP_ID, mTripId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(intent);
            else
                startService(intent);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");

            liveTrackingService = null;
            serviceBound = false;
        }
    };

    private void stopTrackingTrip() {
        if (liveTrackingService != null) {
            liveTrackingService.removeLocationUpdate();
        }

        if (serviceBound && serviceConnection != null) {
            unbindService(serviceConnection);
            serviceBound = false;
        }

        dbHelper.addStartTime(String.valueOf(Calendar.getInstance().getTime()), false);
    }

    private boolean checkLocationPermission() {
//        Log.e("API_LEVEL", "BHUJNUJN " + android.os.Build.VERSION.SDK_INT);
        String[] locationPermissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            locationPermissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
        } else {
            locationPermissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(), locationPermissions[0])
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{locationPermissions[0]},
                    RC_LOCATION_PERMISSION);

            return false;
        }

        return true;
    }

    private boolean isGpsEnabled() {

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_LOCATION_PERMISSION:
//                if (resultCode == RESULT_OK) {
                bindService(new Intent(MainActivity.this, LiveTrackingService.class),
                        serviceConnection, Context.BIND_AUTO_CREATE);

                dbHelper.addStartTime(String.valueOf(Calendar.getInstance().getTime()), true);
//                }
                break;
        }
    }
}
