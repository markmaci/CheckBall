package com.example.checkball

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val badges: List<String> = emptyList()
)
