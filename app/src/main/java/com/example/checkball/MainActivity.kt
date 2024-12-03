package com.example.checkball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.checkball.ui.theme.CheckBallTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private val userProfileViewModel = UserProfileViewModel()
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        enableEdgeToEdge()
        userProfileViewModel.fetchUserData("12345")

        setContent {
            CheckBallTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "user_profile_screen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("user_profile_screen") {
                            UserProfileScreen(
                                onViewMatchHistoryClick = {
                                    navController.navigate("match_history_screen")
                                }
                            )
                        }
                        composable("match_history_screen") {
                            val matchHistoryViewModel: MatchHistoryViewModel = viewModel()
                            MatchHistoryScreen(
                                onBackClick = { navController.popBackStack() },
                                matchHistoryViewModel = matchHistoryViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
