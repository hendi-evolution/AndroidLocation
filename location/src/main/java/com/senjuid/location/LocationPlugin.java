package com.senjuid.location;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LocationPlugin {
    private final int REQUEST = 1367;
    private Activity activity;
    private LocationPluginListener locationPluginListener;

    public LocationPlugin(Activity activity) {
        this.activity = activity;
    }

    public void setLocationPluginListener(LocationPluginListener locationPluginListener) {
        this.locationPluginListener = locationPluginListener;
    }

    public void open(String data) {
        if (activity instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
            ActivityResultLauncher<Intent> startForResult = appCompatActivity.prepareCall(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        handleResult(result.getData());
                    }
                }
            });

            Intent intent = new Intent(activity, GeolocationActivity.class);
            intent.putExtra("data", data);
            startForResult.launch(intent);
        } else {
            Intent intent = new Intent(activity, GeolocationActivity.class);
            intent.putExtra("data", data);
            activity.startActivityForResult(intent, REQUEST);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST) {
            handleResult(data);
        }
    }

    private void handleResult(Intent intent) {
        if (intent == null) {
            return;
        }

        Double lon = intent.getDoubleExtra("lon", 0.0);
        Double lat = intent.getDoubleExtra("lat", 0.0);
        if (locationPluginListener != null) {
            locationPluginListener.onLocationRetrieved(lon, lat);
        }
    }

    public interface LocationPluginListener {
        void onLocationRetrieved(Double lon, Double lat);
    }
}
