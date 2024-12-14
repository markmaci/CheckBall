package com.example.checkball.data.model

data class Match(
    val date: String = "",
    val opponent: String = "",
    val result: String = "",
    val score: String = "",
    val pointsScored: Int = 0,
    val assists: Int = 0,
    val rebounds: Int = 0
)
