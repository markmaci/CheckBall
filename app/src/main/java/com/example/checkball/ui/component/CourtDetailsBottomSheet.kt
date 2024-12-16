package com.example.checkball.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
                .heightIn(min = 450.dp)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = court.name,
                style = MaterialTheme.typography.headlineLarge
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
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                        Text(
                            text = "per GoogleMaps",
                            style = MaterialTheme.typography.bodySmall
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

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "More details coming soon...",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}
