package com.vdcodeassociate.mapboxflowroute

import android.annotation.SuppressLint
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin

class MainActivity : AppCompatActivity(), PermissionsListener, LocationEngineListener{

    private lateinit var mapView : MapView

    private lateinit var map : MapboxMap

    private lateinit var permissionsManager: PermissionsManager
    private lateinit var originLocation: Location

    private var locationEngine: LocationEngine? = null
    private var locationLayerPlugin: LocationLayerPlugin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Mapbox.getInstance(applicationContext,getString(R.string.mapbox_access_token))
        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync{
            map = it
            enableLocation()
        }

    }

    private fun enableLocation(){
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            initializeLocationEngine()
        }else{
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocationEngine(){
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine?.activate()

        val lastLocation = locationEngine?.lastLocation
        if(lastLocation != null){
            originLocation = lastLocation
            setCameraLocation(lastLocation)
        }else {
            locationEngine?.addLocationEngineListener(this)
        }
    }

    private fun setCameraLocation(location : Location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
            LatLng(location.latitude,location.longitude),13.0))
    }

    @SuppressLint("MissingPermission", "WrongConstant")
    private fun initializeLocationLayer(){
        locationLayerPlugin = LocationLayerPlugin(mapView,map,locationEngine)
        locationLayerPlugin?.isLocationLayerEnabled = true
        locationLayerPlugin?.cameraMode = CameraMode.TRACKING
        locationLayerPlugin?.renderMode = RenderMode.NORMAL
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(applicationContext,"Location is needed!", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(
            requestCode,permissions,grantResults
        )
    }

    override fun onPermissionResult(granted: Boolean) {
        if(granted){
            enableLocation()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnected() {
        locationEngine?.requestLocationUpdates()
    }

    override fun onLocationChanged(location: Location?) {
        location.let {
            if (location != null) {
                originLocation = location
                setCameraLocation(location)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            locationEngine?.requestLocationUpdates()
            locationLayerPlugin?.onStart()
        }
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    @SuppressLint("MissingPermission")
    override fun onStop() {
        super.onStop()
        locationEngine?.requestLocationUpdates()
        locationLayerPlugin?.onStart()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (outState != null) {
            mapView?.onSaveInstanceState(outState)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        locationEngine?.deactivate()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

}
