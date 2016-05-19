package com.example.einat.goingplaces;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.einat.goingplaces.db.DBManager;
import com.example.einat.goingplaces.db.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends ActionBarActivity implements SearchFragment.OnLocationSelectedListener, SearchFragment.OnFirstLocationListener {

    private DBManager dbManager;
    private PluggedReceiver receiver;
    IntentFilter filter;
    SupportMapFragment mapFrag;
    Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        dbManager = new DBManager(this, 1);
        receiver = new PluggedReceiver();
        filter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Intent set = new Intent(this, Settings.class);
                startActivity(set);
                return true;
            case R.id.action_favorites:
                Intent fav = new Intent(this, FavAct.class);
                fav.putExtra("current", currentLocation);
                startActivity(fav);
                return true;
            case R.id.action_del_faves:
                final AlertDialog delAllDialog = new AlertDialog.Builder(MainActivity.this).create();
                delAllDialog.setTitle(getString(R.string.main_act_del_fav_dialog_title));
                delAllDialog.setMessage(getString(R.string.main_act_del_fav_dialog_msg));
                delAllDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.main_act_del_fav_dialog_btn_del), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbManager.removeAllFav();

                        dialog.dismiss();
                    }
                });
                delAllDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.main_act_del_fav_dialog_btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                delAllDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onLocationSelected(long id, Location myLocation) {

        if (mapFrag != null) {
            //open on tablet
            GoogleMap mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag))
                    .getMap();

            mMap.clear();
            Place place = dbManager.getPlaceByID(id);
            String name = place.getName();
            String address = place.getAddress();
            LatLng latLng = new LatLng(place.getLat(), place.getLon());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                    .title(getString(R.string.place_marker_title) + name).snippet(getString(R.string.place_marker_snip) + address));

            if (myLocation != null) {
                LatLng cLatLan = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(cLatLan).icon(BitmapDescriptorFactory.fromResource(R.drawable.ping))
                        .title(getString(R.string.here_marker_title)));
            } else {
                Toast.makeText(this, "Your current location is not available", Toast.LENGTH_LONG).show();
            }

        } else {
            //open on phone
            Intent in = new Intent(this, MapsActivity.class);
            in.putExtra("id", id);
            in.putExtra("from", "search"); //opening from search list
            in.putExtra("current", myLocation);
            startActivity(in);
        }
    }

    @Override
    public void onLocationReceived(Location firstLocation) {
        currentLocation = firstLocation;
        if (mapFrag != null) {
            //open on tablet
            GoogleMap mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag))
                    .getMap();
            if (firstLocation != null) {
                LatLng cLatLan = new LatLng(firstLocation.getLatitude(), firstLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cLatLan, 15));
                mMap.addMarker(new MarkerOptions().position(cLatLan).icon(BitmapDescriptorFactory.fromResource(R.drawable.ping))
                        .title(getString(R.string.here_marker_title)));
            } else {
                Toast.makeText(this, "Your current location is not available", Toast.LENGTH_LONG).show();
            }
        }
    }
}