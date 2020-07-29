package com.senjuid.androidlocation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.senjuid.location.LocationPlugin
import com.senjuid.location.LocationPlugin.LocationPluginListener
import com.senjuid.location.LocationPluginOptions
import com.senjuid.location.util.LocaleHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var locationPlugin: LocationPlugin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set language
        LocaleHelper.setLocale(this, "in")

        // init plugin
        locationPlugin = LocationPlugin(this)
        locationPlugin?.setLocationPluginListener(object : LocationPluginListener {
            override fun onLocationRetrieved(lon: Double?, lat: Double?, isMock: Boolean?) {
                val address = locationPlugin?.getCompleteAddress(lon!!, lat!!)
                val sb = StringBuilder()
                sb.append("Longitude: ").append(lon).append("\n\n")
                sb.append("Latitude: ").append(lat).append("\n\n")
                sb.append("Fake location: ").append(isMock).append("\n\n")
                sb.append("Address: ").append(address)
                tv_result.text = sb.toString()
            }

            override fun onCanceled() {
                Toast.makeText(this@MainActivity, "Getting location canceled", Toast.LENGTH_LONG).show()
            }
        })
        btn_get_location.setOnClickListener {
            val dataDummy = "{data:[{work_lat: -6.283693, work_lon: 106.725430, work_radius: 0.4 },{work_lat: -6.175110, work_lon: 106.865036, work_radius: 0.5 }]}"
            val options = LocationPluginOptions.Builder()
                    .setData(dataDummy)
                    .build()
            locationPlugin?.open(options)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        locationPlugin?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}