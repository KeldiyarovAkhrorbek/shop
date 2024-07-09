package com.example.shop.Activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.shop.databinding.ActivityYandexBinding
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.location.LocationManagerUtils
import com.yandex.mapkit.location.LocationStatus


class YandexActivity : AppCompatActivity() {


    private lateinit var binding: ActivityYandexBinding

    private lateinit var mapKit: MapKit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("0d000129-e96e-45d1-b5ee-0dae0ce77457")
        MapKitFactory.initialize(this)

        binding = ActivityYandexBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapKit = MapKitFactory.getInstance()

    }


    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapview.onStart()
    }

    override fun onStop() {
        binding.mapview.onStart()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}