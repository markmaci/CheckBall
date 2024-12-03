package com.example.checkball

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

@Composable
fun UserProfileScreen(
    onViewMatchHistoryClick: () -> Unit,
    userProfileViewModel: UserProfileViewModel
) {
    val userProfile = userProfileViewModel.userProfile.collectAsState().value

    LaunchedEffect(Unit) {
        userProfileViewModel.fetchUserProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Text(
            text = userProfile.displayName,
            style = MaterialTheme.typography.titleLarge.copy(color = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
                .background(Color(0xFFF2F2F2), shape = RoundedCornerShape(16.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "@${userProfile.username}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userProfile.location,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onViewMatchHistoryClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00))
        ) {
            Text(
                text = "View Match History",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Player Information",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = userProfile.height,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Height",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = userProfile.weight,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = userProfile.preferredPosition,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Position",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = userProfile.favoriteCourt,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Favorite Court",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Recent Stats",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${userProfile.recentStats.wins}",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Wins",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${userProfile.recentStats.losses}",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Losses",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${userProfile.recentStats.pointsScored}",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Points",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${userProfile.recentStats.assists}",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Assists",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${userProfile.recentStats.rebounds}",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Rebounds",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Badges",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            userProfile.badges.forEach { badge ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFF6F00), shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = badge,
                        style = TextStyle(color = Color.White),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
