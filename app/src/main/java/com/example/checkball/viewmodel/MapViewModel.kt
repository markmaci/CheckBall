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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import com.example.checkball.BuildConfig
import javax.inject.Inject
import android.widget.Toast
import android.util.Log
import org.json.JSONArray

@HiltViewModel
class MapViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    var userLocation by mutableStateOf(LatLng(40.7128, -74.0060)) // Default to New York City
        private set

    private val _locationError = MutableStateFlow<LocationError?>(null)
    val locationError: StateFlow<LocationError?> = _locationError.asStateFlow()

    var basketballCourts by mutableStateOf<List<Place>>(emptyList())
        private set

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                viewModelScope.launch {
                    userLocation = LatLng(location.latitude, location.longitude)
                    fetchBasketballCourts()
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

    private suspend fun fetchBasketballCourts() {
        val apiKey = BuildConfig.API_KEY
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${userLocation.latitude},${userLocation.longitude}&radius=$RADIUS&type=gym&key=$apiKey"

        Log.d("MapViewModel", "Requesting URL: $url")

        try {
            val response = withContext(Dispatchers.IO) { URL(url).readText() }
            Log.d("MapViewModel", "API Response: $response")

            val results = JSONObject(response).optJSONArray("results") ?: JSONArray()

            val courts = mutableListOf<Place>()
            for (i in 0 until results.length()) {
                val result = results.getJSONObject(i)
                val name = result.optString("name", "Unknown Court")
                val location = result.getJSONObject("geometry").getJSONObject("location")
                val lat = location.getDouble("lat")
                val lng = location.getDouble("lng")
                courts.add(Place(name, LatLng(lat, lng)))
            }
            basketballCourts = courts

            Toast.makeText(context, "Fetched ${courts.size} basketball courts", Toast.LENGTH_SHORT).show()
            Log.d("MapViewModel", "Fetched ${courts.size} basketball courts")
        } catch (e: Exception) {
            _locationError.value = LocationError.OtherError(e)
            Log.e("MapViewModel", "Error fetching basketball courts: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val UPDATE_INTERVAL_MS = 5000L  // 5 seconds
        private const val FASTEST_UPDATE_INTERVAL_MS = 2000L  // 2 seconds
        private const val RADIUS = 2000 // Search within 2 km radius
    }
}

data class Place(val name: String, val location: LatLng)

sealed class LocationError {
    object PermissionDenied : LocationError()
    object LocationUnavailable : LocationError()
    data class OtherError(val exception: Exception) : LocationError()
}
