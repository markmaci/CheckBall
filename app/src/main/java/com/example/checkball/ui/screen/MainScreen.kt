package com.example.checkball.ui.screen

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import com.example.checkball.BuildConfig
import com.example.checkball.R
import com.example.checkball.ui.component.CourtDetailsBottomSheet
import com.example.checkball.viewmodel.MapViewModel
import com.example.checkball.viewmodel.Place
import com.example.checkball.viewmodel.distanceInMeters
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null

    val width = 128
    val height = 128

    vectorDrawable.setBounds(0, 0, width, height)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(800), repeatMode = RepeatMode.Reverse
            ), label = ""
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}


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
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            mapViewModel.onCameraMoved(cameraPositionState.position.target, cameraPositionState.position.zoom)
        }
    }

    var lastFetchedCenter by remember { mutableStateOf<LatLng?>(null) }
    var lastFetchedRadius by remember { mutableStateOf<Int?>(null) }

    var showSheet by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var selectedCourt by remember { mutableStateOf<Place?>(null) }

    LaunchedEffect(cameraPositionState.position) {
        delay(300)

        if (showDetails) {
            return@LaunchedEffect
        }

        if (!cameraPositionState.isMoving) {
            val projection = cameraPositionState.projection
            if (projection != null) {
                val visibleRegion = projection.visibleRegion
                val mapCenter = cameraPositionState.position.target
                val rawRadius = distanceInMeters(mapCenter, visibleRegion.farRight)
                val radius = rawRadius.coerceAtLeast(300)

                val radiusThreshold = 100
                val positionThreshold = 100

                val shouldFetch = when {
                    lastFetchedCenter == null || lastFetchedRadius == null -> true
                    distanceInMeters(lastFetchedCenter!!, mapCenter) > positionThreshold -> true
                    kotlin.math.abs(lastFetchedRadius!! - radius) > radiusThreshold -> true
                    else -> false
                }

                if (shouldFetch) {
                    mapViewModel.fetchBasketballCourts(radius)
                    lastFetchedCenter = mapCenter
                    lastFetchedRadius = radius
                }
            }
        }
    }


    val coroutineScope = rememberCoroutineScope()



    Box(
        modifier = Modifier.fillMaxSize()
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
                .padding(top = 50.dp)
                .heightIn(min = 300.dp),
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color(0xFFF2EFDE),
            scrimColor = Color.Transparent
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

    LaunchedEffect(showDetails, selectedCourt) {
        if (showDetails && selectedCourt != null) {
            delay(300)

            val offsetLatitude = selectedCourt!!.location.latitude - 0.0005
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(offsetLatitude, selectedCourt!!.location.longitude),
                    19f
                ),
                durationMs = 1000
            )
        }
    }

    if (showDetails && selectedCourt != null) {
        CourtDetailsBottomSheet(
            court = selectedCourt!!,
            onDismiss = {
                showDetails = false
                selectedCourt = null
            }
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
            .fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = properties,
        contentPadding = PaddingValues(top = 40.dp)
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
                icon = bitmapDescriptorFromVector(context, R.drawable.basketball_map_icon)
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
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
    var isImageLoading by remember { mutableStateOf(true) }

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
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shimmerBrush(showShimmer = isImageLoading, targetValue = 1300f))
            ) {
                AsyncImage(
                    model = court.photoReferences?.firstOrNull()?.let {
                        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=$it&key=${BuildConfig.API_KEY}"
                    } ?: "https://via.placeholder.com/400",
                    contentDescription = "Photo of ${court.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onSuccess = { isImageLoading = false },
                    onError = { isImageLoading = false }
                )
            }

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
