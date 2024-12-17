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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class MapViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val MAX_RETRIES = 5
        private const val INITIAL_DELAY = 1000L
        private const val MULTIPLIER = 2.0
    }

    private val context = application.applicationContext

    private val screenWidth: Int by lazy {
        val displayMetrics = context.resources.displayMetrics
        displayMetrics.widthPixels
    }

    var cameraLocation by mutableStateOf(LatLng(42.350472, -71.106491)) // Default to Marsh Plaza
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

    private fun checkAndAddParkToFirestore(court: Place) {
        val parkRef = firestore.collection("parks")
            .document("${court.location.latitude},${court.location.longitude}")

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
        return ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED
    }


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


    fun onCameraMoved(newCameraLocation: LatLng, newZoomLevel: Float) {
        viewModelScope.launch {
            debounceJob?.cancel()
            debounceJob = launch {
                delay(500)
                Log.d("MapViewModel", "Camera moved to: $newCameraLocation, Zoom: $newZoomLevel")

                cameraLocation = newCameraLocation
                zoomLevel = newZoomLevel

            }
        }
    }

    private fun isOverQueryLimit(jsonResponse: JSONObject): Boolean {
        val status = jsonResponse.optString("status")
        return status.equals("OVER_QUERY_LIMIT", ignoreCase = true)
    }

    fun fetchBasketballCourts(radius: Int) {
        val apiKey = BuildConfig.API_KEY
        val baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"

        viewModelScope.launch {
            val allCourts = mutableListOf<Place>()
            var nextPageToken: String? = null
            var retryCount = 0
            var delayTime = INITIAL_DELAY

            do {
                val url = buildString {
                    append("$baseUrl?location=${cameraLocation.latitude},${cameraLocation.longitude}")
                    append("&radius=$radius")
                    append("&type=park+or+basketball_court")
                    append("&keyword=basketball%20court")
                    append("&key=$apiKey")
                    if (nextPageToken != null) append("&pagetoken=$nextPageToken")
                }

                Log.d("MapViewModel", "Fetching URL: $url")

                try {
                    val response = withContext(Dispatchers.IO) { URL(url).readText() }

                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.optString("status")
                    Log.d("MapViewModel", "API Response Status: $status")

                    if (isOverQueryLimit(jsonResponse)) {
                        if (retryCount < MAX_RETRIES) {
                            Log.w("MapViewModel", "Over query limit. Retrying in $delayTime ms.")
                            delay(delayTime)
                            retryCount++
                            delayTime = (delayTime * MULTIPLIER).toLong()
                            continue
                        } else {
                            Log.e(
                                "MapViewModel",
                                "Exceeded maximum retry attempts due to rate limits."
                            )
                            Toast.makeText(
                                context,
                                "Too many requests. Please try again later.",
                                Toast.LENGTH_LONG
                            ).show()
                            break
                        }
                    } else {
                        retryCount = 0
                        delayTime = INITIAL_DELAY
                    }

                    val results = jsonResponse.optJSONArray("results") ?: JSONArray()
                    Log.d("MapViewModel", "Number of results: ${results.length()}")

                    for (i in 0 until results.length()) {
                        val result = results.getJSONObject(i)
                        val name = result.optString("name", "Unknown Court")
                        val location = result.getJSONObject("geometry").getJSONObject("location")
                        val placeId = result.optString("place_id")
                        val lat = location.getDouble("lat")
                        val lng = location.getDouble("lng")
                        val address = result.optString("vicinity")
                        val phoneNumber = result.optString("formatted_phone_number", null)
                        val website = result.optString("website", null)
                        val rating =
                            result.optDouble("rating", Double.NaN).takeIf { !it.isNaN() }?.toFloat()
                        val userRatingsTotal = result.optInt("user_ratings_total", 0)
                        val photos = result.optJSONArray("photos")?.let { photosArray ->
                            List(photosArray.length()) {
                                photosArray.getJSONObject(it).optString("photo_reference")
                            }
                        }

                        if (photos.isNullOrEmpty()) {
                            Log.d("MapViewModel", "Skipping place without photos: $name")
                            continue
                        }

                        val openingHours = result.optJSONObject("opening_hours")?.let { hours ->
                            OpeningHours(
                                openNow = hours.optBoolean("open_now", false),
                                weekdayText = hours.optJSONArray("weekday_text")
                                    ?.let { weekdayArray ->
                                        List(weekdayArray.length()) { weekdayArray.getString(it) }
                                    }
                            )
                        }

                        val court = Place(
                            name = name,
                            location = LatLng(lat, lng),
                            placeId = placeId,
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
                        Log.d("MapViewModel", "Added court: $name")
                    }

                    nextPageToken = jsonResponse.optString("next_page_token", null)
                    if (nextPageToken != null) {
                        Log.d("MapViewModel", "Next page token: $nextPageToken")
                        delay(2000)
                    }
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error fetching basketball courts: ${e.message}")
                    Toast.makeText(
                        context,
                        "Error fetching courts. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    break
                }
            } while (nextPageToken != null)

            basketballCourts = allCourts
            Toast.makeText(
                context,
                "Fetched ${allCourts.size} basketball courts",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("MapViewModel", "Total courts fetched: ${allCourts.size}")
        }


    }

    suspend fun fetchPlaceDetailsPhotos(placeId: String): List<String> {
        val apiKey = BuildConfig.API_KEY
        val detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json?" +
                "place_id=$placeId&fields=photos&key=$apiKey"

        return withContext(Dispatchers.IO) {
            try {
                val response = URL(detailsUrl).readText()
                val jsonResponse = JSONObject(response)
                val result = jsonResponse.optJSONObject("result") ?: JSONObject()
                val photosArray = result.optJSONArray("photos") ?: JSONArray()

                val photoReferences = mutableListOf<String>()
                for (i in 0 until photosArray.length()) {
                    val photoRef = photosArray.getJSONObject(i).optString("photo_reference")
                    if (photoRef.isNotEmpty()) {
                        photoReferences.add(photoRef)
                    }
                }
                photoReferences
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching place details: ${e.message}")
                emptyList<String>()
            }
        }
    }
}

    data class Place(
        val name: String,
        val location: LatLng,
        val placeId: String? = null,
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

fun distanceInMeters(from: LatLng, to: LatLng): Int {
    val r = 6371000
    val dLat = Math.toRadians(to.latitude - from.latitude)
    val dLng = Math.toRadians(to.longitude - from.longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
            sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return (r * c).toInt()
}
