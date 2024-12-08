package com.example.checkball.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.checkball.viewmodel.AuthViewModel
import com.example.checkball.viewmodel.MapViewModel
import com.example.checkball.viewmodel.Place
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.CameraUpdateFactory
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(navController: NavController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val mapViewModel: MapViewModel = hiltViewModel()
    val user by authViewModel.user.collectAsState()

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
        position = CameraPosition.fromLatLngZoom(mapViewModel.cameraLocation, 12f)
    }

    LaunchedEffect(mapViewModel.cameraLocation) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(mapViewModel.cameraLocation, mapViewModel.zoomLevel),
            durationMs = 1000
        )
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
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
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
                CourtDetailsRow(mapViewModel = mapViewModel)
            }
        }
    )
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
                title = court.name
            )
        }
    }
}

@Composable
fun CourtDetailsRow(mapViewModel: MapViewModel) {
    val courts = mapViewModel.basketballCourts

    if (courts.isNotEmpty()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            itemsIndexed(courts) { index, court ->
                CourtDetailCard(
                    court = court,
                    index = index
                )
            }
        }
    } else {
        Text(
            text = "No basketball courts found.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun CourtDetailCard(court: Place, index: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = "https://via.placeholder.com/150",
                contentDescription = "Image of ${court.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
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
            Text(
                text = "Hours: Open 6 AM - 10 PM", // Placeholder
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
