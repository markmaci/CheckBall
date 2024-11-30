package com.example.checkball.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.checkball.viewmodel.AuthViewModel
import com.example.checkball.viewmodel.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(navController: NavController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val mapViewModel: MapViewModel = hiltViewModel()
    val user by authViewModel.user.collectAsState()

    // Permissions handling
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
        position = CameraPosition.fromLatLngZoom(mapViewModel.userLocation, 12f)
    }

    LaunchedEffect(mapViewModel.userLocation) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(mapViewModel.userLocation, 12f),
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
                if (hasLocationPermissions) {
                    GoogleMapView(cameraPositionState = cameraPositionState, mapViewModel = mapViewModel)
                } else {
                    Text(
                        text = "Location permissions are not granted.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    )
}

@SuppressLint("MissingPermission")
@Composable
fun GoogleMapView(
    cameraPositionState: CameraPositionState,
    mapViewModel: MapViewModel
) {
    val uiSettings = remember { MapUiSettings(zoomControlsEnabled = true) }
    val properties = remember { MapProperties(isMyLocationEnabled = true) }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = properties
    ) {
        Marker(
            state = MarkerState(position = mapViewModel.userLocation),
            title = "You are here"
        )
    }
}
