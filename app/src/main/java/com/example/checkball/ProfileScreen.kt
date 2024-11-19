package com.example.checkball

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(userProfileViewModel: UserProfileViewModel, modifier: Modifier = Modifier) {
    val userProfile = userProfileViewModel.userProfile.value
    val isLoading = userProfileViewModel.isLoading.value
    val errorMessage = userProfileViewModel.errorMessage.value

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (isLoading) {
            Text(text = "Loading...")
        } else if (errorMessage != null) {
            Text(text = "Error: $errorMessage")
        } else {
            Text(text = "Display Name: ${userProfile.displayName}")
            Text(text = "Email: ${userProfile.email}")
            Text(text = "UID: ${userProfile.uid}")
            Text(text = "Badges: ${userProfile.badges.joinToString()}")
        }
    }
}
