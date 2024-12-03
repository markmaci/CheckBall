package com.example.checkball

data class Match(
    val date: String,
    val opponent: String,
    val result: String,
    val score: String,
    val pointsScored: Int,
    val assists: Int,
    val rebounds: Int
)
