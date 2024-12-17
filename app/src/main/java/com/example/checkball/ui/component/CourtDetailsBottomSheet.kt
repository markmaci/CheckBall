package com.example.checkball.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.checkball.BuildConfig
import com.example.checkball.R
import com.example.checkball.ui.screen.openDirections
import com.example.checkball.ui.screen.shimmerBrush
import com.example.checkball.viewmodel.Place
import com.example.checkball.viewmodel.distanceInMeters
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtDetailsBottomSheet(
    court: Place,
    onDismiss: () -> Unit,
    currentUserUid: String,
    userLocation: LatLng?,
    onIGotNextClick: suspend () -> Unit,
    usersAtPark: List<String>
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isOpen = court.openingHours?.openNow == true

    val photoUrls = remember(court) {
        court.photoReferences?.take(10)?.map { photoRef ->
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photo_reference=$photoRef&key=${BuildConfig.API_KEY}"
        } ?: emptyList()
    }

    LaunchedEffect(photoUrls) {
        Log.d("CourtDetailsBottomSheet", "photoUrls size: ${photoUrls.size}")
        photoUrls.forEachIndexed { i, url ->
            Log.d("CourtDetailsBottomSheet", "photoUrls[$i]: $url")
        }
    }

    val distanceMeters = remember(userLocation, court) {
        if (userLocation != null) {
            distanceInMeters(userLocation, court.location)
        } else {
            null
        }
    }

    val (iconRes, etaText) = remember(distanceMeters) {
        if (distanceMeters == null) {
            R.drawable.car_icon to "Directions"
        } else {
            val distanceKm = distanceMeters / 1000.0
            if (distanceKm < 1.5) {
                val mins = (distanceKm * 12).toInt()
                R.drawable.walking_icon to "${mins} min Walk"
            } else {
                val mins = (distanceKm * 2).toInt()
                R.drawable.car_icon to "${mins} min Drive"
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color(0xFFF2EFDE),
        scrimColor = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = court.name,
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            court.address?.let { address ->
                Text(
                    text = "Address: $address",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            court.rating?.let { rating ->
                val reviewCount = court.userRatingsTotal ?: 0
                Text(
                    text = "Rating: %.1f (%d reviews)".format(rating, reviewCount),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            court.openingHours?.let { hours ->
                val pillColor = if (isOpen) Color(0xFF4CAF50) else Color(0xFFF44336)
                val statusText = if (isOpen) "Open" else "Closed"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = pillColor,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "per GoogleMaps",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                hours.weekdayText?.forEach { dayHours ->
                    Text(
                        text = dayHours,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (photoUrls.isNotEmpty()) {
                Text(
                    text = "Photos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photoUrls) { photoUrl ->
                        var isImageLoading by remember { mutableStateOf(true) }

                        Box(
                            modifier = Modifier
                                .height(250.dp)
                                .width(250.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(shimmerBrush(showShimmer = isImageLoading, targetValue = 1300f))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(photoUrl)
                                    .build(),
                                contentDescription = "Court Photo",
                                contentScale = ContentScale.Crop,
                                onSuccess = { isImageLoading = false },
                                onError = {
                                    isImageLoading = false
                                    Log.e("CourtDetailsBottomSheet", "Failed to load image: $photoUrl")
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Text(
                    text = "No photos available",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { openDirections(context, court.location) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(etaText, color = Color.White)
                }

                Button(
                    onClick = {
                        scope.launch { onIGotNextClick() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.i_got_next_icon),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("I Got Next", color = Color.White)
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Players at the Court:",
                style = MaterialTheme.typography.titleMedium
            )
            if (usersAtPark.isEmpty()) {
                Text(
                    text = "No one yet!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    usersAtPark.forEach { username ->
                        Text(
                            text = username,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
