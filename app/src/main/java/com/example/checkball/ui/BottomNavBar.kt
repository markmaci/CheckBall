package com.example.checkball.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.checkball.R

val lacquierRegular = FontFamily(
    Font(R.font.lacquerregular, FontWeight.Normal)
)

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("main", Icons.Default.Home, "Home")
    object History : BottomNavItem("gameDetails", Icons.Default.Info, "History")
    object CommunityFeed : BottomNavItem("communityFeed", Icons.Default.List, "Highlights")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.CommunityFeed,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFFF2EFDE),
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 14.sp,
                            fontFamily = lacquierRegular
                        )
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF000000),
                    unselectedIconColor = Color(0xFF757575),
                    selectedTextColor = Color(0xFF000000),
                    unselectedTextColor = Color(0xFF757575),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
