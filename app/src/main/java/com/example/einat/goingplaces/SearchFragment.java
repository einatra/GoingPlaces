package com.example.einat.goingplaces;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.einat.goingplaces.db.DBManager;
import com.example.einat.goingplaces.db.Place;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment implements View.OnClickListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private EditText editSearch;
    public PlacesListAdapter adapter;
    private DBManager DBmanager;

    private OnLocationSelectedListener selectedListener;
    private OnFirstLocationListener firstLocationListener;
    private Location myLocation;
    public String perimeter;
    private int dontWantLocation;
    SharedPreferences sp;
    private GoogleApiClient myGoogleApiClient;
    private boolean first;

    public SearchFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            selectedListener = (OnLocationSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnLocationSelectedListener");
        }
        try {
            firstLocationListener = (OnFirstLocationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnFirstLocationListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View searchFrag = inflater.inflate(R.layout.fragment_search, container, false);
        dontWantLocation = 0;

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        editSearch = (EditText) searchFrag.findViewById(R.id.editSearch);
        // REMINDER - We agreed I can use only one button which gives search results by key word
        //            if there is one, and all results if empty - Thanks, you can carry on now
        Button btnSearch = (Button) searchFrag.findViewById(R.id.btnT);
        btnSearch.setOnClickListener(this);

        ListView items = (ListView) searchFrag.findViewById(R.id.resultList);

        DBmanager = new DBManager(getActivity(), 1);
        adapter = new PlacesListAdapter(getActivity(), DBmanager.getLastSearch());
        items.setAdapter(adapter);

        items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
                //go to daddy  - he will tell you what to do
                selectedListener.onLocationSelected(id, myLocation);
            }
        });

        items.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
                //dialog - share/add to fav
                AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                dialog.setTitle(getString(R.string.searc_frag_fav_share_dialog_title));
                dialog.setMessage(getString(R.string.searc_frag_fav_share_dialog_msg));

                dialog.setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.searc_frag_fav_share_dialog_btn_share), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double lat = DBmanager.getPlaceByID(id).getLat();
                        double lon = DBmanager.getPlaceByID(id).getLon();
                        String name = DBmanager.getPlaceByID(id).getName();
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, name + "\nhttp://maps.google.com/maps?q=loc:" + lat + "," + lon + "&z=17");
                        startActivity(Intent.createChooser(intent, getString(R.string.searc_frag_fav_share_dialog_share_with)));

                    }
                });

                dialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.searc_frag_fav_share_dialog_btn_add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // adds to fav_table in db
                        DBmanager.insertPlaceToFav(DBmanager.getPlaceByID(id));
                        Boolean open = sp.getBoolean("add", false);
                        if (!open) {
                            //just notify that the place is added to faves list
                            Toast.makeText(getActivity(), getString(R.string.searc_frag_fav_share_dialog_toast_added), Toast.LENGTH_LONG).show();
                        } else {
                            //open the favourites list
                            Intent intent = new Intent(getActivity(), FavAct.class);
                            startActivity(intent);
                        }
                    }
                });

                dialog.show();
                return true;
            }
        });

        buildGoogleApiClient();
        createLocationRequest();
        myGoogleApiClient.connect();
        first = true;

        adapter.notifyDataSetChanged();

        return searchFrag;
    }

    @Override
    public void onStart() {
        super.onStart();
        myGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Location myLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                myGoogleApiClient);
        // suggest the user turn on Location service
        if (myLastLocation == null && dontWantLocation == 0) {
            turnOnLocation();
        }
        firstLocationListener.onLocationReceived(myLastLocation);

        if (first) { // if connection was called from onCreate, start location updates and automatic search
            startLocationUpdates();
            first = false; //will not search when connection is called from onStart, but be available for user search
        }
    }

    protected synchronized void buildGoogleApiClient() {
        myGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    LocationRequest myLocationRequest;

    protected void createLocationRequest() {
        myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(10000);
        myLocationRequest.setFastestInterval(5000);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                myGoogleApiClient, myLocationRequest, this);
    }


    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager =
                ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (myGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                myGoogleApiClient, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        myGoogleApiClient.disconnect();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        selectedListener = null;
    }

    int count = 0;

    @Override
    public void onClick(View v) {
        if (myGoogleApiClient.isConnected()) {
            //get location updates
            startLocationUpdates();
        } else {
            count++;
            if (count == 3) { //remind user to turn on Location
                turnOnLocation();
                count = 0;
            }
        }
    }

    public void turnOnLocation() {
        final AlertDialog locationDialog = new AlertDialog.Builder(getActivity()).create();
        locationDialog.setMessage(getString(R.string.searc_frag_location_on_dialog_msg));
        locationDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.searc_frag_location_on_dialog_btn_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent settings = new Intent
                        ("com.google.android.gms.location.settings.GOOGLE_LOCATION_SETTINGS");
                startActivity(settings);
                locationDialog.dismiss();
            }
        });
        locationDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.searc_frag_location_on_dialog_btn_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dontWantLocation = 1;
                dialog.cancel();
            }
        });
        locationDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getActivity(), "Location Changed", Toast.LENGTH_LONG).show();
        myLocation = location;
        String coordinates = location.getLatitude() + "," + location.getLongitude();
        stopLocationUpdates();

        //start task if there is internet connection
        if (isNetworkAvailable(getActivity())) {

            SearchTask task = new SearchTask();
            String url = "http://api.v3.factual.com/t/places?";
            String key = "vCy2mvOeHmgNf9mRG3In8jMAxyCqPrCr8rqc4cS0";
            String keyWord = editSearch.getText().toString();
            perimeter = sp.getString("perimeter", "2500");

            if (keyWord.equals("")) {
                task.execute(url + "geo={%22$circle%22:{%22$center%22:[" + coordinates + "],%22$meters%22:%20" + perimeter + "}}&KEY=" + key);
            } else {
                task.execute(url + "q=" + keyWord + "&geo={%22$circle%22:{%22$center%22:[" + coordinates + "],%22$meters%22:%20" + perimeter + "}}&KEY=" + key);
            }
        } else { // notify user he won't get new results without internet connection
            Toast.makeText(getActivity(), getString(R.string.searc_frag_toast_no_internet), Toast.LENGTH_LONG).show();
        }
        //"http://api.v3.factual.com/t/places?q=pharmacy&geo={%22$circle%22:{%22$center%22:[32.0840259,34.7785915],%22$meters%22:%202500}}&KEY=kNE2ke8kRW7A0jorvEV922ZzcNONagfPNoOmmcPh")
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {


    }

    public interface OnLocationSelectedListener {
        void onLocationSelected(long id, Location myLocation);
    }

    public interface OnFirstLocationListener {
        void onLocationReceived(Location firstLocation);
    }

    //******************************************************** AsyncTask *********************************************************************

    public class SearchTask extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {

            DBmanager.removeLast();

            // show dialog:
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle(getString(R.string.searc_frag_progress_dialog_title));
            dialog.setMessage(getString(R.string.searc_frag_progress_dialog_msg));
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            BufferedReader reader;
            HttpURLConnection connection;
            StringBuilder builder = new StringBuilder();

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                if (connection.getResponseCode() != 200) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return builder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();

            if (result.equals("{version:3,status:error,error_type:Auth,message:You have exceeded the throttle limit for this request.,data:N D I 100 129}")) {
                Toast.makeText(getActivity(), getString(R.string.async_factuat_limit_toast), Toast.LENGTH_LONG).show();
            } else {
                //get the required fields from the result
                try {
                    JSONObject root = new JSONObject(result);
                    JSONObject response = root.getJSONObject("response");
                    JSONArray data = response.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        String name = data.getJSONObject(i).getString("name");
                        String address = data.getJSONObject(i).getString("address");
                        double dist = data.getJSONObject(i).getDouble("$distance");
                        dist = dist / 1000; // meters to km
                        dist = (double) Math.round(dist * 1000) / 1000; // show only 3 no. after .
                        double lat = data.getJSONObject(i).getDouble("latitude");
                        double lon = data.getJSONObject(i).getDouble("longitude");
                        //save them in new place obj
                        Place place = new Place(name, address, dist, lat, lon);
                        //update obj in db
                        DBmanager.insertPlace(place);
                        //update the listView
                        adapter.swapCursor(DBmanager.getLastSearch());

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}