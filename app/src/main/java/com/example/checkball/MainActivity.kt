package com.example.checkball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.checkball.ui.screen.LoginScreen
import com.example.checkball.ui.screen.SignUpScreen
import com.example.checkball.ui.screen.MainScreen
import com.example.checkball.ui.theme.CheckBallTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    val matchHistoryViewModel: MatchHistoryViewModel = viewModel()

    CheckBallTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("login") { LoginScreen(navController) }
                composable("signup") { SignUpScreen(navController) }
                composable("main") { MainScreen(navController) }
                composable("user_profile_screen") {
                    UserProfileScreen(
                        onViewMatchHistoryClick = {
                            navController.navigate("match_history_screen")
                        },
                    )
                }
                composable("match_history_screen") {
                    MatchHistoryScreen(
                        onBackClick = { navController.popBackStack() },
                        matchHistoryViewModel = matchHistoryViewModel
                    )
                }
            }
        }
    }
}
