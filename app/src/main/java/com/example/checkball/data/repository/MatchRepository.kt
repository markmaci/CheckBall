package com.example.checkball.data.repository

import com.example.checkball.data.model.RecentStats
import com.google.firebase.firestore.FirebaseFirestore

class MatchRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun logGameStats(stats: RecentStats, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val statsRef = firestore.collection("game_stats").document()

        statsRef.set(stats)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
