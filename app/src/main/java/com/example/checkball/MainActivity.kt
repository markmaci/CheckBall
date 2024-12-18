package com.example.checkball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import com.example.checkball.ui.screen.LoginScreen
import com.example.checkball.ui.screen.SignUpScreen
import com.example.checkball.ui.screen.MainScreen
import com.example.checkball.ui.screen.HighlightsScreen
import com.example.checkball.ui.screen.UserProfileScreen
import com.example.checkball.viewmodel.UserProfileViewModel
import com.example.checkball.ui.BottomNavigationBar
import com.example.checkball.ui.screen.MatchHistoryScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val isLoggedIn = auth.currentUser != null
    val startDestination = if (isLoggedIn) "profile" else "login"
    val userId = auth.currentUser?.uid
    val userProfileViewModel: UserProfileViewModel = viewModel()

    Scaffold (
        bottomBar = {
            if (isLoggedIn) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = if (currentDestination == "main") {
                Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            } else {
                Modifier.padding(innerPadding)
            }
        ) {
            composable("login") { LoginScreen(navController) }
            composable("signup") { SignUpScreen(navController) }
            composable("main") { MainScreen() }
            composable("gameDetails") { MatchHistoryScreen(navController) }
            composable("communityFeed") { HighlightsScreen() }

            composable("profile") {
                userId?.let {
                    UserProfileScreen(
                        onViewMatchHistoryClick = { navController.navigate("match_history_screen") },
                        userProfileViewModel = userProfileViewModel,
                        userID = it,
                        navController = navController
                    )
                }
            }
        }
    }
}
