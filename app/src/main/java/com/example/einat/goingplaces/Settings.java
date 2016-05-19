package com.example.einat.goingplaces;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by jbt on 23/07/2015.
 */
public class Settings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
    }
}
