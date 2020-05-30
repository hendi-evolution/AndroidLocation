package com.senjuid.location;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

public class LocationPlugin {
    public static final int REQUEST = 1367;

    private Activity activity;
    private LocationPluginListener locationPluginListener;

    public LocationPlugin(Activity activity) {
        this.activity = activity;
    }

    public void setLocationPluginListener(LocationPluginListener locationPluginListener) {
        this.locationPluginListener = locationPluginListener;
    }

    public void open(LocationPluginOptions options) {
        if (activity instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
            ActivityResultLauncher<Intent> startForResult = appCompatActivity.prepareCall(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        handleResult(result.getData());
                    } else {
                        if (locationPluginListener != null) {
                            locationPluginListener.onCanceled();
                        }
                    }
                }
            });
            startForResult.launch(getIntent(options));
        } else {
            activity.startActivityForResult(getIntent(options), REQUEST);
        }
    }

    public Intent getIntent(LocationPluginOptions options) {
        Intent intent = new Intent(activity, GeolocationActivity.class);
        intent.putExtra("data", options.data);
        intent.putExtra("message1", options.message1);
        intent.putExtra("message2", options.message2);
        return intent;
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST) {
            handleResult(data);
        }
    }

    private void handleResult(Intent intent) {
        if (locationPluginListener != null) {
            if (intent != null) {
                Double lon = intent.getDoubleExtra("lon", 0.0);
                Double lat = intent.getDoubleExtra("lat", 0.0);
                locationPluginListener.onLocationRetrieved(lon, lat);
            } else {
                locationPluginListener.onCanceled();
            }
        }
    }

    public String getCompleteAddress(double lat, double lon) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }

    public interface LocationPluginListener {
        void onLocationRetrieved(Double lon, Double lat);

        void onCanceled();
    }
}
