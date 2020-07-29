package com.senjuid.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.lang.ref.WeakReference

@SuppressLint("MissingPermission")
class GeolocationViewModel(appContext: Context) : ViewModel() {

    companion object {
        const val UPDATE_INTERVAL: Long = 10 * 1000 /* 10 secs */
        const val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
        const val TAG = "GeolocationViewModel"
    }

    private val fusedLocationProviderClient: FusedLocationProviderClient? = LocationServices.getFusedLocationProviderClient(appContext)
    private val wrContext: WeakReference<Context> = WeakReference(appContext)
    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(appContext)
    private val isPermissionGranted: Boolean
        private get() = (ActivityCompat.checkSelfPermission(wrContext.get()!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(wrContext.get()!!, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)

    private var locationRequest: LocationRequest? = null
    private var locationSettingsRequest: LocationSettingsRequest? = null
    private var isFirstRequest = true

    var resolvableApiException = MutableLiveData<ResolvableApiException>()
    var location = MutableLiveData<Location>()

    // MARK: Public Functions
    fun startUpdateLocation() {
        if (isFirstRequest) {
            isFirstRequest = false
            if (isPermissionGranted) {
                fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location -> this@GeolocationViewModel.location.value = location }
            }
        }
        locationRequest = createLocationRequest()
        locationSettingsRequest = buildLocationSettingsRequest(locationRequest)
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener {
                    if (isPermissionGranted) {
                        fusedLocationProviderClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                    }
                }
                .addOnFailureListener { e ->
                    when ((e as ApiException).statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            val exception = e as ResolvableApiException
                            resolvableApiException.postValue(exception)
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage = "Please open your location settings."
                            Log.e(TAG, errorMessage)
                        }
                    }
                }
    }

    fun stopLocationUpdates() {
        //stop location updates when  is no longer active
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "stopLocationUpdates: Successful")
            } else {
                Log.d(TAG, "stopLocationUpdates: " + Log.getStackTraceString(task.exception))
            }
        }
    }

    fun formatAccuracy(wording: String?, location: Location?): String {
        val accuracy: Float
        if (location != null) {
            accuracy = location.accuracy
            return String.format(wording!!, accuracy.toInt())
        }
        return ""
    }

    // MARK: Private Functions
    private fun createLocationRequest(): LocationRequest {
        // Create the location request to start receiving updates
        val locationRequest = LocationRequest()
        locationRequest.interval = UPDATE_INTERVAL
        locationRequest.fastestInterval = FASTEST_INTERVAL
        /*
         * PRIORITIES
         * PRIORITY_BALANCED_POWER_ACCURACY -
         * PRIORITY_HIGH_ACCURACY -
         * PRIORITY_LOW_POWER -
         * PRIORITY_NO_POWER -
         * */locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    private fun buildLocationSettingsRequest(locationRequest: LocationRequest?): LocationSettingsRequest {
        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest!!)
        return builder.build()
    }

    // MARK: Private variable
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            if (!locationAvailability.isLocationAvailable) {
//                stopLocationUpdates();
                if (isPermissionGranted) {
                    fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location -> this@GeolocationViewModel.location.value = location }
                            ?.addOnFailureListener { location.value = null }
                }
            }
        }

        override fun onLocationResult(locationResult: LocationResult) {
//            stopLocationUpdates();
            location.value = locationResult.lastLocation
        }
    }

    override fun onCleared() {
        stopLocationUpdates()
        super.onCleared()
    }
}