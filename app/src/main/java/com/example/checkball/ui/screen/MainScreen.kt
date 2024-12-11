package com.example.checkball.ui.screen

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.example.checkball.ui.BottomNavigationBar
import com.example.checkball.viewmodel.AuthViewModel
import com.example.checkball.viewmodel.MapViewModel
import com.example.checkball.viewmodel.Place
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import com.example.checkball.R
import com.google.firebase.firestore.FirebaseFirestore
import com.example.checkball.di.UserProfile
import com.example.checkball.di.RecentStats

val lacquierRegular = FontFamily(
    Font(R.font.lacquerregular, FontWeight.Normal)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    userId: String? = null,
    firestore: FirebaseFirestore? = null
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val mapViewModel: MapViewModel = hiltViewModel()

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        locationPermissionsState.launchMultiplePermissionRequest()
    }

    val hasLocationPermissions = locationPermissionsState.allPermissionsGranted

    if (hasLocationPermissions) {
        LaunchedEffect(Unit) {
            mapViewModel.fetchUserLocation()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapViewModel.cameraLocation, mapViewModel.zoomLevel)
    }

    LaunchedEffect(mapViewModel.cameraInitialized) {
        if (mapViewModel.cameraInitialized) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(mapViewModel.cameraLocation, mapViewModel.zoomLevel),
                durationMs = 1000
            )
            mapViewModel.startFetchingCourts()
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            mapViewModel.onCameraMoved(cameraPositionState.position.target, cameraPositionState.position.zoom)
        }
    }

    var selectedCourt by remember { mutableStateOf<Place?>(null) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    if (userId != null && firestore != null) {
        LaunchedEffect(userId) {
            loading = true
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val data = document.data
                        userProfile = UserProfile(
                            displayName = data?.get("displayName") as? String ?: "Unknown",
                            uid = data?.get("uid") as? String ?: "",
                            username = data?.get("username") as? String ?: "Unknown",
                            location = data?.get("location") as? String ?: "Unknown",
                            height = data?.get("height") as? String ?: "Unknown",
                            weight = data?.get("weight") as? String ?: "Unknown",
                            preferredPosition = data?.get("preferredPosition") as? String ?: "Unknown",
                            favoriteCourt = data?.get("favoriteCourt") as? String ?: "Unknown",
                            recentStats = RecentStats(
                                wins = (data?.get("recentStats_wins") as? Long)?.toInt() ?: 0,
                                losses = (data?.get("recentStats_losses") as? Long)?.toInt() ?: 0,
                                pointsScored = (data?.get("recentStats_pointsScored") as? Long)?.toInt() ?: 0,
                                assists = (data?.get("recentStats_assists") as? Long)?.toInt() ?: 0,
                                rebounds = (data?.get("recentStats_rebounds") as? Long)?.toInt() ?: 0
                            ),
                            badges = (data?.get("badges") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        )
                    } else {
                        error = "User data not found."
                    }
                    loading = false
                }
                .addOnFailureListener { exception ->
                    error = "Error fetching user data: ${exception.localizedMessage}"
                    loading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CheckBall") },
                actions = {
                    Button(onClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }) {
                        Text("Log Out")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (hasLocationPermissions) {
                        GoogleMapView(
                            cameraPositionState = cameraPositionState,
                            mapViewModel = mapViewModel
                        )
                    } else {
                        Text(
                            text = "Location permissions are not granted.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                CourtDetailsColumn(
                    mapViewModel = mapViewModel,
                    onCardClick = { court ->
                        selectedCourt = court
                    }
                )
            }
        }
    )

    if (selectedCourt != null) {
        CourtDetailsScreen(
            court = selectedCourt,
            onDismiss = { selectedCourt = null }
        )
    }

    if (loading) {
        CircularProgressIndicator()
    } else if (error != null) {
        Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
    } else {
        userProfile?.let { profile ->
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Welcome, ${profile.displayName}")
                Text(text = "Username: ${profile.username}")
                Text(text = "Location: ${profile.location}")
                Text(text = "Height: ${profile.height}")
                Text(text = "Weight: ${profile.weight}")
                Text(text = "Preferred Position: ${profile.preferredPosition}")
                Text(text = "Favorite Court: ${profile.favoriteCourt}")

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Recent Stats:")
                Text(text = "Wins: ${profile.recentStats.wins}")
                Text(text = "Losses: ${profile.recentStats.losses}")
                Text(text = "Points Scored: ${profile.recentStats.pointsScored}")
                Text(text = "Assists: ${profile.recentStats.assists}")
                Text(text = "Rebounds: ${profile.recentStats.rebounds}")

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Badges: ${profile.badges.joinToString(", ")}")
            }
        } ?: run {
            Text(text = "No user data available")
        }
    }
}

@Composable
fun GoogleMapView(
    cameraPositionState: CameraPositionState,
    mapViewModel: MapViewModel
) {
    val uiSettings = remember { MapUiSettings(zoomControlsEnabled = true) }
    val properties = remember {
        MapProperties(
            isMyLocationEnabled = true,
            mapType = MapType.NORMAL
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = properties
    ) {
        mapViewModel.basketballCourts.forEach { court ->
            Marker(
                state = MarkerState(position = court.location),
                title = court.name,
                onClick = {
                    mapViewModel.selectCourt(court)
                    true
                }
            )
        }
    }
}

@Composable
fun CourtDetailsColumn(mapViewModel: MapViewModel, onCardClick: (Place) -> Unit) {
    val courts = mapViewModel.basketballCourts
    val selectedCourt = mapViewModel.selectedCourt

    if (courts.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFF2EFDE)),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)

        ) {
            itemsIndexed(courts) { _, court ->
                CourtDetailCard(
                    court = court,
                    isSelected = court == selectedCourt,
                    onClick = { onCardClick(court) }
                )
            }
        }
    } else {
        Text(
            text = "No basketball courts found.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun CourtDetailCard(
    court: Place,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.Gray else Color.White
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = court.name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = court.description,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = court.imageUrl,
                contentDescription = "Court Image",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CourtDetailsScreen(
    court: Place,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = court.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = court.description,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = court.imageUrl,
                    contentDescription = "Court Image",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
