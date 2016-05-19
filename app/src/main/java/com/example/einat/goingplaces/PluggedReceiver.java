package com.example.einat.goingplaces;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by Einat on 26/07/2015.
 */
public class PluggedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sp.edit();
        int pluggedIn = sp.getInt("plugged", -1);
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, pluggedIn);
        if (pluggedIn!=plugged) {
            String msg = "Phone plugged in";
            switch (plugged) {
                case 0:
                    msg = "Phone plugged out ";
                    break;
                case BatteryManager.BATTERY_PLUGGED_AC:
                    msg += " (AC)";
                    break;
                case BatteryManager.BATTERY_PLUGGED_USB:
                    msg += " (USB)";
                    break;
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    msg += " (Wireless)";
                    break;
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            edit.putInt("plugged", plugged);
            edit.commit();
        }
    }
}
