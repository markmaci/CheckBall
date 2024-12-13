package com.example.checkball.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.checkball.di.UserProfile
import com.example.checkball.viewmodel.SaveProfileStatus
import com.example.checkball.viewmodel.UserProfileViewModel

@Composable
fun UserProfileForm(
    userProfileViewModel: UserProfileViewModel,
    onSaveClick: (UserProfile) -> Unit,
    userProfile: UserProfile? = null
) {
    var displayName by remember { mutableStateOf(userProfile?.displayName ?: "") }
    var username by remember { mutableStateOf(userProfile?.username ?: "") }
    var location by remember { mutableStateOf(userProfile?.location ?: "") }
    var height by remember { mutableStateOf(userProfile?.height ?: "") }
    var weight by remember { mutableStateOf(userProfile?.weight ?: "") }
    var preferredPosition by remember { mutableStateOf(userProfile?.preferredPosition ?: "") }
    var favoriteCourt by remember { mutableStateOf(userProfile?.favoriteCourt ?: "") }

    val saveProfileStatus by userProfileViewModel.saveProfileStatus

    LaunchedEffect(saveProfileStatus) {
        if (saveProfileStatus is SaveProfileStatus.Success) {
            onSaveClick(UserProfile(displayName, username, location, height, weight, preferredPosition, favoriteCourt))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = preferredPosition,
            onValueChange = { preferredPosition = it },
            label = { Text("Preferred Position") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = favoriteCourt,
            onValueChange = { favoriteCourt = it },
            label = { Text("Favorite Court") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val updatedProfile = UserProfile(
                    displayName = displayName,
                    username = username,
                    location = location,
                    height = height,
                    weight = weight,
                    preferredPosition = preferredPosition,
                    favoriteCourt = favoriteCourt
                )
                userProfileViewModel.saveUserProfile(updatedProfile)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save Profile")
        }

        when (val status = saveProfileStatus) {
            is SaveProfileStatus.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
            is SaveProfileStatus.Failure -> {
                Text(text = "Error: ${status.message}", color = MaterialTheme.colorScheme.error)
            }
            else -> {}
        }
    }
}
