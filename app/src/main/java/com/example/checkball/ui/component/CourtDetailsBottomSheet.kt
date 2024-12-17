package com.example.checkball.ui.component

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.checkball.BuildConfig
import com.example.checkball.R
import com.example.checkball.ui.screen.shimmerBrush
import com.example.checkball.viewmodel.Place
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtDetailsBottomSheet(
    court: Place,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

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

            // Open/Closed Indicator and Hours
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
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
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
                                model = ImageRequest.Builder(LocalContext.current)
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

            // Placeholder text for future features
            Text(
                text = "More details coming soon...",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))
            // In the future: Add "Get Directions" and "I Got Next" button here
        }
    }
}