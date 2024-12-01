package com.example.checkball

data class UserProfile(
    val displayName: String = "John Doe",
    val username: String = "12345",
    val badges: List<String> = listOf("MVP", "Best Shooter", "Top Scorer")
)
