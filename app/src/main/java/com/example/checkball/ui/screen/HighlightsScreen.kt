package com.example.checkball.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.checkball.BuildConfig
import com.example.checkball.R
import com.example.checkball.viewmodel.MapViewModel
import com.example.checkball.viewmodel.Place
import com.example.checkball.viewmodel.UserProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun HighlightsScreen() {
    val mapViewModel: MapViewModel = hiltViewModel()
    var basketballCourts by remember { mutableStateOf(listOf<Place>()) }
    var selectedCourt by remember { mutableStateOf<Place?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch courts initially
    LaunchedEffect(Unit) {
        isLoading = true
        fetchCourtsWithPhotos(mapViewModel) { courtsWithPhotos ->
            basketballCourts = courtsWithPhotos
            isLoading = false
        }
    }
    // Transition between pages and the page format of this file
    Crossfade(targetState = selectedCourt, animationSpec = tween(800)) { court ->
        if (court == null) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Parks Highlights",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = lacquierRegular,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color(0xFF8B4513)
                    )

                    IconButton(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                fetchCourtsWithPhotos(mapViewModel) { courtsWithPhotos ->
                                    basketballCourts = courtsWithPhotos
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFF8B4513)
                        )
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF8B4513))
                    }
                } else if (basketballCourts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Text(
                            text = "No parks found nearby.",
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(basketballCourts) { _, court ->
                            CourtCard(
                                court = court,
                                onClick = { selectedCourt = court }
                            )
                        }
                    }
                }
            }
        } else {
            // Uses the other kt file to go to the details of the highlights screen
            HighlightsDetailsCard(
                court = court,
                onBack = { selectedCourt = null },
                userProfileViewModel = UserProfileViewModel()
            )
        }
    }
}

// Helper function to get court photos
private suspend fun fetchCourtsWithPhotos(
    mapViewModel: MapViewModel,
    radius: Int = 2500,
    onCourtsFetched: (List<Place>) -> Unit
) {
    mapViewModel.fetchUserLocation()
    mapViewModel.fetchBasketballCourts(radius)

    val courtsWithPhotos = mapViewModel.basketballCourts.map { court ->
        val photoReferences = court.placeId?.let { mapViewModel.fetchPlaceDetailsPhotos(it) }
        court.copy(photoReferences = photoReferences ?: court.photoReferences)
    }

    onCourtsFetched(courtsWithPhotos)
}

//  This displays each court based off the properties of the court
@Composable
fun CourtCard(court: Place, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFA500)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = court.photoReferences?.firstOrNull()?.let {
                    "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=$it&key=${BuildConfig.API_KEY}"
                } ?: "https://via.placeholder.com/400",
                contentDescription = "Court Image",
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = court.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Address: ${court.address ?: "No Address Available"} ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }
    }
}
