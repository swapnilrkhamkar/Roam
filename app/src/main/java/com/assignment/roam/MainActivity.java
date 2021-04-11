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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_LOCATION_PERMISSION = 1;

    private Button btnStartTrip;
    private Button btnEndTrip;
    private TextView textView;

    private DBHelper dbHelper;
    private static LiveTrackingService liveTrackingService;
    private boolean serviceBound;
    private int tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStartTrip = findViewById(R.id.btnStartTrip);
        Button btnEndTrip = findViewById(R.id.btnEndTrip);
        textView = findViewById(R.id.textView);

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
                try {
                    if (SharedPref.getLocationUpdates(MainActivity.this)) {
                        stopTrackingTrip();
                        SharedPref.setTripStart(MainActivity.this, String.valueOf(Calendar.getInstance().getTime()), false);
                        dbHelper.updateTrip();

                        String startTime = SharedPref.getStartTime(MainActivity.this);
                        String endTime = SharedPref.getEndTime(MainActivity.this);
                        List<Locations> locations = dbHelper.getLocations();
                        List<Trip> trips = dbHelper.getTrips();
                        Log.e("TRIP_SIZE", "nknknk " + trips.size());

                        if (trips != null && trips.size() > 0) {
                            JSONObject query_string = new JSONObject();
                            for (Trip trip : trips) {
                                JSONArray jsonArray = new JSONArray();
                                query_string.put("trip_id", trip.getTrip_id());
                                query_string.put("start_time", trip.getStart_time());
                                query_string.put("end_time", trip.getEnd_time());

                                for (Locations locations1 : locations) {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("latitude", locations1.getLatitude());
                                    jsonObject.put("logitude", locations1.getLongitude());
                                    jsonObject.put("timestamp", locations1.getTimestamp());
                                    jsonObject.put("accuracy", locations1.getAccuracy());
                                    jsonArray.put(jsonObject);
                                }
                                query_string.put("locations", jsonArray);
                            }

                            textView.setText(query_string.toString());
                            dbHelper.deleteAllLocations();
                        }
                    }
                } catch (Exception e) {
                    Log.e("exception", "bhbhb " + e);
                }
            }
        });
    }

    private void startTrackingTrip() {

        if (checkLocationPermission() && isGpsEnabled()) {
            bindService(new Intent(MainActivity.this, LiveTrackingService.class),
                    serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected");

            LiveTrackingService.LocalBinder localBinder = (LiveTrackingService.LocalBinder) service;
            liveTrackingService = localBinder.getService();
            serviceBound = true;

            Intent intent = new Intent(MainActivity.this, LiveTrackingService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(intent);
            else
                startService(intent);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected");

            liveTrackingService = null;
            serviceBound = false;
        }
    };

    private void stopTrackingTrip() {
        Log.e("liveTrackingService ", "stop " + liveTrackingService);
        Log.e("serviceConnection ", "stop " + serviceConnection);
        Log.e("serviceBound ", "stop " + serviceBound);

        if (liveTrackingService != null) {
            liveTrackingService.removeLocationUpdate();
        }

        if (serviceBound && serviceConnection != null) {
            unbindService(serviceConnection);
            serviceBound = false;
        }

    }

    private boolean isGpsEnabled() {

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        switch (requestCode) {
//            case RC_LOCATION_PERMISSION:
//                bindService(new Intent(MainActivity.this, LiveTrackingService.class),
//                        serviceConnection, Context.BIND_AUTO_CREATE);
//                break;
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceBound && serviceConnection != null) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions.length > 0) {
            if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED
                    && requestCode == RC_LOCATION_PERMISSION) {

                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();

                if (!serviceBound && liveTrackingService == null) {
                    startTrackingTrip();
                }
            } else {
                Toast.makeText(this, "Location permission needed to proceed!", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
