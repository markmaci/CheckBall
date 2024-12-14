package com.example.checkball.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject
import com.example.checkball.BuildConfig

@HiltViewModel
class MapViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    var cameraLocation by mutableStateOf(LatLng(40.7128, -74.0060)) // Default to NYC
        private set

    var zoomLevel by mutableFloatStateOf(15f)
        private set

    var cameraInitialized by mutableStateOf(false)
        private set

    var basketballCourts by mutableStateOf<List<Place>>(emptyList())
        private set

    var selectedCourt by mutableStateOf<Place?>(null)
        private set

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val firestore: FirebaseFirestore = Firebase.firestore

    private var debounceJob: Job? = null

    @SuppressLint("MissingPermission")
    fun fetchUserLocation() {
        if (!hasLocationPermission()) return

        val locationRequest = LocationRequest.Builder(5000L)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.locations.firstOrNull()?.let {
                        cameraLocation = LatLng(it.latitude, it.longitude)
                        Log.d("MapViewModel", "User location fetched: $cameraLocation")
                        if (!cameraInitialized) {
                            cameraInitialized = true
                        }
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    fun startFetchingCourts() {
        fetchBasketballCourts()
    }

    fun onCameraMoved(newCameraLocation: LatLng, newZoomLevel: Float) {
        viewModelScope.launch {
            debounceJob?.cancel()
            debounceJob = launch {
                delay(500)
                Log.d("MapViewModel", "Camera moved to: $newCameraLocation, Zoom: $newZoomLevel")

                cameraLocation = newCameraLocation
                zoomLevel = newZoomLevel

                fetchBasketballCourts()
            }
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
                    if (nextPageToken != null) append("&pagetoken=$nextPageToken")
                }

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
                        val address = result.optString("vicinity")
                        val phoneNumber = result.optString("formatted_phone_number", null)
                        val website = result.optString("website", null)
                        val rating = result.optDouble("rating", Double.NaN).takeIf { !it.isNaN() }?.toFloat()
                        val userRatingsTotal = result.optInt("user_ratings_total", 0)
                        val photos = result.optJSONArray("photos")?.let { photosArray ->
                            List(photosArray.length()) { photosArray.getJSONObject(it).optString("photo_reference") }
                        }
                        val openingHours = result.optJSONObject("opening_hours")?.let { hours ->
                            OpeningHours(
                                openNow = hours.optBoolean("open_now", false),
                                weekdayText = hours.optJSONArray("weekday_text")?.let { weekdayArray ->
                                    List(weekdayArray.length()) { weekdayArray.getString(it) }
                                }
                            )
                        }

                        val court = Place(
                            name = name,
                            location = LatLng(lat, lng),
                            address = address,
                            phoneNumber = phoneNumber,
                            website = website,
                            rating = rating,
                            userRatingsTotal = userRatingsTotal,
                            photoReferences = photos,
                            openingHours = openingHours
                        )

                        checkAndAddParkToFirestore(court)

                        allCourts.add(court)
                    }

                    nextPageToken = jsonResponse.optString("next_page_token", null)
                    if (nextPageToken != null) delay(2000)
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error fetching basketball courts: ${e.message}")
                    break
                }
            } while (nextPageToken != null)

            basketballCourts = allCourts
            Toast.makeText(context, "Fetched ${allCourts.size} basketball courts", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndAddParkToFirestore(court: Place) {
        val parkRef = firestore.collection("parks").document("${court.location.latitude},${court.location.longitude}")

        parkRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val parkData = mapOf(
                    "name" to court.name,
                    "location" to mapOf(
                        "latitude" to court.location.latitude,
                        "longitude" to court.location.longitude
                    ),
                    "users" to emptyList<String>(),
                    "address" to court.address,
                    "rating" to court.rating,
                    "photoReferences" to court.photoReferences
                )
                parkRef.set(parkData).addOnSuccessListener {
                    Log.d("MapViewModel", "Park added to Firestore: ${court.name}")
                }.addOnFailureListener {
                    Log.e("MapViewModel", "Failed to add park: ${it.message}")
                }
            }
        }.addOnFailureListener {
            Log.e("MapViewModel", "Failed to check park: ${it.message}")
        }
    }

    fun selectCourt(court: Place) {
        selectedCourt = court
        basketballCourts = basketballCourts.sortedByDescending { it == court }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun calculateRadius(zoom: Float): Int {
        return when {
            zoom >= 16 -> 1000
            zoom >= 14 -> 2000
            zoom >= 12 -> 5000
            else -> 10000
        }
    }
}

data class Place(
    val name: String,
    val location: LatLng,
    val address: String? = null,
    val phoneNumber: String? = null,
    val website: String? = null,
    val rating: Float? = null,
    val userRatingsTotal: Int? = null,
    val photoReferences: List<String>? = null,
    val openingHours: OpeningHours? = null
)

data class OpeningHours(
    val openNow: Boolean,
    val weekdayText: List<String>? = null
)
