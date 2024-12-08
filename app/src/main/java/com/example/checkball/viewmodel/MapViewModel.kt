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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import org.json.JSONObject
import org.json.JSONArray
import java.net.URL
import android.widget.Toast
import android.util.Log
import com.example.checkball.BuildConfig
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    var cameraLocation by mutableStateOf(LatLng(40.7128, -74.0060)) // Default to New York City
        private set

    var zoomLevel by mutableStateOf(15f)
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
                Log.d(
                    "MapViewModel",
                    "User location updated: ${location.latitude}, ${location.longitude}"
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchUserLocation() {
        if (!hasLocationPermission()) {
            Log.e("MapViewModel", "Location permissions not granted.")
            _locationError.value = LocationError.PermissionDenied
            return
        }

        Log.d("MapViewModel", "Permissions granted. Fetching user location.")
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
        } catch (e: SecurityException) {
            Log.e("MapViewModel", "SecurityException when requesting location updates: ${e.message}")
            _locationError.value = LocationError.OtherError(e)
        }
    }

    fun onCameraMoved(newCameraLocation: LatLng, newZoomLevel: Float) {
        Log.d("MapViewModel", "Camera moved. New location: $newCameraLocation, Zoom: $newZoomLevel")

        if (newCameraLocation != cameraLocation || newZoomLevel != zoomLevel) {
            cameraLocation = newCameraLocation
            zoomLevel = newZoomLevel
            fetchBasketballCourts()
        }
    }

    private fun fetchBasketballCourts() {
        val radius = calculateRadius(zoomLevel)
        val apiKey = BuildConfig.API_KEY
        val baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"

        viewModelScope.launch {
            val allCourts = mutableListOf<Place>()
            var nextPageToken: String? = null

            do {
                val url = buildString {
                    append("$baseUrl?location=${cameraLocation.latitude},${cameraLocation.longitude}")
                    append("&radius=$radius&type=park&keyword=basketball%20court&key=$apiKey")
                    if (nextPageToken != null) {
                        append("&pagetoken=$nextPageToken")
                    }
                }

                Log.d("MapViewModel", "Requesting courts with URL: $url")

                try {
                    val response = withContext(Dispatchers.IO) { URL(url).readText() }
                    val jsonResponse = JSONObject(response)

                    val results = jsonResponse.optJSONArray("results") ?: JSONArray()
                    for (i in 0 until results.length()) {
                        val result = results.getJSONObject(i)
                        val name = result.optString("name", "Unknown Court")
                        val location = result.getJSONObject("geometry").getJSONObject("location")
                        val lat = location.getDouble("lat")
                        val lng = location.getDouble("lng")
                        allCourts.add(Place(name, LatLng(lat, lng)))
                    }

                    nextPageToken = jsonResponse.optString("next_page_token", null)
                    Log.d("MapViewModel", "Next page token: $nextPageToken")

                    if (nextPageToken != null) {
                        delay(2000)
                    }
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error fetching basketball courts: ${e.message}")
                    _locationError.value = LocationError.OtherError(e)
                    break
                }
            } while (nextPageToken != null)

            basketballCourts = allCourts
            Log.d("MapViewModel", "Fetched a total of ${allCourts.size} basketball courts.")
            Toast.makeText(context, "Fetched ${allCourts.size} basketball courts", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateRadius(zoom: Float): Int {
        val radius = when {
            zoom >= 16 -> 1000
            zoom >= 14 -> 2000
            zoom >= 12 -> 5000
            zoom >= 10 -> 10000
            else -> 20000
        }
        Log.d("MapViewModel", "Calculated radius for zoom level $zoom: $radius meters")
        return radius
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        val hasPermission = fineLocation == PackageManager.PERMISSION_GRANTED && coarseLocation == PackageManager.PERMISSION_GRANTED
        Log.d("MapViewModel", "Location permissions granted: $hasPermission")
        return hasPermission
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("MapViewModel", "Location updates removed.")
    }

    companion object {
        private const val UPDATE_INTERVAL_MS = 5000L
        private const val FASTEST_UPDATE_INTERVAL_MS = 2000L
    }
}

data class Place(val name: String, val location: LatLng)

sealed class LocationError {
    object PermissionDenied : LocationError()
    object LocationUnavailable : LocationError()
    data class OtherError(val exception: Exception) : LocationError()
}
