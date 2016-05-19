package com.example.einat.goingplaces;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.einat.goingplaces.db.DBManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavesFragment extends Fragment implements FavAct.onClearFav {

    private PlacesListAdapter adapter;
    private DBManager DBmanager;

    public FavesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Context context = getActivity();
        //initialize the onClarfav listener for FavAct
        ((FavAct) context).onClearFav = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View favFrag = inflater.inflate(R.layout.fragment_faves, container, false);
        ListView favList = (ListView) favFrag.findViewById(R.id.listFav);


        DBmanager = new DBManager(getActivity(), 1);
        adapter = new PlacesListAdapter(getActivity(), DBmanager.getAllFav());
        favList.setAdapter(adapter);


        favList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //************************* open the location on map
                Intent in = new Intent(getActivity(), MapsActivity.class);
                in.putExtra("id", id);
                in.putExtra("from", "fav"); //opening from fav list
                startActivity(in);
            }
        });

        favList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
                AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                dialog.setTitle("Delete / Share item");
                dialog.setMessage("What would you like to do?");
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBmanager.removeFavById(id);
                        adapter.swapCursor(DBmanager.getAllFav());
                        dialog.dismiss();
                    }
                });
                dialog.setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.searc_frag_fav_share_dialog_btn_share), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double lat = DBmanager.getFavByID(id).getLat();
                        double lon = DBmanager.getFavByID(id).getLon();
                        String name = DBmanager.getFavByID(id).getName();
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, name + "\nhttp://maps.google.com/maps?q=loc:" + lat + "," + lon + "&z=16");
                        startActivity(Intent.createChooser(intent, getString(R.string.searc_frag_fav_share_dialog_share_with)));
                    }
                });

                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;
            }
        });
        return favFrag;
    }


    @Override
    public void refreshAdapter() {
        adapter.swapCursor(DBmanager.getAllFav());
    }
}
