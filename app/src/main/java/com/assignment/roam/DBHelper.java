package com.assignment.roam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.json.JSONObject;

import static com.assignment.roam.DBHelper.DBHelperItem.COLUMN_NAME_LOCATIONS;
import static com.assignment.roam.DBHelper.DBHelperItem.TABLE_NAME;

public class DBHelper extends SQLiteOpenHelper {
    private Context context;

    private static final String LOG_TAG = "DBHelper";

    public static final String DATABASE_NAME = "location.db";
    private static final int DATABASE_VERSION = 1;

    public static abstract class DBHelperItem implements BaseColumns {
        public static final String TABLE_NAME = "location";

        //        public static final String COLUMN_NAME_TRIP_ID = "tripId";
        public static final String COLUMN_NAME_START_TIME = "start_time";
        public static final String COLUMN_NAME_END_TIME = "end_time";
        public static final String COLUMN_NAME_LOCATIONS = "locations";


    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    DBHelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_START_TIME + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_END_TIME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_LOCATIONS + TEXT_TYPE + ")";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public Trip getTrip() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.COLUMN_NAME_START_TIME,
                DBHelperItem.COLUMN_NAME_END_TIME,
                COLUMN_NAME_LOCATIONS,

        };
        Cursor c = db.query(TABLE_NAME, projection, null, null, null, null, null);
        if (c.moveToNext()) {
            Trip item = new Trip();
            item.setTrip_id(c.getString(c.getColumnIndex(DBHelperItem._ID)));
            item.setStart_time(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_START_TIME)));
            item.setEnd_time(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_END_TIME)));
//            item.setLocations(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_LOCATIONS)));
            c.close();
            return item;
        }
        return null;
    }

    public void addStartTime(String startTime, boolean start) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        if (start)
            cv.put(DBHelperItem.COLUMN_NAME_START_TIME, startTime);

        else
            cv.put(DBHelperItem.COLUMN_NAME_END_TIME, startTime);

        db.insert(TABLE_NAME, null, cv);
    }

    public void insert(JSONObject jsonObject) {
        ContentValues values = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            values.put(COLUMN_NAME_LOCATIONS, jsonObject.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        db.insert(TABLE_NAME, null, values);
    }
}
