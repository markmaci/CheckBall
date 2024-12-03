package com.example.checkball.data.model

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val badges: List<String> = emptyList()
)