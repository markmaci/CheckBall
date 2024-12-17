package com.example.checkball.data.model

data class Match(
    val userId: String = "",
    val date: String = "",
    val opponent: String = "",
    val result: String = "",
    val pointsScored: Int = 0,
    val opponentPointsScored: Int = 0,
    val assists: Int = 0,
    val rebounds: Int = 0
)

