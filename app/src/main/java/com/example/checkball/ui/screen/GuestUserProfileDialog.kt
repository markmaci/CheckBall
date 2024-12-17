package com.example.checkball.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.checkball.viewmodel.UserProfileViewModel
import com.example.checkball.data.model.User
import com.example.checkball.data.model.RecentStats
import com.example.checkball.viewmodel.SaveProfileStatus
import androidx.compose.ui.window.Dialog

@Composable
fun GuestUserProfileDialog(
    userId: String,
    onDismiss: () -> Unit,
    userProfileViewModel: UserProfileViewModel
) {
    var guestProfile by remember { mutableStateOf<User?>(null) }
    var loadingStatus by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        userProfileViewModel.getUserProfile { profile ->
            if (profile != null) {
                guestProfile = profile
            } else {
                errorMessage = "Failed to load user profile."
            }
            loadingStatus = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(min = 400.dp, max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Player Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (loadingStatus) {
                    CircularProgressIndicator(color = Color(0xFF00796B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading profile...", style = MaterialTheme.typography.bodyMedium)
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "An unknown error occurred",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Red),
                        textAlign = TextAlign.Center
                    )
                } else {
                    guestProfile?.let { user ->
                        Text(
                            text = user.displayName.ifEmpty { "No Name" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(100.dp),
                            tint = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "@${user.username.ifEmpty { "Unknown" }}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PlayerInformationSection(userProfile = user)

                        Spacer(modifier = Modifier.height(16.dp))

                        RecentStatsSection(recentStats = user.recentStats ?: RecentStats())
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}
