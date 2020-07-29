package com.senjuid.location

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class LocationPlugin(private val activity: Activity) {
    private var locationPluginListener: LocationPluginListener? = null

    companion object {
        const val REQUEST = 1367
    }

    fun setLocationPluginListener(locationPluginListener: LocationPluginListener?) {
        this.locationPluginListener = locationPluginListener
    }

    fun open(options: LocationPluginOptions) {
        if (activity is AppCompatActivity) {
            val startForResult = activity.prepareCall(StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handleResult(result.data)
                } else {
                    locationPluginListener?.onCanceled()
                }
            }
            startForResult.launch(getIntent(options))
        } else {
            activity.startActivityForResult(getIntent(options), REQUEST)
        }
    }

    fun getIntent(options: LocationPluginOptions): Intent {
        val intent = Intent(activity, GeolocationActivity::class.java)
        intent.putExtra("data", options.data)
        intent.putExtra("message1", options.message1)
        intent.putExtra("message2", options.message2)
        return intent
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST) {
            handleResult(data)
        } else if (resultCode == Activity.RESULT_CANCELED && requestCode == REQUEST) {
            locationPluginListener?.onCanceled()
        }
    }

    private fun handleResult(intent: Intent?) {
        if (intent != null) {
            val lon = intent.getDoubleExtra("lon", 0.0)
            val lat = intent.getDoubleExtra("lat", 0.0)
            val isMock = intent.getBooleanExtra("isMock", false)
            locationPluginListener?.onLocationRetrieved(lon, lat, isMock)
        } else {
            locationPluginListener?.onCanceled()
        }
    }

    fun getCompleteAddress(lat: Double, lon: Double): String {
        var strAdd = ""
        val geoCoder = Geocoder(activity, Locale.getDefault())
        try {
            val addresses = geoCoder.getFromLocation(lat, lon, 1)
            if (addresses != null) {
                val returnedAddress = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return strAdd
    }

    interface LocationPluginListener {
        fun onLocationRetrieved(lon: Double?, lat: Double?, isMock: Boolean?)
        fun onCanceled()
    }
}