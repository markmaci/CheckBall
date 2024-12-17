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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.checkball.viewmodel.UserProfileViewModel
import com.example.checkball.data.model.User

import com.example.checkball.data.model.RecentStats
import com.example.checkball.viewmodel.SaveProfileStatus

@Composable
fun UserProfileScreen(
    onViewMatchHistoryClick: () -> Unit,
    userProfileViewModel: UserProfileViewModel,
    userID: String,
    navController: NavHostController
) {
    val saveProfileStatus by userProfileViewModel.saveProfileStatus
    val currentUser by userProfileViewModel.currentUser
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userProfileViewModel.getUserProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Text(
            text = currentUser?.displayName?.ifEmpty { "No Name" } ?: "Loading...",
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
                text = "@${currentUser?.username?.ifEmpty { "No Username" } ?: "Loading..."}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentUser?.location?.ifEmpty { "No Location" } ?: "Loading...",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00))
        ) {
            Text(
                text = "Modify User Profile",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        currentUser?.let { PlayerInformationSection(userProfile = it) }

        Spacer(modifier = Modifier.height(24.dp))

        RecentStatsSection(recentStats = currentUser?.recentStats ?: RecentStats())

        Spacer(modifier = Modifier.height(24.dp))

        BadgesSection(badges = currentUser?.badges ?: emptyList())

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
    }

    if (showDialog) {
        ModifyProfileDialog(
            onDismiss = { showDialog = false },
            userProfileViewModel = userProfileViewModel,
            userProfile = currentUser ?: User()
        )
    }
}

@Composable
fun ModifyProfileDialog(
    onDismiss: () -> Unit,
    userProfileViewModel: UserProfileViewModel,
    userProfile: User
) {
    var displayName by remember { mutableStateOf(userProfile.displayName) }
    var username by remember { mutableStateOf(userProfile.username) }
    var location by remember { mutableStateOf(userProfile.location) }
    var height by remember { mutableStateOf(userProfile.height) }
    var weight by remember { mutableStateOf(userProfile.weight) }
    var preferredPosition by remember { mutableStateOf(userProfile.preferredPosition) }
    var favoriteCourt by remember { mutableStateOf(userProfile.favoriteCourt) }
    val saveStatus by remember { mutableStateOf<SaveProfileStatus?>(null) }

    val saveProfile = {
        userProfileViewModel.saveUserProfile(
            userProfile.copy(
                displayName = displayName,
                username = username,
                location = location,
                height = height,
                weight = weight,
                preferredPosition = preferredPosition,
                favoriteCourt = favoriteCourt
            )
        )
    }

    LaunchedEffect(saveStatus) {
        if (saveStatus is SaveProfileStatus.Success) onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Modify Profile") },
        text = {
            Column {
                OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("Display Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = preferredPosition, onValueChange = { preferredPosition = it }, label = { Text("Preferred Position") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = favoriteCourt, onValueChange = { favoriteCourt = it }, label = { Text("Favorite Court") }, modifier = Modifier.fillMaxWidth())

                when (saveStatus) {
                    SaveProfileStatus.Loading -> { CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally)) }
                    is SaveProfileStatus.Success -> { Text(text = "Profile saved successfully!", color = Color.Green, modifier = Modifier.align(Alignment.CenterHorizontally)) }
                    is SaveProfileStatus.Failure -> { Text(text = "Error: ${(saveStatus as SaveProfileStatus.Failure).message}", color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally)) }
                    else -> {}
                }
            }
        },
        confirmButton = { TextButton(onClick = saveProfile) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun PlayerInformationSection(userProfile: User) {
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

    if (badges.isNotEmpty()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            badges.forEach {
                BadgeBox(it)
            }
        }
    } else {
        Text(
            text = "No badges yet",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
        )
    }
}

@Composable
fun BadgeBox(badge: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .wrapContentSize()
    ) {
        Text(
            text = badge,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
