package com.example.checkball.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.checkball.viewmodel.Place
import com.example.checkball.BuildConfig
import com.example.checkball.R

@Composable
fun CourtDetailsScreen(
    court: Place?,
    onDismiss: () -> Unit
) {
    if (court == null) return

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn(animationSpec = tween(500)) +
                androidx.compose.animation.expandVertically(animationSpec = tween(500)),
        exit = androidx.compose.animation.fadeOut(animationSpec = tween(300)) +
                androidx.compose.animation.shrinkVertically(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2EFDE)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(0.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!court.photoReferences.isNullOrEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            items(court.photoReferences) { photoReference ->
                                AsyncImage(
                                    model = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photo_reference=$photoReference&key=${BuildConfig.API_KEY}",
                                    contentDescription = "Photo of ${court.name}",
                                    modifier = Modifier
                                        .width(300.dp)
                                        .height(200.dp)
                                        .background(Color.Gray)
                                        .padding(8.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No photos available",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = lacquierRegular,
                                color = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text(
                        text = court.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = lacquierRegular,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = "Address: ${court.address ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = lacquierRegular,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    court.phoneNumber?.let {
                        Text(
                            text = "Phone: $it",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = lacquierRegular,
                                color = Color.Black
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    court.website?.let {
                        Text(
                            text = "Website: $it",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = lacquierRegular,
                                color = Color.Black
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    court.rating?.let {
                        Text(
                            text = "Rating: ${String.format("%.1f", it)} (${court.userRatingsTotal ?: 0} reviews)",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = lacquierRegular,
                                color = Color.Black
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    court.openingHours?.let { hours ->
                        Text(
                            text = "Hours: ${if (hours.openNow) "Open Now" else "Closed"}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = lacquierRegular,
                                color = Color.Black
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        hours.weekdayText?.forEach { dayHours ->
                            Text(
                                text = dayHours,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = lacquierRegular,
                                    color = Color.Black
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            isVisible = false
                            onDismiss()
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            "Close",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = lacquierRegular
                            )
                        )
                    }
                }
            }
        }
    }
}


