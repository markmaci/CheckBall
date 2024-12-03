package com.example.checkball

data class UserProfile(
    val displayName: String,
    val uid: String,
    val username: String,
    val location: String,
    val height: String,
    val weight: String,
    val preferredPosition: String,
    val favoriteCourt: String,
    val recentStats: RecentStats,
    val badges: List<String>
)

data class RecentStats(
    val wins: Int,
    val losses: Int,
    val pointsScored: Int,
    val assists: Int,
    val rebounds: Int
)
