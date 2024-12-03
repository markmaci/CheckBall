package com.example.checkball

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("UNCHECKED_CAST")
class UserProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _userProfile = MutableStateFlow(UserProfile("", "", "", "", "", "", "", "", RecentStats(0, 0, 0, 0, 0), emptyList()))

    fun fetchUserData(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    _userProfile.value = UserProfile(
                        displayName = document.getString("displayName") ?: "",
                        uid = document.id,
                        username = document.getString("username") ?: "",
                        location = document.getString("location") ?: "",
                        height = document.getString("height") ?: "",
                        weight = document.getString("weight") ?: "",
                        preferredPosition = document.getString("preferredPosition") ?: "",
                        favoriteCourt = document.getString("favoriteCourt") ?: "",
                        recentStats = RecentStats(
                            wins = document.getLong("wins")?.toInt() ?: 0,
                            losses = document.getLong("losses")?.toInt() ?: 0,
                            pointsScored = document.getLong("pointsScored")?.toInt() ?: 0,
                            assists = document.getLong("assists")?.toInt() ?: 0,
                            rebounds = document.getLong("rebounds")?.toInt() ?: 0
                        ),
                        badges = document.get("badges") as? List<String> ?: emptyList()
                    )
                }
            }
    }
}
