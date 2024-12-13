package com.example.checkball.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.checkball.viewmodel.UserProfileViewModel
import com.example.checkball.di.UserProfile
import com.example.checkball.di.RecentStats
import com.example.checkball.viewmodel.SaveProfileStatus

@Composable
fun UserProfileScreen(
    onViewMatchHistoryClick: () -> Unit,
    userProfileViewModel: UserProfileViewModel,
    onSaveProfile: (UserProfile) -> Unit,
    userID: String
) {
    val saveProfileStatus by userProfileViewModel.saveProfileStatus
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(Unit) {
        userProfileViewModel.getUserProfile { profile ->
            userProfile = profile
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Text(
            text = userProfile?.displayName?.ifEmpty { "No Name" } ?: "Loading...",
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
                text = "@${userProfile?.username?.ifEmpty { "No Username" } ?: "Loading..."}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userProfile?.location?.ifEmpty { "No Location" } ?: "Loading...",
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

        userProfile?.let { PlayerInformationSection(userProfile = it) }

        Spacer(modifier = Modifier.height(24.dp))

        RecentStatsSection(recentStats = userProfile?.recentStats ?: RecentStats())

        Spacer(modifier = Modifier.height(24.dp))

        BadgesSection(badges = userProfile?.badges ?: emptyList())

        Spacer(modifier = Modifier.height(32.dp))

        when (saveProfileStatus) {
            SaveProfileStatus.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color(0xFF00796B)
                )
            }
            SaveProfileStatus.Success -> {
                Text(
                    text = "Profile saved successfully!",
                    color = Color.Green,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is SaveProfileStatus.Failure -> {
                Text(
                    text = "Error saving profile: ${(saveProfileStatus as SaveProfileStatus.Failure).message}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                userProfile?.let { onSaveProfile(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
        ) {
            Text(
                text = "Save Changes",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
            )
        }
    }
}

@Composable
fun PlayerInformationSection(userProfile: UserProfile) {
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
                text = userProfile.height.ifEmpty { "N/A" },
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Height",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = userProfile.weight.ifEmpty { "N/A" },
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Weight",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = userProfile.preferredPosition.ifEmpty { "N/A" },
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Position",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = userProfile.favoriteCourt.ifEmpty { "N/A" },
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Favorite Court",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
            )
        }
    }
}

@Composable
fun RecentStatsSection(recentStats: RecentStats) {
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
                text = "${recentStats.wins}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Wins",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${recentStats.losses}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Losses",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${recentStats.pointsScored}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Points",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${recentStats.assists}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Assists",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${recentStats.rebounds}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Rebounds",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
            )
        }
    }
}

@Composable
fun BadgesSection(badges: List<String>) {
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
        badges.forEach { badge ->
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
}
