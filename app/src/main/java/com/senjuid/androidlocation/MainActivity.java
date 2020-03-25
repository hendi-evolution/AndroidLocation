package com.senjuid.androidlocation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.senjuid.location.LocationPlugin;
import com.senjuid.location.util.LocaleHelper;

public class MainActivity extends Activity {

    LocationPlugin locationPlugin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set language
        LocaleHelper.setLocale(this, "in");

        String dataDummy = "{data:[{work_lat: -6.283693, work_lon: 106.725430, work_radius: 0.4 },{work_lat: -6.175110, work_lon: 106.865036, work_radius: 0.5 }]}";
        locationPlugin = new LocationPlugin(this);
        locationPlugin.setLocationPluginListener(new LocationPlugin.LocationPluginListener() {
            @Override
            public void onLocationRetrieved(Double lon, Double lat) {
                Toast.makeText(MainActivity.this, "MainActivity " + lon + ", " + lat, Toast.LENGTH_LONG).show();
            }
        });
        locationPlugin.open(dataDummy);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (locationPlugin != null) {
            locationPlugin.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

