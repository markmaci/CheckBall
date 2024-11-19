package com.example.checkball

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val userProfile = mutableStateOf(UserProfile())
    val isLoading = mutableStateOf(true)
    val errorMessage = mutableStateOf<String?>(null)

    fun fetchUserData(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    userProfile.value = document.toObject(UserProfile::class.java) ?: UserProfile()
                    isLoading.value = false
                }
            }
            .addOnFailureListener { exception ->
                isLoading.value = false
                errorMessage.value = "Error getting document: ${exception.localizedMessage}"
            }
    }
}
