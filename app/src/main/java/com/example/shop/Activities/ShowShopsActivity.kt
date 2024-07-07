package com.example.shop.Activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.codebyashish.googledirectionapi.AbstractRouting
import com.codebyashish.googledirectionapi.ErrorHandling
import com.codebyashish.googledirectionapi.RouteDrawing
import com.codebyashish.googledirectionapi.RouteInfoModel
import com.codebyashish.googledirectionapi.RouteListener
import com.example.shop.R
import com.example.shop.Shop
import com.example.shop.databinding.ActivityShowShopsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit


class ShowShopsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    RouteListener {

    private lateinit var binding: ActivityShowShopsBinding
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var enabledShops: MutableList<Shop> = mutableListOf()
    private var polyline: MutableList<Polyline> = mutableListOf()

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowShopsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val i = intent
        enabledShops = i.getSerializableExtra("list") as MutableList<Shop>

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapfragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.uiSettings.isZoomControlsEnabled = true
        setupMap()
    }

    private fun setupMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE
            )
            return
        }
        mGoogleMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLong)
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
            }

        }

        mGoogleMap.setOnCameraIdleListener {

        }

        mGoogleMap.setOnCameraMoveListener {
            val newPos = mGoogleMap.projection.fromScreenLocation(
                Point(binding.mapfragment.width / 2, binding.mapfragment.height / 2)
            )
//            val markerOptions = MarkerOptions().position(newPos)
//            markerOptions.title("My location")
//            mGoogleMap.addMarker(markerOptions)
        }

    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("My location")
        mGoogleMap.addMarker(markerOptions)

        enabledShops.forEach { shop ->
            val markerOptions = MarkerOptions().position(LatLng(shop.latitude, shop.longitude))
            markerOptions.title(shop.name)
            mGoogleMap.addMarker(markerOptions)
        }

        val currentLocation = Shop(
            contact_person = "John Doe",
            id = 1,
            isEnabled = true,
//            latitude = currentLatLong.latitude,
//            longitude = currentLatLong.longitude,
            latitude = 41.3347062432484,
            longitude = 69.22088196688297,
            name = "Example Shop",
            phone = "+1234567890"
        )

        enabledShops.add(currentLocation)

        for (i in 0 until enabledShops.count() - 1) {
            val routeDrawing = RouteDrawing.Builder()
                .context(this)
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this).alternativeRoutes(true)
                .waypoints(
                    LatLng(enabledShops[i].latitude, enabledShops[i].longitude),
                    LatLng(enabledShops[i + 1].latitude, enabledShops[i + 1].longitude),
                )
                .build()
            routeDrawing.execute()
        }


    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return true
    }

    override fun onRouteFailure(p0: ErrorHandling?) {
        if (p0 != null)
            Toast.makeText(this, p0.message, Toast.LENGTH_SHORT).show() else {
            Toast.makeText(this, "Could not draw route", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRouteStart() {

    }

    override fun onRouteSuccess(
        routeInfoModelArrayList: ArrayList<RouteInfoModel>?,
        routeIndexing: Int
    ) {
        val polylineOptions = PolylineOptions()
        val polylines = ArrayList<Polyline>()
        if (routeInfoModelArrayList == null) {
            Toast.makeText(this, "Could not draw route", Toast.LENGTH_SHORT).show()
        }
        for (i in 0 until routeInfoModelArrayList!!.count()) {
            if (i == routeIndexing) {
                Log.e("TAG", "onRoutingSuccess: routeIndexing$routeIndexing")
                polylineOptions.color(Color.BLACK)
                polylineOptions.width(12f)
                polylineOptions.addAll(routeInfoModelArrayList.get(routeIndexing).getPoints())
                polylineOptions.startCap(RoundCap())
                polylineOptions.endCap(RoundCap())
                val polyline: Polyline = mGoogleMap.addPolyline(polylineOptions)
                polylines.add(polyline)
            }
        }
    }

    override fun onRouteCancelled() {
        Toast.makeText(this, "Draw route cancel", Toast.LENGTH_SHORT).show()
    }
}