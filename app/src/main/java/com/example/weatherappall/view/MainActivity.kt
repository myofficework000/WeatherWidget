package com.example.weatherappall.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherappall.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherappall.viewmodel.WeatherViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), LocationListener {


    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: WeatherViewModel
    private lateinit var adapter1: ForecastAdapter
    private lateinit var adapter3: PollutionAdapter
    private lateinit var locationManager: LocationManager
    var lat: Double? = null
    var lon: Double? = null
    private val locationPermissionCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        initView()
        getCurrentLocation()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        binding.ibCurrentLocation.setOnClickListener {
            getCurrentLocation()
            if (lat != null && lon != null) {
                viewModel.getCurrentLocationWeather(lat!!, lon!!)
                viewModel.getPollutionForecast(lat!!, lon!!)
            }
        }

        binding.ibSearch.setOnClickListener {
            BottomSheetFragment().apply {
                show(supportFragmentManager, BottomSheetFragment.TAG)
            }
        }


        viewModel.weatherLiveData.observe(this) {
            viewModel.getWeatherForecast(it.name)
            viewModel.getPollutionForecast(it.coord.lat, it.coord.lon)
            binding.tvTemp.text = it.main.temp.toString() + "℃"
            binding.tvCity.text = it.name
        }

        viewModel.forecastLiveData.observe(this) {
            adapter1 = ForecastAdapter(it.list)
            binding.rvForecast.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            binding.rvForecast.adapter = adapter1
        }

        viewModel.pollutionForecastLiveData.observe(this) {
            adapter3 = PollutionAdapter(it.list)
            binding.rvPollutionForecast.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            binding.rvPollutionForecast.adapter = adapter3
        }
    }


    private fun getCurrentLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                500f,
                this
            )
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onLocationChanged(location: Location) {
        lat = location.latitude
        lon = location.longitude

        viewModel.getCurrentLocationWeather(lat!!, lon!!)
        viewModel.getPollutionForecast(lat!!, lon!!)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}