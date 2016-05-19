package com.example.einat.goingplaces;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.einat.goingplaces.db.DBManager;

/**
 * Created by Einat on 27/07/2015.
 */
public class PlacesListAdapter extends CursorAdapter {

    DBManager dbmanager;
    TextView textDist;
    double dist;

    public PlacesListAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        dbmanager = new DBManager(context, 1);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //connect to the views to populate in inflated custom list item
        TextView textName = (TextView)view.findViewById(R.id.textName);
        TextView textAddress = (TextView)view.findViewById(R.id.textAddress);
        textDist = (TextView)view.findViewById(R.id.textDist);

        // extract data from cursor
        String name = cursor.getString(cursor.getColumnIndex(dbmanager.NAME_COL));
        String address = cursor.getString(cursor.getColumnIndex(dbmanager.ADDRESS_COL));
        dist = cursor.getDouble(cursor.getColumnIndex(dbmanager.DIST_COL));

        //populate fields with extracted data
        textName.setText(name);
        textAddress.setText("Address: " + address);

        if (context.getClass().equals(FavAct.class)){ //don't display dist in favList
            textDist.setText(null);
        }
        else{

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String measure = sp.getString("units", "KM");
        if (measure.equals("MI")){
            dist = Math.rint(dist*0.621371192*1000)/1000;
            textDist.setText(dist + context.getString(R.string.miles_from_you));
        }else {
            textDist.setText(dist + context.getString(R.string.km_from_you));
        }
        }
    }
}