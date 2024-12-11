package com.example.checkball.viewmodel

import androidx.lifecycle.ViewModel
import com.example.checkball.di.Match
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MatchHistoryViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

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
}
