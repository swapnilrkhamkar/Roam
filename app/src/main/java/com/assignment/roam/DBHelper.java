package com.assignment.roam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.assignment.roam.DBHelper.DBHelperItem.TABLE_NAME_LOC;
import static com.assignment.roam.DBHelper.DBHelperItem.TABLE_NAME_TRIP;

public class DBHelper extends SQLiteOpenHelper {

    private Context context;

    private static final String LOG_TAG = "DBHelper";
    public static final String DATABASE_NAME = "location.db";
    private static final int DATABASE_VERSION = 1;

    public static abstract class DBHelperItem implements BaseColumns {
        public static final String TABLE_NAME_LOC = "location";

        public static final String COLUMN_NAME_LOC_LAT = "latitude";
        public static final String COLUMN_NAME_LOC_LONG = "longitude";
        public static final String COLUMN_NAME_LOC_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_LOC_ACCURACY = "accuracy";

        public static final String TABLE_NAME_TRIP = "trip";

        public static final String COLUMN_NAME_START_TIME = "start_time";
        public static final String COLUMN_NAME_END_TIME = "end_time";
        public static final String COLUMN_NAME_LOCATIONS = "locations";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES_LOC =
            "CREATE TABLE " + TABLE_NAME_LOC + " (" +
                    DBHelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_LOC_LAT + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_LOC_LONG + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_LOC_TIMESTAMP + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_LOC_ACCURACY + TEXT_TYPE + ")";

    private static final String SQL_CREATE_ENTRIES_TRIP =
            "CREATE TABLE " + TABLE_NAME_TRIP + " (" +
                    DBHelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_START_TIME + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_END_TIME + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_LOCATIONS + TEXT_TYPE + ")";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME_LOC;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_LOC);
        db.execSQL(SQL_CREATE_ENTRIES_TRIP);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_LOC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_TRIP);
        onCreate(db);
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public List<Locations> getLocations() {
        List<Locations> locations = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Locations item = new Locations();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.COLUMN_NAME_LOC_LAT,
                DBHelperItem.COLUMN_NAME_LOC_LONG,
                DBHelperItem.COLUMN_NAME_LOC_TIMESTAMP,
                DBHelperItem.COLUMN_NAME_LOC_ACCURACY
        };
        Cursor c = db.query(TABLE_NAME_LOC, null, null, null, null, null, null);
        while (c.moveToNext()) {
            item.setLatitude(c.getDouble(c.getColumnIndex(DBHelperItem.COLUMN_NAME_LOC_LAT)));
            item.setLongitude(c.getDouble(c.getColumnIndex(DBHelperItem.COLUMN_NAME_LOC_LONG)));
            item.setTimestamp(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_LOC_TIMESTAMP)));
            item.setAccuracy(c.getDouble(c.getColumnIndex(DBHelperItem.COLUMN_NAME_LOC_ACCURACY)));
            locations.add(item);
        }
        c.close();
        return locations;
    }

    public void addLocation(Locations request) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DBHelperItem.COLUMN_NAME_LOC_LAT, request.getLatitude());
        cv.put(DBHelperItem.COLUMN_NAME_LOC_LONG, request.getLongitude());
        cv.put(DBHelperItem.COLUMN_NAME_LOC_TIMESTAMP, String.valueOf(Calendar.getInstance().getTime()));
        cv.put(DBHelperItem.COLUMN_NAME_LOC_ACCURACY, request.getAccuracy());

        db.insert(TABLE_NAME_LOC, null, cv);
    }

    public void deleteAllLocations() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME_LOC, null, null);
    }

    public void addStartTimeTrip(String valueOf) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DBHelperItem.COLUMN_NAME_START_TIME, valueOf);

        db.insert(TABLE_NAME_TRIP, null, cv);
    }

    public List<Trip> getTrips() {
        List<Trip> trips = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Trip item = new Trip();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.COLUMN_NAME_LOC_LAT,
                DBHelperItem.COLUMN_NAME_LOC_LONG,
                DBHelperItem.COLUMN_NAME_LOC_TIMESTAMP,
                DBHelperItem.COLUMN_NAME_LOC_ACCURACY
        };
        Cursor c = db.query(TABLE_NAME_TRIP, null, null, null, null, null, null);
        while (c.moveToNext()) {
            item.setTrip_id(c.getString(c.getColumnIndex(DBHelperItem._ID)));
            item.setStart_time(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_START_TIME)));
            item.setEnd_time(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_END_TIME)));
            item.setLocations(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_LOCATIONS)));
            trips.add(item);
        }
        c.close();
        return trips;
    }

    public void updateTrip() {
        try {

            List<Trip> trips = getTrips();
            String tripId = trips.get(trips.size() - 1).getTrip_id();
            List<Locations> locations = getLocations();
            JSONObject query_string = new JSONObject();
            JSONArray jsonArray = new JSONArray();
//        for (Trip trip : trips) {
//            tripId = trip.getTrip_id();
//        }
            for (Locations locations1 : locations) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("latitude", locations1.getLatitude());
                jsonObject.put("logitude", locations1.getLongitude());
                jsonObject.put("timestamp", locations1.getTimestamp());
                jsonObject.put("accuracy", locations1.getAccuracy());
                jsonArray.put(jsonObject);
            }
            query_string.put("locations", jsonArray);

            if (tripId != null) {
                SQLiteDatabase db = getWritableDatabase();
                ContentValues cv = new ContentValues();

                cv.put(DBHelperItem.COLUMN_NAME_LOCATIONS, query_string.toString());
                cv.put(DBHelperItem.COLUMN_NAME_END_TIME, String.valueOf(Calendar.getInstance().getTime()));

                db.update(TABLE_NAME_TRIP, cv, "_id = ?", new String[]{tripId});
            }
        } catch (Exception e) {

        }
    }
}
