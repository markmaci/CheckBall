package com.example.checkball

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserProfileViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> get() = _userProfile

    fun fetchUserProfile() {
        val userId = "12345"
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    _userProfile.value = document.toObject(UserProfile::class.java) ?: UserProfile()
                }
            }
            .addOnFailureListener {
                _userProfile.value = UserProfile()
            }
    }
}
