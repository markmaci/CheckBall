package com.example.checkball.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.checkball.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.checkball.UserProfile
import com.example.checkball.RecentStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    userId: String,
    firestore: FirebaseFirestore
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val user by authViewModel.user.collectAsState()

    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        loading = true
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    userProfile = UserProfile(
                        displayName = data?.get("displayName") as? String ?: "Unknown",
                        uid = data?.get("uid") as? String ?: "",
                        username = data?.get("username") as? String ?: "Unknown",
                        location = data?.get("location") as? String ?: "Unknown",
                        height = data?.get("height") as? String ?: "Unknown",
                        weight = data?.get("weight") as? String ?: "Unknown",
                        preferredPosition = data?.get("preferredPosition") as? String ?: "Unknown",
                        favoriteCourt = data?.get("favoriteCourt") as? String ?: "Unknown",
                        recentStats = RecentStats(
                            wins = (data?.get("recentStats_wins") as? Long)?.toInt() ?: 0,
                            losses = (data?.get("recentStats_losses") as? Long)?.toInt() ?: 0,
                            pointsScored = (data?.get("recentStats_pointsScored") as? Long)?.toInt() ?: 0,
                            assists = (data?.get("recentStats_assists") as? Long)?.toInt() ?: 0,
                            rebounds = (data?.get("recentStats_rebounds") as? Long)?.toInt() ?: 0
                        ),
                        badges = (data?.get("badges") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                } else {
                    error = "User data not found."
                }
                loading = false
            }
            .addOnFailureListener { exception ->
                error = "Error fetching user data: ${exception.localizedMessage}"
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Main Screen") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome, ${user?.email ?: "User"}!",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (loading) {
                    CircularProgressIndicator()
                } else if (error != null) {
                    Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                } else {
                    userProfile?.let { profile ->
                        Text(text = "Display Name: ${profile.displayName}")
                        Text(text = "Username: ${profile.username}")
                        Text(text = "Location: ${profile.location}")
                        Text(text = "Height: ${profile.height}")
                        Text(text = "Weight: ${profile.weight}")
                        Text(text = "Preferred Position: ${profile.preferredPosition}")
                        Text(text = "Favorite Court: ${profile.favoriteCourt}")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Recent Stats:")
                        Text(text = "Wins: ${profile.recentStats.wins}")
                        Text(text = "Losses: ${profile.recentStats.losses}")
                        Text(text = "Points Scored: ${profile.recentStats.pointsScored}")
                        Text(text = "Assists: ${profile.recentStats.assists}")
                        Text(text = "Rebounds: ${profile.recentStats.rebounds}")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Badges: ${profile.badges.joinToString(", ")}")
                    } ?: run {
                        Text(text = "No user data available")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }) {
                    Text("Log Out")
                }
            }
        }
    )
}
