package com.example.einat.goingplaces;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.einat.goingplaces.db.DBManager;

public class FavAct extends ActionBarActivity {

    DBManager dbManager;
    public onClearFav onClearFav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fav_container, new FavesFragment(), "favFrag").commit();

        dbManager = new DBManager(this, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fav, menu);
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
            case R.id.action_del_faves:
                final AlertDialog delAllDialog = new AlertDialog.Builder(FavAct.this).create();
                delAllDialog.setTitle(getString(R.string.main_act_del_fav_dialog_title));
                delAllDialog.setMessage(getString(R.string.main_act_del_fav_dialog_msg));
                delAllDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.main_act_del_fav_dialog_btn_del), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbManager.removeAllFav();
                        onClearFav.refreshAdapter();
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
    protected void onResume() {
        super.onResume();
    }

    //interface for refreshing adapter in fragment after deleting initiated in FavAct
    public interface onClearFav {
        void refreshAdapter();
    }
}