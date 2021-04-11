package com.assignment.roam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.assignment.roam.DBHelper.DBHelperItem.TABLE_NAME_LOC;

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

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME_LOC;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_LOC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_LOC);
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
}
