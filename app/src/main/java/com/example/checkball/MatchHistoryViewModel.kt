package com.example.checkball

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MatchHistoryViewModel : ViewModel() {
    private val _matchHistory = MutableStateFlow<List<Match>>(emptyList())
    val matchHistory: StateFlow<List<Match>> = _matchHistory

    init {
        _matchHistory.value = listOf(
            Match(
                opponent = "Team A",
                score = "100-90",
                date = "2024-11-01",
                result = "Win",
                pointsScored = 20,
                assists = 5,
                rebounds = 10
            ),
            Match(
                opponent = "Team B",
                score = "80-95",
                date = "2024-11-05",
                result = "Loss",
                pointsScored = 18,
                assists = 7,
                rebounds = 12
            )
        )
    }
}
