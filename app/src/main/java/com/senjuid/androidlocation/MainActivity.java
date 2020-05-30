package com.senjuid.androidlocation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.senjuid.location.LocationPlugin;
import com.senjuid.location.LocationPluginOptions;
import com.senjuid.location.util.LocaleHelper;

public class MainActivity extends AppCompatActivity {

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
                String address = locationPlugin.getCompleteAddress(lon, lat);
                Toast.makeText(MainActivity.this, lon + ", " + lat + ": " + address, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCanceled() {
                Toast.makeText(MainActivity.this, "Getting location canceled", Toast.LENGTH_LONG).show();
            }
        });

        LocationPluginOptions options = new LocationPluginOptions.Builder()
                .setData(dataDummy)
                .build();
        locationPlugin.open(options);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (locationPlugin != null) {
            locationPlugin.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

