package com.example.checkball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import com.example.checkball.ui.BottomNavigationBar
import com.example.checkball.ui.screen.LoginScreen
import com.example.checkball.ui.screen.SignUpScreen
import com.example.checkball.ui.screen.MainScreen
import com.example.checkball.ui.screen.HistoryScreen
import com.example.checkball.ui.screen.HighlightsScreen
import com.example.checkball.ui.screen.ProfileScreen

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
    val startDestination = if (isLoggedIn) "main" else "login"

    Scaffold(
//        bottomBar = {
//            if (isLoggedIn) {
//                BottomNavigationBar(navController)
//            }
//        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("signup") { SignUpScreen(navController) }

            composable("main") { MainScreen(navController) }
            composable("gameDetails") { HistoryScreen(navController) }
            composable("communityFeed") { HighlightsScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
        }
    }
}