package com.senjuid.androidlocation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.senjuid.location.LocationPlugin;
import com.senjuid.location.LocationPluginOptions;
import com.senjuid.location.util.LocaleHelper;

public class MainActivity extends AppCompatActivity {

    LocationPlugin locationPlugin;

    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set language
        LocaleHelper.setLocale(this, "in");

        // init plugin
        locationPlugin = new LocationPlugin(this);
        locationPlugin.setLocationPluginListener(new LocationPlugin.LocationPluginListener() {
            @Override
            public void onLocationRetrieved(Double lon, Double lat, Boolean isMock) {
                String address = locationPlugin.getCompleteAddress(lon, lat);
                StringBuilder sb = new StringBuilder();
                sb.append("Longitude: ").append(lon).append("\n\n");
                sb.append("Latitude: ").append(lat).append("\n\n");
                sb.append("Fake location: ").append(isMock).append("\n\n");
                sb.append("Address: ").append(address);

                tvResult.setText(sb.toString());
            }

            @Override
            public void onCanceled() {
                Toast.makeText(MainActivity.this, "Getting location canceled", Toast.LENGTH_LONG).show();
            }
        });

        tvResult = findViewById(R.id.tv_result);
        findViewById(R.id.btn_get_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dataDummy = "{data:[{work_lat: -6.283693, work_lon: 106.725430, work_radius: 0.4 },{work_lat: -6.175110, work_lon: 106.865036, work_radius: 0.5 }]}";
                LocationPluginOptions options = new LocationPluginOptions.Builder()
                        .setData(dataDummy)
                        .build();
                locationPlugin.open(options);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (locationPlugin != null) {
            locationPlugin.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

