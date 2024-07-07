package com.example.shop.Activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.location.Location
import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.codebyashish.googledirectionapi.AbstractRouting
import com.codebyashish.googledirectionapi.ErrorHandling
import com.codebyashish.googledirectionapi.RouteDrawing
import com.codebyashish.googledirectionapi.RouteInfoModel
import com.codebyashish.googledirectionapi.RouteListener
import com.example.shop.R
import com.example.shop.RetrofitInstance
import com.example.shop.Shop
import com.example.shop.databinding.ActivityAddShopBinding
import com.example.shop.databinding.DialogViewBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.io.IOException

class AddShopActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: ActivityAddShopBinding
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var markerLocation: LatLng
    private var allShops: MutableList<Shop> = mutableListOf()

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddShopBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val i = intent
        allShops = i.getSerializableExtra("list") as MutableList<Shop>

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapfragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnAdd.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add point")
            val dialogViewBinding = DialogViewBinding.inflate(layoutInflater)
            dialogViewBinding.tvLocation.text =
                "Location: " + markerLocation.latitude.toString() + ", " + markerLocation.longitude.toString()
            builder.setView(dialogViewBinding.root)

            builder.setPositiveButton("Add") { dialog, which ->
                if (dialogViewBinding.edName.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please, enter name", Toast.LENGTH_SHORT
                    ).show()
                }
                if (dialogViewBinding.edContact.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please, enter contact person", Toast.LENGTH_SHORT
                    ).show()
                }

                if (dialogViewBinding.edNumber.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please, enter phone number", Toast.LENGTH_SHORT
                    ).show()
                }

                val newShop = Shop(
                    contact_person = dialogViewBinding.edContact.text.toString(),
                    id = 1,
                    isEnabled = true,
                    latitude = markerLocation.latitude,
                    longitude = markerLocation.longitude,
                    name = dialogViewBinding.edName.text.toString(),
                    phone = dialogViewBinding.edNumber.text.toString()
                )

                if (dialogViewBinding.edName.text.isNotEmpty() && dialogViewBinding.edNumber.text.isNotEmpty() && dialogViewBinding.edContact.text.isNotEmpty())
                    lifecycleScope.launch {
                        val response = try {
                            RetrofitInstance.api.addShop(newShop)
                        } catch (e: IOException) {
                            return@launch
                        } catch (e: HttpException) {
                            Log.e(TAG, "HTTPException")
                            return@launch
                        }

                        if (response.isSuccessful && response.body() != null) {
                            Toast.makeText(
                                this@AddShopActivity,
                                "Shop has been added!", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@AddShopActivity,
                                "Could not add shop", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }

            builder.setNegativeButton(android.R.string.no) { dialog, which -> }
            builder.show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.uiSettings.isZoomControlsEnabled = true
        setupMap()
    }

    private fun placeMarkerOnMap() {

        allShops.forEach { shop ->
            val markerOptions = MarkerOptions().position(LatLng(shop.latitude, shop.longitude))
            markerOptions.title(shop.name)
            mGoogleMap.addMarker(markerOptions)
        }
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
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 15f))
            }

        }

        placeMarkerOnMap()

        mGoogleMap.setOnCameraIdleListener {
            markerLocation = mGoogleMap.projection.fromScreenLocation(
                Point(binding.mapfragment.width / 2, binding.mapfragment.height / 2)
            )
        }

        mGoogleMap.setOnCameraMoveListener {
            markerLocation = mGoogleMap.projection.fromScreenLocation(
                Point(binding.mapfragment.width / 2, binding.mapfragment.height / 2)
            )
        }

    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return true
    }
}