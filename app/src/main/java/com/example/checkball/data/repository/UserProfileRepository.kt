package com.example.checkball.data.repository

import com.example.checkball.data.model.UserProfile
import com.example.checkball.di.FirestoreService

class UserProfileRepository(private val firestoreService: FirestoreService) {

    suspend fun saveUserProfile(userProfile: UserProfile) {
        firestoreService.saveUserProfile(userProfile)
    }

    suspend fun getUserProfile(): UserProfile? {
        return firestoreService.getUserProfile()
    }
}
