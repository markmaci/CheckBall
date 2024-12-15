package com.example.checkball.ui.screen

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import com.example.checkball.BuildConfig
import com.example.checkball.R
import com.example.checkball.viewmodel.MapViewModel
import com.example.checkball.viewmodel.Place
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
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

    val coroutineScope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var selectedCourt by remember { mutableStateOf<Place?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .background(Color(0xFFF2EFDE))
    ) {
        if (hasLocationPermissions) {
            GoogleMapView(
                cameraPositionState = cameraPositionState,
                mapViewModel = mapViewModel,
                onCourtSelected = { court ->
                    selectedCourt = court
                    showSheet = true
                }
            )
        } else {
            Text(
                text = "Location permissions are not granted.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        PullTab(
            showSheet = showSheet,
            onClick = { showSheet = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    if (showSheet) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        ModalBottomSheet(
            modifier = Modifier
                .padding(top = 50.dp),
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color(0xFFF2EFDE),
            dragHandle = {
                Icon(
                    imageVector = if (showSheet) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = "Drag Handle",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { showSheet = false }
                )
            }
        ) {
            CourtBottomSheetContent(
                mapViewModel = mapViewModel,
                onCourtClick = { court ->
                    selectedCourt = court
                    coroutineScope.launch {
                        sheetState.hide()
                        showSheet = false
                        showDetails = true
                    }
                }
            )
        }
    }

    if (showDetails && selectedCourt != null) {
        CourtDetailsScreen(
            court = selectedCourt,
            onDismiss = { showDetails = false; selectedCourt = null }
        )
    }
}

@Composable
fun GoogleMapView(
    cameraPositionState: CameraPositionState,
    mapViewModel: MapViewModel,
    onCourtSelected: (Place) -> Unit
) {
    val context = LocalContext.current
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
        )
    }
    val mapStyleOptions = remember {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    }
    val properties = remember {
        MapProperties(
            isMyLocationEnabled = true,
            mapStyleOptions = mapStyleOptions,
        )
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = properties,
    ) {
        mapViewModel.basketballCourts.forEach { court ->
            Marker(
                state = MarkerState(position = court.location),
                title = court.name,
                onClick = {
                    onCourtSelected(court)
                    mapViewModel.selectCourt(court)
                    true
                },
                icon =  BitmapDescriptorFactory.fromResource(R.drawable.basketball_map_icon)
            )
        }
    }
}

@Composable
fun PullTab(showSheet: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = Color(0xFFFFA500),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .height(40.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (showSheet) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = if (showSheet) "Collapse" else "Expand",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Courts Available",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtBottomSheetContent(
    mapViewModel: MapViewModel,
    onCourtClick: (Place) -> Unit
) {
    val courts = mapViewModel.basketballCourts

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            contentAlignment = Alignment.Center
        ) {
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (courts.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(courts) { _, court ->
                    CourtDetailCard(
                        court = court,
                        isSelected = court == mapViewModel.selectedCourt,
                        onClick = { onCourtClick(court) }
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
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF8B4513) else Color(0xFFFFA500)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = court.photoReferences?.firstOrNull()?.let {
                    "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=$it&key=${BuildConfig.API_KEY}"
                } ?: "https://via.placeholder.com/400",
                contentDescription = "Photo of ${court.name}",
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(2f)
            ) {
                Text(
                    text = court.name,
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Address: ${court.address ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                    maxLines = 1
                )
            }
        }
    }
}
