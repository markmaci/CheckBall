package com.example.checkball.di

import com.example.checkball.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun saveUserProfile(userProfile: User) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .set(userProfile)
                .await()
        }
    }

    suspend fun getUserProfile(): User? {
        val userId = auth.currentUser?.uid
        return if (userId != null) {
            val document = db.collection("users").document(userId).get().await()
            document.toObject(User::class.java)
        } else {
            null
        }
    }
}
