package com.example.checkball

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.checkball.ui.screen.MainScreen
import com.example.checkball.ui.screen.LoginScreen
import com.example.checkball.ui.screen.SignUpScreen
import com.example.checkball.ui.screen.MatchHistoryScreen
import com.example.checkball.ui.screen.HighlightsScreen
import com.example.checkball.ui.screen.UserProfileScreen
import com.example.checkball.viewmodel.MatchHistoryViewModel
import com.example.checkball.viewmodel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val matchHistoryViewModel: MatchHistoryViewModel = viewModel()
    val userProfileViewModel: UserProfileViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        composable("main") {
            userId?.let {
                MainScreen(navController = navController, userId = it, firestore = firestore)
            }
        }
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("signup") {
            SignUpScreen(navController = navController)
        }
        composable("user_profile_screen") {
            userId?.let {
                UserProfileScreen(
                    onViewMatchHistoryClick = { navController.navigate("match_history_screen") },
                    userProfileViewModel = userProfileViewModel,
                    userID = it,
                    onSaveProfile = { /* Placeholder lambda for on-save functionality */ }
                )
            }
        }
        composable("match_history_screen") {
            userId?.let {
                MatchHistoryScreen(
                    userId = it,
                    onBackClick = { navController.popBackStack() },
                    matchHistoryViewModel = matchHistoryViewModel
                )
            }
        }
        composable("communityFeed") {
            HighlightsScreen(navController = navController)
        }
    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    LaunchedEffect(currentUser) {
        delay(2000)
        if (currentUser != null) {
            navController.navigate("main") { popUpTo("splash") { inclusive = true } }
        } else {
            navController.navigate("login") { popUpTo("splash") { inclusive = true } }
        }
    }
}
