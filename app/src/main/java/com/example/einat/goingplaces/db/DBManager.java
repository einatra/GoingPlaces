package com.example.einat.goingplaces.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Einat on 15/07/2015.
 */
public class DBManager extends SQLiteOpenHelper {

    public final String LAST_SEARCH_TABLE = "searches";
    public final String FAV_TABLE = "faves";
    public final String _ID_COL = "_id";
    public final String NAME_COL = "name";
    public final String ADDRESS_COL = "address";
    public final String DIST_COL = "distance";
    public final String LAT_COL = "lat";
    public final String LON_COL = "lon";

    public DBManager(Context context, int version) {
        super(context, "places.db", null, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + LAST_SEARCH_TABLE + "( " + _ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NAME_COL + " TEXT, " + ADDRESS_COL + " TEXT, " + DIST_COL + " REAL, " +
                    LAT_COL + " REAL, " + LON_COL + " REAL)");


        db.execSQL("CREATE TABLE " + FAV_TABLE + "( " + _ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME_COL + " TEXT UNIQUE, " + ADDRESS_COL + " TEXT, " + DIST_COL + " REAL, " +
                LAT_COL + " REAL, " + LON_COL + " REAL)");

    }

    public void insertPlace(Place p){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME_COL, p.getName());
        values.put(ADDRESS_COL, p.getAddress());
        values.put(DIST_COL, p.getDist());
        values.put(LAT_COL, p.getLat());
        values.put(LON_COL, p.getLon());
        db.insert(LAST_SEARCH_TABLE, null, values);
        db.close();
    }

    public void insertPlaceToFav(Place p){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME_COL, p.getName());
        values.put(ADDRESS_COL, p.getAddress());
        values.put(DIST_COL, p.getDist());
        values.put(LAT_COL, p.getLat());
        values.put(LON_COL, p.getLon());
        db.insert(FAV_TABLE, null, values);
        db.close();
    }

    public Cursor getLastSearch(){
        SQLiteDatabase db = getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + LAST_SEARCH_TABLE, null);
    }

    public Place getPlaceByID(long id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + LAST_SEARCH_TABLE + " WHERE " + _ID_COL + "=" + id, null);
        Place place;
        c.moveToFirst();
        String name = c.getString(c.getColumnIndex(NAME_COL));
        String address = c.getString(c.getColumnIndex(ADDRESS_COL));
        double dist = c.getDouble(c.getColumnIndex(DIST_COL));
        double lat = c.getDouble(c.getColumnIndex(LAT_COL));
        double lon = c.getDouble(c.getColumnIndex(LON_COL));
        place = new Place(name, address, dist, lat, lon);
        c.close();
        return  place;
    }

    public Place getFavByID(long id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + FAV_TABLE + " WHERE " + _ID_COL + "=" + id, null);
        Place place;
        c.moveToFirst();
        String name = c.getString(c.getColumnIndex(NAME_COL));
        String address = c.getString(c.getColumnIndex(ADDRESS_COL));
        double dist = c.getDouble(c.getColumnIndex(DIST_COL));
        double lat = c.getDouble(c.getColumnIndex(LAT_COL));
        double lon = c.getDouble(c.getColumnIndex(LON_COL));
        place = new Place(name, address, dist, lat, lon);
        c.close();
        return  place;
    }

    public Cursor getAllFav(){
        SQLiteDatabase db = getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + FAV_TABLE, null);
    }

    public void removeLast(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(LAST_SEARCH_TABLE, null, null);
        db.close();
    }

    public void removeFavById(long id){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(FAV_TABLE, _ID_COL + "=" + id, null);
        db.close();
    }

    public void removeAllFav(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(FAV_TABLE, null, null);
        db.close();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
