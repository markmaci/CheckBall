package com.example.checkball.viewmodel

import androidx.lifecycle.ViewModel
import com.example.checkball.data.model.Match
import com.example.checkball.data.model.RecentStats
import com.example.checkball.data.model.User
import com.example.checkball.data.repository.MatchRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MatchHistoryViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val matchRepository = MatchRepository()

    private val _matchHistory = MutableStateFlow<List<Match>>(emptyList())
    val matchHistory: StateFlow<List<Match>> = _matchHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchMatchHistory(userId: String) {
        _isLoading.value = true
        firestore.collection("matches")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val matches = documents.mapNotNull { it.toObject(Match::class.java) }
                _matchHistory.value = matches
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    fun addMatch(newMatch: Match, user: User) {
        val calculatedResult = if (newMatch.pointsScored >= newMatch.opponentPointsScored) {
            "Win"
        } else {
            "Loss"
        }

        val matchWithResult = newMatch.copy(result = calculatedResult)

        val updatedMatches = _matchHistory.value.toMutableList().apply {
            add(0, matchWithResult)
            if (size > 10) removeAt(size - 1)
        }

        _matchHistory.value = updatedMatches
        saveMatchToFirestore(matchWithResult, user)

        val updatedStats = RecentStats(
            pointsScored = matchWithResult.pointsScored,
            assists = matchWithResult.assists,
            rebounds = matchWithResult.rebounds
        )
        matchRepository.logGameStats(updatedStats, {}, {})
    }

    private fun saveMatchToFirestore(newMatch: Match, user: User) {
        firestore.collection("matches")
            .add(newMatch.copy(userId = user.uid))
    }
}
