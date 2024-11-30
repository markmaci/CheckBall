// MapViewModel.kt
package com.example.checkball.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    var userLocation by mutableStateOf(LatLng(40.7128, -74.0060)) // Default to New York City
        private set

    private val _locationError = MutableStateFlow<LocationError?>(null)
    val locationError: StateFlow<LocationError?> = _locationError.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                viewModelScope.launch {
                    userLocation = LatLng(location.latitude, location.longitude)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _locationError.value = LocationError.PermissionDenied
            return
        }

        val locationRequest = LocationRequest.Builder(UPDATE_INTERVAL_MS)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_MS)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            _locationError.value = LocationError.OtherError(e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val UPDATE_INTERVAL_MS = 5000L  // 5 seconds
        private const val FASTEST_UPDATE_INTERVAL_MS = 2000L  // 2 seconds
    }
}

sealed class LocationError {
    object PermissionDenied : LocationError()
    object LocationUnavailable : LocationError()
    data class OtherError(val exception: Exception) : LocationError()
}