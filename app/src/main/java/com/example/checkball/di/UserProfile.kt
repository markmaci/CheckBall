package com.example.checkball.di

data class UserProfile(
    val displayName: String = "",
    val uid: String = "",
    val username: String = "",
    val location: String = "",
    val height: String = "",
    val weight: String = "",
    val preferredPosition: String = "",
    val favoriteCourt: String = "",
    val recentStats: RecentStats = RecentStats(),
    val badges: List<String> = emptyList()
)

data class RecentStats(
    val wins: Int = 0,
    val losses: Int = 0,
    val pointsScored: Int = 0,
    val assists: Int = 0,
    val rebounds: Int = 0
)
