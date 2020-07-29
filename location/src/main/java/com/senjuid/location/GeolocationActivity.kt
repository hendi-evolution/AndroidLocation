package com.senjuid.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.senjuid.location.util.BaseActivity
import kotlinx.android.synthetic.main.activity_maps.*
import org.json.JSONException
import org.json.JSONObject

class GeolocationActivity : BaseActivity() {

    companion object {
        var PERMISSIONS_REQUEST_CODE = 1
        var PERMISSIONS_REQUEST_SETTINGS = 2
        private const val REQUEST_CHECK_SETTINGS = 231
    }

    var mPermissions: AndroidPermissions? = null
    var locationManager: LocationManager? = null
    private var mMap: GoogleMap? = null
    var mHeight = 0
    var geolocationViewModel: GeolocationViewModel? = null

    // Extras
    var workLocationData: String? = null
    var workLat = 0.0
    var workLon = 0.0

    // Label
    var label1: String? = null
    var label2: String? = null

    @SuppressLint("MissingPermission")
    private var onMapReadyCallback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        workLat = -6.174793 // default lat
        workLon = 106.827144 // default lon
        if (workLocationData != null) {
            try {
                val data = JSONObject(workLocationData)
                val locArray = data.getJSONArray("data")
                if (locArray != null && locArray.length() > 0) {
                    for (i in 0 until locArray.length()) {
                        addCompanyLocation(locArray.getJSONObject(i))

                        // set start location
                        if (i == 0) {
                            workLat = locArray.getJSONObject(i).optDouble("work_lat")
                            workLon = locArray.getJSONObject(i).optDouble("work_lon")
                        }
                    }
                }
            } catch (je: JSONException) {
            }
        }
        val sydney = LatLng(workLat, workLon)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16.0f))
        if (ActivityCompat.checkSelfPermission(this@GeolocationActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this@GeolocationActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            setupPermissions()
            return@OnMapReadyCallback
        }
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = false
        val displayMetrics = resources.displayMetrics
        mHeight = displayMetrics.heightPixels

        // get location update
        myLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        label1 = intent.getStringExtra("message1")
        label2 = intent.getStringExtra("message2")
        workLocationData = intent.getStringExtra("data")

        // check google api available
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        if (googleApiAvailability.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this, 404, 200) { dialog ->
                dialog.dismiss()
                finish()
            }.show()
            return
        }

        // create view model
        geolocationViewModel = ViewModelProvider(this, GeolocationViewModelFactory(applicationContext)).get(GeolocationViewModel::class.java)

        initComponent()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = (supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFragment.getMapAsync(onMapReadyCallback)
        val locationFoundRefresh = findViewById<ImageButton>(R.id.button_center_location)
        locationFoundRefresh.setOnClickListener { setupPermissions() }
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        // observe live data
        observeLiveData()

        // hide loading
        showHideLoading(true)
    }

    // Add company location  marker and radius
    @Throws(JSONException::class)
    private fun addCompanyLocation(data: JSONObject) {
        val companyLocation = LatLng(data.getDouble("work_lat"), data.getDouble("work_lon"))

        // Add circle
        if (data.getDouble("work_radius") > 0) { // add circle radius only if geo fencing active

            //add marker
            mMap!!.addMarker(MarkerOptions()
                    .position(companyLocation)
                    .title(getString(R.string.your_company))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_company_marker)))
            val fillColor = 0x4400FF00
            val radius = data.getDouble("work_radius") * 1000
            mMap!!.addCircle(CircleOptions()
                    .center(companyLocation)
                    .radius(radius)
                    .strokeColor(Color.GREEN)
                    .strokeWidth(2f)
                    .fillColor(fillColor))
        }
    }

    private fun observeLiveData() {
        // observe location update
        geolocationViewModel?.location?.observe(this, Observer { location -> setMyLocation(location) })

        // observe high accuracy
        geolocationViewModel?.resolvableApiException?.observe(this, Observer { e ->
            if (e != null) {
                try {
                    e.startResolutionForResult(this@GeolocationActivity, REQUEST_CHECK_SETTINGS)

                    // show message
                    showHideLoading(false)
                    if (intent.getStringExtra("message2") != null) {
                        textView_wrong_location.text = intent.getStringExtra("message2")
                    } else {
                        textView_wrong_location.text = getString(R.string.str_mod_loc_high_accuracy)
                    }
                } catch (ex: SendIntentException) {
                    ex.printStackTrace()
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            geolocationViewModel!!.startUpdateLocation()
        } else {
            showHideLoading(false)
            setMyLocation(null)
        }
    }

    fun myLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        showHideLoading(true)
        geolocationViewModel!!.startUpdateLocation()
    }

    @SuppressLint("MissingPermission")
    fun setMyLocation(location: Location?) {
        if (location == null || mMap == null) {
            hideComponent()
            return
        }

        // My location marker
        val myLocation = LatLng(location.latitude, location.longitude)
        // show map point
        val mapPoint = mMap!!.projection.toScreenLocation(myLocation)
        mapPoint[mapPoint.x] = mapPoint.y // change these values as you need , just hard coded a value if you want you can give it based on a ratio like using DisplayMetrics  as well
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap!!.projection.fromScreenLocation(mapPoint), 16.0f))
        mMap!!.isMyLocationEnabled = true

        // set accuracy
        tv_accuracy.text = geolocationViewModel!!.formatAccuracy(getString(R.string.str_accuracy), location)
        showComponent()
    }

    private fun initComponent() {
//        textView_oops_location = findViewById(R.id.textView_oops_location)
//        textView_wrong_location = findViewById(R.id.textView_wrong_location)
//        tvSearching = findViewById(R.id.tv_state)
//        button_solution_location = findViewById(R.id.button_solution_location)
//        layoutLocationFound = findViewById(R.id.layout_location_found)
//        layoutLocationNotFound = findViewById(R.id.layout_location_not_found)
//        tvAccuracy = findViewById(R.id.tv_accuracy)
//        textView_location_maps_found_title = findViewById(R.id.textView_location_maps_found_title)

        if (!intent.getStringExtra("message1").isNullOrEmpty()) {
            textView_location_maps_found_title.text = intent.getStringExtra("message1")
        }

//        button_location_maps_found_yes = findViewById(R.id.button_location_maps_found_yes)
//        button_location_maps_found_refresh = findViewById(R.id.button_location_maps_found_refresh)
        button_location_maps_found_yes.setOnClickListener {
            val location = geolocationViewModel?.location?.value
            if (location != null) { // make sure location not null
                if (location.isFromMockProvider) {
                    AlertDialog.Builder(this@GeolocationActivity)
                            .setTitle(R.string.str_warning)
                            .setMessage(R.string.str_location_spoofing_message)
                            .setPositiveButton(R.string.str_continue) { dialog, which -> locationFoundAndFinish(location) }
                            .setNegativeButton(R.string.str_dismiss, null)
                            .create()
                            .show()
                } else {
                    locationFoundAndFinish(location)
                }
            }
        }

        button_location_maps_found_refresh.setOnClickListener { onRefreshButtonPressed() }
        button_solution_location.setOnClickListener { setupPermissions() }
    }

    private fun locationFoundAndFinish(location: Location) {
        val intent = Intent()
        intent.putExtra("lon", location.longitude)
        intent.putExtra("lat", location.latitude)
        intent.putExtra("isMock", location.isFromMockProvider)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun showHideLoading(loading: Boolean) {
        if (loading) {
            tv_state.text = getString(R.string.str_mod_loc_searching)
            tv_state.visibility = View.VISIBLE
            layout_location_not_found.visibility = View.GONE
            layout_location_found.visibility = View.GONE
        } else {
            tv_state.visibility = View.GONE
        }
    }

    private fun hideComponent() {
        layout_location_not_found.visibility = View.VISIBLE
        layout_location_found.visibility = View.GONE
        tv_state.visibility = View.GONE
    }

    private fun showComponent() {
        layout_location_not_found.visibility = View.GONE
        layout_location_found.visibility = View.VISIBLE
        tv_state.visibility = View.GONE
    }

    private fun onRefreshButtonPressed() {
        setupPermissions()
    }

    private fun setupPermissions() {
        mPermissions = AndroidPermissions(this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        )
        if (mPermissions!!.checkPermissions()) {
            if (checkLocation()) {
                myLocation()
            }
        } else {
            mPermissions!!.requestPermissions(PERMISSIONS_REQUEST_CODE)
        }
    }

    // Check GPS is active or not
    private val isLocationEnabled: Boolean
        private get() {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

    private fun checkLocation(): Boolean {
        if (!isLocationEnabled) showAlert()
        return isLocationEnabled
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog
                .setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'. Please Enable Location to use this app")
                .setCancelable(false)
                .setPositiveButton("Location Settings") { _, _ ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(myIntent, PERMISSIONS_REQUEST_SETTINGS)
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (mPermissions!!.areAllRequiredPermissionsGranted(permissions, grantResults)) {
                setupPermissions()
            } else {
                onInsufficientPermissions()
            }
        } else if (requestCode == PERMISSIONS_REQUEST_SETTINGS) {
            setupPermissions()
        }
    }

    private fun onInsufficientPermissions() {
        finish()
    }
}