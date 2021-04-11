package com.assignment.roam;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    private static final String TAG = "SharedPref";

    public static final String TRIP = "trip";
    private static final String START_TIME = "start_time";
    private static final String END_TIME = "end_time";

    public static void setTripStart(Context context, String time, boolean start) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TRIP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (start)
            editor.putString(START_TIME, time);
        else
            editor.putString(END_TIME, time);

        editor.apply();
    }

    public static String getStartTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TRIP, Context.MODE_PRIVATE);
        return sharedPreferences.getString(START_TIME, null);
    }

    public static String getEndTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TRIP, Context.MODE_PRIVATE);
        return sharedPreferences.getString(END_TIME, null);
    }

    public static void locationUpdates(Context context, boolean b) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TRIP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("location_updates", b);
        editor.apply();
    }

    public static boolean getLocationUpdates(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TRIP, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("location_updates", false);
    }
}
