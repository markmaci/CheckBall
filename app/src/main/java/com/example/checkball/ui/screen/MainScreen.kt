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
import com.example.checkball.viewmodel.AuthViewModel
import com.example.checkball.viewmodel.MapViewModel
import com.example.checkball.viewmodel.Place
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.example.checkball.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.checkball.R
import androidx.compose.ui.Alignment

val lacquierRegular = FontFamily(
    Font(R.font.lacquerregular, FontWeight.Normal)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(navController: NavHostController) {
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

    Box(modifier = Modifier.fillMaxSize()) {
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
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
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
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = Color(0xFF8B4513))
        } else {
            CardDefaults.cardColors(containerColor = Color(0xFFFFA500))
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = court.photoReferences?.firstOrNull()?.let {
                        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=$it&key=${BuildConfig.API_KEY}"
                    } ?: "https://via.placeholder.com/400",
                    contentDescription = "Photo of ${court.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .aspectRatio(1.25f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = court.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    text = "Address: ${court.location.latitude}, ${court.location.longitude}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
        }
    }
}
